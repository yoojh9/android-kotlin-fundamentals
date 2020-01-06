# 10-3 Design for everyone

## 1.  Add support for right-to-left (RTL) languages
left-to-right(LTR)과 right-to-left(RTL) 언어의 주요 차이점은 표시되는 내용의 방향이다. UI 방향이 LTR에서 RTL로 변경될 때 종종 미러링이라고 부른다. 
미러링은 텍스트, 텍스트 필드 아이콘, 레이아웃, 화살표와 같은 방향이 있는 아이콘을 포함하여 대부분의 화면에 영향을 준다.
숫자(시계, 전화번호), 방향이 없는 아이콘(비행기 모드, WiFi), 재생 컨트롤 및 대부분의 차트 및 그래프와 같은 항목들은 미러링 되지 않는다

RTL 텍스트 방향의 언어를 사용하는 사용자는 전 세계 10억명 이상이다. 안드로이드 개발자는 GDG Finder 앱에 RTL 언어를 지원해야 한다

### Step 1: Add RTL support
이 단계에서 GDG Finder 앱이 RTL 언어를 지원하도록 만든다

#### 1) Android Manifest 파일을 연다

#### 2) <application> 섹션에서 아래와 같이 RTL을 지원하는 코드를 추가한다

```
<application
        ...
        android:supportsRtl="true">
```

#### 3) activity_main.xml을 열고 Design 탭을 선택한다

#### 4) 