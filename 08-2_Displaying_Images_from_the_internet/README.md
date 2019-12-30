# 08-2 Loading and displaying images from the internet

## 1. Display an internet image
웹 URL에 있는 사진을 표시하는 것은 간단하게 들릴 수 있지만 제대로 작동하려면 약간의 기술이 요구된다. 이미지는 압축된 형식에서 안드로이드가 사용할 수 있는 이미지로 다운로드, buffered, decoded 되어야 한다. 
이미지는 메모리 내 캐시, 스토리지 기반 캐시 또는 둘 다에 캐시되어야 한다. 이 모든 작업은 우선 순위가 낮은 background thread에서 발생하므로 UI는 계속 반응한다.
또한 최상의 네트워크 및 CPU 성능을 위해 한번에 하나 이상의 이미지를 가져오고 디코딩 할 수 있다. 

다행히도 Glide라는 라이브러리를 사용하여 이미지를 download, buffer, decode, cache 할 수 있다.
Glide는 기본적으로 두가지가 필요하다
    - load 또는 show 하기를 원하는 image의 URL
    - 이미지를 표현하는 ImageView 객체

이번 단계에서는 real estate 웹 서비스에서 하나의 이미지를 Glide를 사용하여 어떻게 표시하는지 배운다. 


### Step 1: Add Glide dependency

#### 1)  build.gradle(Module: app)을 열어서 dependencies 영역에 Glide 라이브러리를 추가한다

```
implementation "com.github.bumptech.glide:glide:$version_glide"
```

<br>

### Step 2: Update the view model
하나의 Mars 속성에 대한 라이브 데이터를 포함하도록 OvervieewViewModel 클래스를 업데이트 한다.

#### 1) overview/OverviewViewModel.kt를 열어서 LiveData _response 밑에 internal(mutable) external(immutable) live data로 MarsProperty 객체를 추가한다

```
private val _property = MutableLiveData<MarsProperty>()

val property: LiveData<MarsProperty>
   get() = _property
```

#### 2) getMarsRealEstateProperties() 함수에서 try/catch { } 블럭을 찾아서 _property LiveData에 listResult 인덱스 0번쨰 값을 넣는다

```
if (listResult.size > 0) {   
    _property.value = listResult[0]
}
``` 

완성된 try / catch {} 블럭은 아래와 같다

```
try {
   var listResult = getPropertiesDeferred.await()
   _response.value = "Success: ${listResult.size} Mars properties retrieved"
   if (listResult.size > 0) {      
       _property.value = listResult[0]
   }
 } catch (e: Exception) {
    _response.value = "Failure: ${e.message}"
 }
```

<br>

#### 3) res/layout/fragment_overview.xml 파일을 열어서 <TextView> 요소 안에 android:text를 LiveData인 property의 imgSrcUrl 값을 바인딩 시킨다.

```
android:text="@{viewModel.property.imgSrcUrl}"
```

#### 4) 앱을 구동시키면 TextView에 0번째 Mars 프로퍼티의 image URL이 표시된다. 

<br><br>

### Step 3: Create a binding adapter and call Glide
이제 이미지를 로드하기 위해 Glide 작업을 시작해보자. 이 단계에서는 binding adapter를 사용하여 ImageView와 관련된 xml 속성에서 url을 가져오고 Glide를 사용하여 이미지를 로드한다. 바인딩 어댑터는 데이터가 변경될 때 사용자 custom 동작을 제공하기 위해 View와 바인딩 된 데이터 사이에 있는 확장 메소드이다.
이 예제에서 사용자 custom 동작은 이미지 URL을 ImageView에 이미지를 로드하기 위해 Glide를 호출하는 것이다.

#### 1) BindingAdapters.kt를 연다. 이 파일은 앱 전체에서 사용하는 바인딩 어댑터를 가진다

#### 2) bindImage() 함수를 만들고 파라미터로 ImageView, String을 설정한다. 함수 위에 @BindingAdapter 어노테이션을 설정한다. @BindingAdapter 어노테이션은 XML 항목에 imageUrl 속성이 있을 때 바인딩 어댑터가 실행되도록 data binding에게 알려준다.

```
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {

}
```

