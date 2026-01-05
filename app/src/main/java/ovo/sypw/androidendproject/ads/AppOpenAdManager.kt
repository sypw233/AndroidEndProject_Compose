package ovo.sypw.androidendproject.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import ovo.sypw.androidendproject.utils.PreferenceUtils

/**
 * App Open Ad 管理器
 * 在应用从后台切换到前台时显示开屏广告
 * 仅当用户设置启用 Google 广告时生效
 */
class AppOpenAdManager(private val application: Application) : DefaultLifecycleObserver,
    Application.ActivityLifecycleCallbacks {

    companion object {
        private const val TAG = "AppOpenAdManager"

        // Google AdMob 开屏广告测试 ID
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var currentActivity: Activity? = null

    // 是否是首次启动（用于冷启动显示广告）
    private var isFirstLaunch = true

    init {
        // 注册应用生命周期观察者
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        // 注册 Activity 生命周期回调
        application.registerActivityLifecycleCallbacks(this)
    }

    /**
     * 检查是否启用 Google 广告
     */
    private fun isGoogleAdEnabled(): Boolean {
        return PreferenceUtils.useGoogleAd(application)
    }

    /**
     * 检查广告是否可用
     */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && !isShowingAd
    }

    /**
     * 加载开屏广告（仅在启用 Google 广告时）
     */
    fun loadAd() {
        // 如果未启用 Google 广告，不加载
        if (!isGoogleAdEnabled()) {
            Log.d(TAG, "Google 广告未启用，跳过加载")
            isFirstLaunch = false
            return
        }

        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            application,
            AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "开屏广告加载成功")
                    appOpenAd = ad
                    isLoadingAd = false

                    // 首次启动时立即显示广告
                    if (isFirstLaunch) {
                        isFirstLaunch = false
                        showAdIfAvailable()
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "开屏广告加载失败: ${loadAdError.message}")
                    isLoadingAd = false
                    isFirstLaunch = false
                }
            }
        )
    }

    /**
     * 显示开屏广告（如果可用且已启用 Google 广告）
     */
    fun showAdIfAvailable() {
        // 如果未启用 Google 广告，不显示
        if (!isGoogleAdEnabled()) {
            return
        }

        if (isShowingAd) {
            Log.d(TAG, "广告正在显示中")
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "广告不可用，尝试加载")
            loadAd()
            return
        }

        val activity = currentActivity ?: return

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "广告已关闭")
                appOpenAd = null
                isShowingAd = false
                // 广告关闭后预加载下一个广告
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "广告显示失败: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "广告正在显示")
                isShowingAd = true
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }

    // ========== DefaultLifecycleObserver 实现 ==========

    override fun onStart(owner: LifecycleOwner) {
        // 应用从后台切换到前台时显示广告（仅当启用 Google 广告时）
        if (!isFirstLaunch && isGoogleAdEnabled()) {
            showAdIfAvailable()
        }
    }

    // ========== Application.ActivityLifecycleCallbacks 实现 ==========

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // 如果当前没有显示广告，更新当前 Activity
        if (!isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
