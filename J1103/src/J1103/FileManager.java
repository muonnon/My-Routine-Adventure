package J1103;

//루틴 데이터를 텍스트 파일에 저장하고 불러오는 클래스
//RoutineManager의 데이터 영속성 책임 분리

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map; //--251119
import java.util.concurrent.ConcurrentHashMap; //--251119
import java.util.stream.Collectors;

// 루틴 데이터를 파일에 저장하고 불러오는 일만 담당하는 클래스입니다.
public class FileManager {

   private static final String SEPARATOR = "|"; // 필드 구분자
   private static final String DAY_SEPARATOR = ","; // 요일 목록 구분자
   
   private static final String COMPLETION_ENTRY_SEPARATOR = ";"; //--251119
   private static final String COMPLETION_KV_SEPARATOR = ":"; //--251119
   
   private static final String PLAYER_FILE_NAME = "player_data.dat"; // ⭐ Player 파일 이름 상수 (2025-11-12) -- 25.11.24 김민기 : 확장자 변경 txt - > dat

   // 루틴 목록을 파일에 저장하는 기능
   public void saveRoutinesToFile(List<Routine> routines, String fileName) {
      try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
         for (Routine routine : routines) {
            // 1. 요일 리스트를 문자열로 변환
            String daysString = routine.getRepeatDays().stream().collect(Collectors.joining(DAY_SEPARATOR));

            // 2. 마지막 완료 날짜를 Map을 문자열로 변환 (null이면 "null" 저장) - 251124 수정
            Map<String, LocalDate> completionMap = routine.getLastCompletedDate();
               
            String dateString;
               if (completionMap == null || completionMap.isEmpty()) {
                   dateString = "null";
               } else {
                   // Map을 "월:2024-11-25;화:2024-11-24" 형식으로 변환
                   dateString = completionMap.entrySet().stream()
                       .map(entry -> entry.getKey() + COMPLETION_KV_SEPARATOR + entry.getValue().toString())
                       .collect(Collectors.joining(COMPLETION_ENTRY_SEPARATOR));
               }

            // 파일에 저장할 형식: ID|이름|태그|요일목록|완료날짜
            String line = String.format("%s%s%s%s%s%s%s%s%s", 
                                 routine.getId(), SEPARATOR, 
                                 routine.getName(), SEPARATOR, 
                                 routine.getTag(), SEPARATOR, 
                                 daysString, SEPARATOR, 
                                 dateString);

            writer.println(line);
            // 디버깅용 출력 251124
            System.out.println("저장: " + line);
         }
      } catch (IOException e) {
         System.err.println("❌ 루틴 데이터를 파일에 저장하는 중 오류 발생: " + e.getMessage());
      }
   }

   // 루틴 목록을 파일에서 불러오는 기능
   public List<Routine> loadRoutinesFromFile(String fileName) {
      List<Routine> loadedRoutines = new ArrayList<>();
      File file = new File(fileName);

      if (!file.exists()) {
         System.out.println("저장된 파일(" + fileName + ")이 없어 새로 시작합니다.");
         return loadedRoutines;
      }

      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
         String line;
         while ((line = reader.readLine()) != null) {
            // 정규식 문자열을 피하기 위해 분리자(SEPARATOR: "|") 앞에 \\를 붙여야 합니다.
            String[] parts = line.split("\\" + SEPARATOR); 

            // 루틴 ID, 이름, 태그, 요일, 완료날짜 (총 5개)
            if (parts.length >= 5) { // 최소 4개 (구버전 호환용)
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
                           System.out.println("날짜 파싱 실패: "+dateStr); //디버깅용, 251124
                        } 
                     } 
                  }
               } else {
                   System.out.println("잘못된 데이터 형식 (필드 부족): " + line); //251124
               }

               //--251119: 루틴 객체 생성 및 완료 상태 Map 설정
               Routine routine = new Routine(id, name, tag, repeatDays); 
               routine.setLastCompletedDate(lastCompletedDateMap); 

               loadedRoutines.add(routine);
            }
         }
      } catch (IOException e) {
         System.err.println("루틴 데이터를 파일에서 불러오는 중 오류 발생: " + e.getMessage());
      }
      
       // ⭐ 추가: 로드 결과 출력 (디버깅용) 251124
       System.out.println("Check" + loadedRoutines.size() + "개의 루틴 로드 완료");
       for (Routine r : loadedRoutines) {
           System.out.println("  - " + r.getName() + " : " + r.getLastCompletedDate());
       }
      
      return loadedRoutines;
   }
   
   
   /**
    * 객체를 파일에 저장 (직렬화) - 25.11.24 - 김민기  : 기존 코드는 이름|래밸|골드| 등과 같은 단순 숫자/문자만 저장하는 방식이다. 하지만 클래스안에 인벤토리 장착장비 등과 같은복잡한 데이터가 추가되었기 때문에 기존의 텍스트 방식으로는 부족하다 판단
    */
   public void saveObject(Object object, String fileName) {
       try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
           oos.writeObject(object);
           System.out.println("데이터 저장 완료: " + fileName);
       } catch (IOException e) {
           System.err.println("데이터 저장 실패 (" + fileName + "): " + e.getMessage());
       }
   }

   /**
    * 파일에서 객체 불러오기 (역직렬화)
    */
   public Object loadObject(String fileName) {
       File file = new File(fileName);
       if (!file.exists()) return null;

       try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
           return ois.readObject();
       } catch (IOException | ClassNotFoundException e) {
           System.err.println("❌ 데이터 로드 실패 (" + fileName + "): " + e.getMessage());
           return null;
       }
   }

   // =================================================================
   // [Section 3] 플레이어 데이터 관리 (직렬화 방식으로 변경)
   // =================================================================
   
   // ⚠️ 중요: 텍스트 방식 저장 메서드(savePlayerState)를 직렬화 방식으로 교체합니다.
   // 아이템, 장비, 스트릭 정보를 텍스트로 파싱하는 것은 너무 복잡하기 때문입니다.
   
   public void savePlayerState(Player player, String fileName) {
       saveObject(player, fileName);
   }
   
   public Player loadPlayerState() {
       return loadPlayerState(PLAYER_FILE_NAME);
   }
   
   public Player loadPlayerState(String fileName) {
       return (Player) loadObject(fileName);
   }

   // =================================================================
   // [Section 4] 보스 데이터 관리 (직렬화 방식)
   // =================================================================

   public void saveBossState(Boss boss, String fileName) {
       saveObject(boss, fileName);
   }

   public Boss loadBossState(String fileName) {
       return (Boss) loadObject(fileName);
   }
}