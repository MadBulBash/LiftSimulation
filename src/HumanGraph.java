import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class HumanGraph extends Human {
    public static BufferedImage gBuf;   // one graphics buffer for all humans
    private static Font fFx;                    // font for destination floor
    private static Font fNormal;                // font for id¹
    
    public static void setFonts(Font f1, Font f2) {
        fFx = f1;
        fNormal = f2;
    }
    
    public HumanGraph(int num, int bFloor, int eFloor, LogAppender logT) {
        super(num, bFloor,  eFloor, logT);
        setSize(gBuf.getWidth(), gBuf.getHeight());
        setBackground(Color.white);
    }    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //.getScaledInstance(20, 40, 1)
        g.drawImage(gBuf, 0, 0, null);
        g.setFont(fFx);
        g.drawString(Integer.toString(getEndFloor()), 18 ,17);
        g.setFont(fNormal);
        g.drawString(Integer.toString(getId()), 20, 70);
    }
}
