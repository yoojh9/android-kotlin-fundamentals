
# 1. Fragment

## 1. Add a Fragment
 - Fragment는 Activity에서 사용자 인터페이스(UI)의 일부를 나타낸다
 - 하나의 액티비티에서 여러 fragment를 결합하여 다중 창 UI를 구성하고 여러 액티비티들에서 fragment를 재사용 할 수 있다
    
    - fragment는 자체 생명 주기가 있으며 자체 input event를 수신한다
    - 액티비티가 실행되는 동안 fragment를 추가 삭제 할 수 있다

<br><br>

### 1. Create a binding object
  - 프래그먼트를 컴파일 하려면 binding object를 생성하고 fragment의 뷰를 inflicate 시켜야 한다 (activity의 setContentView()와 동일)
  
 <br>
 
 ##### 1. TitleFragment.kr의 onCreateView() 메소드에서 binding 변수를 생성한다
 
 ##### 2. fragment 뷰를 inflicate 하기 위해, DataBindingUtil.inflicate() 메소드를 호출한다 
 
  - 메소드에 다음 4가지 파라미터를 넘긴다
    
    - inflater : LayoutInflater로 binding한 레이아웃을 inflate 하는데 사용된다
    - inflate 할 XML layout resoure (ex. R.layout.fragment_title)
    - container : 부모 ViewGroup의 container (이 파라미터는 optional이다)
    - false : attachToParent의 값
    
 ##### 3. DataBindingUtil.inflate가 반환하는 binding을 binding 변수에 할당한다
 
 ##### 4. 메소드에서 inflate된 뷰를 포함하고 있는 binding.root를 리턴한다.
 
 ```
    // onCreateView()에서 fragment inflate
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FramgnetTitleBinding>(inflater, R.layout.fragment_title, container, false)
        return binding.root    
    }
 ```
 
 <br><br>
 
 ## 2. Add new fragment to the main layout file
   
 - TitleFragment를 activity_main.xml 레이아웃 파일에 추가한다.
 
 ##### 1. activity_main.xml의 LinearLayout 내에 fragment 요소를 추가한다
 
   - XML 레이아웃 파일에서 <fragment> 태그를 통해 fragment를 정의할 수 있다.
  
 ##### 2. fragment의 name에 fragment class의 full path를 입력한다.
 
 ```
  <layout xmlns:android="http://schemas.android.com/apk/res/android"
     xmlns:app="http://schemas.android.com/apk/res-auto">
 
         <LinearLayout
             android:layout_width="match_parent"
             android:layout_height="match_parent"
             android:orientation="vertical">
             <fragment
                 android:id="@+id/titleFragment"
                 android:name="com.example.android.navigation.TitleFragment"
                 android:layout_width="match_parent"
                 android:layout_height="match_parent"
                 />
         </LinearLayout>
 
  </layout>
 ```
 
 