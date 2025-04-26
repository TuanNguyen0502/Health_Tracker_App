package hcmute.edu.vn.healthtrackerapp.model;

public class MedicalRecord {
    private String id;
    private String title;
    private String imageUrl;
    private String public_id;

    public MedicalRecord() {
    } // Needed for Firebase

    public MedicalRecord(String id, String title, String imageUrl, String public_id) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.public_id = public_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPublic_id() {
        return public_id;
    }
}