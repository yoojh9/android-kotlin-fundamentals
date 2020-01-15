# 09-2 WorkManager

## 1. WorkManager
WorkManager는 Android Jetpack의 Android Architecture Components 중의 하나로, WorkManager는 즉시 실행되지 않아도 되는, 연기 가능하고 보장된 실행이 필요한 백그라운드 작업용이다. 

   - Deferrable: 즉시 실행되지 않아도 되는 작업을 의미한다. 예를 들어 분석 데이터를 서버에 전달하거나 백그라운드에서 데이터베이스를 동기화 하는 것은 지연될 수 있는 작업이다
   - Guaranteed execution: 실행 보장은 앱이 종료되거나 장치가 다시 시작되더라도 작업이 실행됨을 의미한다.
   
WorkManager는 백그라운드 작업을 실행하는 동안 호환성 문제와 배터리 및 시스템 상태에 대한 모범 사례를 처리한다. WorkManager는 API level 14까지의 호환성을 제공한다. WorkManager는 장치 API 레벨에 따라 백그라운드 작업을 스케줄하는 적절한 방법을 선택한다.
JobScheduler (API 23 이상) 또는 AlarmManager와 BroadcastReceiver의 조합을 사용할 수 있다.

<image src="./images/workmanger.png" width="70%" height="70%"/>

WorkManager를 사용하면 백그라운드 작업 실행 시기에 대한 기준을 설정할 수도 있다. 예를 들어 배터리 상태, 네트워크 상태 또는 충전 상태가 특정 기준을 충족한 경우에만 작업을 실행할 수도 있다.

**Note**
  - WorkManager는 앱 프로세스가 종료된 경우 안전하게 종료될 수 있는 프로세스 내 백그라운드 작업을 위한 것이 아니다.
  - WorkManager는 즉각적인 실행이 필요한 작업을 위한 것이 아니다.
  
이번 task에서는 네트워크에서 하루에 한번 DevBytes video playlist를 미리 가져오는 작업을 스케줄한다. 이 작업을 스케줄하려면 WorkManager 라이브러리를 사용해야 한다.

<br><br>

## 2. Add the WorkManager dependency

build.gradle(Module:app) 파일을 열고 WorkManager 디펜던시를 추가한다

```
// WorkManager
def work_version = "2.2.0"
implementation "androidx.work:work-runtime-ktx:$work_version" // Kotlin + coroutines
```

<br><br>

## 3. Create a background worker
프로젝트에 code를 추가하기 전에 WorkManager 라이브러리 안의 클래스들을 알아보자

 - **Worker**
 이 클래스는 백그라운드에서 실행할 실제 작업을 정의한다. 이 클래스를 extend 하고 doWork() 메소드를 오버라이드 해라. doWork() 메소드는 서버와 데이터 동기화 또는 이미지 처리와 같이 백그라운드에서 수행 할 코드를 넣는 곳이다.
 
 - **WorkRequest**
 이 클래스는 백그라운드에서 worker를 실행하라는 요청을 나타낸다. 장치 연결 또는 와이파이 연결과 같은 제약 조건을 통해 WorkRequest를 사용하여 worker 작업을 언제 실행할지, 어떻게 실행할지 설정할 수 있다
 
 - **WorkManager**
 이 클래스는 WorkRequest를 스케줄하고 실행한다. WorkManager는 지정한 제한 조건을 준수하면서 시스템 자원에 대한 로드를 분산시키는 방식으로 work 요청을 스케줄한다.
 
<br>

### Step 1: Create a worker
이번 단계에서는 백그라운드에서 DevBytes 비디오 재생 목록을 미리 가져오기 위해 Worker를 추가한다

#### 1) devbyteviewer 패키지에서 work 라는 새로운 패키지를 만든다

#### 2) work 패키지 안에 RefreshDataWorker라는 새로운 코틀린 클래스 파일을 만든다

#### 3) RefreshDataWork 클래스가 CoroutineWorker 클래스를 상속받게 만들고 CoroutineWorker 클래스의 생성자 파라미터로 context와 WorkerParameters를 넘긴다

```
class RefreshDataWorker(appContext: Context, params: WorkerParameters) :
       CoroutineWorker(appContext, params) {
}
```

#### 4) abstract class 에러를 해결하기 위해 doWork() 메소드를 오버라이드 한다.

```
override suspend fun doWork(): Result {
  return Result.success()
}
```

