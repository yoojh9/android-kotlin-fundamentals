# 06-3 Use LiveData To Control Button State

## 1. Add navigation
 ### Step 1: Inspect the code
  #### 1) SleepQualityFragment를 살펴보면 layout을 inflate 시키고 application을 가져오고 binding.root를 리턴한다
  
  #### 2) navigation.xml의 디자인 에디터를 열면 SleepTrackerFragment에서 SleepQualityFragment로의 navigation path와 반대로 SleepQualityFragment에서 SleepTrackerFragment로 이동하는 navigation path를 볼 수 있다
  
  #### 3) navigation.xml 코드를 열어 sleepNightKey라는 이름을 가진 <argument>를 살펴본다. 사용자가 SleepTrackerFragment에서 SleepQualityFragment로 이동할 때 app은 night를 업데이트 하기 위해 sleepNightKey 데이터를 SleepQualityFragment로 전달한다.
  

<br>

 ### Step 2: Add navigation for sleep-quality tracking
 navigation graph는 이미 SleepTrackerFragment에서 SleepQualityFragment로 이동, 그리고 그 반대의 경우도 포함하고 있다. 그러나 한 프래그먼트에서 다른 프래그먼트로 이동하는 click handler는 구현되어 있지 않다. 이제 코드에 ViewModel을 추가해보자
 click handler에서 앱이 다른 destination으로 이동할 때 변경되는 LiveData를 설정한다. 프래그먼트는 LiveData를 관찰하고 데이터가 변경되면 프래그먼트가 destination으로 이동하여 viewModel에게 완료되었음을 알리고 state variable을 재설정한다
 
 #### 1) SleepTrackerViewModel을 열어서 사용자가 stop button을 탭하면 수면 품질 등급을 수집하는 SleepQualityFragment로 이동하도록 navigation을 추가해야한다
 
 #### 2) SleepTrackerViewModel에서 앱이 SleepQualityFragment로 이동할 때 변경되는 LiveData를 만든다. 캡슐화를 사용하면 단지 gettable만 할 수 있는 LiveData를 만들어 viewModel에 전달할 수 있다.
 
 이 코드를 클래스 본문의 최상위에 둔다
 
 ```
 private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
 
 val navigateToSleepQuality: LiveData<SleepNight>
    get() = _navigateToSleepQuality
 ```
 
 <br>
 
 #### 3) navigation을 trigger하는 변수를 reset하는 doneNavigation() 함수를 추가한다
 
 ```
 fun doneNavigating() {
    _navigateToSleepQuality.value = null
 }
 ```
 
 <br>
 
 #### 4) onStopTracking()을 호출하는 stop button을 클릭 시 SleepQualityFragment로 이동하도록 트리거한다. launch() 블록의 마지막에서 _navigateToSleepQuality 변수에 night 값을 넣는다. 변수가 값을 가지고 있으면 app이 SleepQualityFragment로 이동할 때 night를 함꼐 넘긴다.
 
 ```
 _navigateToSleepQuality.value = oldNight
 ```
 
 <br>
 
 #### 5) SleepTrackerFragment는 앱이 언제 navigate 하는지 알기 위해 _navigateToSleepQuality를 observe 해야한다. SleepTrackerFragment의 onCreateView()에 navigateToSleepQuality()에 대한 observer를 추가한다. androidx.lifecycle.Observer를 import한다 
 
 ```
 sleepTrackerViewModel.navigateToSleepQuality.observe(this, Observer {
 })
 ```
 
 <br>
 
 #### 6) observer 블럭 안에서 현재 night의 id를 전달하고 이동시킨다. 그런 다음 doneNavigation()을 호출한다. 
 ```
 night ->
 night?.let {
    this.findNavController().navigate(
            SleepTrackerFragmentDirections
                    .actionSleepTrackerFragmentToSleepQualityFragment(night.nightId))
    sleepTrackerViewModel.doneNavigating()
 }
 ```
 
 <br>
 
 #### 7) 앱을 실행시키고 Start 버튼을 누른 다음 Stop 버튼을 눌러서 SleepQualityFragment로 이동하는지 확인한다.
 
 
 <br><br>
 
