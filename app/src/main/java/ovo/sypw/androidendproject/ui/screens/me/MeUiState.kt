package ovo.sypw.androidendproject.ui.screens.me

import ovo.sypw.androidendproject.data.model.User

sealed interface MeUiState {
    data object Loading : MeUiState
    data class LoggedIn(val user: User) : MeUiState
    data object NotLoggedIn : MeUiState
}
