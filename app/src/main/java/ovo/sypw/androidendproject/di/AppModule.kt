package ovo.sypw.androidendproject.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ovo.sypw.androidendproject.data.remote.AIService
import ovo.sypw.androidendproject.data.remote.ApiService
import ovo.sypw.androidendproject.data.remote.RetrofitClient
import ovo.sypw.androidendproject.data.repository.ChartRepository
import ovo.sypw.androidendproject.data.repository.ChatRepository
import ovo.sypw.androidendproject.data.repository.NewsRepository
import ovo.sypw.androidendproject.data.repository.UserRepository
import ovo.sypw.androidendproject.data.repository.VideoRepository
import ovo.sypw.androidendproject.ui.screens.chart.ChartViewModel
import ovo.sypw.androidendproject.ui.screens.chat.ChatViewModel
import ovo.sypw.androidendproject.ui.screens.home.HomeViewModel
import ovo.sypw.androidendproject.ui.screens.login.LoginViewModel
import ovo.sypw.androidendproject.ui.screens.me.MeViewModel
import ovo.sypw.androidendproject.ui.screens.video.VideoViewModel

val appModule = module {
    // 应用级配置
}

val networkModule = module {
    single { RetrofitClient.okHttpClient }
    single { RetrofitClient.retrofit }
    single { RetrofitClient.retrofit.create(ApiService::class.java) }
    single { AIService(androidContext()) }
}

val repositoryModule = module {
    single { NewsRepository(get(), androidContext()) }
    single { VideoRepository(get()) }
    single { UserRepository() }
    single { ChartRepository(get(), androidContext()) }
    single { ChatRepository(androidContext()) }
}

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::VideoViewModel)
    viewModelOf(::ChartViewModel)
    viewModelOf(::MeViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ChatViewModel)
}
