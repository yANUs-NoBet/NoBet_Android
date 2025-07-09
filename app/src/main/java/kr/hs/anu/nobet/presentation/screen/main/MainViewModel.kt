package kr.hs.anu.nobet.presentation.screen.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    // 버튼 상태
    private val _btnState = MutableLiveData(false)
    val btnState: LiveData<Boolean> = _btnState

    // 버튼 상태 관리
    fun toggle() {
        _btnState.value = !(_btnState.value ?: false)
    }
}
