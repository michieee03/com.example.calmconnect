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
import com.example.calmconnect.controller.impl.StressReliefControllerImpl
import com.example.calmconnect.databinding.FragmentJournalBinding
import com.example.calmconnect.db.entity.JournalEntry
import com.example.calmconnect.util.Result
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalFragment(
    private val controller: StressReliefControllerImpl
) : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = JournalAdapter(dateFormat)
        binding.recyclerViewJournal.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewJournal.adapter = adapter

        controller.getJournalEntries().observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
        }

        binding.btnSaveJournal.setOnClickListener {
            val text = binding.editTextJournal.text.toString()
            val result = controller.saveJournalEntry(text, System.currentTimeMillis())
            when (result) {
                is Result.Success -> {
                    binding.editTextJournal.text?.clear()
                    Toast.makeText(requireContext(), "Entry saved", Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class JournalAdapter(
        private val fmt: SimpleDateFormat
    ) : RecyclerView.Adapter<JournalAdapter.VH>() {

        private var items: List<JournalEntry> = emptyList()

        fun submitList(list: List<JournalEntry>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_journal_entry, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val entry = items[position]
            holder.tvDate.text = fmt.format(Date(entry.timestamp))
            holder.tvText.text = entry.text
        }

        override fun getItemCount() = items.size

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvDate: TextView = v.findViewById(R.id.tvJournalDate)
            val tvText: TextView = v.findViewById(R.id.tvJournalText)
        }
    }
}
