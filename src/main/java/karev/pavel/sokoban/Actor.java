package karev.pavel.sokoban;

import java.awt.Image;
import lombok.Data;

@Data
public class Actor {

    private final int SPACE = 20;

    private Position position;
    private Image originalImage;
    private Image scaledImage;

    public Actor(int x, int y) {
        this.position = new Position(x, y);
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

    private void move(int x, int y) {

        int dx = x() + y;
        int dy = y() + x;

        setX(dx);
        setY(dy);
    }

    public void moveLeft() {
        move(-1, 0);
    }

    public void moveRight() {
        move(1, 0);
    }

    public void moveDown() {
        move(0, 1);
    }

    public void moveUp() {
        move(0, -1);
    }

    public boolean isLeftCollision(Actor actor) {
        return y() - 1 == actor.y() && x() == actor.x();
    }

    public boolean isRightCollision(Actor actor) {
        return y() + 1 == actor.y() && x() == actor.x();
    }

    public boolean isTopCollision(Actor actor) {
        return x() - 1 == actor.x() && y() == actor.y();
    }

    public boolean isBottomCollision(Actor actor) {
        return x() + 1 == actor.x() && y() == actor.y();
    }
}
