package ovo.sypw.androidendproject.data.remote

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import ovo.sypw.androidendproject.data.model.User

/**
 * Firebase 封装类
 * 使用 FirebaseUI 进行身份验证，简化登录流程
 */
object FirebaseWrapper {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * 当前用户
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * 监听认证状态变化
     */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * 使用 FirebaseUI 登出
     * 同时清除 FirebaseUI 的登录状态
     */
    suspend fun signOut(context: Context) {
        AuthUI.getInstance()
            .signOut(context)
            .await()
    }

    /**
     * 删除用户账号
     * 同时删除 FirebaseUI 的登录状态和 Firestore 中的用户数据
     */
    suspend fun deleteAccount(context: Context): Result<Unit> {
        return try {
            val uid = currentUser?.uid
            // 删除 Firestore 中的用户数据
            uid?.let {
                firestore.collection("users").document(it).delete().await()
            }
            // 删除 Firebase Auth 账号
            AuthUI.getInstance()
                .delete(context)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(uid: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.toObject(User::class.java)?.let { Result.success(it) }
                ?: Result.failure(Exception("用户不存在"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
