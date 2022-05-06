package com.equationl.giteetodo.data.repos.model.pagingSource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.giteetodo.data.repos.ReposApi
import com.equationl.giteetodo.data.repos.model.response.Issues
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.common.getIssueState
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.viewmodel.QueryParameter
import com.equationl.giteetodo.viewmodel.TodoCardData
import com.equationl.giteetodo.viewmodel.TodoCardItemData
import retrofit2.HttpException
import retrofit2.Response

private const val TAG = "el, IssuesPagingSource"

class IssuesPagingSource(
    private val reposApi: ReposApi,
    private val queryParameter: QueryParameter
) : PagingSource<Int, TodoCardData>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, TodoCardData> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val repoPath = queryParameter.repoPath

            if (repoPath == "null/null") {
                return LoadResult.Error(IllegalArgumentException("路径为空！"))
            }

            val response = reposApi.getAllIssues(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                queryParameter.accessToken,
                state = queryParameter.state,
                page = nextPageNumber,
                perPage = params.loadSize
            )

            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = (response.headers()["total_page"] ?: "-1").toIntOrNull() ?: -1

            val resultData = resolveIssue(response)

            return LoadResult.Page(
                data = resultData,
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TodoCardData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private fun resolveIssue(response: Response<List<Issues>>): List<TodoCardData> {
        // fixme 直接这样解析可能会造成出现多个同一日期组

        val issueList = response.body()
        if (issueList.isNullOrEmpty()) {
            return emptyList()
        }
        val todoCardDataList = arrayListOf<TodoCardData>()
        var currentDate = Utils.getDateTimeString(issueList[0].createdAt)
        val currentItem = arrayListOf<TodoCardItemData>()

        for (issue in issueList) {
            val issueDate = Utils.getDateTimeString(issue.createdAt)
            Log.i(TAG, "loadIssues: issue=${issue.title}, date=${issue.createdAt}, currentDate=$currentDate, issueDate=$issueDate")
            if (issueDate != currentDate) {
                val tempItem: ArrayList<TodoCardItemData> = arrayListOf()
                tempItem.addAll(currentItem)
                todoCardDataList.add(
                    TodoCardData(currentDate, tempItem)
                )
                Log.i(TAG, "loadIssues: 添加：$currentDate, $tempItem, $currentItem")
                currentDate = issueDate
                currentItem.clear()

                val state = try { IssueState.valueOf(issue.state.uppercase()) } catch (e: IllegalArgumentException) { IssueState.OPEN }
                currentItem.add(
                    TodoCardItemData(issue.title, state, issue.number)
                )
            }
            else {
                val state = getIssueState(issue.state)
                currentItem.add(
                    TodoCardItemData(issue.title, state, issue.number)
                )
            }
        }

        val tempItem: ArrayList<TodoCardItemData> = arrayListOf()
        tempItem.addAll(currentItem)
        todoCardDataList.add(
            TodoCardData(currentDate, tempItem)
        )

        Log.i(TAG, "loadIssues: cardList=$todoCardDataList")
        return todoCardDataList
    }
}