# 07-1 RecyclerView

## 1. RecyclerView
 데이터의 list와 grid를 표시하는 것은 안드로이드의 가장 일반적인 UI 작업 중 하나이다. 텍스트의 리스트는 쇼핑 리스트와 같은 간단한 데이터를 표시하거나 많은 세부 내용을 담고 있는 스크롤 그리드와 같은 복잡한 리스트를 구현할 수 있다
 이런 모든 usecase를 지원하기 위해 안드로이드는 RecyclerView 위젯을 제공한다
 
 <image src="./images/recyclerview.png" width="70%" height="70%"/>
 
 RecyclerView의 가장 큰 장점은 큰 목록에 매우 효율적이라는 점이다.
 
   - 기본적으로 RecyclerView는 현재 화면에 표시된 아이템만 그리거나 처리한다. 예를 들어 목록에 수천개의 요소가 있지만 화면에 10개의 요소만 표시되는 경우 RecyclerView는 화면에 10개의 항목을 그리는 데 충분한 작업만 수행한다. 사용자가 스크롤을 하면 RecyclerView는 화면에 어떤 새 화면이 있어야 하는지 파악하고 해당 화면을 표시하기에 충분한 작업을 수행한다
   - item이 화면 밖으로 스크롤 되면 item의 뷰가 재활용 되어 새로운 content로 채워진다. 이 RecyclerView 동작은 많은 처리 시간을 절약하고 리스트를 유동적으로 스크롤 하는데 도움이 된다. 
   - 아이템이 변경되면 전체 리스트를 다시 그리는 대신 RecyclerView가 item 하나를 업데이트 할 수 있다. 복잡한 항목의 목록을 표시할 때 효율성이 크게 향상된다
 
 아래에 표시된 이미지에서 하나의 뷰가 ABC 데이터로 채워져 있음을 알 수 있다. 해당 뷰가 화면에서 스크롤 된 후 RecyclerView는 새 데이터 XYZ에 대해 뷰를 재사용한다.
 
 <image src="./images/recyclerview_2.png" width="70%" height="70%"/>
 
<br>

### 1) The adapter pattern
다른 전기 소켓을 사용하는 나라는 여행할 경우 어댑터를 사용하여 콘센트를 사용할 수 있음을 알고 있다. 어댑터를 사용하면 한 유형의 플러그를 다른 유형의 플러그로 변환할 수 있듯이 실제로 한 인터페이스를 다른 인터페이스로 변환시킨다
어댑터 패턴은 객체가 다른 API와 작동하도록 도와준다. 

RecyclerView는 데이터를 저장하고 처리하는 방법을 변경하지 않고, adpater를 사용하여 data를 변환시켜 RecyclerView에 표시한다.
sleep-tracker 앱에서는 ViewModel을 변경하지 않고 adapter를 빌드하여 Room 데이터베이스의 데이터를 RecyclerView에 적용시킨다.

<br>

### 2) Implementing a RecyclerView

 <image src="./images/recyclerview_3.png" width="70%" height="70%"/>
 
 RecyclerView에 데이터를 표시하기 위해 다음 단계가 필요하다
 
 - 표시할 Data
 - 뷰의 컨테이너 역할을 하는 레이아웃 파일에 정의 된 RecyclerView 인스턴스
 - 하나의 데이터 항목에 대한 레이아웃, 모든 item이 동일하게 보이는 경우 모든 항목에 대해 동일한 레이아웃을 사용할 수 있지만 필수는 아니다. 한번에 하나의 item 뷰만 생성하고 데이터를 채우기 위해 item layout은 fragment와 별도로 분리되어 만들어야 한다
 - layout manager, layout manager은 뷰의 UI 구성요소의 레이아웃을 다룬다
 - view holder, 뷰 홀더는 ViewHolder 클래스를 상속한다. item layout에 하나의 item을 표시하기 위한 view 정보가 포함되어 있다. 뷰 홀더는 RecyclerView가 화면에서 뷰를 효율적으로 이동하는 데 사용하는 정보도 추가된다
 - adapter, 어댑터는 데이터를 RecyclerView와 연결한다. ViewHolder에 데이터를 표시할 수 있도록 조정한다. RecyclerView는 어댑터를 사용하여 화면에 데이터를 표시하는 방법을 알아낸다
 
