package com.blank.feature.explore.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blank.core.extensions.gone
import com.blank.core.extensions.visible
import com.blank.core.model.NewsItem
import com.blank.feature.explore.R
import com.blank.feature.explore.databinding.ItemSearchResultBinding

class SearchResultAdapter(
    private val onItemClick: (NewsItem) -> Unit,
) : PagingDataAdapter<NewsItem, SearchResultAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(
        private val binding: ItemSearchResultBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let(onItemClick)
                }
            }
        }

        fun bind(item: NewsItem) = with(binding) {
            tvNewsTitle.text = item.title
            tvNewsDescription.text = item.description
            tvSource.text = item.source
            tvTime.text = item.timeAgo

            if (item.isBookmarked) ivBookmarkedBadge.visible() else ivBookmarkedBadge.gone()

            ivNewsImage.load(item.urlToImage) {
                placeholder(com.blank.core.R.drawable.ic_placeholder)
                error(com.blank.core.R.drawable.ic_placeholder)
                crossfade(true)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NewsItem>() {
        override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem) =
            oldItem == newItem
    }
}
