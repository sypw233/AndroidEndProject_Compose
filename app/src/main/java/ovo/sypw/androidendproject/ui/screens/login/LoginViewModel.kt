package ovo.sypw.androidendproject.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.androidendproject.data.repository.UserRepository

/**
 * 登录ViewModel - 参考示例代码 LoginFragment.java
 */
class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()

    fun toggleMode() {
        _isLoginMode.value = !_isLoginMode.value
        _uiState.value = LoginUiState.Idle
    }

    /**
     * 账号密码登录 - 参考示例代码 login 方法
     */
    fun login(username: String, password: String) {
        if (!validateInput(username, password)) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            // 使用用户名作为邮箱 (添加域名)
            val email = if (username.contains("@")) username else "$username@example.com"
            val result = userRepository.signIn(email, password)
            result.fold(
                onSuccess = {
                    _uiState.value = LoginUiState.Success
                },
                onFailure = { e ->
                    _uiState.value = LoginUiState.Error(e.message ?: "登录失败")
                }
            )
        }
    }

    /**
     * 注册 - 参考示例代码 registerFragment
     */
    fun register(username: String, password: String, confirmPassword: String, displayName: String) {
        if (username.isBlank()) {
            _uiState.value = LoginUiState.Error("账号不能为空")
            return
        }
        if (!validateInput(username, password)) return
        if (password != confirmPassword) {
            _uiState.value = LoginUiState.Error("两次密码不一致")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val email = if (username.contains("@")) username else "$username@example.com"
            val result = userRepository.signUp(email, password, displayName)
            result.fold(
                onSuccess = {
                    _uiState.value = LoginUiState.Success
                },
                onFailure = { e ->
                    _uiState.value = LoginUiState.Error(e.message ?: "注册失败")
                }
            )
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isBlank()) {
            _uiState.value = LoginUiState.Error("账号不能为空")
            return false
        }
        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("密码不能为空")
            return false
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("密码至少6位")
            return false
        }
        return true
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }
}
