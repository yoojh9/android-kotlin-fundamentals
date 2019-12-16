# 03-2 Navigation

## 1. Add navigation components to the project

### 1. Add navigation dependencies
 - navigation 라이브러리를 사용하기 위해 gradle file에 navigation 라이브러리를 추가한다
 
 ##### (1) project 레벨의 build.gradle 파일을 열어서 ext 변수와 함께 navigationVersion에 대한 변수를 추가한다
 
   - 최신의 navigation 버전을 알고 싶으면, [Declaring dependencies](https://developer.android.com/jetpack/androidx/releases/navigation#declaring_dependencies) 참고
   
    ext {
        ...
        navigationVersion = '1.0.0.-rc02'
        ...
    }
    
 ##### (2) app 레벨의 build.gradle 파일을 열어 navigation-fragment-ktx와 navigation-ui-ktx 라이브러리를 추가한다
  
    dependencies {
      ...
      implementation"android.arch.navigation:navigation-fragment-ktx:$navigationVersion"
      implementation "android.arch.navigation:navigation-ui-ktx:$navigationVersion"
      ...
    }
    
<br><br>

## 2. Add a navigation graph to the project
 - res 폴더에서 오른쪽 클릭 후 New > Android Resource File
 - New Resource File 다이얼로그에서 Resource type을 Navigation으로 선택한다
 - 생성하면 res > navigation 폴더 아래에 navigation.xml이 생성된다.
 - navigation.xml에서 Design 탭을 눌러 Navigation Editor를 열면 'No NavHostFragment found' 메세지가 나온다. 이는 다음 단계에서 해결할 수 있다.
 

<br><br>

## 3. Create the NavHostFragment
 - navigation host fragment는 navigation graph를 포함하는 host fragment 역할을 한다
 - 사용자가 navigation graph에 정의된 대상간에 이동하면 NavHostFragment는 필요에 따라 fragment를 교환한다.
 - NavHostFragment는 적절한 fragment back stack을 생성하고 관리한다
 
 - app:defaultNavHost 속성을 true로 설정하면 NavHostFragment가 default host가 되고 system Back Button을 가로챈다
 
 ```
    <fragment
        android:id="@+id/myNavHostFragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:navGraph="@navigation/navigation"
        app:defaultNavHost="true" />
 ```
 
<br><br>

## 4. Add fragment to the navigation graph
 ### Step 1: Add two fragments to the navigation graph and connect them with an action
  
  ##### 1) navigation.xml을 열어 navigation Editor에서 New Destination 버튼을 선택한다.
  
  ##### 2) 앱이 실행되자마자 TitleFragment를 사용자에게 보여주고 싶으므로 fragment_title을 처음으로 선택한다. 
  
  ##### 3) new destination 버튼을 눌러서 GameFragment도 추가한다
  
  ##### 4) preview 화면에서 title fragment의 pointer를 game fragment preview에 연결한다. 이 때 두 프래그먼트를 연결하는 action이 만들어진다.
  
   <img src="./images/connect_fragment.png"  width="60%" height="60%">
   
 <br>
  
 ### Step 2: Add a click handler to the play button
 - title fragment와 game fragment가 action에 의해 연결되었다. Play 버튼을 눌렀을 때 game screen으로 사용자를 이동시키고 싶다.
 
 ##### 1) TitleFragment.kt의 onCreateView() 메소드에 return 문장 전에 아래 코드를 추가한다
 
 ```
   binding.playButton.setOnClickListner{}
 ```
 
 ##### 2) SetOnClickListener() 내부에서 바인딩 클래스를 통해 playButton에 액세스하고 game fragment로 이동하는 코드를 추가한다
 
 ```
   binding.playButton.setOnClickListner { view: View ->
        view.findNavController().navigate(R.id.action_titleFragment_to_gameFragment)
   }
 ```

<br><br>

## 5. Add conditional navigation
 - 특정 조건에 따라 화면에 다르게 보이는 navigation도 만들 수 있다.
 - conditinal navigation의 일반적인 사용 사례는 사용자의 로그인 여부에 따라 앱의 흐름이 다른 경우이다.
 - 이번 예쩨에서는 사용자가 모든 질문에 올바르게 대답했는지에 따라 fragment를 분기시킨다.
    - GameWonFragment는 스크린에 "Congratulations!" 메세지를 나타낸다
    - GameOverFramgnet는 스크린에 "Try Again!" 메세지를 나타낸다.
 
 ### Step 1: Add GameWonFragment and GameOverFragment to the navigation graph
 
  - navigation.xml 파일을 열고 New Destination 버튼을 클릭한 후 fragment_game_over와 fragment_game_won 을 추가한다

 <img src="./images/conditional_navigation.png"  width="60%" height="60%">
 
 <br>
 
 ### Step 2: Connect the game fragment to the game-result fragment
 
 - Layout Editor의 preview 영역에서 GameFragment를 GameOverFragment와 GameWonFragment에 각각 연결시킨다.
 
  <img src="./images/connect_conditional_navigation.png"  width="60%" height="60%">
 

  <br>
 
 ### Step 3: Add code to navigate from one fragment to the next
 
 - GameFragment.kt의 onCreateView()에 조건에 따라 navigate 하는 함수를 추가한다
 
 ```
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            ...
            
            if(answers[answerIndex] == currentQuestion.answers[0]) {
                questionIndex++
                
                // Advance to the next question
                if(questionIndex < numQuestions) {
                      
                } else {
                    // GameWonFragment
                    view.findNavController().navigate(R.id.action_gameFragment_to_gameWonFragment)
                }
            } else {
                // GameOverFragment
                view.findNavController().navigate(R.id.action_gameFragment_to_gameOverFragment)
            }
        }

        return binding.root
    }
 ```

<br><br>

## 6. Change the Back button's destination
 - 안드로이드 시스템은 사용자의 화면 이동을 추적한다
 
 - 사용자가 새로운 destination에 도달할 때 마다 Android는 해당 destination을 back stack에 추가한다
 
 - 사용자가 back 버튼을 누르면 백 스택의 맨 위에 있는 대상으로 이동한다
 
 - 보통 기본적으로 백 스택의 상단은 사용자가 마지막으로 본 화면이다
 
 - 하지만 현재 예제 앱에서는 GameOverFragment나 GameWonFragment 화면에서 백 버튼을 눌렀을 경우 GameFragment로 이동하는데, 더 나은 동작을 위해서는 GameFragment가 아닌 TitleFragment로 이동해야 한다.
 
 ### Step 1: Set the pop behavior for the navigation actions
 - 사용자가 GameWon 또는 GameOver 화면에 있을 때 백 버튼을 누르면 타이틀 화면으로 돌아가도록 back stack을 관리해야된다.
 - 프래그먼트를 연결하는 액션에 대해 'pop' 동작을 설정하여 back stack을 관리한다
    
    - **popUpTo** : navigating 전에 지정된 destination으로 백 스택을 "pops up"한다
    
    - **popUpToInclusive = false** : popUpToInclusive 속성이 false 이거나 설정되지 않으면 popUpTo는 지정된 destination까지 모든 destination을 지운다. 그러나 지정된 destination은 백 스택에 남겨둔다
    
    - **popUpToInclusive = true** : popUpToInclusive 속성이 true이면 popUpTo 속성은 백스택에 주어진 destination까지 포함하여 지운다
    
    - popUpToInclusive가 true이고 popUpTo가 앱의 시작 화면으로 설정된 경우에는 앱의 백스택에 있는 모든 destination을 지우므로, 백 버튼은 사용자를 앱에서 완전히 빠져 나오게 한다.
 
 <br>
 
 - 레이아웃 편집기에서 속성들 중 **Pop To** 필드를 사용하여 PopUpTo 속성을 설정할 수 있다
    
    ##### 1) navigation.xml에서 gameFragment와 gameOverFragment를 연결하는 action을 선택한다.
    ##### 2) Attributes 창에서 **Pop To**를 gameFragment로 설정하고, **inclusive** 체크박스를 선택한다.
 
    
        <img src="./images/pop_behavior_1.png"  width="50%" height="50%"/>
      
    
    - 이 속성은 navigation component에 백 스택에서 GameFragment를 포함한 fragment를 제거하도록 지시한다.
    - 이 동작은 **Pop To** 필드에 titleFragment를 설정하고 **Inclusive** 체크박스를 해제하는 것과 같다
    
    
    ##### 3) gameFragment와 gameWonFragment를 연결하는 action을 선택한다
    ##### 4) **Pop To**에 gameFragment를 설정하고 **inclusive** 체크박스를 선택한다.
 
 <br>
 
 ### Step 2: Add more navigation actions and add onClick handlers
  - 사용자가 **Next Match** 또는 **Try Again** 버튼을 눌렀을 경우 GameFragment 화면으로 연결시킨다.
  - 이동한 GameFragment 화면에서는 백 버튼 선택 시 GameWon이나 GameOver 화면이 아닌 TitleFragment 화면으로 이동해야 한다.
  
    ##### 1) navigation.xml에서 gameOverFragment에서 gameFragment로 연결하는 action을 추가한다.
    ##### 2) Attributes 창에서 Pop To 속성을 titleFragment로 설정하고, Inclusive 체크를 해제한다. (titleFragment 까지의 모든 것을 백스택에서 제거한다)
  
    <img src="./images/pop_behavior_2.png"  width="50%" height="50%">
  
    ##### 3) navigation.xml에서 gameWonFragment와 gameFragment를 연결하는 action을 추가한다.
    ##### 4) 2)번의 작업을 반복한다.
   
  <br>  
    
  - **Try Again** 과 **Next Match** 버튼을 눌렀을 때 GameFragment로 이동하는 기능을 추가한다
  
    ##### 1) GameOverFragment.kt 파일에서 onCreateView() 메소드 끝에 return문 직전에 아래 코드를 추가한다.
  
  
     ```
     // Add onClick Handler for Try Again button
        binding.tryAgainButton.setOnClickListner { view: View -> 
                view.findNavController()
                    .naigate(R.id.action_gameOverFragment_to_gameFragment) }
     ```
     
     
    ##### 2) GameWonFragment.kt 파일을 열어서  onCreateView() 메소드 끝에 return문장 전에 아래 코드를 추가한다
    
    ```
    // Add OnClick Handler for Next Match button
            binding.nextMatchButton.setOnClickListener{view: View->
                view.findNavController()
                        .navigate(R.id.action_gameWonFragment_to_gameFragment)}
                        
    ```
    
    ##### 3) 앱을 실행시키면 Next Match와 Try Again 버튼을 눌렀을 때 game을 다시 할 수 있는 game screen으로 이동하는 것을 확인할 수 있다
    ##### 4) Next Match와 Try Again 버튼을 누른 후 시스템 백 버튼을 누르면, 이전 화면이 아닌 titleFragment로 이동하는 것을 확인할 수 있다.

<br><br>    

## 7. Add an Up button in the app bar
 ### 1. The app bar
  - app bar는 action bar라고 불리기도 하며 option menu와 같은 메뉴를 지원한다
 
 ### 2. The Up button
  - 안드로이드는 app bar의 왼쪽 상단에 버튼을 추가할 수 있다
  
  #### Up button vs Back button
    - Up button은 app bar에 나타난다 (스크린샷 1번)
    - Up button은 화면 간 계층 관계를 기반으로 앱 내에서 이동하며, 절대로 앱 밖으로 나가지 않는다
    - Back button은 시스템의 네비게이션 바 또는 디바이스 자체의 버튼이다.
    - Back button은 사용자가 최근에 작업 한 화면(백스택)을 기준으로 이동한다
    
   <img src="./images/backbutton_vs_upbutton.png"  width="50%" height="50%"/>
 
 <br>
 
 ### 3. Add support for an Up button
  - navigation은 NavigationUI라고 불리는 UI 라이브러리를 포함한다.
  - navigation controller는 앱 바와 통합되어 Up 버튼의 동작을 구현하므로 직접 할 필요가 없다
  
  
  ##### 1) MainActivity.kt의 onCreate() 안에서 navigation controller 객체를 찾는 코드를 추가한다
  
  ```
    val navController = this.findNavController(R.id.myNavHostFragment)
  ```
  
  ##### 2) onCreate() 메소드에 navigation controller와 app bar를 연결하는 코드를 추가한다
  
  ```
    NavigationUI.setupActionBarWithNavController(this, navController)
  ```
  
  ##### 3) onSupportNavigateUp() 메소드를 오버라이드 하여 navigateUp() 메소드를 호출한다
  
  ```
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return myController.navigateUp()
    }
  ```
  
  ##### 4) 앱을 실행하고 app bar에 Up button이 나타나는지 확인한다. Up button을 누르면 어디에 위치하든 title 화면으로 이동한다.


<br><br>    

## 8. Add an options menu
 - Android에는 옵션 메뉴를 포함하여 다양한 메뉴들이 있다.
 - 이번 과정에서는 옵션 메뉴에 **About** 메뉴를 추가하여 AboutFragment로 이동시키는 기능을 만든다
 
 ### Step 1: Add the AboutFragment to the navigation graph
   
   ##### 1) navigation.xml 파일을 열어 Design 탭을 선택한다
   
   ##### 2) New Destination 버튼을 눌러서 fragment_about을 선택한다
 
 <br>
 
 ### Step 2: Add the options-menu resource 
   
   ##### 1) res 폴더를 오른쪽 클릭하여 New > Android Resource File을 선택한다
   
   ##### 2) New Resource File 다이얼로그에서 file name을 options_menu로 작성한다
   
   ##### 3) Resource type 을 Menu로 선택하고 OK를 누른다
   
   ##### 4) options_menu.xml 파일을 열어서 Design 탭을 눌러 Layout Editor를 실행시킨다
   
   ##### 5) Palette 창에서 Menu Item을 드래그 하여 design editor 창으로 드롭하면 메뉴 아이템이 생긴다.
   
   ##### 6) menu item의 id를 abountFragment로 설정하고 title을 @string/about으로 지정한다
   
  <br>
  
  ### Step 3: Add an onClick handler
   - About 메뉴에 사용자가 탭 했을 때의 동작을 구현하는 코드를 추가한다
   
   ##### 1) TitleFragment.kt를 열어서 onCreateView() 메소드 내 return 문장 전에 setHasOptionsMenu() 메소드를 호출하고 true를 전달한다
   
   ```
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
        ...
        setHasOptionsMenu(true)
        return binding.root                        
    }
   ```
   
   ##### 2) onCreateOptionsMenu() 메소드를 오버라이드 하고 이 메소드에서 options menu를 추가하고 menu resource file을 inflate한다.
   
   ```
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.options_menu, menu)
    }
   ```
   
   ##### 3) onOptionsItemSelected() 메소드를 오버라이드 하고 메뉴 아이템을 탭 했을 때 적절한 행동을 취하도록 작성한다
   
   ```
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return NavigationUI.onNavDestinationSelected(item!!,
            view!!.findNavController())
            || super.onOptiosItemSelected(item)
    }
   ```
   
<br><br>

## 9. Add an Navigation drawer
 - navigation drawer는 화면 가장자리에서 미끄러져 내려오는 창이다.
 
 - drawer에는 일반적으로 헤더와 메뉴가 있다.
 
 - 핸드폰 사이즈의 디바이스에서는 navigation drawer는 사용하지 않을 때 가려져 있다. 사용자의 action에 따라 navigation drawwer를 나타나게 하는 두가지 방식이 있다.
    - 사용자가 왼쪽에서 오른쪽으로 스와이프 했을 때 나타난다
    - app bar 내의 drawer icon을 탭 했을 때 나타난다. drawer icon은 nav drawer buggon 또는 hamburger icon으로 불린다.
 
 - 이번 예제에서는 navigation drawer에 'about' 메뉴와 'rules' 메뉴를 추가한다.

<br> 

 ### Step 1: Add the Material library to your project
 
 ```
    // app-level gradle build file에 Material libarary dependency 추가
    dependencies {
        ...
        implementation "com.google.android.material:material:$supportlibVersion"
        ...
    }
 ```
 
 ### Step 2: Make sure the destination fragments have IDs
  - navigation graph 내에서 두가지 destination 모두 ID 를 가지고 있는지 확인한다.
  
  
 ### Step 3: Create the drawer menu and the drawer layout
  - navigation drawer을 만드려면 먼저 navigation menu를 생성하고 view를 DrawerLayout 안에 넣어야 한다.
  
  ##### 1) res 폴더에서 오른쪽 클릭하여 New Resource File을 선택하여 resource type은 Menu로 설정 후 생성한다
  
  ##### 2) res > menu > navdrawer_menu.xml의 Design 탭에서 menu item 2개를 추가한다.
  
  ##### 3) 첫번째 메뉴는 id: ruleFragment, title: @string/rules, icon: @drawable/rules로 설정한다
  
  ##### 4) 두번째 메뉴는 id: abountFragment, title: @string/abount, icon: @drawable/about_android_trivia로 설정한다
  
  ##### 5) activity_main.xml 에서 DrawerLayout 안에 drawer를 추가한
 
  ```
    <layout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:app="http://schemas.android.com/apk/res-auto">
       <androidx.drawerlayout.widget.DrawerLayout
           android:id="@+id/drawerLayout"
           android:layout_width="match_parent"
           android:layout_height="match_parent">
    
           <LinearLayout
            . . .
           </LinearLayout>
       </androidx.drawerlayout.widget.DrawerLayout>
    </layout>
  ```
  
  ##### 6) \<\/LinearLayout\> 요소 이후에 navdrawer_menu를 사용하는 NavigationView를 추가한다
  
  ```
  <com.google.android.material.navigation.NavigationView
     android:id="@+id/navView"
     android:layout_width="wrap_content"
     android:layout_height="match_parent"
     android:layout_gravity="start"
     app:headerLayout="@layout/nav_header"
     app:menu="@menu/navdrawer_menu" />
  ```
  
 <br>
  
 ### Step 4: Display the navigation drawer
  - 위에서 만든 navigation drawer와 navigation controller를 연결해야한다.
  
  ##### 1) MainActivity.kt의 onCreate() 메소드에서 navigation drawer를 사용할 수 있는 코드를 추가한다. 
 
  
  ```
    NavigationUI.setupWithNavController(binding.navView, navController)
  ```
  
   - 앱을 실행하면 왼쪽에서 오른쪽으로 swipe 할 때 navigation drawer가 나타난다. 하지만 app bar의 drawer 버튼을 탭 했을 때의 액션은 아직 추가되어 있지 않다
 
 <br> 
    
 ### Step 5: Display the navigation drawer from the drawer button
  
  ##### 1) MainActivity.kt에 lateinit drawerLayout을 추가한다.
  
  ```
    private lateinit var drawerLayout: DrawerLayout
  ```
  
  ##### 2) onCreate() 메소드에서 binding 변수가 초기화 된 후 drawerLayout을 초기화 시키는 코드를 추가한다.
  
  ```
    val binding = DataBindingUtil.setContentView<AcitivyMainBinding>(this, R.layout.activity_main)
    
    drawerLayout = binding.drawerLayout
  ```
  
  ##### 3) 기존에 만들었던 setupActionBarWithNavController()에 3번째 파라미터로 drawerLayout을 전달한다.
  
  ```
    NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
  ```
 
  ##### 4) onSupportNavigateUp() 메소드에서 NavController.navigateUp() 코드 대신  NavigationUI.navigationUp()을 리턴하는 코드로 변경한다.
    
   - navigationUp()은 navigation controller와 drawer layout을 전달한다.
    
  ```
    override fun onSupportNavigateUp(): Boolean {
       val navController = this.findNavController(R.id.myNavHostFragment)
       return NavigationUI.navigateUp(navController, drawerLayout)
    }
  ```
  
  ##### 5) 앱을 실행시킨 후 왼쪽에서 오른쪽으로 스와이프 하거나, drawer button을 눌러 drawer menu가 나오는지 확인한다.
