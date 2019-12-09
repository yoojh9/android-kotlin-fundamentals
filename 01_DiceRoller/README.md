# 01_DicreRoller

## 1. Examine MainActivity
#### 1. AppCompatActivity
 - 모든 최신 Android 기능을 지원하면서 이전 버전의 Android와의 하위 호환성을 제공하는 Activity의 하위 클래스이다.
 - 앱을 최대한 많은 장치와 사용자가 사용할 수 있게 하려면 항상 AppCompatActivity를 사용하는 것을 권장한다.


## 2. Examine and explore the app layout file
#### 1. LinearLayout
 - LinearLayout은 ViewGroup이다.
 - ViewGroup은 다른 View를 보유하고, 뷰의 위치를 지정하는데 도움이 되는 컨테이너이다.

#### 2. ConstraintLayout
 - 안드로이드의 기본 root layout이다.


## 3. Extract string resources
 - layout 파일에 string들을 하드 코딩 하는게 아니라 strings.xml 파일로 분리 시킴

## 4. Context
 - Context 객체를 사용하면 Android OS의 현재 상태와 통신하고 정보를 얻을 수 있다
 - 밑에 예제에서 Toast는 OS에게 toast를 띄우라고 알려줄 수 있다.
 - AppCompatActivity는 Context의 subclass이므로 this 키워드를 사용할 수 있다.
    ```
        Toast.makeText(this, "button clicked", Toast.LENGTH_SHORT).show()
    ```