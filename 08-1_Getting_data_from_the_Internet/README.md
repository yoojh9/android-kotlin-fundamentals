# 08-1 Getting data from the internet

## 1. Explore the MarsRealEstate starter app
MarsRealEstate 앱에는 두가지의 주요 모듈이 있다

   - overview fragment: RecyclerView로 만들어진 썸네일 속성의 이미지 그리드가 포함되어 있음
   - detail view fragment: 각 property에 대한 정보가 포함되어 있다

<image src="./images/overview.png" width="80%" height="80%"/>

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


<br><br>

## 3. Parse the JSON response with Moshi
이제 Mars 웹서비스로부터 Json 응답을 얻어올 수 있다. 그러나 우리가 원하는 건 JSON 문자열이 아니라 Kotlin 객체이다. Moshi라는 라이브러리가 있는데 이는 JSON 문자열을 Kotlin 객체로 변환하는 Android JSON 파서이다.
Retrofit에는 Moshi와 호환되는 converter가 있으므로 Moshi는 우리의 요구사항에 맞는 훌륭한 라이브러리이다.

이번 단계에서는 Retrofit과 함께 Moshi 라이브러리를 사용하여 웹 서비스의 JSON 응답을 유용한 Mars Property kotlin 객체로 parse한다. raw JSON을 표시하는 대신 화성의 데이터 수가 표시 되도록 변경한다.


### Step 1: Add Moshi library dependencies

#### 1) build.gradle (Module: app)을 연다

#### 2) dependencies 섹션에서 아래처럼 Moshi 디펜던시를 추가한다. 디펜던시는 Moshi JSON 라이브러리 및 Moshi의 코틀린 지원용 라이브러리가 추가되어야 한다

```
implementation "com.squareup.moshi:moshi:$version_moshi"
implementation "com.squareup.moshi:moshi-kotlin:$version_moshi"
```

#### 3) dependencies 블럭에서 Retrofit scalar converter 라인을 찾는다

```
implementation "com.squareup.retrofit2:converter-scalars:$version_retrofit"
```

#### 4) converter-moshi를 사용하는 문장으로 변경한다.

```
implementation "com.squareup.retrofit2:converter-moshi:$version_retrofit"
```

#### 5) Sync Now 버튼을 눌러서 새로운 디펜던시로 프로젝트를 rebuild 한다
Retrofit scala deendency 제거와 관련된 컴파일 에러를 볼 수 있는데 이는 다음 단계에서 고치자

<br>

### Step 2: Implement the MarsProperty data class
웹 서비스에서 얻는 JSON 응답의 샘플 항목은 다음과 같다.

```
[{"price":450000,
"id":"424906",
"type":"rent",
"img_src":"http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000ML0044631300305227E03_DXXX.jpg"},
...]
```

Moshi는 JSON 데이타를 parse 하여 Kotlin 객체로 convert한다. 이렇게 하려면 파싱된 결과를 저장하기 위해 Kotlin data class가 있어야 하므로 해당 클래스를 먼저 만들어야 한다.

#### 1) app/java/network/MarsProperty.kt 를 연다

#### 2) MarsProperty 클래스 정의를 아래 코드로 대체한다

```
data class MarsProperty(
   val id: String, val img_src: String,
   val type: String,
   val price: Double
)
```

MarsProperty 클래스의 각 변수는 JSON 객체의 키 name에 해당한다. JSON 유형을 일치시키려면 Double 형인 price를 제외한 모든 type을 String으로 사용한다. Moshi는 JSON을 parse할 때 이름과 키를 매칭시키고 적절한 값으로 데이터 객체를 채운다


#### 3) img_src의 키를 아래와 같이 변경한다. com.squareup.moshi.Json을 import 한다

```
@Json(name = "img_src") val imgSrcUrl: String,
```

데이터 클래스에서 JSON의 응답의 키 이름과 다른 변수 이름을 사용하려면 @JSON 어노테이션을 이용한다. 이 예제에서 데이터 클래스의 변수 이름은 imageSrcUrl이고 이 변수가 매핑되는 JSON 속성은 img_sr이다.

<br>

