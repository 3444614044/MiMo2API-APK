package com.mimo2api.app

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvUrl: TextView
    private lateinit var etPort: EditText
    private lateinit var btnToggle: Button
    private lateinit var btnAdmin: Button
    private lateinit var btnCopy: Button

    private val prefs by lazy { getSharedPreferences("mimo2api", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvUrl = findViewById(R.id.tvUrl)
        etPort = findViewById(R.id.etPort)
        btnToggle = findViewById(R.id.btnToggle)
        btnAdmin = findViewById(R.id.btnAdmin)
        btnCopy = findViewById(R.id.btnCopy)

        etPort.setText(prefs.getInt("port", 8080).toString())

        btnToggle.setOnClickListener { toggleServer() }
        btnAdmin.setOnClickListener {
            if (!ServerState.running) {
                toast("请先启动服务")
                return@click
            }
            startActivity(Intent(this, AdminActivity::class.java))
        }
        btnCopy.setOnClickListener {
            val cm = getSystemService(ClipboardManager::class.java)
            cm.setPrimaryClip(ClipData.newPlainText("MiMo2API", ServerState.baseUrl() + "/v1/chat/completions"))
            toast("API 地址已复制")
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun toggleServer() {
        if (ServerState.running) {
            stopService(Intent(this, ServerService::class.java))
            ServerState.running = false
            refresh()
            return
        }
        val port = etPort.text.toString().toIntOrNull()
        if (port == null || port !in 1024..65535) {
            toast("端口需为 1024–65535 的数字")
            return
        }
        prefs.edit().putInt("port", port).apply()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        ContextCompat.startForegroundService(
            this, Intent(this, ServerService::class.java).putExtra(ServerService.EXTRA_PORT, port)
        )
        tvStatus.text = "启动中…（首次加载 Python 运行时约需数秒）"
        btnToggle.isEnabled = false
        window.decorView.postDelayed({ refresh(); btnToggle.isEnabled = true }, 1500)
    }

    private fun refresh() {
        tvStatus.text = if (ServerState.running) "● 运行中" else "○ 未启动"
        tvUrl.text = buildString {
            append(ServerState.baseUrl())
            append("\nOpenAI:  ").append(ServerState.baseUrl()).append("/v1/chat/completions")
            append("\nAnthropic: ").append(ServerState.baseUrl()).append("/v1/messages")
            append("\n管理页:   ").append(ServerState.baseUrl()).append("/")
        }
        btnToggle.text = if (ServerState.running) "停止服务" else "启动服务"
        btnAdmin.isEnabled = ServerState.running
        ServerState.lastError?.let { tvStatus.text = "● 启动失败：$it" }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
