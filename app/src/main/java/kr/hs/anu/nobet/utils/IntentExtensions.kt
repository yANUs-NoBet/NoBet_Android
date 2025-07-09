package kr.hs.anu.nobet.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

// 온라인 상담 바로가기 구현
fun Context.openPage() {
    val url = "https://www.kcgp.or.kr/portal/main/main.do"
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
    }
}