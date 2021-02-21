# 06-1 Create a Room database
 Room은 Android Jetpack의 데이터베이스 라이브러리이다. 
 Room library는 SQLite 데이터베이스의 가장 최상단 abstraction layer이다.
 SQLite를 직접 사용하는 대신 Room을 사용하면 설정, 구성, 데이터베이스와의 interaction 등을 단순화 시켜준다.
 Room은 SQL 언어를 사용하는 대신 일반 함수 호출을 사용하여 데이터베이스를 조작할 수 있다.
 
 <image src="./images/room_database.png" width="40%" height="40%"/>
 
## 1. Inspect the starter app
 ### Step1: Inspect the starter app
 
 #### 1) Gradle 파일을 살펴보자
 
  - **The project Gradle file** : project-level build.gradle 파일은 특정 라이브러리 version을 명시한다. 
  - **The module Gradle file** : Room을 포함한 모든 Android Jetpack 라이브러리 및 코루틴에 대한 디펜던시를 확인할 수 있다
 
 #### 2) packages와 UI를 살펴본다
 
  - **The database package**에는 Room 데이터베이스와 관련된 코드들이 있다
  - **The sleepquality and sleeptracker package**에는 fragment, view model, view model factory가 각각의 스크린마다 존재한다
 
 #### 3) Util.kt 파일을 살펴보자. sleep-quality 데이터를 표시하는데 필요한 함수들을 포함하고 있다
 
 #### 4) android Test 폴더 (SleepDatabaseTest.kt)에서 database가 의도한대로 작동는지 테스트 해볼 수 있다

<br><br>

## 2. Create the SleepNight entity
 - **entity**는 데이터베이스에 저장할 개체나 개념 및 속성을 나타낸다. entity 클래스는 테이블을 의미하고, 엔티티 클래스의 각 인스턴스는 테이블의 row를 나타낸다. 
