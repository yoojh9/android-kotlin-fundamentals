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

<image src="./images/broken_image.png" width="40%" height="40%"/>

#### 2) res/drawable/loading_animation.xml을 연다. 이 drawable은 animation으로 <animate-rotate> 태그로 선언되어 있다. 애니메이션은 중심점을 기준으로 이미지 drawble인 loading_img.xml을 회전시킨다

<image src="./images/loading_image.png" width="40%" height="40%"/>

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

<br><br>

## 2. Display a grid of images with a RecyclerView

### Step 1: Update the view model
현재 ViewModel에는 하나의 MarsProperty 객체(웹 서비스 응답 목록에서 첫번째 객체)를 보유하는 _property LiveData가 있다. 이 단계에서는 LiveData가 MarsProperty 객체들의 전체 리스트를 보유하도록 변경한다

#### 1) overview/OverviewViewModel.kt를 연다

#### 2) private _property 변수를 _properties로 변경한다. 

```
private val _properties = MutableLiveData<List<MarsProperty>>()
```

#### 3) livedata property를 properties로 대체한다. 

```
 val properties: LiveData<List<MarsProperty>>
        get() = _properties
```

#### 4) 스크롤을 내려서 getMarsRealEstateProperties() 함수의 try { } 블럭에서 기존 _property.value = listResult\[0\] 코드를 아래와 같이 변경한다.

```
_properties.value = listResult
```

전체 try / catch 블럭은 다음과 같다

```
try {
   var listResult = getPropertiesDeferred.await()
   _response.value = "Success: ${listResult.size} Mars properties retrieved"
   _properties.value = listResult
} catch (e: Exception) {
   _response.value = "Failure: ${e.message}"
}
```

<br>

### Step 2: Update the layouts and fragments
이번 단계에서는 single image view가 아닌 grid layout과 recyclerView를 사용하기 위하여 layout과 fragment를 변경한다

#### 1) res/layout/grid_view_item.xml을 열고 데이터 바인딩을 OverviewViewModel에서 MarsProperty로 변경하고 변수 이름을 property로 바꾼다

```
<variable
   name="property"
   type="com.example.android.marsrealestate.network.MarsProperty" />
```

#### 2) <ImageView>의 app:imageUrl 속성을 MarsProperty의 imageSrcUrl로 변경한다

```
app:imageUrl="@{property.imgSrcUrl}"
```

#### 3) overview/OverviewFragment.kt를 열고 onCreateView()에서 FragmentOverviewBinding을 inflate 시키는 코드를 주석 해제한다. GridViewBinding을 inflate 하는 코드는 삭제하거나 주석 처리한다. 

```
val binding = FragmentOverviewBinding.inflate(inflater)
 // val binding = GridViewItemBinding.inflate(inflater)
```

#### 4) res/layout/fragment_overview.xml를 열고 <TexView> 요소를 삭제한다

#### 5) 대신에 GridLayoutManager와 grid_view_item을 레이아웃으로 사용하는 <RecyclerView> 요소를 추가한다. 

```
<androidx.recyclerview.widget.RecyclerView
            android:id="@+id/photos_grid"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:padding="6dp"
            android:clipToPadding="false"
            app:layoutManager=
               "androidx.recyclerview.widget.GridLayoutManager"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:spanCount="2"
            tools:itemCount="16"
            tools:listitem="@layout/grid_view_item" />
```

<br>

### Step 3: Add the photo grid adapter
이 단계에서는 RecyclerView 어댑터를 통해 데이터를 RecyclerView에 바인딩한다

#### 1) overview/PhotoGridAdapter.kt를 연다 

#### 2) PhotoGridAdapter 클래스를 생성한다. 생성자 파라미터는 아래 보이는 것과 같다. PhotoGridAdatper는 생성자로 list item type과 view holder 및 DiffUtil.ItemCallback 구현이 필요한 ListAdpater를 상속한다.

```
class PhotoGridAdapter : ListAdapter<MarsProperty,
        PhotoGridAdapter.MarsPropertyViewHolder>(DiffCallback) {

}
```

#### 3) ListAdapter의 메소드인 onCreateViewHolder()과 onBindViewHolder()를 오버라이드 해라

```
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoGridAdapter.MarsPropertyViewHolder {
   TODO("not implemented") 
}

override fun onBindViewHolder(holder: PhotoGridAdapter.MarsPropertyViewHolder, position: Int) {
   TODO("not implemented") 
}
```

#### 4) PhotoGridAdapter 정의의 끝에서 DiffCallback에 대한 companion object를 추가한다. DiffCallback object는 DiffUtil.ItemCallback을 상속한다.

```
companion object DiffCallback : DiffUtil.ItemCallback<MarsProperty>() {
}
```

