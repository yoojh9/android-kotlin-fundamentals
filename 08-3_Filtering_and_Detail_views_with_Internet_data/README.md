# 08-3 Filtering and Detail views with Internet data

## 1. Add "for sale" images to the overview
이번 단계에서는 판매중인 화성의 땅의 이미지는 달러 기호 아이콘을 추가하도록 한다


### Step 1: Update MarsProperty to include the type
이전 단계에서는 Moshi 라이브러리를 사용하여 Mars 웹 서비스의 raw json 응답을 각각의 MarsProperty 데이터 오브젝트로 parse 했다
이번 단계에서는 MarsProperty 클래스에 자산이 rent를 위한 것인지 아닌지(type은 string 형식으로 "rent"와 "buy" 값을 가질 수 있다) 알려주는 로직을 추가한다. 

#### 1) network/MarsProperty.kt를 열고 MarsProperty의 body를 추가한 후, isRental의 custom getter를 만들고 type이 "rent"일 때 true를 리턴시킨다

```
data class MarsProperty(
       val id: String,
       @Json(name = "img_src") val imgSrcUrl: String,
       val type: String,
       val price: Double)  {
   val isRental
       get() = type == "rent"
}
```

### Step 2: Update the grid item layout
이제 이미지 그리드 항목 레이아웃을 업데이트 하여 판매중인 자산 이미지에만 달러 기호 drawable이 표시되도록 수정한다
grid item의 레이아웃 항목에서 데이터 바인딩 표현식을 사용하여 작업을 수행한다

#### 1) res/layout/grid_view_item.xml 을 열고 \<data\> 요소 내부에 View 클래스를 \<import\> 한다. 
레이아웃 파일에서 데이터 바인딩 표현식을 사용하여 클래스의 구성요소를 import 할 수 있다. 이 경우 View.GONE 및 View.VISIBLE 상수를 사용하므로 View 클래스에 액세스 해야한다.

```
<import type="android.view.View"/>
```

#### 2) 전체 ImageView를 FrameLayout으로 감싸고 FrameLayout 내부 상단에 달러 기호 drawable 리소스를 추가한다

```
<FrameLayout
   android:layout_width="match_parent"
   android:layout_height="170dp">
             <ImageView 
                    android:id="@+id/mars_image"
            ...
</FrameLayout>
```

#### 3) ImageView에서 android:layout_height 속성을 match_parent로 변경하고 새로운 parent인 FrameLayout에 채운다

```
android:layout_height="match_parent"
```

#### 4) 두번째 \<ImageView\>를 추가한다. 이 이미지는 그리드 아이템 항목의 오른쪽 아래 모서리에 표시되며 달러 기호 아이콘으로 res\/drawable\/ic_for_sale_outline.xml에 정의된 drawable을 사용한다

```
<ImageView
   android:id="@+id/mars_property_type"
   android:layout_width="wrap_content"
   android:layout_height="45dp"
   android:layout_gravity="bottom|end"
   android:adjustViewBounds="true"
   android:padding="5dp"
   android:scaleType="fitCenter"
   android:src="@drawable/ic_for_sale_outline"
   tools:src="@drawable/ic_for_sale_outline"/>
```

#### 5) mars_property_type ImageView에 android:visibility 속성을 추가한다. binding 표현식을 사용하여 View.GONE(for a rental) 또는 View.VISIBLE(for a purchase) 값을 visibility에 할당할 수 있다

```
 android:visibility="@{property.rental ? View.GONE : View.VISIBLE}"
```

#### 6) 완성된 grid_view_item.xml 파일은 아래와 같다

```
<layout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:app="http://schemas.android.com/apk/res-auto"
       xmlns:tools="http://schemas.android.com/tools">
   <data>
       <import type="android.view.View"/>
       <variable
           name="property"
           type="com.example.android.marsrealestate.network.MarsProperty" />
   </data>
   <FrameLayout
       android:layout_width="match_parent"
       android:layout_height="170dp">

       <ImageView
           android:id="@+id/mars_image"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:scaleType="centerCrop"
           android:adjustViewBounds="true"
           android:padding="2dp"
           app:imageUrl="@{property.imgSrcUrl}"
           tools:src="@tools:sample/backgrounds/scenic"/>

       <ImageView
           android:id="@+id/mars_property_type"
           android:layout_width="wrap_content"
           android:layout_height="45dp"
           android:layout_gravity="bottom|end"
           android:adjustViewBounds="true"
           android:padding="5dp"
           android:scaleType="fitCenter"
           android:src="@drawable/ic_for_sale_outline"
           android:visibility="@{property.rental ? View.GONE : View.VISIBLE}"
           tools:src="@drawable/ic_for_sale_outline"/>
   </FrameLayout>
</layout>
```


