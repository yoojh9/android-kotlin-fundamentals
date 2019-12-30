# 08-1 Getting data from the internet

## 1. Explore the MarsRealEstate starter app
MarsRealEstate 앱에는 두가지의 주요 모듈이 있다

   - overview fragment: RecyclerView로 만들어진 썸네일 속성의 이미지 그리드가 포함되어 있음
   - detail view fragment: 각 property에 대한 정보가 포함되어 있다

<image src="./images/overview.png" width="70%" height="70%"/>

앱은 각각의 프래그먼트에 대한 ViewModel을 가지고 있다. 이번 프로젝트에서는 네트워크 서비스에 대한 layer를 만들고 ViewModel이 네트워크 layer와 직접 통신한다. 이는 전에 ViewModel이 Room 데이터베이스와 직접 통신하던 방식과 유사하다

overview ViewModel은 화성의 부동산 정보를 얻기 위해 네트워크의 호출을 담당하고 있다. detail ViewModel은 detail fragment에 표시되는 단일 화성 부동산 정보에 대한 상세 데이터가 있다.
각 ViewModel에 대해 수명주기 인식 data binding과 LiveData를 사용하여 데이터가 변할 때 UI를 변경한다.

또한 navigation component를 사용하여 두 프래그먼트 사이를 이동하고 선택된 프로퍼티를 인자로 전달한다.

<br>

## 2. Connect to a web service with Retrofit
다음 웹 서버에서 데이터를 얻는다. https://android-kotlin-fun-mars-server.appspot.com

화성의 부동산 정보 리스트는 다음 URL과 같다. https://android-kotlin-fun-mars-server.appspot.com/realestate

웹서비스의 응답은 일반적으로 구조화된 데이터를 나타내는 형식인 JSON 형식이다. 
이 데이터를 앱으로 가져오려면 앱에서 네트워크 연결을 설정하고 해당 서버와 통신한 다음 응답 데이터를 수신하여 앱이 사용할 수 있는 포맷으로 parse 해야한다. 이 단계에서는 REST client library인 Retrofit을 이용하여 connection을 만든다

<br>

### Step 1: Add Retrofit dependencies to Gradle

#### 1) build.gradle(Module: app)을 연다

#### 2) dependencies 부분에 Retrofit 라이브러리를 추가한다

```
implementation "com.squareup.retrofit2:retrofit:$version_retrofit"
implementation "com.squareup.retrofit2:converter-scalars:$version_retrofit"
```

 - 첫번째 디펜던시는 Retrofit2 라이브러리 자체에 대한 것이고 두번째 디펜던시는 Retrofit 스칼라 converter 대한 것이다
 - 이 converter를 사용하면 Retrofit에서 JSON 결과를 문자열로 반환할 수 있다. 두 라이브러리는 함께 작동한다
 

#### 3) Sync Now를 클릭한다

<br>

### Step 2: Implement MarsApiService
Retrofit은 웹 서비스의 컨텐츠를 기반으로 앱에 네트워크 API를 생성한다. Retrofit은 웹 서비스로부터 데이터를 가져와서 데이터를 디코딩하고 유용한 object의 형식으로 리턴하는 방법을 알고 있는 별도의 converter 라이브러리를 통해 라우팅한다
Retrofit은 XML 및 JSON과 같이 널리 사용되는 웹 데이터 형식을 기본으로 지원한다. Retrofit은 백그라운드 스레드에서 요청 실행과 같이 중요한 사항을 포함하여 대부분의 netowrk layer를 생성한다

MarseApiService 클래스는 앱의 네트워크 계층을 가지고 있다. 즉 이는 ViewModel이 웹 서비스와 통신하는 데 사용할 API를 의미한다. 이 클래스는 Retrofit 서비스의 API를 구현할 클래스이다

<br>

#### 1) app/java/network/MarsApiService.kt를 열고 BASE_URL 상수 아래에 Retrofit builder를 사용하여 Retrofit 객체를 만든다. 

```
private val retrofit = Retrofit.Builder()
   .addConverterFactory(ScalarsConverterFactory.create())
   .baseUrl(BASE_URL)
   .build()
```

Retrofit은 웹서비스를 API를 build하려면 최소한 두가지가 필요하다. 첫번째로는 웹 서비스의 base URI이고 두번쨰는 converter factory이다. converter는 Retrofit에 웹서비스에서 가져온 데이터로 수행할 작업을 알려준다. 
이 케이스에서는 Retrofit에서 웹서비스로부터 JSON 응답을 가져와서 String으로 반환하려고 한다. Retrofit에는 문자열 및 기타 기본 유형을 지원하는 ScalasConverter가 있으므로 ScalaConverterFactory 인스턴스를 사용하여 빌더에서 addConverterFacty()를 호출한다
build()를 호출하여 retrofit 객체를 생성한다


#### 2) retrofit buillder 호출 바로 아래에서 Retrofit이 HTTP 요청을 사용하여 웹 서버와 통신하는 방법을 정의하는 인터페이스를 정의한다

