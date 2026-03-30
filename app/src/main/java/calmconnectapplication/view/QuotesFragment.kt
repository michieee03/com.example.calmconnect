package com.example.calmconnect.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calmconnect.R
import com.example.calmconnect.controller.impl.QuoteControllerImpl
import com.example.calmconnect.databinding.FragmentQuotesBinding
import com.example.calmconnect.db.AppDatabase
import com.example.calmconnect.db.entity.Quote
import com.example.calmconnect.model.QuoteRepository

class QuotesFragment : Fragment() {

    private var _binding: FragmentQuotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var quoteController: QuoteControllerImpl
    private var dailyQuote: Quote? = null

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

        val adapter = QuoteAdapter()
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = adapter

        // Load daily quote
        val quote = quoteController.getDailyQuote()
        dailyQuote = quote
        binding.tvQuoteText.text = "\"${quote.text}\""
        binding.tvQuoteAuthor.text = "— ${quote.author}"
        updateFavoriteButton(quote.isFavorite)

        // Observe favorites
        quoteController.getFavoriteQuotes().observe(viewLifecycleOwner) { favorites ->
            adapter.submitList(favorites)
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

        binding.btnSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a search term", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val results = quoteController.searchQuotes(query)
            adapter.submitList(results)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.btnFavorite.text = if (isFavorite) "★ Favorited" else "☆ Favorite"
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
            holder.tvText.text = "\"${q.text}\""
            holder.tvAuthor.text = "— ${q.author}"
        }

        override fun getItemCount() = items.size

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvText: TextView = v.findViewById(R.id.tvItemQuoteText)
            val tvAuthor: TextView = v.findViewById(R.id.tvItemQuoteAuthor)
        }
    }
}