#### 5) object 안에서 비교 메소드를 구현한다. 메소드는 areItemsTheSame()과 areContentsTheSame()이다

```
override fun areItemsTheSame(oldItem: MarsProperty, newItem: MarsProperty): Boolean {
   TODO("not implemented") 
}

override fun areContentsTheSame(oldItem: MarsProperty, newItem: MarsProperty): Boolean {
   TODO("not implemented") 
}
```

#### 6) areItemsTheSame() 메소드에서 oldItem과 newItem에 대한 객체 참조가 동일한 경우 true를 반환하는 Kotlin의 참조 등식 연산자(===)를 사용하여 구현한다

```
override fun areItemsTheSame(oldItem: MarsProperty, 
                  newItem: MarsProperty): Boolean {
   return oldItem === newItem
}
```

#### 7) areContentsTheSame() 메소드에서 동등성을 비교하기 위해 oldItem과 newItem의 ID를 비교하는 기능을 추가한다

```
override fun areContentsTheSame(oldItem: MarsProperty, 
                  newItem: MarsProperty): Boolean {
   return oldItem.id == newItem.id
}
```

#### 8) PhotoGridAdapter의 companion object 아래에 MarsPropoertyViewHolder라는 이름의 inner class를 생성한다. 이 클래스는 RecyclerView.ViewHolder를 상속한다
- MarsProperty를 레이아웃에 바인딩하려면 GridViewItemBinding 변수가 필요하므로 binding 변수를 MarsPropertyViewHolder에 전달한다.
- base ViewHolder에는 생성자에 뷰가 필요하므로 binding.root 뷰를 전달한다

```
class MarsPropertyViewHolder(private var binding: 
                   GridViewItemBinding):
       RecyclerView.ViewHolder(binding.root) {

}
```

#### 9) MarsPropertyViewHolder에서 bind() 메소드를 만든다. bind() 메소드는 property에 MarsProperty 객체를 저장한다. executePendingBindings()를 호출하여 즉시 반영되도록 한다

```
fun bind(marsProperty: MarsProperty) {
   binding.property = marsProperty
   binding.executePendingBindings()
}
```

#### 10) onCreateViewHolder() 메소드는 GridViewItemBinding 레이아웃을 inflate하고 상위 ViewGroup context에서 정의된 LayoutInflater를 사용하여 생성된 MarsPropertyViewHolder를 리턴해야 한다.

```
return MarsPropertyViewHolder(GridViewItemBinding.inflate(
  LayoutInflater.from(parent.context)))
```

#### 11) onBindViewHolder() 메소드에서 현재 RecyclerView position과 관련된 MarsProperty를 가져오는 getItem()을 호출하고, MarsPropertyViewHolder의 bind() 메소드에 그 프로퍼티를 전달한다

```
val marsProperty = getItem(position)
holder.bind(marsProperty)
```

<br>

### Step 4: Add the binding adapter and connect the parts
마지막으로 BindingAdapter를 이용하여 PhotoGridAdapter를 MarsProperty 객체의 리스트로 초기화한다. BindingAdapter를 사용하여 RecyclerView의 데이터를 설정하면 데이터 바인딩에서 MarsProperty 개체 목록의 LiveData를 자동으로 관찰한다.
그런 다음 MarsProperty 목록이 변경되면 바인딩 어댑터가 자동으로 호출된다. 

#### 1) BindingAdapters.kt를 연다

#### 2) 파일의 끝에 bindRecyclerView() 메소드를 추가하고 인자 값으로 RecyclerView와 MarsProperty 객체 리스트를 넘긴다. @BindingAdapter 어노테이션도 추가한다.

```
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, 
    data: List<MarsProperty>?) {
}
```

#### 3) bindRecyclerView() 함수 내에서 recyclerView.adapter를 PhotoGridAdapter로 캐스트한다. 그리고 data와 함께 adapter.submitList(data)를 호출한다. 새 리스트를 사용할 수 있으면 RecyclerView에 알려준다

```
val adapter = recyclerView.adapter as PhotoGridAdapter
adapter.submitList(data)
```

#### 4) res/layout/fragment_overview.xml를 열어서 RecyclerView에 app:listData 속성을 추가하고 data binding에서 사용하고 있는 viewmodel.properties로 값을 설정한다

```
app:listData="@{viewModel.properties}"
```

#### 5) overview/OverviewFragment.kt를 열어서 onCreateView() 안에 setHasOptionsMenu()가 호출되기 직전에 binding.photosGrid의 RecyclerView 어댑터를 새 PhotoGridAdapter 객체로 초기화해라

```
binding.photosGrid.adapter = PhotoGridAdapter()
```
