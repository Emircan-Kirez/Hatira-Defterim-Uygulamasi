package com.emircankirez.mymemories.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.emircankirez.mymemories.R
import com.emircankirez.mymemories.adapter.MemoryAdapter
import com.emircankirez.mymemories.databinding.FragmentMemoryListBinding
import com.emircankirez.mymemories.model.Memory
import com.emircankirez.mymemories.roomdb.MemoryDao
import com.emircankirez.mymemories.roomdb.MemoryDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MemoryListFragment : Fragment() {
    private lateinit var binding: FragmentMemoryListBinding
    private lateinit var memoryDatabase: MemoryDatabase
    private lateinit var memoryDao: MemoryDao
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        memoryDatabase = Room.databaseBuilder(requireContext(), MemoryDatabase::class.java, "Memories").build()
        memoryDao = memoryDatabase.memoryDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMemoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMemoryInfo()
    }

    private fun getMemoryInfo(){
        compositeDisposable.add(
            memoryDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(memoryList : List<Memory>){
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = MemoryAdapter(memoryList)
        binding.recyclerView.adapter = adapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}