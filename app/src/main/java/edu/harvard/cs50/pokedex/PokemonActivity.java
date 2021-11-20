package edu.harvard.cs50.pokedex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private RequestQueue requestQueue;
    private String pokemon_name;
    private Button button;
    private ImageView sprite;
    private String sprite_url;
    private String descriptiontext;
    private TextView description;
    private boolean pokemon_state;



    public PokemonActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon2);


        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        pokemon_name = getIntent().getStringExtra("name").toLowerCase();
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        button = findViewById(R.id.button);
        sprite = findViewById(R.id.sprite);
        description = findViewById(R.id.description);

        load();
        loaddescription();
    }
    public void load(){
        pokemon_state = getPreferences(Context.MODE_PRIVATE).getBoolean(pokemon_name, false);
        if (pokemon_state == true){
            button.setText("Release");
        }
        else{
            button.setText("Catch");
        }
        type1TextView.setText("");
        type2TextView.setText("");
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name = response.getString("name");
                    nameTextView.setText(name.substring(0,1).toUpperCase() + name.substring(1));
                    numberTextView.setText(String.format("#%03d",response.getInt("id")));
                    JSONArray typeEntries = response.getJSONArray("types");
                    JSONObject sprites = response.getJSONObject("sprites");
                    sprite_url = sprites.getString("front_default");
                    new DownloadSpriteTask().execute(sprite_url);
                    for (int i = 0; i < typeEntries.length();i++){
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1){
                            type1TextView.setText(type.substring(0,1).toUpperCase() + type.substring(1));
                        }
                        else if (slot == 2){
                            type2TextView.setText(type.substring(0,1).toUpperCase() + type.substring(1));
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50","Pokemon details error");
            }
        });
        requestQueue.add(request);

    }

    public void loaddescription(){
        description.setText("");
        String url = "https://pokeapi.co/api/v2/pokemon-species/" + pokemon_name + "/";
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray flavour_text_entries = response.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < flavour_text_entries.length(); i++){
                        JSONObject texts = flavour_text_entries.getJSONObject(i);
                        JSONObject language = texts.getJSONObject("language");
                        String languagename = language.getString("name");
                        if (languagename.equals("en")){
                            descriptiontext = texts.getString("flavor_text");
                            description.setText(descriptiontext);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon-species json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50","Pokemon-species details error");
            }
        });
        requestQueue.add(request);

    }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            sprite.setImageBitmap(bitmap);
        }
    }


    public  void  toggleCatch(View view){
        pokemon_state = getPreferences(Context.MODE_PRIVATE).getBoolean(pokemon_name, false);
        if (pokemon_state == true){
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(pokemon_name,false).commit();
            button.setText("Catch");
        } else {
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(pokemon_name,true).commit();
            button.setText("Release");
        }
    }
}
