package karev.pavel.sokoban;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class Level {

    Level(Level level) {
        this.walls = listDeepCopy(level.walls, Wall.class);
        this.baggs = listDeepCopy(level.baggs, Baggage.class);
        this.areas = listDeepCopy(level.areas, Area.class);
        this.player = new Player(level.player);
        this.map = arrayCopy(level.map);
        this.moves = level.moves;
        this.levelHeight = level.levelHeight;
        this.levelWidth = level.levelWidth;
    }

    private List<Position> baggagePosition(char[][] level) {
        List<Position> answer = new ArrayList<>();
        for (var x = 0; x < level.length; x++) {
            for (var y = 0; y < level[x].length; y++) {
                if (level[x][y] == '$') {
                    answer.add(new Position(x, y));
                }
            }
        }
        return answer;
    }

    private <T> List<T> listDeepCopy(List<T> source, Class<T> clazz) {
        return
            source
            .stream()
            .map(t -> quietConstructor(clazz, t))
            .collect(Collectors.toList());
    }

    @SneakyThrows
    private <T> T quietConstructor(Class<T> clazz, T source) {
        return clazz.getConstructor(clazz).newInstance(source);
    }

    private static char[][] arrayCopy(char[][] source) {
        var copy = new char[source.length][];
        for (var i = 0; i < source.length; i++) {
            copy[i] = new char[source[i].length];
            System.arraycopy(source[i], 0, copy[i], 0, source[i].length);
        }
        return copy;
    }

    private Level(List<Wall> walls,
                 List<Baggage> baggs,
                 List<Area> areas,
                 Player player,
                 char[][] map,
                 int levelHeight,
                 int levelWidth) {
        this.walls = walls;
        this.baggs = baggs;
        this.areas = areas;
        this.player = player;
        this.map = map;
        this.levelHeight = levelHeight;
        this.levelWidth = levelWidth;
        this.moves = 0;
    }

    private final List<Wall> walls;
    private final List<Baggage> baggs;
    private final List<Area> areas;
    private final Player player;
    private final char[][] map;
    private final int levelHeight;
    private final int levelWidth;
    private final int moves;

    public static Level loadLevel(String path) throws IOException {
        List<String> strings = Files.readAllLines(Paths.get(path));
        int levelWidth = strings.stream().max(Comparator.comparingInt(String::length))
            .orElse("")
            .length();
        int levelHeight = strings.size();
        char[][] level = new char[levelHeight][levelWidth];

        List<Wall> walls = new ArrayList<>();
        List<Baggage> baggs = new ArrayList<>();
        List<Area> areas = new ArrayList<>();
        Player player = null;

        for (var x = 0; x < strings.size(); x++) {
            for (var y = 0; y < strings.get(x).length(); y++) {
                level[x][y] = strings.get(x).charAt(y);

                if (level[x][y] == '$') {
                    baggs.add(new Baggage(x, y));
                }
                if (level[x][y] == '#') {
                    walls.add(new Wall(x, y));
                }
                if (level[x][y] == 'X') {
                    areas.add(new Area(x, y));
                }
                if (level[x][y] == '@') {
                    player = new Player(x, y);
                }
            }
        }
        return new Level(
            walls,
            baggs,
            areas,
            player,
            level,
            levelHeight,
            levelWidth);
    }

    protected Collection<Position> possibleMovies(Position position, Predicate<Character> movePredicate) {
        Collection<Position> answer = new ArrayList<>();
        if (position.x + 1 < levelHeight && movePredicate.test(map[position.x + 1][position.y])) {
            answer.add(new Position(position.x + 1, position.y));
        }

        if (position.x - 1 > 0 && movePredicate.test(map[position.x + 1][position.y])) {
            answer.add(new Position(position.x - 1, position.y));
        }

        if (position.y - 1 > 0 && movePredicate.test(map[position.x][position.y - 1])) {
            answer.add(new Position(position.x, position.y - 1));
        }

        if (position.y + 1 < levelWidth && movePredicate.test(map[position.x][position.y + 1])) {
            answer.add(new Position(position.x, position.y + 1));
        }
        return answer;
    }

    public Collection<Position> availableMoves() {
        return possibleMovies(player.getPosition(), character -> character != '#');
    }

    @SneakyThrows
    public void print() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.print("  ");
        for (var x = 0; x < map[0].length; x++) {
            System.out.print(x % 10 + "|");
        }
        System.out.println();

        for (var x = 0; x < map.length; x++) {
            System.out.print(x + "|");
            for (var y = 0; y < map[x].length; y++) {
                System.out.print("\033[4m" + map[x][y] + "|" + "\033[0m");
            }
            System.out.println();
        }

        System.out.println();
        System.out.println();
    }

    public void performMove(Position newPosition) {
        if (map[newPosition.x][newPosition.y] == '#') {
            return;
        }

        var currentPlayerPosition = player.getPosition();
        if (map[newPosition.x][newPosition.y] == '$') {
            Baggage baggage = getBaggs()
                .stream()
                .filter(b -> b.getPosition().equals(newPosition))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Baggage cannot be null"));


            Position baggNewPosition = null;
            if (currentPlayerPosition.x < newPosition.x) { //DOWN
                if (map[newPosition.x + 1][newPosition.y] != '#') {
                    baggNewPosition = new Position(newPosition.x + 1, newPosition.y);
                }
            } else if (currentPlayerPosition.x > newPosition.x) { //UP
                if (map[newPosition.x - 1][newPosition.y] != '#') {
                    baggNewPosition = new Position(newPosition.x - 1, newPosition.y);
                }
            } else if (currentPlayerPosition.y > newPosition.y) { //LEFT
                if (map[newPosition.x][newPosition.y - 1] != '#') {
                    baggNewPosition = new Position(newPosition.x, newPosition.y - 1);
                }
            } else if (currentPlayerPosition.y < newPosition.y) { //RIGHT
                if (map[newPosition.x][newPosition.y + 1] != '#') {
                    baggNewPosition = new Position(newPosition.x, newPosition.y + 1);
                }
            }

            if (Objects.isNull(baggNewPosition)) {
                return;
            }

            map[baggNewPosition.x][baggNewPosition.y] = '$';
            baggage.setPosition(new Position(baggNewPosition));
        }
        map[currentPlayerPosition.x][currentPlayerPosition.y] = ' ';
        map[newPosition.x][newPosition.y] = '@';
        player.setPosition(newPosition);
        //print();
    }

    enum Status {
        STUCKED,
        CONTINUE,
        COMPLETED
    }

    public boolean anyCompleted() {
        return getBaggs()
            .stream()
            .anyMatch(baggage -> getAreas()
                .stream()
                .anyMatch(area -> area.getPosition().equals(baggage.getPosition())));
    }

    public boolean isCompleted() {
        return getBaggs()
            .stream()
            .allMatch(baggage -> getAreas()
                .stream()
                .anyMatch(area -> area.getPosition().equals(baggage.getPosition())));
    }

    public Status checkStatus() {

        if (isCompleted())
            return Status.COMPLETED;

        boolean allBoxesAreStucked = baggs
            .stream()
            .allMatch(baggage -> possibleMovies(baggage.getPosition(), character -> character != '#').isEmpty());

        if (allBoxesAreStucked)
            return Status.STUCKED;

        if (possibleMovies(player.getPosition(), character -> character != '#').isEmpty())
            return Status.STUCKED;


        return Status.CONTINUE;
    }
}
