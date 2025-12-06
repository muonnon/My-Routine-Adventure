# 📊 데이터 모델 문서

## 데이터 파일 개요

| 파일명 | 형식 | 용도 |
|--------|------|------|
| `routines_data.txt` | 텍스트 (파이프 구분) | 루틴 데이터 저장 |
| `player_data.dat` | Java 직렬화 | 플레이어 데이터 저장 |
| `boss_data.txt` | Java 직렬화 | 보스 데이터 저장 |

---

## routines_data.txt

### 형식
```
ID|이름|태그|요일목록|완료날짜Map
```

### 필드 설명
| 필드 | 설명 | 예시 |
|------|------|------|
| ID | 루틴 고유 식별자 | R1, R2, R10 |
| 이름 | 루틴 이름 | 아침 운동 |
| 태그 | 카테고리 | 운동, 공부, 생활 |
| 요일목록 | 반복 요일 (쉼표 구분) | 월,화,수,목,금 |
| 완료날짜Map | 요일:날짜 형식 (세미콜론 구분) | 월:2025-12-05;화:2025-12-03 |

### 구분자 상수
```java
SEPARATOR = "|"              // 필드 구분자
DAY_SEPARATOR = ","          // 요일 목록 구분자
COMPLETION_ENTRY_SEPARATOR = ";"  // 완료 항목 구분자
COMPLETION_KV_SEPARATOR = ":"     // 요일-날짜 구분자
```

### 예시 데이터
```
R1|아침 운동|운동|월,화,수,목,금|월:2025-12-05;화:2025-12-03
R2|독서 30분|공부|월,수,금|월:2025-12-02
R3|물 2L 마시기|생활|월,화,수,목,금,토,일|화:2025-12-05
R10|코딩 연습|공부|월,화,수,목,금|null
```

### 완료날짜Map 파싱 로직
```java
// "월:2025-12-05;화:2025-12-03" → Map<String, LocalDate>
if (!"null".equals(completionStr) && !completionStr.isEmpty()) {
    String[] entries = completionStr.split(";");  // ["월:2025-12-05", "화:2025-12-03"]
    for (String entry : entries) {
        String[] kv = entry.split(":");  // ["월", "2025-12-05"]
        String day = kv[0];
        LocalDate date = LocalDate.parse(kv[1]);
        lastCompletedDateMap.put(day, date);
    }
}
```

---

## player_data.dat (직렬화)

### Player 클래스 구조
```java
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;                              // 플레이어 이름
    private int level;                                // 현재 레벨
    private int currentExp;                           // 현재 경험치
    private int maxExp;                               // 레벨업 필요 경험치
    private int gold;                                 // 보유 골드
    private List<Item> inventory;                     // 인벤토리
    private Map<Item.EquipSlot, Item> equippedItems;  // 장착 장비
    private Set<LocalDate> streakDates;               // 루틴 완료 날짜 기록
    private String weaknessRoutine;                   // 이번 달 취약 루틴
}
```

### 직렬화 포함 객체
```
Player
├── List<Item> inventory
│   └── Item (Serializable)
│       ├── String name
│       ├── int price
│       ├── EquipSlot slot (enum)
│       ├── EffectType effectType (enum)
│       └── int effectValue
├── Map<EquipSlot, Item> equippedItems
└── Set<LocalDate> streakDates
```

### 저장 시점
- 루틴 완료 시 (`completeRoutine`)
- 프로그램 종료 시 (`saveAllData`)

---

## boss_data.txt (직렬화)

### Boss 클래스 구조
```java
public class Boss implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int month;           // 보스가 출현한 월 (1~12)
    private String name;         // 보스 이름
    private String desc;         // 보스 설명
    private int maxHp;           // 최대 HP
    private int currentHp;       // 현재 HP
    private boolean isDefeated;  // 처치 여부
    private String imagePath;    // 보스 이미지 경로
    private String hitImagePath; // 피격 이미지 경로
}
```

### 월 변경 시 초기화 로직
```java
private void checkMonthChange() {
    if (boss.getMonth() != LocalDate.now().getMonthValue()) {
        // 1. 지난달 보스 처치 실패 시 배드 엔딩
        if (!boss.isDefeated() && dashboard != null) {
            dashboard.showStoryDialog("😢 토벌 실패", boss.getBadStory());
        }
        
        // 2. 취약 루틴 초기화
        if (player != null) {
            player.setWeaknessRoutine(null);
        }
        
        // 3. 새 보스 소환
        boss.spawnBossForThisMonth();
    }
}
```

---

## 데이터 흐름

### 저장 흐름
```
사용자 액션 (루틴 완료, 프로그램 종료)
            │
            ▼
    RoutineManager
            │
    ┌───────┼───────┐
    ▼       ▼       ▼
Routine  Player   Boss
(메모리)  (메모리)  (메모리)
    │       │       │
    ▼       ▼       ▼
FileManager.save*()
    │       │       │
    ▼       ▼       ▼
routines_  player_  boss_
data.txt   data.dat data.txt
```

### 로드 흐름
```
프로그램 시작
    │
    ▼
RoutineManager 생성자
    │
    ▼
FileManager.load*()
    │
    ┌───────┼───────┐
    ▼       ▼       ▼
routines_  player_  boss_
data.txt   data.dat data.txt
    │       │       │
    ▼       ▼       ▼
List<Routine>  Player   Boss
    │       │       │
    ▼       ▼       ▼
allRoutines  player   boss
(Map에 저장)  (필드)   (필드)
```

---

## 데이터 무결성

### 루틴 ID 관리
- 형식: "R" + 숫자 (예: R1, R2, R10)
- 로드 시 최대 ID를 찾아 카운터 복원
```java
int maxId = 0;
for (Routine routine : loadedRoutines) {
    int idNum = Integer.parseInt(routine.getId().substring(1));
    if (idNum > maxId) maxId = idNum;
}
routineCounter = maxId;
```

### 완료 날짜 검증
- 오늘 날짜와 비교하여 완료 여부 판단
- 과거 완료 기록은 유지 (통계용)
```java
public boolean isCompletedForDay(String day) {
    LocalDate lastDate = lastCompletedDate.get(day);
    return lastDate != null && lastDate.equals(LocalDate.now());
}
```

### 직렬화 버전 호환
- `serialVersionUID` 사용으로 버전 관리
- 필드 추가/삭제 시 UID 업데이트 필요
```java
private static final long serialVersionUID = 1L;  // Player
private static final long serialVersionUID = 2L;  // Item, Inventory
```

---

## 백업 및 복구

### 수동 백업
```bash
# 데이터 파일 백업
copy routines_data.txt routines_data_backup.txt
copy player_data.dat player_data_backup.dat
copy boss_data.txt boss_data_backup.txt
```

### 복구
```bash
# 백업 파일에서 복원
copy routines_data_backup.txt routines_data.txt
copy player_data_backup.dat player_data.dat
copy boss_data_backup.txt boss_data.txt
```

### 초기화 (새 게임)
```bash
# 데이터 파일 삭제
del routines_data.txt
del player_data.dat
del boss_data.txt
```
