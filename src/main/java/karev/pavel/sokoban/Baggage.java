package karev.pavel.sokoban;

import javax.swing.ImageIcon;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Baggage extends Actor {

    public Baggage(Baggage baggage) {
        super(baggage.x(), baggage.y());
        initBaggage();
    }

    public Baggage(int x, int y) {
        super(x, y);
        initBaggage();
    }

    private void initBaggage() {
        
        var iicon = new ImageIcon(ClassLoader.getSystemResource("baggage.png"));
        var image = iicon.getImage();
        setOriginalImage(image);
    }
}
