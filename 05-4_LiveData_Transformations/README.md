# 05-4 LiveData Transformations

## 1. Add a Timer
 - 이번 단계에서는 앱에 timer를 추가해본다. 단어 리스트가 비어지면 게임을 종료하는 대신에 timer가 끝나면 게임이 종료되는 방식으로 변경한다.
 - 안드로이드에서는 타이머를 구현하는데 사용되는 [CountDownTimer](https://developer.android.com/reference/android/os/CountDownTimer)라는 클래스를 제공한다 
 - configuration 변화 중에 데이터가 손상되지 않도록 GameViewModel에 타이머를 추가한다
 - 프래그먼트에는 timer 틱에 따라 타이머 text view를 업데이트 하는 코드가 포함되어야 한다
 
 GameViewModel 클래스를 아래와 같이 구현한다
 
 #### 1) timer constant를 가지고 있는 companion object를 만든다
 
 ```
 companion object {
 
    // Time when the game is over
    private const val DONE = 0L
 
    // Countdown time interval
    private const val ONE_SECOND = 1000L
 
    // Total time for the game
    private const val COUNTDOWN_TIME = 60000L
 
 }
 ```
 
 <br>
 
 #### 2) 타이머의 카운트다운 시간을 저장하기 위해 _currentTime을 이름으로 하는 MutableLiveData 변수를 추가하고 currentTime을 backing property로 추가한다
 
 ```
 // Countdown time
 private val _currentTime = MutableLiveData<Long>()
 val currentTime: LiveData<Long>
    get() = _currentTime
 ```
 
 <br>
 
 #### 3) CountDownTimer를 타입으로 하는 private 변수 timer를 만든다. initialization 에러는 다음 단계에서 해결한다
 
 ```
 private val timer: CountDownTimer
 ```
 
 <br>
 
 #### 4) init 블럭에서 timer를 시작하고 초기화한다. 총 카운트타운 시간인 COUNTDOWN_TIME과 시간 간격인 ONE_SECOND를 파라미터로 넘긴다. onTick()과 onFinish()를 오버라이드 하고 타이머를 시작한다
 
 ```
 // Creates a timer which triggers the end of the game when it finishes
 timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
 
    override fun onTick(millisUntilFinished: Long) {
        
    }
 
    override fun onFinish() {
        
    }
 }
 
 timer.start()
 ```
 
 <br>
 
 #### 5) 모든 tick 또는 모든 interval마다 호출되는 콜백 메소드인 onTick()을 구현해본다
  - 전달받은 millisUntilFinished 파라미터를 사용하여 _currentTimer를 업데이트 한다
  - millisUntilFinished는 밀리초 단위로 타이머가 완료될 때 까지의 시간이다
  - millisUntilFinished를 초로 변환하고 _currentTime에 할당한다
  
  ```
  override fun onTick(millisUntilFinished: Long)
  {
     _currentTime.value = millisUntilFinished/ONE_SECOND
  }
  ```
  
  <br>
 
 #### 6) onFinish() 콜백 메소드는 타이머가 끝날 때 호출된다. onFinish()를 구현하여 _currentTimer를 업데이트 하고 게임 종료 이벤트를 트리거한다
 
 ```
 override fun onFinish() {
    _currentTime.value = DONE
    onGameFinish()
 }
 ``` 
 
 <br>
 
 #### 7) 단어 list가 비어지면 게임을 종료하는 것이 아니라 단어 리스트를 다시 갱신하도록 nextWord() 메소드를 수정한다
 
 ```
 private fun nextWord() {
    // Shuffle the word list, if the list is empty 
    if (wordList.isEmpty()) {
        resetList()
    } else {
    // Remove a word from the list
    _word.value = wordList.removeAt(0)
 }
 ```
 
 <br>
 
 #### 8) onCleared() 메소드 내에서 메모리 누수를 피하기 위하여 타이머를 취소한다. onCleared () 메소드는 ViewModel이 파괴되기 전에 호출된다
 
 ```
 override fun onCleared() {
    super.onCleared()
    // Cancel the timer
    timer.cancel()
 }
 ```
 
 <br>
 
 #### 9) 앱을 실행시켜서 60초를 기다린 후 앱이 자동으로 종료되는 것을 확인한다. 그러나 타이머가 화면에 나타나지는 않는다. 다음 단계에서 확인해보자
 
 <br><br>

## 2. Add transformation for the LiveData
 - [Transformations.map()](https://developer.android.com/reference/android/arch/lifecycle/Transformations.html#map%28android.arch.lifecycle.LiveData%3CX%3E,%20android.arch.core.util.Function%3CX,%20Y%3E%29) LiveData의 데이터를 조작하고 결과로 LiveData 객체를 반환하는 메소드이다
 - 하지만 이런 변환은 observer가 새로 리턴된 LiveData를 observing하고 있지 않으면 계산되지 않는다
 - 이 메소드는 LiveData와 함수 파라미터를 가진다.
 - Transformations.map()에 전달된 람다 함수는 main thread에서 실행되므로 장시간 실행되는 작업은 포함하지 않는것이 좋다
 
 경과 시간 LiveData 객체를 "MM:SS" 형식의 새 문자열 LiveData 객체로 포맷하고, 포맷된 경과 시간을 화면에 표시한다
 
 game_fragment.xml layout 파일에 이미 timer text view가 포함되어 있다. 지금까지는 timer text view에 표시할 데이터가 없어 텍스트가 표시되지 않았다
 
 #### 1) GameViewModel 클래스에 currentTime을 초기화 한 후 currentTimerString을 이름으로 하는 LiveData 객체를 생성한다. 이 객체는 currentTime의 formatted string 버전이다
 
 #### 2) Transformations.map()을 사용하여 currentTimerString을 정의한다. currentTimer 및 시간 포맷을 변경하는 람다 함수를 전달한다.
  - DateUtils.formatElapsedTime()을 사용하여 람다 함수를 구현할 수 있다
  - 이 메소드는 밀리 초가 걸리고 "MM : SS"문자열 형식으로 변환된다.
  
  ```
  // The String version of the current time
  val currentTimeString = Transformations.map(currentTime) { time ->
     DateUtils.formatElapsedTime(time)
  }
  ```
 
 <br>
 
 #### 3) game_fragment.xml 파일의 timer text view에서 text 속성에 gameViewModel의 currentTimeString을 바인딩 시킨다
 
 ```
 <TextView
    android:id="@+id/timer_text"
    ...
    android:text="@{gameViewModel.currentTimeString}"
    ... />
 ```
 
 <br>
 
 #### 4) 앱을 실행시켜서 timer 텍스트가 1초에 한번씩 업데이트 되는지 확인한다. 이제 타이머가 끝날 때 까지 모든 단어를 순환해도 게임이 종료되지 않는다
 