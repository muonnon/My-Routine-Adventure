package J1103; 

import java.util.Random;

/**
 * 아이템 드랍 로직을 관리하는 클래스 (REQ-022 구현)
 */
public class ItemDropManager {

    private final Random random = new Random(); // 수정(11/21) 랜덤 객체 생성

    /**
     * 루틴 완료 시 드랍될 수 있는 아이템을 정의합니다.
     * Item.java 클래스에 정의된 생성자를 사용합니다.
     */
    private static final Item[] POSSIBLE_DROPS = new Item[]{
        // 이름, 가격, 슬롯, 효과 종류, 효과 수치
    	// 1.보물상자 (기타 아이템, 경험치 보너스)
        new Item("루틴 달성 보물 상자", 100, Item.EquipSlot.ETC, Item.EffectType.EXP_BONUS, 5),
        // 2. 동전 (상점 판매용)
        new Item("행운의 동전", 5, Item.EquipSlot.ETC, Item.EffectType.NONE, 0), 
        // 3. 은화 (저가치 재화) (판매용)
        new Item("빛나는 은화", 100, Item.EquipSlot.ETC, Item.EffectType.NONE, 0),
        // 4. 향로 (나중에 소모품으로 발전가능)
        new Item("집중의 향로", 70, Item.EquipSlot.ETC, Item.EffectType.EXP_BONUS, 2),
        // 5. 목검 - 장비용
        new Item("초심자의 목검", 50, Item.EquipSlot.WEAPON, Item.EffectType.BOSS_ATTACK, 5)
    }; // 수정(11/21) 드랍 가능한 아이템 목록 정의

    /**
     * 주어진 확률에 따라 아이템 드랍을 시도하고, 성공 시 Item 객체를 반환합니다.
     * @param dropRate 아이템 드랍 확률 (0.0 ~ 1.0)
     * @return 드랍에 성공하면 Item 객체, 실패하면 null을 반환
     */
    public Item dropItem(double dropRate) {
        // 드랍 확률 유효성 검사
        if (dropRate <= 0.0) { return null; }
        // 0.0 이상 1.0 미만의 난수 생성 
        if (random.nextDouble() < dropRate) { // 수정(11/21) 난수와 확률 비교하여 드랍 결정
            return getDroppedItem(); // 드랍 성공 시 아이템 반환
        } else {
            return null; // 드랍 실패
        }
    }

    /**
     * 드랍에 성공했을 때 지급할 아이템을 무작위로 반환합니다.
     * @return 드랍된 Item 객체
     */
    private Item getDroppedItem() { // 수정(11/21) 드랍될 아이템 선택 및 반환
        // 목록에서 무작위로 선택
        int index = random.nextInt(POSSIBLE_DROPS.length);
        return POSSIBLE_DROPS[index];
    }
}