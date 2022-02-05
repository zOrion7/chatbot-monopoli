package com.sysag.ChatbotMonopoli;

import java.util.List;

public class Ristorante {
    String nome, link, phone, address;
    int reviews;
    float rating;
    double latitude, longitude;
    List<Object> tipo;

    public Ristorante(String nome, String link, String phone, String address, int reviews, float rating, double latitude, double longitude, List<Object> tipo) {
        this.nome = nome;
        this.link = link;
        this.phone = phone;
        this.address = address;
        this.reviews = reviews;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public String getLink() {
        return link;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public int getReviews() {
        return reviews;
    }

    public float getRating() {
        return rating;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Object> getTipo() {
        return tipo;
    }
}
