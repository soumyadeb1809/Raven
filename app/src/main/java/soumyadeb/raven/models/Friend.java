package soumyadeb.raven.models;

/**
 * Created by Soumya Deb on 17-07-2017.
 */

public class Friend {
    private String timestamp;

    public Friend() {
    }

    public Friend(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
