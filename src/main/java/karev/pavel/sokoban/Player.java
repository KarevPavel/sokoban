package karev.pavel.sokoban;

import java.awt.Image;
import javax.swing.ImageIcon;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player extends Actor {

    public Player(Player player) {
        super(player.x(), player.y());
        this.position = new Position(player.getPosition());
        initPlayer();
    }

    public Player(int x, int y) {
        super(x, y);
        position = new Position(x, y);
        initPlayer();
    }

    private Position position;

    private void initPlayer() {

        ImageIcon iicon = new ImageIcon("src/resources/sokoban.png");
        Image image = iicon.getImage();
        setImage(image);
    }

    public void move(int x, int y) {

        int dx = x() + x;
        int dy = y() + y;
        
        setX(dx);
        setY(dy);
    }

    public Position getPosition() {
        return position;
    }
}
