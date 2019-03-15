package me.kosert.channelbus.sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.kosert.channelbus.EventsReceiver

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val receiver by lazy { EventsReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RandomNumberProvider.init()
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        receiver.subscribe { event: RandomNumberEvent ->
            mainTextView.text = "New random number: ${event.number}"
        }
    }

    override fun onStop() {
        super.onStop()
        receiver.unsubscribeAll()
    }

}
