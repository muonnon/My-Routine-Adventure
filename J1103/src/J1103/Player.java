package J1103;

import java.io.Serializable; 
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Map; // Map 타입을 사용하기 위해 추가

public class Player implements Serializable { 
    private static final long serialVersionUID = 1L; 
    
    private String name;
    private int level;
    private int currentExp;
    private int maxExp; // 현재 레벨에서 필요한 최대 경험치
    private int gold;
    
    // ⭐ 인벤토리 필드를 Inventory 객체로 변경 (인벤토리 로직을 전담)
    private Inventory inventory; 
    
    private Set<LocalDate> streakDates; // 루틴 연속 달성 일자 기록용 Set
    
    // =========================================================================
    // 생성자 (Constructors)
    // =========================================================================
    
    /**
     * 기본 생성자: 새로운 플레이어 생성 시 사용
     */
    public Player() {
        // 기본 값 설정 (새 게임 시작 시)
        this.name = "모험가";
        this.level = 1;
        this.currentExp = 0;
        this.maxExp = calculateMaxExp(1);
        this.gold = 0;
        
        this.inventory = new Inventory(); 
        this.streakDates = new HashSet<>();
    }
    
    /**
     * ⭐ [수정(11/21) - 오류 해결] 플레이어 이름을 받아 새 플레이어 객체를 생성합니다.
     * Player(String) 생성자 없음 오류를 해결하기 위해 추가되었습니다.
     * @param name 플레이어 이름
     */
    public Player(String name) { 
        this(); // 기본 생성자를 호출하여 나머지 필드를 기본값으로 초기화
        this.name = name; // 이름만 덮어쓰기
    }

    /**
     * 파일 로드 및 설정용 생성자 (FileManager에서 사용)
     */
    public Player(String name, int level, int currentExp, int gold) {
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.maxExp = calculateMaxExp(level);
        this.gold = gold;
        
        // 주의: 파일 로드 시 Inventory 객체와 streakDates Set도 로드하는 로직이 FileManager에 필요합니다.
        this.inventory = new Inventory(); 
        this.streakDates = new HashSet<>();
    }
    
    // =========================================================================
    // 핵심 비즈니스 로직 (Core Business Logic)
    // =========================================================================
    
    /**
     * 경험치와 골드를 획득하고 레벨업을 확인합니다.
     * 획득 경험치에 아이템 보너스가 적용됩니다.
     * @param exp 기본 획득 경험치
     * @param gold 획득 골드
     */
    public void gainExpAndGold(int exp, int gold) { 
        // 아이템의 보너스 경험치 합산 (Inventory에 위임)
        int totalExpGain = exp + getTotalBonusExp(); 
        this.gold += gold;
        this.currentExp += totalExpGain;
        
        System.out.println(String.format("EXP +%d, GOLD +%d 획득!", totalExpGain, gold));
        
        // 레벨업 확인
        while (this.currentExp >= this.maxExp) {
            this.currentExp -= this.maxExp;
            this.level++;
            this.maxExp = calculateMaxExp(this.level);
            System.out.println(String.format("🌟 레벨업! 현재 레벨: %d", this.level));
        }
    }
    
    /**
     * 스트릭(연속 달성) 날짜를 오늘 날짜로 갱신합니다.
     * @param date 오늘 날짜
     */
    public void updateStreak(LocalDate date) { 
        streakDates.add(date);
    }
    
    /**
     * 아이템을 구매하고 골드를 차감합니다. (ShopView에서 사용)
     * @return 구매 성공 여부
     */
    public boolean buyItem(Item item) {
    	if (this.gold >= item.getPrice()) {
    		this.gold -= item.getPrice();
    		inventory.addItem(item); // inventory 객체를 통해 아이템 추가
    		return true;
    	} else {
    		System.out.println("❌ 골드가 부족하여 구매에 실패했습니다.");
    		return false;
    	}
    }
    
    /**
     * 아이템 장착 (Inventory 객체에 장착 로직 위임)
     */
    public boolean equipItem(Item item) {
        return inventory.equipItem(item);
    }

    /**
     * 아이템 장착 해제 (Inventory 객체에 해제 로직 위임)
     */
    public Item unequipItem(Item.EquipSlot slot) {
        return inventory.unequipItem(slot);
    }

    /**
     * ⭐ [수정(11/24) - 현재 오류 해결] 장착 중인 아이템 Map을 Inventory에서 가져옵니다. (Inventory 위임)
     */
    public Map<Item.EquipSlot, Item> getEquippedItems() {
        return inventory.getEquippedItems();
    }
    
    /**
     * ⭐ [수정(11/21) - 오류 해결] 장착 아이템으로부터 총 보너스 공격력을 가져옵니다. (Inventory 위임)
     */
    public int getTotalBonusDamage() { 
        return inventory.getTotalBonusDamage();
    }

    /**
     * ⭐ [수정(11/21) - 오류 해결] 장착 아이템으로부터 총 보너스 경험치를 가져옵니다. (Inventory 위임)
     */
    public int getTotalBonusExp() { 
        return inventory.getTotalBonusExp();
    }
    
    /**
     * 다음 레벨까지 필요한 최대 경험치 계산 (단순화된 공식)
     */
    private int calculateMaxExp(int level) {
        return 100 + (level * 20); 
    }

    
    // =========================================================================
    // Getters / Setters
    // =========================================================================
    
    // Getters 
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getCurrentExp() { return currentExp; }
    public int getMaxExp() { return maxExp; }
    public int getGold() { return gold; }
    
    public Inventory getInventory() { return inventory; } // Inventory 객체 자체를 가져옴
    public Set<LocalDate> getStreakDates() { return streakDates; } 
    
    // Setters 
    public void setName(String name) { this.name = name; }
    public void setLevel(int level) { 
        this.level = level; 
        this.maxExp = calculateMaxExp(level); 
    }
    public void setCurrentExp(int currentExp) { 
        this.currentExp = currentExp; 
    }
    public void setMaxExp(int maxExp) { 
        this.maxExp = maxExp; 
    }
    public void setGold(int gold) {
        this.gold = gold;
    }
    
    public void setStreakDates(Set<LocalDate> streakDates) {
        this.streakDates = streakDates != null ? streakDates : new HashSet<>();
    }
    
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}