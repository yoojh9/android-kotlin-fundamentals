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

<image src="./images/fab2.png" width="40%" height="40%"/>


#### 7) 스크롤을 해보면 버튼이 같은 위치에 떠있는 것을 확인할 수 있다

<br>

### Step 2: Style the FAB
이 단계에서는 FAB를 오르쪽 아래 모서리로 이동시키고 FAB의 동작을 나타내는 이미지를 추가한다

#### 1) home_fragment.xml에서 FAB에 layout_gravity 속성을 추가하고 버튼을 화면의 하단과 끝으로 이동시킨다. layout_gravity 속성은 뷰가 화면의 상단, 하단, 시작, 끝 또는 중앙에 배치되도록 알려준다. 또한 \|를 사용하여 position을 결합시킬 수 있다

```
android:layout_gravity="bottom|end"
```

#### 2) 화면 가장자리에 여백을 주기 위해 FAB에 layout_margin을 @dimen/spacing_normal를 준다

```
android:layout_margin="@dimen/spacing_normal"
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

<image src="./images/fab3.png" width="50%" height="50%"/>

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

<image src="./images/diagram.png" width="50%" height="50%"/>

피라미드 다이어그램에서 TextApperance는 theme의 아래에 있다. TextAppearance는 텍스트 스타일을 적용시킬 수 있는 모든 view의 속성이다. 스타일과 같지 않으며 텍스트를 표시하는 방법에 대해서만 정의할 수 있다.
머터리얼 디자인 컴포넌트의 모든 텍스트 스타일은 textAppearance를 사용할 수 있다. 그렇게 하면 정의된 테마 속성이 우선한다.

#### 5) title TextView에서 방금 추가한 스타일을 textAppearance로 바꾼다

#### 6) 앱을 실행시켜서 차이점을 살펴보자. 아래 스크린샷은 Material Style이 title에 적용되고 Title style을 오버라이드 할 때의 차이점을 보여준다
 - Material Style: style="attr/textAppearanceHeadline5"
 - Text Appearance: android:textAppearance="?attr/textAppearanceHeadline5"

<br>

**Material Style** 

<image src="./images/material_style.png" height="50%" width="50%"/>

<br>

**Text Appearance**

<image src="./images/text_appearance.png" height="50%" width="50%"/>

<br><br>

### Step 2: Change the style in the Material theme
textAppearanceHeadline6은 subtitle에 좋은 material이지만 기본 크기는 타이틀 스타일로 적용된 18sp가 아닌 20sp이다. 각각의 subtitle 뷰에 사이즈를 재정의 하는 대신에 Material theme을 수정하고 default size를 재정의 할 수 있다

#### 1) styles.xml을 연다

#### 2) Title과 Subtitle 스타일을 지운다. 지금 이미 Title 대신에 textAppearanceHeadline5를 사용하고 있고 Subtitle 역시 필요 없다

#### 3) CustomHeadline6을 textSize 18sp로 정의한다. 명시적으로 오버라이드 하지 않은 나머지 속성들을 상속받기 위해, parent 값으로 TextAppearance.MaterialComponents.Headline6를 설정한다. 

```
<style name="TextAppearance.CustomHeadline6" parent="TextAppearance.MaterialComponents.Headline6">
   <item name="android:textSize">18sp</item>
</style>
```

#### 4) style 안에서 item 항목에 textAppearanceHeadline6를 커스텀 함으로써 textAppearanceHeadline6 theme의 기본값인 textAppearanceHeadline6를 재정의한다. 

```
<item name="textAppearanceHeadline6">@style/TextAppearance.CustomHeadline6</item>
```

#### 5) home_fragment.xml의 subtitle 뷰에 textAppearanceHeadline6를 적용한다. 

```
android:textAppearance="?attr/textAppearanceHeadline6"
```

<br><br>

## 3. Change the toolbar theme
떄때로 화면의 전부가 아닌 일부를 다른 테마로 변경하고 싶을 수 있다. 예를 들어 툴바에 dark Material components theme을 사용하도록 할 수 있다. 테마 오버레이를 사용하여 이 작업을 수행한다.
테마는 전체 앱의 global 테마를 설정하는 데 사용한다. ThemeOverlay는 특정 뷰, 특히 toolbar와 같은 테마를 오버라이드 하는데 사용된다.

Theme overlay는 케이크 위에 아이싱 하는 것과 같이 기존 테마를 오버레이 하도록 설계된 'thin theme'이다. Theme overlay는 앱의 하위 섹션을 변경하기를 원할 때 사용하기에 유용하다. 예를 들어 toolbar를 어둡게 만들고 나머지 화면 영역은 light theme를 유지하고자 할 때 사용한다.
테마 오버레이를 뷰에 적용하고 오버레이는 해당 뷰 및 모든 하위 뷰에 적용된다.

<br>

### Step 1: Use theme overlays
MaterialComponents의 테마에는 밝은 화면에 어두운 툴바에 대한 옵션이 없다. 이 단계에서는 단지 toolbar의 theme만 변경한다. MaterialComponents에서 사용할 수있는 Dark 테마를 툴바에 오버레이로 적용하여 툴바를 어둡게 만든다

#### 1) activity_main.xml을 열고 (androidx.appcompat.widget.Toolbar)로 정의된 Toolbar를 찾는다. Toolbar는 Material Design의 일부이며 activity가 기본적으로 사용하는 app bar보다 더 많은 커스터마이징을 허용한다

#### 2) toolbar와 toolbar의 childern 뷰를 dark theme으로 변경하기 위해 Toolbar의 테마를 Dark.ActionBar 테마로 변경한다. 

```
<androidx.appcompat.widget.Toolbar
    android:theme="@style/ThemeOverlay.MaterialComponents.Dark.ActionBar"
