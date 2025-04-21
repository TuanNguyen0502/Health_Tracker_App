package hcmute.edu.vn.healthtrackerapp.model;

public class Appointment {
    public String id, userEmail, doctorEmail, date, time;

    public Appointment() {}

    public Appointment(String id, String userEmail, String doctorEmail, String date, String time) {
        this.id = id;
        this.userEmail = userEmail;
        this.doctorEmail = doctorEmail;
        this.date = date;
        this.time = time;
    }
}
