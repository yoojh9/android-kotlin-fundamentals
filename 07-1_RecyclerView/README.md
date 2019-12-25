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

#### 5) 