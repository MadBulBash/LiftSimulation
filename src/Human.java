import javax.swing.JPanel;

public class Human  extends JPanel implements Runnable {
    private int idNum;
    private int begFloor;
    private int endFloor;
    private int status;
    private boolean isWaiting;
    private LogAppender log;
    public boolean running;
    
    public Human(int num, int bFloor, int eFloor, LogAppender logT) {
        idNum = num;
        begFloor = bFloor;
        endFloor = eFloor;
        log = logT;
        running = true;
    }
    
    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        
        isWaiting = true;
        try {
            // if status was restored then check status
            if (status == 0) {
                actionWaiting();
                if(!running) {
                    return;
                }
                sb.append("Human ¹").append(getId())
                  .append("  has entered into the lift");
                log.add(sb.toString());
                actionEnter();
            }
            setWaiting(true);
            actionWaiting();
            if (!running) {        // if needed to exit thread
                return;
            }
            sb = new StringBuilder();
            sb.append("Human ¹").append(getId()).append(" left the lift");
            log.add(sb.toString());
            actionEnter();
            setWaiting(true);
        }
        catch (Exception e) {
            log.add(e.getMessage());
        }
    }
    
    public int getId() {
        return idNum;
    }
    
    public int getStatus() {
        return status;
    }
    
    public int getBegFloor() {
        return begFloor;
    }
    
    public int getEndFloor() {
        return endFloor;
    }
    
    
    public void actionEnter() {
        status++;
    }
    
    private synchronized void actionWaiting() throws InterruptedException {
        while (getWaiting() && running) {
            wait();
        }
    } 
    
    public synchronized void actionWakeUp() {
        isWaiting = false;
        notify();
    }
    
    public void setWaiting(boolean vol) {
        isWaiting = vol;
    }
    
    public boolean getWaiting() {
        return isWaiting;
    }
}
