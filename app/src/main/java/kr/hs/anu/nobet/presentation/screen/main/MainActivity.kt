package kr.hs.anu.nobet.presentation.screen.main

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.hs.anu.nobet.R
import kr.hs.anu.nobet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //버튼 상태값 읽고 값에 따라 상태 바꾸기
        viewModel.btnState.observe(this) { btnState ->
            //차단 전원 버튼 색 변경
            binding.layoutBlockBtn.setBackgroundResource(
                if (btnState) R.drawable.block_btn_on else R.drawable.block_btn_background
            )

            //차단된 사이트 수 텍스트 색 변경
            val block_txt_color =
                ContextCompat.getColor(this, if (btnState) R.color.red else R.color.gray)
            binding.tvBlockNum.setTextColor(block_txt_color)

            //차단 전원 버튼 아이콘 색 변경
            val power_icon_color =
                ContextCompat.getColor(this, if (btnState) R.color.white else R.color.black)
            binding.ivPower.setColorFilter(power_icon_color, PorterDuff.Mode.SRC_IN)

            //상담 정보 박스 색 변경
            binding.layoutGamblingPreventInfoBox.setBackgroundResource(
                if (btnState) R.drawable.gambling_prevent_info_box_on else R.drawable.gambling_prevent_info_box
            )

            //방어중 타이틀 텍스트 변경
            binding.tvViewTitle.text =
                getString(if (btnState) R.string.on_view_title else R.string.off_view_title)
        }

        binding.layoutBlockBtn.setOnClickListener {
            viewModel.toggle()
        }
    }
}
