# Timer2 - 화면 시간 모니터링 앱

Kotlin과 Gradle 9.2.1로 만든 Android 화면 시간 모니터링 앱입니다.

## 기능

이 앱은 다음과 같이 동작합니다:

- **화면이 켜지면**: 타이머를 0으로 만들고 시작합니다
- **화면이 꺼지면**: 타이머를 0으로 만들고 종료합니다
- **타이머가 LIMIT분이 되면**: 알림을 보내고 타이머를 0으로 만듭니다. 이때 타이머는 계속 켜져있습니다
- **사용자 제어**: 이 기능을 on/off 하고 LIMIT를 수정할 수 있습니다 (기본값 30분)

## 기술 스택

- **언어**: Kotlin
- **빌드 도구**: Gradle 9.2.1
- **Android SDK**: 
  - Compile SDK: 35
  - Min SDK: 26 (Android 8.0)
  - Target SDK: 35
- **주요 라이브러리**:
  - AndroidX Core KTX 1.15.0
  - AndroidX AppCompat 1.7.0
  - Material Design Components 1.12.0
  - ConstraintLayout 2.2.0
  - Lifecycle Service 2.8.7

## 프로젝트 구조

```
app/src/main/
├── java/com/example/timer2/
│   ├── MainActivity.kt           # 메인 UI 및 설정 화면
│   ├── AppPreferences.kt         # SharedPreferences 관리
│   ├── ScreenStateReceiver.kt    # 화면 ON/OFF 감지
│   ├── TimerService.kt           # 타이머 관리 서비스
│   └── NotificationHelper.kt     # 알림 생성 및 전송
├── res/
│   ├── layout/
│   │   └── activity_main.xml     # 메인 화면 레이아웃
│   ├── values/
│   │   ├── strings.xml           # 한국어 문자열 리소스
│   │   ├── colors.xml            # 색상 정의
│   │   └── themes.xml            # 앱 테마
│   └── mipmap-*/                 # 앱 아이콘
└── AndroidManifest.xml           # 앱 매니페스트
```

## 주요 컴포넌트

### 1. MainActivity
- 앱의 메인 화면
- 기능 활성화/비활성화 토글
- 시간 제한 설정 입력
- 현재 타이머 상태 표시

### 2. ScreenStateReceiver
- BroadcastReceiver로 화면 ON/OFF 이벤트 감지
- 화면 켜질 때: 타이머 리셋 및 시작
- 화면 꺼질 때: 타이머 리셋 및 정지

### 3. TimerService
- Foreground Service로 백그라운드에서 타이머 실행
- 1초마다 경과 시간 업데이트
- 제한 시간 도달 시 알림 전송 및 타이머 리셋
- 타이머는 알림 후에도 계속 실행

### 4. NotificationHelper
- 알림 채널 생성 및 관리
- 제한 시간 도달 시 알림 전송

### 5. AppPreferences
- SharedPreferences를 사용한 설정 저장
- 저장 항목: 활성화 상태, 시간 제한, 경과 시간, 화면 상태

## 권한

앱이 요구하는 권한:

- `FOREGROUND_SERVICE`: 백그라운드 타이머 실행
- `FOREGROUND_SERVICE_SPECIAL_USE`: 특수 목적 포그라운드 서비스
- `POST_NOTIFICATIONS`: Android 13+ 알림 전송
- `WAKE_LOCK`: 정확한 타이머 동작

## 빌드 방법

```bash
# 프로젝트 디렉토리로 이동
cd timer2

# Debug APK 빌드
./gradlew assembleDebug

# Release APK 빌드
./gradlew assembleRelease
```

**참고**: 현재 빌드 환경에서 `dl.google.com` (Google Maven 저장소)에 대한 접근이 차단되어 있어 빌드가 완료되지 않을 수 있습니다. 이 경우 Android Studio에서 프로젝트를 열거나 네트워크 제한을 해제해야 합니다.

## 설치 및 실행

1. APK 빌드 완료 후 `app/build/outputs/apk/debug/app-debug.apk` 생성
2. Android 기기에 APK 설치
3. 앱 실행 후 기능 활성화
4. 시간 제한 설정 (기본값: 30분)
5. 화면을 켜고 끄면서 타이머 동작 확인

## 알림 동작

타이머가 설정한 시간(기본 30분)에 도달하면:
1. "화면 시간 알림" 제목의 알림 표시
2. "화면을 X분 동안 계속 켜놨습니다!" 메시지 표시
3. 타이머가 자동으로 0으로 리셋
4. 타이머는 계속 실행되며 화면이 꺼질 때까지 카운트 계속

## 라이선스

이 프로젝트는 교육 및 개인 사용 목적으로 만들어졌습니다.