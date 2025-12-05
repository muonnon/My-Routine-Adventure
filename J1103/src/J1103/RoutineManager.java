package J1103;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.util.stream.Collectors;

import J1103.Routine; 


public class RoutineManager {
    
    // 모든 루틴을 ID를 키로 저장하는 Map (중앙 저장소)
    private final Map<String, Routine> allRoutines = new ConcurrentHashMap<>();
    
    // 루틴 ID를 생성하기 위한 카운터
    private int routineCounter = 0;
    
    // ⭐ FileManager 객체 추가(11/12)
    private final FileManager fileManager = new FileManager();
    
    // ⭐ 새로 추가된 필드: Player와 MainDashboard 참조 (11/11)
    private Player player; 
    private MainDashboard dashboard;
    
    // 아이템 드랍 관리자 인스턴스
    private final ItemDropManager itemDropManager = new ItemDropManager(); // 수정(11/21) 아이템 드랍 관리자 인스턴스 생성

    // =========================================================================
    // 파일 입출력 상수
    // =========================================================================
    private static final String ROUTINE_FILE = "routines_data.txt";
    private static final String PLAYER_FILE = "player_data.txt"; // 플레이어 데이터 파일명
    
    
    public RoutineManager() { 
    	// ⭐ 생성자에서 로드 로직 호출
        loadAllData();
    }

    // ⭐ Setter 메서드 (MainDashboard에서 초기화 시 호출)
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    // ⭐ Setter 메서드 (MainDashboard에서 초기화 시 호출)
    public void setDashboard(MainDashboard dashboard) {
        this.dashboard = dashboard;
    }

    // =========================================================================
    // 루틴 관리 핵심 기능
    // =========================================================================
    
    /**
     * 루틴을 생성하고 추가합니다.
     */
    public void addRoutine(String name, String tag, List<String> repeatDays) {
        String id = "R" + (++routineCounter);
        Routine newRoutine = new Routine(id, name, tag, repeatDays);
        allRoutines.put(id, newRoutine);

        // 로그 메시지 출력 (dashboard가 연결되어 있을 때만)
        if (dashboard != null) {
            dashboard.addLogMessage("➕ 루틴 생성: " + name + " (ID: " + id + ")");
        }
        
        // 파일에 저장
        saveAllData();
    }

    /**
     * 루틴을 수정합니다.
     */
    public boolean updateRoutine(String id, String newName, String newTag, List<String> newDays) {
        Routine routine = allRoutines.get(id);
        if (routine != null) {
            routine.setName(newName);
            routine.setTag(newTag);
            routine.setRepeatDays(newDays); // List의 깊은 복사는 Routine 클래스에서 처리
            
            if (dashboard != null) {
                dashboard.addLogMessage("✏️ 루틴 수정: " + newName + " (ID: " + id + ")");
            }
            saveAllData();
            return true;
        }
        return false;
    }

    /**
     * 루틴을 삭제합니다.
     */
    public boolean deleteRoutine(String id) {
        Routine removed = allRoutines.remove(id);
        if (removed != null) {
            if (dashboard != null) {
                dashboard.addLogMessage("🗑️ 루틴 삭제: " + removed.getName() + " (ID: " + id + ")");
            }
            saveAllData();
            return true;
        }
        return false;
    }

