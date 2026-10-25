"""
云农智诊 AI 服务（与 ai-service 同源逻辑）
权重：HTTP 直连 hf-mirror，不使用 huggingface_hub / hf-xet。
"""

import os

os.environ["HF_HUB_DISABLE_XET"] = "1"
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"
os.environ["HF_HUB_ENABLE_HF_TRANSFER"] = "0"

import io
import json
import logging
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Optional

import torch
import timm
from fastapi import FastAPI, File, UploadFile, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image
from safetensors.torch import load_file
from torchvision import transforms

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("yunnong-ai")

REPO_ID = "Arko007/nfnet-f1-plant-disease"
HF_MIRROR = os.environ.get("HF_ENDPOINT", "https://hf-mirror.com").rstrip("/")
MODEL_CACHE_DIR = (
    Path(os.environ.get("HF_HOME") or "/tmp/hf_cache")
    / "http_models"
    / REPO_ID.replace("/", "__")
)

TRANSFORM = transforms.Compose(
    [
        transforms.Resize((512, 512)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.5, 0.5, 0.5], std=[0.5, 0.5, 0.5]),
    ]
)

model = None
class_names = []
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
_load_error = None

app = FastAPI(title="云农智诊 AI 服务", version="1.2.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def download_hf_file(filename: str, max_retries: int = 3) -> str:
    MODEL_CACHE_DIR.mkdir(parents=True, exist_ok=True)
    dest = MODEL_CACHE_DIR / filename
    if dest.is_file() and dest.stat().st_size > 1024:
        return str(dest)

    urls = [
        f"{HF_MIRROR}/{REPO_ID}/resolve/main/{filename}",
        f"{HF_MIRROR}/{REPO_ID}/resolve/main/{filename}?download=true",
        f"https://huggingface.co/{REPO_ID}/resolve/main/{filename}",
    ]
    tmp = dest.with_suffix(dest.suffix + ".part")
    last_err = None
    for attempt in range(1, max_retries + 1):
        for url in urls:
            try:
                logger.info("HTTP 下载 [%d/%d] %s", attempt, max_retries, url)
                req = urllib.request.Request(url, headers={"User-Agent": "yunnong-ai/1.2"})
                with urllib.request.urlopen(req, timeout=300) as resp:
                    total = 0
                    with open(tmp, "wb") as f:
                        while True:
                            chunk = resp.read(1024 * 1024)
                            if not chunk:
                                break
                            f.write(chunk)
                            total += len(chunk)
                if total < 64:
                    raise RuntimeError("内容过小")
                tmp.replace(dest)
                return str(dest)
            except Exception as e:
                last_err = e
                logger.warning("失败 %s: %s", url, e)
                if tmp.exists():
                    try:
                        tmp.unlink()
                    except OSError:
                        pass
        time.sleep(2 * attempt)
    raise RuntimeError(f"HTTP 下载失败 {filename}: {last_err}")


def load_model() -> None:
    global model, class_names, _load_error
    try:
        model_path = download_hf_file("model.safetensors")
        config_path = download_hf_file("config.json")
        with open(config_path, "r", encoding="utf-8") as f:
            config = json.load(f)
        class_names = list(config["class_names"])
        built = timm.create_model(
            config.get("architecture", "nfnet_f1"),
            pretrained=False,
            num_classes=int(config.get("num_classes", len(class_names))),
        )
        built.load_state_dict(load_file(model_path))
        built.to(device)
        built.eval()
        model = built
        _load_error = None
        logger.info("模型就绪 classes=%d", len(class_names))
    except Exception as e:
        _load_error = str(e)
        logger.exception("加载失败: %s", e)


@app.on_event("startup")
async def startup():
    load_model()


@app.get("/health")
async def health():
    ready = model is not None and len(class_names) > 0
    return {
        "status": "ok" if ready else "error",
        "model_ready": ready,
        "download_mode": "http-mirror-only",
        "load_error": _load_error,
    }


@app.post("/predict")
async def predict(request: Request, file: UploadFile = File(None)):
    if model is None or not class_names:
        raise HTTPException(status_code=503, detail=_load_error or "模型未就绪")

    ctype = (request.headers.get("content-type") or "").lower()
    crop = None
    if "application/json" in ctype:
        body = await request.json()
        url = (body or {}).get("image_url")
        crop = (body or {}).get("crop")
        if not url:
            raise HTTPException(status_code=400, detail="需要 image_url")
        req = urllib.request.Request(url, headers={"User-Agent": "yunnong-ai/1.2"})
        with urllib.request.urlopen(req, timeout=60) as resp:
            raw = resp.read()
        image = Image.open(io.BytesIO(raw)).convert("RGB")
    else:
        upload = file
        if upload is None:
            form = await request.form()
            upload = form.get("file")
        if upload is None:
            raise HTTPException(status_code=400, detail="需要 file 或 image_url")
        raw = await upload.read()
        image = Image.open(io.BytesIO(raw)).convert("RGB")

    x = TRANSFORM(image).unsqueeze(0).to(device)
    with torch.no_grad():
        probs = torch.softmax(model(x), dim=1).squeeze(0)

    if crop:
        prefix = f"{crop}__"
        best_i, best_p = None, -1.0
        for i, name in enumerate(class_names):
            if name.startswith(prefix):
                p = float(probs[i].item())
                if p > best_p:
                    best_i, best_p = i, p
        if best_i is not None:
            d = class_names[best_i]
            return {"disease": d, "disease_key": d, "confidence": round(best_p, 4)}

    conf, idx = torch.max(probs, dim=0)
    d = class_names[int(idx.item())]
    return {"disease": d, "disease_key": d, "confidence": round(float(conf.item()), 4)}
