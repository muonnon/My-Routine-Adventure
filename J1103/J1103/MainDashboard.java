package J1103;


 // ( 같은 패키지 J1103에 있으면 import 불필요) (다른 패키지에 있다면 import package.JavaFilename)
import javax.swing.*;

import java.awt.*;
import java.time.LocalDate;
// ⭐ LocalTime import 추가 (로그 시간 표시용) (11/11)
import java.time.LocalTime; 
import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;
// 2025.11.17
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.List;
import javax.swing.DefaultListModel;
import java.util.stream.Collectors;


public class MainDashboard extends JFrame {
    
    private RoutineManager manager;
    private Player player; // ⭐ Player 필드 추가

    // UI 컴포넌트
    private JLabel playerNameLabel;
    private JLabel playerLevelLabel; // ⭐ 레벨 표시 라벨 추가 (11/11)
    private JProgressBar expBar;
    private JLabel goldLabel;
    private JTextArea logArea; // 새로운 로그 영역
    private JButton invenButton; //25.11.17
    // ⭐ 오늘의 루틴 목록 UI 필드 추가 - 25.11.17
    private DefaultListModel<String> todayRoutineListModel;
    private JList<String> todayRoutineList;
    
    // ⭐ FileManager 객체 (로드 시에만 사용) (2025-11-12)
    private final FileManager fileManager = new FileManager(); 

