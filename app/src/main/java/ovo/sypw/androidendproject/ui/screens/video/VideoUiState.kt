package ovo.sypw.androidendproject.ui.screens.video

import ovo.sypw.androidendproject.data.model.Video

sealed interface VideoUiState {
    data object Loading : VideoUiState
    data class Success(
        val videos: List<Video>,
        val hasMore: Boolean
    ) : VideoUiState
    data class Error(val message: String) : VideoUiState
}
