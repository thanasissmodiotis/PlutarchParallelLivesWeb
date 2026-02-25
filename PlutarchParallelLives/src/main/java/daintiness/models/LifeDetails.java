package daintiness.models;


public class LifeDetails {
    private final int birthBeatId;
    private final int deathBeatId;
    private final boolean isAlive;
    private final int duration;

    public LifeDetails(int birthBeatId, int deathBeatId, boolean isAlive, int duration) {
        this.birthBeatId = birthBeatId;
        this.deathBeatId = deathBeatId;
        this.isAlive = isAlive;
        this.duration = duration;
    }

    public LifeDetails(int birthBeatId, int deathBeatId, boolean isAlive) {
        this.birthBeatId = birthBeatId;
        this.deathBeatId = deathBeatId;
        this.isAlive = isAlive;
        this.duration = deathBeatId - birthBeatId + 1;
    }

    public int getBirthBeatId() {
        return birthBeatId;
    }

    public int getDeathBeatId() {
        return deathBeatId;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "LifeDetails{" +
                "birthBeatId=" + birthBeatId +
                ", deathBeatId=" + deathBeatId +
                ", isAlive=" + isAlive +
                ", duration=" + duration +
                '}';
    }
}
