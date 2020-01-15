# 11-1 Using Kotlin Coroutines

## 1. Adding coroutines to a project
코틀린에서 코루틴을 사용하기 위해서는 coroutines-core 라이브러리를 build.gradle(Module: app)에 추가해야 한다.

 - kotlinx-coroutines-core : 코틀린에서 코루틴을 사용하기 위한 주요 인터페이스
 - kotlinx-coroutines-android : 코루틴에서 안드로이드 Main 스레드 지원

```
dependencies {
  ...
  implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:x.x.x"
  implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:x.x.x"
}
``` 

## 2. Coroutines in Kotlin
안드로이드에서는 main thread의 blocking을 피하는 것이 중요하다. 기본 스레드는 Ui에 대한 모든 업데이트를 처리하는 단일 스레드이다. 또한 모든 클릭 핸들러 및 기타 UI 콜백을 호출하는 스레드이기도 하다.
큰 JSON 데이터 셋을 파싱해야 하거나 데이터베이스에 데이터 쓰기, 네트워크에서 데이터 가져오기 등을 일반적인 작업보다 오래 걸린다. 따라서 메인 스레드에서 이와 같은 코드를 호출하면 앱이 일시 중지되거나 중단되거나 정지 될 수 있다.
메인스레드를 너무 오랫동안 block 하면 앱이 중단되어 응용 프로그램이 응답하지 않음 대화 상자가 표시될 수도 있다


### 1) The callback pattern
메인스레드를 차다나하지 않고 장기 실행 작어블 수행하는 한가지 패턴을 callback이다. 콜백을 사용하면 백그라운드 스레드에서 장기 실행 작업을 시작할 수 있다. 작업이 완료되면 콜백이 호출되어 메인 스레드에 결과를 알려준다

```
// Slow request with callbacks
@UiThread
fun makeNetworkRequest() {
    // The slow network request runs on another thread
    slowFetch { result ->
        // When the result is ready, this callback will get the result
        show(result)
    }
    // makeNetworkRequest() exits after calling slowFetch without waiting for the result
}

```

이 코드는 @UiThread로 어노테이션 되어 있으므로 main 스레드에서 수행할 만큼 충분히 빨라야 한다. 즉 다음 화면 업데이트가 지연되지 않도록 매우 빠르게 반환되어야 한다. 그러나 slowFetch()는 완료하는데 몇 초 또는 몇 분이 걸리므로 main thread는 결과값을 기다릴 수 없다.
show(result) 콜백을 사용하면 백그라운드 스레드에서 showFetch를 실행하고, 준비가 되면 결과를 반환할 수 있다.

<br><br>

### 2) Using coroutines to remove callbacks
Callback은 훌륭한 패턴이지만 몇가지 단점이 있다. 콜백을 많이 사용하는 코드는 읽기 어려워지고 추론하기가 더 어려워 질 수 있다. 또한 콜백에서는 예외와 같은 일부 언어 기능을 사용할 수 없다.

Kotlin coroutines는 callback 기반의 코드를 순차적인 코드로 변경할 수 있게 해준다. 순차적으로 작성된 코드는 일반적으로 읽기 쉽고 exceptions와 같은 언어 기능을 사용할 수 있다.

결국 Callback이나 Coroutines이나 장기 실행 작업에서 결과가 나올 때까지 기다렸다가 계속 실행하는 똑같은 일을 처리한다. 그러나 코드에서는 매우 다르게 보인다.

**suspend** 키워드는 코투린을 가능하게 하는 함수 또는 함수 유형을 표시하는 방법이다. 코루틴이 suspend로 표시된 함수를 호출할 때 일반적인 함수 호출처럼 함수가 리턴할 때까지 blocking 하는 것이 아니라, 결과가 준비될 때 까지 실행을 일시 중단한(suspend) 다음 결과와 함께 중단된 곳에서 다시 시작한다.(resume)
결과를 기다리는 동안 다른 기능이나 코루틴이 실행될 수 있도록 실행중인 스레드를 unblock 한다.

```
// Slow request with coroutines
@UiThread
suspend fun makeNetworkRequest() {
    // slowFetch is another suspend function so instead of 
    // blocking the main thread  makeNetworkRequest will `suspend` until the result is 
    // ready
    val result = slowFetch()
    // continue to execute after the result is ready
    show(result)
}

// slowFetch is main-safe using coroutines
suspend fun slowFetch(): SlowResult { ... }
```

