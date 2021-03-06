# 06-2 Coroutines and Room

이제 데이터베이스와 UI가 있으므로 데이터를 수집하고 데이터베이스에 데이터를 추가하고 데이터를 표시해야 한다. 이 모든 작업은 view model에서 진행한다.
sleep-tracker view model은 button click들을 handle하면서 DAO를 통해 database와 상호작용하고, LiveData를 통해 UI에 데이터를 제공한다
모든 데이터베이스 작업은 기본 UI 스레드에서 실행해야하며 코루틴을 사용하여 수행한다.

## 1. Add a ViewModel

### Step 1: Add SleepTrackerViewModel

#### 1) sleeptracker 패키지에서 SleepTrackerViewModel.kt 파일을 연다

#### 2) SleepTrackerViewModel 클래스가 AndroidViewModel()을 상속받은 것을 확인해라. 이 클래스는 ViewModel과 동일하지만 파라미터로 application context를 받아서 매개변수로 사용한다

```
class SleepTrackerViewModel(
       val database: SleepDatabaseDao,
       application: Application) : AndroidViewModel(application) {
}
```

<br>

### Step 2: Add SleepTrackerViewModelFactory

#### 1) sleeptracker 패키지에서 SleepTrackerViewModelFactory.kt 파일을 연다

#### 2) factory 코드를 살펴보자

```
class SleepTrackerViewModelFactory(
       private val dataSource: SleepDatabaseDao,
       private val application: Application) : ViewModelProvider.Factory {
   @Suppress("unchecked_cast")
   override fun <T : ViewModel?> create(modelClass: Class<T>): T {
       if (modelClass.isAssignableFrom(SleepTrackerViewModel::class.java)) {
           return SleepTrackerViewModel(dataSource, application) as T
       }
       throw IllegalArgumentException("Unknown ViewModel class")
   }
}
```

  - SleepTrackViewModelFactory는 ViewModel과 동일한 인자를 사용하고 ViewModelProvider.Factory를 상속한다
  - factory 안에서 모든 클래스 타입을 인자로 받고 viewModel을 리턴하는 create() 코드를 오버라이드한다
  - create() 메소드의 body 안에서 코드는 사용 가능한 SleepTrackerViewModel 클래스가 있는지 체크하고 있는 경우 인스턴스를 반환하고 그렇지 않으면 예외를 발생시킨다

<br>

### Step 3: Update SleepTrackerFragment

#### 1) SleepTrackerFragment에서 application context의 참조를 가져온다. onCreateView()의 binding 아래에 해당 참조를 넣는다.

 - requireNotNull()은 코틀린 함수로 value가 null일 경우 IllegalArgumentException을 발생시킨다
 
```
 val application = requireNotNull(this.activity).application
```

<br>

#### 2) DAO 레퍼런스를 통해 data source 참조를 얻는 작업이 필요하다. onCreateView() 에서 return 전에 dataSource를 정의한다. 데이터베이스의 DAO 레퍼런스는 SleepDatabase.getInstance(application).sleepDatabaseDao를 통해 얻을 수 있다

```
 val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
```

<br>

#### 3) onCreate()에서 dataSource와 application을 인자로 넘겨 viewModelFactory 인스턴스를 생성한다

```
val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)
```

<br>

#### 4) 이제 viewModelFactory를 사용하여 SleepTrackerViewModel 레퍼런스를 얻을 수 있다. 파라미터 SleepTrackerViewModel::class.java는 오브젝트의 런타임 Java 클래스를 나타낸다

```
val sleepTrackerViewModel =
       ViewModelProviders.of(
               this, viewModelFactory).get(SleepTrackerViewModel::class.java)
```

<br>

#### 5) onCreateview()의 최종 코드는 아래와 같다

```
override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)

        val sleepTrackerViewModel =
                ViewModelProviders.of(
                        this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        return binding.root
    }
```

<br>

### Step 4: Add data binding for the view model

fragment_sleep_tracker.xml 레이아웃에서 

#### 1) \<data\> 블럭 안에 SleepTrackerViewModel 레퍼런스를 \<variable\>에 추가한다

```
<data>
   <variable
       name="sleepTrackerViewModel"
       type="com.example.android.trackmysleepquality.sleeptracker.SleepTrackerViewModel" />
</data>
```

<br>

SleepTrackerFragment에서

#### 1) 현재 액티비티를 바인딩의 라이프 사이클 소유자로 설정하십시오.

```
binding.setLifecycleOwner(this)
```

<br>

#### 2) sleepTrackerViewModel 바인딩 변수를 sleepTrackerViewModel 객체값으로 지정한다. 아래 코드를 onCreate()의 SleepTrackerViewModel을 생성하는 코드 아래 넣는다.

