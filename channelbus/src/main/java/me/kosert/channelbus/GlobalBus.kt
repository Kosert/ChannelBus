package me.kosert.channelbus

import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * A simple global instance of [ChannelBus]
 */
@ExperimentalCoroutinesApi
object GlobalBus : ChannelBus()