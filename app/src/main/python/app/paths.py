"""Android 打包适配：可写数据统一放到 MIMO_DATA_DIR（App 私有目录）。

APK 内的 Python 源码只读，所有落盘文件（config/sessions/usage/...）
必须写到应用私有存储。启动器（mobile_main.start）会在 import 任何业务
模块之前设置 MIMO_DATA_DIR 环境变量。
"""
import os
from pathlib import Path

DATA_DIR = Path(os.environ.get("MIMO_DATA_DIR") or (Path(__file__).resolve().parent.parent))
