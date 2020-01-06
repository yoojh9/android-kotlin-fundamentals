# 10-2 Material Design, dimens, and color

## 1. Add a floating action button (FAB)
이번 단계에서는 GDG Finder 앱의 홈 화면에 floating action button(FAB)를 추가한다
FAB는 기본 동작을 나타내는 크고 둥근 버튼으로 사용자가 화면에서 수행해야 할 주요 작업이다. FAB는 아래 왼쪽의 스크린샷과 같이 다른 모든 내용 위에 떠있다.
사용자가 FAB을 누르면 오른쪽 그림과 같이 GDG 목록으로 이동한다.

<image src="./images/fab.png" width="70%" height="70%"/>

<br>

### Step 1: Add a FAB to the home fragment layout

#### 1) build.gradle(Module: app) 파일 안에 material 라이브러리가 포함되어 있는지 확인한다.

```
implementation 'com.google.android.material:material:1.1.0-alpha04'
```

#### 2) res/layout/home_fragment.xml을 열고 Text 탭으로 전환한다
현재 home 스크린 레이아웃은 ConstraintLayout이 자식인 단일 ScrollView를 사용한다. ConstraintLayout에 FAB를 추가한 경우 FAB는 ConstraintLayout 내부에 있으며 모든 내용 위에 떠있지 않고 ConstraintLayout의 나머지 내용과 함께 스크롤 된다. FAB를 현재 레이아웃 위에 띄울 수 있는 방법이 필요하다.

