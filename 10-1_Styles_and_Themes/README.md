# 10-1 Styles and Theme

## 1. Android's styling system
Android는 앱의 모든 뷰 모양을 제어 할 수있는 풍부한 스타일링 시스템을 제공한다. 테마, 스타일, view의 속성을 사용하여 스타일에 영향을 줄 수 있다. 
아래 그림은 각 스타일링 방법의 우선 순위를 요약 한 것이다. 피라미드 다이어그램은 시스템에서 스타일링 방법이 아래에서 위로 적용되는 순서를 보여준다. 
예를 들어 텍스트의 크기를 theme에서 설정하고 이후에 view 속성에서 다르게 텍스트의 사이즈를 정의했다면 뷰 속성은 테마 스타일의 텍스트 사이즈 값으로 재정의 된다.

<image src="./images/styles.png" width="70%" height="70%"/>

<br>

### 1) View Attributes
 - view attribute를 사용하면 각각의 view에 속성을 명시적으로 설정할 수 있다. (view 속성은 재사용 할 수 없다.)
 - 스타일이나 테마를 통해 설정할 수 있는 모든 속성을 사용할 수 있다.
 
margin, padding, constraints와 같은 일회성 디자인에 사용할 수 있다


### 2) Styles
 - 스타일을 사용하여 폰트 사이즈나 컬러와 같은 재사용 가능한 스타일의 정보 집합을 생성할 수 있다.
 - 앱 전체에서 사용되는 작은 공통 디자인 모음을 선언하는 데 좋다

default style을 재정의하여 여러 view에 스타일을 적용한다. 예를 들어 스타일을 사용하면 일관된 header나 button의 집합을 만들 수 있다


### 3) Default Style
 - Android 시스템에서 제공하는 기본 스타일이다.


### 4) Themes
 - 테마를 사용하면 전체 앱의 색상을 정의할 수 있다
 - 테마를 사용하여 전체 앱의 기본 글꼴을 설정한다
 - 라디오 버튼이나 텍스트 뷰와 같은 모든 뷰에 적용한다
 - 전체 앱에 일관되게 적용 할 수있는 속성을 구성하는 데 사용한다


### 5) TextAppearance
 - fontFamily와 같은 텍스트 속성만 있는 스타일링에 사용된다.

<br>

안드로이드는 뷰의 스타일을 적용할 때 사용자가 정의할 수 있는 attributes, styles, themes들의 조합으로 적용한다. attribute는 항상 스타일이나 테마에 지정된 것을 덮어쓴다. 그리고 스타일을 항상 테마에 지정된 것을 덮어쓴다
아래 스크린샷은 사용자 정의 글꼴 및 헤더 크기는 물론 밝은 테마 및 어두운 테마가 있는 GDG-finder 앱을 보여준다.

<image src="./images/gdg-finder-theme.png" width="70%" height="70%"/>  

<br><br>

## 2. Use attributes for styling
이 태스크에서는 attribute를 사용하여 app layout에서 텍스트의 헤더 스타일을 저장한다

 #### 1) 홈 스크린에는 텍스트가 너무 많아서 페이지의 내용과 중요한 내용을 파악하기가 어렵다.
 
 #### 2) home_fragment.xml 파일을 연다
 
 #### 3) 레이아웃은 ConstraintLayout을 사용하여 ScrollView 내부에 요소를 배치한다.
 
 #### 4) 각 뷰를 살펴보면 constraints와 margin layout 속성이 뷰에 설정되어있다. 이러한 속성은 각 뷰 및 화면에 맞게 커스텀 되는 경향이 있기 떄문이다
 
 #### 5) title text view에서 textSize 속성을 추가하여 텍스트 크기를 24sp로 변경한다
 
 SP는 스케일 독립적인 픽셀을 나타내며, 픽셀 밀도와 사용자가 장치 설정에서 설정한 글꼴 크기 환경 설정에 따라 스케일된다
 
```
<TextView
       android:id="@+id/title"
...

android:textSize="24sp"
```

#### 6) title TextView의 textColor를 aRGB 값 #FF555555로 설정하여 불투명한 회색으로 설정한다

```
<TextView
       android:id="@+id/title"
...

android:textColor="#FF555555"
```

#### 7) 안드로이드 스튜디오에서 preview 탭을 열기 위해 View > Tool Windows > Preview를 선택하거나 LayoutEditor에서 오른쪽 가장자리에 있는 Preview 버튼을 누른다. 미리보기에서 타이틀의 텍스트가 회색이고 이전보다 크기가 커졌는지 확인한다

**Tip:** aRGB 값은 색상의 알파 투명도, 빨간색 값, 녹색 값 및 파란색 값을 나타낸다. aRGB 값은 각 색상 구성 요소에 대해 00-FF 범위의 16진 숫자를 사용한다. 

```
#(alpha)(red)(green)(blue)

#(00-FF)(00-FF)(00-FF)(00-FF)
```

   - #FFFF0000 : 완전 불투명한 빨간색
   - #5500FF00 : 반투명 녹색
   - #FF0000FF : 불투명한 파란색
   
   
#### 8) subtitle의 색깔은 헤더와 똑같은 색인 gray로 지정하고 폰트 크기는 18sp로 지정한다. default 알파값은 FF, 즉 불투명이다. 알파 값을 따로 변경하지 않으려면 생략 할 수 있다.

