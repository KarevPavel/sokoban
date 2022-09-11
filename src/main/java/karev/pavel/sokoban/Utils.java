package karev.pavel.sokoban;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static List<Position> pathFinder(Position start, Position destination, Level level, Predicate<Character> moveFilter) {
        Queue<Position> queue = new ArrayDeque<>();
        queue.add(start);

        var cameFrom = new Position[256][256];
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(destination)) {
                break;
            }
            for (Position next : level.possibleMovies(start, moveFilter)) {
                if (Objects.isNull(cameFrom[next.x][next.y])) {
                    queue.add(next);
                    cameFrom[next.x][next.y] = current;
                }
            }
        }

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
}
