# 03-3. ExternalActivity

## 1. Set up and use the Safe Args plugin
 - 사용자가 앱 내에서 데이터를 공유하려면 액티비티에 있는 파라미터를 다른 액티비티로 전달해야 한다
 - 이런 트랜잭션에서 버그를 예방하고 type-safe를 유지하려면 Gradle 플러그인인 Safe Args를 사용해라.
 - 플러그인은 NavDirection 클래스를 생성하는데 이 클래스를 코드에 추가한다
 

 ### Why you need the Safe Args plugin
  - 앱은 종종 프래그먼트끼리 데이터를 주고 받기를 원한다. 데이터를 전달하는 방법 중 하나는 Bundle 객체를 사용하는 것인데, 안드로이드 Bundle은 key-value 저장소이다
  - 키-밸류 저장소는 고유키(문자열)를 사용하여 해당 키와 연관된 값을 가져오는 데이터 구조이다
  - Bundle을 사용하여 fragmentA에서 fragmentB로 데이터를 전달할 수 있는데, fragmentA에서 key-value 쌍의 데이터를 저장한 Bundle을 만들면 fragmentB에서 Bundle 객체로부터 key-value 데이터를 얻을 수 있다
  - 그러나 Bundle은 코드가 컴파일 되더라도 앱이 실행 될  오류가 발생할 가능성이 있다
  
  - 발생할 수 있는 오류는 아래와 같다
    - **type mis-match error** : 만약 A 프래그먼트가 string으로 보내고 B 프래그먼트가 bundle에서 integer로 요청할 경우, 해당 요청은 default 값으로 0를 리턴한다. 0가 유효한 값이므로 앱이 컴파일 될 때는 mis-match type 문제가 발생하지 않지만 앱이 실행될 때 오류로 인하여 앱이 잘못 작동되거나 중단될 수 있다
    - **Missing key errors**: B 프래그먼트에서 bundle에 저장되어 있지 않은 argument를 요청할 경우 null을 리턴해준다. 앱을 컴파일 할 때 오류가 발생하지 않더라도 사용자가 앱을 실행할 때 문제가 발생할 수 있다
   
  - 당연한 이야기지만 productio에 배포하기 전에 에러를 잡기 위해 컴파일 시점에서 에러를 발견하고 싶을 것이다.
  - 이를 위해 안드로이드 Navigation Architecture Component애는 **Safe Args* 기능이 있다
  - Safe Args는 컴파일 시점에서 에러를 발견하는 코드나 클래스를 생성해주는 Gradle plugin이다
  
  <br>
  
  #### Step 1: Add Safe Args to the project
  
   ##### 1) 프로젝트 레벨 build.gradle 파일을 열어서 navigation-safe-args-gradle-plugin 디펜던시를 추가한다
   
      ```
        // project-level build.gradle
        dependencies {
            ...
            classpath "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion"
        }
      ```
   ##### 2) app 레벨 build.gradle에 apply plugin 문장을 추가한다
    
    ```
        apply plugin: 'androidx.navigation.safeargs'
    ```    
      
   ##### 3) 프로젝트를 reBuild 하면 앱 프로젝트에는 이제 생성된 NavDirection 클래스가 포함된다
    
    - Safe Args 플러그인은 각 fragment에 대해 NavDirection 클래스를 생성한다
    - NavDirection 클래스는 앱의 모든 action에 대해 navigation을 나타낸다
    - 예를 들어 GameFragment는 GameFragmentDirection 클래스가 생성되고, GameFragmentDirection 클래스로 type-safe한 argument를 game fragment에서 다른 fragment에 전달할 수 있다
    - generated 된 파일을 보려면 **generatedJava** 폴더를 참고한다.
   
  <br>
   
  #### Step 2: Add a NavDirection class to the game fragment
   
   ##### 1) GameFragment.kt를 열어서 onCreateView() 메소드 내에 NavController.navigate() 메소드의 파라미터를 변경한다.
   
   ##### 2) gameWonFragment의 action ID를 GameFragmentDirections 클래스의 actionGameFragmentToGameWonFragment() 메소드를 사용하는 action ID로 바꾼다
   
   ```
    // Using directions to navigate to the GameWonFragment
    view.findNavController()
        .navigate(GameFragmentDirections.actionGameFragmentToGameWonFragment())
   ```
   
   ##### 3) 위와 같이 gameOverFragment도 action ID를 GameFragmentDirections 클래스의 game over 메소드를 사용하는 ID로 바꾼다.
   ```
    // Using directions to navigate to the GameOverFragment
    view.findNavController()
        .navigate(GameFragmentDirections.actionGameFragmentToGameOverFragment())
   ```