```
binding.sleepTrackerViewModel = sleepTrackerViewModel
```

<br>

#### 3) 바인딩 오브젝트를 다시 생성해야 하므로 오류가 표시 될 수도 있다. 오류를 제거하기 위해 프로젝트를 클린하고 다시 빌드한다.

<br><br>

## 2. Concept: Coroutines
메인 스레드를 차단하지 않고 장시간의 실행 작업을 수행하는 한 가지 패턴은 콜백을 사용하는 것이다. 
코틀린에서 코루틴은 장기 실행 작업을 우아하고 효율적으로 처리하는 방법이다. Kotlin 코루틴을 사용하면 콜백 기반 코드를 순차 코드로 변환 할 수 있다
순차적으로 작성된 코드는 일반적으로 읽기 쉽고 예외와 같은 언어 기능을 사용할 수도 있다.
결국 코루틴과 콜백은 동일한 작업을 수행합니다. 장기 실행 작업에서 결과를 사용할 수 있을 때까지 계속 기다렸다가 실행한다.
<image src="./images/coroutine.png" width="50%" height="50%"/>

코루틴은 다음 속성을 가지고 있다
 - 코루틴은 비동기이며 non-blocking이다
 - 코루틴은 suspend 함수를 사용하여 비동기 코드를 순차적으로 만든다

**코루틴은 비동기이다**
 - 코루틴은 프로그램의 주요 실행 단계와 독립적으로 실행되며 병렬 또는 별도의 프로세스에서 실행된다
 - 앱이 나머지 부분을 처리하는 동안 약간의 작업을 따로 처리할 수 있
 - 예를 들어 조사가 필요한 질문이 있고 동료에게 답을 찾도록 요청한다고 가정한다면, 그것이 '비동기적으로' 그리고 '별도의 스레드'에서 작업하는 것과 유사하다. 동료가 돌아와서 답변이 무엇인지 알려주기 전 까지 다른 작업을 계속 수행할 수 있다
 
**코루틴은 non bocking이다**
 - non blocking의 의미는 코루틴이 main 또는 UI thread를 차단하지 않음을 의미한
 - 따라서 코루틴을 사용하면 UI 작업이 항상 우선하므로 사용자에게 부드러운 UI 처리를 제공할 수 있다

**코루틴은 suspend 함수를 사용하여 비동기 코드를 순차 코드로 만들 수 있다**
 - 코루틴에서 suspend로 표시된 함수를 실행하면, 보통의 함수처럼 함수가 리턴될 때 까지 blocking 되지 않고 결과가 준비될 때 까지 실행을 일시 중단한다
 - suspend는 모든 로컬 변수를 저장하여 현재 코루틴 실행을 정지한다
 - resume은 정지된 위치부터 정지된 코루틴을 계속 실행합니다
 - 코루틴은 일시 중단되어 결과를 기다리는 동안에 실행 중인 thread는 block 되지 않는다. 그래서 다른 코드나 코루틴이 실행될 수 있다
 - suspend 키워드는 코드가 실행되는 스레드를 지정하지 않습니다. suspend 함수는 백그라운드 스레드 또는 메인 스레드에서 실행될 수 있다.
 - blocking과 suspend의 차이점은 스레드는 block되면 다른 작업이 발생하지 않는다는 점이다. 스레드가 suspend된 경우에는 결과를 사용할 수 있을 때 까지 다른 작업이 수행된다

 <image src="./images/block_vs_suspend.png" width="50%" height="50%"/>
 

코루틴을 코틀린에서 사용하려면 3가지가 필요하다
 - A job
 - A dispatcher
 - A scope

<br>

**Job**: 기본적으로 job은 취소할 수 있다. 모든 코루틴은 job을 가지고 있고 코루틴을 취소하기 위해 job을 사용할 수 있다. job은 부모-자식 계층 구조로 배열될 수 있으며 부모 job을 취소하면 일일이 수동으로 취소하지 않아도 모든 자식 job은 알아서 취소된다. 

**Dispatcher**: 디스패처는 다양한 스레드에서 실행하기 위해 코루틴을 보낸다. 

   - Dispatcher.Main: main에서 코루틴을 시작한다. 이 디스패처는 UI와 상호 작용하고 빠른 작업을 수행하기 위해서만 사용해야 한다. 예를 들어 suspend 함수를 호출하고, Android UI 프레임워크 작업을 실행하고, LiveData 객체를 업데이트한다
   - Dispatcher.IO: 이 디스패처는 기본 스레드 외부에서 디스크 또는 네트워크 I/O를 수행하도록 최적화되어 있다. 예를 들어 파일을 읽거나 네트워킹 작업을 수행한다
   - Dispatcher.Default: 이 디스패처는 CPU를 많이 사용하는 작업을 기본 스레드 외부에서 수행하도록 최적화되어 있다. 목록을 정렬하고 JSON을 파싱하는 등의 작업을 수행한다

