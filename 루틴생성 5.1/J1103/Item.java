package J1103;

import java.io.Serializable;

public class Item implements Serializable {

	private static final long serialVersionUID = 2L; // 직렬화를 위해 Serializable 구현 및 UID 설정

	// 장비 부위를 정의하는 Enum
	public enum EquipSlot {
		HEAD("머리"), BODY("몸통"), LEG("다리");

		private final String koreanName;

		EquipSlot(String koreanName) {
			this.koreanName = koreanName;
		}

		public String getKoreanName() {
			return koreanName;
		}
	}

	private final String name;
	private final EquipSlot slot; // 장착 부위

	// 생성자
	public Item(String name, EquipSlot slot) {
		this.name = name;
		this.slot = slot;
	}

	// Getters
	public String getName() {
		return name;
	}

	public EquipSlot getSlot() {
		return slot;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, slot.getKoreanName());
	}
}