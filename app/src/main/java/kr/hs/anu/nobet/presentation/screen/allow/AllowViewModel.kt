package kr.hs.anu.nobet.presentation.screen.allow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.hs.anu.nobet.domain.model.AllowSiteData

class AllowViewModel : ViewModel() {
    private val _siteList = MutableLiveData<List<AllowSiteData>>()
    val siteList: LiveData<List<AllowSiteData>> get() = _siteList

    init {
        loadAllowSites()
    }

    private fun loadAllowSites() {
        // 임시 데이터
        _siteList.value = listOf(
            AllowSiteData("naver.com"),
            AllowSiteData("google.com"),
            AllowSiteData("youtube.com")
        )
    }
}
