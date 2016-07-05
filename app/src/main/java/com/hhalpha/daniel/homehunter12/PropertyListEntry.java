package com.hhalpha.daniel.homehunter12;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Daniel on 6/20/2016.
 */
public class PropertyListEntry {

    private String text;
    private Bitmap pic;
    private Double distance;
    private Double propLat;
    private Double propLng;

    public PropertyListEntry(String text, Bitmap pic, Double distance, Double propLat,Double propLng){
        this.text=text;
        this.pic=pic;
        this.distance=distance;
        this.propLat=propLat;
        this.propLng=propLng;
    }

    public String getPropertyText(){return text;}

    public void setPropertyText(String text){this.text=text;}

    public Bitmap getPic(){return pic;}

    public void setPic(Bitmap pic){
        this.pic=pic;
    }

    public Double getDistance(){return distance;}

    public void setDistance(Double distance){
        this.distance=distance;
    }

    public Double getPropLat(){return propLat;}

    public void setPropLat(Double propLat){
        this.propLat=propLat;
    }

    public Double getPropLng(){return propLng;}

    public void setPropLng(Double propLng){
        this.propLng=propLng;
    }


}