#### 3) bindImage() 함수 안에 imgUrl?.let{ } 블럭을 추가한다.

```
imgUrl?.let {

}
``` 

#### 4) let { } 블럭 안에서 URL string을 Uri 객체로 convert 하는 코드를 추가한다. androidx.core.net.toUri를 import한다
HTTPS 스키마를 사용하기 위해 buildUpon.scheme("https")를 toUri 빌더에 추가한다. toUri() 메소드는 Android KTX core 라이브러리의 Kotlin 확장 함수이다. 그래서 String 클래스의 일부인 것처럼 보인다.

```
val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
```

#### 5) let { } 블럭 안에서 Gllide.with()를 호출하여 Uri 객체로부터 ImageView에 이미지를 로드한다. 

```
Glide.with(imgView.context)
       .load(imgUri)
       .into(imgView)
```

<br><br>

### Step 4: Update the layout and fragments
Glide에서 이미지를 로드 했지만 아직 아무것도 보이지 않는다. 다음 단계는 layout과 프래그먼트를 수정하여 ImageView에 이미지가 보이도록 만든다

#### 1) res/layout/gridview_item.xml 파일을 연다. 이 layout resource file은 RecyclerView의 각 항목에 사용할 파일이다. 여기에서는 임시로 사용하여 단일 이미지만 표현한다

#### 2) <ImageView> 요소 위에 data binding을 위한 <data> 요소를 추가한다. OverviewViewModel 클래스를 바인딩한다.

```
<data>
   <variable
       name="viewModel"
       type="com.example.android.marsrealestate.overview.OverviewViewModel" />
</data>
``` 

#### 3) ImageView에 새로운 이미지를 로드하는 binding adapter를 사용하기 위해 app:imgUrl 속성을 추가한다.

```
app:imageUrl="@{viewModel.property.imgSrcUrl}"
```

#### 4) overview/OverviewFragment.kt를 열어서 onCreateView() 메소드 아래에, FragmentOverviewBinding 클래스를 inflate 시키고 바인딩 변수에 지정하는 행을 주석 처리한다. 이것은 임시 테스트 용으로 나중에 다시 되돌린다.

```
//val binding = FragmentOverviewBinding.inflate(inflater)
```

#### 5) 대신 GridViewItemBinding를 inflate 하기 위해 추가한다.

```
val binding = GridViewItemBinding.inflate(inflater)
```

#### 6) 앱을 실행시키면 MarsProperty의 첫번째 객체의 image가 보여지는 것을 확인할 수 있다.

<br><br>

### Step 5: Add simple loading and error images
Glide는 이미지를 표시하는 동안 placeholder image나 이미지 로드 실패 시 error image를 표시하여 사용자 experience 향상 시킬 수 있다. 예를 들어 이미지가 없거나 손상된 경우의 대응하는 기능을 만들어서 해당 기능을 바인딩 어댑터 및 레이아웃에 추가한다

#### 1) res/drawable/ic_broken_image.xml을 열고 preview 탭을 클릭한다. 오류 이미지의 경우 내장 아이콘 라이브러리에서 사용 가능한 깨진 이미지 아이콘을 사용하고 있다. 이 벡터 drawble은 android:tint 속성을 사용하여 아이콘을 회색으로 표시한다

<image src="./images/broken_image.png" width="70%" height="70%"/>

#### 2) res/drawable/loading_animation.xml을 연다. 이 drawable은 animation으로 <animate-rotate> 태그로 선언되어 있다. 애니메이션은 중심점을 기준으로 이미지 drawble인 loading_img.xml을 회전시킨다

<image src="./images/loading_image.png" width="70%" height="70%"/>

#### 3) BindingAdapter.kt 파일로 돌아와서 bindImage() 함수 안에 load()와 into()사이에 apply()를 호출하도록 Glide.with() 메소드를 수정한다. 
이 코드는 이미지가 loading 되는 동안 placeholder loading image를 설정한다. 또한 이미지 로딩이 실패했을 때 broken_image drawable을 설정하여 에러 이미지를 표시한다. 

```
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = 
           imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
                .load(imgUri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image))
                .into(imgView)
    }
}
```