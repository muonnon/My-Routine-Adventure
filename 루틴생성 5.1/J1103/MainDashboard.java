package J1103;


 // ( 같은 패키지 J1103에 있으면 import 불필요) (다른 패키지에 있다면 import package.JavaFilename)
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
// ⭐ LocalTime import 추가 (로그 시간 표시용) (11/11)
import java.time.LocalTime; 
import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent; 


public class MainDashboard extends JFrame {
    
    private RoutineManager manager;
    private Player player; // ⭐ Player 필드 추가

    // UI 컴포넌트
    private JLabel playerNameLabel;
    private JLabel playerLevelLabel; // ⭐ 레벨 표시 라벨 추가 (11/11)
    private JProgressBar expBar;
    private JLabel goldLabel;
    private JTextArea logArea; // 새로운 로그 영역
    
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
        
        // ⭐ CRITICAL FIX: initUI()를 먼저 호출하여 logArea를 초기화합니다. (2025-11-12)
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
        
        // ⭐ 로그 메시지를 logArea가 초기화된 후에 출력합니다. (2025-11-12)
        addLogMessage(startLogMessage); 
    }
    
    private void initUI() {
        setTitle("나만의 루틴 RPG");
        // ⭐ 종료 버튼을 눌러도 바로 안 꺼지게 설정 (저장 로직을 위해) (2025-11-12)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        setSize(800, 700);
        setLocationRelativeTo(null);
        
        // 전체 레이아웃: GridBagLayout을 사용하여 복잡한 4개 구역 배치를 쉽게 합니다.
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 좌상단: 플레이어 상태 (0, 0)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3; gbc.weighty = 0.2; // 가중치 설정 (20%)
        gbc.fill = GridBagConstraints.BOTH; // 전체 채우기
        gbc.insets = new Insets(5, 5, 5, 5); // 여백
        add(createPlayerStatusPanel(), gbc);
        
        // ⭐ 신규 추가: 루틴 관리 버튼 패널 (0, 1)
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3; gbc.weighty = 0.1; // 가중치 설정 (10%)
        add(createRoutineManagementPanel(), gbc);
        
        // 우상단: 보스 상태 (1, 0) -> 2개 행에 걸쳐 배치 (1, 0)과 (1, 1) 합침
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.gridheight = 2; // 2개 행을 합칩니다 (0, 1)
        gbc.weightx = 0.7; gbc.weighty = 0.3; // 가중치 설정 (30% = 20% + 10%)
        add(createBossStatusPanel(), gbc);
        gbc.gridheight = 1; // 기본값으로 복원

        // 좌하단: 오늘의 루틴 및 날짜 (0, 2)
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3; gbc.weighty = 0.7; // 가중치 설정 (70%)
        add(createDateRoutinePanel(), gbc);

        // 우하단: 시스템 로그 (1, 2)
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.weightx = 0.7; gbc.weighty = 0.7; // 가중치 설정 (70%)
        add(createLogPanel(), gbc); 
        
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
        
        // 경험치 바
        expBar = new JProgressBar(0, player.getMaxExp());
        expBar.setValue(player.getCurrentExp());
        expBar.setStringPainted(true);
        // ⭐ 경험치 바 텍스트 포맷 변경 (11/11)
        expBar.setString(player.getCurrentExp() + " / " + player.getMaxExp() + " EXP");
        
        panel.add(expBar, BorderLayout.SOUTH);
        
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
    
    // 4. 오늘의 루틴 및 날짜 패널 (미구현 상태)
    private JPanel createDateRoutinePanel() {
        JPanel panel = new JPanel(new BorderLayout()); // ⭐ BorderLayout으로 수정 (11/11)
        panel.setBorder(BorderFactory.createTitledBorder("오늘의 루틴"));
        panel.add(new JLabel(LocalDate.now().toString(), JLabel.CENTER), BorderLayout.NORTH);
        JTextArea routineList = new JTextArea("오늘의 루틴 목록 (미구현)");
        routineList.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(routineList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
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