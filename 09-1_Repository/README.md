# 09-1 Repository

## 1. Caching
앱에 네트워크로부터 데이터를 가저오면 디바이스의 storage에 저장함으로써 데이터를 캐시할 수 있다. 장치가 오프라인일 때 또는 같은 데이터에 다시 접근할 때 사용하기 위해 데이터를 캐시할 수 있다.
다음 표는 Android에서 네트워크 캐싱을 구현하는 몇 가지 방법을 보여준다. 


Caching technique | Uses
------------ | -------------
Retrofit은 안드로이드 용 type-safe REST client를 구현하는데 사용되는 네트워크 라이브러리이다. 네트워크 결과의 사본을 로컬로 저장하도록 retrofit을 설정할 수 있다 | 간단한 요청 및 응답, 드문 네트워크 호출 또는 소규모 데이터 세트에 적합한 솔루션이다.
key-value를 저장하기 위해 SharedPreferences를 이용할 수 있다 | 적은 수의 키와 간단한 값에 적합한 솔루션이다. 이 기술을 사용하여 대량의 구조화 된 데이터를 저장할 수 없다.
앱의 내부 저장소 디렉토리에 액세스하여 데이터 파일을 저장할 수 있다. 앱의 패키지 이름은 앱의 내부 저장소 디렉토리를 지정한다. 이 디렉토리는 Android 파일 시스템의 특정 위치에 있다. 이 디렉토리는 앱 전용이며 앱을 제거하면 지워진다. | 미디어 시스템이나 데이터 파일을 저장해야하고 파일을 직접 관리해야하는 경우와 같이 파일 시스템이 해결할 수있는 특정 요구 사항이있는 경우 좋은 솔루션이다. 하지만 이 기술을 사용하여 복잡하고 구조화 된 데이터를 저장할 수 없다.
SQLite에 추상화 계층인 SQLite object-mapping library인 Room을 사용하여 데이터를 캐시할 수 있다. | 장치의 파일 시스템에 구조화 된 데이터를 저장하는 가장 좋은 방법은 로컬 SQLite 데이터베이스에 있기 때문에 복잡하고 구조화 된 데이터에 권장되는 솔루션이다.

<br><br>

## 2. Add an offline cache

이번 작업에서는 Room 데이터베이스를 추가하여 오프라인 캐시로 사용한다 

**Key Concept:** 앱이 실행될 때마다 네트워크에서 데이터를 검색하지 마라. 대신 데이터베이스에서 가져온 데이터를 표시한다. 이 기술은 앱 로딩 시간을 줄여준다.

<image src="./images/cache.png" width="70%" height="70%"/>

네트워크로부터 데이터를 가져올 때 데이터를 즉시 표시하지 않고 데이터베이스에 데이터를 저장한다. 새 네트워크 결과가 수신되면 로컬 데이터베이스를 업데이트하고, 로컬 데이터베이스로부터 화면에 새 컨텐츠를 표시하십시오.
이 기술은 오프라인 캐시가 항상 최신 상태인지 확인한다. 또한 기기가 오프라인 상태인 경우에도 앱은 여전히 ​​로컬로 캐시된 데이터를 로드 할 수 있다.

<br>

### Step 1: Add the Room dependency

#### 1) build.gradle(Module:app) 파일을 열고 Room 디펜던시를 추가한다

```
// Room dependency
def room_version = "2.1.0-alpha06"
implementation "androidx.room:room-runtime:$room_version"
kapt "androidx.room:room-compiler:$room_version"
```

<br>

### Step 2: Add database object
이 단계에서는 데이터베이스 오브젝트를 나타내는 DatabaseVideo라는 데이터베이스 엔티티를 작성한다.
또한 DatabaseVideo 객체를 도메인 객체로 변환하고 네트워크 객체를 DatabaseVideo 객체로 변환하는 편리한 방법을 구현한다. 

#### 1) database/DatabaseEntities.kt를 열고 DatabaseVideo라는 Room 엔터티를 만든다. url을 primary key로 설정한다. 

```
@Entity
data class DatabaseVideo constructor(
       @PrimaryKey
       val url: String,
       val updated: String,
       val title: String,
       val description: String,
       val thumbnail: String)
```

#### 2) database/DatabaseEntities.kt 안에 asDomainModel()이라는 확장 함수를 만든다. 
이 기능을 사용하여 DatabaseVideo 데이터베이스 객체를 도메인 객체로 변환한다.

```
/**
* Map DatabaseVideos to domain entities
*/
fun List<DatabaseVideo>.asDomainModel(): List<DevByteVideo> {
   return map {
       DevByteVideo(
               url = it.url,
               title = it.title,
               description = it.description,
               updated = it.updated,
               thumbnail = it.thumbnail)
   }
}
```

