package me.kosert.channelbus

interface EventCallback<T> {

    /**
     * This function will be called for every received event
     */
    fun onEvent(event: T)
}
