package karev.pavel.sokoban;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;
import karev.pavel.sokoban.Level.Status;

public class Board extends JPanel {


    private static final int SPACE = 20;

    enum Collision {
        LEFT_COLLISION,
        RIGHT_COLLISION,
        TOP_COLLISION,
        BOTTOM_COLLISION
    }

    private Level level;

    public Board() throws IOException, URISyntaxException {
        var completedUrl = ClassLoader.getSystemResource("levels/completed.txt");
        List<String> completedLevels = Files.readAllLines(Paths.get(completedUrl.toURI()));
        var lastLevel = "1";
        if (!completedLevels.isEmpty()) {
            String[] split = completedLevels.get(0).split(",");
            lastLevel = split[split.length - 1];
        }
        initBoard(lastLevel);
    }

    public void solveLevel() {
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

        Deque<Level> levels = new ArrayDeque<>();
        while (Objects.nonNull(winNode.getParent())) {
            levels.add(winNode.getState().getLevel());
            winNode = winNode.getParent();
        }

        int number = 1;
        while (!levels.isEmpty()) {
            System.out.println("№" + number++);
            levels.pop().print();
        }
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

    private void initBoard(String levelNumber) throws IOException {
        addKeyListener(new TAdapter());
        setFocusable(true);
        URL systemResource = ClassLoader.getSystemResource(String.format("levels/level_%s.txt", levelNumber));
        level = Level.loadLevel(systemResource.getPath());
    }

    private void buildWorld(Graphics g) {

        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        ArrayList<Actor> world = new ArrayList<>();

        world.addAll(level.getWalls());
        world.addAll(level.getAreas());
        world.addAll(level.getBaggs());
        world.add(level.getPlayer());

        for (Actor item : world) {

            g.drawImage(item.getImage(), item.x() * SPACE, item.y() * SPACE, this);

            if (level.isCompleted()) {
                g.setColor(new Color(0, 0, 0));
                g.drawString("Completed", 25, 20);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        buildWorld(g);
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            if (level.isCompleted()) {
                return;
            }

            int key = e.getKeyCode();

            switch (key) {

                case KeyEvent.VK_S:
                    solveLevel();
                    break;
                case KeyEvent.VK_LEFT:

                    if (checkWallCollision(level.getPlayer(), Collision.LEFT_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(Collision.LEFT_COLLISION)) {
                        return;
                    }

                    level.getPlayer().move(-SPACE, 0);

                    break;

                case KeyEvent.VK_RIGHT:

                    if (checkWallCollision(level.getPlayer(), Collision.RIGHT_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(Collision.RIGHT_COLLISION)) {
                        return;
                    }

                    level.getPlayer().move(SPACE, 0);

                    break;

                case KeyEvent.VK_UP:

                    if (checkWallCollision(level.getPlayer(), Collision.TOP_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(Collision.TOP_COLLISION)) {
                        return;
                    }

                    level.getPlayer().move(0, -SPACE);

                    break;

                case KeyEvent.VK_DOWN:

                    if (checkWallCollision(level.getPlayer(), Collision.BOTTOM_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(Collision.BOTTOM_COLLISION)) {
                        return;
                    }

                    level.getPlayer().move(0, SPACE);

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

                        bag.move(-SPACE, 0);
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

                        bag.move(SPACE, 0);
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

                        bag.move(0, -SPACE);
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

                        bag.move(0, SPACE);
                        level.isCompleted();
                    }
                }

                break;

            default:
                break;
        }

        return false;
    }
/*
    public void isCompleted() {

        int nOfBags = level.getBaggs().size();
        int finishedBags = 0;

        for (int i = 0; i < nOfBags; i++) {

            Baggage bag = level.getBaggs().get(i);

            for (int j = 0; j < nOfBags; j++) {

                Area area = level.getAreas().get(j);

                if (bag.x() == area.x() && bag.y() == area.y()) {

                    finishedBags += 1;
                }
            }
        }

        if (finishedBags == nOfBags) {

            isCompleted = true;
            repaint();
        }
    }*/

    private void restartLevel() {
/*
        if (isCompleted) {
            isCompleted = false;
        }*/
    }


    /*
    private void printPositionArray(Position[][] arr) {
        for (var x = 0; x < arr.length; x++) {
            for (var y = 0; y < arr[x].length; y++) {
                System.out.print(Objects.isNull(arr[x][y]) ? "# " : "V ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }


    Map<Baggage, List<Position>> bagsMoves() {
        Map<Baggage, List<Position>> answer = new HashMap<>();
        for (Baggage baggage : baggs) {
            //До каждого финиша
            for (Area area : areas) {
                //Строим путь
                var path = pathFinder(baggage.getPosition(), area.getPosition(), level, character -> character != '#');
                if (!path.isEmpty()) {
                    answer.put(baggage, path);
                }
            }
        }
        return answer;
    }

    Map<Baggage, List<Position>> heroMoves(Map<Baggage, List<Position>> bagsList, char[][] level) {
        return bagsList
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, entry -> {
                var bags = entry.getValue();
                var currentBagBosition = entry.getKey().getPosition();
                var path = new LinkedList<>(pathFinder(player.getPosition(), currentBagBosition, level, character -> character != '#' && character != '$'));
                if (path.size() == 1 && currentBagBosition.equals(path.get(0))) {
                    path.clear();
                }

                var playerPosition = player.getPosition();
                for (Position nextBagPos : bags) {
                    List<Position> heroMoves = heroMoves(playerPosition, currentBagBosition, nextBagPos, level);
                    if (!heroMoves.isEmpty()) {
                        path.addAll(heroMoves);
                        playerPosition = heroMoves.get(heroMoves.size() - 1);
                        currentBagBosition = nextBagPos;
                    }
                }
                return path;
            }));
    }

    private List<Position> heroMoves(Position playerPosition, Position bagStart, Position bagEnd, char[][] level) {
        int newX, newY;
        var additionalMove = new Position(bagStart.x, bagStart.y);
        if (bagStart.x > bagEnd.x) {
            newX = bagStart.x + 1;
        } else if (bagStart.x < bagEnd.x) {
            newX = bagStart.x - 1;
        } else {
            newX = bagStart.x;
        }

        if (bagStart.y > bagEnd.y) {
            newY = bagStart.y + 1;
        } else if (bagStart.y < bagEnd.y) {
            newY = bagStart.y - 1;
        } else {
            newY = bagStart.y;
        }

        if (newY < 0 || newX < 0 || newY > 127 || newX > 127) {
            return Collections.emptyList();
        }

        var playerEndPosition = new Position(newX, newY);
        var path = pathFinder(playerPosition, playerEndPosition, level, character -> character != '#' && character != '$');
        path.add(additionalMove);
        return path;
    }


    Node resolve() {
        boolean resolved = false;
        var bagMoves = bagsMoves();

        Map<Baggage, List<Position>> baggageListMap = heroMoves(bagMoves, level);

        var root = new Node();
        root.level = level;
        printArray(level);
        baggageListMap.forEach((baggage, heroPositions) -> {
            var baggageNewNode = new Node();
            root.child.add(baggageNewNode);
            baggageNewNode.parent = root;
            baggageNewNode.level = level;
            var prevPosition = new Position(player.x(), player.y());
            var prevLevelState = level;
            for (Position position : heroPositions) {
                var newState = arrayCopy(prevLevelState);
                makePlayerMove(newState, prevPosition, position);
                printArray(newState);
                var child = new Node();
                child.parent = baggageNewNode;
                child.level = newState;
                prevLevelState = newState;
                prevPosition = position;
                baggageNewNode.child.add(child);
                baggageNewNode = child;
            }
        });

        return null;
    }

    private void makePlayerMove(char[][] level, Position currentPosition, Position newPosition) {
        if (level[newPosition.x][newPosition.y] == '#') {
            return;
        }

        if (level[newPosition.x][newPosition.y] == '$') {
            if (currentPosition.x < newPosition.x) { //DOWN
                if (level[newPosition.x + 1][newPosition.y] != '#') {
                    level[newPosition.x + 1][newPosition.y] = '$';
                }
            } else if (currentPosition.x > newPosition.x) { //UP
                if (level[newPosition.x - 1][newPosition.y] != '#') {
                    level[newPosition.x - 1][newPosition.y] = '$';
                }
            } else if (currentPosition.y > newPosition.y) { //LEFT
                if (level[newPosition.x][newPosition.y - 1] != '#') {
                    level[newPosition.x][newPosition.y - 1] = '$';
                }
            } else if (currentPosition.y < newPosition.y) { //RIGHT
                if (level[newPosition.x][newPosition.y + 1] != '#') {
                    level[newPosition.x][newPosition.y + 1] = '$';
                }
            }
        }
        level[currentPosition.x][currentPosition.y] = ' ';
        level[newPosition.x][newPosition.y] = '@';
    }

    private char[][] arrayCopy(char[][] source) {
        var copy = new char[source.length][];
        for (var i = 0; i < source.length; i++) {
            copy[i] = new char[source[i].length];
            System.arraycopy(source[i], 0, copy[i], 0, source[i].length);
        }
        return copy;
    }

    class Node {

        Node parent;
        List<Node> child = new ArrayList<>();
        int moves;
        Baggage baggage;
        char[][] level = new char[127][127];

    }
*/
}
