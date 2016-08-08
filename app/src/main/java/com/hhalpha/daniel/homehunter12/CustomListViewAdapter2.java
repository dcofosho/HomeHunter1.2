package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Daniel on 7/20/2016.
 */
public class CustomListViewAdapter2 extends ArrayAdapter<String> {
    Context context;
    public CustomListViewAdapter2(Context context, int resourceId, List<String> items){
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder{
        TextView textViewTime;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        String string = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // if the view is not created, create it
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_layout2,
                    null); // inflate the layout programmatically to create in memory model
            holder = new ViewHolder();
            holder.textViewTime = (TextView) convertView.findViewById(R.id.textViewTimeEntry);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag(); // when view is getting reused
        }


        holder.textViewTime.setText(string.replace("[","").replace("]",""));


        return convertView;
    }
}