    public MainDashboard() {
        
        // 1. Manager 생성 (자동으로 루틴 데이터 로드)
        this.manager = new RoutineManager(); 
        
        // 2. Player 데이터 로드 시도 및 로그 메시지 준비
        Player loadedPlayer = fileManager.loadPlayerState();
        
        final String startLogMessage; 
        
        if (loadedPlayer != null) {
            // Player 데이터가 있으면 로드
            this.player = loadedPlayer;
            startLogMessage = "프로그램 시작. (이전 데이터 로드)";
        } else {
            // NameSettingDialog 없이 기본값으로 Player 생성
            this.player = new Player("루틴 수행자"); 
            startLogMessage = "프로그램 시작. (새 프로필 생성: " + this.player.getName() + ")";
        } 
        
        // ⭐ Manager와 Player/Dashboard 연결 설정 (11/11)
        this.manager.setPlayer(this.player); 
        this.manager.setDashboard(this); 
        
        // ⭐ CRITICAL FIX: initUI()를 먼저 호출하여 logArea를 초기화 (2025-11-12)
        initUI(); 
        
        // ⭐ 윈도우 닫기 이벤트 리스너 추가 (2025-11-12)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // ⭐ CRITICAL FIX: public으로 변경된 RoutineManager.saveAllData() 호출 (2025-11-12)
                manager.saveAllData(); 
                dispose(); // 창 닫기
                System.exit(0); // 프로그램 종료
            }
        });
        
        // ⭐ 초기 UI 상태를 Player 데이터로 한 번 업데이트 (11/11)
        updatePlayerStatusUI(); 
        updateTodayRoutinesUI(); // ⭐ (추가) 초기 루틴 목록 로드 (2025.11.17)
    
        // ⭐ 로그 메시지를 logArea가 초기화된 후에 출력 (2025-11-12)
        addLogMessage(startLogMessage); 
    }
    
    private void initUI() {
        setTitle("나만의 루틴 RPG");
        // ⭐ 종료 버튼을 눌러도 바로 안 꺼지게 설정 (저장 로직을 위해) (2025-11-12)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        setSize(800, 700);
        setLocationRelativeTo(null);
        
        // =================================================================
        // 1. 탭 패널 생성 (전체 구조 변경) -- 25.11.19 - 연속일자를 위한 분리 작업
        // =================================================================
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // =================================================================
        // 2. 첫 번째 탭: 대시보드 (기존 화면)
        //    기존에는 JFrame에 바로 add, 이제는 dashboardPanel에 담기
        // =================================================================
        JPanel dashboardPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 좌상단: 플레이어 상태 (0, 0)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3; gbc.weighty = 0.2; // 가중치 설정 (20%)
        gbc.fill = GridBagConstraints.BOTH; // 전체 채우기
        gbc.insets = new Insets(5, 5, 5, 5); // 여백
        dashboardPanel.add(createPlayerStatusPanel(), gbc);
        
        // ⭐ 신규 추가: 루틴 관리 버튼 패널 (0, 1)
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3; gbc.weighty = 0.1; // 가중치 설정 (10%)
        dashboardPanel.add(createRoutineManagementPanel(), gbc); // 수정됨
        
        // 우상단: 보스 상태 (1, 0) -> 2개 행에 걸쳐 배치 (1, 0)과 (1, 1) 합침
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.gridheight = 2; // 2개 행을 합칩니다 (0, 1)
        gbc.weightx = 0.7; gbc.weighty = 0.3; // 가중치 설정 (30% = 20% + 10%)
        dashboardPanel.add(createBossStatusPanel(), gbc); // 수정됨
        gbc.gridheight = 1; // 기본값으로 복원

        // 좌하단: 오늘의 루틴 및 날짜 (0, 2)
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3; gbc.weighty = 0.7; // 가중치 설정 (70%)
        dashboardPanel.add(createDateRoutinePanel(), gbc); // 수정됨

        // 우하단: 시스템 로그 (1, 2)
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.weightx = 0.7; gbc.weighty = 0.7; // 가중치 설정 (70%)
        dashboardPanel.add(createLogPanel(), gbc); // 수정됨 이제 logArea가 생성
        
        // 대시보드 패널을 첫 번째 탭으로 추가
        tabbedPane.addTab("✅ 대시보드", dashboardPanel);
        
        // =================================================================
        // 3. 두 번째 탭: 연속 달성 현황 (StreakWindow) 25.11.19 - 연속일자 분리
        // =================================================================
        StreakWindow streakWindow = new StreakWindow(player);
        
        // StreakWindow에서 만든 패널을 가져와서 탭에 추가
        tabbedPane.addTab("🔥 연속 달성 현황", streakWindow.getUI());

        // 탭 패널을 프레임에 추가
        add(tabbedPane, BorderLayout.CENTER);
        
        // 툴바 추가 (루틴 관리 메뉴 제거됨)
        setJMenuBar(createMenuBar());
    }
    
    // 1. 플레이어 상태 패널 구현 (이름, 레벨, 경험치, 골드 + 인벤토리 버튼(251117))
    private JPanel createPlayerStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("플레이어 상태"));
        
        // 상태 정보 패널 (이름, 레벨, 골드)
        JPanel infoPanel = new JPanel(new GridLayout(3, 1)); 
        
        //1. 이름/레벨
        playerNameLabel = new JLabel("이름: " + player.getName() + " (Lv." + player.getLevel() + ")"); // ⭐ 레벨 표시 통합 (2025-11-12)
        playerLevelLabel = new JLabel("레벨: " + player.getLevel()); // 레벨 정보를 이름에 통합했지만, 필드 유지
        goldLabel = new JLabel("골드: " + player.getGold() + " G");
        
        infoPanel.add(playerNameLabel);
        infoPanel.add(playerLevelLabel);
        infoPanel.add(goldLabel);
        
        panel.add(infoPanel, BorderLayout.NORTH);
        
        //경험치바와 인벤토리 버튼을 담을 컨테이너 - 251117 (센터 배치 후 공간 사용)
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // 경험치 바 - 센터 패널의 NORTH
        expBar = new JProgressBar(0, player.getMaxExp());
        expBar.setValue(player.getCurrentExp());
        expBar.setStringPainted(true);
        // ⭐ 경험치 바 텍스트 포맷 변경 (11/11)
        expBar.setString(player.getCurrentExp() + " / " + player.getMaxExp() + " EXP");
        centerPanel.add(expBar, BorderLayout.NORTH);//251117
        
        // 인벤토리 버튼 추가 - 센터 패널의 CNETER 251117
        invenButton = new JButton("인벤토리 보기"); 
        invenButton.addActionListener(e -> openInventoryView()); 
        centerPanel.add(invenButton, BorderLayout.CENTER);
        
        // ⭐ [추가] 상점 버튼 생성  2025.11.19 - 김민기 : 상점 연동
        JButton shopButton = new JButton("아이템 상점");
        shopButton.addActionListener(e -> new ShopView(player, this)); // this = MainDashboard
        
        // 버튼들을 담을 패널 생성 (버튼 2개를 나란히 놓기 위해) 2025.11.19 - 김민기 : 인벤토리옆에 두기위해 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.add(invenButton);
        buttonPanel.add(shopButton);
        
        // 센터 패널에 추가
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        
        //메인 패널에 센터패널 추가 251117
        panel.add(centerPanel, BorderLayout.CENTER); //기존의 남쪽 대신 중앙에 배치했습니다
        
        return panel;
    }
    
    // ⭐ 플레이어 상태 UI 갱신 메서드 (11/11)
    public void updatePlayerStatusUI() {
        if (player != null) {
            playerNameLabel.setText("이름: " + player.getName() + " (Lv." + player.getLevel() + ")"); // ⭐ 레벨 표시 통합 (2025-11-12)
            playerLevelLabel.setText("레벨: " + player.getLevel()); // 임시
            goldLabel.setText("골드: " + player.getGold() + " G");
            
            expBar.setMaximum(player.getMaxExp()); // 최대 경험치 업데이트
            expBar.setValue(player.getCurrentExp());
            expBar.setString("EXP: " + player.getCurrentExp() + " / " + player.getMaxExp()); // ⭐ EXP 문자열 수정 (2025-11-12)
        }
        updateTodayRoutinesUI(); // ⭐ (추가) 루틴 완료 시 목록 갱신 2025.11.17 - 김민기
    }
    
    // 2. 보스 상태 패널 구현 (임시)
    private JPanel createBossStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("오늘의 보스 상태"));
        
        JPanel bossPanel = new JPanel(new BorderLayout());
        
        // ⭐ 보스 이미지 (임시) ------------------------------------------------------------------------
        JLabel bossImageLabel = new JLabel("[보스 이미지 영역]", JLabel.CENTER); 
        bossPanel.add(bossImageLabel, BorderLayout.CENTER); 
        
        // ⭐ 보스 체력 바
        JProgressBar bossHpBar = new JProgressBar(0, 100);
        bossHpBar.setValue(100); // 시작 체력 100
        bossHpBar.setForeground(Color.RED);
        bossHpBar.setStringPainted(true);
        bossHpBar.setString("HP: 100/100"); 
        bossPanel.add(bossHpBar, BorderLayout.SOUTH);
        
        panel.add(bossPanel, BorderLayout.CENTER);
        return panel;
    }
    
    // 3. 로그 패널 구현 
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("시스템 로그"));
        
        // ⭐ 이 시점에 logArea 필드가 JTextArea 객체로 초기화됩니다.
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        
        // 로그를 추가할 때 맨 아래로 스크롤되도록 설정
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ⭐ 로그 메시지 추가 메서드 (11/11)
    public void addLogMessage(String message) {
        // 시간 포맷 (예: [09:30:00] )
        String timeStamp = "[" + LocalTime.now().withNano(0).toString() + "] "; 
        
        // 기존 텍스트에 새 메시지 추가
        logArea.append(timeStamp + message + "\n"); // ⭐ 줄바꿈 문자 추가 (2025-11-12)
        
        // 스크롤을 맨 아래로 이동
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    // 4. 오늘의 루틴 및 날짜 패널 - 25.11.17
    private JPanel createDateRoutinePanel() {
        JPanel panel = new JPanel(new BorderLayout()); // ⭐ BorderLayout으로 수정 (11/11)
        panel.setBorder(BorderFactory.createTitledBorder("오늘의 루틴"));
        
     // 상단: 현재 날짜 표시
        // ⭐ getTodayKoreanDayName() 메서드는 아래 D 단계에서 추가
        String todayDayName = getTodayKoreanDayName(); 
        JLabel dateLabel = new JLabel(LocalDate.now().toString() + " (" + todayDayName + "요일)", JLabel.CENTER);
        panel.add(dateLabel, BorderLayout.NORTH);

        // 중앙: JList 초기화
        todayRoutineListModel = new DefaultListModel<>();
        todayRoutineList = new JList<>(todayRoutineListModel);
        
        JScrollPane scrollPane = new JScrollPane(todayRoutineList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ⭐오늘 요일 이름 반환 헬퍼 메서드 25.11.17
    private String getTodayKoreanDayName() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        // Locale을 한국어로 설정하여 "월", "화" 등으로 표시
        return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREA);
    }
    
    /**
     * ⭐ (신규) '오늘의 루틴' JList를 최신 데이터로 갱신합니다.
     */
    public void updateTodayRoutinesUI() {
        if (todayRoutineListModel == null) return; // UI가 아직 초기화되지 않았다면 종료
        
        todayRoutineListModel.clear(); // 목록 초기화
        String todayDayName = getTodayKoreanDayName();
        
        // Manager에서 오늘 요일의 루틴 목록 가져오기
        List<Routine> routines = manager.getRoutinesByDay(todayDayName);
        
        if (routines.isEmpty()) {
            todayRoutineListModel.addElement("✅ 오늘 루틴이 없습니다. 휴식을 취하세요!");
        } else {
        // 정렬: 미완료 루틴을 위로, 완료된 루틴을 아래로 - 251124
        routines.sort((r1, r2) -> {
            // 1. 완료 여부로 먼저 정렬 (false가 true보다 앞에)
            boolean completed1 = r1.isCompletedForDay(todayDayName);
            boolean completed2 = r2.isCompletedForDay(todayDayName);
            
            int completedCompare = Boolean.compare(completed1, completed2);
            if (completedCompare != 0) {
                return completedCompare; // 미완료가 먼저
            }
            
            // 2. 완료 상태가 같으면 이름순 정렬
            return r1.getName().compareTo(r2.getName());
        });
        
        // 정렬된 루틴 목록을 화면에 추가
        for (Routine routine : routines) {
            String status = routine.isCompletedForDay(todayDayName) ? "[✔ 완료]" : "[☐ 미완료]";
            todayRoutineListModel.addElement(status + " " + routine.getName());
        }
    }
    }
    
    
    
    
    // 5. 툴바 구현 (루틴 관리가 제거되었으므로 비어있음)
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        return menuBar;
    }
    
    // ⭐ 신규 추가: 루틴 생성/목록 버튼 패널 (0, 1 위치)
    private JPanel createRoutineManagementPanel() {
        // FlowLayout을 사용하여 버튼을 중앙에 배치
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createTitledBorder("루틴 관리"));
        
        // 1. 루틴 생성 버튼
        JButton createButton = new JButton("루틴 생성");
        createButton.addActionListener(e -> {
            RoutineManagerGUI gui = new RoutineManagerGUI(manager); // manager 전달
            gui.setVisible(true);
            // (추가) 루틴 생성 창이 닫힌 후, 오늘의 루틴 목록을 갱신 2025.11.17 - 김민기
            updateTodayRoutinesUI();
        });
        
        // 2. 루틴 목록/수정/삭제 버튼 (클릭 시 RoutineListView 표시)
        JButton listButton = new JButton("루틴 목록/수정/삭제");
        // ⭐ CRITICAL FIX: listButton에 ActionListener 추가 (2025-11-12)
        listButton.addActionListener(e -> {
            // RoutineListView 객체 생성
            RoutineListView listView = new RoutineListView(manager);
            // 목록 창을 화면에 표시
            listView.setVisible(true);
            // 데이터 로드 (목록에 현재 루틴을 표시)
            listView.loadAllRoutines(); 
        });
        
        panel.add(createButton);
        panel.add(listButton);
        
        return panel;
    }
    
    //인벤토리 창을 여는 메소드 251117
    private void openInventoryView() {
    	new InventoryView(player);
    }
    
    /**
     * 프로그램의 메인 시작점입니다.
     */
    public static void main(String[] args) {
        // SwingUtilities.invokeLater를 사용하여 EDT에서 GUI를 시작합니다.
        SwingUtilities.invokeLater(() -> {
            // MainDashboard 객체 생성 (생성자 내부에서 initUI() 호출 및 데이터 로드/저장 로직 설정)
            MainDashboard mainDashboard = new MainDashboard(); 
            mainDashboard.setVisible(true); // 창을 화면에 표시
        });
    }
}