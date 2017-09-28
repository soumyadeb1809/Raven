package soumyadeb.raven.models;

/**
 * Created by Soumya Deb on 13-07-2017.
 */

public class NotificationData {
    private String from;
    private String type;

    public NotificationData(String from, String type) {
        this.from = from;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public String getType() {
        return type;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setType(String type) {
        this.type = type;
    }
}
