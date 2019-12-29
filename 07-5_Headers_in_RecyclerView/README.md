# 07-5 Headers in RecyclerView

## 1. Add a header to your RecyclerView

### Step 1: Create a DataItem class
item의 타입을 추상화하고 어댑터가 item을 처리하게 하려면 SleepNight 또는 Header를 나타내는 data holder class를 작성할 수 있다. 그러면 dataset은 데이터 홀더 항목의 목록이 된다.

#### 1) SleepNightAdapter.kt를 열고 SleepNightListener 클래스 안에 데이터의 항목을 나타내는 DataItem이라는 sealed class를 정의한다
 - sealed class는 닫힌 유형을 정의한다. 즉 dataItem의 서브 클래스는 이 파일에 정의 되어야 한다
 
 ```
 sealed class DataItem {
 
 }
 ```
 
#### 2) DataItem 클래스 내부에 두가지 다른 data item의 타입을 나타내는 클래스를 정의한다. 첫번째는 SleepNightItem이며 이는 SleepNight를 감싸는 래퍼이므로 sleepNight이라는 단일값을 가진다. sealed class의 일부로 만드려면 DataItem을 extend 한다

```
data class SleepNightItem(val sleepNight: SleepNight): DataItem()
```

#### 3) 두번째 클래스는 Header이며 헤더를 나타낸다. 헤더에는 실제 데이터가 없으므로 object로 선언할 수 있다. 이것은 하나의 헤더 인스턴스만 존재한다는 것을 의미한다.

```
object Header: DataItem()
```

#### 4) DataItem 내부의 클래스 레벨에서 id라는 이름의 abstract Long 프로퍼티를 정의한다. 어댑터가 DiffUtil을 사용하여 항목이 변경되었는지 여부와 방법을 판단할 때 DiffItemCallback은 각 항목의 id를 알아야 한다. 
 - id 변수를 추가하면 에러를 볼 수 있는데, SleepNightItem 및 Header가 추상 속성 ID를 재정의 해야 하기 때문이다.
 
```
abstract val id: Long
```

#### 5) Header에서 id 프로퍼티를 override 하여 Long.MIN_VALUE를 할당한다. 

```
override val id = Long.MIN_VALUE
```

#### 6) 완성된 코드는 아래와 같다

```
sealed class DataItem {
    abstract val id: Long
    data class SleepNightItem(val sleepNight: SleepNight): DataItem()      {
        override val id = sleepNight.nightId
    }

    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }
}
```

<br>

### Step 2: Create a ViewHolder for the Header

#### 1) TextView를 표시하는 header.xml이라는 새 레이아웃 리소스 파일에 헤더의 레이아웃을 만든다

```
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/text"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?android:attr/textAppearanceLarge"
    android:text="Sleep Results"
    android:padding="8dp" />
```

#### 2) string.xml에 아래와 같이 header_text를 추가한다

```
<string name="header_text">Sleep Results</string>
```

#### 3) SleepNightAdapter.kt 파일을 열어서 SleepNightAdapter 내부의 ViewHolder 위에 TextViewHolder 클래스를 만든다. 이 클래스는 textview.xml을 inflate하고 TextViewHolder 인스턴스를 반환한다.

```
class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
    companion object {
        fun from(parent: ViewGroup): TextViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.header, parent, false)
            return TextViewHolder(view)
        }
    }
}
```

<br>

<br>

### Step 3: Update SleepNightAdapter
다음으로 SleepNight 선언을 업데이트 해야 한다. 한가지 유형의 ViewHolder만 지원하는 대신 모든 타입의 ViewHolder를 사용할 수 있어야 한다. 

### Define the types of items

#### 1) SleepNightAdapter.kt를 열어서 import 문 아래, SleepNightAdapter 클래스 위에 두가지 상수 타입을 정의한다. RecyclerView는 각 item의 view type을 구별하여 올바른 ViewHolder를 할당할 수 있어야 한다

```
private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1
```

#### 2) SleepNightAdapter 내부에 getItemViewType()를 오버라이드 하고 현재 item의 유형에 따라 헤더 또는 item 상수를 리턴하는 함수를 만든다

```
override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
        is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
        is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
    }
}
```

<br>

### Update the SleepNightAdapter definition

#### 1) SleepNightAdapter 선언에서 ListAdatper의 첫번째 인자를 SleepNight에서 DataItem으로 변경한다

#### 2) SleepNightAdapter 선언에서 ListAdapter의 두번째 generic 인자를 SleepNightAdapter.ViewHolder에서 RecyclerView.ViewHolder로 바꾼다. 

```
class SleepNightAdapter(val clickListener: SleepNightListener):
       ListAdapter<DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()) {
```

<br>

### Update onCreateViewHolder()

#### 1) onCreateViewHolder()의 선언을 변경해서 RecyclerView.ViewHolder를 리턴한다.

```
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
```

#### 2) onCreateViewHolder() 메소드의 구현을 확장하여 각 항목 유형에 적합한 뷰 홀더를 리턴한다

