package com.msaber.openapiapp.repository.main

import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER
import com.msaber.openapiapp.api.main.OpenApiMainService
import com.msaber.openapiapp.api.main.reponse.BlogCreateUpdateResponse
import com.msaber.openapiapp.model.AuthToken
import com.msaber.openapiapp.model.BlogPost
import com.msaber.openapiapp.persistence.BlogPostDao
import com.msaber.openapiapp.repository.JobManager
import com.msaber.openapiapp.repository.NetworkBoundResource
import com.msaber.openapiapp.session.SessionManager
import com.msaber.openapiapp.ui.DataState
import com.msaber.openapiapp.ui.Response
import com.msaber.openapiapp.ui.ResponseType
import com.msaber.openapiapp.ui.main.create_blog.state.CreateBlogViewState
import com.msaber.openapiapp.util.AbsentLiveData
import com.msaber.openapiapp.util.ApiSuccessResponse
import com.msaber.openapiapp.util.DateUtils
import com.msaber.openapiapp.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogRepository @Inject constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
):JobManager("CreateBlogRepository"){


    private val TAG: String = "AppDebug"

    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateBlogViewState>> {
        return object :
            NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
                sessionManager.isInternetAvailable(),
                true,
                true,
                false
            ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {

                // If they don't have a paid membership account it will still return a 200
                // Need to account for that
                if (!response.body.response.equals(RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER)) {
                    val updatedBlogPost = BlogPost(
                        response.body.pk,
                        response.body.title,
                        response.body.slug,
                        response.body.body,
                        response.body.image,
                        DateUtils.convertServerStringDateToLong(response.body.date_updated),
                        response.body.username
                    )
                    updateLocalDB(updatedBlogPost)
                }

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        //this response will return "successful crated" ot "you don't have account"
                        DataState.data(
                            null,
                            Response(response.body.response, ResponseType.Dialog())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return openApiMainService.createBlog(
                    "Token ${authToken.token!!}",
                    title,
                    body,
                    image
                )
            }

            // not applicable
            override fun loadFromCache(): LiveData<CreateBlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDB(cacheObject: BlogPost?) {
                cacheObject?.let {
                    blogPostDao.insert(it)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewBlogPost", job)
            }

        }.asLiveData()
    }


}