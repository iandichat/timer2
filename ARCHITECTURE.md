# 앱 동작 흐름도

## 전체 아키텍처

```
┌─────────────────┐
│   MainActivity  │ ◄── 사용자가 설정 변경
└────────┬────────┘
         │
         ├─ Enable/Disable 토글
         ├─ 시간 제한 설정
         └─ 상태 표시
         │
         ▼
┌─────────────────┐
│ AppPreferences  │ ◄── SharedPreferences에 설정 저장
└────────┬────────┘
         │
         ▼
┌──────────────────────┐
│   TimerService       │ ◄── Foreground Service로 실행
└──────────┬───────────┘
           │
           ├─ 타이머 카운트 (매초)
           ├─ 제한 시간 체크
           └─ 알림 전송
           │
           ▼
┌───────────────────────┐
│ NotificationHelper    │ ◄── 알림 생성 및 전송
└───────────────────────┘

         ┌──────────────────────┐
         │ ScreenStateReceiver  │ ◄── 화면 ON/OFF 브로드캐스트 수신
         └──────────┬───────────┘
                    │
                    └─ TimerService에 액션 전달
```

## 화면 켜짐 시 흐름

```
화면 ON
   │
   ▼
ScreenStateReceiver.onReceive()
   │
   ├─ 기능 활성화 확인
   │
   ├─ 화면 ON 상태 저장
   │
   ├─ 경과 시간 → 0으로 리셋
   │
   └─ TimerService 시작
       │
       ▼
TimerService.startTimer()
   │
   ├─ 시작 시간 기록
   │
   └─ 매초 실행:
       │
       ├─ 경과 시간 계산
       │
       ├─ SharedPreferences에 저장
       │
       ├─ MainActivity에 브로드캐스트
       │
       └─ 제한 시간 체크
           │
           ├─ 미도달 → 계속 카운트
           │
           └─ 도달 →
               │
               ├─ NotificationHelper.sendLimitNotification()
               │   │
               │   └─ 알림 표시: "화면을 X분 동안 계속 켜놨습니다!"
               │
               ├─ 타이머 리셋 (0으로)
               │
               └─ 타이머 계속 실행 ◄─┐
                   │                 │
                   └─────────────────┘
```

## 화면 꺼짐 시 흐름

```
화면 OFF
   │
   ▼
ScreenStateReceiver.onReceive()
   │
   ├─ 기능 활성화 확인
   │
   ├─ 화면 OFF 상태 저장
   │
   ├─ 경과 시간 → 0으로 리셋
   │
   └─ TimerService에 SCREEN_OFF 액션 전달
       │
       ▼
TimerService.stopTimer()
   │
   ├─ Handler callbacks 제거
   │
   ├─ 경과 시간 → 0으로 리셋
   │
   └─ MainActivity에 업데이트 브로드캐스트
```

## 사용자 설정 변경 흐름

```
MainActivity
   │
   ├─ Enable Switch 토글
   │   │
   │   ├─ ON →
   │   │   │
   │   │   ├─ AppPreferences.setEnabled(true)
   │   │   │
   │   │   └─ TimerService 시작
   │   │
   │   └─ OFF →
   │       │
   │       ├─ AppPreferences.setEnabled(false)
   │       │
   │       └─ TimerService 정지
   │
   └─ 시간 제한 변경 + 저장 버튼
       │
       ├─ AppPreferences.setTimeLimit(value)
       │
       └─ 기능 활성화 상태면
           │
           ├─ TimerService 정지
           │
           └─ TimerService 재시작 (새 제한 시간 적용)
```

## 상태 전이도

```
        ┌─────────────┐
        │  비활성화    │
        │  (Disabled) │
        └──────┬──────┘
               │
        Enable Switch ON
               │
               ▼
        ┌─────────────┐
        │  화면 꺼짐   │ ◄───────┐
        │ (Screen Off)│         │
        └──────┬──────┘         │
               │                │
         화면 켜짐              │
               │                │
               ▼                │
        ┌─────────────┐         │
        │  타이머 실행 │         │
        │  (Running)  │         │
        │  0초부터     │         │
        │  카운트 시작 │         │
        └──────┬──────┘         │
               │                │
        ┌──────┴──────┐        │
        │             │         │
   화면 꺼짐    제한 시간 도달    │
        │             │         │
        └─────────────┼─────────┘
                      │
                      ▼
               ┌─────────────┐
               │  알림 전송   │
               │  타이머 리셋 │
               └──────┬──────┘
                      │
                      └─ 타이머 계속 실행
```

## 주요 이벤트

1. **앱 시작** → MainActivity 표시, 저장된 설정 로드
2. **기능 활성화** → TimerService 시작 (Foreground Service)
3. **화면 ON** → 타이머 0으로 리셋 및 카운트 시작
4. **화면 OFF** → 타이머 0으로 리셋 및 카운트 정지
5. **제한 시간 도달** → 알림 전송, 타이머 0으로 리셋, 계속 실행
6. **설정 변경** → SharedPreferences 업데이트, 필요시 서비스 재시작
