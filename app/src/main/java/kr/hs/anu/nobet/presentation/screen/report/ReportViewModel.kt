package kr.hs.anu.nobet.presentation.screen.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReportViewModel : ViewModel() {

    // spinner 상태
    private val _spinnerState = MutableLiveData(false)
    val spinnerState: LiveData<Boolean> = _spinnerState

    // spinner 상태 관리
    fun toggle() {
        _spinnerState.value = !(_spinnerState.value ?: false)
    }
}
