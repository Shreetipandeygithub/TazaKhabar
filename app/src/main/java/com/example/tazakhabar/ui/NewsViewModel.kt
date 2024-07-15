package com.example.tazakhabar.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tazakhabar.model.Article
import com.example.tazakhabar.model.NewsResponse
import com.example.tazakhabar.repository.NewsRepository
import com.example.tazakhabar.util.Resource
import kotlinx.coroutines.launch
import okio.IOException

@RequiresApi(Build.VERSION_CODES.M)
class NewsViewModel(app:Application, val newsRepository: NewsRepository):AndroidViewModel(app) {

    val headlines:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinePage=1
    var headlineResponse:NewsResponse?=null


    val searchNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage=1
    var searchNewsResponse: NewsResponse?=null
    var newSearchQuery:String?=null
    var  oldSearchQuery:String?=null

    init {
        getHeadlines("us")
    }


    fun getHeadlines(countryCode: String)=viewModelScope.launch {
        headlineInternet(countryCode)
    }


    fun searchNews(searchQuery: String)=viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

   private fun handleHeadlineResponse(response: retrofit2.Response<NewsResponse>):Resource<NewsResponse>{
       if (response.isSuccessful){
           response.body()?.let { resultResponse ->
               headlinePage++
               if (headlineResponse==null){
                   headlineResponse=resultResponse
               }else{
                   val oldArticles=headlineResponse?.articles
                   val newArticles=resultResponse.articles
                   oldArticles?.addAll(newArticles)
               }

               return Resource.Success(headlineResponse?:resultResponse)
           }
       }
       return Resource.Error(response.message())
   }

    private fun handleSearchNewsRespond(response: retrofit2.Response<NewsResponse>):Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                if (searchNewsResponse==null || newSearchQuery!=oldSearchQuery){
                    searchNewsPage=1
                    oldSearchQuery=newSearchQuery
                    searchNewsResponse=resultResponse
                }else{
                    searchNewsPage++
                    val oldArticle=searchNewsResponse?.articles
                    val newArticles=resultResponse.articles
                    oldArticle?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    //add news to favourite
    fun addToFavourite(article: Article)=viewModelScope.launch {
        newsRepository.upsert(article)
    }

    //get favourite news
    fun getFavouriteNews()=newsRepository.getFavouriteNews()


    //delete news from favourite
    fun deleteFavourite(article: Article)=viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    //checking internet connection
    fun internetConnection(context: Context):Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when{
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else ->false

                }
            }?:false
        }
    }


    //headline internetConnection
    private suspend fun headlineInternet(countryCode:String){
        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())){
                val resource=newsRepository.getHeadline(countryCode , headlinePage)
                headlines.postValue(handleHeadlineResponse(resource))
            }else{
                headlines.postValue(Resource.Error("No internet "))
            }
        }catch (t:Throwable){
            when(t){
                is IOException-> headlines.postValue(Resource.Error("Unable to connect"))
                else-> headlines.postValue(Resource.Error("No signal"))
            }
        }
    }


    //search new internet connection
    private suspend fun searchNewsInternet(searchQuery:String){
        newSearchQuery=searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())){
                val response=newsRepository.searchNews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchNewsRespond(response))
            }else{
                searchNews.postValue(Resource.Error("No Internet Connection"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException->searchNews.postValue(Resource.Error("Unable to connect"))
                else-> searchNews.postValue(Resource.Error("No signal"))
            }
        }
    }

}
