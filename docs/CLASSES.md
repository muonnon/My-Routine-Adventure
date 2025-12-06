# 📦 클래스 상세 문서

## 목차
1. [데이터 모델 클래스](#데이터-모델-클래스)
2. [매니저 클래스](#매니저-클래스)
3. [UI 클래스](#ui-클래스)
4. [유틸리티 클래스](#유틸리티-클래스)

---

## 데이터 모델 클래스

### Routine.java
루틴 데이터를 저장하는 핵심 모델 클래스입니다.

```java
public class Routine implements Serializable {
    private String id;                           // 고유 식별자 (예: "R1", "R2")
    private String name;                         // 루틴 이름
    private String tag;                          // 태그 (공부, 운동 등)
    private List<String> repeatDays;             // 반복 요일 ["월", "화", ...]
    private Map<String, LocalDate> lastCompletedDate;  // 요일별 마지막 완료 날짜
}
```

**주요 메서드:**
| 메서드 | 설명 |
|--------|------|
| `isCompletedForDay(String day)` | 해당 요일에 오늘 완료했는지 확인 |
| `completeForDay(String day)` | 해당 요일의 완료 날짜를 오늘로 갱신 |
| `getLastCompletedDateMap()` | 요일별 완료 날짜 Map 반환 |

---

### Player.java
플레이어 상태를 저장하는 모델 클래스입니다.

```java
public class Player implements Serializable {
    private String name;                         // 플레이어 이름
    private int level;                           // 현재 레벨
    private int currentExp;                      // 현재 경험치
    private int maxExp;                          // 레벨업 필요 경험치
    private int gold;                            // 보유 골드
    private List<Item> inventory;                // 인벤토리 (소유 아이템)
    private Map<Item.EquipSlot, Item> equippedItems;  // 장착 중인 아이템
    private Set<LocalDate> streakDates;          // 루틴 수행 날짜 기록
    private String weaknessRoutine;              // 이번 달 취약 루틴
}
```

**주요 메서드:**
| 메서드 | 설명 |
|--------|------|
| `gainExp(int exp)` | 경험치 획득 및 자동 레벨업 처리 |
| `buyItem(Item item)` | 아이템 구매 (골드 차감) |
| `equipItem(Item item)` | 아이템 장착 (슬롯 교체 포함) |
| `unequipItem(EquipSlot slot)` | 장비 해제 |
| `getTotalBonusDamage()` | 장착 장비의 공격력 보너스 합산 |
| `getTotalBonusExp()` | 장착 장비의 경험치 보너스 합산 |

**레벨업 공식:**
```
maxExp = 100 + 50 * (level - 1)
Lv.1: 100, Lv.2: 150, Lv.3: 200, ...
```

---

### Boss.java
월간 보스 데이터를 저장하는 모델 클래스입니다.

```java
public class Boss implements Serializable {
    private int month;           // 보스가 출현한 월 (1~12)
    private String name;         // 보스 이름
    private String desc;         // 보스 설명
    private int maxHp;           // 최대 HP (기본 100)
    private int currentHp;       // 현재 HP
    private boolean isDefeated;  // 처치 여부
    private String imagePath;    // 보스 이미지 경로
    private String hitImagePath; // 피격 이미지 경로
}
```

**주요 메서드:**
| 메서드 | 설명 |
|--------|------|
| `spawnBossForThisMonth()` | 현재 월에 맞는 보스 초기화 |
| `takeDamage(int damage)` | 데미지 적용, 처치 시 true 반환 |
| `getHappyStory()` | 보스 처치 성공 스토리 |
| `getBadStory()` | 보스 처치 실패 스토리 |

**월별 보스 컨셉 (예시):**
| 월 | 보스 이름 | 설명 |
|----|----------|------|
| 1월 | 희망찬 겨울 | 새해의 결심을 희망하는 겨울 |
| 2월 | 졸음의 초콜릿 몬스터 | 달콤한 잠으로 유혹하는 몬스터 |
| 12월 | 나태의 눈사람 | 새해의 결심을 얼려버리려는 눈사람 |

---

### Item.java
아이템 데이터를 저장하는 모델 클래스입니다.

```java
public class Item implements Serializable {
    private final String name;           // 아이템 이름
    private final int price;             // 가격
    private final EquipSlot slot;        // 장착 부위
    private final EffectType effectType; // 효과 종류
    private final int effectValue;       // 효과 수치
}
```

**EquipSlot enum:**
| 값 | 한글명 |
|----|--------|
| HEAD | 머리 |
| BODY | 몸통 |
| LEG | 다리 |
| WEAPON | 무기 |
| ETC | 기타 |

**EffectType enum:**
| 값 | 설명 |
|----|------|
| NONE | 효과 없음 |
| BOSS_ATTACK | 공격력 보너스 |
| EXP_BONUS | 경험치 획득량 보너스 |

---

### Inventory.java
인벤토리 관리를 담당하는 클래스입니다.

```java
public class Inventory implements Serializable {
    private final List<Item> items;                      // 보유 아이템
    private final Map<Item.EquipSlot, Item> equippedItems; // 장착 슬롯
}
```

**주요 메서드:**
| 메서드 | 설명 |
|--------|------|
| `addItem(Item item)` | 아이템 추가 |
| `equipItem(Item item)` | 아이템 장착 |
| `unequipItem(EquipSlot slot)` | 장비 해제 |

---

## 매니저 클래스

### RoutineManager.java
루틴 관련 모든 비즈니스 로직의 중앙 허브입니다.

```java
public class RoutineManager {
    private final Map<String, Routine> allRoutines;  // 모든 루틴 저장소
    private int routineCounter;                       // ID 생성 카운터
    private final FileManager fileManager;            // 파일 관리자
    private Player player;                            // 플레이어 참조
    private MainDashboard dashboard;                  // 대시보드 참조
    private Boss boss;                                // 보스 참조
    private final ItemDropManager itemDropManager;    // 아이템 드랍 관리자
}
```

**주요 메서드:**
| 메서드 | 설명 |
|--------|------|
| `addRoutine(name, tag, repeatDays)` | 새 루틴 생성 |
| `updateRoutine(id, name, tag, days)` | 루틴 수정 |
| `deleteRoutine(id)` | 루틴 삭제 |
| `getRoutine(id)` | ID로 루틴 조회 |
| `getRoutinesByDay(day)` | 특정 요일 루틴 목록 |
| `getRoutinesForDay(day)` | 정렬된 요일별 루틴 목록 |
| `completeRoutine(id, day)` | 루틴 완료 처리 (보상 지급) |
| `saveAllData()` | 모든 데이터 저장 |
| `loadAllData()` | 모든 데이터 로드 |

**completeRoutine 보상 시스템:**
- 기본 보상: 20 EXP, 50 Gold
- 취약 루틴 보너스: x2 배율
- 보스 데미지: 4 + 장비 보너스
- 아이템 드랍: 2% 확률 (취약 루틴 20%)

---

### ItemDropManager.java
아이템 드랍 확률과 보상을 관리합니다.

```java
public class ItemDropManager {
    private final Random random;
    private static final Item[] POSSIBLE_DROPS;  // 드랍 가능 아이템 목록
}
```

**드랍 가능 아이템:**
| 아이템 | 슬롯 | 효과 | 가격 |
|--------|------|------|------|
| 루틴 달성 보물 상자 | 기타 | 경험치 +5 | 100G |
| 행운의 동전 | 기타 | 없음 | 5G |
| 빛나는 은화 | 기타 | 없음 | 100G |
| 집중의 향로 | 기타 | 경험치 +2 | 70G |
| 초심자의 목검 | 무기 | 공격력 +5 | 50G |

---

### FileManager.java
파일 입출력을 담당합니다.

**저장 방식:**
| 데이터 | 파일 | 형식 |
|--------|------|------|
| 루틴 | routines_data.txt | 텍스트 (파이프 구분) |
| 플레이어 | player_data.dat | 직렬화 |
| 보스 | boss_data.txt | 직렬화 |

**주요 메서드:**
| 메서드 | 설명 |
|--------|------|
| `saveRoutinesToFile(routines, fileName)` | 루틴 목록 텍스트 저장 |
| `loadRoutinesFromFile(fileName)` | 루틴 목록 텍스트 로드 |
| `saveObject(object, fileName)` | 객체 직렬화 저장 |
| `loadObject(fileName)` | 객체 역직렬화 로드 |
| `savePlayerState(player, fileName)` | 플레이어 저장 |
| `loadPlayerState(fileName)` | 플레이어 로드 |
| `saveBossState(boss, fileName)` | 보스 저장 |
| `loadBossState(fileName)` | 보스 로드 |

---

## UI 클래스

### MainDashboard.java
메인 윈도우 및 프로그램 진입점입니다.

**구성 요소:**
- 대시보드 탭: 플레이어 상태, 보스 상태, 오늘의 루틴, 시스템 로그
- 스트릭 탭: 연속 달성 달력
- 통계 탭: 월간 통계 및 그래프

**주요 메서드:**
| 메서드 | 설명 |
|--------|------|
| `updatePlayerStatusUI()` | 플레이어 상태 UI 갱신 |
| `updateBossUI()` | 보스 상태 UI 갱신 |
| `updateTodayRoutinesUI()` | 오늘의 루틴 목록 갱신 |
| `addLogMessage(message)` | 시스템 로그 추가 |
| `showStoryDialog(title, content)` | 스토리 팝업 표시 |

### RoutineListView.java
요일별 루틴 목록을 표시합니다.

**기능:**
- 7개 탭 (월~일)
- 체크박스로 완료 처리
- 우클릭 컨텍스트 메뉴 (수정/삭제)
- 완료된 루틴 자동 정렬 (아래로)
- 과거/미래 요일 체크 제한

### RoutineManagerGUI.java
루틴 생성 폼을 제공합니다.

### RoutineModify.java
루틴 수정 다이얼로그를 제공합니다.

### InventoryView.java
인벤토리 및 장비 관리 UI입니다.

### ShopView.java
아이템 상점 UI입니다.

### StreakWindow.java
연속 달성 달력을 표시합니다.

### StatisticsPanel.java
월간 통계를 표시합니다.

### SimpleBarChartPanel.java
태그별 막대그래프를 렌더링합니다.

---

## 유틸리티 클래스

### DateUtil.java
시스템 시간 조회를 담당합니다. JVM TimeZone 캐시를 우회하여 실시간 시스템 시간을 반영합니다.

```java
public class DateUtil {
    public static LocalDate getToday()           // 오늘 날짜
    public static DayOfWeek getTodayDayOfWeek()  // 오늘 요일
    public static String getTodayKoreanDay()     // 한국어 요일 ("월", "화"...)
    public static boolean isToday(LocalDate date) // 오늘인지 확인
}
```

**TimeZone 캐시 우회 원리:**
```java
TimeZone.setDefault(null);  // 캐시 초기화
ZoneId zoneId = ZoneId.systemDefault();  // 새로 조회
Clock clock = Clock.system(zoneId);
return LocalDate.now(clock);
```

### RoutineRenderer.java
루틴 목록 테이블의 셀을 커스텀 렌더링합니다.

**스타일:**
- 완료된 루틴: 회색 배경
- 과거/미래 요일: 회색 텍스트

### PopupListener.java
테이블 우클릭 컨텍스트 메뉴를 처리합니다.

### PlayerStatusUpdaer.java
(미사용) 빈 클래스입니다.