    /**
     * 루틴을 완료(체크) 처리하고 아이템 드랍을 시도합니다.
     * @param routineId 완료 처리할 루틴의 ID
     * @param day 완료 처리하는 요일 ("월", "화" 등)
     * @return 완료 처리에 성공했으면 true, 이미 완료했거나 루틴이 없으면 false
     */
    public boolean completeRoutine(String routineId, String day) {
        Routine routine = allRoutines.get(routineId);
        
        // 루틴이 없거나 이미 오늘 완료된 경우
        if (routine == null || routine.isCompletedForDay(day)) {
            if (dashboard != null) {
                 dashboard.addLogMessage("⚠️ 루틴 완료 실패 (이미 완료되었거나 루틴이 없음): " + routineId);
            }
            return false;
        }

        // 1. 루틴 완료 처리
        routine.completeForDay(day);
        
        // 2. 플레이어 경험치, 골드 증가 및 스트릭 업데이트
        if (player != null) {
            player.gainExpAndGold(10, 5); // 수정(11/21) Player의 새로운 메서드 호출
            player.updateStreak(LocalDate.now()); // 수정(11/21) Player의 새로운 메서드 호출
            
            // 3. 아이템 드랍 시도
            double dropRate = 0.2; // 예시로 20% 드랍 확률 설정
            Item droppedItem = itemDropManager.dropItem(dropRate); 
            
            // 4. 드랍 성공 시 인벤토리에 추가 및 로그 표시
            if (droppedItem != null) { 
                player.getInventory().addItem(droppedItem); // 수정(11/21) Inventory 객체의 addItem 호출
                
                if (dashboard != null) {
                    dashboard.addLogMessage("🎉 **아이템 획득!** " + droppedItem.getName() + "이(가) 인벤토리에 추가되었습니다.");
                    // MainDashboard의 UI 갱신 필요 (골드/경험치/인벤토리 상태)
                    dashboard.updatePlayerStatusUI(); 
                } else {
                    System.out.println("🎉 **아이템 획득!** " + droppedItem.getName() + "이(가) 인벤토리에 추가되었습니다.");
                }
            } 
            
            // 플레이어 상태 UI 갱신 (경험치/골드)
            if (dashboard != null) {
                 dashboard.updatePlayerStatusUI();
            }
        }
        
        if (dashboard != null) {
            dashboard.addLogMessage("✅ 루틴 완료: " + routine.getName());
        }
        
        // 파일에 저장
        saveAllData();
        return true;
    }
    
    // =========================================================================
    // 데이터 조회 기능
    // =========================================================================

    /**
     * 모든 루틴 목록을 List 형태로 반환합니다.
     */
    public List<Routine> getAllRoutines() {
        return new ArrayList<>(allRoutines.values()); // ⭐ 안전한 복사본 반환 (11/11)
    }
    
    /**
     * 특정 ID의 루틴을 반환합니다.
     */
    public Routine getRoutine(String id) {
        return allRoutines.get(id); // ⭐ 직접 참조 반환, 수정 시 주의 필요 (11/11)
    }

    /**
     * 특정 요일에 반복되는 루틴 목록만 필터링하여 반환합니다.
     */
    public List<Routine> getRoutinesByDay(String day) {
        // ⭐ 자바 스트림을 사용한 깔끔한 필터링 (11/11)
        return allRoutines.values().stream()
                .filter(routine -> routine.getRepeatDays().contains(day))
                .collect(Collectors.toList());
    }
    
    // =========================================================================
    // 파일 입출력 및 데이터 초기화
    // =========================================================================

    /**
     * 모든 데이터를 파일에서 불러옵니다.
     */
    public void loadAllData() {
        // 1. 루틴 데이터 로드
        List<Routine> loadedRoutines = fileManager.loadRoutinesFromFile(ROUTINE_FILE);
        int maxId = 0;
        
        allRoutines.clear(); // 기존 데이터 초기화
        for (Routine routine : loadedRoutines) {
            allRoutines.put(routine.getId(), routine);
            // ID 문자열에서 숫자 부분만 추출하여 최대값을 갱신합니다. (예: "R10" -> 10)
            try {
                int idNum = Integer.parseInt(routine.getId().substring(1));
                if (idNum > maxId) {
                    maxId = idNum;
                }
            } catch (NumberFormatException ignored) {
                // 숫자가 아닌 경우 무시
            }
        }
        
        // 다음 루틴 ID 카운터를 최대 ID + 1로 설정합니다.
        routineCounter = maxId;

        if (dashboard != null) {
             dashboard.addLogMessage("✅ " + allRoutines.size() + "개의 루틴 로드 완료. 다음 Routine ID 카운터: " + (routineCounter + 1));
        } else {
             System.out.println("✅ " + allRoutines.size() + "개의 루틴 로드 완료. 다음 Routine ID 카운터: " + (routineCounter + 1));
        }
    }

    /**
     * 모든 데이터를 파일에 저장합니다. (MainDashboard 닫기 시점에 호출)
     */
    public void saveAllData() {
        // 1. 루틴 데이터 저장
        fileManager.saveRoutinesToFile(getAllRoutines(), ROUTINE_FILE);

        // 2. 플레이어 데이터 저장
        // player 객체가 null이 아닐 때만 저장 시도
        if (player != null) {
            fileManager.savePlayerState(player, PLAYER_FILE);
        }
    }
}