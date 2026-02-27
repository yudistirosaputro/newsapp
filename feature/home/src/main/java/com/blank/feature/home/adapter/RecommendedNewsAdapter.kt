package com.blank.feature.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blank.feature.home.databinding.ItemNewsArticleBinding
import com.blank.feature.home.model.NewsItem

class RecommendedNewsAdapter(
    private val onItemClick: (NewsItem) -> Unit,
    private val onBookmarkClick: (NewsItem) -> Unit,
) : PagingDataAdapter<NewsItem, RecommendedNewsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewsArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(
        private val binding: ItemNewsArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { onItemClick(it) }
                }
            }
            binding.btnBookmark.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { onBookmarkClick(it) }
                }
            }
        }

        fun bind(item: NewsItem) {
            binding.apply {
                tvNewsTitle.text = item.title
                tvSource.text = item.source
                tvTime.text = item.timeAgo
                chipCategory.text = item.category

                // Set bookmark icon based on state
                btnBookmark.setImageResource(
                    if (item.isBookmarked) {
                        com.blank.feature.home.R.drawable.ic_bookmark_filled
                    } else {
                        com.blank.feature.home.R.drawable.ic_bookmark_outline
                    }
                )

                // TODO: Load image with Glide or Coil
                // For now, use placeholder
                ivNewsImage.setImageResource(com.blank.feature.home.R.drawable.ic_placeholder)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NewsItem>() {
        override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
            return oldItem == newItem
        }
    }
}
