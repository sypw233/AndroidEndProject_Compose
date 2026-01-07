package ovo.sypw.androidendproject.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ovo.sypw.androidendproject.R
import ovo.sypw.androidendproject.data.model.User

/**
 * FirebaseUI 登录 Activity
 * 使用 FirebaseUI 提供的预构建登录界面，支持邮箱/密码和 Google 登录
 */
class FirebaseUILoginActivity : ComponentActivity() {
    val TAG = "FirebaseUILoginActivity"
    private val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchSignIn()
    }

    private fun launchSignIn() {
        // 配置登录提供商：邮箱/密码 和 Google
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // 创建并启动登录 Intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.mipmap.ic_launcher) // 设置应用 Logo
            .setTheme(R.style.Theme_AndroidEndProject_Compose) // 使用应用主题
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // 登录成功
            val user = FirebaseAuth.getInstance().currentUser
            user?.let { firebaseUser ->
                // 保存用户信息到 Firestore
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        saveUserToFirestore(
                            firebaseUser.uid,
                            firebaseUser.email,
                            firebaseUser.displayName,
                            firebaseUser.photoUrl?.toString()
                        )
                    } catch (e: Exception) {
                        // 忽略保存错误，用户已登录
                    }
                }
                Toast.makeText(
                    this,
                    "登录成功：${firebaseUser.displayName ?: firebaseUser.email}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setResult(RESULT_OK)
            finish()
        } else {
            // 用户取消登录或登录失败
            if (response == null) {
                // 用户按返回键取消
                Toast.makeText(this, "登录已取消", Toast.LENGTH_SHORT).show()
            } else {
                // 登录错误
                val error = response.error
                Log.d(TAG, "onSignInResult: " + "登录失败：${error?.localizedMessage ?: "未知错误"}")
                Toast.makeText(
                    this,
                    "登录失败：${error?.localizedMessage ?: "未知错误"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private suspend fun saveUserToFirestore(
        uid: String,
        email: String?,
        displayName: String?,
        avatarUrl: String?
    ) {
        val user = User(
            uid = uid,
            email = email ?: "",
            displayName = displayName,
            avatarUrl = avatarUrl
        )
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(user)
            .await()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, FirebaseUILoginActivity::class.java)
        }
    }
}
