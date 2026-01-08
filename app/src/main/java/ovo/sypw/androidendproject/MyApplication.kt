package ovo.sypw.androidendproject

import android.app.Application
import com.amap.api.maps.MapsInitializer
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ovo.sypw.androidendproject.ads.AppOpenAdManager
import ovo.sypw.androidendproject.di.appModule
import ovo.sypw.androidendproject.di.networkModule
import ovo.sypw.androidendproject.di.repositoryModule
import ovo.sypw.androidendproject.di.viewModelModule

class MyApplication : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

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

        // 3. 初始化高德地图
        MapsInitializer.initialize(this)
//        MapsInitializer.updatePrivacyShow(this,true,true);
//        MapsInitializer.updatePrivacyAgree(this,true);

        // 4. 初始化 Google Mobile Ads SDK
        MobileAds.initialize(this) { initializationStatus ->
            // 初始化完成后加载开屏广告
            appOpenAdManager = AppOpenAdManager(this)
            appOpenAdManager.loadAd()
        }
    }
}