<br><br>

## 2. Implement RecyclerView and an Adapter
레이아웃 파일에 RecyclerView를 추가하고 sleep data를 RecyclerView에 표시하도록 adpater를 설정한다

### Step 1: Add RecyclerView with LayoutManager

#### 1) fragment_sleep_tracker.xml을 열어서 Design 탭을 누른다. 

#### 2) Component Tree 창에서 ScrollView를 지운다. 이 액션은 TextView도 함께 지워진다

#### 3) Palette 창에서 component type 중 containers를 선택한다

#### 4) palette 창에서 Component Tree 창으로 RecyclerView를 드래그 하여 ConstraintLayout 내부에 놓는다

#### 5) 다이얼로그에서 dependency를 추가할거냐고 물으면 안드로이드 스튜디오에서 gradle 파일에 recyclerview 디펜던시를 추가하기 위해 OK를 누른다.

#### 6) build.gradle에서 아래와 비슷한 디펜던시가 있는지 확인한다

```
implementation 'androidx.recyclerview:recyclerview:1.0.0'
```

<br>

#### 7) fragment_sleep_tracker.xml로 돌아가서 Text 탭을 누르고 아래와 같은 RecyclerView를 찾는다

```
<androidx.recyclerview.widget.RecyclerView
   android:layout_width="match_parent"
   android:layout_height="match_parent" />
```

<br>

#### 8) RecyclerView에 sleep_list라는 id를 부여한다

```
android:id="@+id/sleep_list"
```

<br>

#### 9) ConstraintLayout 내부에서 화면의 나머지 부분을 차지하도록 RecyclerView를 배치한다. 이렇게 하려면 RecyclerView의 각 parent의 side를 top의 경우 start button으로, buttom의 경우 clear button으로 설정한다. layout의 width와 height를 0dp로 설정한다

```
android:layout_width="0dp"
android:layout_height="0dp"
app:layout_constraintBottom_toTopOf="@+id/clear_button"
app:layout_constraintEnd_toEndOf="parent"
app:layout_constraintStart_toStartOf="parent"
app:layout_constraintTop_toBottomOf="@+id/stop_button"
```

<br>

#### 10) RecyclerView xml에 레이아웃 관리자(layout manager)를 추가한다. 모든 RecyclerView에는 list에 item을 배치하는 방법을 알려주는 레이아웃 관리자가 필요하다. 안드로이드는 full width 행의 세로 리스트에 항목을 배치하는 LinearLayout을 기본적으로 제공한다

```
app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
```

<br>

#### 11) Design 탭을 열어서 추가된 constraint 조건으 RecyclerView가 사용 가능한 공간을 채우도록 확장되었는지 확인한다

<br>

### Step 2: Create the list item layout and text view holder
RecyclerView는 컨테이너일 뿐이다. 이번 단계에서는 RecyclerView 내에 표시할 항목의 레이아웃과 구조를 생성한다.
가능한 빨리 RecyclerView를 만들어 작동시키기 위해 먼저 수면 quality의 숫자만 표시하는 간단한 목록 항목만 사용한다
이를 위해서는 뷰 홀더인 TextItemViewHolder가 필요하고, 데이터를 표시하기 위한 TextView도 필요하다 

#### 1) text_item_view.xml라는 레이아웃 파일을 만든다. 이후에 템플릿 코드로 사용되므로 root 요소를 어떤 것으로 사용할지는 중요하지 않다

#### 2) text_item_view.xml에서 생성된 코드는 모두 지운다.

