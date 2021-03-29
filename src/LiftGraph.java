import java.awt.Graphics;

public class LiftGraph extends Lift {
    public int[] humInLift;
    
    public LiftGraph(Task task, int width, int height) {
        super(task);
        humInLift = new int[task.getCapacity()];
        // cleaning humans on lift
        for (int i=0; i < humInLift.length; i++) {
           humInLift[i] = -1;
        }
        setSize(width-2, height);
    }
    
    // set place human in lift
    public int getPlace(int num) {
        for (int i=0; i<humInLift.length; i++) {
            if (humInLift[i] == -1) {
                humInLift[i] = num;
                return i;
            }
        }
        return 0;
    }
    
    // release position in lift for other humans
    public void releasePlace(int num) {
        for (int i=0; i < humInLift.length; i++) {
            if (humInLift[i] == num) {
                humInLift[i] = -1;
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.fillRect(0, 0, this.getWidth()-1, this.getHeight());
    }    
}
