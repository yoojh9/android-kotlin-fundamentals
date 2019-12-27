# 07-2 DiffUtil and Data binding with RecyclerView

## 1. Get started and review what you have so far

<image src="./images/recyclerview_1" width="70%" height="70%"/>

 - 사용자의 입력으로 app은 SleepNight 객체 리스트를 생성한다. 각각의 SleepNight 객체는 하룻밤의 수면, 지속시간, 수면의 질 등을 나타낸다
 - SleepNightAdapter는 SleepNight 객체 목록을 RecyclerView가 표시할 수 있도록 조정해준다.
 - SleepNightAdapter는 리사이클러가 데이터를 표시하기 위한 메타 정보, 데이터, 뷰를 포함하는 ViewHolder를 생성한다
 - RecyclerView는 SleepNightAdapter를 사용하여 얼마나 많은 아이템을 화면에 보여줘야 할지(getItemCount()) 결정한다.
 - RecyclerView는 onCreateViewHolder()와 onBindViewHolder()를 사용하여 보여져야 할 데이터가 바인딩 된 view holder를 가져온다.
 
<br>

### notifyDataSetChanged() 메소드는 비효율적이다
 목록 안에 있는 아이템이 변경되어 업데이트 해야 한다고 RecyclerView에 알리려면 SleepNightAdapter에서 notifyDataSetChanged()를 호출해야 한다.
 
 ```
 var data =  listOf<SleepNight>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
 ```
 
 그러나 notifyDataSetChanged()는 리사이클러뷰에게 전체 목록이 유효하지 않음을 알려주고 그 결과로 RecyclerView는 리스트에 화면에 표시되지 않는 아이템을 포함하여 다시 bind하고 다시 draw한다. 이것은 매우 불필요한 작업이다.
 양이 많거나 복잡한 리스트의 경우, 프로세스가 사용자의 목록을 스크롤 할 때 화면이 깜빡거리거나 끊길 수 있을 정도로 오래 걸릴 수 있다
 
 이 문제를 해결하려면 RecyclerView에게 정확히 어떤 것이 변경되었는지 알려서 화면 상에 변경된 view만 업데이트 하도록 할 수 있다
 
 RecyclerView에는 단일 요소를 업데이트하기 위한 풍부한 API가 있다. notifyItemChanged()를 사용하여 RecyclerView에 item이 변경되었음을 알리고 추가, 제거 또는 이동한 항목에 대해서도 사용할 수 있다
 다행히도 더 좋은 방법이 있다


<br>

### DiffUtil is efficient and does the hard work for you
 RecyclerView에는 두 목록 간의 차이점을 계산하기 위한 DiffUtil이라는 클래스가 있다. DiffUtil은 old list와 new list를 비교하여 어떤 것이 다른지 알아낸다. 이것은 추가, 제거 또는 변경된 항목을 찾는다.
 DiffUtil이 어떤것이 변경되었는지 알아내면 RecyclerView는 이 정보를 사용하여 변경, 추가, 제거 또는 이동된 항목만 업데이트 할 수 있으며 이는 전체 목록을 다시 실행하는 것보다 훨신 효율적이다
 

<br><br>

## 2. Refresh list content with DiffUtil
이 작업에서는 DiffUtil을 사용하여 데이터 변경에 대해 RecyclerView를 최적화 하도록 SleepNightAdapter를 업그레이드 한다.


### Step 1: Implement SleepNightDiffCallback
DiffUtil 클래스의 기능을 사용하기 위해 DiffUtil.ItemCallback을 상속한다.

 #### 1) SleepNightAdapter.kt를 연다
 
 #### 2) SleepNightAdapter 클래스 정의 밑에 DiffUtil.ItemCallback을 상속받는 SleepNightDiffCallback 클래스를 만든다. SleepNight를 generic 파라미터로 넘긴다
 
 ```
 class SleepNightDiffCallback : DiffUtil.ItemCallback<SleepNight>() {
 }
 ```
 
 #### 3) Alt + Enter (Option + Enter on Mac)을 눌러 Implement Members를 선택한다
 
 #### 4) areItemsTheSame() 함수 내부에서 SleepNight 객체인 oldItem과 newItem이 같은지 비교하는 코드를 추가한다. 만약 아이템이 동일한 nightId를 가지고 있으면 같은 아이템으로 보고 true를 리턴한다. 그렇지 않으면 false를 리턴한다. DiffUtil은 이 함수를 사용하여 항몽기 추가, 제거 또는 이동되었는지 확인한다
 
 ```
 override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
    return oldItem.nightId == newItem.nightId
 }
 ```
 
 <br>
 
 #### 5) areContentsTheSame() 메소드 안에서 oldItem과 newItem이 같은 데이터를 포함하고 있는지 체크한다. 즉 equality를 체크한다. SleepNight는 데이터 클래스이므로 이 동등성 검사는 모든 필드를 검사한다. Data class는 자동으로 equals와 그 외 몇가지 메소드를 정의한다. oldItem과 newItem간에 차이가 있는 경우 이 함수는 DiffUtil에 항목이 업데이트 되었음을 알려준다.
 
 ```
 override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
    return oldItem == newItem
 }
 ```

