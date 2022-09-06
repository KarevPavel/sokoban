package karev.pavel.sokoban;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import karev.pavel.sokoban.Level.Status;

public class Board extends JPanel {

    private final int OFFSET = 30;
    private final int SPACE = 20;
    private final int LEFT_COLLISION = 1;
    private final int RIGHT_COLLISION = 2;
    private final int TOP_COLLISION = 3;
    private final int BOTTOM_COLLISION = 4;

    private Level level;

    private List<Wall> walls = new ArrayList<>();
    private List<Baggage> baggs = new ArrayList<>();
    private List<Area> areas = new ArrayList<>();
    private Player player;

    private boolean isCompleted = false;


    public Board() throws IOException {

        initBoard();
        test();
    }


    public void test() {
        Tree tree = new Tree(level);
        Node rootNode = tree.getRoot();

        while (true) {
            // Phase 1 - Selection
            var promisingNode = selectPromisingNode(rootNode);
            // Phase 2 - Expansion
            expandNode(promisingNode);
            //promisingNode.getState().getLevel().print();
            // Phase 3 - Simulation
            var nodeToExplore = promisingNode;
            if (!promisingNode.getChildArray().isEmpty()) {
                nodeToExplore = promisingNode.getRandomChildNode();
            }
            Status playoutResult = simulateRandomPlayout(nodeToExplore);
            // Phase 4 - Update
            backPropogation(nodeToExplore, playoutResult);

            if (Status.COMPLETED == playoutResult) {

                System.out.println("COMPLETED!!!");
                break;
            }
        }

    }

    private void backPropogation(Node nodeToExplore, Status status) {
        var tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.getState().incrementVisit();
            if (tempNode.getState().hasCompletedBaggages())
                tempNode.getState().addScore(50);
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
        Node node = rootNode;
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

    private void initBoard() throws IOException {
        addKeyListener(new TAdapter());
        setFocusable(true);
        level = Level.loadLevel("/home/yacopsae/IdeaProjects/Java-Sokoban-Game/src/main/resources/levels/level_1.txt");
    }

    private void buildWorld(Graphics g) {

        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        ArrayList<Actor> world = new ArrayList<>();

        world.addAll(walls);
        world.addAll(areas);
        world.addAll(baggs);
        world.add(player);

        for (Actor item : world) {

            g.drawImage(item.getImage(), item.x(), item.y(), this);

            if (isCompleted) {
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

            if (isCompleted) {
                return;
            }

            int key = e.getKeyCode();

            switch (key) {

                case KeyEvent.VK_S:

                    break;
                case KeyEvent.VK_LEFT:

                    if (checkWallCollision(player, LEFT_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(LEFT_COLLISION)) {
                        return;
                    }

                    player.move(-SPACE, 0);

                    break;

                case KeyEvent.VK_RIGHT:

                    if (checkWallCollision(player, RIGHT_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(RIGHT_COLLISION)) {
                        return;
                    }

                    player.move(SPACE, 0);

                    break;

                case KeyEvent.VK_UP:

                    if (checkWallCollision(player, TOP_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(TOP_COLLISION)) {
                        return;
                    }

                    player.move(0, -SPACE);

                    break;

                case KeyEvent.VK_DOWN:

                    if (checkWallCollision(player, BOTTOM_COLLISION)) {
                        return;
                    }

                    if (checkBagCollision(BOTTOM_COLLISION)) {
                        return;
                    }

                    player.move(0, SPACE);

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

    private boolean checkWallCollision(Actor actor, int type) {

        switch (type) {

            case LEFT_COLLISION:

                for (int i = 0; i < walls.size(); i++) {

                    Wall wall = walls.get(i);

                    if (actor.isLeftCollision(wall)) {

                        return true;
                    }
                }

                return false;

            case RIGHT_COLLISION:

                for (int i = 0; i < walls.size(); i++) {

                    Wall wall = walls.get(i);

                    if (actor.isRightCollision(wall)) {
                        return true;
                    }
                }

                return false;

            case TOP_COLLISION:

                for (int i = 0; i < walls.size(); i++) {

                    Wall wall = walls.get(i);

                    if (actor.isTopCollision(wall)) {

                        return true;
                    }
                }

                return false;

            case BOTTOM_COLLISION:

                for (int i = 0; i < walls.size(); i++) {

                    Wall wall = walls.get(i);

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

    private boolean checkBagCollision(int type) {

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

                            if (checkWallCollision(bag, LEFT_COLLISION)) {
                                return true;
                            }
                        }

                        bag.move(-SPACE, 0);
                        isCompleted();
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

                            if (checkWallCollision(bag, RIGHT_COLLISION)) {
                                return true;
                            }
                        }

                        bag.move(SPACE, 0);
                        isCompleted();
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

                            if (checkWallCollision(bag, TOP_COLLISION)) {
                                return true;
                            }
                        }

                        bag.move(0, -SPACE);
                        isCompleted();
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

                            if (checkWallCollision(bag, BOTTOM_COLLISION)) {

                                return true;
                            }
                        }

                        bag.move(0, SPACE);
                        isCompleted();
                    }
                }

                break;

            default:
                break;
        }

        return false;
    }

    public void isCompleted() {

        int nOfBags = baggs.size();
        int finishedBags = 0;

        for (int i = 0; i < nOfBags; i++) {

            Baggage bag = baggs.get(i);

            for (int j = 0; j < nOfBags; j++) {

                Area area = areas.get(j);

                if (bag.x() == area.x() && bag.y() == area.y()) {

                    finishedBags += 1;
                }
            }
        }

        if (finishedBags == nOfBags) {

            isCompleted = true;
            repaint();
        }
    }

    private void restartLevel() {

        if (isCompleted) {
            isCompleted = false;
        }
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

    private List<Position> pathFinder(Position start, Position destination, char[][] level, Predicate<Character> moveFilter) {
        Queue<Position> queue = new ArrayDeque<>();
        queue.add(start);
        var cameFrom = new Position[levelHeight][levelWidth];
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(destination)) {
                break;
            }
            for (Position next : possibleMovies(current, level, moveFilter)) {
                if (Objects.isNull(cameFrom[next.x][next.y])) {
                    queue.add(next);
                    cameFrom[next.x][next.y] = current;
                }
            }
        }
        printPositionArray(cameFrom);

        var path = new LinkedList<Position>();
        Position current = destination;
        while (!current.equals(start)) {
            path.addFirst(current);
            current = cameFrom[current.x][current.y];
            if (Objects.isNull(current)) {
                return Collections.emptyList();
            }
        }

        return path;
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