#### 7) 앱을 실행시켜서 달러 아이콘 이미지가 나오는지 확인한다

<br><br>

## 2. Filter the results

이번 단계에서는 프래그먼트에 옵션 메뉴를 추가하여 사용자가 rental, for-sale, 또는 all 정렬 조건으로 아이템을 볼 수 있도록 만든다.

Mars 웹서비스에는 api에 query parameter 또는 option(filter)으로 rental 타입이나 buy 타입의 항목만 가져올 수 있다. 

```
https://android-kotlin-fun-mars-server.appspot.com/realestate?filter=buy
```

MarsApiService를 수정하여 retrofit을 사용하여 request에 query 옵션을 추가한다. 


### Step 1: Update the Mars API service

#### 1) network/MarsApiService.kt를 열고 MarsApiFilter 라는 이름의 enum 클래스를 추가한다.

```
enum class MarsApiFilter(val value: String) {
   SHOW_RENT("rent"),
   SHOW_BUY("buy"),
   SHOW_ALL("all") }
```

#### 2) getProperties()를 수정하여 filter를 이름으로 하는 string query parameter를 추가한다. 
@Query 어노테이션은 getProperties() 메소드에 웹 서비스 request에 filter option을 추가하라고 알려준다.
getProperties()가 호출될 때마다 request url에 ?filter=type를 추가하여 response를 전달받는다.

```
fun getProperties(@Query("filter") type: String):  
```

<br>

### Step 2: Update the overview view model

OverviewModel의 getMarsRealEstateProperties() 메소드에서 MarsApiService의 getProperties()를 호출하므로 호출 코드에도 filter 인자를 추가해아 한다


#### 1) overview/OverviewViewModel.kt 열면 이 전 단계의 수정 사항으로 인해 에러를 확인할 수 있다. MarsApiFilter (이 전 단계에서 만들었던 enum class) getMarsRealEstateProperties() 메소드의 인자로 사용한다

```
private fun getMarsRealEstateProperties(filter: MarsApiFilter) {
```

#### 2) Retrofit Service에서 getProperties() 호출 부분을 수정하여 filter query를 String으로 전달한다

```
var getPropertiesDeferred = MarsApi.retrofitService.getProperties(filter.value)
```

#### 3) init 블럭에서 앱이 처음에 로드 될 때 모든 type의 화성 자산이 보이도록 getMarsRealEstateProperties()에 인자의 초기값으로 MarsApiFilter.SHOW_ALL을 전달한다

```
init {
   getMarsRealEstateProperties(MarsApiFilter.SHOW_ALL)
}
```

#### 4) 클래스 마지막에 MarsApiFilter 인자로 getMarsRealEstateProperties()를 호출하는 updateFilter() 메소드를 추가한다

```
fun updateFilter(filter: MarsApiFilter) {
   getMarsRealEstateProperties(filter)
}
```

<br>

### Step 3: Connect the fragment to the options menu
마지막 단계는 overflow 메뉴를 프래그먼트에 연결하여 사용자가 옵션 메뉴를 선택할 때 viewModel의 updateFilter()를 호출하도록 만든다

#### 1) res/menu/overflow_menu.xml를 열면 아래와 같이 옵션 메뉴가 만들어져 있다

```
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/show_all_menu"
        android:title="@string/show_all" />
    <item
        android:id="@+id/show_rent_menu"
        android:title="@string/show_rent" />
    <item
        android:id="@+id/show_buy_menu"
        android:title="@string/show_buy" />
</menu>
```

#### 2) overview/OverviewFragment.kt를 열고 클래스 마지막에 menu 아이템 선택을 처리하는 onOptionsItemSelected() 메소드를 구현한다

```
override fun onOptionsItemSelected(item: MenuItem): Boolean {
} 
```

#### 3) onOptionsItemSelected()에서 적절한 filter 값으로 view model에 있는 updateFilter() 메소드를 호출한다. 
when{} 블럭을 사용하여 메뉴에 맞게 MarsApiFilter 값을 인자로 전달하고, 마지막에 메뉴 항목을 처리했으므로 true를 리턴한다. 

```
override fun onOptionsItemSelected(item: MenuItem): Boolean {
   viewModel.updateFilter(
           when (item.itemId) {
               R.id.show_rent_menu -> MarsApiFilter.SHOW_RENT
               R.id.show_buy_menu -> MarsApiFilter.SHOW_BUY
               else -> MarsApiFilter.SHOW_ALL
           }
   )
   return true
}
```

