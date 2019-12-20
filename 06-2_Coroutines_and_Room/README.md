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

<image src="./image/coroutine.png" width="70%", height="70%"/>

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
 - 그런 다음 코루틴이 중돤된 부분부터 재시작되어 결과가 나타난다
 - 코루틴은 일시 중단되어 결과를 기다리는 동안에 실행 중인 thread는 block 되지 않는다. 그래서 다른 코드나 코루틴이 실행될 수 있다
 - suspend 키워드는 코드가 실행되는 스레드를 지정하지 않습니다. suspend 함수는 백그라운드 스레드 또는 메인 스레드에서 실행될 수 있다.
 - blocking과 suspend의 차이점은 스레드는 block되면 다른 작업이 발생하지 않는다는 점이다. 스레드가 suspend된 경우에는 결과를 사용할 수 있을 때 까지 다른 작업이 수행된다
 
 <image src="./images/block_vs_suspend.png" width="70%", height="70%"/>
 
<br>

코루틴을 코틀린에서 사용하려면 3가지가 필요하다
 - A job
 - A dispatcher
 - A scope

<br>


 