<br><br>

## 2. Add and pass arguments
 - gameWonFragment에서 gameFragmentDirection의 메소드로 argument를 전달한다.
 
 ### Step 1: Add arguments to the game-won fragment
 
   ##### 1) navigation.xml에서 Design탭을 눌러 navigation graph를 연다.
   ##### 2) preview에서 gameWonFragment를 선택한다.
   ##### 3) Attributes 창에서 Argument 영역을 확대하고 + 아이콘을 눌러 name이 numQuestions이고 type이 Integer인 argument를 추가한다
   ##### 4) 마찬가지로 두번째 argument로 name이 numCorrect이고  타입이 Integer인 argument를 추가한다
    
   - 이 상태에서 app을 빌드할 경우 compile error가 난다.
    
    ```
       No value passed for parameter 'numQuestions'
       No value passed for parameter 'numCorrect'
    ```
    
  
  ### Step 2: Pass the arguments
   - 이번 단계에서는 numQuetions과 questionIndex 인수를 GameFragmentDirections 클래스의 actionGameFragmentToGameWonFragment()로 전달한다.
   
   ##### 1) GameFragment.kt를 열어 아래와 같이 numQuestions와 questionIndex 인수를 추가한다
   
   ```
    // before
     view.findNavController()
          .navigate(GameFragmentDirections
                .actionGameFragmentToGameWonFragment())
                
    // after
    view.findNavController()
          .navigate(GameFragmentDirections
                .actionGameFragmentToGameWonFragment(numQuestions, questionIndex))            
   ```
   
   ##### 2) GameWonFragment.kt에서 Bundle 객체로부터 argument들을 추출한다. 
   
   ```
    val args = GameWonFragmentArgs.fromBundle(arguments!!)
    Toast.makeText(context, "NumCorrect: ${args.numCorrect}, NumQuestions: ${args.numQuestions}", Toast.LENGTH_LONG).show()
   ```
   
 <br>
 
  ### Step 3: Replace fragment classes with NavDirection classes
   - "safe arguments"를 사용하려면 fragment 클래스를 NavDirection 클래스를 이용하여 변경해야 한다.
   - 이제 다른 fragment에 type-safe arguments를 사용할 수 있다
   
   - TitleFragment, GameOverFragment, GameWonFragment의 navigate() 메소드에서 전달하는 action id를 NavDirection 클래스의 action id로 변경한다.
   
   ##### 1) TitleFragment.kt를 열어서 onCreateView() 내에 위치한 navigate() 메소드의 action id를 아래와 같이 바꾼다
   
   ```
    // view.findNavController().navigate(R.id.action_titleFragment_to_gameFragment) // before
    view.findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToGameFragment())
   ```
   
   ##### 2) GameOverFragment.kt의 Try Again과 GameWonFragment의 Next Match 버튼 클릭 핸들러의 action id도 동일하게 변경한다.
   
<br><br>

