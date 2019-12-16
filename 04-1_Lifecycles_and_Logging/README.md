# 04-1_Lifecycles and Logging

## 1. Explore the lifecycle methods and add basic logging
 - 모든 액티비티와 프래그먼트는 lifecycle을 가지고 있다
 - activity 라이프 사이클은 activity가 처음 초기화 된 시점부터 마지막으로 destroy 되고 시스템 메모리 상에서 회수될 때 까지 여러 상태로 구성된다. 
 - 사용자가 앱을 시작하고 액티비티 간 또는 앱의 내부 및 외부를 이동하면서, 그리고 앱을 떠나면서 activity의 상태는 변한다
 
 <img src="./images/activity_lifecycle.png"  width="40%" height="40%"/>
 
 - 안드로이드는 ativity가 다른 상태로 변할 때 마다 callback을 invoke한다.
 - lifecycle의 변경될 때 수행되어야 할 작업으로 method를 오버라이드 할 수 있다.
 - 아래 다이어그램은 오버라이드 가능한 메소드와 lifecycle을 함께 보여준다.
 
  <img src="./images/activity_lifecycle2.png"  width="40%" height="40%"/>
  
 <br> 
  
 - 프래그먼트 역시 lifecycle을 가지고 있다. 프래그먼트의 라아프사이클은 액티비티의 라이프사이클과 많이 유사하다. 프래그먼트 라이프사이클은 아래 그림과 같다
 
 <img src="./images/fragment_lifecycle.png"  width="40%" height="40%"/>

<br>
 
 ### Step 1: Examine the onCreate()
  - 안드로이드 logging API를 사용하여 안드로이드 생명주기에 대해 더 파악해보자
  
  ```
      override fun onCreate(savedInstanceState: Bundle?) {
      ...
      }
  ```
  
  - onCreate() 메소드는 액티비티에서 일회성의 초기화가 이루어져아 한다. 예를 들어 onCreate()에서 layout을 inflate 시키고, click listener와 data binding을 정의할 수 있다
  
  <br>
  
  ### Step 2: Implement the onStart()
   - onStart()는 onCreate()가 호출된 직후 불린다. onStart()가 실행되면 activity가 screen에 나타난다
   - onCreate()는 액티비티를 초기화하기 위해 한번만 호출되지만 onStart()는 여러번 호출될 수 있다.
   - onStart()는 onStop()과 쌍을 이루는데, 사용자가 앱을 시작한 다음 기기의 홈 화면으로 돌아가면 activity가 중지되고 더 이상 화면에 표시되지 않는다.
   
   ##### 1. 안드로이드 스튜디오에서 MainActivity.kt를 열고 ctrl+o를 눌러 오버라이드 할 수 있는 메소드들의 리스트를 살펴본다
   
   ```
   override fun onStart() {
      super.onStart()
   }
   ```
   
   ##### 2. compile 후 앱을 실행시키고 로그를 살펴본다
   
   - onCreate()와 onStart() 메소드가 차례로 호출되고 activity가 화면에 표시된다
   - 홈 버튼을 눌러서 나갔다가 다시 activity로 들어오면 activity가 중단된 곳에서 다시 시작된다. onStart()는 Logcat에 2번 찍히지만 onCreate()는 다시 호출되지 않는 것을 볼 수 있다.
   
   ```
   I/MainActivity: onCreate Called
   I/MainActivity: onStart Called
   I/MainActivity: onStart Called
   I/MainActivity: onStart Called
   I/MainActivity: onStart Called
   ```

<br><br>

