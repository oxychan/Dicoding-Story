package com.dicoding.dicodingstory.data

import android.util.Log
import androidx.lifecycle.asLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.dicodingstory.data.local.LoginPreference
import com.dicoding.dicodingstory.data.response.Story
import com.dicoding.dicodingstory.data.response.StoryResponse
import com.dicoding.dicodingstory.data.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.reflect.TypeVariable

class StoryPagingSource(
    private val apiService: ApiService, private val loginPreference: LoginPreference
) : PagingSource<Int, Story>() {
    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val token = loginPreference.getAuthenticatedUser().first().token
            val position = params.key ?: INITIAL_PAGE_INDEX

            val responseData = apiService.getStoriesWithPaging(
                token = "Bearer $token",
                page = position,
                size = params.loadSize,
                location = 0
            )

            val typeVariables: List<Story> = responseData.listStory?.filterNotNull() ?: emptyList()

            LoadResult.Page(
                data = typeVariables,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (typeVariables.isNullOrEmpty()) null else position + 1
            )

        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }

    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}