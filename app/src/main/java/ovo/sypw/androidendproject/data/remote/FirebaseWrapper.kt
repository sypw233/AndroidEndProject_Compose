package ovo.sypw.androidendproject.data.remote

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase 封装类
 * 直接集成 Firebase Auth，支持邮箱/密码和 Google 登录
 */
object FirebaseWrapper {

    private const val TAG = "FirebaseWrapper"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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
            Log.d(TAG, "authStateFlow: 认证状态变化, currentUser=${auth.currentUser?.uid}")
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * 邮箱密码登录
     */
    suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        Log.d(TAG, "signInWithEmail: 开始, email=$email")
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d(
                TAG,
                "signInWithEmail: 成功, user=${result.user?.uid}, isEmailVerified=${result.user?.isEmailVerified}"
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail: 失败", e)
            Result.failure(e)
        }
    }

    /**
     * 邮箱密码注册 (带用户名)
     * 注册后发送验证邮件并登出用户
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String = ""
    ): Result<AuthResult> {
        Log.d(TAG, "signUpWithEmail: 开始, email=$email, displayName=$displayName")
        return try {
            Log.d(TAG, "signUpWithEmail: 调用 createUserWithEmailAndPassword")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "signUpWithEmail: 用户创建成功, user=${result.user?.uid}")
            result.user?.let { firebaseUser ->
                // 如果提供了用户名，更新 Firebase Profile
                if (displayName.isNotBlank()) {
                    Log.d(TAG, "signUpWithEmail: 更新用户名")
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = displayName
                    }
                    firebaseUser.updateProfile(profileUpdates).await()
                    Log.d(TAG, "signUpWithEmail: 用户名更新完成")
                }
                // 发送验证邮件
                Log.d(TAG, "signUpWithEmail: 发送验证邮件")
                firebaseUser.sendEmailVerification().await()
                Log.d(TAG, "signUpWithEmail: 验证邮件发送完成")
                // 登出用户（需要验证后才能登录）
                Log.d(TAG, "signUpWithEmail: 登出用户")
                signOut()
                Log.d(TAG, "signUpWithEmail: 用户已登出")
            }
            Log.d(TAG, "signUpWithEmail: 返回成功结果")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithEmail: 失败", e)
            Result.failure(e)
        }
    }

    /**
     * 使用凭证登录 (用于 Google 登录)
     */
    suspend fun signInWithCredential(credential: AuthCredential): Result<AuthResult> {
        Log.d(TAG, "signInWithCredential: 开始")
        return try {
            val result = auth.signInWithCredential(credential).await()
            Log.d(TAG, "signInWithCredential: 成功, user=${result.user?.uid}")
            Log.d(TAG, "signInWithCredential: 返回成功结果")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithCredential: 失败", e)
            Result.failure(e)
        }
    }

    /**
     * 登出
     */
    fun signOut() {
        Log.d(TAG, "signOut: 执行登出")
        auth.signOut()
        Log.d(TAG, "signOut: 登出完成, currentUser=${auth.currentUser}")
    }

    /**
     * 删除用户账号
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 发送密码重置邮件
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        Log.d(TAG, "sendPasswordResetEmail: 开始, email=$email")
        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "sendPasswordResetEmail: 邮件发送成功")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordResetEmail: 失败", e)
            Result.failure(e)
        }
    }
}
