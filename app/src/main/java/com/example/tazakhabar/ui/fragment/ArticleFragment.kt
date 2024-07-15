package com.example.tazakhabar.ui.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.navArgs
import com.example.tazakhabar.R
import com.example.tazakhabar.databinding.FragmentArticleBinding
import com.example.tazakhabar.ui.NewsActivity
import com.example.tazakhabar.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class ArticleFragment : Fragment(R.layout.fragment_article) {


    lateinit var newsViewModel: NewsViewModel
    val args:ArticleFragmentArgs by navArgs()
    lateinit var binding: FragmentArticleBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentArticleBinding.bind(view)

        newsViewModel=(activity as NewsActivity).newsViewModel
        val article=args.article
        binding.webView.apply {
            webViewClient= WebViewClient()
            article.url?.let {
                loadUrl(it)
            }
        }

        binding.fab.setOnClickListener {
            newsViewModel.addToFavourite(article)
            Snackbar.make(view,"Added To Favourite", Snackbar.LENGTH_SHORT).show()
        }
    }

}