엔티티 클래스에는 Room이 데이터베이스의 정보를 표시하고 상호 작용하는 방법을 알려주는 mapping을 가지고 있다. 각 프로퍼티는 컬럼을 의미한다
 - **query**는 하나의 데이터베이스 테이블 또는 결합된 테이블에서 데이터를 요청하거나 데이터에 대한 처리를 요청한다. 일반적인 query는 entity를 create, read, update, delete 한다. 이번 실습에서는 record 내 sleep night 데이터를 모두 읽고 start time을 기준으로 정렬해본다.
 
 앱의 User Experience는 일부 데이터를 로컬로 유지함으로써 큰 이점을 얻을 수 있다. 또한 관련 데이터를 캐싱하면 사용자가 오프라인에서도 앱을 사용할 수 있게 해준다. 
 앱이 서버에 의존하는 경우 캐싱을 통해 사용자는 오프라인 상태에서 로컬로 지속되는 콘텐츠를 수정할 수 있으며, 앱이 다시 연결되면 캐시 된 변경사항을 백그라운드에서 원활하게 서버에 동기화 시킬 수 있다.
 
 **Room**은 kotlin data class에서 SQlite 테이블로 저장할 수 있는 entity로 변환하고, function을 SQL query로 변환한다.
 
 각 엔티티는 어노테이션이 달린 data class로 정의하고 DAO(data access object)와 상호작용 해야한다. 
 Room은 annotated된 클래스를 사용하여 데이터베이스의 테이블을 생성하고 데이터베이스에서 작동하는 쿼리를 생성한다.
 
 
 <image src="./images/room_and_dao.png" width="500"/>
 
 ### Step 1: Create the SleepNight entity
  이 작업에서는 night of sleep을 어노테이션이 달린 data class로 정의한다. 이것은 database entity임을 의미한다. 
  start time, end time, quality rating을 기록해야 하며 night를 구별할 유니크한 ID도 필요하다
  
  #### 1) database 패키지에서 SleepNight.kt 파일을 연다
  
  #### 2) ID, start time(in milliseconds), end time(in milliseconds), 숫자형의 sleep-quality rating을 파라미터로 하는 SleepNight 데이터 클래스를 생성한다
   - sleepQuality는 -1로 초기화한다. -1은 어떠한 등급도 아니다.
   - start time은 현재 시간을 milliseconds로 나타낸 값으로 초기화한다
   - end time도 초기화하는데, 아직 종료 시간이 기록되지 않았으므로 start time으로 초기화한다.
  
  ```
  data class SleepNight(
         var nightId: Long = 0L,
         val startTimeMilli: Long = System.currentTimeMillis(),
         var endTimeMilli: Long = startTimeMilli,
         var sleepQuality: Int = -1
  )
  ```
  
  <br>
  
  #### 3) 클래스 선언 전에 @Entity 어노테이션을 추가한다. tableName은 daily_sleep_quality_table로 지정한다
   - Entity는 몇가지 argument를 가질 수 있는데, argument 없이 그냥 @Entity를 이용하면 class 이름과 같은 이름이 table 이름이 된다.
   - 이번 실습에서는 tableName argument를 이용하여 테이블의 이름을 daily_sleep_quality_table로 지정해본다.
   - tableName 인자는 optional이나 tableName을 명시하는 것을 권장한다
   
   ```
   @Entity(tableName = "daily_sleep_quality_table")
   data class SleepNight(...)
   ```
  <br>
  
  #### 4) nightId를 primary key로 지정하기 위해, nightId 위에 @PrimaryKey 어노테이션을 추가한다. 파라미터인 autoGenerate를 true로 설정하여 Room이 각 엔티티의 ID를 생성하도록 한다. 각각의 night ID는 unique가 보장된다
  
  ```
  @PrimaryKey(autoGenerate = true)
  var nightId: Long = 0L,...
  ```
  
  <br>
  
  #### 5) 남아있는 프로퍼티에 @ColumnInfo 어노테이션을 추가한다. 파라미터로 name 프로퍼티를 사용하여 column 이름을 지정할 수 있다.
  
  ```
  @Entity(tableName = "daily_sleep_quality_table")
  data class SleepNight(
         @PrimaryKey(autoGenerate = true)
         var nightId: Long = 0L,
  
         @ColumnInfo(name = "start_time_milli")
         val startTimeMilli: Long = System.currentTimeMillis(),
  
         @ColumnInfo(name = "end_time_milli")
         var endTimeMilli: Long = startTimeMilli,
  
         @ColumnInfo(name = "quality_rating")
         var sleepQuality: Int = -1
  )
  ```

<br><br>