#### 3) TextView에 start와 end에 16db 패딩을 추가하고 텍스트 사이즈를 24sp로 설정한다. with는 match_parent, height는 wrap_content로 맞춘다. 이 뷰는 RecyclerView 내에 표시되므로 ViewGroup 안에 view를 배치할 필요는 없다
 
```
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:textSize="24sp"
    android:paddingStart="16dp"
    android:paddingEnd="16dp"
    android:layout_width="match_parent"       
    android:layout_height="wrap_content" />
```

<br>

#### 4) Util.kt를 열어서 아래에 정의된 TextItemViewHolder 클래스를 생성한다.

```
class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)
```

<br>

### Step 3: Create SleepNightAdapter
RecyclerView를 구현하면서 가장 중요한 작업은 adapter를 생성하는 작업이다. 이전 단계에서 이미 각각에 item에 대한 layout과 item view에 대한 간단한 view holder를 가지고 있다. 이제 adapter를 만들 차례이다. adapter는 view holder를 생성하고 RecyclerView를 표시할 데이터로 채운다

#### 1) sleeptracker 패키지에서 SleepNightAdapter라는 코틀린 클래스를 만든다

#### 2) SleepNightAdapter 클래스를 RecyclerView.Adapter를 상속하도록 만든다. adapter는 사용할 view holder를 알아야 하므로 TextItemViewhHolder를 전달한다.

```
class SleepNightAdapter: RecyclerView.Adapter<TextItemViewHolder>() {}
```

<br>

#### 3) SleepNightAdatper의 상단의 SleepNight 타입의 listOf객체를 만든다 

```
var data = listOf<SleepNight>()
```

<br>

#### 4) SleepNightAdapter에서 getItemCount()를 오버라이드 하고 data 변수에 들어있는 sleep nights list의 size를 리턴한다. RecyclerView는 adpater가 보여줘야 할 item이 몇개인지 알아야 하며, getItemCount()를 호출하여 이를 수행한다.

```
override fun getItemCount() = data.size
```

<br>

#### 5) SleepNightAdapter에서 onBindViewHolder()를 아래와 같이 override한다. onBindViewHolder()는 특정한 위치에 하나의 아이템을 표시하기 위해 RecyclerView에 의해 호출된다. 그래서 onBindViewHolder()는 view holder와 바인딩할 데이터의 position이라는 두가지 인자를 사용한다. 이 앱의 경우 홀더는 TextItemViewHolder이고 위치는 리스트의 위치이다

```
override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
}
``` 

<br>

#### 6) onBindViewHolder() 안에 인자로 주어진 데이터의 position 값으로 하나의 item 변수를 생성한다

```
val item = data[position]
``` 

<br>

#### 7) 이전에 생성한 ViewHolder에는 textView 프로퍼티를 가지고 있다. onBindViewHolder() 안에 textView의 text를 sleep-quality-number로 설정한다. 

```
holder.textView.text = item.sleepQuality.toString()
```

<br>

#### 8) SleepNightAdapter에서 onCreateViewHolder()를 override 한다. onCreateViewHolder()는 RecyclerView가 하나의 item을 나타내는 view holder가 필요할 경우 호출된다.
 - 이 함수는 두 가지 파라미터를 받고 ViewHolder를 리턴한다. 하나는 parent 파라미터로 view holder를 가지고 있는 view group으로서 항상 RecyclerView이다. 
 - viewType 파라미터는 RecyclerView에 여러 view가 있을 때 사용된다. 예를 들어 text view의 리스트, 이미지, 비디오를 같은 RecyclerView에 놓는다면 onCreateViewHolder()는 사용할 뷰 유형이 어떤 것인지 알아야한다.
 
 ```
 override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
 }
 ```

<br>