```

#### 3) Toolbar의 background를 colorPrimaryDark로 변경한다

```
android:background="?attr/colorPrimaryDark"
```

변경된 배경색으로 인해 텍스트가 눈에 잘 띄지 않는다. 새 이미지를 만들거나 새 이미지를 만들지 않고 대비를 높이려면 ImageView에서 색조를 설정할 수 있다. 이로 인해 전체 ImageView가 지정된 색상으로 'tint'된다.
ColorOnPrimary 속성은 primary color 위에 그려질 때 텍스트 또는 아이콘에 대한 접근성 가이드라인을 충족시키는 색상이다. 

<image src="./images/toolbar_theme1.png" width="70%" height="70%"/>

#### 4) Toolbar 내부의 ImageView 안에서 tint를 colorOnPrimary로 설정한다. drawabale은 이미지와 GDG Finder text를 모두 포함하고 있으므로 밝게 표시된다

```
android:tint="?attr/colorOnPrimary"
```

#### 5) 앱을 실행시켜 header가 dark theme으로 변경된 것을 확인한다. 또한 tint 속성은 "GDG Finder"라는 텍스트와 아이콘을 포함한 로고 이미지를 밝게 변경한다.

<image src="./images/toolbar_theme2.png" width="70%" height="70%"/>

<br><br>

## 4. Use dimensions
전문적으로 보이는 앱은 동일한 룩앤필을 갖는다. 이런 앱들은 모든 화면에 같은 컬러들과 비슷한 레이아웃들을 가지고 있다. 이를 통해 앱이 좋게 보여질 뿐만 아니라 사용자가 화면을 더 쉽게 이해하고 상호작용 할 수 있다

Dimens, 또는 dimension은 재사용 가능한 측정 값을 지정할 수 있다. dp를 사용하여 marin, height, padding을 지정하고 sp를 사용하여 폰트의 사이즈를 설정한다

이번 단계에서는 화면의 오른쪽과 왼쪽에 일관된 마진을 적용하는데 사용할 dimen을 정의해보자

### Step 1: Examine your code

#### 1) home_fragment.xml을 연다

#### 2) Design 탭을 눌러서 blueprint가 활성화 되어있는지 살펴본다

#### 3) Component Tree 창에서 start_guideline과 end_guideline을 선택한다. 

#### 4) start_guideline과 end_guideline의 값이 16, 26인 것을 확인한다

#### 5) Text 탭으로 전환한다

#### 6) ConstraintLayout 하단에 두가지의 Guidelines이 정의되어 있는 것을 확인한다. Guidelines은 컨텐츠의 가장자리를 정의하는 화면에서 수직 또는 수평의 선을 정의할 수 있다. 전체 화면 이미지를 제외한 모든 것이 가이드라인 안에 배치된다

```
<androidx.constraintlayout.widget.Guideline
   android:id="@+id/start_guideline"
   android:layout_width="wrap_content"
   android:layout_height="wrap_content"
   android:orientation="vertical"
   app:layout_constraintGuide_begin="@dimen/spacing_normal" />

<androidx.constraintlayout.widget.Guideline
   android:id="@+id/end_guideline"
   android:layout_width="wrap_content"
   android:layout_height="wrap_content"
   android:orientation="vertical"
   app:layout_constraintGuide_end="26dp" />
```

layout_constraintGuide_begin="@dimen/spacing_normal"는 Material 명세를 따르지만 app:layout_constraintGuide_end="26dp"은 @dimen/spacing_normal가 되어야 한다.
여기서는 수동으로 값을 고치지만 이러한 margin에 대해 dimension을 만든 다음 앱 전체에서 일관되게 적용하는 것이 좋다

<br>

### Step 2: Create a dimension

#### 1) home_fragment.xml에서 app:layout_constraintGuide_begin="@dimen/spacing_normal"의 @dimen/spacing_normal에 커서를 놓는다

#### 2) Alt + Enter (Option + Enter for mac)을 이용하여 intention menu를 열고 Extract dimension resource를 선택한다

#### 3) dimension의 Resource Name은 spacing_normal로 설정한다. 나머지는 그대로 둔 채 OK를 클릭한다

#### 4) layout_constraintGuide_end도 spacing_normal dimension을 적용한다

```
<androidx.constraintlayout.widget.Guideline
       android:id="@+id/end_grid"
       app:layout_constraintGuide_end="@dimen/spacing_normal"
