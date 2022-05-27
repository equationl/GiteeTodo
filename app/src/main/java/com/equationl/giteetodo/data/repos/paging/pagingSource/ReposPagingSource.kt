package com.equationl.giteetodo.data.repos.paging.pagingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.giteetodo.data.repos.RepoApi
import com.equationl.giteetodo.data.user.model.response.Repo
import retrofit2.HttpException

private const val TAG = "el, ReposPagingSource"

class ReposPagingSource(
    private val repoApi: RepoApi,
    private val accessToken: String
) : PagingSource<Int, Repo>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Repo> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response = repoApi.getRepos(accessToken, page = nextPageNumber, perPage = params.loadSize)
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

    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}