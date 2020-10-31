package castle.comp3021.assignment.gui;

import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * Timer for counting time elapsed
 */
public class DurationTimer {
    /**
     * default time in seconds during each round
     */
    private static int defaultEachRound = 30;

    @NotNull
    private Timer flowTimer = new Timer(true);


    private final List<Runnable> onTickCallbacks = new ArrayList<>();

    /**
     * time elapsed
     */
    private int ticksElapsed;


    public static void setDefaultEachRound(int duration) {
        defaultEachRound = duration;
    }


    public static int getDefaultEachRound() {
        return defaultEachRound;
    }


    DurationTimer() {
        ticksElapsed = 0;
    }

    /**
     *  Registers a callback to be run when a tick has passed.
     * @param cb
     */
    void registerTickCallback(@NotNull final Runnable cb) {
        onTickCallbacks.add(cb);
    }


    /**
     * start the timer, timer should increase 1 every 1 second
     * Hint:
     *  - You may need to use {@link Timer#scheduleAtFixedRate(TimerTask, long, long)}
     */
    void start() {
        //TODO
        this.ticksElapsed = 0;
        var task = new TimerTask() {
            @Override
            public void run() {
                ticksElapsed++;
            }
        };
        flowTimer.scheduleAtFixedRate(task, 1000,1000);
    }

    /**
     * Stop the timer
     */
    void stop() {
        //TODO
        flowTimer.cancel();
    }

}
