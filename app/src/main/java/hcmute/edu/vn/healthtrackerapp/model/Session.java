package hcmute.edu.vn.healthtrackerapp.model;

public class Session {
    private long startTime;
    private long endTime;
    private int steps;
    private float distance;
    private float calories;

    public Session(long startTime, long endTime, int steps, float distance, float calories) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.steps = steps;
        this.distance = distance;
        this.calories = calories;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getSteps() {
        return steps;
    }

    public float getDistance() {
        return distance;
    }

    public float getCalories() {
        return calories;
    }
}