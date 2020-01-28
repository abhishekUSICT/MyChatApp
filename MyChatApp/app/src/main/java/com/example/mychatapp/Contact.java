package com.example.mychatapp;

public class Contact {
    public String name, image;
    public Contact(){}
    public Contact(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public String getImage() {
        return image;
    }
}
