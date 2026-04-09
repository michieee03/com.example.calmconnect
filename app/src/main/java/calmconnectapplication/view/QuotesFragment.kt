package calmconnectapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import calmconnectapplication.R
import calmconnectapplication.controller.impl.QuoteControllerImpl
import calmconnectapplication.databinding.FragmentQuotesBinding
import calmconnectapplication.db.AppDatabase
import calmconnectapplication.db.entity.Quote
import calmconnectapplication.model.QuoteRepository

class QuotesFragment : Fragment() {

    private var _binding: FragmentQuotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var quoteController: QuoteControllerImpl
    private var dailyQuote: Quote? = null
    private var isShowingSearchResults = false

    private val adapter = QuoteAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        quoteController = QuoteControllerImpl(QuoteRepository(db.quoteDao()))

        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = adapter

        // Load daily quote
        val quote = quoteController.getDailyQuote()
        dailyQuote = quote
        binding.tvQuoteText.text = "\u201C${quote.text}\u201D"
        binding.tvQuoteAuthor.text = "— ${quote.author}"
        updateFavoriteButton(quote.isFavorite)

        // Observe favorites (default view)
        quoteController.getFavoriteQuotes().observe(viewLifecycleOwner) { favorites ->
            if (!isShowingSearchResults) {
                showFavorites(favorites)
            }
        }

        binding.btnFavorite.setOnClickListener {
            val q = dailyQuote ?: return@setOnClickListener
            if (q.isFavorite) {
                quoteController.removeFavorite(q.id)
                dailyQuote = q.copy(isFavorite = false)
            } else {
                quoteController.saveToFavorites(q.id)
                dailyQuote = q.copy(isFavorite = true)
            }
            updateFavoriteButton(dailyQuote!!.isFavorite)
        }

        // Live search as user types
        binding.editTextSearch.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isEmpty()) {
                isShowingSearchResults = false
                binding.tvSectionLabel.text = "⭐ Saved Favorites"
                binding.tvResultCount.visibility = View.GONE
                // Re-observe favorites to refresh the list
                quoteController.getFavoriteQuotes().observe(viewLifecycleOwner) { favorites ->
                    if (!isShowingSearchResults) showFavorites(favorites)
                }
            }
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Type a keyword or author to search", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val results = quoteController.searchQuotes(query)
            isShowingSearchResults = true
            binding.tvSectionLabel.text = "🔍 Search Results"
            binding.tvResultCount.text = "${results.size} found"
            binding.tvResultCount.visibility = View.VISIBLE
            showResults(results)
        }

        binding.btnClose.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showFavorites(list: List<Quote>) {
        adapter.submitList(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerViewFavorites.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showResults(list: List<Quote>) {
        adapter.submitList(list)
        if (list.isEmpty()) {
            binding.tvEmpty.text = "No quotes found.\nTry a different keyword."
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerViewFavorites.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerViewFavorites.visibility = View.VISIBLE
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.btnFavorite.text = if (isFavorite) "★  Saved to Favorites" else "☆  Save to Favorites"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class QuoteAdapter : RecyclerView.Adapter<QuoteAdapter.VH>() {
        private var items: List<Quote> = emptyList()

        fun submitList(list: List<Quote>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_quote, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val q = items[position]
            holder.tvText.text = "\u201C${q.text}\u201D"
            holder.tvAuthor.text = "— ${q.author}"
        }

        override fun getItemCount() = items.size

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvText: TextView = v.findViewById(R.id.tvItemQuoteText)
            val tvAuthor: TextView = v.findViewById(R.id.tvItemQuoteAuthor)
        }
    }
}
