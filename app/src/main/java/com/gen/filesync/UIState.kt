package com.gen.filesync

sealed class UIState {
    object Loading: UIState()
    data class Success(val data: Any) : UIState()
    data class Error(val reason: String) : UIState()
}