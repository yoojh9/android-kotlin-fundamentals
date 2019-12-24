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