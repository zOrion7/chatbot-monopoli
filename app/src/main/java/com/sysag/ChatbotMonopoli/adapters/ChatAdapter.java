package com.sysag.ChatbotMonopoli.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sysag.ChatbotMonopoli.Itinerario;
import com.sysag.ChatbotMonopoli.MainActivity;
import com.sysag.ChatbotMonopoli.R;
import com.sysag.ChatbotMonopoli.Ristorante;
import com.sysag.ChatbotMonopoli.Spiaggia;
import com.sysag.ChatbotMonopoli.models.Message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

  private List<Message> messageList;
  private Activity activity;

  // RISTORANTE
  //primo
  public static TextView restaurant_name, restaurant_phone_number, restaurant_rating, restaurant_reviews_number, restaurant_distance;
  public static Button restaurant_gmaps;
  //secondo
  public static TextView restaurant_name2, restaurant_phone_number2, restaurant_rating2, restaurant_reviews_number2, restaurant_distance2;
  public static Button restaurant_gmaps2;

  // ITINERARI
  public static ImageView itinerario_imageView;
  public static TextView itinerario_titolo, itinerario_descrizione, itinerario_categoria, itinerario_mezzi, itinerario_durata, itinerario_distanza;
  public static Button itinerario_gmaps;

  // LUOGHI DI INTERESSE E SPIAGGE
  public static ImageView luoghi_imageView;
  public static TextView luoghi_name, luoghi_description, luoghi_type_spiagge_tel, luoghi_distance;
  public static Button luoghi_gmaps;

  public ChatAdapter(List<Message> messageList, Activity activity) {
    this.messageList = messageList;
    this.activity = activity;
  }

  // Questa funzione crea il ViewHolder ed effettua l'inflate dopo aver effetuato i binding con onBindViewHolder
  @NonNull @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    if(restaurant_name!=null)Log.d("CHECK", "*************************************************************\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tTEST FINALE: " + restaurant_name.getText());
    View view = LayoutInflater.from(activity).inflate(R.layout.adapter_message_one, parent, false);
    Log.d("KALKAMAR", "INFLATE");
    return new MyViewHolder(view);
  }

  // Questa funzione gestisce i messaggi inviati e ricevuti, mostrandoli o nascondendoli. Se il messaggio è ricevuto, verrà mostrato solo il messaggio grigio a sinistra e verrà nascosto il verde a destra (e viceversa).
  // Il nostro piano è quindi non più di creare un'altra view per un messaggio grigio diverso, ma di modificare quella stessa view (vedi file xml) inserendo una "struttura" e nascondendola quando non necessaria.
  @Override public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

    //.d("CHECK", "position: " + position + "  specialMap: " + MainActivity.specialMap.get(position));

    // QUI VIENE SALVATO IL MESSAGGIO, NELLA FUNZIONE BINDING, forse dobbiamo avvalorare le nostre stringhe come nome ristorante qui
    String message = messageList.get(position).getMessage();
    holder.restaurantView.setVisibility(View.GONE);
    holder.restaurantView2.setVisibility(View.GONE);
    holder.itinerariView.setVisibility(View.GONE);
    holder.luoghiView.setVisibility(View.GONE);

    boolean isReceived = messageList.get(position).getIsReceived();

    // TODO sfruttare la variabile position per stampare l'elemento corretto dalla lista di ristoranti o itinerari.
    if(isReceived) {

      holder.messageReceive.setText(message);
      restaurant_name.setText(message);
      Log.d("KALKAMAR", "position = " + position + "   specialMap_ristoranti.get(position) = " + MainActivity.specialMap_ristoranti.get(position) + "   message: " + message + "   restaurant.name = " + restaurant_name.getText());
      holder.messageReceive.setVisibility(View.VISIBLE);
      holder.messageSend.setVisibility(View.GONE);
      holder.restaurantView.setVisibility(View.VISIBLE);

      /*check_ristoranti(holder, position);
      check_itinerari(holder, position);
      check_spiagge(holder, position);*/

      //check_eventi(holder, position);
      //check_luoghi_di_interesse(holder, position);
      //check_spiagge(holder, position);

      Log.d("CHECK", "SETTO Messaggio ricevuto: " + message + "     in posizione " + position);
      Log.d("CHECK", "RICEVUTO\n-----------------------------------------------------------------------------------------");
    }else {
      holder.messageSend.setText(message);
      holder.messageSend.setVisibility(View.VISIBLE);
      holder.messageReceive.setVisibility(View.GONE);
      holder.restaurantView.setVisibility(View.GONE);
      holder.restaurantView2.setVisibility(View.GONE);
      Log.d("CHECK", "SETTO Messaggio inviato: " + message + "     in posizione " + position);
      Log.d("CHECK", "INVIATO\n-----------------------------------------------------------------------------------------");
    }
  }

  @Override public int getItemCount() {
    return messageList.size();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder{

    TextView messageSend;
    TextView messageReceive;
    View restaurantView, restaurantView2, itinerariView, luoghiView;
    ImageView star1, star2, star3, star4, star5, star1_2, star2_2, star3_2, star4_2, star5_2;
    ImageView star_h1, star_h2, star_h3, star_h4, star_h5, star_h1_2, star_h2_2, star_h3_2, star_h4_2, star_h5_2;

    MyViewHolder(@NonNull View itemView) {
      super(itemView);

      // STELLE
      // prima wave
      star1 = itemView.findViewById(R.id.star_filled_1);
      star2 = itemView.findViewById(R.id.star_filled_2);
      star3 = itemView.findViewById(R.id.star_filled_3);
      star4 = itemView.findViewById(R.id.star_filled_4);
      star5 = itemView.findViewById(R.id.star_filled_5);
      star_h1 = itemView.findViewById(R.id.star_halved_1);
      star_h2 = itemView.findViewById(R.id.star_halved_2);
      star_h3 = itemView.findViewById(R.id.star_halved_3);
      star_h4 = itemView.findViewById(R.id.star_halved_4);
      star_h5 = itemView.findViewById(R.id.star_halved_5);
      // seconda wave
      star1_2 = itemView.findViewById(R.id.star_filled_1_2);
      star2_2 = itemView.findViewById(R.id.star_filled_2_2);
      star3_2 = itemView.findViewById(R.id.star_filled_3_2);
      star4_2 = itemView.findViewById(R.id.star_filled_4_2);
      star5_2 = itemView.findViewById(R.id.star_filled_5_2);
      star_h1_2 = itemView.findViewById(R.id.star_halved_1_2);
      star_h2_2 = itemView.findViewById(R.id.star_halved_2_2);
      star_h3_2 = itemView.findViewById(R.id.star_halved_3_2);
      star_h4_2 = itemView.findViewById(R.id.star_halved_4_2);
      star_h5_2 = itemView.findViewById(R.id.star_halved_5_2);

      // RISTORANTE
      // ATTENZIONE, questo binding avviene tante volte, ma dopo la nostra assegnazione non avviene, quindi dovremmo riuscire a forzarlo
      //primo
      restaurant_name = itemView.findViewById(R.id.restaurant_name);
      restaurant_phone_number = itemView.findViewById(R.id.restaurant_phone_number);
      restaurant_rating = itemView.findViewById(R.id.restaurant_rating);
      restaurant_reviews_number = itemView.findViewById(R.id.restaurant_reviews_number);
      restaurant_distance = itemView.findViewById(R.id.restaurant_distance);
      restaurant_gmaps = itemView.findViewById(R.id.restaurant_gmaps);
      //secondo
      restaurant_name2 = itemView.findViewById(R.id.restaurant_name2);
      restaurant_phone_number2 = itemView.findViewById(R.id.restaurant_phone_number2);
      restaurant_rating2 = itemView.findViewById(R.id.restaurant_rating2);
      restaurant_reviews_number2 = itemView.findViewById(R.id.restaurant_reviews_number2);
      restaurant_distance2 = itemView.findViewById(R.id.restaurant_distance2);
      restaurant_gmaps2 = itemView.findViewById(R.id.restaurant_gmaps2);

      // ITINERARI
      itinerario_titolo = itemView.findViewById(R.id.itinerario_titolo);
      itinerario_descrizione = itemView.findViewById(R.id.itinerario_description);
      itinerario_categoria = itemView.findViewById(R.id.itinerario_categoria);
      itinerario_mezzi = itemView.findViewById(R.id.itinerario_mezzi);
      itinerario_durata = itemView.findViewById(R.id.itinerario_durata);
      itinerario_distanza = itemView.findViewById(R.id.itinerario_distanza);
      itinerario_gmaps = itemView.findViewById(R.id.itinerario_gmaps);
      itinerario_imageView = itemView.findViewById(R.id.itinerario_imageView);

      // LUOGHI DI INTERESSE E SPIAGGE
      luoghi_name = itemView.findViewById(R.id.luoghi_name);
      luoghi_description = itemView.findViewById(R.id.luoghi_description);
      luoghi_type_spiagge_tel = itemView.findViewById(R.id.luoghi_type_spiagge_tel);
      luoghi_distance = itemView.findViewById(R.id.luoghi_distance);
      luoghi_gmaps = itemView.findViewById(R.id.luoghi_gmaps);
      luoghi_imageView = itemView.findViewById(R.id.luoghi_imageView);

      restaurantView = itemView.findViewById(R.id.restaurantView);
      restaurantView2 = itemView.findViewById(R.id.restaurantView2);
      itinerariView = itemView.findViewById(R.id.itinerarioView);
      luoghiView = itemView.findViewById(R.id.luoghiView);

      messageSend = itemView.findViewById(R.id.message_send);
      messageReceive = itemView.findViewById(R.id.message_receive);
      if(restaurant_name!=null)Log.d("CHECK", "*************************************************************\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tTEST DOPO FINALE: " + restaurant_name.getText() + "\tmessaggio:" + messageReceive.getText());

    }
  }

  private void check_ristoranti(MyViewHolder holder, int position) {
    Log.d("CHECKO", "position " + position + "   specialMap " + MainActivity.specialMap_ristoranti.get(position));
    Log.d("CHECKO", "check_ristoranti: " + MainActivity.specialMap_ristoranti.get(position));
    if(MainActivity.specialMap_ristoranti.get(position)>=0){  // IF SPECIAL

      MainActivity.check_distance(MainActivity.distanze_ristoranti);
      Log.d("CHECK", "SPECIAL\n-----------------------------------------------------------------------------------------");

      //int index = MainActivity.specialMap_ristoranti.get(position)/10; // INDICE DEL PRIMO RISTORANTE DA STAMPARE
      int index = MainActivity.closest_position; // INDICE DEL PRIMO RISTORANTE DA STAMPARE
      int index2= MainActivity.second_closest_position; // INDICE DEL SECONDO RISTORANTE DA STAMPARE
      int quantity = MainActivity.specialMap_ristoranti.get(position)%10;  // QUANTITà DI RISORANTI DA STAMPARE

      // SET DI ITERATOR PER SELEZIONARE I RISTORANTI CORRETTI
      Iterator<Ristorante> iterator = MainActivity.ristoranti.iterator();
      Ristorante temp = null;

      // if(MainActivity.specialMap_ristoranti.get(position)<10){
        //  Log.d(""CHECK, "specialMap: " + MainActivity.specialMap_ristoranti.get(position));
        //  temp = iterator.next();
        // }else {

      for(int i=0; i<=index; i++){
        Log.d("NUOVO", "i = " + i);
        temp = iterator.next();
      }
      set_ristoranti(holder, position, temp);
      Log.d("NUOVO", "NOME 1: " + temp.getNome() + "   position" + position);;
      iterator = MainActivity.ristoranti.iterator();
      for(int i=0; i<= index2; i++){
        Log.d("NUOVO", "i = " + i);
        temp = iterator.next();
      }
      set_ristoranti2(holder, position, temp);
      Log.d("NUOVO", "NOME 2: " + temp.getNome() + "   position:" + position);;
      Log.d("CHECK", "temp = " + temp.getNome());

      //}
      holder.restaurantView.setVisibility(View.VISIBLE);
      holder.restaurantView2.setVisibility(View.VISIBLE);
      Log.d("CHECK", "index = " + index + "  index2 = " + index2);
    }
  }

  // SETTAGGIO DEL NOME CON LINK INCORPORATO
  private void set_ristoranti(MyViewHolder holder, int position, Ristorante temp ) {

    String text = "<a href='" + temp.getLink() + "'>" + temp.getNome() + "</a>";
    Log.d("CHECK", "position: " + position);
    Log.d("CHECK", "nome:" + temp.getNome() + "  link : " + temp.getLink());
    Log.d("CHECK", "specialMap : " + MainActivity.specialMap_ristoranti);
    restaurant_name.setMovementMethod(LinkMovementMethod.getInstance());
    restaurant_name.setText(Html.fromHtml(text));
    restaurant_phone_number.setText(activity.getString(R.string.telephone) + ": " + temp.getPhone());
    restaurant_rating.setText(""+temp.getRating());
    restaurant_reviews_number.setText(" (" + String.valueOf(temp.getReviews()) + ")");
    hide_stars_ristoranti(holder, temp);
    Location locationB = new Location("position to compare");
    locationB.setLatitude(temp.getLatitude());
    locationB.setLongitude(temp.getLongitude());
    Log.d("GPS", "temp.latitude = " + temp.getLatitude());
    Log.d("GPS", "temp.longitude = " + temp.getLongitude());
    restaurant_distance.setText(String.format("%.02f", MainActivity.user_position.distanceTo(locationB)/1000) + " km");
  }

  // SETTAGGIO DEL NOME CON LINK INCORPORATO
  private void set_ristoranti2(MyViewHolder holder, int position, Ristorante temp) {

    String text = "<a href='" + temp.getLink() + "'>" + temp.getNome() + "</a>";
    Log.d("CHECK", "position: " + position);
    Log.d("CHECK", "nome:" + temp.getNome() + "  link : " + temp.getLink());
    Log.d("CHECK", "specialMap : " + MainActivity.specialMap_ristoranti);
    restaurant_name2.setMovementMethod(LinkMovementMethod.getInstance());
    restaurant_name2.setText(Html.fromHtml(text));
    restaurant_phone_number2.setText(activity.getString(R.string.telephone) + ": " + temp.getPhone());
    restaurant_rating2.setText(""+temp.getRating());
    restaurant_reviews_number2.setText(" (" + String.valueOf(temp.getReviews()) + ")");
    hide_stars_ristoranti2(holder, temp);
    Location locationB = new Location("position to compare");
    locationB.setLatitude(temp.getLatitude());
    locationB.setLongitude(temp.getLongitude());
    Log.d("GPS", "temp.latitude = " + temp.getLatitude());
    Log.d("GPS", "temp.longitude = " + temp.getLongitude());
    restaurant_distance2.setText(String.format("%.02f", MainActivity.user_position.distanceTo(locationB)/1000) + " km");
  }

  private void hide_stars_ristoranti(MyViewHolder holder, Ristorante temp) {
    if(temp.getRating()==0.5) {
      hide_stars(holder.star1);
      hide_stars(holder.star_h2);
      hide_stars(holder.star2);
      hide_stars(holder.star_h3);
      hide_stars(holder.star3);
      hide_stars(holder.star_h4);
      hide_stars(holder.star4);
      hide_stars(holder.star_h5);
      hide_stars(holder.star5);
    }else if (temp.getRating()==1.0){
      hide_stars(holder.star_h2);
      hide_stars(holder.star2);
      hide_stars(holder.star_h3);
      hide_stars(holder.star3);
      hide_stars(holder.star_h4);
      hide_stars(holder.star4);
      hide_stars(holder.star_h5);
      hide_stars(holder.star5);
    }else if (temp.getRating()==1.5){
      hide_stars(holder.star2);
      hide_stars(holder.star_h3);
      hide_stars(holder.star3);
      hide_stars(holder.star_h4);
      hide_stars(holder.star4);
      hide_stars(holder.star_h5);
      hide_stars(holder.star5);
    }else if (temp.getRating()==2.0){
      hide_stars(holder.star_h3);
      hide_stars(holder.star3);
      hide_stars(holder.star_h4);
      hide_stars(holder.star4);
      hide_stars(holder.star_h5);
      hide_stars(holder.star5);
    }else if (temp.getRating()==2.5){
      hide_stars(holder.star3);
      hide_stars(holder.star_h4);
      hide_stars(holder.star4);
      hide_stars(holder.star_h5);
      hide_stars(holder.star5);
    }else if (temp.getRating()==3.0){
      hide_stars(holder.star_h4);
      hide_stars(holder.star4);
      hide_stars(holder.star_h5);
      hide_stars(holder.star5);
    }else if (temp.getRating()==3.5){
      hide_stars(holder.star4);
      hide_stars(holder.star_h5);
      hide_stars(holder.star5);
    }else if (temp.getRating()==4.0){
      hide_stars(holder.star_h5);
      hide_stars(holder.star5);
    }else if (temp.getRating()==4.5) {
      hide_stars(holder.star5);
    }
  }

  private void hide_stars_ristoranti2(MyViewHolder holder, Ristorante temp) {
    if(temp.getRating()==0.5) {
      hide_stars(holder.star1_2);
      hide_stars(holder.star_h2_2);
      hide_stars(holder.star2_2);
      hide_stars(holder.star_h3_2);
      hide_stars(holder.star3_2);
      hide_stars(holder.star_h4_2);
      hide_stars(holder.star4_2);
      hide_stars(holder.star_h5_2);
      hide_stars(holder.star5_2);
    }else if (temp.getRating()==1.0){
      hide_stars(holder.star_h2_2);
      hide_stars(holder.star2_2);
      hide_stars(holder.star_h3_2);
      hide_stars(holder.star3_2);
      hide_stars(holder.star_h4_2);
      hide_stars(holder.star4_2);
      hide_stars(holder.star_h5_2);
      hide_stars(holder.star5_2);
    }else if (temp.getRating()==1.5){
      hide_stars(holder.star2_2);
      hide_stars(holder.star_h3_2);
      hide_stars(holder.star3_2);
      hide_stars(holder.star_h4_2);
      hide_stars(holder.star4_2);
      hide_stars(holder.star_h5_2);
      hide_stars(holder.star5_2);
    }else if (temp.getRating()==2.0){
      hide_stars(holder.star_h3_2);
      hide_stars(holder.star3_2);
      hide_stars(holder.star_h4_2);
      hide_stars(holder.star4_2);
      hide_stars(holder.star_h5_2);
      hide_stars(holder.star5_2);
    }else if (temp.getRating()==2.5){
      hide_stars(holder.star3_2);
      hide_stars(holder.star_h4_2);
      hide_stars(holder.star4_2);
      hide_stars(holder.star_h5_2);
      hide_stars(holder.star5_2);
    }else if (temp.getRating()==3.0){
      hide_stars(holder.star_h4_2);
      hide_stars(holder.star4_2);
      hide_stars(holder.star_h5_2);
      hide_stars(holder.star5_2);
    }else if (temp.getRating()==3.5){
      hide_stars(holder.star4_2);
      hide_stars(holder.star_h5_2);
      hide_stars(holder.star5_2);
    }else if (temp.getRating()==4.0){
      hide_stars(holder.star_h5_2);
      hide_stars(holder.star5_2);
    }else if (temp.getRating()==4.5) {
      hide_stars(holder.star5_2);
    }
  }

  private void check_itinerari(MyViewHolder holder, int position) {
    if(MainActivity.specialMap_itinerari.get(position)>0){  // IF SPECIAL

      Log.d("CHECK", "check itinerari superato\n-----------------------------------------------------------------------------------------");

      int index = MainActivity.specialMap_itinerari.get(position)/10; // INDICE DEL PRIMO RISTORANTE DA STAMPARE
      int quantity = MainActivity.specialMap_itinerari.get(position)%10;  // QUANTITà DI RISORANTI DA STAMPARE

      Log.d("CHECK", "Itinerari index = " + index + "\t\tquantity = " +quantity);

      // SET DI ITERATOR PER SELEZIONARE GLI ITINERARI CORRETTI
      Iterator<Itinerario> iterator = MainActivity.itinerari.iterator();
      Itinerario temp = null;

      if(MainActivity.specialMap_itinerari.get(position)<10){
        Log.d("CHECK", "specialMap: " + MainActivity.specialMap_itinerari.get(position));
        temp = iterator.next();
      }else {
        for (int i = 0; i <= index; i++) {
          temp = iterator.next();
        }
      }

      Log.d("CHECK", "position: " + position);
      Log.d("CHECK", "nome:" + temp.getTitolo() + "  categoria : " + temp.getCategoria());
      Log.d("CHECK", "specialMap : " + MainActivity.specialMap_itinerari);

      String url_itinerario = temp.getLink_gmaps();
      String text = "<a href='" + temp.getLink() + "'>" + temp.getTitolo() + "</a>";
      String immagine_itinerario = temp.getImmagine();
      itinerario_titolo.setMovementMethod(LinkMovementMethod.getInstance());
      itinerario_titolo.setText(Html.fromHtml(text));
      itinerario_descrizione.setText(temp.getDescrizione());
      itinerario_categoria.setText("Categoria:  " + temp.getCategoria());
      itinerario_mezzi.setText("Mezzi:  " + temp.getMezzi());
      itinerario_durata.setText("Durata: " + temp.getDurata() + "'");
      Location locationB = new Location("position to compare");
      locationB.setLatitude(temp.getLatitudine());
      locationB.setLongitude(temp.getLongitudine());
      Log.d("GPS", "temp.latitude = " + temp.getLatitudine());
      Log.d("GPS", "temp.longitude = " + temp.getLongitudine());
      itinerario_distanza.setText(String.format("%.02f", MainActivity.user_position.distanceTo(locationB)/1000) + " km");
      LoadImage loadImage = new LoadImage(itinerario_imageView);
      loadImage.execute(immagine_itinerario);
      Log.d("URL", "link = " + url_itinerario);
      itinerario_gmaps.setOnClickListener(v -> {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url_itinerario));
        activity.startActivity(i);
      });

      holder.itinerariView.setVisibility(View.VISIBLE);
      Log.d("CHECK", "SETTO TextView restaurant_name: " + itinerario_titolo.getText());
    }
  }

  private void check_spiagge(MyViewHolder holder, int position) {
    if(MainActivity.specialMap_spiagge.get(position)>0){  // IF SPECIAL

      Log.d("CHECK", "check spiagge superato\n-----------------------------------------------------------------------------------------");

      int index = MainActivity.specialMap_spiagge.get(position)/10; // INDICE DEL PRIMO LUOGO DA STAMPARE
      int quantity = MainActivity.specialMap_spiagge.get(position)%10;  // QUANTITà DI LUOGHI DA STAMPARE

      Log.d("CHECK", "spiagge index = " + index + "\t\tquantity = " +quantity);

      // SET DI ITERATOR PER SELEZIONARE GLI spiagge CORRETTI
      Iterator<Spiaggia> iterator = MainActivity.spiagge.iterator();
      Spiaggia temp = null;

      if(MainActivity.specialMap_spiagge.get(position)<10){
        Log.d("CHECK", "specialMap: " + MainActivity.specialMap_spiagge.get(position));
        temp = iterator.next();
      }else {
        for (int i = 0; i <= index; i++) {
          temp = iterator.next();
        }
      }

      Log.d("CHECK", "position: " + position);
      Log.d("CHECK", "nome:" + temp.getNome() + "  telefono: " + temp.getTelefono());
      Log.d("CHECK", "specialMap : " + MainActivity.specialMap_spiagge);

      String url_spiaggia = temp.getLink_gmaps();
      String immagine_spiaggia = temp.getImmagine();
      luoghi_name.setMovementMethod(LinkMovementMethod.getInstance());
      luoghi_name.setText(temp.getNome());
      luoghi_description.setText(temp.getDescrizione());
      if (temp.getTelefono()!="null") luoghi_type_spiagge_tel.setText("Telefono: " + temp.getTelefono());
      Location locationB = new Location("position to compare");
      locationB.setLatitude(temp.getLatitudine());
      locationB.setLongitude(temp.getLongitudine());
      Log.d("GPS", "temp.latitude = " + temp.getLatitudine());
      Log.d("GPS", "temp.longitude = " + temp.getLongitudine());
      luoghi_distance.setText(String.format("%.02f", MainActivity.user_position.distanceTo(locationB)/1000) + " km");
      LoadImage loadImage = new LoadImage(luoghi_imageView);
      loadImage.execute(immagine_spiaggia);
      Log.d("URL", "link = " + url_spiaggia);
      luoghi_gmaps.setOnClickListener(v -> {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url_spiaggia));
        activity.startActivity(i);
      });

      holder.luoghiView.setVisibility(View.VISIBLE);
      Log.d("CHECK", "SETTO TextView luoghi_name: " + luoghi_name.getText());
    }
  }

  public void hide_stars(ImageView star){
    star.setVisibility(View.GONE);
  }

  //Non più utilizzata, le stelle sono visibili di default e vengono nascoste quando serve
  public void show_stars(ImageView star){
    star.setVisibility(View.VISIBLE);
  }


  private class LoadImage extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;
    public LoadImage(ImageView imageView) {
      this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
      String url_immagine = strings[0];
      Bitmap bitmap = null;
      try {
        InputStream inputStream = new java.net.URL(url_immagine).openStream();
        bitmap = BitmapFactory.decodeStream(inputStream);
        float aspectRatio = bitmap.getWidth() /
                (float) bitmap.getHeight();
        int width = 300;
        int height = Math.round(width / aspectRatio);

        bitmap = Bitmap.createScaledBitmap(
                bitmap, width, height, false);
      } catch (IOException e){
        e.printStackTrace();
      }
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      imageView.setImageBitmap(bitmap);
    }
  }
}
