package daintiness.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Beat {
    private final int beatId;
    private final String rawDate;
    private final LocalDateTime date;

    public Beat(int beatId, String rawDate, LocalDateTime date) {
        this.beatId = beatId;
        this.rawDate = rawDate;
        this.date = date;
    }

    public int getBeatId() {
        return beatId;
    }

    public String getRawDate() {
        return rawDate;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "id: " + beatId +
                ", date: " + date;
    }

    public String getDateAsString() {
        if (date == null) {
            return String.valueOf(beatId);
        } else {
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