#### 3) network/DataTransferObjects.kt를 열고 asDatabaseModel()이라는 확장 함수를 만든다. 이 함수를 사용하여 네트워크 객체를 DatabaseVideo 데이터베이스 객체로 변환한다

```
/**
* Convert Network results to database objects
*/
fun NetworkVideoContainer.asDatabaseModel(): List<DatabaseVideo> {
   return videos.map {
       DatabaseVideo(
               title = it.title,
               description = it.description,
               url = it.url,
               updated = it.updated,
               thumbnail = it.thumbnail)
   }
}
```

<br>

### Step 3: Add VideoDao
이번 단계에서는 VideoDao를 구현하고 데이터베이스에 액세스하기 위한 두 가지 helper 메소드를 정의한다. 한 가지 helper 메소드는 데이터베이스에서 비디오를 가져오고 다른 method는 비디오를 데이터베이스에 삽입한다.

#### 1) database/Room.kt에서 VideoDao 인터페이스를 정의하고 @Dao 어노테이션을 붙인다

```
@Dao
interface VideoDao { 
}
```

#### 2) VideoDao 인터페이스 내에서 getVideos() 메소드를 생성하여 데이터베이스에서 모든 비디오 데이터를 가져온다. 
이 메소드의 리턴 타입은 LiveData로 변경하면 데이터베이스의 데이터가 바뀔 때 마다 UI에 표시되는 데이터가 변경된다. 

```
   @Query("select * from databasevideo")
   fun getVideos(): LiveData<List<DatabaseVideo>>
```

#### 3) VideoDao 인터페이스 내에 insertAll() 메소드를 만든다. 네트워크로부터 받은 모든 비디오 리스트를 데이터베이스에 저장한다. 비디오 entry가 이미 데이터베이스에 있는 경우 데이터베이스 항목을 덮어쓴다. 이를 수행하려면 onConflict 인수를 사용하여 충돌 전략을 REPLACE로 설정한다.

```
@Insert(onConflict = OnConflictStrategy.REPLACE)
fun insertAll( videos: List<DatabaseVideo>)
``` 

<br>

### Step 4: Implement RoomDatabase
이 단계에서는 RoomDatabase를 구현하여 오프라인 캐시에 대한 데이터베이스를 추가한다.

#### 1) database/Room.kt에서 VideoDao 인터페이스 아래에 VideosDatabase라고 불리는 abstract class를 생성한다. VideosDatabase는 RoomDatabase를 상속한다.

#### 2) @Database 어노테이션을 사용하여 VideosDatabase 클래스를 룸 데이터베이스로 표시한다. 이 데이터베이스에 속하는 DatabaseVideo 엔티티를 선언하고 버전 번호를 1로 설정한다

#### 3) VideosDatabase 내부에서 VideoDao 유형의 변수를 정의하여 Dao 메소드에 액세스한다.

```
@Database(entities = [DatabaseVideo::class], version = 1)
abstract class VideosDatabase: RoomDatabase() {
   abstract val videoDao: VideoDao
}
```

#### 4) 클래스 외부에 싱글톤 오브젝트 값을 가지는 private lateinit 변수 INSTANCE를 생성한다. 여러 데이터베이스 인스턴스가 동시에 열리지 않도록하려면 VideosDatabase가 싱글톤이어야 한다.

#### 5) 클래스 외부에서 getDatabase() 메소드를 작성하고 정의한다. synchronized 블럭에서 INSTANCE 변수를 초기화하고 리턴한다.

```
@Dao
interface VideoDao {
...
}
abstract class VideosDatabase: RoomDatabase() {
...
}

private lateinit var INSTANCE: VideosDatabase

fun getDatabase(context: Context): VideosDatabase {
   synchronized(VideosDatabase::class.java) {
       if (!::INSTANCE.isInitialized) {
           INSTANCE = Room.databaseBuilder(context.applicationContext,
                   VideosDatabase::class.java,
                   "videos").build()
       }
   }
   return INSTANCE
}
```

**Tip:** .isInitialized는 lateinit 코틀린 프로퍼티에 값이 할당되어 있으면 true, 그렇지 않으면 false를 리턴한다

이제 Room을 사용하여 데이터베이스를 구현하였다.

<br><br>

## 3. Repositories

### The repository pattern
repository 패턴은 데이터 소스를 나머지 앱과 격리시키는 디자인 패턴이다. repository는 데이터 소스(영구 모델, 웹서비스, 캐시)와 나머지 앱 사이를 중재한다.
아래 다이어그램은 LiveData를 사용하는 액티비티와 같은 앱 구성요소가 레파지토리를 통해 데이터 소스와 상호 작용하는 방법을 보여준다.

<image src="./images/repository.png" width="70%" height="70%"/>

