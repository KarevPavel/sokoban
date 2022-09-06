package karev.pavel.sokoban;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import karev.pavel.sokoban.Level.Status;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class State {
    private Level level;
    private int visitCount;
    private Status status;
    int score;

    public State() {

    }

    public State(State state) {
        this.level = new Level(state.getLevel());
        this.visitCount = state.getVisitCount();
        this.status = state.status;
        this.score = state.score;
    }

    public State(Level lvl) {
        this.level = new Level(lvl);
    }

    public List<State> getAllPossibleStates() {
        List<State> possibleStates = new ArrayList<>();
        Collection<Position> availableMoves = this.level.availableMoves();
        availableMoves.forEach(p -> {
            var newState = new State(this.level);
            newState.getLevel().performMove(p);
            possibleStates.add(newState);
        });
        return possibleStates;
    }


    void incrementVisit() {
        this.visitCount++;
    }

    Level getLevel() {
        return level;
    }

/*    void randomPlay() {
        List<Position> availablePositions = this.level.availableMoves();
        int totalPossibilities = availablePositions.size();
        int selectRandom = (int) (Math.random() * totalPossibilities);
        this.level.performMove(availablePositions.get(selectRandom));
    }*/


    public void addScore(int score) {
        this.score += score;
    }

    public boolean hasCompletedBaggages() {
        return this.level.anyCompleted();
    }

    public boolean isCompleted() {
        return this.level.isCompleted();
    }
}
