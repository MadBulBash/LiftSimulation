import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Building extends JPanel {
    public static final int LIFT_WIDTH = 200;
    public static final int LIFT_HEIGH = 8;
    private static final int LIFT_DELAY = 2;
    private static final int HUMAN_DELAY = 2;
    
    public BufferedImage gBuf;
    public LiftGraph pLift;
    public List<HumanGraph> allHumans;
    
    private LogAppender log;
    private int panHeight;                      // main panel height
    private int panWidth;                       // main panel width
    private int cellWidth;                      // floor width from each side
    private int floorHeight;                    // floor height for each floor
    private int[] CurStay;                      // array of waiting humans
    private int[] CurTrans;                     // array of transported humans
    private Task task;
    private Graphics graph;

    Building(Task task, LogAppender log) {
        this.task = task;
        this.log = log;
        // load human picture
        try {
            ZipFile zfl = new ZipFile(System.getProperty("user.dir")
                                        + "/LiftSimulation.jar");
            ZipEntry humImage = zfl.getEntry("Human.gif");
            gBuf = ImageIO.read(zfl.getInputStream(humImage));  
            // init BufferedImage for human objects 
            HumanGraph.gBuf = gBuf;
          }
        catch (Exception exp) {
            log.add(exp.toString());
        }
        CurStay = new int[task.getFloors()];
        CurTrans= new int[task.getFloors()];
        setHumanFonts();
    }   
    
    public void setHumans(List<HumanGraph> hmns) {
        allHumans = hmns;
    }

    public final void setHumanFonts() {
        AffineTransform fontAT = (new AffineTransform());
        fontAT.rotate(Math.toRadians(20)); 
        Font fx = new Font("terminal", Font.PLAIN, 13).deriveFont(fontAT); 
        Font stndrt = new Font("serif", Font.BOLD , 14);  
        HumanGraph.setFonts(fx, stndrt);
    }
    
    // set new task
    public void setTask(Task task) {
        this.task = task;
    }
    
    // overload new task
    public void setTask(int delay) {
        this.task.setDelay(delay);
    }

    // add lift panel on building panel
    public void addLift() {
        add(pLift,null);
    }
    
    /// create new lift
    public LiftGraph createLift() {
        pLift = new LiftGraph(task, LIFT_WIDTH, LIFT_HEIGH);
        return pLift;        
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        graph = g;
        super.paintComponent(g);
        drawFloors();
    }
    
    public void initGraphObjPosition() {
        panWidth = getWidth();
        panHeight = getHeight();
        cellWidth =  (panWidth - LIFT_WIDTH) / 2;
        floorHeight = panHeight / task.getFloors(); 
        pLift.setLocation(cellWidth + 2, (task.getFloors()- 
                            pLift.getFloor() + 1) * floorHeight - LIFT_HEIGH);
    }
    
    void drawFloors() {
        for (int i=1; i<=task.getFloors(); i++) {
            graph.drawRect(0, i * floorHeight - LIFT_HEIGH, cellWidth, 
                                LIFT_HEIGH);
            graph.drawRect(cellWidth + LIFT_WIDTH, i * floorHeight - LIFT_HEIGH,
                            panWidth, LIFT_HEIGH);
            graph.drawString("Floor " + Integer.toString((task.getFloors() - i 
                                + 1)), 5 , (i - 1) * floorHeight + 15) ;
        }
    }
      
    void scanHumStartPositions() {
        for (int i=0; i<allHumans.size(); i++) {
            CurStay[allHumans.get(i).getBegFloor() - 1]++;
            allHumans.get(i).setLocation(cellWidth - 
                (gBuf.getWidth() * CurStay[allHumans.get(i).getBegFloor() - 1]),
                    floorHeight * (task.getFloors() - 
                                    allHumans.get(i).getBegFloor() + 1) 
                        - LIFT_HEIGH - gBuf.getHeight());
        }
    }

    public void liftAnim() {
        int pos;
        boolean flagMoveUp = false;
        
        if ((!pLift.getDirrection() && (pLift.getFloor() < task.getFloors())) ||
            (pLift.getDirrection() && (pLift.getFloor() == 1))) {
                flagMoveUp = true;
        }
        for (int i = 1; i <= floorHeight; i++) {
            if (!flagMoveUp) {
                pos = pLift.getY() + 1;
            }
            else {
                pos = pLift.getY() - 1;
            }
            
            // move lift
            pLift.setLocation(pLift.getX(), pos);
            
            // move humans
            for (int j=0; j < task.getCapacity(); j++) {
                if (pLift.humInLift[j] >= 0) {    // if human stay at position j
                    HumanGraph h = allHumans.get(pLift.humInLift[j]);
                    pos = h.getY();
                    if (flagMoveUp) {
                        pos--;
                    }
                    else {
                        pos++;
                    }
                    h.setLocation(h.getX(), pos);
                }
            }
            repaint();
            pause(LIFT_DELAY * task.getDelay());
         }
     }

    public void animateExit(HumanGraph hmn) {
        int xPos = cellWidth + LIFT_WIDTH + (CurTrans[hmn.getEndFloor() - 1])
                                                * gBuf.getWidth();
        
        pLift.releasePlace(hmn.getId());      // release place in lift    
        // until human not rich his floor and position not out of screen
        for (int i = hmn.getX(); (i <= xPos) && (i < getWidth()); i++) {
            hmn.setLocation(i, hmn.getY());
            repaint();
            pause(HUMAN_DELAY * task.getDelay()); 
        }
        CurTrans[hmn.getEndFloor() - 1]++;
    }

    private void strafeHumans2Lift(HumanGraph curHum) {
        for (HumanGraph hmn:allHumans) {
            if ((hmn.getStatus() == 0) && (hmn.getBegFloor() == pLift.getFloor())
                                        && (curHum != hmn)) {
                hmn.setLocation(hmn.getX() + gBuf.getWidth(), hmn.getY());
            }
        }
    }
    
    public void animateEnter(HumanGraph hmn) {
        int xPos = cellWidth + 1 + LIFT_WIDTH / task.getCapacity()
                                * pLift.getPlace(hmn.getId());
        
        for (int i = hmn.getX(); i <= xPos; i++) {
            hmn.setLocation(i, hmn.getY());
            repaint();
            pause(HUMAN_DELAY * task.getDelay());
        }
        strafeHumans2Lift(hmn);
    }
    
    private void pause(int delay) {
        try {
            TimeUnit.MICROSECONDS.sleep(delay);
        } catch (InterruptedException ex) {
            log.add(ex.toString());
        }
    }
}
