package kr.hs.anu.nobet.presentation.screen.block_list

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kr.hs.anu.nobet.R
import kr.hs.anu.nobet.databinding.ActivityBlockListBinding
import kr.hs.anu.nobet.utils.openPage

class BlockListActivity : AppCompatActivity() {

    private val viewModel: BlockListViewModel by viewModels()
    private lateinit var binding: ActivityBlockListBinding
    private lateinit var blockAdapter: BlockRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBlockListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 로고 클릭시 메인 화면으로
        binding.ivTopbarLogo.setOnClickListener {
            finish()
        }

        binding.tvMobile.setOnClickListener {
            this.openPage()
        }

        //TODO 페이징 사용 예정

        // Adapter
        blockAdapter = BlockRecyclerAdapter()
        binding.recyclerBlock.apply {
            layoutManager = LinearLayoutManager(this@BlockListActivity)
            adapter = blockAdapter
        }

        viewModel.siteList.observe(this) { list ->
            blockAdapter.submitList(list)
        }
    }
}