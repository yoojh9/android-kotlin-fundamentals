# 06-2 Coroutines and Room

이제 데이터베이스와 UI가 있으므로 데이터를 수집하고 데이터베이스에 데이터를 추가하고 데이터를 표시해야 한다. 이 모든 작업은 view model에서 진행한다.
sleep-tracker view model은 button click들을 handle하면서 DAO를 통해 database와 상호작용하고, LiveData를 통해 UI에 데이터를 제공한다
모든 데이터베이스 작업은 기본 UI 스레드에서 실행해야하며 코루틴을 사용하여 수행한다.

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

