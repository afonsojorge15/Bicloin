package pt.tecnico.bicloin.hub.domain;

public class User {
    private final String user_name;
    private final String name;
    private final String phoneNumber;


    public User(String user_name, String name, String phoneNumber) {
        this.user_name = user_name;
        this.name = name;
        this.phoneNumber = phoneNumber;

    }

    String getUsername() {
        return user_name;
    }

    String getName() {
        return name;
    }

    String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_name='" + user_name + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
