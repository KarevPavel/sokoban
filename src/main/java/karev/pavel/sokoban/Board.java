package karev.pavel.sokoban;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Stack;
import javax.swing.JComponent;
import javax.swing.JPanel;
import karev.pavel.sokoban.Level.Status;
import karev.pavel.sokoban.Utils.Direction;
import lombok.SneakyThrows;

public class Board extends JPanel {


    private int levelNumber;
    private static final int SPACE = 20;
    private transient Level level;
    private LinkedList<Animation> animations = new LinkedList<>();

    enum Collision {
        LEFT_COLLISION,
        RIGHT_COLLISION,
        TOP_COLLISION,
        BOTTOM_COLLISION
    }

    public Board() throws IOException, URISyntaxException {
        var completedUrl = ClassLoader.getSystemResource("levels/completed.txt");
        List<String> completedLevels = Files.readAllLines(Paths.get(completedUrl.toURI()));
        levelNumber = 1;
        if (!completedLevels.isEmpty()) {
            String[] split = completedLevels.get(0).split(",");
            levelNumber = Integer.getInteger(split[split.length - 1]);
        }
        initBoard();
    }

    public Stack<Position> solveLevel() {
        var tree = new Tree(level);
        var rootNode = tree.getRoot();
        Node winNode;
        while (true) {
            // Phase 1 - Selection
            var promisingNode = selectPromisingNode(rootNode);
            // Phase 2 - Expansion
            expandNode(promisingNode);
            // Phase 3 - Simulation
            var nodeToExplore = promisingNode;
            if (!promisingNode.getChildArray().isEmpty()) {
                nodeToExplore = promisingNode.getBestChildNode();
            }
            Status playoutResult = simulateRandomPlayout(nodeToExplore);
            // Phase 4 - Update
            backPropogation(nodeToExplore, playoutResult);

            if (Status.COMPLETED == playoutResult) {
                System.out.println("COMPLETED!!!");
                winNode = nodeToExplore;
                break;
            }
        }

        Stack<Position> positions = new Stack<>();
        Stack<Level> levels = new Stack<>();
        while (Objects.nonNull(winNode.getParent())) {
            levels.add(winNode.getState().getLevel());
            positions.add(winNode.getState().getLevel().getPlayer().getPosition());
            winNode = winNode.getParent();
        }
        levels.add(winNode.getState().getLevel());
        positions.add(winNode.getState().getLevel().getPlayer().getPosition());

        var number = 1;
        while (!levels.isEmpty()) {
            //System.out.println("№" + number++);
            //var pos = positions.pop();
            //System.out.println("PlayerPosition: [" + pos.x + " : " + pos.y + " ]");
            levels.pop().print();
        }
        return positions;
    }