**Scope**: 코루틴의 scope는 코루틴이 실행되는 context의 범위를 정의한다. scope는 코루틴의 job과 Dispatcher에 대한 정보를 결합한다. scope는 코루틴을 추적한다. 코루틴을 시작하면 scope 안에 있는데, 즉 코루틴이 추적할 scope를 나타낸다.

<br><br>

## 3. Collect and display the data
다음과 같은 방식으로 사용자가 sleep data와 상호작용 하기를 원한다
 - 사용자가 start 버튼을 누르면 앱은 새로운 sleep night를 생성하고 데이터베이스에 sleep night을 저장한다
 - 사용자가 stop 버튼을 누르면 앱은 sleep night의 end time을 갱신한다
 - 사용자가 clear 버튼을 누르면 데이터베이스에 있는 데이터를 지운다
이러한 데이터베이스 작업은 오래 걸릴 수 있으므로 별도의 스레드에서 실행해야 한다

<br>

### Step 1: 데이터베이스 작업을 위한 코루틴 설정
Sleep Tracker 앱의 시작 버튼을 누르면 SleepTrackerViewModel에서 함수를 호출하여 SleepNight의 새 인스턴스를 만들고 데이터베이스에 인스턴스를 저장하려고 한다. 

버튼을 누르면 SleepNight 생성 또는 업데이트와 같은 데이터베이스 작업이 트리거된다. 이러한 이유로 인해 코루틴을 사용하여 앱 버튼의 클릭 핸들러를 구현한다.

#### 1) app-level의 build.gradle 파일을 열고 코루틴에 대한 dependency를 찾는다. 코루틴을 사용하려면 아래와 같은 디펜던시가 필요하다
```
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutine_version"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutine_version"
```

<br>

#### 2) SleepTrackerViewModel 파일을 연다. 

#### 3) 클래스의 body에 viewModelJob을 정의하 Job의 인스턴스를 할당한다. 이 viewModelJob을 사용하면 뷰 모델이 더 이상 사용되지 않고 파괴 될 때, 이 뷰 모델로 시작된 모든 코루틴을 취소 할 수 있다.

```
private var viewModelJob = Job()
```

<br>

#### 4) 클래스의 끝에서 onCleared() 메소드를 재정의하고 모든 코루틴을 취소해라. ViewModel이 소멸되면 onCleared()가 호출된다.

```
override fun onCleared() {
   super.onCleared()
   viewModelJob.cancel()
}
```

<br>

#### 5) viewModelJob 정의 바로 아래에 코루틴에 대한 uiScope를 정의한다. 이 scope는 코루틴이 실행될 스레드를 결정하며 scope는 job에 대해서도 알아야 한다. scope를 얻으려면 CoroutineScope의 인스턴스를 요청하고 dispatcher 및 job을 전달해라.
 - Dispatchers.Main을 사용하는 것은 uiScope에서 실행 된 코루틴이 기본 스레드(Main thread)에서 실행됨을 의미한다.
 - 코루틴이 일부 처리를 수행한 후 UI가 업데이트 되기 때문에 viewModel에 의해 시작된 코루틴에 사용하기에 적합하다.
```
private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
``` 

<br>

#### 6) uiScope 정의 아래에 current night을 유지하기 위해 tonight이라는 변수를 선언한다. 데이터를 observe하고 update하기 위해 MutableLiveData 타입으로 선언한다.

```
private var tonight = MutableLiveData<SleepNight?>()
```

<br>

#### 7) init 블럭에서 tonight 변수를 초기화하기 위해 initializeTonight() 함수를 호출한다. initializeTonight()는 다음 단계에서 구현한다

```
init {
   initializeTonight()
}

```

<br>

#### 8) init 블럭 아래에 initializeTonight()을 구현한다. uiScope 범위에서 coroutine을 launch 시킨다. uiScope 내부에서 getTonightFromDatabase()를 호출하여 데이터베이스에서 tonight의 값을 가져와 tonight.value에 할당한다

```
private fun initializeTonight() {
   uiScope.launch {
       tonight.value = getTonightFromDatabase()
   }
}
```

<br>

#### 9) getTonightFromDatabase()를 구현한다. nullable한 SleepNight 객체를 리턴하는 private suspend 함수를 리턴한다

