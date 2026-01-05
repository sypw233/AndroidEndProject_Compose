# AI 开发需求文档 - Compose 项目模板

> 本文档为 AI 编码助手提供完整的项目构建指南，基于 Jetpack Compose + Flow + MVVM 架构。

---

## 一、项目初始化

### 1.1 创建项目

使用 Android Studio 创建新项目时选择:
- **Template**: Empty Activity (Compose)
- **Package Name**: `ovo.sypw.androidendproject`
- **Minimum SDK**: API 35 (Android 15)
- **Build Configuration Language**: Kotlin DSL

### 1.2 项目基本配置

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "AndroidEndProject"
include(":app")
```

---

## 二、依赖配置

### 2.1 Version Catalog (gradle/libs.versions.toml)

```toml
[versions]
agp = "8.13.1"
kotlin = "2.2.21"

# AndroidX
coreKtx = "1.17.0"
splashscreen = "1.2.0"
lifecycle = "2.10.0"
navigation = "2.9.6"
activityCompose = "1.12.1"

# Compose
composeBom = "2024.12.00"

# Coroutines
coroutines = "1.10.2"

# Koin
koin = "4.1.1"

# Network
retrofit = "3.0.0"
okhttp = "5.3.2"
gson = "2.13.2"

# Image
coil = "3.3.0"

# Firebase
firebaseBom = "34.6.0"
googleServices = "4.4.4"

# Accompanist
accompanist = "0.34.0"

# Charts
composeCharts = "0.2.1"

# Video
media3 = "1.5.0"

# Baidu Map
baiduMap = "7.6.6"

# Test
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"

[libraries]
# AndroidX Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "splashscreen" }

# Compose BOM
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }

# Compose Integration
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Lifecycle
lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Coroutines
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }

# Koin
koin-bom = { group = "io.insert-koin", name = "koin-bom", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android" }
koin-core = { group = "io.insert-koin", name = "koin-core" }
koin-compose = { group = "io.insert-koin", name = "koin-androidx-compose" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }

# Coil
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-network = { group = "io.coil-kt.coil3", name = "coil-network-okhttp", version.ref = "coil" }

# Firebase
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }

# Accompanist
accompanist-pager = { group = "com.google.accompanist", name = "accompanist-pager", version.ref = "accompanist" }
accompanist-pager-indicators = { group = "com.google.accompanist", name = "accompanist-pager-indicators", version.ref = "accompanist" }
accompanist-systemui = { group = "com.google.accompanist", name = "accompanist-systemuicontroller", version.ref = "accompanist" }

# Charts
compose-charts = { group = "io.github.bytebeats", name = "compose-charts", version.ref = "composeCharts" }

# Video - Media3
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }

# Baidu Map
baidu-map = { group = "com.baidu.lbsyun", name = "BaiduMapSDK_Map", version.ref = "baiduMap" }
baidu-map-search = { group = "com.baidu.lbsyun", name = "BaiduMapSDK_Search", version.ref = "baiduMap" }
baidu-map-util = { group = "com.baidu.lbsyun", name = "BaiduMapSDK_Util", version.ref = "baiduMap" }

# Test
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

### 2.2 app/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "ovo.sypw.androidendproject"
    compileSdk = 36

    defaultConfig {
        applicationId = "ovo.sypw.androidendproject"
        minSdk = 35
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        manifestPlaceholders["BAIDU_MAP_API_KEY"] = "YOUR_BAIDU_MAP_KEY"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.splashscreen)
    
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.foundation)
    debugImplementation(libs.compose.ui.tooling)
    
    // Compose Integration
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    
    // Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    
    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    
    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    
    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    
    // Accompanist
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.systemui)
    
    // Charts
    implementation(libs.compose.charts)
    
    // Video
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    
    // Baidu Map
    implementation(libs.baidu.map)
    implementation(libs.baidu.map.search)
    implementation(libs.baidu.map.util)
    
    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

---

## 三、包结构创建

请按以下结构创建包和文件:

