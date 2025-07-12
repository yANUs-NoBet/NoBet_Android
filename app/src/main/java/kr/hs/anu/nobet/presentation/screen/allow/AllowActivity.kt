package kr.hs.anu.nobet.presentation.screen.allow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kr.hs.anu.nobet.R
import kr.hs.anu.nobet.databinding.ActivityAllowBinding
import kr.hs.anu.nobet.utils.openPage

class AllowActivity : AppCompatActivity() {

    private val viewModel : AllowViewModel by viewModels()
    private lateinit var binding : ActivityAllowBinding
    private lateinit var allowAdapter: AllowRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ivTopbarLogo.setOnClickListener {
            finish()
        }

        binding.tvMobile.setOnClickListener {
            this.openPage()
        }

        allowAdapter = AllowRecyclerAdapter()
        binding.recyclerAllow.apply {
            layoutManager = LinearLayoutManager(this@AllowActivity)
            adapter = allowAdapter
        }

        viewModel.siteList.observe(this) { list ->
            allowAdapter.submitList(list)
        }
    }
}