package karev.pavel.sokoban;

import java.awt.Image;
import javax.swing.ImageIcon;

public class Area extends Actor {

    public Area(Area area) {
        super(area.x(), area.y());
        position = new Position(area.x(), area.y());
        initArea();
    }

    public Area(int x, int y) {
        super(x, y);
        position = new Position(x, y);
        initArea();
    }

    private Position position;

    private void initArea() {

        ImageIcon iicon = new ImageIcon("src/resources/area.png");
        Image image = iicon.getImage();
        setImage(image);
    }

    public Position getPosition() {
        return position;
    }
}
