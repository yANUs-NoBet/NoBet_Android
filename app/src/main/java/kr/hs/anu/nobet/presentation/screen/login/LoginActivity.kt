package kr.hs.anu.nobet.presentation.screen.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.hs.anu.nobet.R
import kr.hs.anu.nobet.databinding.ActivityLoginBinding
import androidx.core.net.toUri
import kr.hs.anu.nobet.utils.openPage

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // logo 누르면 홈으로 이동
        binding.ivTopbarLogo.setOnClickListener {
            finish()
        }

        binding.btnLogin.setOnClickListener {
            //TODO 로그인 로직 구현 예정
        }

        // 온라인 상담 바로가기
        binding.tvMobile.setOnClickListener {
            this.openPage()
        }
    }
}