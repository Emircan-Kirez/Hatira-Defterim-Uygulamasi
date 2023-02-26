package com.emircankirez.mymemories.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.emircankirez.mymemories.databinding.RecyclerRowBinding
import com.emircankirez.mymemories.model.Memory
import com.emircankirez.mymemories.view.MemoryListFragmentDirections

class MemoryAdapter(val memoryList : List<Memory>) : RecyclerView.Adapter<MemoryAdapter.MemoryHolder>() {
    class MemoryHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoryHolder(binding)
    }

    override fun getItemCount(): Int {
        return memoryList.size
    }

    override fun onBindViewHolder(holder: MemoryHolder, position: Int) {
        holder.binding.txtMemory.text = memoryList[position].memoryName
        holder.itemView.setOnClickListener {
            val action = MemoryListFragmentDirections.actionMemoryListFragmentToDetailsFragment("old", memoryList[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }


}