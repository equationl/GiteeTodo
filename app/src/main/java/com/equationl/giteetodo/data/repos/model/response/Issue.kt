package com.equationl.giteetodo.data.repos.model.response


import com.google.gson.annotations.SerializedName

data class Issue(
    @SerializedName("assignee")
    val assignee: Assignee?,
    @SerializedName("body")
    val body: String?,
    @SerializedName("branch")
    val branch: String?,
    @SerializedName("collaborators")
    val collaborators: List<Any>,
    @SerializedName("comments")
    val comments: Int,
    @SerializedName("comments_url")
    val commentsUrl: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("deadline")
    val deadline: String?,
    @SerializedName("depth")
    val depth: Int,
    @SerializedName("finished_at")
    val finishedAt: String?,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("issue_state")
    val issueState: String,
    @SerializedName("issue_state_detail")
    val issueStateDetail: IssueStateDetail,
    @SerializedName("issue_type")
    val issueType: String,
    @SerializedName("issue_type_detail")
    val issueTypeDetail: IssueTypeDetail,
    @SerializedName("labels")
    val labels: List<Label>,
    @SerializedName("labels_url")
    val labelsUrl: String,
    @SerializedName("milestone")
    val milestone: Milestone?,
    @SerializedName("number")
    val number: String,
    @SerializedName("parent_id")
    val parentId: Int,
    @SerializedName("parent_url")
    val parentUrl: String?,
    @SerializedName("plan_started_at")
    val planStartedAt: String?,
    @SerializedName("priority")
    val priority: Int,
    @SerializedName("program")
    val program: Program?,
    @SerializedName("repository")
    val repository: Repository,
    @SerializedName("repository_url")
    val repositoryUrl: String,
    @SerializedName("scheduled_time")
    val scheduledTime: Double,
    @SerializedName("security_hole")
    val securityHole: Boolean,
    @SerializedName("state")
    val state: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("user")
    val user: SimpleUser
)

