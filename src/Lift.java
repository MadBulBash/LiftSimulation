import javax.swing.JPanel;

public class Lift extends JPanel implements Runnable {
    private int maxFloor;               // max floors in building
    private int floor;                  // current floor
    private boolean isWaiting;          // flag of waiting
    private boolean isMoveDown;         // move direction
    public boolean running;             // flag of moving
    
    public Lift(Task task) {
        this.maxFloor = task.getFloors();
        this.floor = 1;
    }

    public void setWaiting(boolean vol) {
        isWaiting = vol;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public boolean getDirrection() {
        return isMoveDown;
    }
    
    public boolean getStatus() {
        return isWaiting;
    }

    public synchronized void actionWakeUp() {
        isWaiting = false;
        notify();
    }
        
    private synchronized void actionWait() throws InterruptedException {
        while (isWaiting & running) {
            wait();
        }
    }

    public void nextFloor() {
        if (!isMoveDown) {
            if (floor < maxFloor) {
                floor++;
            }
            else {
                isMoveDown = true;
                floor--;
            }
        }
        else {
            if (floor > 1) {
                floor--;
            }
            else {
                isMoveDown = false;
                floor++;
            }
        }
    }        
        
    @Override
    public void run() {
        isWaiting = true;
        try {
            while (running) {
                actionWait();
                if(!running) {
                    return;
                }
                nextFloor();
                isWaiting = true;
            }
        } 
        catch (Exception e) {
            return;
        }
    }
}
