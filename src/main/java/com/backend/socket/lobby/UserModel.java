package com.backend.socket.lobby;

public class UserModel {
    private String name;
    private String email;
    private String image;
    private int id;
    private String username;
    private String role;
    private String display_name;
    private String access_token;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public UserModel() {
    }

    public UserModel(String name, String email, String image, int id, String username, String role, String display_name, String access_token) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.id = id;
        this.username = username;
        this.role = role;
        this.display_name = display_name;
        this.access_token = access_token;
    }
}
