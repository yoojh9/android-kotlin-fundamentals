# 01_DiceRoller

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


#### 3. Extract string resources
 - layout 파일에 string들을 하드 코딩 하는게 아니라 strings.xml 파일로 분리 시킴

#### 4. Context
 - Context 객체를 사용하면 Android OS의 현재 상태와 통신하고 정보를 얻을 수 있다
 - 밑에 예제에서 Toast는 OS에게 toast를 띄우라고 알려줄 수 있다.
 - AppCompatActivity는 Context의 subclass이므로 this 키워드를 사용할 수 있다.
    ```
        Toast.makeText(this, "button clicked", Toast.LENGTH_SHORT).show()
    ```
#### 5. Activities
 - MainActivity는 AppCompatActivity의 subclass이며, Activity의 하위 클래스이기도 하다.
 - Activity는 앱 UI를 그리고, input 이벤트를 수신하는 핵심 Android 클래스이다.
 - 모든 layout file은 activity들과 관련이 있다.
 - setContentView() 메소드는 activity가 create 될 때 layout을 inflate 시키는 메소드이다
 - layout inflation은 xml 레이아웃 파일에 정의된 뷰가 kotlin 메모리의 뷰 객체로 변환되는 프로세스이다

#### 6. Views
 - app layout 안에 모든 UI 요소들은 View 클래스의 하위 클래스들이다. views라고도 불린다.
 - TextView, Button은 views의 한 예다
 - View 요소들은 ViewGroup으로 묶일 수 있는데, 뷰 그룹은 뷰 또는 다른 뷰 그룹 내의 컨테이너 역할을 한다.
 - LinearLayout은 뷰를 선형으로 배열하는 뷰 그룹의 예이다.

## 3. Use a default image
#### 1. tools
 - tools 네임스페이스는 안드로이드 스튜디오의 design editor나 preview에서 placeholder 영역을 정의할 때 사용한다
 - tools 네임스페이스는 앱을 컴파일 할 때는 지워진다.

## 4. Understanding API levels and compatibility
#### 1. Explore API levels

    ```
    android {
        compileSdkVersion 29
        defaultConfig {
            applicationId "com.example.kotlin.a01_diceroller"
            minSdkVersion 19
            targetSdkVersion 29
            versionCode 1
            versionName "1.0"
            testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        }
    ```

 - compileSdkVersion은 gradle이 app을 컴파일 하는 데 사용하는 android API 레벨을 지정한다.
 
 
    ```
        compileSdkVersion 29
    ```
 
 - targetSdkVersion은 앱을 테스트한 최신 API이다. 많은 경우에 compileSdkVersion과 동일한 값이다
 
 
    ```
        targetSdkVersion 29
    ```
 
 - minSdkVersion 파라미터는 앱이 실행될 가장 오래된 Android 버전을 결정한다.
 - 이 API 레벨보다 오래된 Android OS 기기는 앱을 전혀 실행할 수 없다
 - API 레벨을 너무 낮게 설정하면 Android OS의 최신 기능들을 놓치게 되지만 너무 높게 설정하면 앱이 최신 기기에서만 실행될 수 있다,
 
 
   ```
       minSdkVersion 19
   ``` 
       
## 5. Explore compatibility (호환성)
 - 2018년 구글은 support library를 확장하면서 많은 이전 버전과 호환되는 클래스와 기능을 포함하는 라이브러리의 모음인 Android Jetpack을 발표했다
 - Jetpack은 이전에 Android support library로 알려진 라이브러리를 대체하고 확장한다.
 - AppCompatActivity는 다양한 플랫폼 OS 수준에서 activity가 동일하게 보이도록 하는 호환성 클래스이다
 - AppCompatActivity는 androidx.appcompat.app 패키지 내의 클래스이다
 - androidx는 Android Jetpack 라이브러리의 네임스페이스이다.
 
 
    ```
        import androidx.appcompat.app.AppCompatActivity
    
        class MainActivity : AppCompatActivity() {
        
        }
        
        
        // build.gradle (Module: app)
        dependencies {
            implementation 'androidx.appcompat:appcompat:1.0.2'
        }
        
    ``` 
    
## 6. Add compatibility for vector drawables
 - res/drawable 내 주사위 이미지 파일들은 주사위의 색상과 모양을 정의하는 XML 파일이다.
 - 이런 종류의 파일을 vector drawable 파일이라고 한다
 - vector drawable 파일을 PNG와 같은 비트맵 이지미 형식과 비교하자면 vector drawable 파일은 품질의 손실 없이 이미지를 확대할 수 있으며, bitmap 포맷 형식의 이미지보다 크기가 훨씬 작은 파일이다.
 - 유의할 점은 vector drawable이 API 21이상에서 지원된다는 점이다.
 - minimum SDK가 API 19로 세팅되어 있다면, 21 버전보다 낮은 안드로이드 디바이스들은 vector 파일을 PNG 파일로 변환하여 빌드한다. 이런 PNG 파일들은 앱의 사이즈를 늘리게된다.
 - 좋은 소식은 vector drawable을 위한 Android X 호환성 라이브러리가 있다는 점이다. (안드로이드 API 레벨 7까지 지원)


    ```
        // buidl.gradle (Module: app)
        android {
            defaultConfig {
                vectorDrawables.useSupportLibrary = true
            }
            
        // activity_main.xml
        // app 네임스페이스는 사용자 지정 코드나 android framework가 아닌 라이브러리에서 제공되는 속성을 위한 네임스페이스이다.
        android:src -> app:srcCompat 으로 변경
        
    ```