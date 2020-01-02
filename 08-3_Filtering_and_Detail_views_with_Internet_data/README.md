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