#### 4) 앱을 실행시키고 여러 옵션 메뉴들을 선택해본다

<br><br>

## 3. Create a detail page and set up navigation
이번 단계에서는 detail fragment를 생성하고 특정 property에 대해 상세 정보를 표시한다. detail fragment는 rental 또는 for-sale 타입에 관계 없이 large image와 가격을 보여준다.
detail fragment는 사용자가 overview의 grid image를 탭할 때 실행된다. 해당 기능을 만드려면 RecyclerView grid item에 onClick 리스너를 추가해야 하고 새로운 fragment로 navigate 시켜야 한다
viewModel의 LiveData의 변경을 trigger하여 navigate 할 수 있다. 또한 Navigation의 구성 요소인 safe args 플러그인을 사용하여 선택된 MarsProperty의 정보를 overview에서 detail fragment로 전달할 수 있다


### Step 1: Create the detail view model and update detail layout
이전에 overview의 viewModel과 fragment에 했던 작업처럼 detail fragment의 layout 파일과 view model을 구현해야 한다

#### 1) detail/DetailViewModel.kt를 열어서 DetailViewModel의 생성자 파라미터로 MarsProperty 객체를 받는 것을 확인한다

```
class DetailViewModel( marsProperty: MarsProperty,
                     app: Application) : AndroidViewModel(app) {
}
```

#### 2) 클래스 정의 내에 detail 뷰에 상세 정보를 표시하기 위해 overview에서 선택된 MarsProperty의 LiveData를 추가한다.
MutableLiveData를 작성하는 일반적인 패턴에 따라 MarsProperty 자체의 값은 MutableLiveData로 가지고 있고, immutable public LiveData를 expose 시킨다

```
private val _selectedProperty = MutableLiveData<MarsProperty>()
val selectedProperty: LiveData<MarsProperty>
   get() = _selectedProperty
```

#### 3) init{}에서 생성자로부터 얻은 MarsProperty 객체를 _selectedProperty의 값으로 할당한다

```
    init {
        _selectedProperty.value = marsProperty
    }
```

#### 4) res/layout/fragment_detail.xml을 열어서 design 탭을 누른다. 
이 레이아웃 파일을 DetailFragment의 레이아웃 파일이며 large photo를 위한 ImageView와 property type(rental, buy)을 나타내는 TextView, 그리고 price를 위한 TextView가 있다.
ConstraintLayout은 ScrollView로 Wrapping되어 있으므로 화면에 표시하기 너무 커지면 자동으로 스크롤이 생성된다. (ex. 사용자가 가로로 볼 때)

#### 5) Text 탭으로 돌아가서 layout 상단 <ScrollView> 요소 바로 앞에 <data> 태그를 추가하여 layout과 detail view model을 연결시킨다

```
<data>
   <variable
       name="viewModel"
       type="com.example.android.marsrealestate.detail.DetailViewModel" />
</data>
```

#### 6) ImageView 요소에 app:imageUrl 속성을 추가하고 viewModel의 selected property에 있는 imgSrcUrl를 할당한다
어댑터가 app:imageUrl 속성을 사용하는지 계속 watch하고 있으므로 Glide를 사용하여 이미지를 로드하는 바인딩 어댑터도 여기에서 자동으로 사용된다

```
app:imageUrl="@{viewModel.selectedProperty.imgSrcUrl}"
```

<br>

### Step 2: Define navigation in the overview view model
사용자가 overview에서 사진을 탭하면 클릭한 항목에 대한 세부 정보를 나타내는 fragment로 이동해야 한다

#### 1) overview/OverviewViewModel.kt를 열어서 MutableLiveData 타입으로 _navigateToSelectedProperty 프로퍼티를 만들고 immutable LiveData 타입으로 expose 시킨다
LiveData가 non-null로 바뀌면 navigation이 trigger 된다

```
private val _navigateToSelectedProperty = MutableLiveData<MarsProperty>()
val navigateToSelectedProperty: LiveData<MarsProperty>
   get() = _navigateToSelectedProperty
``` 

#### 2) 클래스 끝에 displayPropertyDetails()를 추가하여 _navigateToSelectedProperty.value에 선택된 MarsProperty 객체를 할당한다

```
fun displayPropertyDetails(marsProperty: MarsProperty) {
   _navigateToSelectedProperty.value = marsProperty
}
```

#### 3) displayPropertyDetailsComplete() 메소드를 추가하여 _navigateToSelectedProperty에 null을 할당한다. navigation이 이미 완료됐고, navigation을 다시 trigger 하지 않기 위해 이 작업이 필요하다

