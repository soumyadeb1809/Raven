package soumyadeb.raven.models;

/**
 * Created by Soumya Deb on 27-07-2017.
 */

public class FriendRequest {
    private String request_state;

    public FriendRequest() {
    }

    public FriendRequest(String request_state) {
        this.request_state = request_state;
    }

    public String getRequest_state() {
        return request_state;
    }

    public void setRequest_state(String request_state) {
        this.request_state = request_state;
    }
}