```
<TextView
       android:id="@+id/subtitle"
...
android:textSize="18sp"
android:textColor="#555555"
```

#### 9) subtitle 텍스트 뷰에 아래와 같은 속성을 추가한다. preview 탭을 샤용하여 앱 모양이 어떻게 바뀌는지 확인해라. 그럼 다음 추가한 속성을 제거한다

```
<TextView
       android:id="@+id/subtitle"
       ...
       android:textAllCaps="true"
       android:textStyle="bold"
       android:background="#ff9999"
```

<br><br>

## 3. Use themes and downloadable fonts
앱에서 글꼴을 사용할 때 필요한 글꼴 파일을 APK의 일부로 제공할 수 있다. 간단하지만 이 솔루션은 일반적으로 앱을 다운로드 하고 설치하는데 시간이 오래 걸리므로 권장하지 않는다. 
Android에서는 앱이 런타임에 다운로드 가능한 글꼴 API를 사용하여 글꼴을 다운로드 할 수 있다. 앱이 기기의 다른 앱과 동일한 글꼴을 사용하는 경우 Android는 글꼴을 한번만 다운로드하여 기기의 저장 공간을 절약할 수 있다.

이번 단계에서는 테마를 사용하는 앱에 있는 모든 view에 다운로드 가능한 폰트를 사용한다.

### Step 1: Apply a downloadable font

#### 1) home_fragment.xml의 Design 탭을 연다

#### 2) Component Tree 창에서 Title 텍스트 뷰를 선택한다

#### 3) Attributes 창에서 fontFamily 속성을 찾는다

#### 4) drop-down 버튼을 누른다

#### 5) More Font를 선택한다.

#### 6) Resource 창이 뜨면 \'lobster\' 폰트를 검색한다.

#### 7) Lobster Two를 선택한다

#### 8) Create downloadable font 라디오 버튼을 선택하고 OK를 선택한다

#### 9) Android Manifest 파일을 연다

#### 10) manifest 파일 아래에 name 및 resource의 속성이 \'preloaded_fonts\'로 설정된 meta 태그를 찾는다. 
이 태그는 Google Play 서비스에 이 앱이 다운로드 한 글꼴을 사용하려고 한다고 알려준다. 장치에서 글꼴을 아직 사용할 수 없는 경우 font provider는 인터넷에서 글꼴을 다운로드 한다.

```
<meta-data android:name="preloaded_fonts" android:resource="@array/preloaded_fonts"/>
```

#### 11) res/values 폴더에서 preloaded_fonts.xml 파일을 찾는다. preload_fonts.xml에는 이 앱에서 다운로드 가능한 모든 글꼴을 나열하는 배열로 정의되어 있다.

#### 12) 마찬가지로 res/fonts/lobster_two.xml 파일에는 글꼴에 대한 정보가 있다

#### 13) home_fragment.xml를 열고 preview와 code에 Lobster Two 글꼴이 title TextView에 적용되어 있는지 확인한다

#### 14) res/values/styles.xml을 열고 기본 AppTheme 테마에 대해 살펴본다

#### 15) 모든 텍스트에 Lobster Two 글꼴을 적용하려면 테마를 업데이트 해야 한다

#### 16) <style> 태그에서 parent 속성을 찾아본다. 모든 스타일 태그는 parent를 지정할 수 있고 모든 parent의 속성을 상속 할 수 있다. 
이 코드는 안드로이드 라이브러리에 의해 정의된 Theme를 지정한다. [MaterialComponents](https://material.io/develop/android/docs/theming-guide/)는 버튼이 어떻게 작동해야 하는지부터 툴바를 어떻게 그려야 하는지에 대한 명세를 제공한다.
테마의 기본 설정 중 사용자가 원하는 부분만 커스텀 할 수 있다. 현재 GDG finder app은 Light 버전에 No actionBar 테마를 사용하고 있다

```
    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.MaterialComponents.Light.NoActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>

```

#### 17) AppTheme 내부에서 font family를 lobster_two로 설정한다. android:fontFamily와 fontFamily 속성 모두 설정해야 한다. 이는 parent 테마가 둘 다 사용하기 때문이다. 

```
<style name="AppTheme"  
...    
        <item name="android:fontFamily">@font/lobster_two</item>
        <item name="fontFamily">@font/lobster_two</item>
```

#### 18) 앱을 실행시키고 새로운 폰트가 모든 텍스트에 적용되어 있는 것을 확인한다. navigation drawer을 열고 다른 화면으로 이동해보자. 다른 화면에도 변경된 텍스트가 적용된 것을 확인할 수 있다

```

```

<br>

### Step 2: Apply the theme to the title

#### 1) home_fragment.xml를 열고 lobster_two 속성을 가지고 있는 title TextView를 찾는다. 테마에서 지정한 font family와 같으므로 텍스트 뷰에 지정된 fontFamily를 삭제하고 다시 앱을 실행시킨다

#### 2) title TextView로 돌아와 fontFamily 속성을 다른 값으로 설정한다. app:fontFamily="serif-monospace"로 fontFamily 값을 변경한다.

```
<TextView
       android:id="@+id/title"
       ...
       app:fontFamily="serif-monospace"
```

#### 3) 앱을 시행하면 view의 로컬 속성이 theme 속성보다 우선함을 알 수 있다.

#### 4) title TextView의 fontFamily 속성을 제거한다

