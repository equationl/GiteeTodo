package com.equationl.giteetodo.data.repos.model.pagingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.giteetodo.data.repos.ReposApi
import com.equationl.giteetodo.data.user.model.response.Repos
import retrofit2.HttpException

private const val TAG = "el, ReposPagingSource"

class ReposPagingSource(
    private val reposApi: ReposApi,
    private val accessToken: String
) : PagingSource<Int, Repos>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Repos> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response = reposApi.getRepos(accessToken, page = nextPageNumber, perPage = params.loadSize)
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = (response.headers()["total_page"] ?: "-1").toIntOrNull() ?: -1
            return LoadResult.Page(
                data = response.body() ?: listOf(),
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Repos>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}