import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JTextArea;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;

public class Controller implements Runnable {
    //private static final int RND = 100;
    public Building pBuilding;
    public boolean running = true;          // flag for trminate simulation
    private Task task;
    private List<HumanGraph> allHumans;
    private ThreadGroup hmnsThread;
    private Thread liftThread;
    private int freeSpace;                  // free space in lift
    private int transported;                // transported humans
    private int transact;                   // lift turns
    private LogAppender log;
    private JButton btn;                    // Button (Start/Stop)

    
    public Controller(Task task, JTextArea jte, Building jPan, LogAppender log){
        this.log = log;
        this.task = task;                   // set task for using in all objects
        freeSpace = task.getCapacity();     // set current free space in lift
        pBuilding = jPan;
    }
    
    public void setTask(Task task) {
        this.task = task;
    }
    
    public void stratLiftThread() {   
        liftThread = new Thread(pBuilding.pLift, "Lift");
        pBuilding.pLift.running = true;
        liftThread.start();
    }
    
    synchronized public void interruptHumanThreads() {
        for ( int i=0; i<task.getHumans(); i++) {
            allHumans.get(i).running = false;
            allHumans.get(i).actionWakeUp();
        }
    }
    
    synchronized public void startAllThreads() {
        pBuilding.setHumans(allHumans);
        startHumansThread();
        stratLiftThread();
    }
    
    private void startHumansThread() {   
        for (int i=0; i<task.getHumans(); i++) {
            allHumans.get(i).running = true;
            new Thread(hmnsThread, allHumans.get(i), "Human ¹" + i).start();
        }
    }
    
    public void humFactory() {
        Random rnd = new Random();
        int int1, int2;
        
        allHumans = new ArrayList<HumanGraph>();
        hmnsThread = new ThreadGroup("Humans");
        StringBuilder sb;
        for (int i=0; i<task.getHumans(); i++) {
            do {
                int1 = rnd.nextInt(task.getFloors()) + 1;
                int2 = rnd.nextInt(task.getFloors()) + 1;
            } while(int1 == int2);
            allHumans.add(new HumanGraph(i, int1, int2, log));  
            sb = new StringBuilder();
            sb.append("Human ¹").append(i).append(" has been created on ")
              .append(int1).append(" floor, going to ").append(int2)
              .append(" floor");
            log.add(sb.toString());
            pBuilding.setHumans(allHumans);
        }
        int1 = 1;
    }
    
     public void addAllHumansOnPanel() {
         for(HumanGraph hmn:allHumans) {
             pBuilding.add(hmn);
         }
     }
    
    public void cntrlrAction(JButton btn) {
        this.btn = btn;
        startHumansThread();
        stratLiftThread();
    }
    
    public LiftGraph createLift() {
        pBuilding.pLift = new LiftGraph(task, pBuilding.LIFT_WIDTH, 
                                            pBuilding.LIFT_HEIGH);
        return pBuilding.pLift;
    }
    
    @Override
    public void run() {
        double timeout;

        try {
            waitHumans();
        } catch (InterruptedException ex) {
            log.add(ex.toString());
        }
        timeout = System.currentTimeMillis();
        try {
        while ((transported < task.getHumans()) && !Thread.interrupted() 
                    && running) {
            pBuilding.repaint();
            if(pBuilding.pLift.getStatus()) {
                log.add("--------- <Turn " + ++transact + "> ----- Lift on the "
                + pBuilding.pLift.getFloor() + " floor ------------"); 
                // check humans on exit from lift
                for(HumanGraph hmn:allHumans) {
             /*       if(!running)
                        return;*/
                    if((hmn.getStatus()==1) && (hmn.getEndFloor()
                            == pBuilding.pLift.getFloor())) {
                        freeSpace++;
                        transported++;
                        pBuilding.animateExit(hmn);
                        hmn.actionWakeUp();         // wake up human for exit
                    }
                    try {
                        waitHumans();
                    } catch (InterruptedException ex) {
                        log.add(ex.toString());
                    }
                }
                // check humans on enter in lift
                for(HumanGraph hmn:allHumans) {
                    if ((hmn.getStatus() == 0) && hmn.getBegFloor() == 
                            pBuilding.pLift.getFloor() && freeSpace > 0) {
                        freeSpace--;
                        // wake up human for enter
                        pBuilding.animateEnter(hmn);
                        hmn.actionWakeUp();
                    }
                    try {
                        waitHumans();
                    } catch (InterruptedException ex) {
                        log.add(ex.toString());
                    }
                }
                // Wake up lift (set isWaiting flag)
                if(transported < task.getHumans()) {
                    pBuilding.liftAnim();
                }
                pBuilding.pLift.actionWakeUp();
            }
        }
        }catch (Exception e) {
            log.add(e.toString());
        }
        pBuilding.pLift.running = false;
        interruptHumanThreads();
        btn.setText("Start");
        timeout = (System.currentTimeMillis() - timeout) / 1000;
        if(transported==task.getHumans()) {
            log.add("----------- Simulation is finished. Elapsed time: "
                        + timeout + " sec ------------------");
        }
        else {
            log.add("-------------Simulation is terminated. Elapsed time: "
                        + timeout + " sec ------------------");
        }
    }
      
    private void waitHumans() throws InterruptedException {
        for (Human hmn:allHumans) {
            while (!hmn.getWaiting()){
                TimeUnit.NANOSECONDS.sleep(0);
            }
        }
    }    
}
