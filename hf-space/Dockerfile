# 基于官方精简 Python 3.11 镜像
FROM python:3.11-slim

# 创建 UID=1000 的非 root 用户（Hugging Face Spaces 安全要求）
RUN useradd -m -u 1000 user

# 切换到非 root 用户
USER user

# 设置环境变量：用户主目录、可写缓存路径、禁用 Python 缓冲
ENV HOME=/home/user \
    PATH=/home/user/.local/bin:$PATH \
    HF_HOME=/home/user/.cache/huggingface \
    PYTHONUNBUFFERED=1

# 设置工作目录
WORKDIR /home/user/app

# 先复制依赖清单并安装（利用 Docker 层缓存）
COPY --chown=user requirements.txt .
RUN pip install --no-cache-dir --user -r requirements.txt

# 复制应用代码
COPY --chown=user main.py .

# 暴露 Hugging Face Spaces 要求的端口
EXPOSE 7860

# 启动 FastAPI 服务
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "7860"]
