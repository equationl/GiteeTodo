package com.equationl.giteetodo.util.event

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


/**
 * 事件消息传递
 *
 * */
object FlowBus {
    private const val TAG = "FlowBus"

    val sendEvents = MutableSharedFlow<MessageEvent>(extraBufferCapacity = Channel.UNLIMITED)

    val events = sendEvents.asSharedFlow()

}


class MessageEvent(val type: EventKey, vararg var params: Any?) {
    override fun toString(): String {
        return "type=$type, params=${params.asList()}"
    }
}