콜백 기반 코드와 비교할 때 코루틴 코드는 적은 코드로 현재 스레드를 unblocking 하는 동일한 결과를 달성한다. 순차적인 코드 스타일로 인해 여러개의 콜백을 만들지 않고도 여러 장기 실행 작업을 쉽게 연결할 수 있다.
예를 들어 두개의 네트워크 엔드포인트에서 결과를 가져와서 데이터베이스에 저장하는 코드는 콜백 없이 코루틴에서 아래와 같이 함수로 작성될 수 있다.

```
// Request data from network and save it to database with coroutines

// Because of the @WorkerThread, this function cannot be called on the
// main thread without causing an error.
@WorkerThread
suspend fun makeNetworkRequest() {
    // slowFetch and anotherFetch are suspend functions
    val slow = slowFetch()
    val another = anotherFetch()
    // save is a regular function and will block this thread
    database.save(slow, another)
}

// slowFetch is main-safe using coroutines
suspend fun slowFetch(): SlowResult { ... }
// anotherFetch is main-safe using coroutines
suspend fun anotherFetch(): AnotherResult { ... }
```

<br><br>

## 3. Controlling the UI with coroutines
이 단계에서는 delay 후에 메세지를 표시하기 위해 코루틴을 작성한다. 

### 1) Understanding CoroutineScope
코틀린에서 모든 코루틴은 CoroutineScope 내에서 실행된다. scope는 job을 통해 코루틴의 lifetime을 제어한다. scope 작업을 취소하면 해당 scope에서 시작된 모든 코루틴이 취소된다. 안드로이드에서는 사용자가 Activity나 Fragment로부터 떠날 때와 같이 scope를 사용하여 실행중인 모든 coroutine을 취소할 수 있다
또한 scope를 사용하면 default dispatcher를 지정할 수도 있다. dispatcher는 코루틴을 실행하는 스레드를 제어한다.

UI로 시작된 코루틴의 경우에는 안드로이드의 기본 스레드인 Dispatchers.Main에서 시작하는 것이 좋다. Dispatchers.Main에서 시작된 코루틴은 suspend 되는 동안 main thread를 block 하지 않는다. ViewModel 코루틴은 거의 항상 메인 스레드에서 UI를 업데이트하므로 메인 스레드에서 코루틴을 시작하면 추가적인 스레드 전환을 절약할 수 있다.
메인 스레드에서 시작한 코루틴은 언제든지 디스패처를 전환할 수 있다. 예를 들어 큰 JSON 결과를 parse 하는 작업을 main thread에서 벗어나 다른 dispatcher를 사용할 수 있다.

**Coroutine offer mian-safety**
코루틴은 언제든지 스레드를 쉽게 전환하고 원래 스레드로 결과를 다시 전달할 수 있으므로 Main 스레드에서 UI 관련 코륀을 시작하는 것이 좋다. Room 또는 Retrofit과 같은 라이브러리는 코루틴을 사용할 때 main-safety를 제공하므로 네트워크나 데이터베이스를 호출할 때 thread를 관리할 필요가 없다. 이로 인해 코드가 더 단순해 질 수 있다.
그러나 목록 정렬 및 파일 읽기와 같은 blcking code는 코루틴을 사용하는 경우에도 main-safety를 만들기 위해 명시적인 코드가 필요하다. 코루틴을 아직 지원하지 않는 네트워킹 또는 데이터베이스 라이브러리를 사용하는 경우에도 마찬가지이다.


### 2) Using viewModelScope
AndroidX lifecycle-viewmodel-ktx 라이브러리는 UI 관련 코루틴을 시작하도록 구성된 ViewModels에 CoroutineScope를 추가한다. 이 라이브러리를 사용하려면 프로젝트의 build.gradle(Module: app) 파일에 라이브러리를 포함시켜야 한다.

```
dependencies {
  ...
  implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:x.x.x"
}
```

이 라이브러리는 ViewModel 클래스의 확장함수로 viewModelScope를 추가한다. 이 scope는 Dispatchers.Main에 바인딩되며 ViewModel이 지워질 때 자동으로 취소된다.


### 3) Switch from threads to coroutines

