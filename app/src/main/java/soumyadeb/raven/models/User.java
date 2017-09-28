package soumyadeb.raven.models;

/**
 * Created by Soumya Deb on 21-06-2017.
 */

public class User {
    private String name;
    private String status;
    private String image;
    private String thumb_image;
    private String key;
    private String device_token;
    private Boolean online;
    public User(){
        // Empty constructor
    }

    public User (String name, String status, String image, String thumb_image, String device_token, Boolean online){
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
        this.device_token = device_token;
        this.online = online;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getDevice_token() {
        return device_token;
    }
}