```
app/src/main/java/ovo/sypw/androidendproject/
├── MyApplication.kt
├── data/
│   ├── model/
│   │   ├── Banner.kt
│   │   ├── News.kt
│   │   ├── User.kt
│   │   ├── Video.kt
│   │   └── ChartData.kt
│   ├── remote/
│   │   ├── ApiService.kt
│   │   ├── RetrofitClient.kt
│   │   └── FirebaseWrapper.kt
│   └── repository/
│       ├── NewsRepository.kt
│       ├── VideoRepository.kt
│       ├── UserRepository.kt
│       └── ChartRepository.kt
├── di/
│   └── AppModule.kt
├── ui/
│   ├── MainActivity.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   ├── Type.kt
│   │   └── Shape.kt
│   ├── navigation/
│   │   ├── Screen.kt
│   │   └── NavGraph.kt
│   ├── components/
│   │   ├── BannerCarousel.kt
│   │   ├── LoadingIndicator.kt
│   │   ├── ErrorView.kt
│   │   ├── NewsItem.kt
│   │   ├── VideoItem.kt
│   │   └── ChartViews.kt
│   └── screens/
│       ├── splash/
│       │   └── SplashScreen.kt
│       ├── intro/
│       │   └── IntroScreen.kt
│       ├── main/
│       │   └── MainScreen.kt
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   ├── HomeViewModel.kt
│       │   └── HomeUiState.kt
│       ├── video/
│       │   ├── VideoScreen.kt
│       │   ├── VideoDetailScreen.kt
│       │   ├── VideoViewModel.kt
│       │   └── VideoUiState.kt
│       ├── chart/
│       │   ├── ChartScreen.kt
│       │   ├── ChartViewModel.kt
│       │   └── ChartUiState.kt
│       ├── me/
│       │   ├── MeScreen.kt
│       │   ├── MeViewModel.kt
│       │   └── MeUiState.kt
│       ├── login/
│       │   ├── LoginScreen.kt
│       │   ├── LoginViewModel.kt
│       │   └── LoginUiState.kt
│       ├── news/
│       │   └── NewsDetailScreen.kt
│       └── map/
│           └── MapScreen.kt
└── utils/
    ├── Extensions.kt
    └── PreferenceUtils.kt
```

---

## 四、核心文件实现规范

### 4.1 Application 入口

**文件**: `MyApplication.kt`

```kotlin
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
        // 3. 初始化百度地图 (如需要)
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(this)
    }
}
```

### 4.2 数据模型规范

每个 Model 类需包含:
- data class 定义所有字段
- 可选的伴生对象常量
- 必要的默认值

**示例**: `News.kt`
```kotlin
data class News(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val publishTime: String,
    val imageUrl: String? = null,
    val imageUrls: List<String>? = null,
    val sourceUrl: String,
    val category: String = "general",
    val viewType: Int = VIEW_TYPE_TEXT
) {
    companion object {
        const val VIEW_TYPE_TEXT = 0
        const val VIEW_TYPE_SINGLE = 1
        const val VIEW_TYPE_MULTI = 2
    }
}
```

### 4.3 Repository 规范

- 所有数据获取方法返回 `Flow<Result<T>>`
- 网络请求失败时使用 Mock 数据
- 使用 suspend 函数处理异步操作

**模板**:
```kotlin
class XxxRepository(private val apiService: ApiService) {
    fun getXxxList(page: Int = 1): Flow<Result<XxxData>> = flow {
        try {
            val response = apiService.getXxxList(page)
            if (response.code == 200 && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.success(getMockData(page))) // 降级到 Mock 数据
        }
    }
}
```

### 4.4 ViewModel 规范

- 继承 `ViewModel()`
- 使用 `MutableStateFlow` 管理状态
- 暴露 `StateFlow` 给 UI
- 在 `init` 块中加载初始数据

**模板**:
```kotlin
class XxxViewModel(private val repository: XxxRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<XxxUiState>(XxxUiState.Loading)
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()
    
    init { loadData() }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = XxxUiState.Loading
            repository.getXxxList()
                .catch { e -> _uiState.value = XxxUiState.Error(e.message ?: "Error") }
                .collect { result ->
                    result.fold(
                        onSuccess = { _uiState.value = XxxUiState.Success(it) },
                        onFailure = { _uiState.value = XxxUiState.Error(it.message ?: "Error") }
                    )
                }
        }
    }
    
    fun refresh() = loadData()
}
```

### 4.5 UiState 规范

使用 sealed interface 定义:
```kotlin
sealed interface XxxUiState {
    data object Loading : XxxUiState
    data class Success(val data: XxxData) : XxxUiState
    data class Error(val message: String) : XxxUiState
}
```

### 4.6 Screen 规范

