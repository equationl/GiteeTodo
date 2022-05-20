package com.equationl.giteetodo.data.repos.model.pagingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.giteetodo.data.repos.ReposApi
import com.equationl.giteetodo.data.repos.model.response.Issues
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.common.getIssueState
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.viewmodel.GroupBy
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
                labels = queryParameter.labels,
                state = queryParameter.state,
                direction = queryParameter.direction,
                createdAt = queryParameter.createdAt,
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
        // fixme 直接这样解析分组数据会由于是分页加载的导致出现多个相同分组

        return when (DataStoreUtils.getSyncData(DataKey.SettingGroupBy, GroupBy.Date.name)) {
            GroupBy.Date.name -> {
                resolveIssueByDate(response)
            }
            GroupBy.Label.name -> {
                resolveIssueByLabel(response)
            }
            GroupBy.State.name -> {
                resolveIssueByState(response)
            }
            else -> {
                resolveIssueByDate(response)
            }
        }
    }

    private fun resolveIssueByDate(response: Response<List<Issues>>): List<TodoCardData> {
        val issueList = response.body()
        if (issueList.isNullOrEmpty()) {
            return emptyList()
        }
        val todoCardDataList = arrayListOf<TodoCardData>()
        var currentDate = Utils.getDateTimeString(issueList[0].createdAt)
        val currentItem = arrayListOf<TodoCardItemData>()

        for (issue in issueList) {
            val issueDate = Utils.getDateTimeString(issue.createdAt)
            //Log.i(TAG, "loadIssues: issue=${issue.title}, date=${issue.createdAt}, currentDate=$currentDate, issueDate=$issueDate")
            if (issueDate != currentDate) {
                val tempItem: ArrayList<TodoCardItemData> = arrayListOf()
                tempItem.addAll(currentItem)
                todoCardDataList.add(
                    TodoCardData(currentDate, tempItem)
                )
                //Log.i(TAG, "loadIssues: 添加：$currentDate, $tempItem, $currentItem")
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

        //Log.i(TAG, "loadIssues: cardList=$todoCardDataList")
        return todoCardDataList
    }

    private fun resolveIssueByLabel(response: Response<List<Issues>>): List<TodoCardData> {
        val issueList = response.body()
        if (issueList.isNullOrEmpty()) {
            return emptyList()
        }
        val todoCardDataList = arrayListOf<TodoCardData>()

        for (issue in issueList) {
            val labelList: List<Label> = issue.labels
            val state = try { IssueState.valueOf(issue.state.uppercase()) } catch (e: IllegalArgumentException) { IssueState.OPEN }
            val title = issue.title
            val number = issue.number

            if (labelList.isEmpty()) {
                val index = todoCardDataList.indexOfFirst{ it.cardTitle == "无标签" }
                if (index == -1) {
                    todoCardDataList.add(TodoCardData("无标签", arrayListOf(TodoCardItemData(title, state, number))))
                }
                else {
                    todoCardDataList[index].itemArray.add(TodoCardItemData(title, state, number))
                }
            }
            else {
                for (label in labelList) {
                    val index = todoCardDataList.indexOfFirst{ it.cardTitle == label.name }
                    if (index == -1) {
                        todoCardDataList.add(TodoCardData(label.name, arrayListOf(TodoCardItemData(title, state, number))))
                    }
                    else {
                        todoCardDataList[index].itemArray.add(TodoCardItemData(title, state, number))
                    }
                }
            }
        }
        return todoCardDataList
    }

    private fun resolveIssueByState(response: Response<List<Issues>>): List<TodoCardData> {
        val issueList = response.body()
        if (issueList.isNullOrEmpty()) {
            return emptyList()
        }
        val todoCardDataList = arrayListOf<TodoCardData>()

        for (issue in issueList) {
            val state = try { IssueState.valueOf(issue.state.uppercase()) } catch (e: IllegalArgumentException) { IssueState.OPEN }
            val index = todoCardDataList.indexOfFirst{ it.cardTitle == state.humanName }

            if (index == -1) {
                todoCardDataList.add(TodoCardData(state.humanName, arrayListOf(TodoCardItemData(issue.title, state, issue.number))))
            }
            else {
                todoCardDataList[index].itemArray.add(TodoCardItemData(issue.title, state, issue.number))
            }
        }
        return todoCardDataList
    }
}