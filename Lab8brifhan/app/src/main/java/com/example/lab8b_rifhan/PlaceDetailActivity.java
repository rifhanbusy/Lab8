package com.example.lab8b_rifhan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaceDetailActivity extends AppCompatActivity {

    private ImageView imagePlace;
    private TextView txtName, txtAddress, txtPhone, txtRating, txtWebsite, txtOpenNow;
    private static final String API_KEY = "AIzaSyDr64tr-Y3YopYDi7PmbUou96Q0o3wSYlI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        // Bind Views
        imagePlace = findViewById(R.id.imagePlace);
        txtName = findViewById(R.id.txtName);
        txtAddress = findViewById(R.id.txtAddress);
        txtPhone = findViewById(R.id.txtPhone);
        txtRating = findViewById(R.id.txtRating);
        txtWebsite = findViewById(R.id.txtWebsite);
        txtOpenNow = findViewById(R.id.txtOpenNow);

        // Get Place ID from Intent
        String placeId = getIntent().getStringExtra("place_id");
        if (placeId != null) {
            fetchPlaceDetails(placeId);
        } else {
            Toast.makeText(this, "Error: Place ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchPlaceDetails(String placeId) {
        // Build the URL for the Place Details API call
        String url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=" + placeId + "&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");

                        // Extract text data
                        String name = result.optString("name", "N/A");
                        String address = result.optString("formatted_address", "N/A");
                        String phone = result.optString("formatted_phone_number", "N/A");
                        double rating = result.optDouble("rating", 0.0);
                        String website = result.optString("website", "N/A");

                        txtName.setText(name);
                        txtAddress.setText(address);
                        txtPhone.setText("Phone: " + phone);
                        txtRating.setText("Rating: " + rating);
                        txtWebsite.setText("Website: " + website);

                        // Handle Opening Hours
                        if (result.has("opening_hours")) {
                            boolean openNow = result.getJSONObject("opening_hours").optBoolean("open_now");
                            txtOpenNow.setText(openNow ? "Open Now" : "Closed");
                        } else {
                            txtOpenNow.setText("Opening hours not available");
                        }

                        // Handle Photo loading using Glide
                        if (result.has("photos")) {
                            String photoReference = result.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
                            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + API_KEY;
                            Glide.with(this).load(photoUrl).into(imagePlace);
                        }

                        // Make elements clickable
                        txtPhone.setOnClickListener(v -> {
                            if (!phone.equals("N/A")) {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                                startActivity(intent);
                            }
                        });

                        txtWebsite.setOnClickListener(v -> {
                            if (!website.equals("N/A")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                                startActivity(intent);
                            }
                        });

                        txtAddress.setOnClickListener(v -> {
                            if (result.has("geometry")) {
                                // Added the try-catch block here!
                                try {
                                    JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                                    String lat = location.getString("lat");
                                    String lng = location.getString("lng");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + lat + "," + lng));
                                    intent.setPackage("com.google.android.apps.maps");
                                    startActivity(intent);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing details", Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show());
        queue.add(request);
    }
}