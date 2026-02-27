package com.blank.feature.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blank.feature.home.R
import com.blank.feature.home.databinding.ItemBreakingNewsBinding
import com.blank.feature.home.model.NewsItem

class BreakingNewsAdapter(
    private val onItemClick: (NewsItem) -> Unit,
    private val onBookmarkClick: (NewsItem) -> Unit,
) : ListAdapter<NewsItem, BreakingNewsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBreakingNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemBreakingNewsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            binding.btnBookmark.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookmarkClick(getItem(position))
                }
            }
        }

        fun bind(item: NewsItem) {
            binding.apply {
                tvNewsTitle.text = item.title
                tvSource.text = item.source
                tvTime.text = item.timeAgo
                chipCategory.text = item.category

                btnBookmark.setImageResource(
                    if (item.isBookmarked) {
                        R.drawable.ic_bookmark_filled
                    } else {
                        R.drawable.ic_bookmark_outline
                    }
                )

                ivNewsImage.load(item.urlToImage) {
                    placeholder(R.drawable.ic_placeholder)
                    error(R.drawable.ic_placeholder)
                    crossfade(true)
                }
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
