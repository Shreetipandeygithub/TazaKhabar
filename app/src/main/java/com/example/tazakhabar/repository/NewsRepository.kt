package com.example.tazakhabar.repository

import com.example.tazakhabar.api.retrofitInstance
import com.example.tazakhabar.db.ArticleDatabase
import com.example.tazakhabar.model.Article
import retrofit2.http.Query
import java.util.Locale.IsoCountryCode

//created after ArticleDatabase************
class NewsRepository(val db:ArticleDatabase){

    suspend fun getHeadline(countryCode: String, pageNumber: Int)=
        retrofitInstance.api.getHeadlines(countryCode,pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int)=
        retrofitInstance.api.searchForNews(searchQuery,pageNumber)

    suspend fun upsert(article: Article)=
        db.getArticleDao().upsert(article)

    fun getFavouriteNews()=db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article)=db
        .getArticleDao().deleteArticle(article)

}