suspending function은 일시 중지 했다가 나중에 다시 시작할 수 있는 함수이다. suspending function은 장기 실행 작업을 처리할 수 있고 main thread를 blocking 하지 않고 완료될 때 까지 기다릴 수 있다

<br>

### Step 2: Implement doWork()
Worker 클래스 내의 doWork() 메소드는 백그라운드 스레드에서 호출된다. 이 메소드는 작업을 동기적으로 수행하며 ListenableWorker.Result 객체를 반환해야 한다. 안드로이드 시스템은 Worker에게 처리하고 ListenableWorker.Result 객체를 리턴하는 데 최대 10분의 시간을 준다. 이 시간이 만료되면 시스템은 Worker를 강제로 중지시킨다.

ListenableWorker.Result 객체를 생성하려면 다음 정적 메소드 중 하나를 호출하여 백그라운드 작업의 완료 상태를 나타내라

   - Result.success() : 작업이 성공적으로 완료됨
   - Result.failure(): 작업이 영구적인 실패로 완료됨
   - Result.retry(): 작업에 일시적인 오류가 발생하여 다시 시도해야 함
 
이 작업에서는 doWork() 메소드를 구현하여 네트워크에서 DevBytes 비디오 재생 목록을 가져온다. VideosRepository 클래스의 기존 메소드를 재사용하여 네트워크에서 데이터를 얻어올 수 있다.

<br>

#### 1) RefreshDataWorker의 doWork() 메소드 내부에서 VideosDatabase 객체 및 VideosRepository 객체를 생성하고 인스턴스화 한다.

```
override suspend fun doWork(): Result {
   val database = getDatabase(applicationContext)
   val repository = VideosRepository(database)

   return Result.success()
}
```


#### 2) doWork() 메소드 내의 return 문 위에 try 블럭에서 refreshVideos() 메소들르 호출하는 코드를 추가한다.

```
try {
   repository.refreshVideos( )
   Timber.d("Work request for sync is run")
   } catch (e: HttpException) {
   return Result.retry()
}
```

#### 3) 다음은 완성된 RefreshDataWorker 클래스 코드이다

```
class RefreshDataWorker(appContext: Context, params: WorkerParameters) :
       CoroutineWorker(appContext, params) {

   override suspend fun doWork(): Result {
       val database = getDatabase(applicationContext)
       val repository = VideosRepository(database)
       try {
           repository.refreshVideos()
       } catch (e: HttpException) {
           return Result.retry()
       }
       return Result.success()
   }
}
```

<br><br>

## 3. Define a periodic WorkRequest
Worker는 작업의 단위를 정의하고, WorkRequest는 작업이 언제 어떻게 작동할지 정의한다. WorkRequest 클래스에는 두 가지의 구체적인 implementations가 있다.

 - OneTimeWorkRequest 클래스는 일회성 작업을 위한 것이다 (일회성 작업은 한번만 발생한다)
 - PeriodicWorkRequest 클래스는 주기적 작업, 특정 시간 간격마다 반복되어야 하는 작업을 위한 것이다.
 
