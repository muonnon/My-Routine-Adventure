package J1103;

import java.io.Serializable; // ⭐ Serializable 인터페이스 추가 (파일 저장을 위해)

public class Player implements Serializable { // ⭐ Serializable 인터페이스 구현
    
    private static final long serialVersionUID = 1L; // 직렬화 버전 UID
    
    private String name;
    private int level;
    private int currentExp;
    private int maxExp; // 현재 레벨에서 필요한 최대 경험치
    private int gold;
    
    //인벤토리 필드 추가 -- 251117
    private final Inventory inven;

    // 생성자: 이름과 초기값을 설정합니다.
    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.currentExp = 0;
        // ⭐ Lv.1의 maxExp는 100으로 설정 (calculateMaxExp 메서드 사용)
        this.maxExp = calculateMaxExp(this.level); 
        this.gold = 0; 
        this.inven = new Inventory(); //인벤토리 초기화 -- 251117
    }

    // =========================================================================
    // 레벨업 로직 관련 메서드
    // =========================================================================

    /**
     * 특정 레벨에서 다음 레벨로 가기 위해 필요한 총 경험치(maxExp)를 계산합니다.
     * Lv.1 -> 100, Lv.2 -> 150, Lv.3 -> 200, ... (레벨당 50 증가)
     */
    private int calculateMaxExp(int level) {
        if (level < 1) return 100;
        // 공식: 100 + 50 * (level - 1)
        return 100 + 50 * (level - 1);
    }
    
    /**
     * 경험치를 획득하고 레벨업 여부를 확인하여 처리합니다.
     * 이 메서드가 RoutineManager에서 호출되어야 합니다.
     */
    public void gainExp(int exp) {
        // 1. 경험치 추가
        this.currentExp += exp;
        
        // 2. 레벨업 반복 확인 및 처리
        while (this.currentExp >= this.maxExp) {
            
            // 2-1. 초과 경험치 계산 (다음 레벨로 이월될 양)
            int remainingExp = this.currentExp - this.maxExp;
            
            // 2-2. 레벨업
            this.level++;
            
            // 2-3. maxExp 및 currentExp 업데이트
            this.maxExp = calculateMaxExp(this.level); // 다음 레벨의 필요 경험치 설정
            this.currentExp = remainingExp; // 초과 경험치 이월
            
            // TODO: MainDashboard에 레벨업 알림 로그를 추가하거나, 
            // RoutineManager를 통해 로그를 출력할 수 있도록 설계해야 합니다.
            System.out.println("🎉 레벨업! 현재 레벨: Lv." + this.level + 
                               " (다음 레벨 필요 EXP: " + this.maxExp + ")");
        }
    }


    // =========================================================================
    // Getters / Setters
    // =========================================================================
    
    // Getters (UI에 값을 표시하기 위해 사용)
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getCurrentExp() { return currentExp; }
    public int getMaxExp() { return maxExp; }
    public int getGold() { return gold; }

    // 인벤토리 Getter 추가
    public Inventory getInventory() {
    	return inven;
    }
    
    // Setters (주로 파일 로드 시 사용)
    public void setName(String name) { this.name = name; }
    public void setLevel(int level) { 
        this.level = level; 
        this.maxExp = calculateMaxExp(level); // 레벨 설정 시 maxExp도 재계산
    }
    public void setCurrentExp(int currentExp) { 
        // 이 Setter는 주로 파일 로드 시에만 사용하도록 남겨둡니다.
        this.currentExp = currentExp; 
    }
    public void setMaxExp(int maxExp) { 
        // 이 Setter는 외부에서 직접 maxExp를 바꾸는 것을 막기 위해 제거하거나 private으로 만드는 것이 좋습니다. 
        // 여기서는 파일 로드 호환성을 위해 유지하되, 내부 로직에 의해 변경되도록 합니다.
        this.maxExp = maxExp;
    }
    public void setGold(int gold) { 
        this.gold = gold; 
    }

    /**
     * 골드를 획득합니다.
     */
    public void gainGold(int gold) {
        this.gold += gold;
    }
}