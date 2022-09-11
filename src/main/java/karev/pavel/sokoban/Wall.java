package karev.pavel.sokoban;

import java.awt.Image;
import javax.swing.ImageIcon;
import lombok.Data;

public class Wall extends Actor {

    private Image image;

    public Wall(Wall wall) {
        super(wall.x(), wall.y());
        initWall();
    }

    public Wall(int x, int y) {
        super(x, y);
        initWall();
    }

    private void initWall() {
        var icon = new ImageIcon(ClassLoader.getSystemResource("wall.png"));
        image = icon.getImage();
        setImage(image);
    }
}
