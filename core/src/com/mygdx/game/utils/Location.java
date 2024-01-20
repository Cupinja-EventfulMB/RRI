package com.mygdx.game.utils;

public class Location {
    private String institution;
    private String city;
    private String street;
    private Geolocation geolocation;
    private String description;
    private String email;

    public Location(String institution, String city, String street, Geolocation geolocation, String description, String email) {
        this.institution = institution;
        this.city = city;
        this.street = street;
        this.geolocation = geolocation;
        this.description = description;
        this.email = email;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Geolocation getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(Geolocation geolocation) {
        this.geolocation = geolocation;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

