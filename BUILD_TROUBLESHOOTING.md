# 빌드 문제 해결 방법

## 문제

현재 빌드 환경에서 `dl.google.com` 도메인이 차단되어 있어 Android Gradle Plugin (AGP)을 다운로드할 수 없습니다.

```
Plugin [id: 'com.android.application'] was not found in any of the following sources
```

## 해결 방법

### 방법 1: Android Studio 사용 (권장)

1. Android Studio를 설치합니다
2. "Open an Existing Project"를 선택하고 이 프로젝트를 엽니다
3. Android Studio가 자동으로 필요한 의존성을 다운로드합니다
4. Build > Make Project를 실행합니다

### 방법 2: 네트워크 제한 해제

빌드 환경에서 다음 도메인에 대한 접근을 허용합니다:
- `dl.google.com` (Google Maven 저장소)
- `repo.maven.apache.org` (Maven Central)

### 방법 3: 프록시 또는 미러 사용

`gradle.properties`에 프록시 설정을 추가:

```properties
systemProp.http.proxyHost=your.proxy.host
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=your.proxy.host
systemProp.https.proxyPort=8080
```

또는 `settings.gradle.kts`에서 미러 저장소를 설정할 수 있습니다.

### 방법 4: 오프라인 빌드 (의존성 캐시 필요)

다른 환경에서 먼저 빌드하여 Gradle 캐시를 생성한 후:

```bash
# 의존성 다운로드
./gradlew --refresh-dependencies

# 캐시 디렉토리 복사
cp -r ~/.gradle/caches /path/to/restricted/environment/
```

## 빌드가 성공하면

빌드 출력물은 다음 위치에 생성됩니다:

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

APK 파일을 Android 기기에 설치하여 앱을 실행할 수 있습니다.