MainViewModel.kt
```
/**
* Wait one second then update the tap count.
*/
private fun updateTaps() {
   // TODO: Convert updateTaps to use coroutines
   tapCount++
   BACKGROUND.submit {
       Thread.sleep(1_000)
       _taps.postValue("$tapCount taps")
   }
}
```

이 코드는 BACKGROUND ExecutorService(util/Executor.kt에 정의)를 사용하여 백그라운드 스레드에서 실행된다. sleep()은 현재 스레드를 차단하므로 Main 스레드에서 호출되면 UI가 정지된다. 


MainViewModel.kt

```
/**
* Wait one second then display a snackbar.
*/
fun updateTaps() {
   // launch a coroutine in viewModelScope
   viewModelScope.launch {
       tapCount++
       // suspend this coroutine for one second
       delay(1_000)
       // resume in the main dispatcher
       // _snackbar.value can be called directly from main thread
       _taps.postValue("$tapCount taps")
   }
}
```

#### 1) viewModelScope.launch는 viewModelScope에서 코루틴을 시작한다. 이것은 viewModelScope에 전달한 job이 취소되면 job/scope 내에 있는 coroutines은 모두 취소된다. delay가 반환되기 전에 사용자가 activity를 떠난 경우 코루틴은 ViewModel이 소멸됨에 따라 onCleared()가 호출될 떄 자동으로 취소된다.

#### 2) viewModelScope에는 default dispatcher인 Dispatchers.Main이 있으므로 코루틴은 기본 스레드에서 실행된다

#### 3) delay는 suspend 함수이다. coroutine이 main thread에서 실행되더라도 delay는 1초동안 thread를 block하지 않는다. 대신 dispatcher는 다음 명령문에서 1초 안에 코루틴이 재개되도록 스케줄한다

<br><br>

## 4. Moving from callbacks to coroutines
이번 단계에서는 코루틴을 사용하도록 repository를 변환한다. 이를 위해 ViewModel, Repository, Room 및 Retrofit에 코루틴을 추가한다.
코루틴을 사용하기 전에 아키텍처의 각 부분이 어떤 것을 담당하는지 이해하는 것이 필요하다

#### 1) MainDatabase는 Room을 사용하여 Title를 저장하고 로드하는 database를 구현한다

#### 2) MainNetwork는 새로운 title을 가져오는 network API를 구현한다. title을 가져오기 위해 Retrofit을 사용한다. 현재 Retrofit은 무작위로 오류 또는 가상의 데이터를 반환하도록 만들어져있지만 실제 네트워크 요청을 하는 것처럼 동작한다

#### 3) TitleRepository는 네트워크 및 데이터베이스의 데이터를 결합하여 title을 가져오거나 refresh 하기 위한 single API를 구현한다

#### 4) MainViewModel은 화면의 상태를 나타내며 이벤트를 처리한다. 사용자가 화면을 누를 때 repository에게 title을 갱신하라고 지시한다

네트워크 요청은 UI 이벤트에 의해 주도되고 이를 기반으로 코루틴을 시작하려고 하므로 코루틴을 사용하기 시작하는 자연스러운 장소는 viewModel이다

#### The callback version

```
// MainViewModel.kt

/**
* Refresh the title, showing a loading spinner while it refreshes and errors via snackbar.
*/
fun refreshTitle() {
   // TODO: Convert refreshTitle to use coroutines
   _spinner.value = true
   repository.refreshTitleWithCallbacks(object: TitleRefreshCallback {
       override fun onCompleted() {
           _spinner.postValue(false)
       }

       override fun onError(cause: Throwable) {
           _snackBar.postValue(cause.message)
           _spinner.postValue(false)
       }
   })
}
```

이 함수는 화면에서 사용자가 클릭할 때마다 호출된다. 그러면 레파지토리가 title을 새로 고치고 새로운 title을 데이터베이스 쓴다. 
이 구현은 콜백을 사용하여 몇가지 작업을 수행한다.

 - 쿼리를 시작하기 전에 _spinner.value = true로 로딩 스피너를 표시한다
 - 결과를 얻으면 로딩 스피너를 지우기 위해 _spinner.value = false를 할당한다
 - 오류가 발새하면 스낵바에 스피너를 표시하고 지우도록 지시한다.
 
