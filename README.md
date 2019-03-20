# ChannelBus
[![](https://jitpack.io/v/Kosert/ChannelBus.svg)](https://jitpack.io/#Kosert/ChannelBus)

ChannelBus is an event bus implementation for Android. 
- **Powered by Kotlin Coroutines and Channels**
- **Thread-aware**
- **Fully operable from Java code**

## Example

### Subscribing
Most use cases will consist of subscribing in your Activity/Fragment `onStart` method and unsubscribing in `onStop`.
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
### Posting
Instance of any class can posted as an event.
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
    implementation 'com.github.Kosert:ChannelBus:0.2'
}
```


## License
Apache 2.0