[CoordinatorLayout](https://developer.android.com/reference/android/support/design/widget/CoordinatorLayout)은 서로의 위에 뷰를 쌓을 수 있는 view group이다. 
ScrollView가 전체 화면을 차지하고 FAB가 화면 아래쪽 가장자리 근처에 떠야 한다.


#### 3) home_fragment.xml에서 ScrollView를 CoordinatorLayout으로 감싼다

```
<androidx.coordinatorlayout.widget.CoordinatorLayout
       android:layout_height="match_parent"
       android:layout_width="match_parent">
...

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

#### 4) <ScrollView> 대신 <androidx.core.widget.NestedScrollView>를 사용한다

```
androidx.core.widget.NestedScrollView
```


#### 5) CoordinatorLayout 내부에서 NestedScrollView 아래에 FloatingActionButton을 추가한다

```
<com.google.android.material.floatingactionbutton.FloatingActionButton
       android:layout_width="wrap_content"
       android:layout_height="wrap_content"/>
```

#### 6) 앱을 실행시키면 왼쪽 상단 코너 쪽에 둥근 버튼이 생긴 것을 볼 수 있다

<image src="./images/fab2.png" width="70%" height="70%"/>


#### 7) 스크롤을 해보면 버튼이 같은 위치에 떠있는 것을 확인할 수 있다

<br>

### Step 2: Style the FAB
이 단계에서는 FAB를 오르쪽 아래 모서리로 이동시키고 FAB의 동작을 나타내는 이미지를 추가한다

#### 1) home_fragment.xml에서 FAB에 layout_gravity 속성을 추가하고 버튼을 화면의 하단과 끝으로 이동시킨다. layout_gravity 속성은 뷰가 화면의 상단, 하단, 시작, 끝 또는 중앙에 배치되도록 알려준다. 또한 \|를 사용하여 position을 결합시킬 수 있다

```
android:layout_gravity="bottom|end"
```

#### 2) 화면 가장자리에 여백을 주기 위해 FAB에 layout_margin을 16dp를 준다

```
android:layout_margin="16dp"
```

#### 3) 제공된 ic_gdg 아이콘을 FAB의 이미지로 사용한다.

```
app:srcCompat="@drawable/ic_gdg"
```

<br>

### Step 3: Add a click listener to the FAB
이 단계에서는 FAB에 클릭 핸들러를 추가하여 사용자를 GDG 목록으로 이동시킨다. 

#### 1) home_fragment.xml의 <data> 태그 안에서 HomeViewModel에 대해 변수 viewModel을 정의한다.

```
<variable
   name="viewModel"
   type="com.example.android.gdgfinder.home.HomeViewModel"/>
```

#### 2) FAB에 onClick 리스너를 추가하여 onFabClicked()를 호출한다

```
android:onClick="@{() -> viewModel.onFabClicked()}"
```

#### 3) home 패키지에서 HomeViewModel 클래스를 열고 navigation live data와 function을 확인한다. FAB를 클릭하면 onFabClicked() 클릭 핸들러가 호출되고 app은 navigation을 trigger 시킨다.

```
private val _navigateToSearch = MutableLiveData<Boolean>()
val navigateToSearch: LiveData<Boolean>
   get() = _navigateToSearch

fun onFabClicked() {
   _navigateToSearch.value = true
}

fun onNavigatedToSearch() {
   _navigateToSearch.value = false
}
```

#### 4) home 패키지에서 HomeFragment 클래스를 연다. onCreateView()는 HomeViewModel을 만들고 viewModel에 할당한다


#### 5) onCreateView()에서 binding에 viewModel을 추가한다

```
binding.viewModel = viewModel
```

#### 6) 또한 onCreateView()에 GDG 목록으로 navigate 하는 observer를 추가한다

```
viewModel.navigateToSearch.observe(viewLifecycleOwner,
    Observer<Boolean> { navigate ->
        if(navigate) {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_gdgListFragment)
            viewModel.onNavigatedToSearch()
       }
})
```

#### 7) findNavController과 Observer를 아래와 같이 import 한다

```
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Observer
```


#### 8) 앱을 실행시키고 FAB를 눌러서 GDG list로 이동하는지 확인한다. 실제 기기에서 앱을 실행중인 경우 위치 권한을 요청한다. 에뮬레이터에서 앱을 실행중인 경우 다음 메시지와 함께 빈 페이지가 표시 될 수 있다.

<image src="./images/fab3.png" width="70%" height="70%"/>

이 메세지가 에뮬레이터에서 보이면 인터넷에 연결되어 있고 위치 설정이 켜져있는지 확인해라.

<br><br>

## 2. Use styling in a world of Material Design
머터리얼 디자인 구성요소를 최대한 활용하려면 테마 속성을 사용해라. Theme attributes는 앱의 기본 색상(primary color)과 같은 다양한 유형의 스타일 정보를 가리키는 변수이다. MaterialComponents에 대해 테마 속성을 지정함으로써 앱을 더 쉽게 스타일링 할 수 있다. 색상이나 글꼴에 설정 한 값은 모든 위젯에 적용되므로 일관된 디자인과 브랜딩을 가질 수 있다

### Step 1: Use Material theme attributes
이번 단계에서는 머터리얼 디자인 테마 속성을 사용하여 뷰 스타일을 지정하도록 홈 화면에서 제목 헤더의 스타일을 변경한다. 

#### 1) 타이포그래픽 테마에 대한 Material 웹 페이지를 연다 [https://material.io/develop/android/theming/typography/](https://material.io/develop/android/theming/typography/)
이 페이지는 머티리얼 테마에서 사용 가능한 모든 스타일을 보여준다

#### 2) page에서 textAppearanceHeadline5(Regular 24sp)와 textAppearanceHeadline6 (Regular 20sp)를 찾는다. 이 두가지 속성은 우리 앱과 잘 어울린다

#### 3) home_fragment.xml에서 title TextView의 현재 스타일\(android:textAppearance="@style/TextAppearance.Title"\)을 style="?attr/textAppearanceHeadline5"로 변경한다.
?attr은 테마 속성을 살펴보고, 현재 테마에 정의된 것 처럼 Headline 5를 적용하는 방법이다.

```
<TextView
       android:id="@+id/title"
       style="?attr/textAppearanceHeadline5"
```

#### 4) preview를 확인하면 title의 폰트가 변경된 것을 볼 수 있다. 아래의 스타일 우선순의 피라미드 다이어그랢에 표시된 것처럼 뷰에서 설정한 스타일이 테마에서 설정한 스타일을 재정의하기 때문에 발생한다

<image src="./images/diagram.png" width="70%" height="70%"/>

피라미드 다이어그램에서 TextApperance는 theme의 아래에 있다. TextAppearance는 텍스트 스타일을 적용시킬 수 있는 모든 view의 속성이다. 스타일과 같지 않으며 텍스트를 표시하는 방법에 대해서만 정의할 수 있다.
머터리얼 디자인 컴포넌트의 모든 텍스트 스타일은 textAppearance를 사용할 수 있다. 그렇게 하면 정의된 테마 속성이 우선한다.

#### 5) title TextView에서 방금 추가한 스타일을 textAppearance로 바꾼다

#### 6) 앱을 실행시켜서 차이점을 살펴보자. 아래 스크린샷은 Material Style이 title에 적용되고 Title style을 오버라이드 할 때의 차이점을 보여준다
 - Material Style: style="attr/textAppearanceHeadline5"
 - Text Appearance: android:textAppearance="?attr/textAppearanceHeadline5"
 
 <image src="./images/material_style.png"/> <image src="./images/text_appearance.png"/>