## 2. Record the sleep quality
 SleepQualityFragment를 업데이트 하기 위해 ViewModel과 ViewModelFactory를 만들어야 한다.
 
 ### Step 1: Create a ViewModel and a ViewModelFactory
 
 #### 1) sleepquality 패키지에서 SleepQualityViewModel.kt를 연다
 
 #### 2) sleepNightKey와 database를 인자로 가지는 SleepQualityViewModel 클래스를 생성하고 SleepTrackerViewModel에서 했던 것처럼 factory로 database를 전달한다. 또한 navigation에서 sleepNightKey를 전달해야한다.
 
 ```
 class SleepQualityViewModel(
    private val sleepNightKey: Long = 0L,
    val database: SleepDatabaseDao) : ViewModel() {  
 }
 ```
 
 <br>
 
 #### 3) SleepQualityViewModel 클래스에서 Job과 UiScope를 정의하고 onCleared()를 오버라이드한다
 
 ```
 private val viewModelJob = Job()
 private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
 
 override fun onCleared() {
    super.onCleared()
    viewModelJob.cancel()
 }
 ```
 
 <br>
 
 #### 4) SleepTrackerFragment로 돌아가기 위해 앞에서 했던 것과 같이 _navigateToSleepTracker를 선언하고 navigateToSleepTracker와 doneNavigating()을 구현한다
 
 ```
 private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
 
 val navigateToSleepTracker: LiveData<Boolean?>
    get() = _navigateToSleepTracker
   
 fun doneNavigating() {
    _navigateToSleepTracker.value = null
 }
 ```
 
 <br>
 
 #### 5) 모든 sleep-quality 이미지에서 사용하는 onSetSleepQuality()라는 click handler를 만든다. 이전 단계에서 했던 coroutine 패턴을 그대로 사용한다
 
   - uiScope에서 coroutine을 실행하고 I/O dispatcher로 변경한다
   - sleepNightKey를 사용하여 tonight 값을 얻는다
   - sleep quality를 설정한다
   - 데이터베이스를 업데이트 한다
   - navigation을 트리거시킨다
   
 ```
 fun onSetSleepQuality(quality: Int) {
     uiScope.launch {
         // IO is a thread pool for running operations that access the disk, such as
         // our Room database.
         withContext(Dispatchers.IO) {
             val tonight = database.get(sleepNightKey) ?: return@withContext
             tonight.sleepQuality = quality
             database.update(tonight)
         }

         // Setting this state variable to true will alert the observer and trigger navigation.
         _navigateToSleepTracker.value = true
     }
 }
 ```
   
 <br>
 
 #### 6) sleepquality 패키지에서 SleepQualityViewModelFactory.kt를 열어 SleepQualityViewModelFactory 클래스를 추가한다. 이 클래스는 이전에 본 것과 동일한 코드를 사용한다.
 
 ```
 class SleepQualityViewModelFactory(
        private val sleepNightKey: Long,
        private val dataSource: SleepDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepQualityViewModel::class.java)) {
            return SleepQualityViewModel(sleepNightKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
 }
 ```
 
 <br>
 
 ### Step 2: Update the SleepQualityFragment
 
 #### 1) SleepQualityFragment.kt를 열어서 onCreateView()에 navigation에서 전달한 argument를 가져와야한다. 이 argument는 SleepQualityFragmentArgs에 있고 bundle로부터 추출해야한다.
 
 ```
 val arguments = SleepQualityFragmentArgs.fromBundle(arguments!!)
 ```
 
 <br>
 
 #### 2) dataSource 값을 얻는다.
 
 ```
 val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
 ```
 
 <br>
 
 #### 3) factory를 생성하고 dataSource와 SleepNightKey를 인자로 넘긴다.
 
 ```
 val viewModelFactory = SleepQualityViewModelFactory(arguments.sleepNightKey, dataSource)
 ```
 
 <br>
 
 #### 4) ViewModel 레퍼런스를 얻는다
 
 ```
 val sleepQualityViewModel =
    ViewModelProviders.of(
        this, viewModelFactory).get(SleepQualityViewModel::class.java)
 ```
 
 <br>
 
 #### 5) ViewModel에 binding 객체를 더한다.
 ```
 binding.sleepQualityViewModel = sleepQualityViewModel
 ```
 
 <br>
 
 #### 6) observer를 추가한다.
 
 ```
 sleepQualityViewModel.navigateToSleepTracker.observe(this, Observer {
    if (it == true) {
        this.findNavController().navigate(SleepQualityFragmentDirections.actionSleepQualityFragmentToSleepTrackerFragment())
        sleepQualityViewModel.doneNavigating()
    }
 })
 ```

 <br>
 
 ### Step 3: Update the layout file and run the app
 
 #### 1) fragment_sleep_quality.xml 레이아웃 파일을 열고 <data> 블럭 안에 SleepQualityViewModel 변수를 추가한다
 
 ```
    <data>
        <variable
            name="sleepQualityViewModel"
            type="com.example.android.trackmysleepquality.sleepquality.SleepQualityViewModel" />
    </data>
 ```
 
 <br>
 
 #### 2) 6개의 sleep-quallity 이미지 각각에 아래와 같은 클릭 핸들러를 추가한다. 이미지에 quality 등급을 매칭시킨다.
 
 