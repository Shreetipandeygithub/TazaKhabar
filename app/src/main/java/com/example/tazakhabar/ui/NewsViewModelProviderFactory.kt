package com.example.tazakhabar.ui

import android.app.Application
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tazakhabar.repository.NewsRepository

class NewsViewModelProviderFactory(val app:Application, val newsRepository: NewsRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NewsViewModel(app, newsRepository) as T
        } else {
            TODO("VERSION.SDK_INT < M")
        }
    }
}