package moscow.rockstar.render.diagnostics;

/** Counts low-level draw calls for the optional in-game render diagnostics. */
public final class DrawCallCounter {
    private static int currentCount;
    private static int lastCount;
    private static int countingDepth;

    private DrawCallCounter() {
    }

    public static void recordDrawCall() {
        if (countingDepth > 0) {
            currentCount++;
        }
    }

    public static void reset() {
        lastCount = currentCount;
        currentCount = 0;
        countingDepth = 0;
    }

    public static void beginCounting() {
        countingDepth++;
    }

    public static void endCounting() {
        if (countingDepth > 0) {
            countingDepth--;
        }
    }

    public static int getCount() {
        return lastCount;
    }
}