<br><br>

## 3. Use ListAdapter to manage your list
RecyclerView를 이용하는 일반적인 패턴은 변경되는 목록을 표시하는 것이다. RecyclerViewe는 adapter 클래스인 ListAdapter를 제공한다. ListAdapter는 사용자를 위해 목록을 추적하고 목록이 업데이트 될 때 어댑터에 알린다

### Step 1: ListAdapter를 상속하도록 adapter를 변경한다.

#### 1) SleepNightAdapter.kt 파일에서 SleepNightAdapter가 ListAdapter를 상속하도록 변경한다.

#### 2) ListAdapter에 첫번째 인자로 SleepNight을 추가하고, 두번쨰 인자로 SleepNightAdapter.ViewHolder를 넘긴다

#### 3) SleepNightDiffCallback()를 생성자에 파라미터로 추가한다. ListAdapter는 이를 사용하여 목록에서 변경된 사항을 파악한다. 여기까지 완성된 SleepNightAdapter 클래스의 선언은 아래와 같다

```
class SleepNightAdapter : ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()) {
```

#### 4) SleepNightAdapter 클래스에서 setter를 포함한 data 필드를 삭제한다. ListAdapter가 list를 계속 추적하므로 data 필드는 더이상 필요없다.

#### 5) onBindViewHolder()의 에러를 제거하기 위해 item 변수를 변경한다. data를 사용하는 대신에 ListAdatper에서 제공하는 getItem(position)을 호출하여 item을 얻어온다.

```
val item = getItem(position)
```

<br>

### Step 2: submitList()를 사용하여 목록을 업데이트 해라
변경된 목록이 사용 가능해지면 코드에서 ListAdapter에 알려야 한다. ListAdapter는 새로운 버전의 목록을 사용할 수 있음을 ListAdapter에 알리기 위해 submitList()라는 메소드를 사용한다. 
이 메소드가 호출되면 ListAdapter는 새 목록을 이전 목록과 비교하여 추가, 제거, 이동 또는 변경된 항목을 감지한다
그런 다음 ListAdapter는 RecyclerView에 표시된 항목을 업데이트 한다

#### 1) SleepTrackerFragment.kt를 연다
 
#### 2) onCreateViewe() 안에 sleepTrackerViewModel observer 코드 내의 에러가 나는 data 변수를 찾는다

#### 3) adapter.data = it 을 adapter.submitList(it)으로 변경한다.

```
sleepTrackerViewModel.nights.observe(viewLifecycleOwner, Observer {
   it?.let {
       adapter.submitList(it)
   }
})
```

<br><br>

## 3. Use DataBinding with RecyclerView
이전 data binding 관련 작업에서 진행했던 것처럼 findViewById() 호출하는 부분을 모두 지운다

<br>

### Step 1: Add data binding to the layout file

#### 1) list_item_sleep_night.xml 레이아웃 파일의 Text 탭을 연다

#### 2) constraintLayout 태그에 커서를 올려놓고 Alt + Enter(Option + Enter on Mac)를 누른다. 

#### 3) convert to data binding layout을 선택한다. 이 것은 layout을 <layout> 태그로 감싸고 <data> 태그를 안에 추가해준다

#### 4) 상단의 <data> 태그에 sleep 변수를 선언한다

#### 5) 아래와 같이 작성한다

```
   <data>
     <variable
        name="sleep"
        type="com.example.android.trackmysleepquality.database.SleepNight"/>
   </data>
```

