package J1103;


import java.util.List; // ⭐ 이 줄을 추가해야 합니다.
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate; // LocalDate 사용을 위해서
import java.util.ArrayList; // ⭐ List<String> 수정용으로 추가 (11/11)


public class Routine {
    private String id; // 고유 식별자 추가
    private String name;
    private String tag;
    private List<String> repeatDays;
    // ⭐ 마지막 완료 날짜 기록 필드 추가
    private Map<String, LocalDate> lastCompletedDate;;
    
    // 기존 생성자 유지 lastCompleteDate 는 null 로 시작
    public Routine(String id, String name, String tag, List<String> repeatDays) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.repeatDays = new ArrayList<>(repeatDays); // ⭐ 깊은 복사 (11/11)
        this.lastCompletedDate = new ConcurrentHashMap<>(); // --251119
        
    }
    


    // ⭐ 로컬데이트에 관한 Getter 및 Setter 추가
    public Map<String, LocalDate> getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(Map<String, LocalDate> lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }
    
	// --251119: 특정 요일에 대해 오늘 완료되었는지 확인하는 메서드 추가
	public boolean isCompletedForDay(String day) {
		LocalDate lastDate = lastCompletedDate.get(day);
		return lastDate != null && lastDate.equals(LocalDate.now());
	}
	
	// --251119: 특정 요일의 완료 날짜를 오늘로 갱신하는 메서드 추가
	public void completeForDay(String day) {
		lastCompletedDate.put(day, LocalDate.now());
	}
    
    // ⭐ 오늘 완료했는지 확인하는 헬퍼 메서드 -- 오늘 날짜로 기록됨. / ex : 월요일 탭에서 생성한 a루틴을 체크 -> 체크된 a루틴의 객체가 오늘로 기록됨 -> 화요일 탭 이동하여 생성된 a루틴 확인 -> 
    // isCompletedToday()는 루틴의 lastCompletedDate가 오늘 날짜와 같으므로 **true**를 반환 -> 클릭 불가
    // 데이터 저장 클래스와 연동 할때 지금 이가 잘 되는지 판단할 것임
    public boolean isCompletedToday() {
        return lastCompletedDate != null && lastCompletedDate.equals(LocalDate.now());
    }
    
    // Getter 메서드
    public String getId() { return id; }
    public String getName() { return name; }
    public String getTag() { return tag; }
    public List<String> getRepeatDays() { return repeatDays; } // ⭐ Read-only 반환이 안전함. (11/11)

    // ⭐ Setter 메서드 (RoutineModify에서 사용) (11/11)
    public void setName(String name) { this.name = name; }
    public void setTag(String tag) { this.tag = tag; }
    public void setRepeatDays(List<String> repeatDays) { this.repeatDays = new ArrayList<>(repeatDays); } // ⭐ 리스트 교체

	// --251119: toString 메서드 추가 (디버깅용)
	@Override
	public String toString() {
		return "ID: " + id + ", 이름: " + name + ", 태그: " + tag + ", 반복: " + repeatDays.toString() + // --251112
				", 완료일(Map): " + lastCompletedDate.toString(); // --251112

	}
}
	