- 使用 `koinViewModel()` 获取 ViewModel
- 使用 `collectAsStateWithLifecycle()` 收集状态
- 根据 UiState 显示不同 UI

**模板**:
```kotlin
@Composable
fun XxxScreen(
    viewModel: XxxViewModel = koinViewModel(),
    onItemClick: (Item) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (val state = uiState) {
        is XxxUiState.Loading -> LoadingIndicator()
        is XxxUiState.Success -> XxxContent(data = state.data, onItemClick = onItemClick)
        is XxxUiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadData() })
    }
}
```

---

## 五、导航配置

### 5.1 Screen 定义

```kotlin
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Intro : Screen("intro")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Video : Screen("video")
    data object Chart : Screen("chart")
    data object Me : Screen("me")
    data object Login : Screen("login")
    data object Map : Screen("map")
    
    data object NewsDetail : Screen("news_detail/{url}/{title}") {
        fun createRoute(url: String, title: String) = 
            "news_detail/${URLEncoder.encode(url, "UTF-8")}/${URLEncoder.encode(title, "UTF-8")}"
    }
    
    data object VideoDetail : Screen("video_detail/{videoId}") {
        fun createRoute(videoId: String) = "video_detail/$videoId"
    }
}
```

### 5.2 导航流程

```
SplashScreen 
    ├── (首次启动) → IntroScreen → MainScreen
    └── (非首次) → MainScreen
    
MainScreen (底部导航)
    ├── HomeScreen → NewsDetailScreen
    ├── ChartScreen
    ├── VideoScreen → VideoDetailScreen
    └── MeScreen → LoginScreen / MapScreen
```

---

## 六、Koin 模块配置

```kotlin
val appModule = module { }

val networkModule = module {
    single { RetrofitClient.okHttpClient }
    single { RetrofitClient.retrofit }
    single { RetrofitClient.retrofit.create(ApiService::class.java) }
}

val repositoryModule = module {
    single { NewsRepository(get()) }
    single { VideoRepository(get()) }
    single { UserRepository() }
    single { ChartRepository() }
}

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::VideoViewModel)
    viewModelOf(::ChartViewModel)
    viewModelOf(::MeViewModel)
    viewModelOf(::LoginViewModel)
}
```

---

## 七、AndroidManifest.xml 配置

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application
        android:name=".MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AndroidEndProject"
        android:usesCleartextTraffic="true">
        
        <!-- 百度地图 API Key -->
        <meta-data
            android:name="com.baidu.lbsapi.API_KEY"
            android:value="${BAIDU_MAP_API_KEY}" />

        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.AndroidEndProject">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
    </application>
</manifest>
```

---

## 八、功能实现清单

### 8.1 必须实现的功能

| 模块 | 功能 | 优先级 |
|------|------|--------|
| Splash | 启动页 3 秒跳转 | P0 |
| Intro | 引导页 (首次启动) | P0 |
| Main | 底部导航栏 4 Tab | P0 |
| Home | 轮播图 + 新闻列表 | P0 |
| Video | 视频列表 + 播放 | P0 |
| Chart | 折线/柱状/饼图展示 | P1 |
| Me | 个人中心 + 登录状态 | P0 |
| Login | 登录/注册 (Firebase) | P0 |
| Map | 百度地图 + POI 搜索 | P2 |

### 8.2 通用组件

| 组件 | 功能 |
|------|------|
| `LoadingIndicator` | 全屏加载指示器 |
| `ErrorView` | 错误提示 + 重试按钮 |
| `BannerCarousel` | 自动轮播图 + 指示器 |
| `NewsItem` | 新闻列表项 (3 种样式) |
| `VideoItem` | 视频列表项 (封面 + 时长) |

---

## 九、开发注意事项

1. **状态管理**: 所有 UI 状态必须通过 StateFlow 管理
2. **生命周期**: 使用 `collectAsStateWithLifecycle()` 代替 `collectAsState()`
3. **导航**: 使用 `popUpTo` + `inclusive` 控制返回栈
4. **列表**: 始终使用 `LazyColumn` + `key` 参数
5. **图片**: 统一使用 Coil 的 `AsyncImage`
6. **主题**: 遵循 Material 3 色彩系统
7. **预览**: 为每个 Composable 添加 `@Preview`

---

## 十、测试要求

- 所有 ViewModel 需有单元测试
- 所有 Repository 需有单元测试
- 关键 UI 需有 Compose UI 测试
