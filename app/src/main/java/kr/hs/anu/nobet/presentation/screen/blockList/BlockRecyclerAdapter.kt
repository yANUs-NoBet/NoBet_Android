package kr.hs.anu.nobet.presentation.screen.blockList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.hs.anu.nobet.databinding.BlockItemBinding
import kr.hs.anu.nobet.domain.model.BlockSiteData

class BlockRecyclerAdapter : RecyclerView.Adapter<BlockRecyclerAdapter.BlockViewHolder>() {

    private val itemList = mutableListOf<BlockSiteData>()

    fun submitList(newList: List<BlockSiteData>) {
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class BlockViewHolder(private val binding: BlockItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BlockSiteData) {
            binding.tvBlockSite.text = item.siteUrl
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BlockRecyclerAdapter.BlockViewHolder {
        val binding = BlockItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BlockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size
}
