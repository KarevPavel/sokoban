package karev.pavel.sokoban;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JFrame;

public class Sokoban extends JFrame {

    public Sokoban() throws IOException, URISyntaxException {
        initUI();
    }

    private void initUI() throws IOException, URISyntaxException {
        
        Board board = new Board();
        add(board);

        setTitle("Sokoban");
        
        setSize(800, 600);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        
        EventQueue.invokeLater(() -> {

            Sokoban game = null;
            try {
                game = new Sokoban();
                game.setVisible(true);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }
}
