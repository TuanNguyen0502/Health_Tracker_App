package hcmute.edu.vn.healthtrackerapp.model;

public class MedicalRecord {
    private String title;
    private String imageUrl;

    public MedicalRecord() {} // Needed for Firebase

    public MedicalRecord(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}