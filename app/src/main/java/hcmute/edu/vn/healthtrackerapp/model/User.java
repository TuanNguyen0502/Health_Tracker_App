package hcmute.edu.vn.healthtrackerapp.model;

public class User {
    private String email;
    private String name;
    private String phone;
    private String role;

    public User() {
        // Firebase yêu cầu constructor rỗng
    }

    public User(String email, String name, String phone, String role) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
    }
}
