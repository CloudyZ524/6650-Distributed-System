package Model;

import consumer.LiftRide;

public class SkiRecord {
    private int skierId;
    private int seasonId;
    private int dayId;
    private int resortId;
    private LiftRide liftRide;

    public SkiRecord(int skierId, int seasonId, int dayId, int resortId, LiftRide liftRide) {
        this.skierId = skierId;
        this.seasonId = seasonId;
        this.dayId = dayId;
        this.resortId = resortId;
        this.liftRide = liftRide;
    }

    // get the combined sort key
    public String getSeasonDayId() {
        return seasonId + "#" + dayId;
    }

    public int getSkierId() {
        return skierId;
    }

    public void setSkierId(int skierId) {
        this.skierId = skierId;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public int getDayId() {
        return dayId;
    }

    public void setDayId(int dayId) {
        this.dayId = dayId;
    }

    public int getResortId() {
        return resortId;
    }

    public void setResortId(int resortId) {
        this.resortId = resortId;
    }

    public LiftRide getLiftRide() {
        return liftRide;
    }

    public void setLiftRide(LiftRide liftRide) {
        this.liftRide = liftRide;
    }
}