repository를 구현하려면 다음 작업에서 만드는 VideosRepository 클래스와 같은 리포지토리 클래스를 사용한다. repository 클래스는 나머지 앱과 데이터 소스를 분리하고 나머지 앱에 대한 데이터 액세스를 위한 깨끗한 API를 제공한다. 코드 분리 및 아키텍처에는 리포지토리 클래스를 사용하는 것이 좋다.


<br>

### Advantages of using a repository
리포지토리 모듈은 데이터 작업을 처리하고 여러 백엔드를 사용할 수 있다. 일반적인 실제 응용 프로그램에서 repository 네트워크는에서 데이터를 가져올지 또는 로컬 데이터베이스에 캐시된 결과를 사용할지 여부를 결정하기 위한 로직을 구현한다. 이를 통해 코드를 모듈화하고 테스트 할 수 있다.

<br><br>

## 4. Create a repository
이 태스크에서는 이전 단계에서 구현한 오프라인 캐시를 관리하기 위한 저장소를 생성한다. Room 데이터베이스에는 오프라인 캐시를 관리하기 위한 로직이 없고 단지 데이터를 삽입하고 검색하는 메소드만 있다. repository에는 네트워크 결과를 가져오고 데이터베이스를 최신 상태로 유지하는 로직이 있다.

<br>

### Step 1: Add a repository

#### 1) repository/VideosRepository.kt에서 VideosRepository 클래스를 생성한다. Dao 메소드에 액세스 하기 위해 클래스의 생성자 매개 변수로 VideosDatabase 객체를 전달한다

```
class VideosRepository(private val database: VideosDatabase) {
}
```

#### 2) VideoRepository 클래스 내부에 인자와 리턴값이 없는 refreshVideos() 메소드를 추가한다. 이 메소드는 offline cache를 갱신하는데 사용되는 APIㅣ다

#### 3) refreshVideo() 메소드를 suspend function으로 만든다. refreshVideo()는 데이터베이스 작업을 수행하므로 코루틴에서 호출해야한다

**Note:** 안드로이드의 데이터베이스는 파일 시스템 또는 디스크에 저장되므로, 데이터를 저장하려면 disk I/O를 수행해야 한다. 디스크 I/O 또는 디스크 읽기 및 쓰기 작업은 느리고 작업이 완료될 때 까지 항상 현재 스레드를 block 한다. 이 때문에 disk I/O는 I/O dispatcher에서 실행해야 한다. 이 dispatcher는 withContext(Dispatcher.IO){}를 사용하여 blocking I/O 작업을 스레드 공유 풀에 떠넘긴다

#### 4) refreshVideos() 메소드 안에서 network와 database 작업을 수행하기 위해 coroutine의 context를 Dispather.IO로 변경한다.

```
suspend fun refreshVideos() {
   withContext(Dispatchers.IO) {
   }
}
```

#### 5) withContext 블럭에서 Retrofit Service 인스턴스인 DevByteNetwork를 사용하여 network로부터 DevByte video playlist 데이터를 가져온다. playlist 데이터가 available 될 때까지 suspend 하기 위해 await() 함수를 사용한다.

```
val playlist = DevByteNetwork.devbytes.getPlaylist().await()
```

#### 6) refreshVideos() 안에서 network로부터 playlist 데이터를 가져온 후에 playlist 데이터를 Room database에 저장한다
playlist를 저장하기 위해 VideosDatabase object인 database를 사용한다. 네트워크로부터 얻은 playlist를 넘겨서 insertAll() DAO 메소드를 호출한다. asDatabaseModel() 확장함수를 사용하여 playlist를 database 객체로 변환한다.

```
database.videoDao.insertAll(playlist.asDatabaseModel())
``` 

#### 7) 완성된 refreshVideos() 메소드는 아래와 같다

```
suspend fun refreshVideos() {
   withContext(Dispatchers.IO) {
       Timber.d("refresh videos is called");
       val playlist = DevByteNetwork.devbytes.getPlaylist().await()
       database.videoDao.insertAll(playlist.asDatabaseModel())
   }
}
```

<br>

### Step 2: Retrieve data from the database
이번 단계에서는 database에 있는 video playlist를 읽기 위해 LiveData 객체를 만든다. LiveData 객체는 데이터베이스가 변경되면 자동으로 update 된다. attached된 fragment 또는 activity는 새 값으로 refresh된다.

#### 1) VideosRepository 클래스에서 DevByteVideo 객체 리스트를 가지고 있는 videos라는 LiveData 객체를 선언한다

#### 2) videos 객체를 database.videoDao를 사용하여 초기화한다. getVideos()라는 DAO 메소드를 호출한다. getVideos() 메소드는 DevByteVideo 객체가 아닌 database objects를 리턴하는 메소드이므로 안드로이드 스튜디오는 \'type mismatch\' 에러를 발생시킨다.