## 2. Use Timeber for logging
 - **Timber**는 유명한 로깅 라이브러리이다
 - Timber는 안드로이드 내장 Log 클래스와 비교하여 몇가지 장점이 있다
    
   - 클래스 이름으로 로그 태그를 생성한다
   - 앱의 릴리즈 버전에서는 로그를 표시하지 않도록 도와준다
   - crash-reporting 라이브러리와 통합할 수 있다
   
   
 ### Step 1: Add Timber to Gradle
  - [Timber project - github](https://github.com/JakeWharton/timber#download)의 README.md 내용 중 Download에서 버전 정보를 참고한다
  
  
  ```
    implementation 'com.jakewharton.timber:timber:4.7.1'
  ```
  
  - build.gradle(Module:app)을 열어서 dependencies 영역에 추가한다.
  
 <br>
 
 ### Step 2: Create an Application class and initialize Timber
  - 이번 단계에서는 Application을 만든다. Application은 global 어플리케이션의 상태를 포함하는 기본 클래스이다
  - 또한 운영 체제가 앱과 상호 작용하기 위해 사용하는 주요 객체이다
  - 따로 지정하지 않으면 Android에서 사용하는 기본 Application 클래스가 제공되므로 앱을 만들기 위해 특별한 작업을 수행할 필요 없이 항상 앱에 대해 생성된 Application 객체가 있다
  - 전체 앱에서 이 로깅 라이브러리를 사용하고 다른 모든 것이 설정되기 전에 라이브러리를 한번 초기화해야 하므로 Timber는 Application 클래스를 사용한다
  - (액티비티 코드는 Application 클래스에 넣지 않는다.)
  
  - Application 클래스를 만들고 나면 Android manifest에 클래스를 지정해야 된다
    
    ##### 1. ClickerApplication 이라는 새로운 클래스를 만든다.
    
        ```
         package com.example.android.lifecycles
         
         class ClickerApplication {
         }
         
        ```
    
    ##### 2. ClickerApplication을 Application의 하위 클래스로 변경한다
    
        ```
         class ClickerApplication : Application() {
         
         }
        ```
    
    ##### 3. onCreate() 메소드를 오버라이드 한다
    
        ```
         class ClickerApplication : Application() {
            
            override fun onCreate() {
                super.onCreate()
            }
         }
        ```
    
    ##### 4. onCreate() 메소드 안에서 Timber 라이브러리를 초기화한다.
     - Timber 라이브러리를 Application에서 초기화하면 activity에서 Timber 라이브러리를 사용할 수 있다
     
        ```
        class ClickerApplication : Application() {
        
            override fun onCreate() {
                super.onCreate()
        
                Timber.plant(Timber.DebugTree())
            }
        }
        ```
    
    ##### 5. AndroidManifest.xml을 열어서 \<application\> 요소에 ClickerApplication 클래스를 추가한다.
        ```
        <application
           android:name=".ClickerApplication"
        ...
        ```
 
 <br>
    
 ### Step 3: Add Timber log statements
  - Log.i() 대신에 Timber 라이브러리를 사용하도록 변경한다.
  
    ##### 1. MainActivity의 onCreate() 메소드에서 Log.i()를 Timber.i()로 변경한다.
      - Timber는 log tag가 필요하지 않고, 클래스의 이름을 자동으로 tag로 처리한다. 
      
        ```
        Timber.i("onCreate called")
        ```
    
    ##### 2. onStart()의 Log도 변경한다.
    
    ##### 3. 남아있는 다른 lifecycle 메소드로 오버라이드 하여 Timber 로그를 추가한다.
    
    ```
       override fun onResume() {
          super.onResume()
          Timber.i("onResume Called")
       }
       
       override fun onPause() {
          super.onPause()
          Timber.i("onPause Called")
       }
       
       override fun onStop() {
          super.onStop()
          Timber.i("onStop Called")
       }
       
       override fun onDestroy() {
          super.onDestroy()
          Timber.i("onDestroy Called")
       }
       
       override fun onRestart() {
          super.onRestart()
          Timber.i("onRestart Called")
       } 
    ```
  
  <br>
  
  - 액티비티가 시작되면 다음 세가지 라이프 사이클 콜백이 호출된 것을 볼 수 있다
    - onCreate(): to create the app
    - onStart(): to start it and make it visible on the screen.
    - onResume(): to give the activity focus and make it ready for the user to interact with it.

<br><br>

## 3. Explore lifecycle use cases
 ### Use case 1: Opening and closing the activity
  - 앱을 처음 시작한 다음 완전히 종료한다
  
  ##### 1. 앱을 처음 실행될 때 onCreate(), onStart(), onResume() 콜백이 호출된다.
  
  ##### 2. 컵케이크를 몇번 탭한다.
  
  ##### 3. 백 버튼을 누르면 onPause(), onStop(), onDestroy()가 차례로 호출된다.
   - 이 경우 백 버튼을 사용하면 activity(앱)이 완전히 종료된다.
   - onDestroy() 메소드는 activity가 완전히 종료될 수 있으 가비지 컬렉터에 의해 수집될 수 있음을 의미한다
   - 액티비티는 코드에서 finish()를 호출하거나 사용자가 앱을 끌 경우 완전히 종료된다.
   - 또한 앱이 오랫동안 화면에 표시되지 않을 경우 android 시스템에서 activity를 자체적으로 종료할 수도 있다. 안드로이드에서는 배터리를 보호하고 다른 앱에서 앱 리소스를 사용할 수 있도록 이 작업을 수행한다.