data class IssueStateDetail(
    @SerializedName("color")
    val color: String,
    @SerializedName("command")
    val command: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("serial")
    val serial: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class IssueTypeDetail(
    @SerializedName("color")
    val color: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("ident")
    val ident: String,
    @SerializedName("is_system")
    val isSystem: Boolean,
    @SerializedName("template")
    val template: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class Repository(
    @SerializedName("assignee")
    val assignee: List<Assignee>,
    @SerializedName("assignees_number")
    val assigneesNumber: Int,
    @SerializedName("assigner")
    val assigner: Assigner,
    @SerializedName("blobs_url")
    val blobsUrl: String,
    @SerializedName("branches_url")
    val branchesUrl: String,
    @SerializedName("can_comment")
    val canComment: Boolean,
    @SerializedName("collaborators_url")
    val collaboratorsUrl: String,
    @SerializedName("comments_url")
    val commentsUrl: String,
    @SerializedName("commits_url")
    val commitsUrl: String,
    @SerializedName("contributors_url")
    val contributorsUrl: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("default_branch")
    val defaultBranch: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("empty_repo")
    val emptyRepo: Boolean,
    @SerializedName("enterprise")
    val enterprise: Enterprise?,
    @SerializedName("fork")
    val fork: Boolean,
    @SerializedName("forks_count")
    val forksCount: Int,
    @SerializedName("forks_url")
    val forksUrl: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("gvp")
    val gvp: Boolean,
    @SerializedName("has_issues")
    val hasIssues: Boolean,
    @SerializedName("has_page")
    val hasPage: Boolean,
    @SerializedName("has_wiki")
    val hasWiki: Boolean,
    @SerializedName("homepage")
    val homepage: String?,
    @SerializedName("hooks_url")
    val hooksUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("human_name")
    val humanName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("internal")
    val internalName: Boolean,
    @SerializedName("issue_comment")
    val issueComment: Boolean?,
    @SerializedName("issue_comment_url")
    val issueCommentUrl: String,
    @SerializedName("issues_url")
    val issuesUrl: String,
    @SerializedName("keys_url")
    val keysUrl: String,
    @SerializedName("labels_url")
    val labelsUrl: String,
    @SerializedName("language")
    val language: String?,
    @SerializedName("license")
    val license: String?,
    @SerializedName("members")
    val members: List<String?>,
    @SerializedName("milestones_url")
    val milestonesUrl: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("namespace")
    val namespace: Namespace,
    @SerializedName("notifications_url")
    val notificationsUrl: String,
    @SerializedName("open_issues_count")
    val openIssuesCount: Int,
    @SerializedName("outsourced")
    val outsourced: Boolean,
    @SerializedName("owner")
    val owner: Owner,
    @SerializedName("paas")
    val paas: String?,
    @SerializedName("parent")
    val parent: String?,
    @SerializedName("path")
    val path: String,
    @SerializedName("private")
    val privateName: Boolean,
    @SerializedName("programs")
    val programs: List<String?>? = null,
    @SerializedName("project_creator")
    val projectCreator: String,
    @SerializedName("project_labels")
    val projectLabels: List<ProjectLabels>,
    @SerializedName("public")
    val publicName: Boolean,
    @SerializedName("pull_requests_enabled")
    val pullRequestsEnabled: Boolean,
    @SerializedName("pulls_url")
    val pullsUrl: String,
    @SerializedName("pushed_at")
    val pushedAt: String,
    @SerializedName("recommend")
    val recommend: Boolean,
    @SerializedName("releases_url")
    val releasesUrl: String,
    @SerializedName("ssh_url")
    val sshUrl: String,
    @SerializedName("stargazers_count")
    val stargazersCount: Int,
    @SerializedName("stargazers_url")
    val stargazersUrl: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("tags_url")
    val tagsUrl: String,
    @SerializedName("testers")
    val testers: List<Tester>? = null,
    @SerializedName("testers_number")
    val testersNumber: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("watchers_count")
    val watchersCount: Int
)

data class Assignee(
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("events_url")
    val eventsUrl: String,
    @SerializedName("followers_url")
    val followersUrl: String,
    @SerializedName("following_url")
    val followingUrl: String,
    @SerializedName("gists_url")
    val gistsUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("organizations_url")
    val organizationsUrl: String,
    @SerializedName("received_events_url")
    val receivedEventsUrl: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("repos_url")
    val reposUrl: String,
    @SerializedName("starred_url")
    val starredUrl: String,
    @SerializedName("subscriptions_url")
    val subscriptionsUrl: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("url")
    val url: String
)

data class Assigner(
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("events_url")
    val eventsUrl: String,
    @SerializedName("followers_url")
    val followersUrl: String,
    @SerializedName("following_url")
    val followingUrl: String,
    @SerializedName("gists_url")
    val gistsUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("organizations_url")
    val organizationsUrl: String,
    @SerializedName("received_events_url")
    val receivedEventsUrl: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("repos_url")
    val reposUrl: String,
    @SerializedName("starred_url")
    val starredUrl: String,
    @SerializedName("subscriptions_url")
    val subscriptionsUrl: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("url")
    val url: String
)

data class Namespace(
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("path")
    val path: String,
    @SerializedName("type")
    val type: String
)

data class Owner(
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("events_url")
    val eventsUrl: String,
    @SerializedName("followers_url")
    val followersUrl: String,
    @SerializedName("following_url")
    val followingUrl: String,
    @SerializedName("gists_url")
    val gistsUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("organizations_url")
    val organizationsUrl: String,
    @SerializedName("received_events_url")
    val receivedEventsUrl: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("repos_url")
    val reposUrl: String,
    @SerializedName("starred_url")
    val starredUrl: String,
    @SerializedName("subscriptions_url")
    val subscriptionsUrl: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("url")
    val url: String
)

data class Tester(
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("events_url")
    val eventsUrl: String,
    @SerializedName("followers_url")
    val followersUrl: String,
    @SerializedName("following_url")
    val followingUrl: String,
    @SerializedName("gists_url")
    val gistsUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("organizations_url")
    val organizationsUrl: String,
    @SerializedName("received_events_url")
    val receivedEventsUrl: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("repos_url")
    val reposUrl: String,
    @SerializedName("starred_url")
    val starredUrl: String,
    @SerializedName("subscriptions_url")
    val subscriptionsUrl: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("url")
    val url: String
)

data class Milestone(
    val url: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    val number: Int,
    @SerializedName("repository_id")
    val repositoryId: Int,
    val state: String,
    val title: String,
    val description: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("open_issues")
    val openIssues: Int,
    @SerializedName("closed_issues")
    val closedIssues: Int,
    @SerializedName("due_on")
    val dueOn: String,
)

data class Program(
    val id: Int,
    val name: String,
    val description: String
)

data class Enterprise(
    val id: Int,
    val type: String,
    val name: String,
    val path: String,
    @SerializedName("html_url")
    val htmlUrl: String
)

data class ProjectLabels(
    val id: Int,
    val name: String,
    val ident: String
)