```
val videos: LiveData<List<DevByteVideo>> = database.videoDao.getVideos()
```

#### 3) 에러를 고치기 위해 Transformation.map을 사용하여 database objects의 리스트를 domain objects의 리스트로 변경한다. 변환함수로는 asDomainModel()을 사용한다.
Transformations.map 메소드는 LiveData 객체를 다른 LiveData 객체로 변환하는데 사용하는 conversion function이다. 이 transformation은 activity 또는 fragment가 반환된 LiveData 속성을 관찰하는 경우에만 연산된다.

```
val videos: LiveData<List<DevByteVideo>> = Transformations.map(database.videoDao.getVideos()) {
   it.asDomainModel()
}
```

<br><br>

## 5. Integrate the repository using a refresh strategy
이번 단계에서는 간단한 refresh 전략을 사용하여 repository를 ViewModel과 통합한다. 네트워크로부터 데이터를 직접 가져오는게 아니라 Room database로부터 video playlist를 가져와서 표시한다. 

database refresh는 네트워크의 데이터와 동기화 되도록 로컬 데이터베이스를 업데이트하거나 새로 고치는 프로세스이다. 이 샘플 앱의 경우에는 간단한 refresh 전략을 사용한다. 샘플 앱은 repository에서 데이터를 요청하는 모듈이 local data를 refresh하는 역할을 한다. 

실제 상용 앱에서는 refresh 전략은 더 복잡해야한다. 예를 들어, 코드가 백그라운드에서 자동으로 데이터를 refresh 하거나 (대역폭을 고려하여), 사용자가 다음에 가장 많이 사용할 데이터를 캐시할 수 있다

 
#### 1) viewmodels/DevByteViewModel.kt 파일을 열어서 DevByteViewModel 클래스 안에 private 멤버 변수로 videosRepository 타입의 videosRepository라는 변수를 생성한다. 싱글톤 VideoDatabase 객체를 전다하여 변수를 인스턴스화 한다

```
private val videosRepository = VideosRepository(getDatabase(application))
```

#### 2) DevByteViewModel 클래스에서 refreshDataFromNetwork() 메소드를 refreshDataFromRepository() 메소드로 대체한다
예전 메소드 refreshDataFromNetwork()는 Retrofit 라이브러리를 사용하여 네트워크로터 video playlist 데이터를 가져온다. 새로운 메소드는 respository에서 video playlist를 로드한다.

```
/**
* Refresh data from the repository. Use a coroutine launch to run in a
* background thread.
*/
private fun refreshDataFromRepository() {
   viewModelScope.launch {
       try {
           videosRepository.refreshVideos()
           _eventNetworkError.value = false
           _isNetworkErrorShown.value = false

       } catch (networkError: IOException) {
           // Show a Toast error message and hide the progress bar.
           if(playlist.value!!.isEmpty())
               _eventNetworkError.value = true
       }
   }
}
```

#### 3) DevByteViewModel 클래스의 init 블록에서 refreshDataFromNetwork() 메소드 호출을 refreshDataFromRepository()로 변경한다. 이 메소드는 network로부터 직접 가져오는 것이 아니라 repository로부터 video playlist를 가지고 온다

```
init {
   refreshDataFromRepository()
}
```

#### 4) DevByteViewModel 클래스에서 _playlist 프로퍼티와 backing property인 playlist를 삭제한다

```
// 삭제할 코드
private val _playlist = MutableLiveData<List<Video>>()
...
val playlist: LiveData<List<Video>>
   get() = _playlist
```

#### 5) DevByteViewModel 클래스에서 videosRepository 객체를 인스턴스화 한 이후에 repository로 부터 얻은 video list LiveData를 val playlist 변수에 할당한다.

```
val playlist = videosRepository.videos
```

#### 6) 앱을 실행한다. 앱은 이전과 같이 실행되지만 이제 DevBytes playlist를 network에서 가져오고 Room database에 저장한다. playlist는 network에서 직접 가져오는게 아니라 Room database로부터 가져와서 화면에 표시한다.

#### 7) 차이점을 알아보기 위해 비행기 모드를 디바이스에서 활성화한다 

#### 8) 앱을 다시 실행한다. \'Network Error\' 토스트 메세지가 뜨지 않는 것을 확인할 수 있다. 대신 playlist를 offline cache에서 가져오고 화면에 나타낸다

#### 9) 디바이스에서 비행기 모드를 끈다

#### 10) 앱을 종료하고 다시 실행한다. 앱은 background에서 network request를 실행하는 동안, offline cache로부터 playlist를 로드한다. 

만약 network로부터 새로운 데이터가 