package J1103;

import java.io.Serializable;
import java.time.LocalDate;

public class Boss implements Serializable {
    private static final long serialVersionUID = 1L;

    private int month;       // 보스가 출현한 월 (1~12)
    private String name;     // 보스 이름
    private String desc;     // 보스 컨셉 설명
    private int maxHp;
    private int currentHp;
    private boolean isDefeated; // 처치 여부
    private String imagePath; // 25.11.30 김민기 : 보스 이미지 파일 경로 저장
    private String hitImagePath;

    public Boss() {
        // 생성 시 현재 월에 맞는 보스 소환
        spawnBossForThisMonth();
    }

    /**
     * 현재 날짜(월)에 맞춰 보스 데이터를 초기화합니다.
     */
    public void spawnBossForThisMonth() {
        LocalDate now = LocalDate.now();
        this.month = now.getMonthValue(); // 현재 월 (1~12)
        this.isDefeated = false;
        
        // 밸런스 설정: 100 HP
        // (31일 동안 매일 1개만 해도 깰 수 있게 하려면, 루틴 1개당 데미지를 4~5 정도로 설정하면 됨)
        this.maxHp = 100; 
        this.currentHp = this.maxHp;

        // 월별 컨셉 설정 (개발자가 텍스트만 채우면 됨)
        setMonthlyConcept(this.month);
    }

    /**
     * ⭐ [개발자 영역] 월별 보스 컨셉과 스토리를 설정하는 곳입니다.
     * 여기에 원하시는 텍스트를 채워 넣으세요.
     */
    private void setMonthlyConcept(int month) {  // ------------------------------------------------------- 1~12월까지 채우면됨 이후 default 삭제 : 현재는 그리할 이유가 없으므로 12,1,2 까지 진행
        switch (month) {
            case 1:
                this.name = "희망찬 겨울 (1월)";
                this.desc = "새해의 결심을 희망하는 겨울입니다.";
//                this.imagePath = "images/SnowBoss.png";
//                this.hitImagePath = "images/SnowBoss_Hit.png";
                break;
            case 2:
                this.name = "졸음의 초콜릿 몬스터 (2월)";
                this.desc = "달콤한 잠으로 유혹하는 몬스터입니다.";
//                this.imagePath = "images/boss_1.png" 
//                this.hitImagePath = "images/Boss2_Hit.png";
                break;
            // ... 3월 ~ 12월까지 case 추가 ...a
            case 3:
                this.name = "몬스터 (3월)";
                this.desc = "몬스터입니다.";
//              this.imagePath = "images/boss_1.png" 
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 4:
                this.name = "몬스터 (4월)";
                this.desc = "몬스터입니다.";
//              this.imagePath = "images/boss_1.png" 
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 5:
                this.name = "몬스터 (5월)";
                this.desc = "몬스터입니다.";
//              this.imagePath = "images/boss_1.png" 
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 6:
                this.name = "몬스터 (6월)";
                this.desc = "몬스터입니다.";
//              this.imagePath = "images/boss_1.png" 
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 7:
                this.name = "몬스터 (7월)";
                this.desc = "몬스터입니다.";
//              this.imagePath = "images/boss_1.png" 
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 8:
                this.name = "몬스터 (8월)";
                this.desc = "몬스터입니다.";
//              this.imagePath = "images/boss_1.png" 
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 9:
                this.name = "몬스터 (9월)";
                this.desc = "몬스터입니다.";
//              this.imagePath = "images/boss_1.png" // 
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 10:
                this.name = "몬스터 (10월)";
                this.desc = "몬스터입니다.";
//              this.imagePath = "images/boss_1.png" // 10월 보스 이미지....
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 11:
                this.name = "빼빼로킹 (11월)";
                this.desc = "빼빼로를 주면 안잡아먹는답니다";
              this.imagePath = "images/StickChocolateBoss.png"; // 11월 보스 이미지....
//              this.hitImagePath = "images/Boss3_Hit.png";
                break;
            case 12:
                this.name = "나태의 눈사람 (12월)";
                this.desc = "새해의 결심을 얼려버리려는 차가운 눈사람입니다.";
                this.imagePath = "images/SnowBoss.png"; 
                this.hitImagePath = "images/SnowBoss_Hit.png";
        }
    }

    /**
     * ⭐ [개발자 영역] 해피 엔딩 스토리 (보스 처치 성공 시)
     */
    public String getHappyStory() {
        switch (this.month) {
            case 1:
                return "눈사람이 녹아내리며 따뜻한 의지가 피어올랐습니다!\n"
                     + "당신은 나태함을 이겨내고 멋진 새해를 시작했습니다.";
            // ... 월별 해피 스토리 추가 ...
            default:
                return "보스를 물리치고 성취감을 얻었습니다!";
        }
    }

    /**
     * ⭐ [개발자 영역] 배드 엔딩 스토리 (월이 지날 때까지 처치 못함)
     */
    public String getBadStory() {
        switch (this.month) {
            case 1:
                return "눈사람의 한기에 마음까지 얼어버렸습니다...\n"
                     + "1월의 결심은 눈 속에 파묻히고 말았습니다.";
            // ... 월별 배드 스토리 추가 ...
            default:
                return "게으름에 지고 말았습니다. 다음 달엔 힘내봅시다.";
        }
    }

    // --- 전투 로직 ---

    public boolean takeDamage(int damage) {
        if (isDefeated) return false; // 이미 죽었으면 데미지 안 받음

        this.currentHp -= damage;
        if (this.currentHp <= 0) {
            this.currentHp = 0;
            this.isDefeated = true;
            return true; // 처치됨 (막타)
        }
        return false; // 아직 살아있음
    }

    // --- Getters ---
    public String getName() { return name; }
    public String getDesc() { return desc; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getMonth() { return month; }
    public boolean isDefeated() { return isDefeated; }
    public String getImagePath() { return imagePath; } // 25.11.30 김민기 : 이미지 경로
    public String getHitImagePath() { return hitImagePath; } // 25.11.30 김민기 : 공격받은 모션
}