package ovo.sypw.androidendproject.ui.screens.home

import ovo.sypw.androidendproject.data.model.Banner
import ovo.sypw.androidendproject.data.model.News

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val banners: List<Banner>,
        val news: List<News>,
        val hasMore: Boolean
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}
