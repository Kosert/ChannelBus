package me.kosert.channelbus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.*

/**
 * This class holds all broadcast channels and handles event posting.
 * You can use [GlobalBus] that is just plain instance of this class or create your own implementation.
 */
@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
open class ChannelBus {

    private val channels = mutableMapOf<Class<*>, BroadcastChannel<Any>>()

    /**
     * Gets a [BroadcastChannel] for events of the given type. Creates new channel if one does't exist.
     * @return [BroadcastChannel] for events that are instances of [clazz]
     */
    internal fun <T : Any> forEvent(clazz: Class<T>): BroadcastChannel<T> {
        return channels.getOrPut(clazz) {
            ConflatedBroadcastChannel()
        } as BroadcastChannel<T>
    }

    /**
     * Posts new event to channel of the [event] type.
     * @param retain If the [event] should be retained in the channel for future subscribers. This is true by default.
     */
    @JvmOverloads
    fun <T : Any> post(event: T, retain: Boolean = true) {
        val channel = forEvent(event.javaClass)
        channel.offer(event).also {
            if (!it)
                throw IllegalStateException("ConflatedChannel cannot take element, this should never happen")
        }
        if (!retain)
            dropEvent(event.javaClass)
    }

    /**
     * Returns last posted event that was instance of [clazz] or `null` if no event of the given type is retained.
     * @return Retained event that is instance of [clazz]
     * @see awaitEvent
     */
    fun <T : Any> getLastEvent(clazz: Class<T>): T? {
        val channel = channels.getOrElse(clazz) { null }
        val value = (channel as? ConflatedBroadcastChannel<Any>)?.valueOrNull
        return value as? T
    }

    /**
     * Returns last posted event that was instance of [clazz] or awaits for it if one is not retained.
     * @param skipRetained Skips event retained in the channel. This is `false` by default
     * @return New or retained event that is instance of [clazz]
     * @see getLastEvent
     */
    suspend fun <T : Any> awaitEvent(clazz: Class<T>, skipRetained: Boolean = false): T {
        val receiveChannel = forEvent(clazz).openSubscription()

        if (skipRetained)
            receiveChannel.poll()

        while (true) {
            val received = receiveChannel.receive().takeUnless { it is DummyEvent }
            return received ?: continue
        }
    }

    /**
     *  Removes retained event of type [clazz]
     */
    fun dropEvent(clazz: Class<*>) {
        if (!channels.contains(clazz)) return
        val channel = channels[clazz] as BroadcastChannel<Any>
        channel.offer(DummyEvent())
    }
}