작업은 일회성 또는 주기적일 수 있으므로 적절한 클래스를 선택하라. 반복 작업 스케줄링에 대한 더 많은 정보는 [recurring work documentation](https://developer.android.com/topic/libraries/architecture/workmanager/how-to/recurring-work)를 참고한다

**Note:** 주기적인 작업의 최소 interval은 15분이다. 

이 태스크에서는 이전 태스크에서 작성한 Worker를 실행하기 위해 WorkRequest를 정의하고 스케줄링한다.

<br>

### Step 1: Set up recurring work
안드로이드 앱 내에서 Application 클래스는 activity및 service와 같은 모든 구성요소를 포함하는 기본 클래스이다. application 또는 패키지에 대한 프로세스가 생성되면 Application 클래스(또는 Application 클래스의 하위 클래스)가 다른 클래스들보다 먼저 인스턴스화 된다

샘플 앱에서 DevByteApplication 클래스는 Application 클래스의 하위 클래스이다. DevByteApplication 클래스는 WorkManager를 스케줄하기에 좋은 장소이다

#### 1) DevByteApplication 클래스에서 반복 백그라운드 작업을 설정하기 위해 setupRecurringWork() 메소드를 작성한다.

```
/**
* Setup WorkManager background job to 'fetch' new network data daily.
*/
private fun setupRecurringWork() {
}
``` 

#### 2) setupRecurringWork() 메소드 내부에서 PeriodicWorkRequestBuilder() 함수를 사용하여 하루에 한번 실행되는 periodic work request를 생성하고 초기화한다. 이전 단계에서 생성했던 RefreshDataWorker 클래스를 타입으로 전달하고, TimeUnit.DAYS의 시간으로 1의 interval을 생성자 파라미터로 전달한다.

```
val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
       .build()
```

<br>

### Step 2: Schedule a WorkRequest with WorkManager
WorkRequest 정의가 끝나면 enqueueUniquePeriodicWork()를 사용하여 WorkManager로 스케줄 할 수 있다. 이 메소드를 사용하면 한번에 하나의 특정 이름의 PeriodicWorkRequest만 활성화 할 수 있는 큐에 unique한 이름의 PeriodicWorkRequest를 추가할 수 있게 해준다.

예를 들어 오직 하나의 동기화 작업만 활성화 할 수 있다. 만약 하나의 동기화 작업이 보류중(pending)인 경우 ExistingPeriodicWorkPolicy를 사용하여 동기화 작업을 실행하거나 새로운 작업으로 교체하도록 선택할 수 있다
WorkManager를 스케줄하는 방법은 [WorkManager](https://developer.android.com/reference/androidx/work/WorkManager.html#public-methods_1) documentation을 참고해라.


#### 1) RefreshDataWorker 클래스에서 클래스 시작 부분에 companion object를 추가한다. worker를 unique하게 식별하기 위해 work의 이름을 정의한다.

```
companion object {
   const val WORK_NAME = "com.example.android.devbyteviewer.work.RefreshDataWorker"
}
```

#### 2) DevByteApplication 클래스의 setupRecurringWork() 메소드의 끝 부분에서 enqueueUniquePeriodicWork() 함수를 사용하여 작업을 스케줄한다. ExistingPeriodicWorkPolicy에 대해 KEEP enum을 전달한다. 

```
WorkManager.getInstance().enqueueUniquePeriodicWork(
       RefreshDataWorker.WORK_NAME,
       ExistingPeriodicWorkPolicy.KEEP,
       repeatingRequest)
```

동일한 이름의 pending (완료되지 않은) 작업이 존재하는 경우 ExistingPeriodicWorkPolicy.KEEP 매개변수는 WorkManager가 이전의 주기적 작업을 유지하고 새 작업 요청을 삭제하도록 한다


**Best Practice:** onCreate() 메소드는 main thread에서 실행된다. 장기 실행 작업을 onCreate()에서 실행하면 UI thread를 block하거나 앱 로드가 지연될 수도 있다. 이 문제를 방지하려면 코루틴 내에서 Timber 초기화 및 WorkManager 스케줄링과 같은 작업을 실행해라.

#### 3) DevByteApplication 클래스 시작 부분에 CoroutineScope 객체를 생성한다. 생성자 파라미터로 Dispatchers.Default를 넘긴다

```
private val applicationScope = CoroutineScope(Dispatchers.Default)
```


#### 4) DevByteApplication 클래스 내에서 코루틴을 시작하기 위해 delayedInit()이라는 새로운 메소드를 만든다

```
private fun delayedInit() {
   applicationScope.launch {
   }
}
```

#### 5) delayedInit() 함수 내에서 setupRecurringWork()를 호출한다

#### 6) onCreate()에 있는 Timber 초기화 코드를 delayedInit() 메소드로 옮긴다

```
private fun delayedInit() {
   applicationScope.launch {
       Timber.plant(Timber.DebugTree())
       setupRecurringWork()
   }
}
```

#### 7) DevByteApplication 클래스의 onCreate()의 마지막 부분에 delayedInit()을 호출하는 코드를 추가한다

```
override fun onCreate() {
   super.onCreate()
   delayedInit()
}
```

#### 8) 안드로이드 스튜디오의 Logcat 창을 열고 \'RefreshDataWorker\'을 로그의 필터로 건다

#### 9) 앱을 실행시킨다. WorkManager는 반복 작업을 즉시 예약한다.
Logcat 창에서 work request가 스케줄 되고 성공적으로 실행됨을 나타내는 로그 문장을 확인할 수 있다
WM-WorkerWrapper 로그는 WorkManager 라이브러리에서 표시되는 로그이므로 로그 메시지를 변경할 수는 없다.

```
D/RefreshDataWorker: Work request for sync is run
I/WM-WorkerWrapper: Worker result SUCCESS for Work [...]
```

<br>

### Step 3: (Optional) Schedule the WorkRequest for a minimum interval
이번 단계에서는 time interval을 1일에서 15분으로 줄인다. 

#### 1) DevByteApplication 클래스의 setupRecurringWork() 메소드에서 현재 repeatingRequest 변수를 주석 처리하고, 주기적 반복 간격이 15분인 새로운 work request를 추가한다

```
// val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
//        .build()
val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(15, TimeUnit.MINUTES)
       .build()
```

#### 2) Logcat 창을 열어서 \'RefreshDataWorker\'를 필터로 로그를 확인한다

#### 3) 앱을 실행시킨다. WorkManager는 반복 작업을 즉시 스케줄링한다. work request는 15분마다 한번씩 실행된다.
앱을 실행하거나 닫아도 work manager는 계속 실행되어야 한다.
interval이 가끔 15분보다 적거나 15분보다 많은 것을 확인한다. (정확한 타이밍은 OS 배터리의 최적화에 달렸다)


```
12:44:40 D/RefreshDataWorker: Work request for sync is run
12:44:40 I/WM-WorkerWrapper: Worker result SUCCESS for Work 
12:59:24 D/RefreshDataWorker: Work request for sync is run
12:59:24 I/WM-WorkerWrapper: Worker result SUCCESS for Work 
13:15:03 D/RefreshDataWorker: Work request for sync is run
13:15:03 I/WM-WorkerWrapper: Worker result SUCCESS for Work 
```

이제 worker를 생성하고 WorkManager로 work request를 스케줄링 했다. 그러나 아직 제약 조건을 지정하지 않았다. 
배터리가 부족하거나 절전 상태이거나 네트워크에 연결되어 있지 않은 경우에도 WorkManager는 하루에 한번 작업을 예약한다. 
이는 장치 배터리 및 성능에 영향을 미치며 user experience가 저하될 수 있다.
다음 작업에서는 제약 조건을 추가하여 이 문제를 해결해보자.

<br><br>

## 4. Add constraints
이전 단계에서 work request를 에약하기 위해 WorkManager를 사용했다. 이번 단계에서는 work 실행 시기에 대한 기준을 추가한다

WorkRequest를 정의할 때 worker 실행 시기에 대한 제한 조건을 지정할 수 있다.
예를 들어 장치가 유휴 상태이거나 장치가 연결되어 있고 Wi-Fi에 연결되어 있을 때만 작업을 실행하도록 지정할 수 있다. 또한 작업 재시도를 위한 backoff 정책을 지정할 수도 있다. 

**PeriodicWorkRequest and constraints**
작업 반복을 위한 WorkRequest(ex.PeriodicWorkRequest)는 취소 될 때 까지 여러 번 실행된다. 첫번째 실행은 즉시 또는 주어진 제약 조건이 충족되는 즉시 발생한다.
다음 실행은 interval 간격 동안 발생한다. 실행은 지연될 수도 있다. 예를 들어 장치가 Doze 모드에있을 때 WorkManager는 OS 배터리 최적화가 적용되므로 실행이 지연 될 수 있다.

<br>

### Step 1: Add a Constraints object and set one constraint
이번 단계에서는 Constraints 객체를 만들고 네트워크 유형 제약 조건인 객체에 대해 하나의 제약 조건을 설정한다. 

#### 1) DevByteApplication 클래스의 setupRecurringWork() 메소드 시작 부분에 Constraints 타입의 val 변수를 정의한다. Constraints.Builder() 메소드를 사용해라
androidx.work.Constraints를 import 한다

```
val constraints = Constraints.Builder()
``` 

#### 2) setRequiredNetworkType() 메소드를 사용하여 constraints 오브젝트에 network-type 제약 조건을 추가한다. work request는 와이파이 환경에서만 실행되게 하기 위해 UNMETERED enum을 사용한다.

```
.setRequiredNetworkType(NetworkType.UNMETERED)
```

#### 3) builder에서 제한 조건을 생성하려면 build() 메소드를 사용한다

```
val constraints = Constraints.Builder()
       .setRequiredNetworkType(NetworkType.UNMETERED)
       .build()
``` 

이제 새로 생성된 Constraints 객체를 work request에 설정하면 된다

#### 4) DevByteApplication 클래스의 setupRecurringWork() 메소드 내에서 Constraints 객체를 periodic work request에 설정한다. constraints를 설정하려면 build() 메소드 호출 위에 setConstraints() 메소드를 추가한다.

```
   val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(15, TimeUnit.MINUTES)
           .setConstraints(constraints)
           .build()
```

<br>

### Step 2: Run the app and notice the logs
이 단계에서는 앱을 실행하고 제한된(constrained) 작업 요청(work reqeust)이 백그라운드에서 시간 interval 마다 실행되고 있음을 알 수 있다

#### 1) 장치 또는 에뮬레이터에서 앱을 제거하여 이전에 예약된 작업을 취소한다.

#### 2) Wi-Fi를 끄면 제약 조건이 어떻게 작동하는지 확인할 수 있다
현재 코드는 하나의 제약 조건만 설정하여 work request가 와이파이 네트워크에서만 실행되어야 한다. 따라서 와이파이를 끄면 이 제약조건이 충족되지 않는다.

#### 3) 앱을 실행하고 Logcat 창을 확인해보자. WorkManager는 백그라운드 작업을 즉시 예약한다. 하지만 네트워크 제약 조건이 충족되지 않았으므로 작업이 실행되지는 않는다

```
11:31:44 D/DevByteApplication: Periodic Work request for sync is scheduled
```

#### 4) 장치 또는 애뮬레이터에서 와이파이를 켜고 Logcat 창을 본다. 이제 네트워크 제약 조건이 충족되는 한 예약된 백그라운드 작업이 약 15분마다 실행된다.

```
11:31:44 D/DevByteApplication: Periodic Work request for sync is scheduled
11:31:47 D/RefreshDataWorker: Work request for sync is run
11:31:47 I/WM-WorkerWrapper: Worker result SUCCESS for Work [...]
11:46:45 D/RefreshDataWorker: Work request for sync is run
11:46:45 I/WM-WorkerWrapper: Worker result SUCCESS for Work [...] 
12:03:05 D/RefreshDataWorker: Work request for sync is run
12:03:05 I/WM-WorkerWrapper: Worker result SUCCESS for Work [...] 
```

<br>

### Step 3: Add more constraints
이번 단계에서는 아래의 제약조건을 PeriodicWorkRequest에 추가한다.

  - 배터리가 부족하지 않을 때
  - 디바이스가 충전 중 일때
  - 장치 유휴(API 레벨 23 (Android M) 이상에서만 사용 가능)
  
DevByteApplication 클래스에 아래 작업들을 추가해보자₩

#### 1) DevByteApplication 클래스의 setupRecurringWork() 메소드 내부에서 배터리가 부족하지 않은 경우에만 작업 요청을 실행해야 함을 나타내본다. build() 메소드 호출 전에 constraint 객체에 setRequiresBatteryNotLow() 메소드를 사용하여 제한 조건을 추가한다.

```
.setRequiresBatteryNotLow(true)
```

#### 2) 작업 요청을 업데이트 하여 장치가 충전 중일 때만 실행되도록 한다. build() 메소드 호출 전에 setRequiresCharging() 메소드를 사용하여 제약 조건을 추가한다

```
.setRequiresCharging(true)
```

#### 3) 장치가 유휴 상태 일 때만 작동하도록 작업 요청을 업데이트 한다. setRequiresDeviceIdle()를 사용하여 constraint를 추가한다. 이 제한 조건은 사용자가 디바이스를 적극적으로 사용하지 않는 경우에만 작업 요청을 실행한다.
이 기능은 Android 6.0 (Marshmallow) 이상에서만 사용할 수 있으므로 SDK 버전 M 이상에 대한 조건을 추가한다.

```
.apply {
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
       setRequiresDeviceIdle(true)
   }
}
```

완성된 constraints 객체는 아래와 같다

```
val constraints = Constraints.Builder()
   .setRequiredNetworkType(NetworkType.UNMETERED)
   .setRequiresBatteryNotLow(true)
   .setRequiresCharging(true)
   .apply {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           setRequiresDeviceIdle(true)
       }
   }
   .build()
```

#### 4) setupRecurringWork() 메소드 내에서 요청 간격을 하루에 한 번으로 다시 변경한다.

```
val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
       .setConstraints(constraints)
       .build()
```

#### 5) 이전에 예약 된 작업 요청을 제거하려면 장치 또는 에뮬레이터에서 DevBytes 앱을 제거한다.

#### 6) 앱을 실행하면 WorkManager가 즉시 작업 요청을 예약한다. 모든 제한 조건이 충족되면 작업 요청이 하루에 한 번 실행된다

#### 7) 이 작업 요청은 앱이 실행되고 있지 않더라도 앱이 설치되어있는 한 백그라운드에서 실행된다. 따라서 휴대 전화에서 앱을 제거해야 한다.

WorkManager는 작업을 예약하고 실행하여 시스템 리소스를 최적화 한다. 사용자와 배터리는 매우 행복하다.
