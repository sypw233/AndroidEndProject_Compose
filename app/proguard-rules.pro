# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.stream.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Data Models
-keep class ovo.sypw.androidendproject.data.model.** { *; }
-keep class ovo.sypw.androidendproject.data.remote.model.** { *; }
# Data class componentN methods (needed for destructuring)
-keepclassmembers class ovo.sypw.androidendproject.data.model.** {
    public *** component*();
}

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# Koin
-keep class org.koin.** { *; }

-dontwarn io.micrometer.context.ContextAccessor
-dontwarn javax.enterprise.inject.spi.Extension
-dontwarn reactor.blockhound.integration.BlockHoundIntegration

# AMap (Gaode Map)
-keep class com.amap.api.** { *; }
-keep class com.autonavi.** { *; }
-keep class com.loc.** { *; }
-keep class com.amap.api.location.** { *; }
-keep class com.amap.api.fence.** { *; }
-keep class com.amap.api.maps.** { *; }
-keep class com.amap.api.navi.** { *; }

# 3D Map specific
-keep class com.amap.api.maps.model.** { *; }
-keep interface com.amap.api.maps.** { *; }

# JNI/Native related
-keepclasseswithmembernames class * {
    native <methods>;
}