```
fun displayPropertyDetailsComplete() {
   _navigateToSelectedProperty.value = null
}
``` 

<br>

### Step 3: Set up the click listeners in the grid adapter and fragment

#### 1) overview/PhotoGridAdapter.kt 파일을 열고 클래스 하단에 OnClickListener를 이름으로 하는 커스텀 클래스를 만든다. OnClickListner는 marsProperty를 파라미터로 갖는 람다식을 생성자 파라미터로 가지고 있다. 클래스 내부에서 onClick() 함수를 정의하여 람다식 파라미터를 값으로 전달한다

```
class OnClickListener(val clickListener: (marsProperty:MarsProperty) -> Unit) {
     fun onClick(marsProperty:MarsProperty) = clickListener(marsProperty)
}
```    

#### 2) PhotoGridAdapter에서 스크롤을 올려 클래스의 생성자에 private 프로퍼티로 OnClickListener를 추가한다 

```
class PhotoGridAdapter( private val onClickListener: OnClickListener ) :
       ListAdapter<MarsProperty,              
           PhotoGridAdapter.MarsPropertyViewHolder>(DiffCallback) {
```

#### 3) onBindviewHolder() 메소드의 그리드 item에 onClickListener를 추가하여 사진을 클릭 가능하게 만든다. getItem() 및 bind() 호출 사이에 클릭 리스너를 정의한다.

```
override fun onBindViewHolder(holder: MarsPropertyViewHolder, position: Int) {
   val marsProperty = getItem(position)
   holder.itemView.setOnClickListener {
       onClickListener.onClick(marsProperty)
   }
   holder.bind(marsProperty)
}
```

#### 4) overview/OverviewFragment.kt를 열고 onCreateView()에 binding.photosGrid.adapter의 프로퍼티를 초기화 하는 라인을 아래와 같이 변경한다
이 코드는 PhotoGridAdatper 생성자에 PhotoGridAdapter.onClickListener 객체를 추가하고 전달된 MarsProperty 객체와 함께 viewModel.displayPropertyDetails()를 호출한다.
이것은 view model의 navigation을 위한 LiveData를 트리거한다.

```
binding.photosGrid.adapter = PhotoGridAdapter(PhotoGridAdapter.OnClickListener {
   viewModel.displayPropertyDetails(it)
})
```

<br>

### Step 4: Modify the navigation graph and make MarsProperty parcelable
현재 PhotoGridAdapter의 클릭 리스터가 탭을 처리하고 view model로부터 navigation을 트리거하는 방법을 가지고 있다. 그러나 아직 DetailFragment에 MarsProperty 객체는 전달하지 않고 있다
이를 위해 navigation 컴포넌트에서 safe args를 사용해야 한다

#### 1) res/navigation/nav_graph.xml을 열어서 Text 탭을 눌러 navigation graph 코드를 살펴본다

#### 2) detail_fragment의 \<fragment\> 요소에 아래와 같이 \<argument\> 요소를 추가한다

```
<argument
   android:name="selectedProperty"
   app:argType="com.example.android.marsrealestate.network.MarsProperty"
   />

```

#### 3) 컴파일 하면 아래와 같은 에러가 발생한다
```
Caused by: java.lang.IllegalArgumentException: com.example.android.marsrealestate.network.MarsProperty is not Serializable or Parcelable.
```

#### 4) 위의 에러는 MarsProperty가 Parcelable이 아니기 때문에 발생한다. Parcelable 인터페이스는 객체를 serialized하게 만들어준다. 그래서 객체의 데이터가 fragment나 activity간에 전달되게 해준다.
이 예제에서는 MarsProperty 객체를 detail fragment로 Safe Args를 통해서 전달하는데, MarsProperty는 반드시 Parcelable 인터페이스를 구현해야 한다.
좋은 소식은 코틀린이 이 인터페이스를 구현하는데 손쉬운 방법을 제공한다는 것이다.

#### 5) network/MarsProperty.kt를 열어서 @Parcelize 어노테이션을 클래스 선언 위에 추가한다.
@Parcelize 어노테이션은 kotlin android extension을 사용하여 이 클래스에서 Parcelable 인터페이스의 메소드를 자동으로 구현한다.

```
@Parcelize
data class MarsProperty (
```

#### 6) MarsProperty가 Parcelable을 상속받도록 변경한다. 
```
@Parcelize
data class MarsProperty (
       val id: String,
       @Json(name = "img_src") val imgSrcUrl: String,
       val type: String,
       val price: Double) : Parcelable {
```

