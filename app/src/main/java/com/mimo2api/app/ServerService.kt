package com.mimo2api.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chaquo.python.AndroidPlatform
import com.chaquo.python.Python

/** 全局 Context（Chaquopy 启动用）。 */
object AppHolder {
    @Volatile lateinit var context: android.content.Context
}

/**
 * 前台服务：在后台线程里以内嵌 Python 运行时启动 uvicorn（MiMo2API 服务）。
 */
class ServerService : Service() {

    companion object {
        const val EXTRA_PORT = "extra_port"
        const val CHANNEL_ID = "mimo2api_server"

        private val pyLock = Any()
        private var pyStarted = false

        /** Chaquopy 16：首次使用前必须以 AndroidPlatform 启动解释器。 */
        private fun python(): Python = synchronized(pyLock) {
            if (!pyStarted) {
                Python.start(AndroidPlatform(AppHolder.context))
                pyStarted = true
            }
            Python.getInstance()
        }
    }

    private var worker: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppHolder.context = applicationContext
        val port = intent?.getIntExtra(EXTRA_PORT, 8080) ?: 8080
        ServerState.port = port
        startForeground(1, buildNotification(port))
        if (ServerState.running) return START_NOT_STICKY

        ServerState.running = true
        ServerState.lastError = null
        worker = Thread {
            try {
                python().getModule("mobile_main").callAttr("start", filesDir.absolutePath, port)
            } catch (t: Throwable) {
                ServerState.lastError = t.message ?: t.javaClass.simpleName
                ServerState.running = false
                stopSelf()
            }
        }.apply {
            name = "mimo2api-python"
            isDaemon = true
            start()
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(port: Int): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "MiMo2API 服务", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val tap = PendingIntent.getActivity(
            this, 0,
            Intent(this, AdminActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_server)
            .setContentTitle("MiMo2API 运行中")
            .setContentText("http://127.0.0.1:$port · 点按打开管理页面")
            .setContentIntent(tap)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        ServerState.running = false
        // uvicorn.run 阻塞在 daemon 线程里，进程内无平滑退出接口；杀掉解释器线程组即可
        worker?.interrupt()
        super.onDestroy()
    }
}