```

#### 5) 안드로이드 스튜디오에서 **Replace All** 창을 연다 (Cmd+Shift+R on the Mac or Ctrl+Shift+R on Windows)

#### 6) @dimen/spacing_normal를 검색하고 dimens.xml의 항목을 제외한 모든 항목을 @dimen/spacing_normal로 바꾼다

#### 7) 앱을 실행시켜서 텍스트의 왼쪽와 오른쪽 간격이 동일한 것을 확인한다. 

<image src="./images/dimension.png" width="70%" height="70%"/>


<br><br>

## 5. Use colors
color resource와 Material Theme을 사용하면 앱 전체에 일관적인 색깔을 적용할 수 있다. 최상의 색상과 색상 조합을 고르는 것은 어려울 수 있지만 도움이 되는 도구가 있따

첫번쨰 유용한 툴은 [Color Tool](https://material.io/resources/color/)이다. tool에서 앱의 full Material color scheme을 얻으려면 기본 메인 color 2가지를 고르고 tool이 나머지 color를 적절하게 만든다.

이 단계에서는 Material color scheme을 생성하고 앱에 적용시켜보자

<br>

### Step 1: Create a Material color scheme

#### 1) [https://material.io/tools/color/](https://material.io/tools/color/)를 연다. 이 도구를 사용하여 UI의 색상 조합을 탐색할 수 있다.

#### 2) **Primary**를 선택하고 color를 클릭한다.

#### 3) **Secondary**를 선택하고 color를 클릭한다

#### 4) tool에서 만들어진 것과 다른 텍스트 color를 원할 경우 **Text**를 클릭해서 color를 선택한다

#### 6) **ACCESSIBILITY** 탭을 클릭하면 리포트를 얻을 수 있다. 이 보고서는 현재 선택된 색상이 얼마나 읽기 쉬운지에 대한 정보를 제공한다

<image src="./images/color_tool.png" width="70%" height="70%"/>


#### 7) 아래와 같이 삼각형 느낌표 아이콘을 찾아보자

<image src="./images/exclamation.png" width="70%" height="70%"/>

**Note:** 이 도구는 다양한 형태의 시력 손상을 지닌 사람에게 얼마나 잘 보이는지에 대해 평가를 제공한다. contrast(대비)가 향상되면 텍스트를 보다 쉽게 읽을 수 있다.


#### 8) 도구에서 **CUSTOM** 탭으로 전환하고 다음 두가지 컬러를 입력한다
 - Primary: #669df6
 - Secondary: #a142f4
 
 Primary color는 GDG 로고에서 사용된 색상을 기반으로 하는 파란색이다. secondary color는 홈 화면 이미지에서 풍선 색깔이다. 
 
#### 9) 오른쪽 상단에서 EXPORT 와 ANDROID를 선택한다. 

#### 10) colors.xml이 다운로드 된다

<br>

### Step 2: Apply the Material color scheme to your app

#### 1) 텍스트 에디터에서 colors.xml을 연다

#### 2) 안드로이드 스튜디오에서 values/color.xml을 연다

#### 3) values/colors.xml의 리소스를 다운로드 한 colors.xml 파일의 내용으로 변경한다

#### 4) style.xml을 연다

#### 5) AppTheme에서 에러가 나는 colorPrimary, colorPrimaryDark, colorAccent를 지운다

#### 6) AppTheme에서 새로운 color 6개 속성을 정의한다.

```
<item name="colorPrimary">@color/primaryColor</item>
<item name="colorPrimaryDark">@color/primaryDarkColor</item>
<item name="colorPrimaryVariant">@color/primaryLightColor</item>
<item name="colorOnPrimary">@color/primaryTextColor</item>
<item name="colorSecondary">@color/secondaryColor</item>
<item name="colorSecondaryVariant">@color/secondaryDarkColor</item>
<item name="colorOnSecondary">@color/secondaryTextColor</item>
```

#### 7) 그러나 colorPrimaryDark 위에 표시될 때 로고 tint("GDG Finder" 텍스트를 포함)가 두드러질 정도로 colorOnPrimary가 충분히 밝지 않다

#### 8) activity_main.xml에서 Toolbar를 찾고 ImageView의 tint를 colorOnSecondary로 변경한다

#### 9) 앱을 실행시킨다

**ColorOnPrimary:** primary color 위에 그려지는 텍스트 및 아이콘에 대한 접근성 가이드라인을 통과한 색상이다
**colorOnSecondary:** Secondary color 위에 그려지는 텍스트 및 아이콘에 대한 접근성 가이드라인을 통과한 색상이다

