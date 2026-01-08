package ovo.sypw.androidendproject.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.androidendproject.data.repository.UserRepository

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        Log.d(TAG, "signInWithEmail: 开始登录, email=$email")
        if (email.isBlank() || password.isBlank()) {
            Log.w(TAG, "signInWithEmail: 邮箱或密码为空")
            _uiState.value = LoginUiState.Error("邮箱或密码不能为空")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "signInWithEmail: 设置状态为 Loading")
            _uiState.value = LoginUiState.Loading
            Log.d(TAG, "signInWithEmail: 调用 userRepository.signInWithEmail")
            val result = userRepository.signInWithEmail(email, password)
            Log.d(TAG, "signInWithEmail: 收到结果, isSuccess=${result.isSuccess}, isFailure=${result.isFailure}")
            result.fold(
                onSuccess = { authResult ->
                    val user = authResult.user
                    Log.d(TAG, "signInWithEmail: onSuccess, user=${user?.uid}, isEmailVerified=${user?.isEmailVerified}")
                    if (user != null) {
                        // 检查邮箱是否已验证
                        if (user.isEmailVerified) {
                            Log.d(TAG, "signInWithEmail: 邮箱已验证, 设置状态为 Success")
                            _uiState.value = LoginUiState.Success(user)
                            Log.d(TAG, "signInWithEmail: 状态已设置为 Success, 当前状态=${_uiState.value}")
                        } else {
                            Log.d(TAG, "signInWithEmail: 邮箱未验证, 登出并设置错误状态")
                            // 邮箱未验证，登出并提示
                            userRepository.signOut()
                            _uiState.value = LoginUiState.Error("请先验证您的邮箱后再登录")
                            Log.d(TAG, "signInWithEmail: 已设置错误状态")
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail: 用户信息为空")
                        _uiState.value = LoginUiState.Error("登录失败：用户信息为空")
                    }
                },
                onFailure = { e ->
                    Log.e(TAG, "signInWithEmail: onFailure", e)
                    _uiState.value = LoginUiState.Error(e.message ?: "登录失败")
                }
            )
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        Log.d(TAG, "signUpWithEmail: 开始注册, email=$email, displayName=$displayName")
        if (email.isBlank() || password.isBlank()) {
            Log.w(TAG, "signUpWithEmail: 邮箱或密码为空")
            _uiState.value = LoginUiState.Error("邮箱或密码不能为空")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "signUpWithEmail: 设置状态为 Loading")
            _uiState.value = LoginUiState.Loading
            Log.d(TAG, "signUpWithEmail: 调用 userRepository.signUpWithEmail")
            val result = userRepository.signUpWithEmail(email, password, displayName)
            Log.d(TAG, "signUpWithEmail: 收到结果, isSuccess=${result.isSuccess}, isFailure=${result.isFailure}")
            result.fold(
                onSuccess = { _ ->
                    Log.d(TAG, "signUpWithEmail: onSuccess, 设置状态为 VerificationRequired")
                    // 注册成功，提示用户验证邮箱
                    _uiState.value = LoginUiState.VerificationRequired
                    Log.d(TAG, "signUpWithEmail: 状态已设置为 VerificationRequired, 当前状态=${_uiState.value}")
                },
                onFailure = { e ->
                    Log.e(TAG, "signUpWithEmail: onFailure", e)
                    _uiState.value = LoginUiState.Error(e.message ?: "注册失败")
                }
            )
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "signInWithGoogle: 开始 Google 登录, email=${account.email}")
        viewModelScope.launch {
            Log.d(TAG, "signInWithGoogle: 设置状态为 Loading")
            _uiState.value = LoginUiState.Loading
            try {
                val idToken = account.idToken
                Log.d(TAG, "signInWithGoogle: idToken=${if (idToken != null) "存在" else "null"}")
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    Log.d(TAG, "signInWithGoogle: 调用 userRepository.signInWithCredential")
                    val result = userRepository.signInWithCredential(credential)
                    Log.d(TAG, "signInWithGoogle: 收到结果, isSuccess=${result.isSuccess}, isFailure=${result.isFailure}")
                    result.fold(
                        onSuccess = { authResult ->
                            val user = authResult.user
                            Log.d(TAG, "signInWithGoogle: onSuccess, user=${user?.uid}")
                            if (user != null) {
                                Log.d(TAG, "signInWithGoogle: 设置状态为 Success")
                                _uiState.value = LoginUiState.Success(user)
                                Log.d(TAG, "signInWithGoogle: 状态已设置为 Success, 当前状态=${_uiState.value}")
                            } else {
                                Log.w(TAG, "signInWithGoogle: 用户信息为空")
                                _uiState.value = LoginUiState.Error("Google 登录失败：用户信息为空")
                            }
                        },
                        onFailure = { e ->
                            Log.e(TAG, "signInWithGoogle: onFailure", e)
                            _uiState.value = LoginUiState.Error(e.message ?: "Google 登录失败")
                        }
                    )
                } else {
                    Log.w(TAG, "signInWithGoogle: idToken 为 null")
                    _uiState.value = LoginUiState.Error("Google 登录失败：无法获取 ID Token")
                }
            } catch (e: Exception) {
                Log.e(TAG, "signInWithGoogle: 异常", e)
                _uiState.value = LoginUiState.Error(e.message ?: "Google 登录处理出错")
            }
        }
    }
    
    fun resetState() {
        Log.d(TAG, "resetState: 重置状态为 Idle")
        _uiState.value = LoginUiState.Idle
    }
}
