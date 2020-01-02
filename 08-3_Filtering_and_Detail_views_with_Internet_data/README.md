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