#### 6) ListItemSleepNightBinding 바인딩 객체와 함께 관련된 코드들이 프로젝트의 generated files에 추가된다.

<br>

### Step 2: Inflate the item layout using data binding

#### 1) SleepNightAdapter.kt를 연다

#### 2) ViewHolder 클래스에서 from() 메소드를 찾는다

#### 3) view 변수 선언을 지운다

```
// 지워야 할 코드
val view = layoutInflater
       .inflate(R.layout.list_item_sleep_night, parent, false)
```

#### 4) view 변수가 있던 곳에 ListItemSleepNightBinding 바인딩 객체를 inflate한 binding 변수를 새로 정의한다.

```
val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)
```

#### 5) function의 끝에 view를 리턴하던 문장 대신 binding을 리턴한다

```
return ViewHolder(binding)
``` 

#### 6) 에러를 제거하기 위해 binding 단어에 커서를 두고 Alt + Enter(Option + Enter on a Mac)을 누른다

#### 7) Change parameter 'itemview' type of primary constructor of class 'ViewHolder' to 'ListItemSleepNightBinding'을 누른다.
 - 이는 ViewHolder 클래스의 파라미터 타입을 변경한다.

#### 8) 스크롤을 올려서 ViewHolder 선언문의 itemView 타입이 View에서 ListItemSleepNightBinding으로 변경된 것을 확인할 수 있다. itemView에 에러가 표시되는데 이는 from() 메소드에서 View 타입의 itemView를 binding으로 변경했기 때문이다
 - ViewHolder 클래스에서 itemView 변수 중 하나에 커서를 두고 오른쪽 버튼을 클릭한 뒤 Refactor > Rename을 선택한다. 이름을 binding으로 변경한다

#### 9) constructor 파라미터인 binding 앞에 val을 붙여서 프로퍼티로 만든다

#### 10) 부모 클래스인 RecyclerView.ViewHolder를 호출할 때 매개변수를 binding에서 binding.root로 변경한다

#### 11) 완성된 코드는 아래와 같다

```
class ViewHolder private constructor(val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root){
```

<br>

### Step 3: Replace findViewById()
이제 findViewById() 대신에 binding 객체를 사용하여 sleepLength, quality, qualityImage 프로퍼티를 변경할 수 있다

#### 1) sleepLength, qualityString, qualityImage의 초기화를 binding 객체의 뷰를 사용해서 변경한다.

```
val sleepLength: TextView = binding.sleepLength
val quality: TextView = binding.qualityString
val qualityImage: ImageView = binding.qualityImage
```

#### 2) binding 뷰 객체는 굳이 선언하지 않고 바로 사요할 수 있다. 위의 선언을 지우고 bind()함수에서 binding.sleepLength.text와 같이 직접 사용한다


<br><br>

## 4. Create binding adapters
이번 단계에서는 binding adapter와 함께 data binding을 사용하여 데이터를 설정하도록 앱을 업그레이드 한다.
이전 단계에서는 transformations 클래스를 사용하여 LiveData를 가져와서 textView에 보여줄 포맷팅 된 문자열을 생성했다.
그러나 다른 타입 또는 복합 타입을 바인드 해야 하는 경우 데이터 바인딩에서 해당 타입을 사용하는 데 도움이 되는 바인딩 어댑터를 제공할 수 있다.
바인딩 어댑터는 데이터를 가져와서 데이터 또는 텍스트나 이미지와 같은 뷰를 바인딩 하는데 사용할 수 있는 데이터에 적용하는 어댑터이다.

quality image, 그리고 각각의 text 필드에 binding adapter를 구현하려고 한다. 요약하자면 바인딩 어댑터를 선언하기 위해 항목과 뷰를 가져오는 메소드를 정의하고 @BindingAdapter를 어노테이션으로 달 수 있다
메소드의 본문에서 transformation을 구현한다. 코틀린에서는 데이터를 수신하는 view 클래스에서 확장 함수로 binding adapter를 작성할 수 있다

<br>

### Step 1: Create binding adapters

#### 1) SleepNightAdapter.kt 파일을 연다

#### 2) binding.sleepLength, binding.quality, binding.qualityImage의 값을 계산하는 코드를 가져와서 대신 adapter 내부에서 사용한다.

