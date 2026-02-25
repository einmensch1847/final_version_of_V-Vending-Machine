package View;

public class Admin {
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String level;

    public Admin(String username, String fullname, String email, String phone, String level) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.level = level;
    }

    public String getUsername() { return username; }
    public String getFullname() { return fullname; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getLevel() { return level; }

    // ======= Setter ها =======
    public void setFullname(String fullname) { this.fullname = fullname; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLevel(String level) { this.level = level; }
}
