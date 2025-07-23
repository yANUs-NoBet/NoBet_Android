package kr.hs.anu.nobet.presentation.screen.report

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.hs.anu.nobet.R
import kr.hs.anu.nobet.databinding.ActivityReportBinding
import kr.hs.anu.nobet.utils.openPage

class ReportActivity : AppCompatActivity() {

    private val viewModel: ReportViewModel by viewModels()
    private lateinit var binding: ActivityReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.spinnerState.observe(this) { spinnerState ->
            binding.layoutReportMenu.visibility = if (spinnerState) View.VISIBLE else View.INVISIBLE
        }

        // 로고 클릭시 메인으로
        binding.ivTopbarLogo.setOnClickListener {
            finish()
        }

        // 온라인 상담
        binding.tvMobile.setOnClickListener {
            this.openPage()
        }

        binding.layoutSpinner.setOnClickListener {
            viewModel.toggle()
        }

        binding.ivDrop.setOnClickListener {
            viewModel.toggle()
        }

        // TODO menu 값 클릭했을때 선택값으로 데이터 처리 (서버랑 연결하면서 처리)
        binding.tvSiteReport.setOnClickListener(clickListener)
        binding.tvFakeBlockReport.setOnClickListener(clickListener)
        binding.tvOtherReport.setOnClickListener(clickListener)
    }

    private val clickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.tv_site_report -> {
                binding.tvReportInfo.text = binding.tvSiteReport.text
                viewModel.toggle()
            }
            R.id.tv_fake_block_report -> {
                binding.tvReportInfo.text = binding.tvFakeBlockReport.text
                viewModel.toggle()
            }
            R.id.tv_other_report -> {
                binding.tvReportInfo.text = binding.tvOtherReport.text
                viewModel.toggle()
            }
        }
    }
}
