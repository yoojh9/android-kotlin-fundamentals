# 07-4 Interacting with RecyclerView items

## 1. Inspect the changes to the app

### Step 1: Inspect the code for the sleep details screen
이번 단계에서는 click handler를 구현하여 클릭된 sleep night의 detail을 보여주는 fragment로 이동시키는 네비게이션을 만든다.

#### 1) sleepdetail 패키지 내에는 프래그먼트에 하룻밤의 detail 정보를 표시하기 위한 fragment, view model, view model factory가 포함되어 있다

#### 2) sleepdetail 패키지 내의 SleepDetailViewModel을 열고 코드를 살펴본다. 이 view model은 생성자 파라미터로 SleepNightKey와 DAO를 받는다
 
 - ViewModel 클래스의 본문에는 주어진 키로 SleepNight 객체를 가져오는 코드가 있고, close 버튼을 눌렀을 때 SleepTrackerFragment로 이동시키는 navigateToSleepTracker 변수도 있다
 - SleepDatabaseDao에는 LiveData<SleepNight>을 리턴하는 getNightWithId() 함수가 정의되어 있다
  
#### 3) SleepDetailFragment 코드를 살펴보면 data binding, view model, navigation의 observer를 설정하고 있는 것을 알 수 있다.

#### 4) fragment_sleep_datail.xml을 살펴보면 각 view에 데이터를 표시할 수 있는 view model인 sleepDetailViewModel 변수가 <data> 태그에 정의되어 있는 것을 알 수 있다
 
#### 5) navigation.xml 파일을 열고 sleep_tracker_fragment에서 sleep_tracker_detail로 이동하는 action_sleep_tracker_fragment_to_sleepDetailFragment 액션이 추가되어 있는 것을 확인한다

<br><br>

## 2. Make items clickable
이 단계에서는 탭한 항목에 대해 세부 정보 화면을 표시하여 사용자의 탭에 응답하도록 RecyclerView를 업데이트 한다

클릭을 받고 처리하는 작업은 두 부분으로 이루어진다. 
 - 첫째, 클릭을 listen 하고 어떤 item이 클 되었는지 결정해야한다
 - 둘째, 클릭에 액션으로 응답해야 한다.

앱에 클릭 리스너를 추가하기에 가장 적당한 장소는 어디일까?
 - SleepTrackerFragment는 많은 뷰를 호스팅한다. 그래서 fragment 레벨 수준에서 click 이벤트를 리스닝하면 어떤 아이템이 클릭되었는지 알 수 없다. 
 - RecyclerView 레벨에서 listening 하면 사용자가 목록에서 어떤 항목을 클릭했는지 정확히 파악하기는 어렵다
 - 클릭한 항목 하나에 대한 정보를 가장 빨리 얻을 수 있는 곳은 ViewHolder 객체에 있다. ViewHolder 객체는 하나의 목록 항목을 나타내기 때문이다.
 
ViewHolder가 click 이벤트를 listen 하기에는 좋은 곳이지만 일반적으로 클릭을 처리하기에 적합한 곳은 아니다. 그래서 click 이벤트를 핸들링 하기에 가장 적합한 곳은 어디일까?
 - Adapter는 뷰에 data item들을 보여준다. 그러므로 adapter에서는 click을 handle할 수 있다. 그러나 adapter의 역할은 데이터를 적용해서 보여주는 것이고 app logic 작업은 하지 않는다
 - click 이벤트의 처리는 ViewModel에서 해야한다. ViewModel은 data에 접근할 수 있고 클릭에 대한 응답으로 발생해야되는 사항을 결정하는 로직을 가질 수 있다
 
<br>

### Step 1: Create a click listener and trigger it from the item layout

 #### 1) sleeptracker 패키지에서 SleepNightAdapter.kt 파일을 연다

 #### 2) 파일의 끝에 SleepNightListener라는 새로운 클래스를 만든다 

```
class SleepNightListener() {
    
}
```

 #### 3) SleepNightListener 클래스 안에 onClick() 함수를 추가한다. 항목을 클릭하면 onClick() 함수가 호출된다 (뷰의 android:onClick 속성을 나중에 이 함수로 설정해야한다)

```
class SleepNightListener() {
    fun onClick() = 
}
```

 #### 4) 함수의 인자로 SleepNight 타입의 night를 추가한다. view는 표시되는 항목을 알고 있으며 클릭을 처리하기 위해 해당 정보를 전달해야 한다

