plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python")
}

android {
    namespace = "com.mimo2api.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mimo2api.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "2.6.6.4"
    }

    signingConfigs {
        create("release") {
            val storePath = System.getenv("RELEASE_STORE_FILE")
            if (!storePath.isNullOrBlank()) {
                storeFile = file(storePath)
                storePassword = System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: "mimo2api"
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val rc = signingConfigs.getByName("release")
            signingConfig = if (rc.storeFile != null) rc else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// 内嵌 Python 运行时：全部依赖都是纯 Python wheel，无需任何本地编译。
// pydantic 钉在 1.10.x（纯 Python），代码本就 v1 兼容；cryptography 无 Android
// 构建，config.py 已做降级（无它时明文落盘，与旧明文配置兼容）。
chaquopy {
    defaultConfig {
        version = "3.12"
        pip {
            install("fastapi==0.115.5")
            install("uvicorn==0.32.1")
            install("httpx==0.27.2")
            install("pydantic==1.10.21")
            install("python-multipart==0.0.20")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
