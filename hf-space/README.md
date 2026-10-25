---
title: 云农智诊 AI 服务
emoji: 🌿
colorFrom: green
colorTo: blue
sdk: docker
app_port: 7860
pinned: false
license: mit
---

# 云农智诊 AI 服务

基于 FastAPI + timm 的植物病害识别推理服务，部署于 Hugging Face Spaces（Docker SDK）。

## 模型

- 模型仓库：[Arko007/nfnet-f1-plant-disease](https://huggingface.co/Arko007/nfnet-f1-plant-disease)
- 架构：NFNet-F1（timm）
- 类别数：88 种作物病害 / 健康状态

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/health` | 健康检查 |
| `POST` | `/predict` | 上传叶片图片，返回病害名称与置信度 |

### 预测示例

```bash
curl -X POST "https://<your-space>.hf.space/predict" \
  -F "file=@leaf.jpg"
```

返回：

```json
{"disease": "Tomato__late_blight", "confidence": 0.98}
```

## 本地运行

```bash
docker build -t yunnong-ai .
docker run -p 7860:7860 yunnong-ai
```