```
class SleepNightListener() {
    fun onClick(night: SleepNight) = 
}

```

 #### 5) onClick()의 기능을 정의하려면 SleepNightListener의 생성자에서 clickListener 콜백을 제공하고 onClick()에 할당한다

```
class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
   fun onClick(night: SleepNight) = clickListener(night.nightId)
}
```

 #### 6) list_item_sleep_night.xml 파일을 열고 \<data\> 블럭 안에 데이터 바인딩을 통해 SleepNightListener 클래스를 사용할 수 있도록 새 변수를 추가해라.
 - 이제 레이아웃에서 SleepNightListener 클래스의 onClick() 함수에 접근할 수 있다

```
<variable
    name="clickListener"
    type="com.example.android.trackmysleepquality.sleeptracker.SleepNightListener" />
```

 #### 7) item 항목의 어느 부분에서나 클릭을 listen 하려면 ConstraintLayout에 android:onClick 속성을 추가해라
 - 아래와 같이 data binding lambda를 사용하여 clickListener:onClick(sleep)을 속성에 추가한다
 
```
android:onClick="@{() -> clickListener.onClick(sleep)}"
```

<br>

### Step 2: Pass the click listener to the view holder and the binding object

 #### 1) SleepNightAdapter.kt 파일을 연다
 
 #### 2) SleepNightAdatper 클래스의 생성자를 val clickListener: SleepNightListener 프로퍼티를 받도록 수정한다. adapter가 ViewHolder를 바인딩할 때 이 클릭 리스너를 제공해야한다
 
 ```
 class SleepNightAdapter(val clickListener: SleepNightListener):
        ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()) {
 ```
 
 #### 3) 클릭 리스너를 ViewHolder로 전달하려면 holder.bind()에 대한 호출을 업데이트 해야한다.
 - 파라미터를 추가했기 때문에 compile error가 발생한다.
 
 ```
 holder.bind(getItem(position)!!, clickListener)
 ```
 
 #### 4) 에러에 커서를 올려두고 Alt + Enter (Option + Enter on Mac)를 눌러 'Add parameter to function bind'를 선택한다.
 
 #### 5) ViewHolder 클래스의 bind() 함수 내에 click listener를 binding 객체에 지정한다. 
 
 ```
 binding.clickListenr = clickListener
 ```
 
 #### 6) 최종적으로 어댑터 생성자에서 클릭 리스너를 가져와서 ViewHolder와 binding 객체에 전달했다.
 
<br>

### Step 3: Display a toast when an item is tapped
 이제 클릭을 캡쳐하는 코드가 준비되었지만 항목이 탭 되었을 때 어떤 일이 발생할지 구현되어 있지 않다. 간단한 응답은 항목을 클릭할 때 클릭된 항목의 nightId를 토스트로 보여주는 것이다. 
 
 #### 1) SleepTrackerFragment.kt를 연다
 
 #### 2) onCreateView()에서 adapter 변수를 찾는다. click listener 파라미터가 필요하므로 error가 발생한다
 
 #### 3) SleepNightAdapter에 Lambda를 전달하여 클릭 리스너를 정의한다. 이 간단한 람다식은 nightId를 토스트 메세지로 보여준다. 
 
 ```
 val adapter = SleepNightAdapter(SleepNightListener { nightId ->
     Toast.makeText(context, "${nightId}", Toast.LENGTH_LONG).show()
 })
 ```

<br><br>

## 3. Handle item clicks
이번 단계에서는 RecyclerView에서 아이템이 클릭될 때 토스트 대신에 다른 이벤트를 발생시켜 보자. 클릭한 night에 대해 상세 정보 fragment로 이동하는 기능을 만들어본다

### Step 1: Navigate on click
step 1에서는 단지 토스트를 보여주는 것 대신에 nightTrackerFragment.onCreateView()에서 click listener lambda를 변경하여 nightId를 SleepTrackerViewModel로 전달하고 SleepDetailFragment로 이동시키는 작업을 진행한다

 #### 1) SleepTrackerViewModel.kt를 연다
 
 #### 2) SleepTrackerViewModel 내부에 onSleepNightClicked() 클릭 핸들러 함수를 정의한다.
 
 ```
 fun onSleepNightClicked(id: Long) {
 
 }
 ```
 
 #### 3) 함수 내부에 _navigationToSleepDetail 값을 id로 설정하여 navigation을 트리거 시킨다.
 
 ```
 fun onSleepNightClicked(id: Long) {
    _navigateToSleepDetail.value = id
 }
 ```