#### 3) sleeptracker 패키지에서 BindingUtils.kt 파일을 만든다

#### 4) TextView에 setSleepDurationFormatted 확장함수를 선언하고 매개변수로 SleepNight을 넘긴다. 이 함수는 수면시간을 계산하고 포맷하기 위한 어댑터가 된다
```
fun TextView.setSleepDurationFormatted(item: SleepNight) {}
```

#### 5) ViewHolder.bind()에서 했던 것처럼 setSleepDurationFormatted 함수 내부에서 데이터를 뷰에 바인딩한다. convertDurationToFormatted()를 호출하고 TextView의 text에 포맷팅 된 텍스트를 넣는다
TextView의 확장함수이므로 text 프로퍼티에 직접 접근할 수 있다

```
text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, context.resources)
```

#### 6) 바인딩 어댑터에 대한 데이터 바인딩을 알리려면 함수에 @BindingAdapter 어노테이션을 추가한다

#### 7) 이 함수는 sleepDurationFormatted 속성의 어댑터이므로 sleepDurationFormatted를 @BindingAdapter에 인자로 전달한다

```
@BindingAdapter("sleepDurationFormatted")
```

#### 8) 두 번째 어댑터는 SleepNight 객체의 값에 따라 수면 품질을 설정한다. setSleepQualityString() 이라는 확장함수는 TextView에 만들고 SleepNight을 인자로 넘긴다

#### 9) 함수에 body에 ViewHolder.bind()에서와 같이 데이터를 뷰에 바인딩한다. convertNumericQualityToString을 호출하고 text에 설정한다

#### 10) 함수에 @BindingAdapter("sleepQualityString) 어노테이션을 추가한다

```
@BindingAdapter("sleepQualityString")
fun TextView.setSleepQualityString(item: SleepNight) {
   text = convertNumericQualityToString(item.sleepQuality, context.resources)
}
```

#### 11) 세번쨰 binding adapter는 image view에 image를 설정한다. ImageView에 setSleepImage() 확장함수를 만들고 ViewHolder.bind()에 있던 코드를 사용하여 아래와 같이 만든다

```
@BindingAdapter("sleepImage")
fun ImageView.setSleepImage(item: SleepNight) {
   setImageResource(when (item.sleepQuality) {
       0 -> R.drawable.ic_sleep_0
       1 -> R.drawable.ic_sleep_1
       2 -> R.drawable.ic_sleep_2
       3 -> R.drawable.ic_sleep_3
       4 -> R.drawable.ic_sleep_4
       5 -> R.drawable.ic_sleep_5
       else -> R.drawable.ic_sleep_active
   })
}
```

<br><br>

### Step 2: Update SleepNightAdapter

#### 1) SleepNightAdapter.kt를 연다

#### 2) 데이터 바인딩과 새 어댑터를 사용하여 작업을 수행하기 위해 bind() 안에 있는 모든 코드를 지운다. 

#### 3) bind() 내부에서 sleep에 item을 하당한다. 

```
binding.sleep = item
```

#### 4) 그 밑에 binding.executePendingBindings()를 추가한다. 이 함수는 즉시 바인딩을 실행해야 할 때 강제로 실행하기 위해 사용된다. RecyclerView에서 binding adapter를 사용할 때는 뷰 크기 조절 속도를 약간 높일 수 있으므로 항상 executePendingBinding()을 호출하는 것이 좋다
```
binding.executePendingBindings()
```

<br><br>

### Step 3: Add bindings to XML layout

#### 1) list_item_sleep_night.xml 파일을 연다

#### 2) ImageView 안에 이미지를 설정하는 바인딩 어댑터와 이름이 같은 app 속성을 추가한다. 아래 코드와 같이 sleep 변수를 넘긴다. 이 속성은 adapter를 통해 view와 binding object 사이의 connection을 생성한다. sleepImage가 참조될 때마다 어댑터는 SleepNight으로부터 데이터를 적용시킨다.

```
app:sleepImage="@{sleep}
``` 

#### 3) slee_length와 quality_string TextView에도 같은 방식으로 적용한다. sleepDurationFormatted나 sleepQualityString을 참조할 때 마다 adapter가 SleepNight의 data를 적용시킨다

```
app:sleepDurationFormatted="@{sleep}"
app:sleepQualityString="@{sleep}
```

#### 4) 

 