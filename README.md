# ChannelBus

ChannelBus is an event bus implementation for Android. 
- **Powered by Kotlin Coroutines and Channels**
- **Thread-aware**
- **Fully operable from Java code**

## Example

### Subscribe
```kotlin
class SomeActivity : AppCompatActivity() {

    private val receiver by lazy { EventsReceiver() }

    override fun onStart() {
        super.onStart()
        receiver.subscribe { event: MyEvent ->
            // handle new event
        }
    }

    override fun onStop() {
        super.onStop()
        receiver.unsubscribeAll()
    }
```
### Post
```kotlin
GlobalBus.post(MyEvent())
```

For detailed documentation, [check the wiki](https://github.com/Kosert/ChannelBus/wiki).


## Include in your project

In your project root `build.gradle`:
```gradle
allprojects {
    repositories {
		maven { url 'https://jitpack.io' }
	}
}
```

In your module's `build.gradle`:
```gradle
dependencies {
	implementation 'com.github.kosert:channelbus:0.1'
}
```


## License
Apache 2.0
