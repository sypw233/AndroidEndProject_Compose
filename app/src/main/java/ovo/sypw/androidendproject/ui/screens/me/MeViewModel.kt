package ovo.sypw.androidendproject.ui.screens.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.androidendproject.data.model.User
import ovo.sypw.androidendproject.data.repository.UserRepository

class MeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MeUiState>(MeUiState.Loading)
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()

    init {
        checkLoginState()
    }

    fun checkLoginState() {
        viewModelScope.launch {
            userRepository.authStateFlow().collect { firebaseUser ->
                if (firebaseUser != null) {
                    val result = userRepository.getUserInfo(firebaseUser.uid)
                    result.fold(
                        onSuccess = { user ->
                            _uiState.value = MeUiState.LoggedIn(user)
                        },
                        onFailure = {
                            // 如果获取用户信息失败，使用 Firebase 用户基本信息
                            _uiState.value = MeUiState.LoggedIn(
                                User(
                                    uid = firebaseUser.uid,
                                    email = firebaseUser.email ?: "",
                                    displayName = firebaseUser.displayName,
                                    avatarUrl = firebaseUser.photoUrl?.toString()
                                )
                            )
                        }
                    )
                } else {
                    _uiState.value = MeUiState.NotLoggedIn
                }
            }
        }
    }

    fun logout() {
        userRepository.signOut()
    }
}
