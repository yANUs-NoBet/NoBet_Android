package kr.hs.anu.nobet.presentation.screen.block_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.hs.anu.nobet.domain.model.BlockSiteData

class BlockListViewModel : ViewModel() {
    private val _siteList = MutableLiveData<List<BlockSiteData>>()
    val siteList: LiveData<List<BlockSiteData>> get() = _siteList

    init {
        loadBlockSites()
    }

    private fun loadBlockSites() {
        // 임시 데이터
        _siteList.value = listOf(
            BlockSiteData("example.com"),
            BlockSiteData("example.com"),
            BlockSiteData("example.com")
        )
    }
}