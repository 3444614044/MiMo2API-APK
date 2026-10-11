package com.mimo2api.app

/** 跨组件共享的服务状态（Service 写、Activity 读）。 */
object ServerState {
    @Volatile var running = false
    @Volatile var port = 8080
    @Volatile var lastError: String? = null

    fun baseUrl() = "http://127.0.0.1:$port"
}
