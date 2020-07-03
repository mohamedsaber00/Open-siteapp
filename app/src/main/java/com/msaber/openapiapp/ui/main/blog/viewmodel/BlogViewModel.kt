package com.msaber.openapiapp.ui.main.blog.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.msaber.openapiapp.persistence.BlogQueryUtils
import com.msaber.openapiapp.repository.main.BlogRepository
import com.msaber.openapiapp.session.SessionManager
import com.msaber.openapiapp.ui.BaseViewModel
import com.msaber.openapiapp.ui.DataState
import com.msaber.openapiapp.ui.Loading
import com.msaber.openapiapp.ui.main.blog.viewmodel.*
import com.msaber.openapiapp.ui.main.blog.viewmodel.getSearchQuery
import com.msaber.openapiapp.ui.main.blog.state.BlogStateEvent
import com.msaber.openapiapp.ui.main.blog.state.BlogStateEvent.*
import com.msaber.openapiapp.ui.main.blog.state.BlogViewState
import com.msaber.openapiapp.util.AbsentLiveData
import com.codingwithmitch.openapi.util.PreferenceKeys.Companion.BLOG_FILTER
import com.codingwithmitch.openapi.util.PreferenceKeys.Companion.BLOG_ORDER
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
): BaseViewModel<BlogStateEvent, BlogViewState>(){

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )
        sharedPreferences.getString(
            BLOG_ORDER,
            BlogQueryUtils.BLOG_ORDER_ASC
        )?.let {
            setBlogOrder(
                it
            )
        }
    }

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when(stateEvent){

            is BlogSearchEvent -> {
                clearLayoutManagerState()
                return sessionManager.cachedToken.value?.let { authToken ->
                    Log.d(TAG, "Blog Search Event: got auth token.")
                    blogRepository.searchBlogPost(
                        authToken = authToken,
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage()
                    )
                }?: AbsentLiveData.create()
            }

            is RestoreBlogListFromCache -> {
                return blogRepository.restoreBlogListFromCache(
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage()
                )
            }

            is CheckAuthorOfBlogPost -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.isAuthorOfBlogPost(
                        authToken = authToken,
                        slug = getSlug()
                    )
                }?: AbsentLiveData.create()
            }

            is DeleteBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.deleteBlogPost(
                        authToken = authToken,
                        blogPost = getBlogPost()
                    )
                }?: AbsentLiveData.create()
            }

            is UpdateBlogPostEvent -> {

                return sessionManager.cachedToken.value?.let { authToken ->

                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    blogRepository.updateBlogPost(
                        authToken = authToken,
                        slug = getSlug(),
                        title = title,
                        body = body,
                        image = stateEvent.image
                    )
                }?: AbsentLiveData.create()
            }

            is None ->{
                return liveData {
                    emit(
                        DataState(
                            null,
                            Loading(false),
                            null
                        )
                    )
                }
            }
        }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun saveFilterOptions(filter: String, order: String){
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
        Log.d(TAG, "CLEARED...")
    }



}











