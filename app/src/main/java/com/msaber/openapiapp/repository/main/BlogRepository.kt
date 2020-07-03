package com.msaber.openapiapp.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import com.msaber.openapiapp.api.GenericResponse
import com.msaber.openapiapp.api.main.OpenApiMainService
import com.msaber.openapiapp.api.main.reponse.BlogCreateUpdateResponse
import com.msaber.openapiapp.api.main.reponse.BlogListSearchResponse
import com.msaber.openapiapp.model.AuthToken
import com.msaber.openapiapp.model.BlogPost
import com.msaber.openapiapp.persistence.BlogPostDao
import com.msaber.openapiapp.persistence.returnOrderedBlogQuery
import com.msaber.openapiapp.repository.JobManager
import com.msaber.openapiapp.repository.NetworkBoundResource
import com.msaber.openapiapp.session.SessionManager
import com.msaber.openapiapp.ui.DataState
import com.msaber.openapiapp.ui.Response
import com.msaber.openapiapp.ui.ResponseType
import com.msaber.openapiapp.ui.main.blog.state.BlogViewState
import com.msaber.openapiapp.util.AbsentLiveData
import com.msaber.openapiapp.util.ApiSuccessResponse
import com.msaber.openapiapp.util.DateUtils
import com.msaber.openapiapp.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.msaber.openapiapp.util.GenericApiResponse
import com.msaber.openapiapp.util.constant.Constants
import com.msaber.openapiapp.util.constant.Constants.Companion.PAGINATION_PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class BlogRepository @Inject constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("BlogRepository") {

    private val TAG = "BlogRepository"


    fun searchBlogPost(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            sessionManager.isInternetAvailable(),
            true,
            false,
            true
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    //Finish bu viewing db.cache
                    result.addSource(loadFromCache()) { viewState ->
                        viewState.blogFields.isQueryInProgress = false
                        if (page * Constants.PAGINATION_PAGE_SIZE > viewState.blogFields.blogList.size) {
                            //no more search results from cache
                            //there is another method to check for no more result error from network in the fragment
                            viewState.blogFields.isQueryExhausted = true
                        }
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {
                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for (blogPostResponse in response.body.results) {
                    blogPostList.add(
                        BlogPost(
                            pk = blogPostResponse.pk,
                            title = blogPostResponse.title,
                            slug = blogPostResponse.slug,
                            body = blogPostResponse.body,
                            image = blogPostResponse.image,
                            date_updated = DateUtils.convertServerStringDateToLong(
                                blogPostResponse.date_updated
                            ),
                            username = blogPostResponse.username
                        )
                    )
                }
                updateLocalDB(blogPostList)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {

                return openApiMainService.searchListBlogPosts(
                    "Token ${authToken.token}",
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.returnOrderedBlogQuery(query, filterAndOrder, page).switchMap {
                    object : LiveData<BlogViewState>() {
                        override fun onActive() {
                            super.onActive()
                            value = BlogViewState(
                                BlogViewState.BlogFields(
                                    blogList = it,
                                    isQueryInProgress = true
                                )
                            )
                        }
                    }
                }
            }

            override suspend fun updateLocalDB(cacheObject: List<BlogPost>?) {
                if (cacheObject != null) {
                    withContext(IO) {
                        for (blogPost in cacheObject) {
                            try {
                                //launch each blog as a separate jon to executed in parallel
                                val j = launch {
                                    Log.d(TAG, "updateLocalDB: inserting Blog")
                                    blogPostDao.insert(blogPost)
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "updateLocalDB: error update cache on blogPost ${blogPost.slug}"
                                )
                            }
                        }
                    }
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPost", job)
            }

        }.asLiveData()
    }


    fun restoreBlogListFromCache(
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        return object: NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            sessionManager.isInternetAvailable(),
            false,
            false,
            true
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main){
                    result.addSource(loadFromCache()){ viewState ->
                        viewState.blogFields.isQueryInProgress = false
                        if(page * PAGINATION_PAGE_SIZE > viewState.blogFields.blogList.size){
                            viewState.blogFields.isQueryExhausted = true
                        }
                        onCompleteJob(DataState.data(
                            viewState,
                            null
                        ))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<BlogListSearchResponse>
            ) {
                // ignore
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return AbsentLiveData.create()
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page)
                    .switchMap {
                        object: LiveData<BlogViewState>(){
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    BlogViewState.BlogFields(
                                        blogList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDB(cacheObject: List<BlogPost>?) {
                // ignore
            }

            override fun setJob(job: Job) {
                addJob("restoreBlogListFromCache", job)
            }

        }.asLiveData()
    }



    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, BlogViewState>(
            sessionManager.isInternetAvailable(),
            true,
            true,
            false
        ) {


            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main) {

                    Log.d(TAG, "handleApiSuccessResponse: ${response.body.response}")
                    if (response.body.response.equals(RESPONSE_NO_PERMISSION_TO_EDIT)) {
                        onCompleteJob(
                            DataState.data(
                                data = BlogViewState(
                                    viewBlogFields = BlogViewState.ViewBlogFields(
                                        isAuthorOfBlogPost = false
                                    )
                                ),
                                response = null
                            )
                        )
                    } else if (response.body.response.equals(RESPONSE_HAS_PERMISSION_TO_EDIT)) {
                        onCompleteJob(
                            DataState.data(
                                data = BlogViewState(
                                    viewBlogFields = BlogViewState.ViewBlogFields(
                                        isAuthorOfBlogPost = true
                                    )
                                ),
                                response = null
                            )
                        )
                    } else {
                        onErrorReturn(
                            ERROR_UNKNOWN,
                            shouldUseDialog = false,
                            shouldUseToast = false
                        )
                    }
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            // Make an update and change nothing.
            // If they are not the author it will return: "You don't have permission to edit that."
            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.isAuthorOfBlogPost(
                    "Token ${authToken.token!!}",
                    slug
                )
            }

            // not applicable
            override suspend fun updateLocalDB(cacheObject: Any?) {

            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }


        }.asLiveData()
    }

    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, BlogPost, BlogViewState>(
            sessionManager.isInternetAvailable(),
            true,
            true,
            false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                if (response.body.response == SUCCESS_BLOG_DELETED) {
                    updateLocalDB(blogPost)
                } else {
                    onCompleteJob(
                        DataState.error(
                            Response(
                                ERROR_UNKNOWN,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.deleteBlogPost(
                    "Token ${authToken.token!!}",
                    blogPost.slug
                )
            }

            override suspend fun updateLocalDB(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.deleteBlogPost(blogPost)
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(SUCCESS_BLOG_DELETED, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("deleteBlogPost", job)
            }

        }.asLiveData()
    }


    fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, BlogViewState>(
            sessionManager.isInternetAvailable(),
            true,
            true,
            false
        ) {
            //not needed
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                val updateBlogPost =
                    BlogPost(
                        response.body.pk,
                        response.body.title,
                        response.body.slug,
                        response.body.body,
                        response.body.image,
                        DateUtils.convertServerStringDateToLong(response.body.date_updated),
                        response.body.username
                    )
                updateLocalDB(updateBlogPost)
                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            data = BlogViewState(
                                viewBlogFields = BlogViewState.ViewBlogFields(blogPost = updateBlogPost)
                            ),response = Response(response.body.response,ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
               return openApiMainService.updateBlog("Token ${authToken.token}",slug,title, body, image)
            }

            //not needed
            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDB(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.updateBlogPost(blogPost.pk,blogPost.title,blogPost.body,blogPost.image)
                }
            }

            override fun setJob(job: Job) {

                addJob("UpdateBlogPost",job)
            }

        }.asLiveData()

    }

}