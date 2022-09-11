package karev.pavel.sokoban;

import java.awt.Image;
import javax.swing.ImageIcon;

public class Area extends Actor {

    public Area(Area area) {
        super(area.x(), area.y());
        initArea();
    }

    public Area(int x, int y) {
        super(x, y);
        initArea();
    }

    private void initArea() {
        ImageIcon iicon = new ImageIcon(ClassLoader.getSystemResource("area.png"));
        Image image = iicon.getImage();
        setOriginalImage(image);
    }
}
