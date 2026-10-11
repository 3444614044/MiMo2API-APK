"""Android 启动入口：设置数据目录与端口后启动 uvicorn 服务。"""


def start(data_dir: str, port: int) -> None:
    import os
    os.environ["MIMO_DATA_DIR"] = data_dir
    os.environ["PORT"] = str(port)
    os.environ.setdefault("HOST", "0.0.0.0")
    os.chdir(data_dir)  # 相对路径兜底
    import main
    main.main()
