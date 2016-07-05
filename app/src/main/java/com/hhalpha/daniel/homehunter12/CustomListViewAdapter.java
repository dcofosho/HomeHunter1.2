package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Daniel on 6/20/2016.
 */
public class CustomListViewAdapter extends ArrayAdapter<PropertyListEntry> {
    Context context;
    public CustomListViewAdapter(Context context, int resourceId, List<PropertyListEntry> items){
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder{
        TextView textViewProp;
        ImageView imageViewProp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        PropertyListEntry propertyListEntry = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // if the view is not created, create it
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_layout1,
                    null); // inflate the layout programmatically to create in memory model
            holder = new ViewHolder();
            holder.imageViewProp = (ImageView) convertView.findViewById(R.id.imageViewProp);
            holder.textViewProp = (TextView) convertView.findViewById(R.id.textViewProp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag(); // when view is getting reused
        }


        holder.textViewProp.setText(propertyListEntry.getPropertyText()+propertyListEntry.getDistance());
        holder.imageViewProp.setImageBitmap(propertyListEntry.getPic());

        return convertView;
    }
}
