package com.equationl.giteetodo.constants

object DefaultText {
    const val README_FILE_NAME = "README.md"
    const val README_CONTENT = """
            
    ***
    
    使用 [GiteeTodo](https://github.com/equationl/GiteeTodo) 创建。

    > GiteeTodo 是一款基于 compose + viewmodel + Retrofit 实现的 MVI 架构 TODO 应用；使用 Gitee（码云）的 issue 作为储存仓库。
            
        """


    const val ABOUT_CONTENT = """
        ## 关于
        GiteeTodo 是一款基于 compose + viewmodel + Retrofit 实现的 MVI 架构 TODO 应用；使用 Gitee（码云）的 issue 作为储存仓库。
        
        ### 主要功能
        - 查看仓库列表（仅获取类型为 *个人* ，且登录账号为 *创建者* 的仓库）
        - 新建仓库
        - 根据仓库查看 ISSUE 列表（支持筛选）
        - 新建 ISSUE
        - 快速标记 ISSUE 完成状态
        - 查看某个 ISSUE 详情
        - 编辑某个 ISSUE 详情
        - 支持标签（label）管理
        - 支持查看、新建、编辑、删除 ISSUE 评论
        - 适配深色模式
        
        ### 源码地址
        [GiteeTodo](https://github.com/equationl/GiteeTodo)
        
        ### 联系我
        email: admin@likehide.com
        
        website: [www.likehide.com](http://www.likehide.com)
        
        其他APP：[app.likehide.com](http://app.likehide.com)
        
    """
}