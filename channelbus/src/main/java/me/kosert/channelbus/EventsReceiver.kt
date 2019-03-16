package me.kosert.channelbus

import android.os.Looper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Class for receiving events posted to [ChannelBus]
 *
 *@param bus [ChannelBus] instance to subscribe to. If not set, [GlobalBus] will be used
 */
@ExperimentalCoroutinesApi
open class EventsReceiver @JvmOverloads constructor(
    private val bus: ChannelBus = GlobalBus
) {

    private val listeners = mutableListOf<EventListener>()

    private var returnDispatcher =
        if (Looper.myLooper() == Looper.getMainLooper())
            Dispatchers.Main
        else
            Dispatchers.Default

    /**
     * Set the `CoroutineDispatcher` which will be used to launch your callbacks.
     *
     * If this [EventsReceiver] was created on the main thread the default dispatcher will be [Dispatchers.Main].
     * In any other case [Dispatchers.Default] will be used.
     */
    fun returnOn(dispatcher: CoroutineDispatcher): EventsReceiver {
        returnDispatcher = dispatcher
        return this
    }

    /**
     * Subscribe to events that are instances of [clazz] with the given [callback] function.
     * The [callback] can be called immediately if event of type [clazz] is retained in the channel.
     *
     * @param clazz Type of event to subscribe to
     * @param skipRetained Skips event retained in the channel. This is `false` by default
     * @param callback The callback function
     * @return This instance of [EventsReceiver] for chaining
     */
    @JvmOverloads
    fun <T : Any> subscribeTo(clazz: Class<T>, skipRetained: Boolean = false, callback: suspend (event: T) -> Unit): EventsReceiver {

        if (listeners.any { it.clazz == clazz })
            throw IllegalArgumentException("Already subscribed for event type: $clazz")

        val channel = bus.forEvent(clazz).openSubscription()
        val job = CoroutineScope(Job() + Dispatchers.Default).launch {

            if (skipRetained)
                channel.poll()

            while (true) {
                val received = channel.receive()
                if (received is DummyEvent) continue

                withContext(returnDispatcher) { callback(received) }
            }
        }
        listeners.add(EventListener(clazz, job, channel))
        return this
    }

    /**
     * A variant of [subscribeTo] that uses an instance of [EventCallback] as callback.
     *
     * @param clazz Type of event to subscribe to
     * @param skipRetained Skips event retained in the channel. This is `false` by default
     * @param callback Interface with implemented callback function
     * @return This instance of [EventsReceiver] for chaining
     * @see [subscribeTo]
     */
    @JvmOverloads
    fun <T : Any> subscribeTo(clazz: Class<T>, callback: EventCallback<T>, skipRetained: Boolean = false): EventsReceiver {
        return subscribeTo(clazz, skipRetained) { callback.onEvent(it) }
    }

    /**
     * Simplified [subscribeTo] for Kotlin.
     * Type of event is automatically inferred from [callback] parameter type.
     *
     * @param skipRetained Skips event retained in the channel. This is `false` by default
     * @param callback The callback function
     * @return This instance of [EventsReceiver] for chaining
     */
    inline fun <reified T : Any> subscribe(skipRetained: Boolean = false, noinline callback: suspend (event: T) -> Unit): EventsReceiver {
        return subscribeTo(T::class.java, skipRetained, callback)
    }

    /**
     * A variant of [subscribe] that uses an instance of [EventCallback] as callback.
     *
     * @param skipRetained Skips event retained in the channel. This is `false` by default
     * @param callback Interface with implemented callback function
     * @return This instance of [EventsReceiver] for chaining
     * @see [subscribe]
     */
    inline fun <reified T : Any> subscribe(callback: EventCallback<T>, skipRetained: Boolean = false): EventsReceiver {
        return subscribeTo(T::class.java, callback, skipRetained)
    }

    /**
     * Unsubscribe from all events
     */
    fun unsubscribeAll() {
        listeners.forEach { it.unsubscribe() }
        listeners.clear()
    }

    private class EventListener(
        val clazz: Class<*>,
        private val job: Job,
        private val channel: ReceiveChannel<*>
    ) {

        fun unsubscribe() {
            job.cancel()
            channel.cancel()
        }
    }
}
