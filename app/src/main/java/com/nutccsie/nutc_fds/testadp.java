package com.nutccsie.nutc_fds;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by user on 2016/5/12.
 */
public class testadp extends ArrayAdapter<String> {

    private List<String> mWeights ;
    private int yellow_warn=0;
    private int red_warn=0;


    public testadp(Context context, List<String> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        mWeights  = objects;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        Log.d("listvalue",mWeights.get(position).substring(mWeights.get(position).length()-4,mWeights.get(position).length()-1));
        int itemWeight = Integer.parseInt(mWeights.get(position).substring(mWeights.get(position).length()-4,mWeights.get(position).length()-1).trim());
        v.setBackgroundColor(Color.rgb(0,255,0));
        if (itemWeight<=yellow_warn){
            v.setBackgroundColor(Color.rgb(255,200,140));
        }
        if (itemWeight<=red_warn){
            v.setBackgroundColor(Color.rgb(255,0,0));
        }
        return v;
    }

    public void setYellowWarnValue(int value){
        yellow_warn=value;
    }
    public void setRedWarnValue(int value){
        red_warn=value;
    }
}
