package J1103;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Inventory implements Serializable {
    
    private static final long serialVersionUID = 2L;

    // 1. 아이템을 모아두는 곳 (인벤토리)
    private final List<Item> items = new ArrayList<>();
    
    // 2. 3. 아이템을 부위별로 장착하는 곳 (장비 슬롯)
    // 부위별로 하나의 Item 객체만 저장 가능 (장착)
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

    //아이템 장착 (장착 후 기존 장비는 인벤토리에 남습니다)
    public boolean equipItem(Item item) { //장착할 아이템
        if (!items.contains(item)) {
            System.out.println("장착 실패: 인벤토리에 없는 아이템입니다.");
            return false;
        }

        Item.EquipSlot slot = item.getSlot();
        Item currentlyEquipped = equippedItems.get(slot);

        // 현재 장착된 아이템이 있다면, 인벤토리에 그대로 남겨두고 새로운 아이템으로 교체합니다.
        // items.remove(item); // 인벤토리에서 제거할지 여부는 게임 설계에 따라 다름. 여기서는 인벤토리에 남아있도록 함.

        equippedItems.put(slot, item);
        
        String log = String.format("'%s' 슬롯에 '%s' 장착 완료.", 
                                   slot.getKoreanName(), item.getName());
        if (currentlyEquipped != null) {
             log += String.format(" (기존 장비 '%s' 해제)", currentlyEquipped.getName());
        }
        System.out.println(log);
        return true; //장착성공여부
    }
    
    //아이템 장착 해제
    public Item unequipItem(Item.EquipSlot slot) { //slot 해제할 장비 부위
        Item item = equippedItems.get(slot);
        
        if (item != null) {
            equippedItems.put(slot, null); // 슬롯 비우기
            System.out.println(String.format("'%s' 슬롯의 '%s' 장착 해제.", slot.getKoreanName(), item.getName()));
        } else {
            System.out.println(String.format("'%s' 슬롯에 장착된 장비가 없습니다.", slot.getKoreanName()));
        }
        return item; //해제된 아이템(없다면 null)
    }

    // Getters for UI/Debug
    public List<Item> getItems() { 
        return Collections.unmodifiableList(items); 
    }
    
    public Map<Item.EquipSlot, Item> getEquippedItems() { 
        return Collections.unmodifiableMap(equippedItems); 
    }
}