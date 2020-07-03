package com.msaber.openapiapp.persistence

import androidx.lifecycle.LiveData
import com.msaber.openapiapp.model.BlogPost
import com.msaber.openapiapp.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED
import com.msaber.openapiapp.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_USERNAME
import com.msaber.openapiapp.persistence.BlogQueryUtils.Companion.ORDER_BY_DESC_DATE_UPDATED
import com.msaber.openapiapp.persistence.BlogQueryUtils.Companion.ORDER_BY_DESC_USERNAME

class BlogQueryUtils {


    companion object {
        private val TAG: String = "AppDebug"

        // values
        const val BLOG_ORDER_ASC: String = ""
        const val BLOG_ORDER_DESC: String = "-"
        const val BLOG_FILTER_USERNAME = "username"
        const val BLOG_FILTER_DATE_UPDATED = "date_updated"

        val ORDER_BY_ASC_DATE_UPDATED = BLOG_ORDER_ASC + BLOG_FILTER_DATE_UPDATED //date
        val ORDER_BY_DESC_DATE_UPDATED = BLOG_ORDER_DESC + BLOG_FILTER_DATE_UPDATED //-date
        val ORDER_BY_ASC_USERNAME = BLOG_ORDER_ASC + BLOG_FILTER_USERNAME//username
        val ORDER_BY_DESC_USERNAME = BLOG_ORDER_DESC + BLOG_FILTER_USERNAME//-username
    }
}


fun BlogPostDao.returnOrderedBlogQuery(
    query: String,
    filterAndOrder: String,
    page: Int
): LiveData<List<BlogPost>> {

    when {

        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) -> {
            return searchBlogPostsOrderByDateDESC(query = query, page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) -> {
            return searchBlogPostsOrderByDateASC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_DESC_USERNAME) -> {
            return searchBlogPostsOrderByAuthorDESC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_ASC_USERNAME) -> {
            return searchBlogPostsOrderByAuthorASC(
                query = query,
                page = page
            )
        }
        else ->
            return searchBlogPostsOrderByDateASC(
                query = query,
                page = page
            )
    }
}