onComplete() 콜백에는 title이 전송되지 않는다. 모든 title은 Room 데이터베이스에 쓰므로 UI는 Room이 업데이트 한 LiveData를 관찰하여 현재 title로 업데이트 한다

이제 코루틴으로 업데이트 하여 정확히 똑같은 동작을 수행하는 코드로 바꿔보자. UI를 자동으로 최신 상태로 유지하기 위해 Room 데이터베이스와 같은 observable한 data source를 사용하는 것이 좋다

#### The coroutine version 
refreshTitle을 코루틴으로 작성해보자. TitleRepository.kt에서 비어있는 suspend function을 생성한다. 코틀린에게 코루틴으로 작동한다고 알리기 위해 suspend 연ㅅ나자를 사용한다

```
suspend fun refreshTitle() {
    // TODO: Refresh from network and write to database
    delay(500)
}
```

Retrofit 및 Room을 사용하여 새 title을 가져와 코루틴을 사용하여 데이터베이스에 쓰도록 코드를 만든다. 지금은 일하는 척 하는데 500 밀리초를 사용하고 계속 진핸한다.
MainViwModel에서 callback 버전의 refreshTitle을 새로운 코루틴을 launch하는 것으로 변경한다

```
fun refreshTitle() {
   viewModelScope.launch {
       try {
           _spinner.value = true
           repository.refreshTitle()
       } catch (error: TitleRefreshError) {
           _snackBar.value = error.message
       } finally {
           _spinner.value = false
       }
   }
}
```

이 함수를 살펴보자

```
viewModelScope.launch {
```

이것은 Dispatchers.Main을 사용한다. refreshTitle이 네트워크 요청 및 데이터베이스 쿼리를 작성하더라도 코루틴을 사용하여 메인 스레드에서 안전하게 호출할 수 있다.
우리는 viewModelScope를 사용하기 있기 떄문에 사용자가 이 화면을 떠나면 코루틴으로 시작된 작업은 자동으로 취소된다. 즉 추가 네트워크 요청이나 데이터베이스 쿼리를 실행하지 않는다.

```
try {
    _spinner.value = true
    repository.refreshTitle()
}

```
refreshTitle()은 suspending function이므로 보통의 함수와는 다르게 동작한다. 콜백을 따로 전달할 필요는 없다. resume 될 때 까지 코루틴은 suspend 된다. 일반 blocking 함수처럼 보이지만 메인 스레드를 차단하지 않고 resuming 되기 전에 네트워크 및 데이터베이스 쿼리가 완료될 때까지 자동으로 대기한다


<br><br>

## 5. Making main-safe functions from blocking code
이 단계에서는 코루틴이 실행되는 스레드를 전환하는 방법을 배운다.


### 1) Review the existing callback code in refreshTitle

```
// TitleRepository.kt

fun refreshTitleWithCallbacks(titleRefreshCallback: TitleRefreshCallback) {
   // This request will be run on a background thread by retrofit
   BACKGROUND.submit {
       try {
           // Make network request using a blocking call
           val result = network.fetchNextTitle().execute()
           if (result.isSuccessful) {
               // Save it to database
               titleDao.insertTitle(Title(result.body()!!))
               // Inform the caller the refresh is completed
               titleRefreshCallback.onCompleted()
           } else {
               // If it's not successful, inform the callback of the error
               titleRefreshCallback.onError(
                       TitleRefreshError("Unable to refresh title", null))
           }
       } catch (cause: Throwable) {
           // If anything throws an exception, inform the caller
           titleRefreshCallback.onError(
                   TitleRefreshError("Unable to refresh title", cause))
       }
   }
}
```

TitleReopsitory.kt에서 refreshTitleWithCallbacks 메소드는 호출자와 loading 및 에러 상태를 커뮤니케이션 하기 위해 콜백을 구현했다.

#### 1) BACKGROUND ExecutorService를 사용하여 다른 스레드로 전환한다

#### 2) fetchNextTitle을 execute() blocking 메소드와 함께 사용하여 network request를 실행한다. network request는 현재 스레드에서 동작하며 이 경우에는 BACKGROUND 스레드이다.

#### 3) result가 successful 하면 database에 insertTitle 메소드로 데이터를 저장하고, onCompleted() 메소드를 호출한다

