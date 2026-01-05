package ovo.sypw.androidendproject.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import ovo.sypw.androidendproject.data.model.User

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
     * 邮箱密码登录
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("登录失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 邮箱密码注册 (带用户名)
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String = ""
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                // 如果提供了用户名，更新 Firebase Profile
                if (displayName.isNotBlank()) {
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = displayName
                    }
                    firebaseUser.updateProfile(profileUpdates).await()
                }
                // 保存用户信息到 Firestore
                saveUserToFirestore(firebaseUser, displayName)
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("注册失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 登出
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * 保存用户信息到 Firestore
     */
    private suspend fun saveUserToFirestore(firebaseUser: FirebaseUser, displayName: String = "") {
        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = displayName.ifBlank { firebaseUser.displayName },
            avatarUrl = firebaseUser.photoUrl?.toString()
        )
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(user)
            .await()
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
