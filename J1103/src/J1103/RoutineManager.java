package J1103;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.io.Serializable;
import java.time.LocalDate;



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
    
    private Boss boss; // 25.11.24 - 김민기
    
    public Player getPlayer() { return player; }// Player 객체를 꺼내쓰기 위해 12.01
    //25.11.24 아이템 드랍매니저
    private final ItemDropManager itemDropManager = new ItemDropManager();
    
    // =========================================================================
    // 파일 입출력 상수
    // =========================================================================
    private static final String ROUTINE_FILE = "routines_data.txt";
    private static final String PLAYER_FILE = "player_data.dat"; // 플레이어 데이터 파일명
    
    private static final String BOSS_FILE = "boss_data.txt"; // 25.11.19 - 김민기  ------- 통일 하고 싶으면 해도됨
    
    public RoutineManager() { 
    	// ⭐ 생성자에서 로드 로직 호출
        loadAllData();
    }
    
    // Getter 메서드 25.11.24 - 김민기
    public Boss getBoss() { return boss; }

    // ⭐ Setter 메서드 (MainDashboard에서 초기화 시 호출)
    public void setPlayer(Player player) {
        this.player = player;
        // 플레이어가 설정된 직후에 월 변경 체크
        // 이제 player가 null이 아니므로 정상적으로 취약 루틴을 지울 수 있음
        if (this.boss != null) {
            checkMonthChange();
       }
        
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
        	int expReward = 20;  
        	int goldReward = 50;
            int damage = 4 + player.getTotalBonusDamage();
            double dropRate = 0.02; // 기본 드랍률 2% ----------------------------------------------------------------------------------------------------  수정가능
            
            
            // 2. 취약 루틴 체크 (보너스 적용)
            boolean isWeakness = false;
            if (player.getWeaknessRoutine() != null && player.getWeaknessRoutine().equals(routine.getName())) {
                isWeakness = true;
                
                expReward *= 2;
                goldReward *= 2;
                damage *= 2;
                dropRate = 0.2; // 취약 루틴은 드랍률 20%로 상향!
            }
            
            
            if (player != null && dashboard != null) { 
                
                // ⭐ [핵심 수정] 레벨업 로직이 포함된 player.gainExp() 호출
                player.gainExp(expReward);  
                player.setGold(player.getGold() + goldReward); // 골드 획득
                player.getStreakDates().add(LocalDate.now());  // 스트릭 날짜 업데이트 25.11.19 연속일자용
                
                // UI 갱신 및 로그 출력
                dashboard.updatePlayerStatusUI();
                dashboard.addLogMessage(
                    "'" + routine.getName() + "' 루틴 완료! (+" + expReward + " EXP, +" + goldReward + " G)" ); 
                
                Item droppedItem = itemDropManager.dropItem(dropRate); // 설정된 확률로 시도
                if (droppedItem != null) {
                    player.getInventory().add(droppedItem); // 인벤토리에 추가
                    dashboard.addLogMessage("🎁 **[아이템 획득!]** " + droppedItem.getName() + "을(를) 주웠습니다!");
                    // 효과음 재생 등을 여기에 추가 가능
                }
                
                // B. 보스 자동 공격 로직 25.11.24 - 김민기
                    if (boss != null && !boss.isDefeated()) {
                        // 데미지: 기본 4 + 아이템 보너스
                        boolean isDead = boss.takeDamage(damage);
                        if (isDead) {
                            dashboard.showStoryDialog("🎉 토벌 성공!", boss.getHappyStory());
                            dashboard.addLogMessage("🏆 보스 [" + boss.getName() + "] 처치 완료!");
                            player.gainGold(500); // 추가 보상
                            dashboard.updateBossUI(); // 처치 완료 시에만 즉시 UI 갱신
                        } else {
                            dashboard.addLogMessage("⚔️ 보스에게 " + damage + "의 피해를 입혔습니다.");
                            dashboard.showBossHitEffect(); // 보스 피격 애니메이션 표시 (내부에서 타이머 후 updateBossUI 호출)
                        }
                    }
                
            } else if (dashboard != null) {
                dashboard.addLogMessage("시스템 오류: 플레이어 또는 대시보드 연결이 끊어졌습니다. 보상 지급 실패.");
            }
            
            //--251119: 완료 상태 변경 시 파일에 자동 저장 (직렬화 방식 사용 확인!)
            fileManager.saveRoutinesToFile(getAllRoutines(), ROUTINE_FILE); 
            fileManager.savePlayerState(player, "player_data.dat");
            if (boss != null) fileManager.saveBossState(boss, BOSS_FILE);
            
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
        
        // 25.11.19 - 김민기 : 보스로직 파일 : filemanager에 loadBossState가 없다면 아래처럼 처리.
        try {
            this.boss = (Boss) fileManager.loadObject(BOSS_FILE); // loadObject가 있다고 가정
       } catch (Exception e) {
            this.boss = null;
       }

       if (this.boss == null) {
           this.boss = new Boss();
       }
       
   }
    
    // 2025.11.24 - 김민기 : 월 변경 체크
    private void checkMonthChange() {
        if (boss.getMonth() != LocalDate.now().getMonthValue()) {
            // 지난달 보스 처치 실패 시 배드 엔딩
            if (!boss.isDefeated() && dashboard != null) {
                 dashboard.showStoryDialog("😢 토벌 실패", boss.getBadStory());
            }
            
            if (player != null) {
                player.setWeaknessRoutine(null); 
            }
            
            boss.spawnBossForThisMonth(); // 새 보스 소환
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
        // 25.11.24 - 김민기 : 보스 저장
        if (boss != null) {
            fileManager.saveObject(boss, BOSS_FILE); // saveObject 필요
        }
    }
}