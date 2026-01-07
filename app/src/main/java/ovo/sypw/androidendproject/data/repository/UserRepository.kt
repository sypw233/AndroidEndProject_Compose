package ovo.sypw.androidendproject.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import ovo.sypw.androidendproject.data.model.User
import ovo.sypw.androidendproject.data.remote.FirebaseWrapper
import ovo.sypw.androidendproject.ui.activity.FirebaseUILoginActivity

/**
 * 用户仓库
 * 提供用户认证相关功能，使用 FirebaseUI 进行登录
 */
class UserRepository {

    val currentUser: FirebaseUser?
        get() = FirebaseWrapper.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = FirebaseWrapper.authStateFlow()

    /**
     * 创建启动 FirebaseUI 登录的 Intent
     */
    fun createSignInIntent(context: Context) = FirebaseUILoginActivity.createIntent(context)

    /**
     * 登出
     */
    suspend fun signOut(context: Context) {
        FirebaseWrapper.signOut(context)
    }

    /**
     * 删除账号
     */
    suspend fun deleteAccount(context: Context): Result<Unit> {
        return FirebaseWrapper.deleteAccount(context)
    }

    suspend fun getUserInfo(uid: String): Result<User> {
        return FirebaseWrapper.getUserInfo(uid)
    }

    fun isLoggedIn(): Boolean = currentUser != null

    fun getCurrentDisplayName(): String {
        return currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "用户"
    }
}
