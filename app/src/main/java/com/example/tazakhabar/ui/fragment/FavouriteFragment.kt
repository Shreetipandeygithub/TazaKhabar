package com.example.tazakhabar.ui.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tazakhabar.R
import com.example.tazakhabar.adapter.NewsAdapter
import com.example.tazakhabar.databinding.FragmentFavouriteBinding
import com.example.tazakhabar.ui.NewsActivity
import com.example.tazakhabar.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar


class FavouriteFragment : Fragment(R.layout.fragment_favourite) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var binding: FragmentFavouriteBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentFavouriteBinding.bind(view)


        newsViewModel=(activity as NewsActivity).newsViewModel
        setUpFavouriteRecycler()

        newsAdapter.setOnItemClickListener {
            val bundle=Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_favouriteFragment_to_articleFragment, bundle)
        }

        val itemTouchHelperCallback=object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.differ.currentList[position]
                newsViewModel.deleteFavourite(article)
                Snackbar.make(view, "Removed from Favourite", Snackbar.LENGTH_SHORT).apply {
                    setAction("Undo") {
                        newsViewModel.addToFavourite(article)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.recyclerFavourites)
        }

        newsViewModel.getFavouriteNews().observe(viewLifecycleOwner, Observer { articles->
            newsAdapter.differ.submitList(articles)
        })
    }



    private fun setUpFavouriteRecycler(){
        newsAdapter= NewsAdapter()
        binding.recyclerFavourites.apply {
            adapter=newsAdapter
            layoutManager= LinearLayoutManager(activity)
        }
    }

}