## 3. Create the DAO
 이번 단계에서는 DAO(data access object)를 정의한다. 안드로이드에서 DAO는 데이터베이스 insert, delete 및 update를 위한 편리한 method를 제공한다
 
 Room 데이터베이스를 사용하면 Kotlin 함수를 정의하고 호출함으로써 database를 query 할 수 있다. 이 코틀린 함수는 SQL 쿼리에 매핑된다. 
 이런 mapping은 DAO에서 어노테이션을 사용하여 정의할 수 있고, 이 mapping을 통해 Room은 필요한 코드를 생성한다
 
 DAO는 데이터베이스에 액세스 하기 위한 사용자 인터페이스를 정의하는 것으로 단순히 생각하면 된다. 
 일반적인 데이터베이스 작업을 위해 Room 라이브러리는 @Insert, @Delete, @update와 같은 편리한 어노테이션을 제공한다.
 그 밖에 SQLite 기반의 쿼리를 작성할 수 있는 @Query 어노테이션이 있다.
 
 추가로 안드로이드 스튜디오에서 쿼리를 만들 때 컴파일러가 SQL 쿼리에서 구문 오류를 확인한다. 
 
 sleep-tracker 데이터베이스에는 다음 기능이 있어야 한다
    - **Insert** new nights
    - **Update** an existing night to update an end time and a quality rating
    - **Get** a specific night based on its key
    - **Get all nights**, so you can display them
    - **Get the most recent night**
    - **Delete** all entries in the database
 
 <br>
 
 ### Step 1: Create the SleepDatabase DAO
  #### 1) database 패키지에서 SleepDatabaseDao.kt를 연다.
  
  #### 2) SleepDatabaseDao interface에 @Dao 어노테이션이 붙어있는 것을 확인한다. 모든 DAO는 @Dao 어노테이션 키워드가 필요하다
  
  ```
  @Dao
  interface SleepDatabaseDao {}
  ```
  
  <br>
  
  #### 3) interface의 body 부분에 @Insert 어노테이션을 추가하고 @Insert 어노테이션 아래에 insert() 함수를 추가한다.
   - insert() 함수에는 Entity 클래스인 SleepNight 인스턴스를 인자로 사용한다
   - 코틀린 코드에서 insert() 함수를 호출하면 Room은 데이터베이스에 SleepNight를 추가하는 코드를 생성한다. 코틀린 코드 insert()를 호출하면 Room은 테이블에 entity를 추가하는 SQL 쿼리를 실행시킨다.
   
   ```
   @Insert
   fun insert(night: SleepNight)
   ```
   
   <br>
   
   #### 4) update() 함수에 @Update 어노테이션을 추가한다. 업데이트 된 엔티티는 전달되는 엔티티와 동일한 키를 가진 엔티티이다. 엔티티의 일부 속성 또는 전체 엔티티를 업데이트 할 수 있다
   
   ```
   @Update
   fun update(night: SleepNight)
   ```
    
   <br>
   
   #### 5) Long 타입의 key를 인자로 하고 nullable한 SleepNight을 리턴하는 get() 함수를 추가한다. 상단에 @Query 어노테이션을 추가한다
   
   ```
    @Query
    fun get(key: Long): SleepNight?
   ```
   
   #### 6) @Query는 SQLite 쿼리인 문자열 파라미터가 필요하다.
   - daily_sleep_quality_table로부터 모든 column을 가져온다
   - **WHERE** 조건을 사용하여 nightId와 key가 같은 데이터를 가져온다.
   - **:key**: : 표기법을 사용하여 function의 argument인 key를 쿼리에서 참조한다 
   
   ```
   ("SELECT * from daily_sleep_quality_table WHERE nightId = :key")
   ```
   
   <br>
   
   #### 7) @Query를 어노테이션으로 하는 clear() 함수를 만든다. 이 함수는 daily_sleep_quality_table의 모든 데이터를 DELETE 하지마 테이블을 지우진 않는다.
   @Delete 어노테이션은 하나의 아이템을 삭제한다. @Delete는 특정 항목을 삭제하는 데 유용하지만 테이블에서 모든 아이템을 지우는 데는 효율적이지 않다.
   
   ```
   @Query("DELETE FROM daily_sleep_quality_table")
   fun clear()
   ```
   
   <br>
   
   #### 8) getTonight() 함수에 @Query 어노테이션을 추가하여 nullable한 SleepNight을 리턴하는 함수를 만든다. 오늘밤의 데이터를 얻기 위해서 nightId 키를 오름차순으로 정렬한 값 중 LIMIT 1을 사용하여 하나의 요소를 전달한다
   
   ```
   @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
   fun getTonight(): SleepNight?
   ```
   
   <br>
   
   #### 9) @Query 어노테이션을 getAllNights() 함수에 추가한다
   - daily_sleep_quality_table의 모든 컬럼을 가져오고, nighId를 최신값으로 정렬한다.
   - getAllNights()가 SleepNight 엔티티의 리스트를 LiveData로 리턴한다.
   - Room은 이 LiveData를 업데이트 된 상태로 유지하므로 데이터를 한번만 명시적으로 가져오면 된다.
   - 이 작업을 위해서는 androidx.lifecycle.LiveData를 import 해야한다.
    
   ```
   @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
   fun getAllNights(): LiveData<List<SleepNight>>
   ```

