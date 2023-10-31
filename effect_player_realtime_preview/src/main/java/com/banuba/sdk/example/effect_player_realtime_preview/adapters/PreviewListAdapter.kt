package com.banuba.sdk.example.effect_player_realtime_preview.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.banuba.sdk.example.effect_player_realtime_preview.databinding.PreviewItemBinding

class PreviewListAdapter (private val mScreenWidth: Int, private val itemSelected: (SelectableItem, Int) -> Unit):
        ListAdapter<SelectableItem, PreviewListAdapter.ItemViewHolder>(SelectableItemDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder
            = ItemViewHolder(PreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    private fun View.setHorizontalMargins(left: Int, right: Int) {
        if (this.layoutParams is ViewGroup.MarginLayoutParams) {
            (this.layoutParams as ViewGroup.MarginLayoutParams).setMargins(left, 0, right, 0)
            this.requestLayout()
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.binding.info.text = getItem(position).name
        holder.binding.info.measure(0, 0)
        val margin: Int = mScreenWidth / 2 - holder.binding.info.measuredWidth / 2
        val left: Int = if (position == 0) margin else 0
        val right: Int = if (position == itemCount - 1) margin else 0
        holder.itemView.setHorizontalMargins(left, right)
        holder.bind(getItem(position), position)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (payloads[0] == true) {
                holder.bindSelectedState(getItem(position).isSelected)
            }
        }
    }

    inner class ItemViewHolder(val binding: PreviewItemBinding):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SelectableItem, position: Int) = with(binding) {
            info.text = item.name
            selector.isVisible = item.isSelected
            root.setOnClickListener {
                itemSelected(item, position)
            }
        }

        fun bindSelectedState(isSelected: Boolean) {
            binding.selector.isVisible = isSelected
        }
    }

    private class SelectableItemDiff: DiffUtil.ItemCallback<SelectableItem>() {
        override fun areItemsTheSame(oldItem: SelectableItem, newItem: SelectableItem): Boolean
                = oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: SelectableItem, newItem: SelectableItem): Boolean
                = oldItem == newItem

        override fun getChangePayload(oldItem: SelectableItem, newItem: SelectableItem): Any?
                = if (oldItem.isSelected != newItem.isSelected) true else null
    }
}
