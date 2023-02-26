package com.emircankirez.mymemories.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation
import com.emircankirez.mymemories.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.memory_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_new_memory){
            val action = MemoryListFragmentDirections.actionMemoryListFragmentToDetailsFragment("new", 0)
            Navigation.findNavController(this, R.id.navHost).navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }
}