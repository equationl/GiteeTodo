package com.equationl.giteetodo.constants

object ChooseRepoType {
    const val WIDGET_SETTING = "widgetSetting"

    /**
     * 当前设置的小组件 ID，用于设置小组件的仓库
     *
     * 这里属于是偷懒了，直接缓存到了 Object 中
     * */
    var currentWidgetAppId = -1
}