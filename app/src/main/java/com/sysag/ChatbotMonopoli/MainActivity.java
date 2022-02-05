package com.sysag.ChatbotMonopoli;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.sysag.ChatbotMonopoli.adapters.ChatAdapter;
import com.sysag.ChatbotMonopoli.helpers.SendMessageInBg;
import com.sysag.ChatbotMonopoli.interfaces.BotReply;
import com.sysag.ChatbotMonopoli.models.Message;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.EntityType;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Lists;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements BotReply {

  public static boolean special = false;  // SERVE A MOSTRARE LA STRUTTURA SPECIALE
  public static int position = 0; // NUMERO DI MESSAGGI INVIATI O RICEVUTI TODO vedi sopra

  RecyclerView chatView;
  ChatAdapter chatAdapter;
  List<Message> messageList = new ArrayList<>();
  EditText editMessage;
  ImageButton btnSend;

  // Neo4j
  private static Config.ConfigBuilder builder = Config.builder().withEncryption();
  private static Config config = builder.build();
  private static Driver driver = GraphDatabase.driver("bolt://db-qd9efbalp1ynyuy8vjog.graphenedb.com:24786", AuthTokens.basic( "neo4j", "jungle-othello-tonight-equal-pearl-1837" ), config);
  private static Session session = driver.session();

  // DialogFlow
  private SessionsClient sessionsClient;
  private SessionName sessionName;
  private String uuid = UUID.randomUUID().toString();
  private String TAG = "mainactivity";
  private EntityType entityType;  // per prendere il parametro da chiedere al database
  private String dialog_flow_response;  // stringa che ritorna l'entità rilevata da Dialogflow

  // Ristorante
  public static HashMap<Integer, Integer> specialMap_ristoranti = new HashMap<Integer, Integer>();  // salvo (position, -1) di default, (position, ALTRO) se bisogna stampare una struttura (ristoranti, itinerari...)
  public static Ristorante ristorante;
  public static ArrayList<Ristorante> ristoranti = new ArrayList<Ristorante>();
  public static int size_ristoranti;
  public static int restaurant_index;
  public static Distanza distanza_ristorante;
  public static ArrayList<Distanza> distanze_ristoranti = new ArrayList<Distanza>();
  private boolean check_ristorante;


  // Itinerario
  public static HashMap<Integer, Integer> specialMap_itinerari = new HashMap<Integer, Integer>();
  public static Itinerario itinerario;
  public static ArrayList<Itinerario> itinerari = new ArrayList<Itinerario>();
  public static int size_itinerari;
  public static int itinerari_indice;
  private boolean check_itinerari;

  // Spiaggia
  public static HashMap<Integer, Integer> specialMap_spiagge = new HashMap<Integer, Integer>();
  public static Spiaggia spiaggia;
  public static ArrayList<Spiaggia> spiagge = new ArrayList<Spiaggia>();
  public static int size_spiagge;
  public static int spiagge_indice;
  private boolean check_spiagge;

  //GPS
  public static Location user_position;
  private static Location locationB; // punto interessato
  public static double closest_distance = 9999999;
  public static double second_closest_distance = 9999999;
  public static int closest_position;
  public static int second_closest_position;
  public static int counter=0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Log.d(TAG, "onCreate: ");
    chatView = findViewById(R.id.chatView);
    editMessage = findViewById(R.id.editMessage);
    btnSend = findViewById(R.id.btnSend);

    chatAdapter = new ChatAdapter(messageList, this);
    chatView.setAdapter(chatAdapter);

    // GPS
    GPSTracker gpsTracker = new GPSTracker(this);
    user_position = new Location("actual position");
    locationB = new Location("position to compare");

    if ( Build.VERSION.SDK_INT >= 23){
      if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
              PackageManager.PERMISSION_GRANTED  ){
        requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                1);
        return ;
      }
    }


    btnSend.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        String message = editMessage.getText().toString();
        Log.d("GPS", "arrivo");

        // GPS
        if (gpsTracker.getIsGPSTrackingEnabled()) {   // check if GPS enabled but not if permission for app is granted
          Log.d("GPS", "Entro in GPS e setto latitudine e longitudine");
          gpsTracker.updateGPSCoordinates();
          user_position.setLatitude(gpsTracker.latitude);
          user_position.setLongitude(gpsTracker.longitude);

          locationB.setLatitude(42.958783333333336);
          locationB.setLongitude(12.28923166666667);

          float x = user_position.distanceTo(locationB);

          Log.d("GPS", "distance = " + x);
          Log.d("GPS", "Mia latitudine = " + gpsTracker.latitude);
          Log.d("GPS", "Mia longitudine = " + gpsTracker.longitude);
        }else{
          // can't get location
          // GPS or Network is not enabled
          // Ask user to enable GPS/network in settings
          gpsTracker.showSettingsAlert();
        }

        // Invia messaggio
        if (!message.isEmpty()) {
          messageList.add(new Message(message, false));
          editMessage.setText("");
          sendMessageToBot(message);
          Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
          specialMap_ristoranti.put(position, -1);
          position++;
          Objects.requireNonNull(chatView.getLayoutManager())
              .scrollToPosition(messageList.size() - 1);
        } else {
          Toast.makeText(MainActivity.this, "Please enter text!", Toast.LENGTH_SHORT).show();
        }
      }
    });

    setUpBot();
  }

  private void setUpBot() {
    try {
      InputStream stream = this.getResources().openRawResource(R.raw.credential);
      GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
          .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
      String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

      SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
      SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
          FixedCredentialsProvider.create(credentials)).build();
      sessionsClient = SessionsClient.create(sessionsSettings);
      sessionName = SessionName.of(projectId, uuid);

      Log.d(TAG, "projectId : " + projectId);
    } catch (Exception e) {
      Log.d(TAG, "setUpBot: " + e.getMessage());
    }
  }

  private void sendMessageToBot(String message) {
    QueryInput input = QueryInput.newBuilder()      // SWITCHARE LINGUA
        .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
    new SendMessageInBg(this, sessionName, sessionsClient, input).execute();
  }

  // LA CHIAVE è BOTREPLY, INSIEME ALLE ISTRUZIONI A RIGA 142-143-144. IL PROBLEMA è CHE BOTREPLY è UNA STRINGA...

  // QUESTA FUNZIONE SERVE A CHIEDERE LA RISPOSTA A DIALOGFLOW
  @Override
  public void callback(DetectIntentResponse returnResponse) {
    restaurant_index = -1;
    itinerari_indice = -1;
    spiagge_indice = -1;
     if(returnResponse!=null) {
       String botReply = returnResponse.getQueryResult().getFulfillmentText();  // RISPOSTA DEL BOT
       if(!botReply.isEmpty()) {
         dialog_flow_response = returnResponse.getQueryResult().getParameters().toString();

         /* METODO 1 PER PRENDERE IL VALORE DALLA RISPOSTA DI DIALOGFLOW
         Pattern pattern = Pattern.compile("values[^s]+?string_value\\s?:\\s?\"(.*?)(?<!\\\\)\""); // PRENDERE SOLO IL VALORE DALLA RISPOSTA DI DIALOGFLOW GRAZIE AL REGEX
         Matcher matcher = pattern.matcher(pizza);
         while (matcher.find()) {
           String value = matcher.group(1);
           System.out.println(value);
         }*/

         Log.d("REPLY", "response: " + dialog_flow_response);
         // METODO 2 PER PRENDERE IL VALORE DALLA RISPOSTA DI DIALOGFLOW
         @SuppressLint({"NewApi", "LocalSuppress"}) List<String> string_values = Arrays.asList(dialog_flow_response.split("\n")).stream()
                 .filter(i -> i.contains("string_value:"))
                 .map(i -> i.substring(i.indexOf("string_values:") + 12).replace("\"", "").replace(":", "").replace(" ",""))  // +13 perchè string_values sono 13 caratteri
                 .collect(Collectors.toList());
         Log.d("REPLY", "ljsdgnklsadg: ");
         // CRASH
         //Log.d("REPLY", "callback: " + string_values.get(0));

         Log.d("CONTROLLO", "response = " + dialog_flow_response);
         if(string_values.size()>1){
           Log.d("CONTROLLO", "111111111111111111111111111");
           createReply(string_values.get(1));
         }else if(!string_values.isEmpty()){
           Log.d("CONTROLLO", "22222222222222222222222222222");
           createReply(string_values.get(0));
         }

         specialMap_ristoranti.put(position, restaurant_index);
         specialMap_itinerari.put(position, itinerari_indice);
         specialMap_spiagge.put(position, spiagge_indice);
         Log.d("TAG", "position : " + position + " restaurant_index: " + restaurant_index + " quindi specialMap " + position + ": " + specialMap_ristoranti.get(position));

         // salva la botreply nella lista di messaggi
         messageList.add(new Message(botReply, true));
         // aggiorna la chat
         chatAdapter.notifyDataSetChanged();
         chatAdapter.notifyItemChanged(position);   //  TENTATIVO FALLITO
         position++;
         Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);

         Log.d("TAG", botReply);


       }else {
         Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
       }
     }else {
       Toast.makeText(this, "failed to connect!", Toast.LENGTH_SHORT).show();
     }
  }

  // GENERAZIONE MANUALE DI UNA RISPOSTA DEL BOT
  private void createReply(String string_values) {
    String string = check(string_values);
    Log.d("REPLY", "Generazione risposta... con string: " + string);
      if (check_ristorante){
        Log.d("REPLY", "creo la reply dei ristoranti passando string = " + string);
        //special=true; //ora lo setto nella funzione query
        query_ristorante(string);  // prima lettera maiuscola...
        //specialMap.put(position, restaurant_index);
        Log.d("SIZES", "roba : " + specialMap_ristoranti.get(position));
        check_ristorante=false;
        //position++;
      }else if (check_itinerari) {
        Log.d("REPLY", "creo la reply degli itinerari");
        query_itinerario(string);
        check_itinerari=false;
      } else if(check_spiagge){
        query_spiaggia();
        check_spiagge = false;
        //specialMap.put(position, restaurant_index);
        Log.d("SIZES", "roba : " + specialMap_spiagge.get(spiagge_indice/10));
        check_spiagge= false;
        //position++;
      }
  }

  private String check(String string_values) {
    Log.d("REPLY", "string_value di partenza = " + string_values);
    string_values.toLowerCase();
    if (string_values.contains("pizza")||string_values.contains("Pizza")){
      string_values="Pizza";
      Log.d("REPLY", "contiene PIZZA");
      check_ristorante=true;
    }else if (string_values.contains("sushi")||string_values.contains("Sushi")){
      string_values="Sushi";
      Log.d("REPLY", "contiene SUSHI");
      check_ristorante=true;
    }else if (string_values.contains("italiana")||string_values.contains("Italiana")||string_values.contains("Italian")||string_values.contains("italian")) {
      string_values = "Italian";
      Log.d("REPLY", "contiene ITALIANA");
      check_ristorante = true;
    }else if (string_values.contains("cinese")||string_values.contains("Cinese")||string_values.contains("Chinese")||string_values.contains("chinese")){
      string_values="Chinese";
      Log.d("REPLY", "contiene CINESE");
      check_ristorante=true;
    }else if (string_values.contains("americana")||string_values.contains("Americana")||string_values.contains("American")||string_values.contains("american")){
      string_values="American";
      Log.d("REPLY", "contiene AMERICANA");
      check_ristorante=true;
    }else if (string_values.contains("grill")||string_values.contains("Grill")){
      string_values="Grill";
      Log.d("REPLY", "contiene GRILL");
      check_ristorante=true;
    }else if (string_values.contains("asiatica")||string_values.contains("Asiatica")||string_values.contains("Asian")||string_values.contains("asian")){
      string_values="Asian";
      Log.d("REPLY", "contiene ASIATICA");
      check_ristorante=true;
    }else if (string_values.contains("bar")||string_values.contains("Bar")){
      string_values="Bar";
      Log.d("REPLY", "contiene BAR");
      check_ristorante=true;
    }else if (string_values.contains("pub")||string_values.contains("Pub")){
      string_values="Pub";
      Log.d("REPLY", "contiene PUB");
      check_ristorante=true;
    }else if (string_values.contains("internazionale")||string_values.contains("Internazionale")||string_values.contains("International")||string_values.contains("international")){
      string_values="International";
      Log.d("REPLY", "contiene INTERNAZIONALE");
      check_ristorante=true;
    }else if (string_values.contains("barbecue")||string_values.contains("Barbecue")){
      string_values="Barbecue";
      Log.d("REPLY", "contiene BARBECUE");
      check_ristorante=true;
    }else if (string_values.contains("birreria")||string_values.contains("Birreria")||string_values.contains("Brewery")||string_values.contains("birra")||string_values.contains("Birra")||string_values.contains("beer")||string_values.contains("Beer")){
      string_values="Beer restaurants";
      Log.d("REPLY", "contiene BIRRERIA");
      check_ristorante=true;
    }else if (string_values.contains("mediterranea")||string_values.contains("Mediterranea")||string_values.contains("Mediterranean")||string_values.contains("mediterranean")){
      string_values="Mediterranean";
      Log.d("REPLY", "contiene MEDITERRANEA");
      check_ristorante=true;
    }else if (string_values.contains("caffè")||string_values.contains("Caffè")||string_values.contains("Cafe")||string_values.contains("cafe")){
      string_values="Cafe";
      Log.d("REPLY", "contiene CAFFÈ");
      check_ristorante=true;
    }else if (string_values.contains("pesce")||string_values.contains("Pesce")||string_values.contains("Seafood")||string_values.contains("seafood")||string_values.contains("Fish")||string_values.contains("fish")) {
      string_values = "Seafood";
      Log.d("REPLY", "contiene PESCE");
      check_ristorante = true;
    }else if (string_values.contains("Strada")||string_values.contains("strada")||string_values.contains("Street")||string_values.contains("street")){
        string_values="Street Food";
        Log.d("REPLY", "contiene CIBO DI STRADA");
      check_ristorante=true;
    }else if (string_values.contains("pugliese")||string_values.contains("Pugliese")||string_values.contains("Apulia")||string_values.contains("apulia")){
      string_values="Apulian";
      Log.d("REPLY", "contiene PUGLIESE");
      check_ristorante=true;
    }else if (string_values.contains("contemporanea")||string_values.contains("Contemporanea")||string_values.contains("Contemporanean")||string_values.contains("contemporanean")){
      string_values="Contemporary";
      Log.d("REPLY", "contiene CONTEMPORANEA");
      check_ristorante=true;
    }else if (string_values.contains("salutistica")||string_values.contains("Salutistica")||string_values.contains("salutare")||string_values.contains("Salutare")||string_values.contains("Healthy")||string_values.contains("healthy")){
      string_values="Healthy";
      Log.d("REPLY", "contiene SALUTISTICA");
      check_ristorante=true;
    }else if (string_values.contains("europea")||string_values.contains("Europea")||string_values.contains("European")||string_values.contains("european")){
      string_values="European";
      Log.d("REPLY", "contiene EUROPEA");
      check_ristorante=true;
    }else if (string_values.contains("spagnola")||string_values.contains("Spagnola")||string_values.contains("Spanish")||string_values.contains("spanish")){
      string_values="Spanish";
      Log.d("REPLY", "contiene SPAGNOLA");
      check_ristorante=true;
    }else if (string_values.contains("fast")||string_values.contains("Fast")){
      string_values="Fast Food";
      Log.d("REPLY", "contiene FAST FOOD");
      check_ristorante=true;
    }else if (string_values.contains("steakhouse")||string_values.contains("Steakhouse")||string_values.contains("Carne")||string_values.contains("carne")){
      string_values="Steakhouse";
      Log.d("REPLY", "contiene STEAKHOUSE");
      check_ristorante=true;
    }else if (string_values.contains("fusion")||string_values.contains("Fusion")){
      string_values="Fusion";
      Log.d("REPLY", "contiene FUSION");
      check_ristorante=true;
    }else if (string_values.contains("gastronom")||string_values.contains("Gastronom")){
      string_values="Deli";
      Log.d("REPLY", "contiene GASTRONOMIA");
      check_ristorante=true;
    }else if (string_values.contains("gastropub")||string_values.contains("Gastropub")){
      string_values="Gastropub";
      Log.d("REPLY", "contiene GASTROPUB");
      check_ristorante=true;
    }else if (string_values.contains("vino")||string_values.contains("Vino")||string_values.contains("Wine")||string_values.contains("wine")){
      string_values="Wine Bar";
      Log.d("REPLY", "contiene WINE BAR");
      check_ristorante=true;
    }else if (string_values.contains("zuppe")||string_values.contains("Zuppe")||string_values.contains("Soup")||string_values.contains("soup")){
      string_values="Soups";
      Log.d("REPLY", "contiene ZUPPE");
      check_ristorante=true;
    }else if (string_values.contains("giappo")||string_values.contains("Giappo")||string_values.contains("Japan")||string_values.contains("japan")){
      string_values="Japanese";
      Log.d("REPLY", "contiene GIAPPONESE");
      check_ristorante=true;
    }else if (string_values.contains("vegetarian")||string_values.contains("Vegetarian")){
      string_values="Vegetarian Friendly";
      Log.d("REPLY", "contiene VEGETARIAN");
      check_ristorante=true;
    }else if (string_values.contains("vegan")||string_values.contains("Vegan")){
      string_values="Vegan Options";
      Log.d("REPLY", "contiene VEGAN");
      check_ristorante=true;
    }else if (string_values.contains("glut")||string_values.contains("Glut")){
      string_values="Gluten Free Options";
      Log.d("REPLY", "contiene GLUT");
      check_ristorante=true;
    }else if (string_values.contains("sud")||string_values.contains("Sud")||string_values.contains("South")||string_values.contains("south")){
      string_values="Southern-Italian";
      Log.d("REPLY", "contiene SOUTH");
      check_ristorante=true;
    }else if(string_values.contains("mare")||string_values.contains(("Mare"))){
      string_values="Mare";
      Log.d("REPLY", "contiene MARE");
      check_itinerari=true;
    }else if(string_values.contains("storico")||string_values.contains(("Storico"))){
      string_values="Centro storico";
      Log.d("REPLY", "contiene CENTRO STORICO");
      check_itinerari=true;
    }else if (string_values.contains("campagna")||string_values.contains(("Campagna"))){
      string_values="Campagna";
      Log.d("REPLY", "contiene CAMPAGNA");
      check_itinerari=true;
    }else if (string_values.contains("rupestre")||string_values.contains(("Rupestre"))){
      string_values="Civiltà rupestre";
      Log.d("REPLY", "contiene CIVILTA RUPESTRE");
      check_itinerari=true;
    }else if(string_values.contains("spiaggia")||string_values.contains(("Spiaggia"))){
      string_values="Spiaggia";
      Log.d("REPLY", "contiene SPIAGGIA");
      check_spiagge=true;
    }
    Log.d("REPLY", "string_values modificata = " + string_values);
    return string_values;
  }

  @Override
  protected void onDestroy() {
    session.close();
    driver.close();
    Log.d("DDD", "onDestroy: ");
    super.onDestroy();
  }

  public static void query_ristorante(String tipo){ // GUIDA PER CONNETTERE AL DATABASE: link perso. (dovrebbe stare su heroku)

    Log.d("QUERY", "QUERY RISTORANTE");

    Result result = session.run("MATCH (n:Ristorante) WHERE \"" + tipo + "\" IN n.type RETURN n.name AS name, n.type AS type, n.rating AS rating, n.reviews AS reviews, n.phone AS phone, n.longitude AS longitude, n.latitude AS latitude, n.link AS link, n.address AS address");

    Log.d("RISTY", "MATCH (n:Ristorante) WHERE \"" + tipo +  "\" IN n.type RETURN n.name AS name, n.type AS type, n.rating AS rating, n.reviews AS reviews, n.phone AS phone, n.longitude AS longitude, n.latitude AS latitude, n.link AS link, n.address AS address");

    int n = 0; // contatore risultati trovati

    if(!tipo.isEmpty()){
      size_ristoranti = ristoranti.size();
      Log.d("RISTY", "ristoranti.size : " + ristoranti.size());
      while ( result.hasNext() )
      {
        Log.d("RISTY", "size: " + size_ristoranti + " n : " + (n+1));
        Record record = result.next();
        //risposta = risposta + "Nome:  " + record.get("name").asString() + "\nTipo:  " + record.get("type").asList() + "\nLink  " + record.get("link").asString() + "\nRecensioni:  " + record.get("reviews").asInt() + "\nRating:  " + record.get("rating").asFloat() + "\nTelefono:  " + record.get("phone").asString() + "\nIndirizzo:  " + record.get("address").asString() + "\nLatitudine:  " + record.get("latitude").asDouble() + "\nLongitudine:  " + record.get("longitude").asDouble() + "\n--------------------------------------\n\n";
        //Log.d("RISTY", risposta);

        ristorante = new Ristorante (record.get("name").asString(),
                record.get("link").asString(),
                record.get("phone").asString(),
                record.get("address").asString(),
                record.get("reviews").asInt(),
                record.get("rating").asFloat(),
                record.get("latitude").asDouble(),
                record.get("longitude").asDouble(),
                record.get("type").asList());

        ristoranti.add(ristorante);

        // STAMPA PROVA CON ITERATORE
        Iterator<Ristorante> iterator = ristoranti.iterator();
        Ristorante temp;
        int i=0;
        do{
          temp = iterator.next();
          i++;
        }while(i<=n);

        Log.d("RISTY", "Nome:  " + temp.nome);
        Log.d("RISTY", "Tipo:  " + temp.tipo.toString());
        Log.d("RISTY", "Link:  " + temp.link);
        Log.d("RISTY", "Recensioni:  " + temp.reviews);
        Log.d("RISTY", "Rating:  " + temp.rating);
        Log.d("RISTY", "Telefono:  " + temp.phone);
        Log.d("RISTY", "Indirizzo:  " + temp.address);
        Log.d("RISTY", "Latitudine:  " + temp.latitude);
        Log.d("RISTY", "Longitudine:  " + temp.longitude);
        Log.d("RISTY", "----------------------------------------------------------------------------------------------------");

        distanza_ristorante = new Distanza(n,temp.latitude,temp.longitude);
        distanze_ristoranti.add(distanza_ristorante);

        Log.d("RISTY", "latitudine salvata = " + distanza_ristorante.latitude );
        Log.d("RISTY", "longitudine salvata = " + distanza_ristorante.longitude);

        n++;
      }
    }

    Log.d("TAG", "size after: " + ristoranti.size());
    restaurant_index = size_ristoranti *10 + n; // facendo per 10 mi trovo la cifra dei ristoranti trovati con uno 0 alla fine, quindi sommo n per avere in un numero entrambi i valori. MA SE n>10?    ERRORE?
    Log.d("SIZES", "n : " + n);
    Log.d("SIZES", "restaurant / 10 : " + restaurant_index/10);
    Log.d("SIZES", "restaurant % 10 : " + restaurant_index%10);
  }

  public static void query_itinerario(String categoria){ // GUIDA PER CONNETTERE AL DATABASE: link perso. (dovrebbe stare su heroku)

    Log.d("ITTY", "QUERY ITINERARIO CON CATEGORIA = " + categoria);

    Result result = session.run("MATCH (n:Itinerario) WHERE \"" + categoria + "\" IN n.Categoria RETURN n.Titolo AS titolo, n.Link AS link, n.Descrizione AS descrizione, n.Categoria AS categoria, n.Mezzo AS mezzi, n.Durata AS durata, n.Immagine AS immagine, n.Long AS longitude, n.Lat AS latitude, n.Link_gmaps AS link_gmaps LIMIT 2");

    String test = "MATCH (n:Itinerario) WHERE \"" + categoria + "\" IN n.Categoria RETURN n.Titolo AS titolo, n.Link AS link, n.Descrizione AS descrizione, n.Categoria AS categoria, n.Mezzo AS mezzi, n.Durata AS durata, n.Immagine AS immagine, n.Long AS longitude, n.Lat AS latitude, n.Link_gmaps AS gmaps LIMIT 2";
    Log.d("ITTY", "STRINGA: " + test);

    int n = 0; // contatore risultati trovati

    size_itinerari = itinerari.size();
    Log.d("ITTY", "itinerari.size : " + itinerari.size());
    while ( result.hasNext() )
    {
      Log.d("ITTY", "size: " + size_itinerari + " n : " + (n+1));
      Record record = result.next();
      //risposta = risposta + "Nome:  " + record.get("name").asString() + "\nTipo:  " + record.get("type").asList() + "\nLink  " + record.get("link").asString() + "\nRecensioni:  " + record.get("reviews").asInt() + "\nRating:  " + record.get("rating").asFloat() + "\nTelefono:  " + record.get("phone").asString() + "\nIndirizzo:  " + record.get("address").asString() + "\nLatitudine:  " + record.get("latitude").asDouble() + "\nLongitudine:  " + record.get("longitude").asDouble() + "\n--------------------------------------\n\n";
      //Log.d("QUERY", risposta);

      Log.d("ITTY", "qui arrivo 1 ");
      Log.d("ITTY", "qui arrivo 2 " + record.get("titolo").asString());
      Log.d("ITTY", "qui arrivo 3");
      itinerario = new Itinerario (record.get("titolo").asString(),
              record.get("link").asString(),
              record.get("link_gmaps").asString(),
              record.get("descrizione").asString(),
              record.get("categoria").asString(),
              record.get("immagine").asString(),
              record.get("durata").asString(),
              record.get("latitude").asString(),
              record.get("longitude").asString(),
              record.get("mezzi").asString());

      Log.d("ITTY", "qui arrivo 4");

      itinerari.add(itinerario);

      // STAMPA PROVA CON ITERATORE
      Iterator<Itinerario> iterator = itinerari.iterator();
      Itinerario temp;
      int i=0;
      do{
        temp = iterator.next();
        i++;
      }while(i<=n);

      Log.d("ITTY", "Titolo:  " + temp.titolo);
      Log.d("ITTY", "Link:  " + temp.link);
      Log.d("ITTY", "Categoria:  " + temp.categoria);
      Log.d("ITTY", "Gmaps:  " + temp.link_gmaps);
      Log.d("ITTY", "Descrizione:  " + temp.descrizione);
      Log.d("ITTY", "Mezzi:  " + temp.mezzi);
      Log.d("ITTY", "Durata:  " + temp.durata);
      Log.d("ITTY", "Immagine:  " + temp.immagine);
      Log.d("ITTY", "Indirizzo:  " + temp.durata);
      Log.d("ITTY", "Latitudine:  " + temp.latitudine);
      Log.d("ITTY", "Longitudine:  " + temp.longitudine);
      Log.d("ITTY", "----------------------------------------------------------------------------------------------------");

      n++;
    }

    Log.d("ITTY", "size after: " + itinerari.size());
    itinerari_indice = size_itinerari *10 + n;
    Log.d("ITTY", "n : " + n);
    Log.d("ITTY", "itinerario / 10 : " + itinerari_indice/10);
    Log.d("ITTY", "itinerario % 10 : " + itinerari_indice%10);
  }

  public static void query_spiaggia(){ // GUIDA PER CONNETTERE AL DATABASE: link perso. (dovrebbe stare su heroku)

    Log.d("SPIA", "QUERY SPIAGGIA");

    Result result = session.run("MATCH (n:Spiaggia) RETURN n.Nome AS nome, n.Descrizione AS descrizione, n.Immagine AS immagine, n.Long AS longitude, n.Lat AS latitude, n.Link_gmaps AS link_gmaps LIMIT 2");

    String test = "MATCH (n:Spiaggia) RETURN n.Nome AS nome, n.Descrizione AS descrizione, n.Immagine AS immagine, n.Long AS longitude, n.Lat AS latitude, n.Link_gmaps AS link_gmaps LIMIT 2";
    Log.d("SPIA", "STRINGA: " + test);

    int n = 0; // contatore risultati trovati

    size_spiagge = spiagge.size();
    Log.d("SPIA", "spiagge.size : " + spiagge.size());
    while ( result.hasNext() )
    {
      Log.d("SPIA", "size: " + size_spiagge + " n : " + (n+1));
      Record record = result.next();
      //risposta = risposta + "Nome:  " + record.get("name").asString() + "\nTipo:  " + record.get("type").asList() + "\nLink  " + record.get("link").asString() + "\nRecensioni:  " + record.get("reviews").asInt() + "\nRating:  " + record.get("rating").asFloat() + "\nTelefono:  " + record.get("phone").asString() + "\nIndirizzo:  " + record.get("address").asString() + "\nLatitudine:  " + record.get("latitude").asDouble() + "\nLongitudine:  " + record.get("longitude").asDouble() + "\n--------------------------------------\n\n";
      //Log.d("QUERY", risposta);

      Log.d("SPIA", "qui arrivo davvero ");
      Log.d("SPIA", "qui arrivo.... " + record.get("nome").asString());
      spiaggia = new Spiaggia (record.get("nome").asString(),
              record.get("descrizione").asString(),
              record.get("link_gmaps").asString(),
              record.get("telefono").asString(),
              record.get("immagine").asString(),
              record.get("latitude").asString(),
              record.get("longitude").asString());

      Log.d("SPIA", "qui arrivo");

      spiagge.add(spiaggia);

      // STAMPA PROVA CON ITERATORE
      Iterator<Spiaggia> iterator = spiagge.iterator();
      Spiaggia temp;
      int i=0;
      do{
        temp = iterator.next();
        i++;
      }while(i<=n);

      Log.d("SPIA", "Titolo:  " + temp.nome);
      Log.d("SPIA", "Descrizione:  " + temp.descrizione);
      Log.d("SPIA", "Telefono:  " + temp.telefono);
      Log.d("SPIA", "Gmaps:  " + temp.link_gmaps);
      Log.d("SPIA", "Immagine:  " + temp.immagine);
      Log.d("SPIA", "Latitudine:  " + temp.latitudine);
      Log.d("SPIA", "Longitudine:  " + temp.longitudine);
      Log.d("SPIA", "----------------------------------------------------------------------------------------------------");

      n++;
    }

    Log.d("SPIA", "size after: " + spiagge.size());
    spiagge_indice = size_spiagge *10 + n;
    Log.d("SPIA", "n : " + n);
    Log.d("SPIA", "spiaggia / 10 : " + spiagge_indice/10);
    Log.d("SPIA", "spiaggia % 10 : " + spiagge_indice%10);
  }

  public static void check_distance(ArrayList<Distanza> distanze){   // creare 2 array: latitudine e longitudine. Durante la ricerca salvare in questi array e prima di stampare fare check_distance, per ottenere il i primi due luoghi più vicini.
    counter=0;
    closest_distance=999999999;
    Log.d("DISTANCE", "check_distance: ENTRO       distance.size = " + distanze.size());
    for (int i=0;i<distanze.size();i++){
      locationB.setLatitude(distanze.get(counter).latitude);
      locationB.setLongitude(distanze.get(counter).longitude);

      Log.d("DISTANCE", "check_distance: arrivo " + counter);
      Log.d("DISTANCE", "user_position.distanceTo(locationB)= " + user_position.distanceTo(locationB));
      Log.d("DISTANCE", "closest_distance = " + closest_distance);
      Log.d("DISTANCE", "second_closest_distance = " + second_closest_distance);

      if(user_position.distanceTo(locationB) < closest_distance){   // se l'ultima distanza calcolata è più piccola della attuale prima, sostituiscila e metti l'attuale prima nella seconda.
        Log.d("DISTANCE", "PRIMO IF");
        second_closest_distance=closest_distance;
        closest_distance=user_position.distanceTo(locationB);
        second_closest_position = closest_position;
        closest_position = counter;
      }else if (user_position.distanceTo(locationB) < second_closest_distance){   //  se invece è più grande o uguale dell'attuale prima, confrontala con la seconda e in caso fosse più piccola sostituiscila.
        Log.d("DISTANCE", "SECONDO IF \n-----------------------------------------------------------------------------");
        second_closest_distance=user_position.distanceTo(locationB);
        second_closest_position = counter;
      }
      counter++;
    }
    Log.d("NUOVO", "closest = " + closest_position + "   second = " + second_closest_position);
  }
}
