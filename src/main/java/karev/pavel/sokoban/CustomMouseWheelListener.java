package karev.pavel.sokoban;

import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import javax.swing.JComponent;

public class CustomMouseWheelListener implements java.awt.event.MouseWheelListener {

    private final List<Actor> world;
    private final JComponent component;
    private double zoom;

    public CustomMouseWheelListener(List<Actor> world, JComponent component) {
        this.world = world;
        this.component = component;
        this.zoom = 1;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        mouseWheelEvent.consume();
        if (mouseWheelEvent.isControlDown()) {
            if (mouseWheelEvent.getWheelRotation() < 0) {
                zoom += 0.5;
            } else {
                zoom -= 0.5;
            }

            if (zoom < 1) {
                zoom = 1;
            }

            world.forEach(actor -> {
                var image = actor.getOriginalImage();
                int newWidth = (int) (image.getWidth(component) * zoom);
                int newHeight = (int) (image.getHeight(component) * zoom);
                actor.setScaledImage(image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT));
            });
            component.repaint();
        }
    }
}