    private void backPropogation(Node nodeToExplore, Status status) {
        var tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.getState().incrementVisit();
            if (tempNode.getState().hasCompletedBaggages()) {
                tempNode.getState().addScore(50);
            }
            tempNode.getState().setStatus(status);
            tempNode = tempNode.getParent();
        }
    }

    private Status simulateRandomPlayout(Node node) {
        var tempNode = new Node(node);
        var tempState = tempNode.getState();
        var status = tempState.getLevel().checkStatus();

        if (status == Status.STUCKED) {
            tempNode.getParent().getState().setScore(Integer.MIN_VALUE);
        }

        return status;
    }

    private Node selectPromisingNode(Node rootNode) {
        var node = rootNode;
        while (!node.getChildArray().isEmpty()) {
            node = UCT.findBestNodeWithUCT(node);
        }
        return node;
    }

    private void expandNode(Node node) {
        List<State> possibleStates = node.getState().getAllPossibleStates();
        possibleStates.forEach(state -> {
            Node newNode = new Node(state);
            newNode.setParent(node);
            node.getChildArray().add(newNode);
        });
    }

    @SneakyThrows
    private void initBoard() {
        addKeyListener(new TAdapter(this));
        setFocusable(true);
        URL systemResource = ClassLoader.getSystemResource(String.format("levels/level_%s.txt", levelNumber));
        level = Level.loadLevel(systemResource.getPath());
    }

    private boolean animationCompleted = false;
    private boolean animationStated = false;
    private Instant animationStart;
    private int greenColor;

    private void buildWorld(Graphics g) {

        if (level.isCompleted()) {

            if (!animationStated) {
                animationStart = Instant.now();
                greenColor = 0;
                animationStated = true;
            }

            var now = Instant.now();
            Instant timeleft = now.minus(animationStart.toEpochMilli(), ChronoUnit.MILLIS);
            if (timeleft.get(ChronoField.MILLI_OF_SECOND) > 50) {
                greenColor += 10;
                animationStart = now;
            }

            if (greenColor == 250) {
                animationStated = false;
                animationCompleted = true;
            }

            g.clearRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(0, greenColor, 0));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(Color.BLUE);
            g.setFont(new Font(null, 0, 50));
            g.drawString("COMPLETED", getWidth() / 2 - (5 * 25), getHeight() / 2);
            if (!animationCompleted) {
                updateUI();
            }
            return;
        }

        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        ArrayList<Actor> world = new ArrayList<>();
        world.addAll(level.getWalls());
        world.addAll(level.getAreas());
        world.addAll(level.getBaggs());
        world.add(level.getPlayer());

        if (getMouseWheelListeners().length == 0) {
            addMouseWheelListener(new CustomMouseWheelListener(world, this));
        }

        for (Actor item : world) {
            if (Objects.isNull(item.getScaledImage())) {
                g.drawImage(item.getOriginalImage(), item.y() * SPACE, item.x() * SPACE, this);
            } else {
                var scaledImage = item.getScaledImage();
                int height = scaledImage.getHeight(this);
                int weight = scaledImage.getWidth(this);
                g.drawImage(item.getScaledImage(), item.y() * weight, item.x() * height, this);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        buildWorld(g);

        ListIterator<Animation> iterator = animations.listIterator();
        while (iterator.hasNext()) {
            Animation next = iterator.next();
            if (next.isAnimationCompleted()) {
                iterator.remove();
            }
            next.fireUp();
        }
    }


    private class TAdapter extends KeyAdapter {

        private final JComponent jComponent;

        public TAdapter(JComponent jComponent) {
            this.jComponent = jComponent;
        }

        @Override
        public void keyPressed(KeyEvent e) {

            if (level.isCompleted()) {
                return;
            }

            int key = e.getKeyCode();

            switch (key) {

                case KeyEvent.VK_S:
                    Stack<Position> positions = solveLevel();

                    animations.add(new Animation(jComponent, ChronoField.MILLI_OF_DAY, 1000) {

                        @Override
                        public void onFireUp() {
                            var nextPosition = positions.pop();
                            var player = level.getPlayer();
                            var playerPosition = player.getPosition();
                            var direction = Utils.calcDirection(playerPosition, nextPosition);
                            var collision = Utils.directionToCollision(direction);
                            if (Objects.nonNull(collision)) {
                                checkBagCollision(collision);
                            }
                            player.setPosition(nextPosition);
                            jComponent.repaint();
                        }

                        @Override
                        public boolean finishCondition() {
                            return positions.isEmpty();
                        }
                    });

                    break;
                case KeyEvent.VK_LEFT:

                    if (checkWallCollision(level.getPlayer(), Collision.LEFT_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(Collision.LEFT_COLLISION)) {
                        return;
                    }

                    level.getPlayer().moveLeft();

                    break;

                case KeyEvent.VK_RIGHT:

                    if (checkWallCollision(level.getPlayer(), Collision.RIGHT_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(Collision.RIGHT_COLLISION)) {
                        return;
                    }

                    level.getPlayer().moveRight();

                    break;

                case KeyEvent.VK_UP:

                    if (checkWallCollision(level.getPlayer(), Collision.TOP_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(Collision.TOP_COLLISION)) {
                        return;
                    }

                    level.getPlayer().moveUp();

                    break;

                case KeyEvent.VK_DOWN:

                    if (checkWallCollision(level.getPlayer(), Collision.BOTTOM_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(Collision.BOTTOM_COLLISION)) {
                        return;
                    }

                    level.getPlayer().moveDown();

                    break;

                case KeyEvent.VK_R:

                    restartLevel();

                    break;

                default:
                    break;
            }

            repaint();
        }
    }

    private boolean checkWallCollision(Actor actor, Collision type) {

        switch (type) {

            case LEFT_COLLISION:

                for (int i = 0; i < level.getWalls().size(); i++) {

                    Wall wall = level.getWalls().get(i);

                    if (actor.isLeftCollision(wall)) {

                        return true;
                    }
                }

                return false;

            case RIGHT_COLLISION:

                for (int i = 0; i < level.getWalls().size(); i++) {

                    Wall wall = level.getWalls().get(i);

                    if (actor.isRightCollision(wall)) {
                        return true;
                    }
                }

                return false;

            case TOP_COLLISION:

                for (int i = 0; i < level.getWalls().size(); i++) {

                    Wall wall = level.getWalls().get(i);

                    if (actor.isTopCollision(wall)) {

                        return true;
                    }
                }

                return false;

            case BOTTOM_COLLISION:

                for (int i = 0; i < level.getWalls().size(); i++) {

                    Wall wall = level.getWalls().get(i);

                    if (actor.isBottomCollision(wall)) {

                        return true;
                    }
                }

                return false;

            default:
                break;
        }

        return false;
    }

    private boolean checkBagCollision(Collision type) {

        List<Baggage> baggs = level.getBaggs();
        var player = level.getPlayer();

        switch (type) {

            case LEFT_COLLISION:

                for (int i = 0; i < baggs.size(); i++) {

                    Baggage bag = baggs.get(i);

                    if (player.isLeftCollision(bag)) {

                        for (int j = 0; j < baggs.size(); j++) {

                            Baggage item = baggs.get(j);

                            if (!bag.equals(item)) {

                                if (bag.isLeftCollision(item)) {
                                    return true;
                                }
                            }

                            if (checkWallCollision(bag, Collision.LEFT_COLLISION)) {
                                return true;
                            }
                        }

                        bag.moveLeft();
                        level.isCompleted();
                    }
                }

                return false;

            case RIGHT_COLLISION:

                for (int i = 0; i < baggs.size(); i++) {

                    Baggage bag = baggs.get(i);

                    if (player.isRightCollision(bag)) {

                        for (int j = 0; j < baggs.size(); j++) {

                            Baggage item = baggs.get(j);

                            if (!bag.equals(item)) {

                                if (bag.isRightCollision(item)) {
                                    return true;
                                }
                            }

                            if (checkWallCollision(bag, Collision.RIGHT_COLLISION)) {
                                return true;
                            }
                        }

                        bag.moveRight();
                        level.isCompleted();
                    }
                }
                return false;

            case TOP_COLLISION:

                for (int i = 0; i < baggs.size(); i++) {

                    Baggage bag = baggs.get(i);

                    if (player.isTopCollision(bag)) {

                        for (int j = 0; j < baggs.size(); j++) {

                            Baggage item = baggs.get(j);

                            if (!bag.equals(item)) {

                                if (bag.isTopCollision(item)) {
                                    return true;
                                }
                            }

                            if (checkWallCollision(bag, Collision.TOP_COLLISION)) {
                                return true;
                            }
                        }

                        bag.moveUp();
                        level.isCompleted();
                    }
                }

                return false;

            case BOTTOM_COLLISION:

                for (int i = 0; i < baggs.size(); i++) {

                    Baggage bag = baggs.get(i);

                    if (player.isBottomCollision(bag)) {

                        for (int j = 0; j < baggs.size(); j++) {

                            Baggage item = baggs.get(j);

                            if (!bag.equals(item)) {

                                if (bag.isBottomCollision(item)) {
                                    return true;
                                }
                            }

                            if (checkWallCollision(bag, Collision.BOTTOM_COLLISION)) {

                                return true;
                            }
                        }

                        bag.moveDown();
                        level.isCompleted();
                    }
                }

                break;

            default:
                break;
        }

        return false;
    }

    private void restartLevel() {
        initBoard();
        buildWorld(getGraphics());
    }

}
