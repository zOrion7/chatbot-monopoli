package com.sysag.ChatbotMonopoli;

public class Itinerario {
    String titolo, link, link_gmaps, descrizione, categoria, immagine, mezzi;
    int durata;
    double latitudine, longitudine;
    //List<Object> mezzi;

    public Itinerario(String titolo, String link, String link_gmaps, String descrizione, String categoria, String immagine, String durata, String latitudine, String longitudine, String mezzi) {
        this.titolo = titolo;
        this.link = link;
        this.link_gmaps = link_gmaps;
        this.descrizione = descrizione;
        this.categoria = categoria;
        this.immagine = immagine;
        this.durata = Integer.parseInt(durata);
        this.latitudine = Double.parseDouble(latitudine);
        this.longitudine = Double.parseDouble(longitudine);
        this.mezzi = mezzi;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getLink(){
        return link;
    }

    public String getLink_gmaps() {
        return link_gmaps;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getImmagine() {
        return immagine;
    }

    public int getDurata() {
        return durata;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public String getMezzi(){
        return mezzi;
    }

    /*public List<Object> getMezzi() {
        return mezzi;
    }*/
}