```
private suspend fun getTonightFromDatabase(): SleepNight? { }
```

<br>

#### 10) getTonightFromDatabase() 함수 본문 내에서 Dispatchers.IO 컨텍스트에서 실행되는 코루틴으로부터 결과를 리턴한다. 데이터베이스에서 데이터를 가져오는 것은 I/O 조작이며 UI와 관련이 없으므로 I/O 디스패처를 사용한다.

```
return withContext(Dispatchers.IO) {}
```  

<br>

#### 11) return 블록 내에서 코루틴이 데이터베이스에서 tonight을 가져오게 한다. 시작 시간과 종료 시간이 동일하지 않으면 이미 시간이 측정 완료된 것으로 간주하고 null을 반환한다

```
   var night = database.getTonight()
   if (night?.endTimeMilli != night?.startTimeMilli) {
       night = null
   }
   night
```
 
<br>

#### 12) 완료된 suspend getTonightFromDatabase() 함수는 아래와 같다

```
private suspend fun getTonightFromDatabase(): SleepNight? {
   return withContext(Dispatchers.IO) {
       var night = database.getTonight()
       if (night?.endTimeMilli != night?.startTimeMilli) {
           night = null
       }
       night
   }
}
```

<br>

### Step 2: Add the click handler for the Start button
이제 start button의 클릭 핸들러인 onStartTracking()을 구현할 수 있다. 새로운 SleepNight을 생성하여 데이터베이스에 저장한 다음 tonight에 할당해야 한다. onStartTracking()는 initializeTonight()과 비슷해질 것이다

<br>

#### 1) SleepTrackerViewModel.kt 파일에서 onStartTracking()을 선언한다

```
fun onStartTracking() {}
```

<br>

#### 2) onStartTracking() 메소드에서 UI를 계속 업데이트 하려면 결과가 필요하므로 uiScope 내에서 코루틴은 launch한다

```
uiScope.launch {}
```

<br>

#### 3) coroutine launch 블럭 안에 현재시간을 시작 시간으로 가지고 있는 새로운 SleepNight() 객체를 만든다.

```
val newNight = SleepNight()
```

<br>

#### 4) coroutine launch 안에서 insert()를 호출하여 데이터베이스에 newNight을 추가한다. 지금은 suspend insert() 메소드가 없어 에러가 날 것이다.

```
insert(newNight)
```

<br>

#### 5) tonight 값을 가져와서 업데이트한다.

```
tonight.value = getTonightFromDatabase()
```

<br>

#### 6) onStartTracking() 안에 SleepNight을 인자로 갖는 private suspend insert() 함수를 정의한다

```
private suspend fun insert(night: SleepNight) {}
```

<br>

#### 7) insert() 함수 내에서 I/O context로 코루틴을 시작하고 DAO에서 insert()를 호출하여 데이터베이스에 추가한다

```
withContext(Dispatchers.IO) {
   database.insert(night)
}
```

<br>

#### 8) fragment_sleep_tracker.xml 레이아웃 파일에서 앞에서 설정한 데이터 바인딩을 사용하여 start button에 클릭 핸들러로 onStartTracking()를 추가한다

```
android:onClick="@{() -> sleepTrackerViewModel.onStartTracking()}"
```

<br>

#### 9) 앱을 빌드하고 Start 버튼을 누른다. 이 액션은 data를 생성하지만 아무것도 볼 수는 없다. 다음 단계에서 이 문제를 수정하자

<br>

```
이제 패턴을 확인할 수 있다

1. 결과가 UI에 영향을 주기 때문에 코루틴을 main thread 또는 UI thread로 실행시킨다. 
2. 오래 걸리는 작업은 결과를 기다리는 동안 UI thread를 block하지 않기 위해 suspend function을 사용하여 호출한다
3. 오래 걸리는 작업은 UI와 관련이 없다. I/O 컨텍스트로 변하여 이러한 종류의 작업에 최적화 되고 설정된 스레드 풀에서 실행될 수 있도록 한다
4. 그런 다음 데이터베이스 기능을 호출하여 작업을 수행한다

패턴은 아래와 같다

fun someWorkNeedsToBeDone {
    uiScope.launch {
        suspendFunction()
    }
}

suspend fun suspendFunction() {
    withContext(Dispatchers.IO){
        longrunningWork()
    }
}
```

<br>

