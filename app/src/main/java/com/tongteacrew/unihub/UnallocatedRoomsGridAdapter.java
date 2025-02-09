package com.tongteacrew.unihub;

import static java.lang.Math.ceil;
import static java.lang.Math.max;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class UnallocatedRoomsGridAdapter extends BaseAdapter {

    Context context;
    ArrayList<Map<String, Object>> times;
    LayoutInflater inflater;
    GridView gridView;
    ArrayList<Integer> rowHeights = new ArrayList<>();

    public UnallocatedRoomsGridAdapter(Context context, ArrayList<Map<String, Object>> times, GridView gridView) {
        this.context = context;
        this.times = times;
        this.inflater = LayoutInflater.from(context);
        this.gridView = gridView;
    }

    @Override
    public int getCount() {
        return times.size();
    }

    @Override
    public Object getItem(int i) {
        return times.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        if(view==null) {

            view = inflater.inflate(R.layout.card_unallocated_rooms, viewGroup, false);
            holder = new ViewHolder();
            holder.time = view.findViewById(R.id.time);
            holder.roomList = view.findViewById(R.id.room_list);
            holder.roomsLayout = view.findViewById(R.id.rooms_layout);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        Map<String, Object> time = times.get(i);
        holder.time.setText(time.keySet().iterator().next());
        holder.roomList.setText(String.valueOf(time.values().iterator().next()));

        gridView.post(new Runnable() {
            @Override
            public void run() {
                if(rowHeights.size()==0) {
                    getRowHeight();
                }
            }
        });

        holder.roomsLayout.post(new Runnable() {
            @Override
            public void run() {
                holder.roomsLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rowHeights.get(i/3)));
                gridView.requestLayout();
            }
        });

        return view;
    }

    void getRowHeight() {
        int totalGridLayoutHeight=0;
        for(int i=0; i<ceil(times.size()/3.0); i++) {
            int maxItemHeight=0;
            for(int j=0; j<3; j++) {
                if(gridView.getChildAt(i*3+j)!=null) {
                    maxItemHeight = max(maxItemHeight, gridView.getChildAt(i*3+j).getHeight());
                }
            }
            rowHeights.add(maxItemHeight);
            totalGridLayoutHeight += maxItemHeight;
        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = (int) (totalGridLayoutHeight+gridView.getVerticalSpacing()*(ceil(times.size()/3.0)-1));
        gridView.setLayoutParams(params);
        gridView.requestLayout();
    }

    static class ViewHolder {
        TextView time, roomList;
        RelativeLayout roomsLayout;
    }
}
