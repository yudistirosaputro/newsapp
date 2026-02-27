package com.blank.feature.home

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blank.core.base.BaseFragment
import com.blank.feature.home.adapter.BreakingNewsAdapter
import com.blank.feature.home.adapter.RecommendedNewsAdapter
import com.blank.feature.home.databinding.FragmentHomeBinding
import com.blank.feature.home.model.NewsItem
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private lateinit var breakingNewsAdapter: BreakingNewsAdapter
    private lateinit var recommendedNewsAdapter: RecommendedNewsAdapter

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupAdapters()
        setupRecyclerViews()
        setupCategoryChips()
        loadMockData()
    }

    private fun setupAdapters() {
        breakingNewsAdapter = BreakingNewsAdapter(
            onItemClick = { newsItem ->
                navigateToDetail(newsItem)
            },
            onBookmarkClick = { newsItem ->
                // TODO: Toggle bookmark
                Toast.makeText(requireContext(), "Bookmark: ${newsItem.title}", Toast.LENGTH_SHORT).show()
            }
        )

        recommendedNewsAdapter = RecommendedNewsAdapter(
            onItemClick = { newsItem ->
                navigateToDetail(newsItem)
            },
            onBookmarkClick = { newsItem ->
                Toast.makeText(requireContext(), "Bookmark: ${newsItem.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun navigateToDetail(newsItem: NewsItem) {
        val bundle = Bundle().apply {
            putParcelable(DetailArticleFragment.ARG_NEWS_ITEM, newsItem)
        }
        findNavController().navigate(R.id.detailArticleFragment, bundle)
    }

    private fun setupRecyclerViews() {
        binding.rvBreakingNews.apply {
            adapter = breakingNewsAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }

        binding.rvRecommendedNews.apply {
            adapter = recommendedNewsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds[0])
                val category = selectedChip?.text?.toString() ?: "All"
                Toast.makeText(requireContext(), "Category: $category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMockData() {
        val breakingNews = listOf(
            NewsItem(
                id = "1",
                title = "The Future of AI: What to Expect in 2025",
                description = "Artificial Intelligence is rapidly evolving, with new breakthroughs expected to transform industries in 2025.",
                content = "Artificial Intelligence has made remarkable strides in recent years, and 2025 promises to be a watershed moment for the technology. From healthcare to finance, AI is poised to revolutionize how we work and live.\n\nMajor tech companies are investing billions in AI research, with a particular focus on generative AI and large language models. These technologies are becoming more sophisticated, capable of understanding context and nuance in ways that were previously impossible.\n\nExperts predict that by 2025, AI will be integrated into nearly every aspect of our daily lives, from personalized healthcare recommendations to autonomous transportation systems. The challenge will be ensuring these technologies are developed responsibly and ethically.",
                source = "TechCrunch",
                timeAgo = "2 hours ago",
                category = "Technology"
            ),
            NewsItem(
                id = "2",
                title = "Global Markets React to New Economic Policies",
                description = "Stock markets around the world show mixed reactions as central banks announce coordinated policy changes.",
                content = "Global financial markets experienced significant volatility today as major central banks announced coordinated policy adjustments aimed at addressing inflation concerns while supporting economic growth.\n\nThe Federal Reserve, European Central Bank, and Bank of England all signaled a cautious approach to interest rate adjustments, emphasizing the need to balance inflation control with economic stability.\n\nAsian markets opened higher on the news, while European markets showed mixed results. Analysts suggest that the coordinated approach may help prevent excessive currency fluctuations and maintain global economic stability.",
                source = "Reuters",
                timeAgo = "3 hours ago",
                category = "Business"
            ),
            NewsItem(
                id = "3",
                title = "Revolutionary Battery Technology Unveiled",
                description = "Scientists announce breakthrough in solid-state battery technology that could revolutionize electric vehicles.",
                content = "A team of researchers has unveiled a new solid-state battery technology that promises to significantly improve energy density and charging speeds for electric vehicles.\n\nThe new batteries, which replace liquid electrolytes with solid materials, could potentially double the range of electric vehicles while reducing charging times to just 15 minutes.\n\nThe technology also addresses safety concerns associated with traditional lithium-ion batteries, as solid-state designs are less prone to overheating and thermal runaway. Major automakers have already expressed interest in licensing the technology for future vehicle models.",
                source = "BBC News",
                timeAgo = "4 hours ago",
                category = "Science"
            ),
            NewsItem(
                id = "4",
                title = "Championship Finals: Everything You Need to Know",
                description = "The highly anticipated championship match is set to begin this weekend with record viewership expected.",
                content = "Sports fans around the world are gearing up for what promises to be one of the most exciting championship finals in recent history. The two competing teams have both had remarkable seasons, overcoming numerous challenges to reach this point.\n\nTicket sales have broken records, with millions expected to watch the match across various platforms. The economic impact on the host city is estimated to be in the hundreds of millions.\n\nAnalysts are divided on predictions, with both teams showing exceptional form in recent matches. Weather conditions are expected to be ideal, ensuring a fair contest between two worthy competitors.",
                source = "ESPN",
                timeAgo = "5 hours ago",
                category = "Sports"
            )
        )

        val recommendedNews = listOf(
            NewsItem(
                id = "5",
                title = "SpaceX Successfully Launches New Satellite Constellation",
                description = "The latest Falcon 9 mission deploys 60 new satellites, expanding global internet coverage.",
                content = "SpaceX has successfully launched another batch of Starlink satellites, bringing the total constellation to over 5,000 active satellites in low Earth orbit.\n\nThe mission marks another milestone in the company's plan to provide global high-speed internet coverage, particularly to underserved and remote areas. The new satellites feature upgraded technology that improves bandwidth and reduces latency.\n\nAstronomers continue to express concerns about the impact of large satellite constellations on night sky observations, though SpaceX has implemented measures to reduce reflectivity in newer satellite models.",
                source = "Space.com",
                timeAgo = "1 hour ago",
                category = "Technology"
            ),
            NewsItem(
                id = "6",
                title = "New Study Reveals Benefits of Mediterranean Diet",
                description = "Research confirms that following a Mediterranean diet can significantly reduce risk of heart disease.",
                content = "A comprehensive new study published in the New England Journal of Medicine provides further evidence supporting the health benefits of the Mediterranean diet.\n\nThe 10-year study following over 50,000 participants found that those adhering to a Mediterranean diet had a 30% lower risk of cardiovascular disease and a 25% lower risk of type 2 diabetes.\n\nThe diet emphasizes fruits, vegetables, whole grains, legumes, nuts, and olive oil, with moderate consumption of fish and poultry. Researchers note that the benefits extend beyond physical health, with participants also reporting improved mental wellbeing.",
                source = "Health Daily",
                timeAgo = "2 hours ago",
                category = "Health"
            ),
            NewsItem(
                id = "7",
                title = "Tech Giants Report Strong Quarterly Earnings",
                description = "Major technology companies exceed analyst expectations with robust revenue growth.",
                content = "Leading technology companies have reported better-than-expected quarterly earnings, driven by strong growth in cloud computing services and artificial intelligence initiatives.\n\nThe results suggest that despite economic uncertainties, the technology sector continues to thrive. Cloud revenue growth was particularly strong, with enterprise adoption accelerating across industries.\n\nInvestors responded positively to the reports, with tech stocks rallying in after-hours trading. Analysts have raised their full-year forecasts for the sector, citing sustained demand for digital transformation services.",
                source = "CNBC",
                timeAgo = "3 hours ago",
                category = "Business"
            ),
            NewsItem(
                id = "8",
                title = "Climate Summit Reaches Historic Agreement",
                description = "World leaders commit to ambitious new targets for carbon emissions reduction.",
                content = "After two weeks of intense negotiations, world leaders have reached a landmark agreement on climate action, committing to more aggressive targets for reducing greenhouse gas emissions.\n\nThe agreement includes binding commitments to achieve net-zero emissions by 2050 and to phase out coal power in developed nations by 2035. Developing nations will receive increased financial support to transition to clean energy.\n\nEnvironmental groups have cautiously welcomed the agreement while emphasizing the need for rapid implementation. Scientists warn that current commitments, while improved, may still fall short of limiting global warming to 1.5 degrees Celsius.",
                source = "BBC News",
                timeAgo = "4 hours ago",
                category = "Science"
            ),
            NewsItem(
                id = "9",
                title = "Olympic Preparations Enter Final Stage",
                description = "Host city completes major infrastructure projects ahead of upcoming Olympic Games.",
                content = "With just months remaining before the opening ceremony, host city organizers have announced the completion of all major Olympic venues and infrastructure projects.\n\nThe new Olympic Village will house over 15,000 athletes and features state-of-the-art training facilities and sustainable design elements. Transportation improvements, including expanded public transit, will leave a lasting legacy for residents.\n\nSecurity preparations are also in their final phase, with organizers coordinating with international agencies to ensure a safe and successful Games. Ticket sales have exceeded expectations, with over 90% of available tickets already sold.",
                source = "Sports Illustrated",
                timeAgo = "5 hours ago",
                category = "Sports"
            ),
            NewsItem(
                id = "10",
                title = "Breakthrough in Cancer Research Announced",
                description = "New immunotherapy treatment shows promising results in early clinical trials.",
                content = "Medical researchers have announced promising results from early-stage clinical trials of a novel immunotherapy treatment that could transform cancer care.\n\nThe treatment, which uses personalized mRNA technology to train the immune system to recognize and attack cancer cells, showed positive responses in 75% of trial participants with advanced melanoma.\n\nWhile larger trials are needed to confirm the results, researchers are optimistic about the potential to apply this approach to other types of cancer. The treatment has shown fewer side effects compared to traditional chemotherapy.",
                source = "Medical News",
                timeAgo = "6 hours ago",
                category = "Health"
            )
        )

        breakingNewsAdapter.submitList(breakingNews)
        recommendedNewsAdapter.submitList(recommendedNews)
    }
}
