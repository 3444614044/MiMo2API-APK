# MiMo2API · Android APK

把 [MiMo2API](https://github.com/Water008/MiMo2API)（小米 MiMo → OpenAI / Anthropic 兼容 API）打包成 Android 应用：
内嵌 **Chaquopy Python 3.12 运行时**，在手机上以**前台服务**运行 uvicorn，本机即可提供
`/v1/chat/completions`、`/v1/messages` 等端点。

[![Android CI](https://github.com/3444614044/MiMo2API-APK/actions/workflows/android.yml/badge.svg)](https://github.com/3444614044/MiMo2API-APK/actions/workflows/android.yml)

## 使用

1. 安装 APK，打开 → 选端口（默认 8080）→ **启动服务**。
2. **管理页面** 里配置 MiMo 账号（service_token / user_id / xiaomichatbot_ph）与 API Key（默认 `sk-mimo`）。
3. 在任意客户端把 API Base 指到 `http://127.0.0.1:8080/v1`（局域网访问把 127.0.0.1 换成手机 IP）。
4. 服务以前台服务运行，退出 App 也保持在线；通知栏点按可回到管理页。

## 打包要点（与原项目的差异）

| 项 | 处理 |
|---|---|
| Python 运行时 | Chaquopy 16.0 / Python 3.12，全部依赖为纯 Python wheel，无本地编译 |
| pydantic | 钉在 **1.10.21**（纯 Python wheel；pydantic-core 无 Android 构建）。本项目代码本就 v1 兼容（唯一的 `model_dump` 在注释里） |
| cryptography | 无 Android 构建 → `config.py` 做降级：缺失时明文落盘（与旧明文配置格式兼容） |
| 可写路径 | APK 内源码只读：config/sessions/usage/responses/批次数据全部改写到应用私有目录（`MIMO_DATA_DIR`），由 `app/paths.py` 统一提供 |
| 启动入口 | 新增 `mobile_main.start(data_dir, port)`，先设环境变量再 import 业务模块 |

## GitHub Actions 构建

push 即构建（JDK 17 + Gradle 8.9 → `assembleRelease`），产物在 Actions 的 **Artifacts**（`MiMo2API-apk`）；
打 `v*` 标签会自动创建 Release 附带 APK。

签名（可选， Secrets）：`KEYSTORE_BASE64` / `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD`；
不配置则用 debug 签名，产物仍可直接安装。
