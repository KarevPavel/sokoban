package karev.pavel.sokoban;


import java.time.Instant;
import java.time.temporal.ChronoField;
import javax.swing.JComponent;
import lombok.Getter;

public abstract class Animation {

    public Animation(JComponent targetComponent) {
        this(targetComponent, ChronoField.MILLI_OF_DAY, 50);
    }

    public Animation(JComponent targetComponent, int millis) {
        this(targetComponent, ChronoField.MILLI_OF_DAY, millis);
    }

    public Animation(JComponent targetComponent, ChronoField unit, int timeleft) {
        this.unit = unit;
        this.timeleft = timeleft;
        this.targetComponent = targetComponent;
    }

    public void onFinish() {}
    public abstract void onFireUp();
    public abstract boolean finishCondition();

    private boolean animationStated = false;

    @Getter
    private boolean animationCompleted = false;
    private Instant animationStart;

    protected final JComponent targetComponent;
    private final ChronoField unit;
    private final int timeleft;


    public void fireUp() {
        if (!animationStated) {
            animationStart = Instant.now();
            animationStated = true;
        }

        var now = Instant.now();
        long timeleft = now.minus(animationStart.toEpochMilli(), unit.getBaseUnit()).toEpochMilli();
        System.out.println();
        if (timeleft > this.timeleft) {
            onFireUp();
            animationStart = Instant.now();
        }

        if (finishCondition()) {
            animationStated = false;
            animationCompleted = true;
            onFinish();
        }

        if (!animationCompleted) {
            targetComponent.updateUI();
        }
    }
}
