package karev.pavel.sokoban;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "parent")
public class Node {

    State state;
    Node parent;
    List<Node> childArray;

    public Node() {
        this.state = new State();
        childArray = new ArrayList<>();
    }

    public Node(State state) {
        this.state = state;
        childArray = new ArrayList<>();
    }

    public Node(State state, Node parent, List<Node> childArray) {
        this.state = state;
        this.parent = parent;
        this.childArray = childArray;
    }

    public Node(Node node) {
        this.childArray = new ArrayList<>();
        this.state = new State(node.getState());
        if (node.getParent() != null)
            this.parent = node.getParent();

        List<Node> childArray = node.getChildArray();
        for (Node child : childArray) {
            this.childArray.add(new Node(child));
        }
    }

    public Node getBestChildNode() {
        var entry = getChildArray()
            .stream()
            .map(n -> {
                var level = n.getState().getLevel();
                var playerPosition = level.getPlayer().getPosition();
                var filteredBags = level.getBaggs()
                    .stream()
                    //Ignore already completed baggages
                    .filter(baggage -> level.getAreas().stream()
                        .noneMatch(area -> area.getPosition().equals(baggage.getPosition())))
                    .collect(Collectors.toList());

                if (filteredBags.size() < 1) {
                    return new SimpleEntry<>(n, Integer.MIN_VALUE);
                }

                return new SimpleEntry<>(n, filteredBags
                    .stream()
                    .map(baggage -> Utils.pathFinder(playerPosition, baggage.getPosition(), level,
                                               character -> character != '#').size())
                    .reduce(Integer::sum)
                    .orElseThrow());
            })
            .min((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
            .orElseThrow();

        return entry.getKey();
    }

    public Node getRandomChildNode() {
        int noOfPossibleMoves = this.childArray.size();
        int selectRandom = (int) (Math.random() * noOfPossibleMoves);
        return this.childArray.get(selectRandom);
    }

    public Node getChildWithMaxScore() {
        return Collections.max(this.childArray, Comparator.comparing(c -> c.getState().getVisitCount()));
    }
}
