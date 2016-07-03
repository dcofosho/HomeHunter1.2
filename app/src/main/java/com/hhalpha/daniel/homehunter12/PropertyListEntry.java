package com.hhalpha.daniel.homehunter12;

import android.graphics.Bitmap;

/**
 * Created by Daniel on 6/20/2016.
 */
public class PropertyListEntry {
    private String text;
    private Bitmap pic;

    public PropertyListEntry(String text, Bitmap pic){
        this.text=text;
        this.pic=pic;
    }

    public String getPropertyText(){return text;}

    public void setPropertyText(String text){this.text=text;}

    public Bitmap getPic(){return pic;}

    public void setPic(Bitmap pic){
        this.pic=pic;
    }

}
