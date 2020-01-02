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

