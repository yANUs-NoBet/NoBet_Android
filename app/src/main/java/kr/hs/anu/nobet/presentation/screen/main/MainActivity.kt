package kr.hs.anu.nobet.presentation.screen.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.hs.anu.nobet.R
import kr.hs.anu.nobet.databinding.ActivityMainBinding
import kr.hs.anu.nobet.presentation.screen.allow.AllowActivity
import kr.hs.anu.nobet.presentation.screen.blockList.BlockListActivity
import kr.hs.anu.nobet.presentation.screen.login.LoginActivity
import kr.hs.anu.nobet.presentation.screen.report.ReportActivity
import kr.hs.anu.nobet.utils.NoBetVpnService
import kr.hs.anu.nobet.utils.openPage

class MainActivity : AppCompatActivity() {

    private val REQ_PREPARE_VPN = 1001
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

        // 버튼 상태값 읽고 값에 따라 상태 바꾸기
        viewModel.btnState.observe(this) { btnState ->
            if (btnState) startVpn() else stopVpn()

            // 차단 전원 버튼 색 변경
            binding.layoutBlockBtn.setBackgroundResource(
                if (btnState) R.drawable.block_btn_on else R.drawable.block_btn_background
            )

            // 차단된 사이트 수 텍스트 색 변경
            val block_txt_color =
                ContextCompat.getColor(this, if (btnState) R.color.red else R.color.gray)
            binding.tvBlockNum.setTextColor(block_txt_color)

            // 차단 전원 버튼 아이콘 색 변경
            val power_icon_color =
                ContextCompat.getColor(this, if (btnState) R.color.white else R.color.black)
            binding.ivPower.setColorFilter(power_icon_color, PorterDuff.Mode.SRC_IN)

            // 상담 정보 박스 색 변경
            binding.layoutGamblingPreventInfoBox.setBackgroundResource(
                if (btnState) R.drawable.gambling_prevent_info_box_on else R.drawable.gambling_prevent_info_box
            )

            // 방어중 타이틀 텍스트 변경
            binding.tvViewTitle.text =
                getString(if (btnState) R.string.on_view_title else R.string.off_view_title)
        }

        // 전원 버튼 클릭했을때 상태변화 함수 뷰 모델에서 호출
        binding.layoutBlockBtn.setOnClickListener {
            viewModel.toggle()
        }

        // 메뉴
        binding.ivMenu.setOnClickListener {
            showMenu(it)
        }

        // 온라인 상담 연결
        binding.tvMobile.setOnClickListener {
            this.openPage()
        }
    }

    // 메뉴 커스텀 함수
    private fun showMenu(anchor: View) {
        val popupMenu = LayoutInflater.from(anchor.context).inflate(R.layout.popup_menu, null)

        val popupWindow = PopupWindow(
            popupMenu,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 다른 영역 클릭시 닫힘
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        // popupWindow를 감싸는 배경, 배경이 있어야 다른 영역 클릭시 닫힘
        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // 기존 뷰 아래에 띄우기
        popupWindow.showAsDropDown(anchor, 0, 30)

        // TODO 이동 처리는 다음 branch 에서 화면 만들고 거기서 이어줄 예정
        popupMenu.findViewById<ConstraintLayout>(R.id.menu_login).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss()
        }

        popupMenu.findViewById<ConstraintLayout>(R.id.menu_block_pass).setOnClickListener {
            val intent = Intent(this, AllowActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss()
        }

        popupMenu.findViewById<ConstraintLayout>(R.id.menu_log).setOnClickListener {
            val intent = Intent(this, BlockListActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss()
        }

        popupMenu.findViewById<ConstraintLayout>(R.id.menu_report).setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss()
        }
    }

    private fun startVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, REQ_PREPARE_VPN)
        } else {
            onActivityResult(REQ_PREPARE_VPN, Activity.RESULT_OK, null)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PREPARE_VPN && resultCode == Activity.RESULT_OK) {
            ContextCompat.startForegroundService(
                this,
                Intent(this, NoBetVpnService::class.java)
            )
        }
    }

    private fun stopVpn() {
        // 서비스가 떠 있을 때만 STOP 액션 전달 (foregroundService 금지!)
        if (NoBetVpnService.isRunning) {
            startService(
                Intent(this, NoBetVpnService::class.java).apply {
                    action = NoBetVpnService.ACTION_STOP
                }
            )
        }
        // 보조: 실행 중이면 종료, 아니면 그냥 무시됨
        stopService(Intent(this, NoBetVpnService::class.java))
    }
}