#### 4) result가 successful 하지 않거나 exception이 발생하면 onError() 메소드를 호출하고 caller에게 failed에 대해 알린다.

이 콜백 기반 구현은 메인 스레드를 차단하지 않으므로 main-safe하다. 그러나 작업이 완료될 때 caller에게 알리려면 callback을 사용해야 한다. 


### 2) Calling blocking calls from coroutines
코루틴을 사용하여 콜백을 제거하고 result를 처음에 호출한 스레드로 다시 전달할 수 있다. 큰 목록의 sorting이나 filtering 또는 디스크 읽기와 같은 코루틴 내부에서 blocking 또는 CPU 집약적인 작업을 수행할 때마다 이 패턴을 사용할 수 있다.
이 패턴은 CPU 집약적인 작업을 할 때 사용되어야 한다. 가능하면 Room과 retrofit과 같은 라이브러리에서 suspend function을 사용하는 것이 좋다.

dispatcher를 전환하려면 코루틴은 withContext를 사용한다. 기본적으로 코루틴은 세 가지 Dispatchers가 제공된다. Main, IO, Default가 이와 같다. Default dispatcher가 CUP 집약적인 작업에 최적화 되어있다면, IO dispatcher는 네트워크나 디스크로부터 읽어오는 작업에 최적화 되어있다. 

```
suspend fun refreshTitle() {
   // interact with *blocking* network and IO calls from a coroutine
   withContext(Dispatchers.IO) {
       val result = try {
           // Make network request using a blocking call
           network.fetchNextTitle().execute()
       } catch (cause: Throwable) {
           // If the network throws an exception, inform the caller
           throw TitleRefreshError("Unable to refresh title", cause)
       }
      
       if (result.isSuccessful) {
           // Save it to database
           titleDao.insertTitle(Title(result.body()!!))
       } else {
           // If it's not successful, inform the callback of the error
           throw TitleRefreshError("Unable to refresh title", null)
       }
   }
}
```

이 구현은 network와 database를 호출할 때 blocking call을 사용하지만 callback 버전보다 더 단순하다.
이 코드는 여전히 blocking call을 사용한다. execute()와 insertTitle()은 코루틴이 실행중인 thread를 차단시킨다. 그러나 withContext를 사용하여 Dispatchers.IO로 변경하였기 떄문에 IO dispatcher 내에 있는 스레드 중 하나를 blocking 한다.

콜백 버전과 비교하면 2가지 중요한 차이점이 있다

#### 1) withContext는 결과를 호출한 Dispatcher(이 경우 Dispatchers.Main)으로 결과를 리턴한다. 콜백 버전에서는 BACKGROUND executor service의 스레드에서 콜백을 호출했다

#### 2) caller는 이 함수에 callback을 전달할 필요가 없다. 결과 또는 오류를 얻기 위해 suspend 및 resume에 의존한다.

<br><br>

## 6. Coroutines in Room & Retrofit

### Coroutines in Room

MainDatabase.kt를 열고 insertTitle을 suspend function으로 만든다.

```
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertTitle(title: Title)

```

이렇게하면 Room이 쿼리를 main-thread 상태로 만들고 백그라운드 스레드에서 자동으로 실행한다. 그러나 이는 코루틴 내부에서만 이 쿼리를 호출할 수 있음을 의미한다.

### Coroutines in Retrofit
다음 단계로 Retofit과 coroutines을 어떻게 통합하는지 알아보자. MainNetwork.kt를 열고 fetchNextTitle을 suspend function으로 바꾼다
 
 - suspend function은 Retrofit 2.6.0 이상의 버전에서 지원한다.
 
```
interface MainNetwork {
   @GET("next_title.json")
   suspend fun fetchNextTitle(): String
}
```

Retrofit과 suspend function을 사용하려면 다음 두가지 작업을 수행해야 한다.

#### 1) 함수에 suspend를 추가한다

#### 2) 리턴 타입에서 Call 래퍼를 제거한다. retrofit의 전체 결과에 대해 액세스 하기를 원한다면 Result<String> 타입을 사용한다

Retrofit은 자동으로 main-safe한 suspend function을 만들어 Dispatchers.Main에서 직접 호출할 수 있다.

Room과 Retrofit은 모두 custom dispatcher를 사용하고 Dispatchers.IO를 사용하지 않는다. 


### Using Room and Retrofit
