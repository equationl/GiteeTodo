package com.equationl.giteetodo.data.repos.paging.remoteMediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.equationl.giteetodo.data.repos.RepoApi
import com.equationl.giteetodo.data.repos.db.IssueDb
import com.equationl.giteetodo.data.repos.model.common.IssueRemoteKey
import com.equationl.giteetodo.data.repos.model.common.TodoShowData
import com.equationl.giteetodo.data.repos.model.response.Issue
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.viewmodel.QueryParameter
import retrofit2.HttpException
import retrofit2.Response
import java.io.InvalidObjectException

@OptIn(ExperimentalPagingApi::class)
class IssueRemoteMediator(
    private val queryParameter: QueryParameter,
    private val database: IssueDb,
    private val repoApi: RepoApi
) : RemoteMediator<Int, TodoShowData>() {
    private val issueDao = database.issue()
    private val issueKeyDao = database.issueRemoteKey()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH // 启动时不强制刷新
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, TodoShowData>): MediatorResult {
        return try {
            val nextPage = when (loadType) {
                /*
                * 如果当前状态为刷新的话则将加载页面设置为 1，也就是从头开始加载
                * */
                LoadType.REFRESH -> {
                    1
                }
                /*
                * 这个表示向上滑动，这里不需要处理向上滑动，所以直接返回完成
                * */
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                /*
                * 如果loadType是APPEND，那么我们将在列表中查找最后一项，并查看NewsRemoteKeys数据指定的下一页。
                * 使用函数getRemoteKeyForLastItem()我们可以获得最后一个NewsRemoteKeys ，
                * 这样我们就可以从它的nextKey值中获得适当的下一页编号。
                * 如果没有这样的对象，这意味着我们到达了分页的末尾，
                * 在这种情况下，我们将返回MediatorResult.Success并带有endOfPaginationReached = true 。
                * */
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                        ?: throw InvalidObjectException("Result is empty")
                    remoteKeys.nextKey ?: return MediatorResult.Success(true)
                }
            }

            val repoPath = queryParameter.repoPath

            if (repoPath == "null/null") {
                return MediatorResult.Error(IllegalArgumentException("路径为空！"))
            }
            val response = repoApi.getAllIssues(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                queryParameter.accessToken,
                labels = queryParameter.labels,
                state = queryParameter.state,
                direction = queryParameter.direction,
                createdAt = queryParameter.createdAt,
                page = nextPage,
                perPage = when (loadType) {
                    LoadType.REFRESH -> state.config.initialLoadSize
                    else -> state.config.pageSize
                }
            )

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val issueList = response.body() ?: emptyList()

            /*
            * 将从服务器获取到的数据重新解析
            *
            * 此步的目的：
            *   1. 服务器返回数据有上百个字段，而且每个字段都互相嵌套，如果都存进数据库不好理清各个字段之间的关系
            *   2. 本地实际使用到的字段不足十个，其他全是冗余字段，即使保存了也不会使用，不如索性不保存了
            * */
            val resultData = resolveIssue(response)

            val totalPage = (response.headers()["total_page"] ?: "-1").toIntOrNull() ?: -1

            database.withTransaction {
                if (loadType == LoadType.REFRESH) { // 如果刷新的话则先清除所有本地数据
                    issueKeyDao.clearAll()
                    issueDao.clearAll()
                }

                val prevKey = if (nextPage == 1) null else nextPage - 1
                val nextKey = if (nextPage >= totalPage || totalPage == -1) null else nextPage + 1
                val keys = issueList.map {
                    IssueRemoteKey(issueId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                issueKeyDao.insertAll(keys)
                issueDao.insertAll(resultData)
            }

            MediatorResult.Success(
                endOfPaginationReached = nextPage >= totalPage || totalPage == -1
            )
        } catch (tr: Throwable) {
            Log.w(TAG, "load: load data fail", tr)
            MediatorResult.Error(tr)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, TodoShowData>): IssueRemoteKey? {
        return state.lastItemOrNull()?.let { issue ->
            database.withTransaction { issueKeyDao.remoteKeysByNewsId(issue.id) }
        }
    }

    private fun resolveIssue(response: Response<List<Issue>>): List<TodoShowData> {
        return resolveIssueByDate(response)
    }

    private fun resolveIssueByDate(response: Response<List<Issue>>): List<TodoShowData> {
        val issueList = response.body()
        if (issueList.isNullOrEmpty()) {
            return emptyList()
        }
        val todoShowDataList = arrayListOf<TodoShowData>()

        for (issue in issueList) {
            val issueDate = Utils.getDateTimeString(issue.updatedAt)
            val issueState = try { IssueState.valueOf(issue.state.uppercase()) } catch (e: IllegalArgumentException) { IssueState.OPEN }

            todoShowDataList.add(
                TodoShowData(
                    id = issue.id,
                    title = issue.title,
                    number = issue.number,
                    state = issueState,
                    updateAt = issue.updatedAt,
                    createdAt = issue.createdAt,
                    labels = issue.labels,
                    headerTitle = issueDate
                )
            )
        }

        return todoShowDataList
    }

    companion object {
        private const val TAG = "el, IssueRemoteMediator"
    }
}