### Step 3: Display the data
 - DAO의 getAllNights ()가 LiveData를 반환하므로 SleepTrackerViewModel에서 nights 변수는 LiveData를 참조한다.
 - 데이터베이스의 데이터가 변경될 떄 마다 LiveData nights가 최신 데이터를 표시하도록 업데이트 되는게 Room 기능 중 하나이다. Room은 데이터베이스와 일치하도록 데이터를 업데이트 하므로 LiveData를 명시적으로 set하거나 update할 필요가 없다
 - 텍스트 뷰에 night를 표시하면 객체 참조값이 표시되는데, 객체의 내용을 보기 위해서는 형식화된 문자열로 변환해야 한다. 데이터베이스에서 새로운 데이터를 가져올 때 transformation map을 실행시켜 보자
 
 #### 1) Util.kt 파일을 열어 formatNight() 메소드 주석을 해제하고 import를 추가한다
 
 #### 2) formatNights()의 리턴 타입이 HTML 형식 문자열인 Spanned 인 것을 확인한다
 
 #### 3) strings.xml을 열어서 CDATA를 사용하여 sleep data를 표시하기 위해 문자열 리소스 포맷을 사용한다
 
 #### 4) SleepTrackerViewMOdel 파일을 열어서 nights라는 변수를 정의한다. nights에는 데이터베이스에서 모든 nights의 값을 가져와 할당시킨다.
 
 ```
 private val nights = database.getAllNights()
 ```
 
 <br>
 
 #### 5) nights 선언 바로 아래에 nights를 nightsString으로 변환하는 코드를 넣는다. Util.kt의 foramtNights() 함수를 사용한다. nights를 Transformations 클래스의 map() 함수에 전달한다.
  - 문자얼 리소스에 액세스 하기 위해 nights와 Resource를 매개변수로 하는 formatNights() 함수를 호출한다
  
  ```
  val nightsString = Transformations.map(nights) { nights ->
     formatNights(nights, application.resources)
  }
  ```
 
 <br>
 
 #### 6) fragment_sleep_tracker.xml 레이아웃을 열어서 TextView에 anroid:text 속성을 추가하여 nightsString에 대한 참조로 바꿀 수 있다
 
 ```
 "@{sleepTrackerViewModel.nightsString}"
 ```
 
 <br>
 
 #### 7) 앱을 빌드하고 실행시킨다. 모든 sleep data의 start time이 화면에 나타난다. start button을 한번 더 눌러서 데이터가 추가되는지 확인해본다.
 
 다음 단계에서는 Stop 버튼을 구현한다
 
 <br>
 
 ### Step 4: Add the click handler for the Stop button
 이전 단계에서 진행했던 같은 패턴을 사용하여 SleepTrackerViewModel의 Stop button 클릭 핸들러를 구현한다
 
 #### 1) viewModel에 onStopTracking()을 추가하고, 코루틴을 uiScope에서 실행시킨다. end time이 아직 저장되지 않았다면 endTimeMilli에 현재 시간을 넣고 night data로 update()를 호출한다
  - 코틀린에서는 return@label 문법을 이용해서 여러 중첩 함수 중 이 명령이 리턴하는 함수를 지정할 수 있다
  
  ```
  fun onStopTracking() {
     uiScope.launch {
         val oldNight = tonight.value ?: return@launch
         oldNight.endTimeMilli = System.currentTimeMillis()
         update(oldNight)
     }
  }
  ```
  
  <br>
  
 #### 2) insert()를 구현했던 같은 패터을 사용하여 update()를 구현한다
 ```
 private suspend fun update(night: SleepNight) {
    withContext(Dispatchers.IO) {
        database.update(night)
    }
 }
 ```
 
 <br>
 
 #### 3) fragment_sleep_tracker.xml 파일을 열어서 stop_button에 클릭 핸들러를 추가한다
 ```
 android:onClick="@{() -> sleepTrackerViewModel.onStopTracking()}"
 ```
 
 <br>
 
 #### 4) 앱을 빌드시키고 실행시켜서 Start 버튼을 누르고 Stop 버튼을 누른다. 
 
 <br>
 
 ### Step 5: Add the click handler for the Stop button

 #### 1) 유사하게 onCLear()와 clear()를 구현한다
 
 ```
 fun onClear() {
    uiScope.launch {
        clear()
        tonight.value = null
    }
 }
 
 suspend fun clear() {
    withContext(Dispatchers.IO) {
        database.clear()
    }
 }
 ```
 
 <br>
 
 #### 2) fragment_sleep_tracker.xml을 열어서 clear_button 버튼에 클릭 핸들러를 연결한다
 
 ```
 android:onClick="@{() -> sleepTrackerViewModel.onClear()}"
 ```
 
 <br>
 
 #### 3) 앱을 실행시켜서 Clear를 눌러 모든 데이터를 제거한다
 
 