## 3. Add an implicit intent and a "share" menu item
 - 게임 결과를 전달하기 위해 암시적 인텐트를 사용하여 sharing 기능을 추가할 수 있다
 - GameWonFragment 클래스 내에 options menu를 공유 기능 메뉴로 구현할 수 있다
 
 <br>
 
 ### Implicit intents
  - 지금까지는 네비게이션 구성 요소를 사용하여 activity 내 fragment 간의 이동을 구현했다
  
  - 안드로이드는 intent를 사용하여 다른 앱에서 제공하는 activity로 이동을 허용한다.
  
  - 이번 예제에서는 사용자가 게임 플레이 결과를 공유할 수 있는 기능을 추가한다.
  
  - **Intent**는 android component간 커뮤니케이션을 위해 사용되는 메세지 객체이다.
  
  - **implicit intent**는 어떤 앱이나 액티비티가 작업을 처리할지 몰라도 activity를 시작할 수 있다. 예를 들어 사진앱의 경우 다수의 android app이 같은 implicit intent를 처리할 수 있다. 안드로이드는 사용자에게 chooser를 보여주며, 사용자는 요청을 처리할 앱을 선택한다
  
  - 각각의 implicit intent는 수행할 작업의 유형을 설명하 ACTION을 가지고 있어야 된다. 보통 action에는 ACTION_VIEW, ACTION_EDIT, ACTION_DIAL 등이 있다.
  
  <br>
  
  #### Step 1: Add an options menu to the Congratulations Screen
   - GameWonFragment.kt 파일을 연다
   - onCreateView() 메소드에서 return 문 전에 setHasOptionsMenu(true)를 설정한다
   
   ```
    setHasOptionsMenu(true)
   ```
  
  #### Step 2: Build and call an implicit intent
   - 사용자의 게임 데이터를 전달하는 intent를 만드는 코드를 추가한다.
   - 여러 앱이 ACTION_SEND 인텐트를 처리할 수 있으므로 chooser가 사용자에게 띄어질 것이다.
   
   ##### 1. GameWonFragment의 onCreateView()에서 ACTION_SEND 인텐트로 공유할 메세제를 전달할 수 있다.
   ```
        // Creating our Share Intent
        private fun getShareIntent(): Intent {
            val args = GameWonFragmentArgs.fromBundle(arguments!!)
            val shareIntent = Intent(Intent.ACTION_SEND)
    
            shareIntent.setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, getString(R.string.share_success_text, args.numCorrect, args.numQuestions))
    
            return shareIntent
        }
   ```
   
   ##### 2. startActivity()를 호출하여 intent를 얻는 메소드를 만든다
   ```
        private fun shareSuccess(){
            startActivity(getShareIntent())
        }
   ```
   
   ##### 3. onCreateOptionsMenu()를 오버라이드 하여 winner_menu에 inflate 시킨다.
    
   - getShareIntent()를 사용하여 shareIntent를 얻는다. 
   
   - ShareIntent가 Activity로 변환(resolve)되는지 확인하기 위해 Android package manager를 사용한다.
   
   - Android Package Manager는 기기에 설치된 앱 및 액티비티들을 추적한다.
   
   - resolveActivity()가 null 이면 shareIntent가 resolve 되지 않은 것이므로, sharing menu를 invisible 시킨다
   
   ```
       // Showing the Share Menu Item Dynamically
       override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
          super.onCreateOptionsMenu(menu, inflater)
          inflater?.inflate(R.menu.winner_menu, menu)
          // check if the activity resolves
          if (null == getShareIntent().resolveActivity(activity!!.packageManager)) {
              // hide the menu item if it doesn't resolve
              menu?.findItem(R.id.share)?.setVisible(false)
          }
       }
   ``` 
   
   ##### 4. menu item을 처리하기 위해 onOptionsItemSelected() 메소드를 오버라이드 한다. share menu가 눌릴 경우 shareSuccess()를 호출하는 메소드를 추가한다
   
   ```
       // Sharing from the Menu
       override fun onOptionsItemSelected(item: MenuItem?): Boolean {
          when (item!!.itemId) {
              R.id.share -> shareSuccess()
          }
          return super.onOptionsItemSelected(item)
       }
   ```
   