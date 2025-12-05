package J1103;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Inventory implements Serializable {
    
    private static final long serialVersionUID = 2L;

    // 1. 아이템을 모아두는 곳 (인벤토리 - 가방)
    private final List<Item> items = new ArrayList<>();
    
    // 2. 아이템을 부위별로 장착하는 곳 (장비 슬롯)
    private final Map<Item.EquipSlot, Item> equippedItems = new HashMap<>();

    public Inventory() {
        // 인벤토리 초기화 시점에 장비 슬롯을 미리 설정 (모든 부위 null)
        for (Item.EquipSlot slot : Item.EquipSlot.values()) {
            equippedItems.put(slot, null);
        }
    }

    /**
     * 아이템 획득 (인벤토리에 추가)
     */
    public void addItem(Item item) {
        items.add(item);
        System.out.println("아이템 획득: " + item.getName());
    }

    /**
     * 아이템 장착
     */
    public boolean equipItem(Item item) { 
        if (item.getSlot() == null || item.getSlot() == Item.EquipSlot.NONE) {
            System.out.println("장착 실패: 장착 가능한 아이템이 아닙니다.");
            return false;
        }
        
        Item.EquipSlot slot = item.getSlot();
        Item currentlyEquipped = equippedItems.get(slot);

        // 현재 장착된 아이템이 있다면, 인벤토리에 그대로 남겨두고 새로운 아이템으로 교체합니다.
        equippedItems.put(slot, item);
        
        String log = String.format("'%s' 슬롯에 '%s' 장착 완료.", 
                                   slot.getKoreanName(), item.getName());
        if (currentlyEquipped != null) {
             log += String.format(" (기존 장비 '%s' 해제)", currentlyEquipped.getName());
        }
        System.out.println(log);
        return true; 
    }
    
    /**
     * 아이템 장착 해제
     * @param slot 해제할 장비 부위
     * @return 해제된 Item 객체 (없으면 null)
     */
    public Item unequipItem(Item.EquipSlot slot) { 
        Item item = equippedItems.get(slot);
        
        if (item != null) {
            equippedItems.put(slot, null); // 슬롯 비우기
            System.out.println(String.format("'%s' 슬롯의 '%s' 장착 해제.", slot.getKoreanName(), item.getName()));
            return item;
        } else {
            System.out.println(String.format("해제할 '%s' 슬롯에 장착된 아이템이 없습니다.", slot.getKoreanName()));
            return null;
        }
    }
    
    // =========================================================================
    // 보너스 능력치 계산 메서드 (InventoryView에서 Player를 통해 호출됨)
    // =========================================================================

    /**
     * ⭐ [수정(11/21) - 오류 해결] 장착된 아이템으로부터 총 보스 공격력 보너스를 계산합니다.
     */
    public int getTotalBonusDamage() { 
        int totalDamage = 0;
        // 장착된 아이템 맵을 순회
        for (Item item : equippedItems.values()) {
            // 아이템이 장착되어 있고, 효과 타입이 BOSS_ATTACK인 경우
            if (item != null && item.getEffectType() == Item.EffectType.BOSS_ATTACK) {
                totalDamage += item.getEffectValue();
            }
        }
        return totalDamage;
    }

    /**
     * ⭐ [수정(11/21) - 오류 해결] 장착된 아이템으로부터 총 경험치 보너스를 계산합니다.
     */
    public int getTotalBonusExp() { 
        int totalExp = 0;
        // 장착된 아이템 맵을 순회
        for (Item item : equippedItems.values()) {
            // 아이템이 장착되어 있고, 효과 타입이 EXP_BONUS인 경우
            if (item != null && item.getEffectType() == Item.EffectType.EXP_BONUS) {
                totalExp += item.getEffectValue();
            }
        }
        return totalExp;
    }


    // =========================================================================
    // Getters
    // =========================================================================
    
    /**
     * 인벤토리(가방)에 있는 모든 아이템 목록을 반환합니다. (Read-only)
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items); 
    }

    /**
     * 현재 장착된 모든 아이템을 <슬롯, 아이템> 형태로 반환합니다. (Read-only)
     */
    public Map<Item.EquipSlot, Item> getEquippedItems() {
        return Collections.unmodifiableMap(equippedItems);
    }
}
