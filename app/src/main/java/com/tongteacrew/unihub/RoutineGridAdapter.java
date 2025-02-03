package com.tongteacrew.unihub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class RoutineGridAdapter extends BaseAdapter {

    Context context;
    ArrayList<Map<String, Object>> routine;
    LayoutInflater inflater;

    public RoutineGridAdapter(Context context, ArrayList<Map<String, Object>> routine) {
        this.context = context;
        this.routine = routine;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return routine.size();
    }

    @Override
    public Object getItem(int position) {
        return routine.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView==null) {

            convertView = inflater.inflate(R.layout.card_routine_course, parent, false);
            holder = new ViewHolder();
            holder.title1 = convertView.findViewById(R.id.title_1);
            holder.title2 = convertView.findViewById(R.id.title_2);
            holder.title3 = convertView.findViewById(R.id.title_3);
            holder.title4 = convertView.findViewById(R.id.title_4);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, Object> courseData = routine.get(position);
        String thirdTitle = "";

        if(courseData.containsKey("title5")) {
            thirdTitle = String.valueOf(courseData.get("title3"))+" "+String.valueOf(courseData.get("title4"));
            holder.title1.setText((String) courseData.get("title1"));
            holder.title2.setText((String) courseData.get("title2"));
            holder.title3.setText((String) thirdTitle);
            holder.title4.setText((String) courseData.get("title5"));
        }
        else {
            holder.title1.setText((String) courseData.get("title1"));
            holder.title2.setText((String) courseData.get("title2"));
            holder.title3.setText((String) courseData.get("title3"));
            holder.title4.setText((String) courseData.get("title4"));
        }

        return convertView;
    }

    static class ViewHolder {
        TextView title1, title2, title3, title4;
    }
}