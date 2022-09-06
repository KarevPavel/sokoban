package karev.pavel.sokoban;

import java.awt.Image;
import lombok.Getter;
import lombok.Setter;

public class Actor {

    private final int SPACE = 20;

    @Getter
    @Setter
    private Position position;
    private Image image;

    public Actor(int x, int y) {
        this.position = new Position(x, y);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image img) {
        image = img;
    }

    public int x() {
        return position.getX();
    }

    public int y() {
        return position.getY();
    }

    public void setX(int x) {
        this.position.x = x;
    }

    public void setY(int y) {
        this.position.y = y;
    }



    public boolean isLeftCollision(Actor actor) {
        
        return x() - SPACE == actor.x() && y() == actor.y();
    }

    public boolean isRightCollision(Actor actor) {
        
        return x() + SPACE == actor.x() && y() == actor.y();
    }

    public boolean isTopCollision(Actor actor) {
        
        return y() - SPACE == actor.y() && x() == actor.x();
    }

    public boolean isBottomCollision(Actor actor) {
        
        return y() + SPACE == actor.y() && x() == actor.x();
    }
}
