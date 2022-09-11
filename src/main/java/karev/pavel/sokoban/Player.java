package karev.pavel.sokoban;

import javax.swing.ImageIcon;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player extends Actor {

    public Player(Player player) {
        super(player.x(), player.y());
        initPlayer();
    }

    public Player(int x, int y) {
        super(x, y);
        initPlayer();
    }

    private void initPlayer() {
        var icon = new ImageIcon(ClassLoader.getSystemResource("sokoban.png"));
        var image = icon.getImage();
        setOriginalImage(image);
    }
}
