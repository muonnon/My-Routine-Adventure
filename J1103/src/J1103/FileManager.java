// 2025-12-05: 데이터 영속성 버그 수정
package J1103;

//루틴 데이터를 텍스트 파일에 저장하고 불러오는 클래스
//RoutineManager의 데이터 영속성 책임 분리

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// 루틴 데이터를 파일에 저장하고 불러오는 일만 담당하는 클래스입니다.
public class FileManager {

	private static final String SEPARATOR = "|"; // 필드 구분자
	private static final String DAY_SEPARATOR = ","; // 요일 목록 구분자
	
	private static final String COMPLETION_ENTRY_SEPARATOR = ";"; //--251119
	private static final String COMPLETION_KV_SEPARATOR = ":"; //--251119
	
	private static final String PLAYER_FILE_NAME = "player_data.txt"; // ⭐ Player 파일 이름 상수 (2025-11-12)
	private static final String BOSS_FILE_NAME = "boss_data.txt"; // ⭐ Boss 파일 이름 상수 (2025-12-05)

	// 루틴 목록을 파일에 저장하는 기능
	// ⭐ 2025-12-05: Map 형식으로 완료 날짜 저장 수정
	public void saveRoutinesToFile(List<Routine> routines, String fileName) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
			for (Routine routine : routines) {
				// 1. 요일 리스트를 문자열로 변환
				String daysString = routine.getRepeatDays().stream().collect(Collectors.joining(DAY_SEPARATOR));

				// 2. ⭐ 완료 날짜 Map을 문자열로 변환 (형식: 월:2025-12-05;화:2025-12-03)
				Map<String, LocalDate> completedMap = routine.getLastCompletedDate();
				String dateString;
				if (completedMap == null || completedMap.isEmpty()) {
					dateString = "null";
				} else {
					dateString = completedMap.entrySet().stream()
						.map(entry -> entry.getKey() + COMPLETION_KV_SEPARATOR + entry.getValue().toString())
						.collect(Collectors.joining(COMPLETION_ENTRY_SEPARATOR));
				}

				// 파일에 저장할 형식: ID|이름|태그|요일목록|완료날짜Map
				String line = String.format("%s%s%s%s%s%s%s%s%s", 
											routine.getId(), SEPARATOR, 
											routine.getName(), SEPARATOR, 
											routine.getTag(), SEPARATOR, 
											daysString, SEPARATOR, 
											dateString);

				writer.println(line);
			}
			System.out.println("✅ " + routines.size() + "개의 루틴 저장 완료: " + fileName);
		} catch (IOException e) {
			System.err.println("❌ 루틴 데이터를 파일에 저장하는 중 오류 발생: " + e.getMessage());
		}
	}

	// 루틴 목록을 파일에서 불러오는 기능
	public List<Routine> loadRoutinesFromFile(String fileName) {
		List<Routine> loadedRoutines = new ArrayList<>();
		File file = new File(fileName);

		if (!file.exists()) {
			System.out.println("⚠️ 저장된 파일(" + fileName + ")이 없어 새로 시작합니다.");
			return loadedRoutines;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// 정규식 문자열을 피하기 위해 분리자(SEPARATOR: "|") 앞에 \\를 붙여야 합니다.
				String[] parts = line.split("\\" + SEPARATOR); 

				// 루틴 ID, 이름, 태그, 요일, 완료날짜 (총 5개)
				if (parts.length >= 4) { // 최소 4개 (구버전 호환용)
					String id = parts[0];
					String name = parts[1];
					String tag = parts[2];
					List<String> repeatDays = Arrays.asList(parts[3].split(DAY_SEPARATOR));

					// 5번째 필드 (완료 날짜) 처리 -- 251119 수정
					Map<String, LocalDate> lastCompletedDateMap = new ConcurrentHashMap<>(); 
					String completionStr = parts[4];
					
					if (!"null".equals(completionStr) && !completionStr.isEmpty()) { 
						String[] entries = completionStr.split(COMPLETION_ENTRY_SEPARATOR); 
						for (String entry : entries) { 
							String[] kv = entry.split(COMPLETION_KV_SEPARATOR); // 요일:날짜 분리 
							if (kv.length == 2) { 
								String day = kv[0]; 
								String dateStr = kv[1];
								try { 
									LocalDate date = LocalDate.parse(dateStr); 
									lastCompletedDateMap.put(day, date); // 맵에 저장
								} catch (DateTimeParseException ignored) { 
									// 날짜 형식이 잘못된 경우 무시
								} 
							} 
						}
					}

					//--251119: 루틴 객체 생성 및 완료 상태 Map 설정
					Routine routine = new Routine(id, name, tag, repeatDays); 
					routine.setLastCompletedDate(lastCompletedDateMap); 

					loadedRoutines.add(routine);
				}
			}
		} catch (IOException e) {
			System.err.println("❌ 루틴 데이터를 파일에서 불러오는 중 오류 발생: " + e.getMessage());
		}
		return loadedRoutines;
	}
	
	/**
	 * ⭐ 플레이어 상태를 파일에 저장합니다. (2025-11-12)
	 * 형식: 이름|레벨|현재경험치|최대경험치|골드
	 */
	public void savePlayerState(Player player, String fileName) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
			String line = String.format("%s%s%d%s%d%s%d%s%d",
										player.getName(), SEPARATOR, 
										player.getLevel(), SEPARATOR, 
										player.getCurrentExp(), SEPARATOR, 
										player.getMaxExp(), SEPARATOR, 
										player.getGold());
			writer.println(line);
		} catch (IOException e) {
			System.err.println("❌ 플레이어 데이터를 파일에 저장하는 중 오류 발생: " + e.getMessage());
		}
	}
	
	/**
	 * ⭐ 플레이어 상태를 파일에서 불러옵니다. (2025-11-12)
	 * MainDashboard에서 호출 시 파일 이름을 생략하도록 오버로드했습니다.
	 */
	public Player loadPlayerState() {
		return loadPlayerState(PLAYER_FILE_NAME);
	}
	
	/**
	 * ⭐ 플레이어 상태를 파일에서 불러옵니다. (2025-11-12)
	 * 파일이 없거나 형식이 잘못되면 null을 반환합니다.
	 */
	public Player loadPlayerState(String fileName) {
		File file = new File(fileName);

		if (!file.exists()) {
			return null;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = reader.readLine();
			if (line != null) {
				String[] parts = line.split("\\" + SEPARATOR);

				// 이름, 레벨, 현재Exp, 최대Exp, 골드 (총 5개)
				if (parts.length == 5) { 
					String name = parts[0];
					int level = Integer.parseInt(parts[1]);
					int currentExp = Integer.parseInt(parts[2]);
					int maxExp = Integer.parseInt(parts[3]);
					int gold = Integer.parseInt(parts[4]);

					Player player = new Player(name);
					player.setLevel(level); // Player.java에 setLevel이 있어야 합니다.
					player.setCurrentExp(currentExp);
					player.setMaxExp(maxExp); // Player.java에 setMaxExp가 있어야 합니다.
					player.setGold(gold);

					return player;
				}
			}
		} catch (IOException | NumberFormatException e) {
			System.err.println("❌ 플레이어 데이터를 파일에서 불러오는 중 오류 발생. 새 프로필로 시작합니다: " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * ⭐ 보스 상태를 파일에 저장합니다. (2025-12-05)
	 * 형식: 월|현재HP|처치여부
	 */
	public void saveBossState(Boss boss, String fileName) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
			String line = String.format("%d%s%d%s%s",
										boss.getMonth(), SEPARATOR,
										boss.getCurrentHp(), SEPARATOR,
										boss.isDefeated());
			writer.println(line);
			System.out.println("✅ 보스 상태 저장 완료: " + fileName);
		} catch (IOException e) {
			System.err.println("❌ 보스 데이터를 파일에 저장하는 중 오류 발생: " + e.getMessage());
		}
	}
	
	/**
	 * ⭐ 보스 상태를 파일에서 불러옵니다. (2025-12-05)
	 * 파일이 없거나 월이 다르면 새 보스를 생성하여 반환합니다.
	 */
	public Boss loadBossState() {
		return loadBossState(BOSS_FILE_NAME);
	}
	
	/**
	 * ⭐ 보스 상태를 파일에서 불러옵니다. (2025-12-05)
	 */
	public Boss loadBossState(String fileName) {
		File file = new File(fileName);
		Boss boss = new Boss(); // 기본 보스 생성 (현재 월 기준)
		
		if (!file.exists()) {
			System.out.println("⚠️ 보스 데이터 파일이 없어 새 보스를 생성합니다.");
			return boss;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = reader.readLine();
			if (line != null) {
				String[] parts = line.split("\\" + SEPARATOR);

				// 월, 현재HP, 처치여부 (총 3개)
				if (parts.length == 3) {
					int savedMonth = Integer.parseInt(parts[0]);
					int currentHp = Integer.parseInt(parts[1]);
					boolean isDefeated = Boolean.parseBoolean(parts[2]);
					
					int currentMonth = DateUtil.getToday().getMonthValue();
					
					// ⭐ 저장된 월과 현재 월이 다르면 새 보스로 초기화 (매월 1일 리셋)
					if (savedMonth != currentMonth) {
						System.out.println("📅 새로운 달이 시작되어 보스가 초기화됩니다! (" + savedMonth + "월 → " + currentMonth + "월)");
						return boss; // 새 보스 반환
					}
					
					// 같은 달이면 저장된 상태 복원
					boss.setCurrentHp(currentHp);
					boss.setDefeated(isDefeated);
					System.out.println("✅ 보스 상태 로드 완료: HP=" + currentHp + "/" + boss.getMaxHp() + ", 처치=" + isDefeated);
					return boss;
				}
			}
		} catch (IOException | NumberFormatException e) {
			System.err.println("❌ 보스 데이터를 불러오는 중 오류 발생. 새 보스로 시작합니다: " + e.getMessage());
		}
		return boss;
	}
}