### Step 3: Update MarsApiService and OverviewViewModel
MarsProperty 데이터 클래스를 사용하면 이제 Moshi data를 포함하도록 ViewModel과 network API를 업데이트 할 수 있다


#### 1) network/MarsApiService.kt를 연다. ScalarsConverterFactory에 오류가 발생하는 걸 확인할 수 있다. 이 에러는 Step 1에서 Retrofit Dependency가 바꼈기 때문에 발생한다.

#### 2) 파일 상단 Retrofit builder 직전에 다음 코드를 추가하여 Moshi 인스턴스를 생성한다. com.squareup.moshi.Moshi과 com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory를 import한다

```
private val moshi = Moshi.Builder()
   .add(KotlinJsonAdapterFactory())
   .build()
```

#### 3) Retrofit builder가 ScalarConverterFactory 대신에 MoshiConverterFactory 사용하도록 변경하고 create()에 moshi 인스턴스를 전달하도록 변경한다. 

```
private val retrofit = Retrofit.Builder()
   .addConverterFactory(MoshiConverterFactory.create(moshi))
   .baseUrl(BASE_URL)
   .build()
```

#### 4) MarsApiService 인터페이스를 Retrofit에서 Call<String>을 반환하는 대신 MarsProperty 개체의 목록을 반환하도록 수정한다

```
interface MarsApiService {
   @GET("realestate")
   fun getProperties():
      Call<List<MarsProperty>>
}
```

#### 5) OverviewViewModel.kt 파일을 열어서 getProperties().enqueue() 메소드 안의 Callback<String>을 Callback<List<MarsProperty>>로 변경한다.

```
MarsApi.retrofitService.getProperties().enqueue( 
   object: Callback<List<MarsProperty>> {
```

#### 6) onFailure()에서 Call<String>을 Call<List<MarsProperty>>로 변경한다

```
override fun onFailure(call: Call<List<MarsProperty>>, t: Throwable) {
```

#### 7) onResponse()의 인자 2개도 같은 방식으로 변경한다.

```
override fun onResponse(call: Call<List<MarsProperty>>, 
   response: Response<List<MarsProperty>>) {
```

#### 8) onResponse()의 본문에서 존재하던 _response.value 할당 문을 아래와 같이 변경한다.

```
_response.value = 
   "Success: ${response.body()?.size} Mars properties retrieved"
```

<br><br>

## 4. Use coroutines with Retrofit
Retrofit API 서비스는 실행 중이지만 구현해야 하는 두 개의 콜백 메소드가 있는 콜백을 사용해야 한다. 하나는 success이고 다른 하나는 failure를 다룬다. failure의 결과는 exception을 보고한다.
콜백을 사용하는 대신 예외 처리와 함께 코루틴을 사용한다면 코드가 더 효율적이고 읽기 쉽게 보인다. 편리하게도 Retrofit coroutine을 통합하는 라이브러리가 있다

이번 단계에서는 코루틴을 사용하도록 network service와 ViewModel을 변경한다

<br>

### Step 1: Add coroutine dependencies

#### 1) build.gradle(Module: app)을 연다

#### 2) dependencies 부분에 core Kotlin coroutine 라이브러리 및 Retrofit coroutine 라이브러리를 추가한다.

```
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version_kotlin_coroutines"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version_kotlin_coroutines"
implementation "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:$version_retrofit_coroutines_adapter"

```

<br>

### Step 2: Update MarsApiService and OverviewViewModel

#### 1) MarsApiService.kt 에서 Retrofit builder가 CoroutineCallAdapterFactory를 사용하도록 변경한다. 전체 빌더의 모습은 아래와 같다

```
private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()
```

CallAdapter는 Retrofit이 default Call 클래스 이외의 것을 리턴하는 API를 생성하는 기능을 추가한다. 이 경우 CoroutineCallAdapterFactory를 사용하면 getProperties()가 반환하는 Call 객체를 Deferred 객체로 대체할 수 있다.


#### 2) getProperties()에서 Call<List<MarsProperty>>를 Deferred<List<MarseProperty>>로 변경한다. kotlinx.coroutines.Deferred를 import한다