- retrofit2.http.GET과 retrofit2.Call를 import 한다

```
interface MarsApiService {
    @GET("realestate")
    fun getProperties():
            Call<String>
}
```

현재 목표는 웹서비스에서 JSON 응답 문자열을 얻는 것이므로 getProperties() 메소드 하나만 필요하다. Retrofit에게 이 메소드가 어떤 역할을 수행하는지 알려주려면 @GET 어노테이션을 사용하고 해당 웹 서비스의 특정 경로 또는 endpoint를 지정한다. 이 예제에서는 엔드포인트를 realestate라고 한다
getProperties()가 호출될 때 Retrofit은 baseUrl에 realestate라는 엔드포인트를 붙이고 Call object를 생성한다. Call object는 request를 시작하기 위해 사용된다


#### 3) MarsApiService 인터페이스 아래에 Retrofit 서비스를 초기화 하기 위한 public object인 MarsApi를 정의한다

```
object MarsApi {
    val retrofitService : MarsApiService by lazy { 
       retrofit.create(MarsApiService::class.java) }
}
```

Retrofit create() 메소드는 MarsApiService 인터페이스를 사용하여 Retrofit 서비스 자체를 생성한다. 이 호출은 비용이 많이 들고 앱에서 Retrofit 서비스 인스턴스는 하나만 필요하므로 MarsApi라는 public object를 사용하고 Retrofit 서비스를 lazy 초기화 한다.
이제 모든 설정은 끝났다. 앱에서 MarseApi.retrofitService를 호출할 때마다 MarsApiService 인터페이스를 구현한 singleton Retrofit 객체가 얻어질 것이다.

<br>

### Step 3: Call the web service in OverviewViewModel

#### 1) OverviewViewModel.kt 파일을 열어서 getMarsRealEstateProperties() 메소드까지 스크롤을 내린다

```
private fun getMarsRealEstateProperties() {
   _response.value = "Set the Mars API Response here!"
}
```

이 메소드는 Retrofit 서비스를 호출하고 반환된 JSON 문자열을 다루는 함수이다. 지금은 response 위한 placeholder 문자열만 있다


#### 2) placeholder 행을 삭제한다

#### 3) getMarseRealEstateProperties()의 내부에 아래 코드를 추가한다. retrofit2.Callback을 import한다. 

MarsApi.retrofitService.getProperties() 메소드는 Call 오브젝트를 리턴한다. 그런 다음 해당 객체에서 enqueue()를 호출하여 백그라운드 스레드에서 네트워크의 request를 시작할 수 있다

```
MarsApi.retrofitService.getProperties().enqueue( 
   object: Callback<String> {
})
```

#### 4) object에 빨간 줄이 생기면 Code > Implement methods를 선택하여 onResponse()와 onFailure() 두가지를 선택한다

```
override fun onFailure(call: Call<String>, t: Throwable) {
       TODO("not implemented") 
}

override fun onResponse(call: Call<String>, 
   response: Response<String>) {
       TODO("not implemented") 
}

```

#### 5) onFailure() 메소드에서 TODO를 지우고 _response에 failure message를 넣는다. _response는 텍스트 뷰에 표시되는 내용을 결정하는 LiveData 문자열이다. onFailure()의 callback은 웹 서비스 응답이 실패했을 때 호출된다. 이 응답의 경우 _response의 상태를 "Failure: "로 설정하고 Throwable 인수의 메세지와 연결한다

```
override fun onFailure(call: Call<String>, t: Throwable) {
   _response.value = "Failure: " + t.message
}
``` 

#### 6) onResponse()에서 TODO를 지우고 _response에 response body를 할당한다. onResponse()의 callback은 웹 서비스의 request가 성공했을 때 호출되는 콜백이며 웹 서비스는 response를 리턴한다

```
override fun onResponse(call: Call<String>, 
   response: Response<String>) {
      _response.value = response.body()
}
```

<br>

### Step 4: Define the internet permission

앱을 컴파일 하고 실행하면 아래와 같은 에러를 발견할 수 있다.

```
    Process: com.example.android.marsrealestate, PID: 2494
    java.lang.SecurityException: Permission denied (missing INTERNET permission?)
```

이 에러 메세지는 앱에 인터넷 권한이 없음을 말해준다. 인터넷에 연결하는 것은 보안 문제를 유발할 수 있으므로 앱이 디폴트로 인터넷에 연결하는 것은 막혀있다. 그러므로 앱이 인터넷에 액세스 해야 한다는 것을 명시적으로 안드로이드에 알려야 한다


#### 1) app/manifests/AndroidManifest.xml를 열고 <application> 태그 직전에 아래 라인을 추가한다

```
<uses-permission android:name="android.permission.INTERNET" />
```

#### 2) 앱을 다시 실행시키면 Mars data가 포함된 JSON 텍스트를 볼 수 있다.
