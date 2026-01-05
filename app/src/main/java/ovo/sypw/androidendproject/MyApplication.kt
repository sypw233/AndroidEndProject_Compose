package ovo.sypw.androidendproject

import android.app.Application
import com.baidu.mapapi.SDKInitializer
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ovo.sypw.androidendproject.di.appModule
import ovo.sypw.androidendproject.di.networkModule
import ovo.sypw.androidendproject.di.repositoryModule
import ovo.sypw.androidendproject.di.viewModelModule

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. 初始化 Koin
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule, networkModule, repositoryModule, viewModelModule)
        }

        // 2. 初始化 Firebase
        FirebaseApp.initializeApp(this)

        // 3. 初始化百度地图
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(this)
    }
}
