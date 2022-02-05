package com.sysag.ChatbotMonopoli;

public class Spiaggia {
    String nome, descrizione, link_gmaps, telefono, immagine;
    double latitudine, longitudine;

    public Spiaggia(String nome, String descrizione, String link_gmaps, String telefono, String immagine, String latitudine, String longitudine) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.link_gmaps = link_gmaps;
        this.telefono = telefono;
        this.immagine = immagine;
        this.latitudine = Double.parseDouble(latitudine);
        this.longitudine = Double.parseDouble(longitudine);
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getLink_gmaps() {
        return link_gmaps;
    }

    public String getTelefono(){
        return telefono;
    }

    public String getImmagine() {
        return immagine;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

}
