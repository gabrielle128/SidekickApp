package com.gingineers.sidekick.authentication;

public class User {
    public String email;

    public User(){
    }

    public User(String email){
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
