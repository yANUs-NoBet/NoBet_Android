package kr.hs.anu.nobet.presentation.screen.allow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.hs.anu.nobet.databinding.AllowItemBinding
import kr.hs.anu.nobet.domain.model.AllowSiteData

class AllowRecyclerAdapter : RecyclerView.Adapter<AllowRecyclerAdapter.AllowViewHolder>() {

    private val itemList = mutableListOf<AllowSiteData>()

    fun submitList(newList: List<AllowSiteData>) {
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class AllowViewHolder(private val binding: AllowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllowSiteData) {
            // TODO 데이터 넣어주기
            binding.tvAllowSite.text = item.siteUrl

            binding.btnDel.setOnClickListener {
                // TODO 삭제
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowViewHolder {
        val binding = AllowItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AllowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllowViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size
}
