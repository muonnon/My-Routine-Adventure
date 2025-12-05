package J1103;

import java.io.Serializable;


public class Item implements Serializable {

	private static final long serialVersionUID = 2L; // 직렬화를 위해 Serializable 구현 및 UID 설정

	// 장비 부위를 정의하는 Enum
	public enum EquipSlot {
		HEAD("머리"), BODY("몸통"), LEG("다리"), WEAPON("무기"), // 25.11.19 - 김민기 : 무기 추가
		NONE("없음"); // 수정(11/21) 장착 불가 아이템을 위한 NONE 슬롯 추가

		private final String koreanName;
		EquipSlot(String koreanName) { this.koreanName = koreanName; }
		public String getKoreanName() { return koreanName; }
	}

	  //--- 필드 ---
    private final String name;      // 이름
    private final int price;        // 가격 (상점용)
    private final EquipSlot slot;   // 장착 부위
    private final EffectType effectType; // 효과 종류
    private final int effectValue;       // 효과 수치 (예: 10)
    
	// 2. ⭐ 아이템 효과 종류 (새로 추가)  25.11.19 - 김민기 : 아이템 효과 추가
    // 보스 공격력 또는 경험치 중 하나만 선택하도록 함
    public enum EffectType {
        NONE("효과 없음"),
        BOSS_ATTACK("공격력"),
        EXP_BONUS("경험치 획득량");

        private final String desc;
        EffectType(String desc) { this.desc = desc; }
        public String getDesc() { return desc; }
    }

    
	// 생성자
	public Item(String name, int price, EquipSlot slot, EffectType effectType, int effectValue) {
		this.name = name;
		this.slot = slot;
		this.price = price;
        this.effectType = effectType;
        this.effectValue = effectValue;
	}

	// Getters
	public String getName() { return name; }
    public int getPrice() { return price; }
    public EquipSlot getSlot() { return slot; }
    public EffectType getEffectType() { return effectType; }
    public int getEffectValue() { return effectValue; }

 // --- 정보 표시용 --- // 25.11.19 -김민기 추가
    @Override
    public String toString() {
        // 예: "철 투구 (머리) - 보스 공격력 +5 [가격: 100G]"
        return String.format("%s (%s) - %s +%d [가격: %d G]", 
                name, slot.getKoreanName(), effectType.getDesc(), effectValue, price);
    }
    
    // 툴팁용 설명 반환 (나중에 UI에서 쓸 수 있음)
    public String getDescription() {
        return String.format("[%s] 착용 시 %s %d 증가", slot.getKoreanName(), effectType.getDesc(), effectValue);
    }
}