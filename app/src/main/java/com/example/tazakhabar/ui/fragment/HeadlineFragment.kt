package com.example.tazakhabar.ui.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.getSystemService
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tazakhabar.R
import com.example.tazakhabar.adapter.NewsAdapter
import com.example.tazakhabar.databinding.FragmentHeadlineBinding
import com.example.tazakhabar.ui.NewsActivity
import com.example.tazakhabar.ui.NewsViewModel
import com.example.tazakhabar.util.Constants
import com.example.tazakhabar.util.Resource


class HeadlineFragment : Fragment(R.layout.fragment_headline) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter:NewsAdapter
    lateinit var retryButton:Button
    lateinit var errorText:TextView
    lateinit var itemHeadlineError:CardView
    lateinit var binding:FragmentHeadlineBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentHeadlineBinding.bind(view)


        itemHeadlineError=view.findViewById(R.id.itemHeadlinesError)
        var inflater=requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view:View=inflater.inflate(R.layout.item_error, null)

        retryButton=view.findViewById(R.id.retryButton)
        errorText=view.findViewById(R.id.errorText)

        newsViewModel=(activity as NewsActivity).newsViewModel
        setupHeadlineRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle=Bundle().apply {
                putSerializable("article", it)
            }

            findNavController().navigate(R.id.action_headlineFragment_to_articleFragment, bundle)
        }

        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response->

            when(response){

                is Resource.Success<*> ->{
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let {newsResponse->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages=newsResponse.totalResults/Constants.QUERY_PAGE_SIZE + 2
                        isLastPage=newsViewModel.headlinePage==totalPages
                        if (isLastPage){
                            binding.recyclerHeadlines.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error<*> ->{
                    hideProgressBar()
                    response.message?.let {message->
                        Toast.makeText(activity, "Sorry error: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }


                is Resource.Loading<*> ->{
                    showProgressBar()
                }
            }
        })

        retryButton.setOnClickListener {
            newsViewModel.getHeadlines("us")
        }

    }



    var isError=false
    var isLoading=false
    var isLastPage=false
    var isScrolling=false

    private fun hideProgressBar(){
        binding.paginationProgressBar.visibility=View.INVISIBLE
        isLoading=false
    }
    private fun showProgressBar(){
        binding.paginationProgressBar.visibility=View.VISIBLE
        isLoading=true
    }

    private fun hideErrorMessage(){
        itemHeadlineError.visibility=View.INVISIBLE
        isError=false
    }

    private fun showErrorMessage(message:String){
        itemHeadlineError.visibility=View.VISIBLE
        errorText.text=message
        isError=true
    }

    //pagination: used to load and display small chunks of data at a time
    val scrollListener=object :RecyclerView.OnScrollListener() {

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibileItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoError = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibileItemPosition + visibleItemCount >= totalItemCount
            val isNotBeginning = firstVisibileItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE


            val shouldPaginate =
                isNoError && isNotLoadingAndNotLastPage && isAtLastItem && isNotBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate){
                newsViewModel.getHeadlines("us")
                isScrolling
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupHeadlineRecyclerView(){
        newsAdapter= NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter=newsAdapter
            layoutManager=LinearLayoutManager(activity)
            addOnScrollListener(this@HeadlineFragment.scrollListener)
        }
    }

}