<br><br>

## 4. Create and test a Room database
 이번 단계에서는 이전 단계에서 작성한 엔티티 및 DAO를 사용하는 Room 데이터베이스를 생성한다.
 
 @Database 어노테이션이 달린 추상 데이터베이스 홀더 클래스를 만들어야 하며, 이 클래스는 데이터베이스가 존재하지 않는 경우 데이터베이스의 인스턴스를 생성하거나, 데이터베이스의 인스턴스가 이미 존재할 경우 데이터베이스에 대한 참조를 리턴하는 하나의 메소드를 가지고 있다.
 
 Room 데이터베이스를 얻는 것은 다소 복잡하므로 코드를 시작하기 전에 일반적인 프로세스는 다음과 같다
 
   - RoomDatabase를 상속하는 public abstract 클래스를 생성한다. 이 클래스는 database holder로서의 역할을 한다.
   - @Database 어노테이션을 추가한다. 인자로 데이터베이스의 엔티티들을 선언하고 버전 번호를 설정한다
   - companion object에서 SleepDatabaseDao를 리턴하는 추상 메소드 및 속성을 정의한다. 추상 메소드의 body는 Room이 생성한다.
   - 전체 앱에서 Room 데이터베이스의 인스턴스는 하나만 필요하므로 RoomDatabase는 싱글톤으로 만든다
   - Room의 database builder를 사용하여 데이터베이스가 존재하지 않을 경우 데이터베이스를 생성한다. 존재할 경우에는 데이터베이스의 인스턴스를 리턴한다
   
 Room 데이터베이스를 위한 코드는 거의 동일하므로 이 코드를 템플릿으로 사용할 수 있다
 
 <br>
 
 ### Step 1: Create the database
  #### 1) database 패키지에서 SleepDatabase.kt 파일을 연다
  
  #### 2) RoomDatabase를 상속한 abstract 클래스인 SleepDatabase 클래스를 생성하고, 위에 @Database 어노테이션을 붙인다.
  
  ```
  @Database()
  abstract class SleepDatabase : RoomDatabase() {}
  ```
  
  <br>
  
  #### 3) 위의 코드를 실행할 경우 entities와 version 파라미터가 없다는 error를 보게 된다. Room이 데이터베이스를 만드려면 @Database 어노테이션에 몇가지 argument가 필요하다
  
   - entities의 list 중 하나로 SleepNight를 전달한다
   - version을 1로 지정한다. 스키마가 변경될 때마다 버전 번호를 늘려야 한다
   - 스키마 버전의 히스토리 백업을 유지하지 않으려면 exportSchema를 false로 설정한다
   
   ```
   @Database(entities = [SleepNight::class], version = 1, exportSchema = false)
   ```
   
  <br>
  
  #### 4) database는 DAO에 대해 알기를 원하므로 class의 body 안에 SleepDatabaseDao 값을 리턴하는 abstract val을 선언한다. 물론 여러개의 DAO를 가질 수도 있다
  
  ```
  abstract val sleepDatabaseDao: SleepDatabaseDao
  ```  
  
  <br>
  
  #### 5) 그 아래에 **companion** object를 정의한다. companion object는 클래스를 인스턴스화 하지 않고도 데이터베이스를 생성하거나 가져올 수 있는 메소드에 액세스하게 해준다. 이 클래스의 유일한 목적은 데이터베이스를 제공하는 것이므로 클래스를 인스턴스화 할 이유가 없다
  
  ```
  companion object {}
  ```
  
  <br>
  
  #### 6) companion object 내에 database를 위한 private nullable 변수로 INSTANCE를 선언하고 null로 초기화 한다. INSTANCE 변수는 일단 데이터베이스가 생성되면 데이터베이스의 참조를 유지한다. 이렇게 하면 compute cost가 많이 드는 데이터베이스 connection을 반복적으로 열지 않아도 된다.
  
   - INSTANCE에 @Volatile 어노테이션을 추가한다. volatile 변수의 값은 절대 캐시되지 않으며 모든 읽고 쓰는 작업은 main memory에서 이루어진다.
   - 이를 통해 INSTANCE 값을 항상 최신으로 유지하고 모든 쓰레드의 실행을 동일하게 유지한다. 
   - 하나의 스레드에서 INSTANCE로 변경한 내용은 다른 모든 스레드에서 즉시 볼 수 있으며, 두 개의 스레드가 각각 캐시에서 동일한 엔티티를 업데이트 하여 문제가 발생하는 상황이 나타나지 않는다
   
   ```
   @Volatile
   private var INSTANCE: SleepDatabase? = null
   ```
   
   <br>
   
  #### 7) INSTANCE 선언 밑에 SleepDatabase를 리턴 타입으로 하고 데이터베이스 빌더에 필요한 Context를 파라미터로 하는 getInstacne() 메소드를 선언한다. 
   - getInstance()가 아직 아무것도 반환하지 않기 때문에 오류가 표시된다
  
  ```
  fun getInstance(context: Context): SleepDatabase {}
  ```
  
  <br>
  
  #### 8) getInstance() 메소드에 synchronized{} 블럭을 추가하고 context에 액세스 할 수 있도록 **this**를 전달한다. 
   - 여러 스레드가 동시에 하나의 데이터베이스 인스턴스를 요청할 수 있으므로 하나가 아닌 두 개의 데이터베이스가 생성될 수도 있다. 이 문제는 sample app에서는 발생하지 않지만 더 복잡한 앱에서는 발생할 수도 있다
   - 데이터베이스를 가져오는 코드를 synchronized 블럭에 wrapping 하면 한 번에 하나의 실행 스레드만이 코드 블럭에 들어갈 수 있으므로 데이터베이스가 한번만 초기화 된다
  
  ```
  synchronized(this) {}
  ```
  
  <br>
  
  #### 9) synchronized 블럭에서 INSTANCE의 현재 값을 instance 지역 변수로 복사한다. 이는 로컬 변수에서만 사용할 수 있는 코틀린의 [스마트 캐스트](https://kotlinlang.org/docs/typecasts.html)를 활용하기 위한 것이다.
  
  ```
  var instance = INSTANCE
  ```
  
  <br>
  
  #### 10) synchronized 블럭의 마지막에서 instance를 리턴한다
  
  ```
  return instance
  ```
  
  <br>
  
  #### 11) return 문 위에 if 문장을 추가하여 데이터베이스 인스턴스가 아직 생성되지 않았는지 확인하기 위해 instance null 체크를 실행한다.
  
  ```
  if(instance == null) {}
  ```
  
  <br>
  
  #### 12) 만약 instance가 null이면 database builder를 사용하여 database를 얻어온다. if문의 body에서 Room.databaseBuilder를 호출하고 넘겨받은 context와 database 클래스, 그리고 database 이름인 sleep_history_database를 파라미터로 넘긴다.
   - 에러를 제거하기 위해 migration strategy와 build() 함수를 다음 단계에서 추가한다.
   
  ```
  instance = Room.databaseBuilder(
                             context.applicationContext,
                             SleepDatabase::class.java,
                             "sleep_history_database")
  ```
  
  <br>
  
  #### 13) builder에 migration 전략을 추가한다. .fallbackToDestructiveMigration()을 사용한다.
   - 일반적으로 스키마가 변경될 때 마이그레이션 전략을 제공하는 마이그레이션 객체를 제공한다. 
   - migration object는 어떻게 old 스키마에 있는 모든 row를 가져와서 새 스키마의 행으로 변환하여 데이터가 손실되지 않도록 정의하는 객체이다.
   - Migration은 이번 코드랩 범위를 넘어가며, 단순한 solution으로는 데이터베이스를 destroy하고 rebuild하는 전략이 있다. 이 전략은 데이터를 모두 잃게 된다.
  
  ```
  .fallbackToDestuctiveMigration()
  ```
  
  <br>
  
  #### 14) 마지막에 .build()를 호출한다
  
  ```
  .build()
  ```
  
  <br>
  
  #### 15) if문에 마지막 단계로 INSTANCE = instance를 할당한다
  ```
  INSTANCE = instance
  ```
  
  <br>
  
  #### 16) 최종 코드는 아래와 같다
  ```
  @Database(entities = [SleepNight::class], version = 1, exportSchema = false)
  abstract class SleepDatabase : RoomDatabase() {
  
      abstract val sleepDatabaseDao: SleepDatabaseDao
  
      companion object {
  
          @Volatile
          private var INSTANCE: SleepDatabase? = null
  
          fun getInstance(context: Context): SleepDatabase {
              synchronized(this){
                  var instance = INSTANCE
                  if(instance == null) {
                      instance = Room.databaseBuilder(
                          context.applicationContext,
                          SleepDatabase::class.java,
                          "sleep_history_database"
                      )
                          .fallbackToDestructiveMigration()
                          .build()
  
                      INSTANCE = instance
                  }
                  return instance
              }
          }
      }
  }
  ```
  
  이 코드는 컴파일 되고 실행되지만 실제로 작동하는지는 알 수 없다. 따라서 기본적인 테스트 코드 추가하기에 좋다
  

 <br>
 
 ### Step 2: Test the SleepDatabase
 - 이 단계에서는 제공된 테스트를 실행하여 데이터베이스가 작동하는지 확인한다. 이를 통해 데이터베이스가 구축되기 전에 데이터베이스가 작동하는지 확인할 수 있다. 제공된 코드는 간단하다.
 - starter app은 androidTest 폴더를 포함하고 있다. androidTest 폴더는 안드로이드 장치와 관련된 unit test를 포함하고 있다. 물론 안드로이드 프레임워크와 관련이 없는 순수한 단위 테스트를 작성하고 실행할 수도 있다
 
 #### 1) androidTest 폴더에서 SleepDatabaseTest 파일을 열어서 주석 처리 된 코드의 주석을 지우고 코드를 살펴보자
 
   - SleepDatabaseTest는 test 클래스이다.
   - @RunWith 어노테이션은 테스트를 설정하고 실행하는 프로그램인 test runner를 식별한다
   - 먼저 @Before 어노테이션이 붙은 함수가 실행되고, SleepDatabaseDao를 사용하여 "in-memory"에 SleepDatabase를 만든다 "in-memory"의 의미는 데이터베이스가 파일 시스템에 저장되는게 아니므로 test가 실행되면 삭제된다
   - 또한 in-memory 데이터베이스를 빌드할 때 코드는 다른 특정 메소드인 allowMainThreadQueries를 호출한다. main thread에서 쿼리를 실행하려고 하면 기본적으로 에러가 발생하지만 이 방법을 사용하면 메인 쓰레드에서 테스트를 실행할 수 있으며 이는 테스트 중에만 수행해야 한다
   - @Test 주석이 달린 테스트 메소드를 통해 create, insert, retrieve 테스트를 진행하고 검증할 수 있다. 문제가 발생하면 exception이 발생한다. 
   - 테스트가 완료되면 @After 어노테이션이 붙은 함수가 실행되어 데이터베이스를 닫는다

 <br>
 
  #### 2) 테스트 파일을 오른쪽 클릭하여 Run 'SleepDatabaseTest'를 선택한다
  
  #### 3) 테스트를 실행하고 SleepDatabaseTest 창에서 모든 테스트가 통과되었는지 확인한다
  
    
   <image src="./images/test.png" width="500"/>
