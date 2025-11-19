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
    
    public void setDashboard(MainDashboard dashboard) {
        this.dashboard = dashboard;
    }


    /**
     * 새로운 루틴을 생성하고 중앙 저장소에 추가합니다.
     */
    public String addRoutine(String name, String tag, List<String> repeatDays) {
        // 1. 고유 ID 생성
        String id = "R" + (++routineCounter);
        
        // 2. Routine 객체 생성
        Routine newRoutine = new Routine(id, name, tag, repeatDays);
        
        // 3. 중앙 저장소에 추가
        allRoutines.put(id, newRoutine);
        
        // 4. 로그 출력
        if (dashboard != null) {
            dashboard.addLogMessage("새 루틴 생성: " + name + " (ID: " + id + ")");
        }
        
        return id;
    }

    /**
     * 루틴 정보를 수정합니다.
     */
    public boolean updateRoutine(String id, String newName, String newTag, List<String> newRepeatDays) {
        Routine routineToUpdate = allRoutines.get(id);
        if (routineToUpdate != null) {
            // Routine 클래스의 Setter 메서드 사용
            routineToUpdate.setName(newName);
            routineToUpdate.setTag(newTag);
            routineToUpdate.setRepeatDays(newRepeatDays);
            
            if (dashboard != null) {
                dashboard.addLogMessage("루틴 수정: " + newName + " (ID: " + id + ")");
            }
            return true;
        }
        return false;
    }
    
    /**
     * 루틴을 삭제합니다.
     */
    public boolean deleteRoutine(String id) {
        Routine deletedRoutine = allRoutines.remove(id);
        if (deletedRoutine != null && dashboard != null) {
            dashboard.addLogMessage("루틴 삭제: " + deletedRoutine.getName() + " (ID: " + id + ")");
        }
        return deletedRoutine != null;
    }

    /**
     * 고유 ID로 Routine 객체를 반환합니다. (RoutineListView의 수정/삭제 로직에서 사용)
     * @param id 찾으려는 루틴의 고유 ID
     * @return 해당 ID의 Routine 객체, 없으면 null
     */
    public Routine getRoutine(String id) {
        return allRoutines.get(id);
    }
    
    /**
     * 모든 루틴 목록을 List<Routine> 형태로 반환합니다.
     */
    public List<Routine> getAllRoutines() {    	
    	return new ArrayList<>(allRoutines.values());
        
    }
    
    //--251119: 특정 요일에 해당하는 루틴 목록을 정렬하여 반환 (미완료 -> 완료 순)
    public List<Routine> getRoutinesForDay(String day) { 
        
        // Stream을 사용하여 필터링 및 정렬된 리스트를 반환 
        return allRoutines.values().stream() 
            .filter(routine -> routine.getRepeatDays().contains(day)) 
            .sorted((r1, r2) -> { 
                // 1. 오늘 해당 '요일'에 완료된 루틴을 미완료 루틴보다 뒤에 배치 
                // isCompletedForDay(day)를 사용하여 현재 탭의 요일에 대해서만 완료 여부 판단 
                int completedCompare = Boolean.compare(r1.isCompletedForDay(day), r2.isCompletedForDay(day));
                if (completedCompare != 0) {
                    return completedCompare; 
                } 
                // 2. 완료 상태가 같으면 이름 순으로 정렬 
                return r1.getName().compareTo(r2.getName());
            }) 
            .collect(Collectors.toList()); 
    }
    
	// 2025 - 11 - 17 : 다시 추가
	// RoutineManager.java 내부에 추가 (getAllRoutines() 메서드 아래 등)
    /**
     * 특정 요일에 반복되는 루틴 목록을 반환합니다.
     * @param dayName (예: "월", "화", "수")
     * @return 해당 요일의 루틴 목록
     */
    public List<Routine> getRoutinesByDay(String dayName) {
        // stream()을 사용한 필터링 (user's import: java.util.stream.Collectors)
        return allRoutines.values().stream()
            .filter(routine -> routine.getRepeatDays().contains(dayName))
            .collect(Collectors.toList());
    }

    /**
     * 루틴 완료를 처리하고 마지막 완료 날짜를 갱신하며 보상을 지급합니다.
     */
    //--251119: 루틴 완료를 처리하고 요일별 완료 날짜를 갱신합니다 (day 파라미터 추가)
    public boolean completeRoutine(String id, String day) { //day 인자 추가
        Routine routine = allRoutines.get(id);
        
        //--251119: isCompletedForDay(day) 사용해 오늘 해당 요일에서 완료했는지 확인
        if (routine != null && !routine.isCompletedForDay(day)) { 
        	routine.completeForDay(day); //--251119: Map에 해당 요일의 오늘 날짜 기록
            
            // 보상 값
            final int EXP_REWARD = 20;
            final int GOLD_REWARD = 50;
            
            if (player != null && dashboard != null) { 
                
                // ⭐ [핵심 수정] 레벨업 로직이 포함된 player.gainExp() 호출
                player.gainExp(EXP_REWARD); 
                
                // 골드 획득
                player.setGold(player.getGold() + GOLD_REWARD);
                
                // 스트릭 날짜 업데이트 25.11.19 연속일자용
                player.getStreakDates().add(LocalDate.now()); 
                
                // UI 갱신 및 로그 출력
                dashboard.updatePlayerStatusUI();
                dashboard.addLogMessage(
                    "'" + routine.getName() + "' 루틴 완료! (+" + EXP_REWARD + " EXP, +" + GOLD_REWARD + " G)"
                );
            } else if (dashboard != null) {
                dashboard.addLogMessage("시스템 오류: 플레이어 또는 대시보드 연결이 끊어졌습니다. 보상 지급 실패.");
            }
            
            //--251119: 완료 상태 변경 시 파일에 자동 저장
            fileManager.saveRoutinesToFile(getAllRoutines(), ROUTINE_FILE); 
            
            return true;
        }
        return false; // 이미 완료했거나 루틴이 없음
    }
    
    // =========================================================================
    // 파일 입출력 로직 (FileManager 위임)
    // =========================================================================

    /**
     * 파일에서 모든 데이터를 로드하고 매니저 상태를 초기화합니다.
     */
    private void loadAllData() {
        // 1. 루틴 데이터 로드
        List<Routine> loadedRoutines = fileManager.loadRoutinesFromFile(ROUTINE_FILE);
        
        allRoutines.clear();
        routineCounter = 0; // 카운터 초기화
        
        int maxId = 0;
        
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
            // ⭐ [오류 수정] savePlayerState에 파일명을 함께 전달
            fileManager.savePlayerState(player, PLAYER_FILE); 
        }
        
        if (dashboard != null) {
            dashboard.addLogMessage("✅ 모든 데이터(루틴, 플레이어) 저장 완료.");
        } else {
            System.out.println("✅ 모든 데이터(루틴, 플레이어) 저장 완료.");
        }
    }
}