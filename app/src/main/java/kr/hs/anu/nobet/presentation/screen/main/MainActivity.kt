package kr.hs.anu.nobet.presentation.screen.main

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.hs.anu.nobet.R
import kr.hs.anu.nobet.databinding.ActivityMainBinding
import androidx.core.graphics.drawable.toDrawable

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

        //전원 버튼 클릭했을때 상태변화 함수 뷰 모델에서 호출
        binding.layoutBlockBtn.setOnClickListener {
            viewModel.toggle()
        }

        //메뉴
        binding.ivMenu.setOnClickListener { 
            showMenu(it)
        }
    }

    //메뉴 커스텀 함수
    private fun showMenu(anchor: View) {
        val popupMenu = LayoutInflater.from(anchor.context).inflate(R.layout.popup_menu, null)

        val popupWindow = PopupWindow(
            popupMenu,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        popupWindow.showAsDropDown(anchor, 0, 30)

        popupMenu.findViewById<ConstraintLayout>(R.id.menu_login).setOnClickListener {
            //TODO 로그인 화면으로 이동
            Toast.makeText(anchor.context, "로그인 클릭됨", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }
        
        //TODO 다른 메뉴도 클릭시 동작 추가
    }
}
