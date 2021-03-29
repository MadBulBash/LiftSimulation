import javax.swing.*; 
import javax.swing.border.*;
import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainGUI {  
    private static final int WIDTH = 900;
    private static final int HEIGHT = 700;
    private Task task;
    private Controller controller;
    private Thread contrThread;
    private LogAppender log;
    private JFrame jFrame;
    private JPanel upPanel;
    private JPanel logPanel;
    private Building pBuilding;       
    private JSpinner sCapacity;
    private JSpinner sHumans;
    private JSpinner sFloors;
    private JSlider sDelay;
    private JButton bStartStop;   
    private JTextArea tLog;
    private LiftGraph pLift;
      
    public int getFloors() {
        return Integer.decode(sFloors.getValue().toString());
    }   
    
    public int getHumans() {
        return Integer.decode(sHumans.getValue().toString());
    }

    public int getCapacity() {
        return Integer.decode(sCapacity.getValue().toString());
    }   

    public int getDelay() {
        return sDelay.getValue();
    }       
    
    public MainGUI() { 
        jFrame = new JFrame("Lift simulation"); 
        jFrame.setSize(WIDTH,HEIGHT);
        jFrame.setLayout(new BorderLayout());
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        jFrame.setMinimumSize(new Dimension(600, 700));
        upPanel = new JPanel();
        logPanel = new JPanel();
        upPanel.setBorder(new EtchedBorder());          
        logPanel.setBorder(new EtchedBorder());          
        upPanel.setLayout(new FlowLayout());
        logPanel.setLayout(new BorderLayout());
        bStartStop = new JButton("Start");
        sCapacity = new JSpinner(new SpinnerNumberModel(4, 1, 50, 1));
        sHumans = new JSpinner(new SpinnerNumberModel(20, 1, 1000, 1));
        sHumans.setPreferredSize(new Dimension(50, 22));
        sFloors = new JSpinner(new SpinnerNumberModel(4, 2, 50, 1));
        sDelay = new JSlider(JSlider.HORIZONTAL, 10, 20000, 10); 
        sDelay.setPreferredSize(new Dimension(200, 50));     
        tLog = new JTextArea(10,75);
        tLog.setLineWrap(true);
        tLog.setWrapStyleWord(true);
        tLog.setFont(new Font("System", Font.PLAIN, 10));
        tLog.setEditable(false);
        logPanel.add(new JScrollPane(tLog)); 
        jFrame.add(upPanel,BorderLayout.NORTH);
        jFrame.add(logPanel,BorderLayout.SOUTH);
        upPanel.add(new JLabel("Capacity:")); 
        upPanel.add(sCapacity); 
        upPanel.add(new JLabel("Humans:")); 
        upPanel.add(sHumans); 
        upPanel.add(new JLabel("Floors:")); 
        upPanel.add(sFloors); 
        upPanel.add(bStartStop); 
        upPanel.add(new JLabel("Delay:")); 
        upPanel.add(sDelay);
        log = new LogAppender(tLog);
        log.add("************************** Application started *******************************");
        jFrame.setVisible(true);  
        task = new Task(getHumans(), getFloors(), getCapacity(), getDelay());
        pBuilding = new Building(task, log);
        if(pBuilding.gBuf == null) {
            log.add("File Human.gif not found!");
            return;
        }
        pBuilding.setLayout(null);
        pLift = pBuilding.createLift();
        pBuilding.setBackground(Color.white);
        pBuilding.addLift();

        sDelay.addChangeListener(new  ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {      
                if(sDelay.getValueIsAdjusting()){
                    pBuilding.setTask(getDelay());
                }
            }
        });      
        
        bStartStop.addActionListener(new  ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {           
                if("Start".equals(bStartStop.getText())){
                    jFrame.setResizable(false);
                    bStartStop.setText("Stop");
                    task = new Task(getHumans(), getFloors(), getCapacity(), getDelay());
                    log.add("--------------------------- Simulation started ----------------------------");
                    log.add("----------- Lift capacity: " + task.getCapacity()
                            + ", humans: " + task.getHumans() + ", floors: "
                            + task.getFloors() + " -----------");
                    pBuilding = new Building(task, log);        
                    pBuilding.setLayout(null);   
                    pLift = pBuilding.createLift();
                    pBuilding.setBackground(Color.white);
                    pBuilding.addLift();
                    jFrame.remove(pBuilding);
                    jFrame.add(pBuilding,BorderLayout.CENTER);
                    jFrame.setVisible(true);
                    pBuilding.initGraphObjPosition();
                    controller = new Controller(task, tLog, pBuilding, log);
                    controller.humFactory();      
                    controller.addAllHumansOnPanel();
                    pBuilding.scanHumStartPositions();
                    pBuilding.repaint();
                    contrThread = new Thread(controller, "Controller");
                    controller.cntrlrAction(bStartStop);
                    contrThread.start();
                }
                else {
                    controller.running = false;
                    bStartStop.setText("Start");
                }
            }
        });
        
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log.add("************************* Application is closed "
                            + "******************************");
                System.exit(0);
            }
        });
    }
}
