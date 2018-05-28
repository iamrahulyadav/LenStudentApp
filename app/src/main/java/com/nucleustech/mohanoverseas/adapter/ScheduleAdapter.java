package com.nucleustech.mohanoverseas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nucleustech.mohanoverseas.model.Schedule;
import com.nucleustech.mohanoverseas.student.R;

import java.util.ArrayList;

/**
 * Created by ritwik.rai on 24/12/17.
 */

public class ScheduleAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Schedule> courses = new ArrayList<>();


    public ScheduleAdapter(Context mContext, ArrayList<Schedule>  cityMap) {
        this.mContext = mContext;
        this.courses = cityMap;
        mInflater = LayoutInflater.from(mContext);

    }

    @Override
    public int getCount() {
        return courses.size();
    }

    @Override
    public Object getItem(int position) {
        return courses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View hView = convertView;
        if (convertView == null) {
            hView = mInflater.inflate(R.layout.item_view_schedules, null);
            ScheduleAdapter.ViewHolder holder = new ScheduleAdapter.ViewHolder();
            holder.tv_scheduleChat_dateTime = (TextView) hView.findViewById(R.id.tv_scheduleChat_dateTime);
            holder.tv_reschedule = (TextView) hView.findViewById(R.id.tv_reschedule);
            holder.tv_cancel = (TextView) hView.findViewById(R.id.tv_cancel);
            holder.tv_studentName=(TextView) hView.findViewById(R.id.tv_studentName);
            hView.setTag(holder);
        }

        ScheduleAdapter.ViewHolder holder = (ScheduleAdapter.ViewHolder) hView.getTag();
        holder.tv_scheduleChat_dateTime.setText(courses.get( position).scheduledDate+"   "+courses.get( position).scheduledTime);
        holder.tv_studentName.setText(courses.get(position).name);
        return hView;
    }


    class ViewHolder {
        TextView tv_scheduleChat_dateTime,tv_reschedule,tv_cancel,tv_studentName;

    }
}
