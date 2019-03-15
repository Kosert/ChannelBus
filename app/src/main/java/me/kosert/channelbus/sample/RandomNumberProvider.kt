package me.kosert.channelbus.sample

import kotlinx.coroutines.*
import me.kosert.channelbus.GlobalBus
import kotlin.random.Random

@ExperimentalCoroutinesApi
object RandomNumberProvider {

    // Posts new random number event every 3 seconds
    fun init() {

        CoroutineScope(Job() + Dispatchers.Default).launch {
            while (true) {
                val newRandomNumber = Random.nextInt()
                GlobalBus.post(RandomNumberEvent(newRandomNumber))
                delay(3000)
            }
        }
    }
}