#### 9) onCreateViewHolder()에서 LiatyoutInflater의 인스턴스를 만든다.
 - layout inflater는 xml 레이아웃에서 어떻게 뷰를 생성하는지 알고 있다. context에는 어떻게 view를 inflate 시키는지에 대한 정보를 포함하고 있다.
 - 어댑터는 항상 parent view group(RecyclerView)의 컨텍스트를 전달한다.

```
val layoutInflater = LayoutInflater.from(parent.context)
```

<br>

#### 10) onCreateViewHolder()에서 layoutInflater에 inflate를 요청하여 view를 만든다
 - 뷰의 XML layout과 parent view group을 넘기고, 세번째로 boolean 값인 attachToRoot를 넘긴다
 - attachToRoot는 false로 할당하는데 왜냐하면 RecyclerView가 시간에 따라 item을 view의 계층 구조에 추가하므로 false로 둔다
 
```
val view = layoutInflater
       .inflate(R.layout.text_item_view, parent, false) as TextView
```

<br>

#### 11) onCreateView()에서 TextItemViewHolder를 리턴한다.

```
return TextItemViewHolder(view)
```

<br>

#### 12) RecyclerView는 데이터에 대해 아무것도 모르기 때문에 데이터가 변하면 어댑터는 RecyclerView에게 알려주어야 한다. RecyclerView는 어댑터가 제공하는 뷰 홀더에 대해서만 알고 있다.
 - 표시되는 데이터가 변경되었을 때 RecyclerView에게 알리려면 SleepNightAdapter 클래스의 맨 위에 있는 데이터변수에 사용자 정의 setter를 추가한다
 - setter에서 data에 새로운 값을 제공한 다음 notifyDataSetChanged()를 호출하여 새 데이터로 목록을 다시 그리도록 트리거한다.
 - notifyDataSetChange() 함수가 호출될 때 RecyclerView는 item 뿐만 아니라 모든 리스트를 다시 그린다. 
 ```
 var data =  listOf<SleepNight>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
 ```
 
 <br><br>
 
 ### Step 4: Tell RecyclerView about the Adapter
 RecyclerView는 뷰 홀더를 얻는 데 사용할 어댑터에 대해 알아야 한다.
 
 #### 1) SleepTrackerFragment.kt를 열어서 onCreateView() 메소드에 adapter를 생성한다. 아래 코드를 return 문장 전, viewModel 생성 코드 이후에 넣는다
 ```
 val adapter = SleepNightAdapter()
 ```
 
 <br>
 
 #### 2) adapter를 RecyclerView와 연결한다
 
 ```
 binding.sleepList.adpater = adapter
 ```
 
 <br>
 
 #### 3) clean 하고 rebuild하여 binding 객체를 업데이트 한다.
 
 <br><br>
 
  
 ### Step 5: Get data into the adapter
 지금까지 어댑터와 어댑터에서 RecyclerView로 데이터를 가져 오는 방법을 알아봤다. 이제 viewModel에서 adpater로 데이터를 가져와야 한다.
 
 #### 1) SleepTrackerViewModel을 열어서 표시해야할 데이터인 모든 sleep nights를 저장하는 nights 변수를 찾는다. nights 변수는 데이터베이스에서 getAllNights()를 호출하여 설정된다.
 
 #### 2) nights에서 private 접근 제어자를 삭제한다. 이 변수를 액세스 해야 하는 observer를 만들어야 하므로 private을 제거한다. 
 ```
 val nights = database.getAllNights()
 ```
 
 <br>
 
 #### 3) database 패키지에서 SleepDatabaseDao를 연다
 
 #### 4) getAllNights() 함수를 찾고 이 함수가 SleepNights의 리스트를 LiveData 형식으로 리턴하는 것을 확인한다. 즉 nights 변수에는 Room에 의해 업데이트 된 LiveData가 포함되고 nights가 언제 바뀌는지 observe 할 수 있다.
 
 #### 5) SleepTrackerFragment를 연다
 
 #### 7) onCreateView()의 adapter 생성 코드 아래에 night 변수의 observer를 생성한다. 프래그먼트의 viewLifeCycleOwner를 라이프 사이클 소유자로 제공함으로써 RecyclerView가 화면에 있을 때만 옵저버가 활성화 되도록 한다
 
 ```
 sleepTrackerViewModel.nights.observe(viewLifecycleOwner, Observer {
    })
 ```
 
 <br>
 
 #### 8) observer 내부에서 nights에 non-null 값을 얻을 때 마다 adpater의 data에 값을 할당한다. 
 
 ```
 sleepTrackerViewModel.nights.observe(viewLifecycleOwner, Observer {
    it?.let {
        adapter.data = it
    }
 })
 ```

 <br><br>
 
 ### Step 6: Explore how view holders are recycled
 RecyclerView는 view holder를 재활용한다. view과 화면에서 스크롤 될 때 RecyclerView는 화면으로 스크롤 되는 view를 위해 view를 재사용한다. 
 이러한 뷰 홀더가 재활용 되므로 onBindViewHolder()는 이전 item이 view holder에 설정했을 수 있는 커스텀 설정을 재설정 해야한다.
 예를 들어 수면 품질 등급이 1 이하이고 수면 상태가 좋지 않은 뷰 홀더에서는 텍스트 색상을 빨간색으로 설정할 수 있다
 
 #### 1) SleepNightAdapter 클래스에서 onBindViewHolder() 메소드에 다음 코드를 추가한다
 
 ```
 if (item.sleepQuality <= 1) {
    holder.textView.setTextColor(Color.RED) // red
 }
 ```
 
 <br>
 
 #### 2) 애블 실행시키고 낮은 수면 품질 데이터를 몇개 추가한다. 그러면 숫자가 빨간색으로 변한다. 
 
 #### 3) 다음은 높은 숫자가 빨간색으로 표시될 때 까지 높은 등급의 수면 품질을 추가한다.
 - RecyclerView는 view holder를 재사용하기 때문에 red view holder 중 하나가 높은 수면 등급의 rating 항목을 표시할 수도 있다. 결국 높은 등급이 빨간색으로 잘못 표시될 수도 있다.
 
 <image src="./images/recyclerview_err.png" width="50%" height="50%"/>
 
 <br>
 
 #### 4) 위의 문제를 고치기 위해 else 문에 color 값을 black으로 설정한다. 두 조건을 모두 명시하면 뷰 홀더는 각 항목에 올바른 텍스트 색상을 사용한다.
 ```
 if (item.sleepQuality <= 1) {
    holder.textView.setTextColor(Color.RED) // red
 } else {
    // reset
    holder.textView.setTextColor(Color.BLACK) // black
 }
 ```
 
 <br><br>
 
 ## 3. Create a ViewHolder for all the sleep data
  이번 단계에서는 이전에 만들었던 심플한 뷰 홀더를 더 많은 데이터를 표시할 수 있는 뷰홀더로 바꾼다. Util.kt에 추가했던 ViewHolder는 단순히 TextView를 wrap하고 있다.
  
  ```
  class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)
  ```
  
  그렇다면 RecyclerView는 왜 TextView를 직접 사용하지 않을까? 
  이 한줄의 코드는 많은 기능을 제공한다. ViewHolder는 item의 뷰와 RecyclerView에서의 위치에 대한 메타데이터를 설명한다.
  RecyclerView는 이 기능을 사용하여 스크롤 될 때 뷰를 올바른 위치에 배치하고 어댑터에서 항목을 추가하거나 제거할 때 애니메이션과 뷰와 같은 흥미로운 작업을 수행할 수 있다
  
  만약 RecyclerView가 ViewHolder에 저장된 뷰에 액세스 해야 하는 경우, 뷰 홀더의 itemView 속성을 사용하여 액세스 할 수 있다.
  RecyclerView는 스크린에 아이템을 바인딩하거나 테두리와 같이 뷰를 decoration 하거나 접근성을 구현할 때 itemView를 사용한다
  
  <br>
  
  ### Step 1: Create the item layout
  이번 단계에서는 하나의 item에 대한 layout file을 만든다. layout은 ConstraintLayout와 sleep quality를 나타내는 ItemView, 수면 시간을 나타내는 TextView 그리고 수면의 질을 텍스트로 표현한 TextView로 구성되어 있다.
  
  #### 1) list_item_sleep_night 라는 이름으로 layout resource file을 생성한다
  
  ```
  <?xml version="1.0" encoding="utf-8"?>
  <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
     xmlns:app="http://schemas.android.com/apk/res-auto"
     xmlns:tools="http://schemas.android.com/tools"
     android:layout_width="match_parent"
     android:layout_height="wrap_content">
  
     <ImageView
         android:id="@+id/quality_image"
         android:layout_width="@dimen/icon_size"
         android:layout_height="60dp"
         android:layout_marginStart="16dp"
         android:layout_marginTop="8dp"
         android:layout_marginBottom="8dp"
         app:layout_constraintBottom_toBottomOf="parent"
         app:layout_constraintStart_toStartOf="parent"
         app:layout_constraintTop_toTopOf="parent"
         tools:srcCompat="@drawable/ic_sleep_5" />
  
     <TextView
         android:id="@+id/sleep_length"
         android:layout_width="0dp"
         android:layout_height="20dp"
         android:layout_marginStart="8dp"
         android:layout_marginTop="8dp"
         android:layout_marginEnd="16dp"
         app:layout_constraintEnd_toEndOf="parent"
         app:layout_constraintStart_toEndOf="@+id/quality_image"
         app:layout_constraintTop_toTopOf="@+id/quality_image"
         tools:text="Wednesday" />
  
     <TextView
         android:id="@+id/quality_string"
         android:layout_width="0dp"
         android:layout_height="20dp"
         android:layout_marginTop="8dp"
         app:layout_constraintEnd_toEndOf="@+id/sleep_length"
         app:layout_constraintHorizontal_bias="0.0"
         app:layout_constraintStart_toStartOf="@+id/sleep_length"
         app:layout_constraintTop_toBottomOf="@+id/sleep_length"
         tools:text="Excellent!!!" />
  </androidx.constraintlayout.widget.ConstraintLayout>
  ```
  
  <br>
  
  ### Step 2: Create ViewHolder
  
  #### 1) SleepNightAdapter.kt를 열어서 SleepNightAdapter 안에 RecyclerView.ViewHolder를 상속한 ViewHolder 클래스를 생성한다
  
  ```
  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){}
  ```
  
  <br>
  
  #### 2) ViewHolder 내부에서 뷰에 대한 참조를 가져온다. 이 ViewHolder를 바인딩 할 때마다 image와 두개의 text view에 액세스 해야한다. (나중에 이 코드는 데이터 바인딩을 사용하여 변경할 수 있다)
  
  ```
  val sleepLength: TextView = itemView.findViewById(R.id.sleep_length)
  val quality: TextView = itemView.findViewById(R.id.quality_string)
  val qualityImage: ImageView = itemView.findViewById(R.id.quality_image)
  ```
  
  <br>
   
  ### Step 3: Use the ViewHolder in SleepNightAdapter
  
  #### 1) SleepNightAdapter 정의에서 기존 TextItemViewHolder 대신에 SleepNightAdapter.ViewHolder를 사용하도록 변경한다.
  
  ```
  class SleepNightAdapter: RecyclerView.Adapter<SleepNightAdapter.ViewHolder>() {
  ```
 
  <br>
  
  #### 2) onCreateViewHolder()를 업데이트한다
  
   ##### (1) onCreateViewHOlder()의 선언에서 리턴 타입을 ViewHolder로 변경한다.
   
   ##### (2)  layout inflator가 사용하고 있는 layout resource를 list_item_sleep_night으로 변경한다.
   
   ##### (3)  TextView로 캐스트 한 것을 지운다
   
   ##### (4)  TextItemViewHolder를 리턴하는 것 대신에 ViewHolder를 리턴한다
   
   ```
       override fun onCreateViewHolder(
               parent: ViewGroup, viewType: Int): ViewHolder {
           val layoutInflater = 
               LayoutInflater.from(parent.context)
           val view = layoutInflater
                   .inflate(R.layout.list_item_sleep_night, 
                            parent, false)
           return ViewHolder(view)
       }
   ```
   
  <br>
  
  #### 3) onBindViewHolder()를 업데이트한다
  
   ##### (1) onBindViewHolder()에서 holder 파라미터가 TextItemViewHolder 대신 ViewHolder가 되도록 변경한다
   
   ##### (2) onBindViewHolder() 코드에서 item 정의를 제외한 모든 코드를 지운다
   
   ##### (3) val res를 정의하고 이 뷰 resource의 참조값을 가지는 변수를 정의한다
   
   ```
   val res = holder.itemView.context.resources
   ```
   
   ##### (4) sleepLength text view의 텍스트를 설정한다
   
   ```
   holder.sleepLength.text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
   ```
   
   ##### (5) 위의 코드를 붙여 넣으면 에러가 발생하는데 이는 convertDurationToFormatted()가 정의되어 있지 않기 때문이다. Util.kt를 열어서 주석을 해제한다.
   
   ##### (6) onBindViewHolder()로 돌아와서 quality를 정의하기 위해 convertNumericQualityToString()를 사용한다
   
   ```
   holder.quality.text= convertNumericQualityToString(item.sleepQuality, res)

   ```
   
   ##### (7) quality에 대한 올바른 이미지를 설정한다.
   
   ```
   holder.qualityImage.setImageResource(when (item.sleepQuality) {
      0 -> R.drawable.ic_sleep_0
      1 -> R.drawable.ic_sleep_1
      2 -> R.drawable.ic_sleep_2
      3 -> R.drawable.ic_sleep_3
      4 -> R.drawable.ic_sleep_4
      5 -> R.drawable.ic_sleep_5
      else -> R.drawable.ic_sleep_active
   })
   ```
  
  <br>
  
 #### 4) BindViewHolder()를 모두 업데이트 한 코드는 아래와 같으며, ViewHolder 내의 모든 데이터를 설정한다.
 
 ```
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         val item = data[position]
         val res = holder.itemView.context.resources
         holder.sleepLength.text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
         holder.quality.text= convertNumericQualityToString(item.sleepQuality, res)
         holder.qualityImage.setImageResource(when (item.sleepQuality) {
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
 
 <br>
 
 #### 5) 앱을 실행시키면 sleep-quality 아이콘과 수면 시간 수면 품질 등의 텍스트가 보여지는 것을 확인할 수 있다.
 
 <br><br>
  
 ## 4. Improve your code
 지금 코드는 화면에 표시하기 위한 코드와 뷰 홀더를 관리하기 위한 코드가 혼재되어 있다. 그리고 onBindViewHolder()는 ViewHolder를 어떻게 업데이트 할지 세부 사항까지 알고 있다
 
 상용 앱에서는 아마 다수의 view holder가 존재할 것이며, 더 복잡한 어댑터가 있을 수 있다. 
 
 뷰 홀더와 관련된 것들만 뷰 홀더에 있도록 코드를 변경해보자
 
 <br>
 
 ### Step 1: Refactor onBindViewHolder()
 이 단계에서는 코드를 리팩토링 하고 모든 뷰 홀더 기능을 ViewHolder로 옮긴다. 이 리팩토링의 목적은 사용자에게 앱이 표시되는 방식을 변경하는 것이 아니라 개발자가 코드를 보다 쉽고 안전하게 작업할 수 있도록 해준다. 다행히 Android Studio에는 도움이 되는 도구가 있다.
 
 #### 1) SleepNightAdapter의 onBindViewHolder()에서 item 변수 선언문을 제외한 모든 항목을 선택한다.
 
 #### 2) 오른쪽 버튼을 클릭하고 Refactor > Extract > Function을 선택한다
 
 #### 3) 함수의 이름은 bind로 설정하고 OK를 클릭한다
  - bind() 함수가 onBindViewHolder() 밑에 위치한다
  
  ```
    private fun bind(holder: ViewHolder, item: SleepNight) {
        val res = holder.itemView.context.resources
        holder.sleepLength.text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
        holder.quality.text = convertNumericQualityToString(item.sleepQuality, res)
        holder.qualityImage.setImageResource(when (item.sleepQuality) {
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
  
  <br>
  
  #### 4) bind()의 holder 파라미터의 holder 단어에 커서를 두고 Alt + Enter (Option + Enter on a mac)을 눌러 intention menu를 연다. **Convert parameter to receiver**를 선택하여 아래와 같이 확장 함수로 만든다.
  
  ```
  private fun ViewHolder.bind(item: SleepNight) {...}
  ```
  
  <br>
  
  #### 5) bind() 함수를 잘라내서 ViewHolder로 이동시킨다.
  
  #### 6) bind()를 public으로 만든다
  
  #### 7) 이제 ViewHolder 클래스 안에서는 \'ViewHolder.\'을 지울 수 있다. ViewHolder 클래스의 최종 bind 함수는 아래와 같다
  ```
  fun bind(item: SleepNight) {
     val res = itemView.context.resources
     sleepLength.text = convertDurationToFormatted(
             item.startTimeMilli, item.endTimeMilli, res)
     quality.text = convertNumericQualityToString(
             item.sleepQuality, res)
     qualityImage.setImageResource(when (item.sleepQuality) {
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
  
 ### Step 2: Refactor onCreateViewHolder
 
 어댑터의 onCreateViewHolder() 메소드는 ViewHolder의 layout resource의 view를 inflate 시킨다. 그러나 인플레이션은 어댑터와 관련이 없고 ViewHolder와 관련이 있다. 인플레이션은 ViewHolder에서 일어나야한다
 
 #### 1) onCreateViewHolder()의 body에 있는 모든 코드를 선택한다
 
 #### 2) 오른쪽 클릭하여 Refactor > Extract > Function을 선택한다
 
 #### 3) 함수의 이름은 from으로 하고 Ok를 클릭한다
 
 #### 4) 커서를 from 함수의 이름에 두고 Alt + Enter (Option + Enter on a Mac)을 눌러서 intention menu를 연다
 
 #### 5) **Move to companion object**를 선택한다. from()은 ViewHolder의 인스턴스가 아니라 ViewHolder의 클래스에서 호출되어야 하므로 companion object이어야 한다.
 
 #### 6) companion object를 ViewHolder 클래스로 옮긴다.
 
 #### 7) from()을 public으로 변경한다
 
 #### 8) onCreateViewHolder()에서 리턴문을 return ViewHolder.from(parent)으로 변경한다
 
 완성된 onCreateViewHolder()와 from() 메소드는 아래와 같다.
 
 ```
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         return ViewHolder.from(parent)
     }
     
     
     companion object {
        fun from(parent: ViewGroup): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                    .inflate(R.layout.list_item_sleep_night, parent, false)
            return ViewHolder(view)
        }
     }
     
 ```
 
 #### 9) ViewHolder의 constructor를 private으로 선언한다. 왜냐하면 이제 from()이 새로운 ViewHolder 인스턴스를 리턴하는 메소드이므로 더이상 ViewHolder의 생성자를 호출할 필요가 없다
 
 ```
 class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
 ```
 
 