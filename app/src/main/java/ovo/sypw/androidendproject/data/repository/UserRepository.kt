package ovo.sypw.androidendproject.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import ovo.sypw.androidendproject.data.remote.FirebaseWrapper

/**
 * 用户仓库
 * 提供用户认证相关功能
 */
class UserRepository {

    val currentUser: FirebaseUser?
        get() = FirebaseWrapper.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = FirebaseWrapper.authStateFlow()

    suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        return FirebaseWrapper.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthResult> {
        return FirebaseWrapper.signUpWithEmail(email, password, displayName)
    }

    suspend fun signInWithCredential(credential: AuthCredential): Result<AuthResult> {
        return FirebaseWrapper.signInWithCredential(credential)
    }

    fun signOut() {
        FirebaseWrapper.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> {
        return FirebaseWrapper.deleteAccount()
    }

    fun isLoggedIn(): Boolean = currentUser != null

    fun getCurrentDisplayName(): String {
        return currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "用户"
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return FirebaseWrapper.sendPasswordResetEmail(email)
    }
}