```
@GET("realestate")
fun getProperties():
   Deferred<List<MarsProperty>>
```

Deferred 인터페이스는 결과값을 리턴하는 코루틴 작업을 정의한다 (Deferred는 Job을 상속함). Deferred 인터페이스는 await() 메소드를 포함하고 있고, await() 메소드는 값이 준비 될 때까지 blocking 없이 대기한 다음 해당 값이 준비 될 때 반환한다.


#### 3) OverviewViewModel.kt를 열어서 init 블럭에 코루틴 job을 추가한다

```
private var viewModelJob = Job()
```

#### 4) Main dispatcher를 사용하는 새로운 job에 대해 coroutine scope를 생성한다

```
private val coroutineScope = CoroutineScope(
   viewModelJob + Dispatchers.Main )
```

Dispatchers.Main 디스패처는 작업에 UI 쓰레드를 사용한다. Retrofit은 background 쓰레드에서 모든 작업을 수행하므로 scope에 다른 쓰레드를 사용할 필요가 없다. 이것을 결과를 얻을 때 MutableLiveData의 값을 쉽게 업데이트 하게 해준다.


#### 5) getMarsRealEstateProperties() 내에 모든 코드를 지운다. enqueue(), onFailure(), onResponse() 콜백 대신에 코루틴을 사용한다

#### 6) getMarsRealEstateProperties()에서 코루틴을 시작한다.

```
coroutineScope.launch { 

}
```

네트워크 작업에 대해 Retrofit이 반환하는 Deferred 객체를 사용하려면 코루틴 내부에 있어야 하므로 여기서 생성한 코루틴을 시작한다. 여전히 메인 스레드에서 코드를 실행하고 있지만 코루티이 동시성을 관리하게 된다

#### 7) launch 블럭 내에서 retrofitService 객체의 getProperties()를 호출한다
MarsApi 서비스로부터 getProperties()를 호출하면 background thread에서 network 호출을 생성하고 시작하여 각 작업에 대해 Deferred 객체를 반환한다.

```
var getPropertiesDeferred = MarsApi.retrofitService.getProperties()
```

#### 8) launch 블럭 내에서 try / catch 블럭을 추가하여 exception을 handle 할 수 있다

```
try {

} catch (e: Exception) {
  
}
```

#### 9) try {} 블럭 안에 Deferred 객체의 await()를 호출한다

```
var listResult = getPropertiesDeferred.await()
``` 

Deferred 객체의 await()를 호출하면 값이 준비 되었을 때 네트워크 호출의 결과값을 반환해준다. await() 메소드는 non-blocking 이므로, Mars API 서비스는 현재 스레드(UI 스레드)를 blocking 하지 않고 네트워크로부터 데이터를 얻어온다. 작업이 완료되면 중단된 지점부터 코드가 계속 실행 된다.

#### 10) try {} 블럭 안에 await() 메소드 이후에 successful message로 response message를 변경한다

```
_response.value = 
   "Success: ${listResult.size} Mars properties retrieved"
```

#### 11) catch { } 블럭 안에 failure 응답을 처리한다

```
_response.value = "Failure: ${e.message}"
```

#### 12) 완성된 getMarsRealEstateProperties() 메소드는 아래와 같다

```
private fun getMarsRealEstateProperties() {
   coroutineScope.launch {
       var getPropertiesDeferred = 
          MarsApi.retrofitService.getProperties()
       try {          
           _response.value = 
              "Success: ${listResult.size} Mars properties retrieved"
       } catch (e: Exception) {
           _response.value = "Failure: ${e.message}"
       }
   }
}
```

#### 13) 클래스의 밑에 onCleared() 콜백을 다음 코드와 함께 추가한다

```
override fun onCleared() {
   super.onCleared()
   viewModelJob.cancel()
}
```

ViewModel이 사라지면 이 ViewModel을 사용하고 있던 OverviewFragment도 사라지기 때문에 ViewModel이 파괴되면 데이터 로드를 중단해야 한다. ViewModel이 파괴될 때 로드를 중지하려면 onClear()를 오버라이드 하여 job을 취소해야 한다

