package com.example.handygit.Model;

public class UserModel {
    private String firstName,lastName,phoneNumber;


    public UserModel(){

    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName){
        this.firstName=firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName){
        this.lastName=lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber=phoneNumber;
    }


}