<br>

### Step 5: Connect the fragments

#### 1) overview/OverviewFragment.kt를 열고 onCreateView() 내에서 photo grid adapter를 초기화 하는 라인 아래에 아래와 같이 overview view model의 navigatedToSelectedProperty를 observe 하는 코드를 추가한다
observer는 MarsProperty(lambda에서는 it)이 null이 아닌 경우에만 findNavController()로 navigation을 제어한다. 
또한 displayPropertyDetailsComplete()를 호출하여 view model의 LiveData를 null로 설정한다. 이는 OverviewFragment로 다시 돌아올 때 실수로 navigation을 다시 trigger 하지 않도록 해준다
```
viewModel.navigateToSelectedProperty.observe(this, Observer {
   if ( null != it ) {   
      this.findNavController().navigate(
              OverviewFragmentDirections.actionShowDetail(it))             
      viewModel.displayPropertyDetailsComplete()
   }
})
```

#### 2) detail/DetailFragment.kt를 열고 onCreateView() 내부의 setLifecycleOwner() 아래에 다음 코드를 넣는다.
이 코드는 Safe Args에서 선택된 MarsProperty 객체를 가져온다. 코틀린의 not-null 연산자 '!!'를 사용한다. 하지만 selectProperty가 없을 경우 null pointer exception이 발생하므로 production 환경에서는 어떤식으로든 에러를 처리해야 한다.

```
val marsProperty = DetailFragmentArgs.fromBundle(arguments!!).selectedProperty
```

#### 3) 다음 라인에 DetailViewModel의 인스턴스를 얻기 위해 DetailViewModelFactory의 객체를 생성한다

```
val viewModelFactory = DetailViewModelFactory(marsProperty, application)

```

#### 4) 마지막으로 factory로 부터 DetailViewModel의 인스턴스를 얻어오고 viewModel에 연결시킨다.

```
      binding.viewModel = ViewModelProviders.of(
                this, viewModelFactory).get(DetailViewModel::class.java)
```

#### 5) 앱을 실행시키고 Mars property photo를 탭하면 property의 상세 정보를 표시하는 detail fragment로 이동한다. 

<br><br>

## 4. Create a more useful detail page
MarsProperty는 property type(rent or buy) 및 property price를 가지고 있다. 세부 화면에는 이 값이 모두 포함되어야 하며 rental 타입일 경우에는 월별 가격으로 표시하는 것이 유용하다

#### 1) res/values/strings.xml를 연다. 가격에는 property type에 따라 display_price_monthly_rental 리소스 또는 the display_price 리소스를 사용한다

```
<string name="type_rent">Rent</string>
<string name="type_sale">Sale</string>
<string name="display_type">For %s</string>
<string name="display_price_monthly_rental">$%,.0f/month</string>
<string name="display_price">$%,.0f</string>
```

#### 2) detail/DetailViewModel.kt를 열어서 클래스 하단에 아래 코드를 추가한다

```
val displayPropertyPrice = Transformations.map(selectedProperty) {
   app.applicationContext.getString(
           when (it.isRental) {
               true -> R.string.display_price_monthly_rental
               false -> R.string.display_price
           }, it.price)
}
```

#### 3) 프로젝트의 string resources에 액세스 하기 위해 생성된 R 클래스를 import 한다

```
import com.example.android.marsrealestate.R
```

#### 4) displayPropertyPrice transformation 이후에 아래 코드를 추가한다. 이 transformation은 property type의 종류에 따라 여러 문자열 리소스를 연결한다

```
val displayPropertyType = Transformations.map(selectedProperty) {
   app.applicationContext.getString(R.string.display_type,
           app.applicationContext.getString(
                   when (it.isRental) {
                       true -> R.string.type_rent
                       false -> R.string.type_sale
                   }))
}
```

#### 5) res/layout/fragment_detail.xml를 열고 LiveData transformation으로 생성한 새 문자열을 바인딩 한다. 이를 위해서는 property type 텍스트에 viewModel.displayPropertyType를 설정하고 price 텍스트에 viewModel.displayPropertyPrice를 설정한다

```
<TextView
   android:id="@+id/property_type_text"
...
android:text="@{viewModel.displayPropertyType}"
...
   tools:text="To Rent" />

<TextView
   android:id="@+id/price_value_text"
...
android:text="@{viewModel.displayPropertyPrice}"
...
   tools:text="$100,000" />
```

#### 6) 앱을 컴파일하고 실행하면 모든 속성의 데이터가 formatted 된 것을 확인할 수 있다
