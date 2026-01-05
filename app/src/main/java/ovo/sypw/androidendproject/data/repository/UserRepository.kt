package ovo.sypw.androidendproject.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import ovo.sypw.androidendproject.data.model.User
import ovo.sypw.androidendproject.data.remote.FirebaseWrapper

class UserRepository {

    val currentUser: FirebaseUser?
        get() = FirebaseWrapper.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = FirebaseWrapper.authStateFlow()

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return FirebaseWrapper.signInWithEmail(email, password)
    }

    suspend fun signUp(email: String, password: String, displayName: String = ""): Result<FirebaseUser> {
        return FirebaseWrapper.signUpWithEmail(email, password, displayName)
    }

    fun signOut() {
        FirebaseWrapper.signOut()
    }

    suspend fun getUserInfo(uid: String): Result<User> {
        return FirebaseWrapper.getUserInfo(uid)
    }

    fun isLoggedIn(): Boolean = currentUser != null

    fun getCurrentDisplayName(): String {
        return currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "用户"
    }
}