```
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
        ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
        ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
        else -> throw ClassCastException("Unknown viewType ${viewType}")
    }
}
```

<br>

### Update onBindViewHolder()

#### 1) ViewHolder의 onBindViewHolder() 파라미터를 ViewHolder에서 RecyclerView.ViewHolder로 변경한다. 

```
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
```

#### 2) holder가 ViewHolder인 경우에만 뷰 홀더에 데이터를 할당하는 조건을 추가한다

```
when (holder) {
    is ViewHolder -> {...}
```

#### 3) getItem ()에 의해 리턴된 오브젝트 유형을 DataItem.SleepNightItem으로 캐스트한다. 완성된 onBindViewHolder() 함수는 다음과 같다.

```
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
        is ViewHolder -> {
            val nightItem = getItem(position) as DataItem.SleepNightItem
            holder.bind(nightItem.sleepNight, clickListener)
        }
    }
}
```

### Update the diffUtil callbacks

#### 1) SleepNightDiffCallback의 메소드를 변경하여 SleepNight 대신 새 DataItem 클래스를 사용하도록 변경한다 

```
class SleepNightDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}
```

<br>

### Add and submit the header

#### 1) SleepNightAdapter 내부에서 ListAdapter의 submitList() 함수를 대체하여 사용 할 addHeaderAndSubmitList() 함수를 정의한다. 이 함수는 sleepNight 목록을 가져와서 리스트를 전달하는 대신 헤더를 추가한 후 리스트를 리턴한다 

```
fun addHeaderAndSubmitList(list: List<SleepNight>?) {}
```

#### 2) addHeaderAndSubmitList()에서 전달된 목록이 null인 경우 header만 리턴하고 그렇지 않은 경우 리스트의 head에 header를 붙여 list를 submit 한다

```
val items = when (list) {
    null -> listOf(DataItem.Header)
    else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
}
submitList(items)
```

#### 3) SleepTrackerFragment.kt를 열어서 submitList()를 addHeaderAndSubmitList()로 바꾼다

<br><br>

## 2. Use coroutines for list manipulations
이 앱을 위해 수정해야 할 두가지가 있다

   - header는 왼쪽 상단에 표시되며 쉽게 구분할 수 없다
   - 하나의 header가 있는 짧은 리스트에는 문제 되지 않지만 UI 쓰레드의 addHeaderAndSubmitList()에서 목록을 조작해서는 안된다.
   - 항목을 추가해야 할 위치를 결정하기 위해 수백개의 item, 여러 헤더 및 로직이 있다고 생각해보자. 이 작업은 코루틴에 속한다

코루틴을 사용하도록 addHeaderAndSubmitList()을 수정한다

<br>

#### 1) SleepNightAdapter 클래스 내부에서 상단에서 Dispatchers.Default를 사용하여 CoroutineScope를 정의한다.

```
private val adapterScope = CoroutineScope(Dispatchers.Default)
```

#### 2) addHeaderAndSubmitList() 안에서 adapterScope로 코루틴을 시작하여 목록을 조작한다. 그런 다음 Dispatchers.Main로 전환하여 list를 submit한다

```
fun addHeaderAndSubmitList(list: List<SleepNight>?) {
    adapterScope.launch {
        val items = when (list) {
            null -> listOf(DataItem.Header)
            else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
        }
        withContext(Dispatchers.Main) {
            submitList(items)
        }
    }
}
```
   
<br><br>

## 3. Extend the header to span across the screen
현재 header는 그리드의 다른 item과 너비가 같으며 하나의 가로 및 세로 span을 차지한다. 전체 그리드는 하나의 span 너비의 세개의 항목을 가로로 맞추므로 머리글은 세개의 span을 가로로 사용해야 한다.
header 너비를 고치려면 전체 column에 데이터를 채울 때 GridLayoutManager에 알려야 한다.
GridLayoutManager에서 SpanSizeLookup을 설정하여 이 작업을 수행할 수 있다. 이것은 GridLayoutManager가 목록의 각 항목에 사용할 스팬 수를 결정하기 위해 사용하는 configuration object이다.

#### 1) SleepTrackerFragment.kt를 연다

#### 2) onCreateView()의 끝에서 manager 변수를 정의하는 코드를 찾는다

#### 3) manager 아래에 manager.spanSizeLookup를 정의한다. setSpanSizeLookup은 람다를 사용하지 않기 때문에 객체를 만들어야 한다. 이 경우 GridLayoutManager.SpanSizeLookup에 object : classname으로 선언하여 오브젝트를 만든다

```
manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
}
``` 

#### 4) 컴파일 오류를 고치기 위해 getSpanSize()를 오버라이드 한다.

```
manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int) =  when (position) {
        0 -> 3
        else -> 1
    }
}
```

#### 5) header 모양을 개선하려면 header.xml을 열고 아래 코드를 header.xml 레이아웃에 추가한다

```
android:textColor="@color/white_text_color"
android:layout_marginStart="16dp"
android:layout_marginTop="16dp"
android:layout_marginEnd="16dp"
android:background="@color/colorAccent"
```