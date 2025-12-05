# My-Routine-Adventure (MRA) 프로젝트 문서

## 📌 프로젝트 개요

**My-Routine-Adventure (MRA)**는 루틴 관리와 RPG 게이미피케이션 요소를 결합한 Java Swing 기반 데스크톱 애플리케이션입니다.

사용자가 일상 루틴을 등록하고 완료하면 경험치, 골드, 아이템을 획득하며, 월별 보스를 처치하는 재미있는 목표를 제공합니다.

### 주요 특징
- 🎮 **게이미피케이션**: 루틴 완료 시 경험치/골드 획득, 레벨업 시스템
- 👹 **월별 보스 시스템**: 매달 다른 보스 등장, 루틴 완료로 보스 공격
- 🎒 **인벤토리 & 장비 시스템**: 아이템 획득, 장착하여 능력치 강화
- 🏪 **상점 시스템**: 골드로 장비 구매
- 📊 **통계 & 연속 달성**: 월간 통계, 달력 기반 스트릭 표시
- 💾 **데이터 영속성**: 텍스트 파일 기반 데이터 저장/로드

---

## 📁 프로젝트 구조

```
J1103/
├── src/
│   └── J1103/
│       ├── MainDashboard.java      # 메인 진입점 및 대시보드 UI
│       ├── Player.java              # 플레이어 데이터 모델
│       ├── Routine.java             # 루틴 데이터 모델
│       ├── RoutineManager.java      # 루틴 비즈니스 로직 관리
│       ├── FileManager.java         # 파일 입출력 담당
│       ├── Boss.java                # 보스 데이터 모델
│       ├── Item.java                # 아이템 데이터 모델
│       ├── Inventory.java           # 인벤토리 관리
│       ├── ItemDropManager.java     # 아이템 드랍 로직
│       ├── RoutineManagerGUI.java   # 루틴 생성 GUI
│       ├── RoutineListView.java     # 루틴 목록/수정/삭제 GUI
│       ├── RoutineModify.java       # 루틴 수정 다이얼로그
│       ├── RoutineRenderer.java     # 루틴 테이블 셀 렌더러
│       ├── PopupListener.java       # 우클릭 팝업 메뉴 리스너
│       ├── InventoryView.java       # 인벤토리 GUI
│       ├── ShopView.java            # 상점 GUI
│       ├── StreakWindow.java        # 연속 달성 달력 표시
│       ├── StatisticsPanel.java     # 월간 통계 패널
│       ├── SimpleBarChartPanel.java # 막대 그래프 패널
│       └── PlayerStatusUpdaer.java  # (미구현 - 빈 클래스)
├── docs/                            # 문서 폴더
├── bin/                             # 컴파일된 클래스 파일
├── player_data.txt                  # 플레이어 데이터 저장 파일
├── routines_data.txt                # 루틴 데이터 저장 파일
└── boss_data.txt                    # 보스 데이터 저장 파일
```

---

## 📚 상세 문서 목록

| 문서명 | 설명 |
|--------|------|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | 전체 아키텍처 및 클래스 다이어그램 |
| [CLASSES.md](./CLASSES.md) | 각 클래스별 상세 설명 |
| [DATA_MODEL.md](./DATA_MODEL.md) | 데이터 모델 및 파일 포맷 |
| [UI_GUIDE.md](./UI_GUIDE.md) | UI 구조 및 화면 설명 |
| [FEATURES.md](./FEATURES.md) | 기능 상세 및 요구사항 매핑 |

---

## 🚀 실행 방법

### 사전 요구사항
- **Java JDK 8 이상** (권장: JDK 11+)
- IDE: Eclipse, IntelliJ IDEA 또는 VS Code with Java Extension

### 실행 순서
1. `J1103/src` 폴더를 프로젝트 소스로 설정
2. `J1103.MainDashboard` 클래스의 `main()` 메서드 실행
3. 프로그램이 시작되면 대시보드 화면이 표시됩니다.

```bash
# 커맨드라인 실행 (J1103 폴더에서)
javac -d bin src/J1103/*.java
java -cp bin J1103.MainDashboard
```

---

## 👨‍💻 개발 정보

- **프로젝트명**: My-Routine-Adventure
- **주요 개발 기간**: 2025년 11월 ~ 12월
- **기술 스택**: Java SE, Swing GUI
- **데이터 저장**: 텍스트 파일 기반 (CSV 유사 형식)

---

## 📝 버전 히스토리

| 날짜 | 주요 변경 사항 |
|------|---------------|
| 2025-11-12 | FileManager 클래스 분리, 플레이어 데이터 저장 기능 추가 |
| 2025-11-17 | 인벤토리/상점 시스템 추가 |
| 2025-11-19 | 아이템 효과 시스템, 연속 달성 탭 분리 |
| 2025-11-21 | 아이템 드랍 시스템, 버그 수정 |
| 2025-11-30 | 보스 이미지 경로 추가 |
| 2025-12-01 | 월간 통계 패널 추가 |
