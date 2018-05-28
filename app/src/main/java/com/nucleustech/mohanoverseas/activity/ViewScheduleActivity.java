package com.nucleustech.mohanoverseas.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nucleustech.mohanoverseas.student.R;
import com.nucleustech.mohanoverseas.model.UserClass;
import com.nucleustech.mohanoverseas.util.Util;
import com.nucleustech.mohanoverseas.volley.ServerResponseCallback;
import com.nucleustech.mohanoverseas.volley.VolleyTaskManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ritwik.rai on 24/12/17.
 */

public class ViewScheduleActivity extends AppCompatActivity implements ServerResponseCallback {

    private TextView tv_scheduleChat_dateTime;
    private Context mContext;
    VolleyTaskManager volleyTaskManager;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    static int  changedYear, changedMonth, changedDay, changedHour, changedMinute, changedSecond;
    private boolean isScheduleService = false;
    Calendar newDate;
    Calendar newTime;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule);
        mContext = ViewScheduleActivity.this;
        dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        timeFormatter= new SimpleDateFormat("hh:mm:ss",Locale.US);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Scheduled Meet");
        tv_scheduleChat_dateTime = (TextView) findViewById(R.id.tv_scheduleChat_dateTime);

        volleyTaskManager = new VolleyTaskManager(mContext);

        Calendar newCalendar = Calendar.getInstance();
        UserClass userClass = Util.fetchUserClass(mContext);
        tv_scheduleChat_dateTime.setText(userClass.scheduledChatTimeStamp);
        //Toast.makeText(mContext,"Month: "+newCalendar.get(Calendar.MONTH),Toast.LENGTH_SHORT).show();
        //changedMonth= ;
        datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                changedYear = year;
                changedMonth = monthOfYear+1;
                changedDay = dayOfMonth;
                timePickerDialog.show();
                // tv_scheduleChat_dateTime.setText(dateFormatter.format(newDate.getTime()));

            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));


        timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        newTime= Calendar.getInstance();
                        newTime.set(changedYear,changedMonth,changedDay,hourOfDay,minute);
                        changedHour = hourOfDay;
                        changedMinute = minute;

                        //tv_scheduleChat_dateTime.setText(changedDay + "/" + changedMonth + "/" + changedYear + "    " + changedHour + ":" + changedMinute + ":" + "00");
                        tv_scheduleChat_dateTime.setText(dateFormatter.format(newDate.getTime())+"   "+timeFormatter.format(newTime.getTime() ));
                        tv_scheduleChat_dateTime.setClickable(false);
                        callScheduleChatService();
                    }
                }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("userID", "" + Util.fetchUserClass(mContext).getUserId());
        volleyTaskManager.doPostFetchSchedule(requestMap, true);
    }

    public void onRescheduleClick(View view) {
        setScheduleForAdminChat();
    }


    @Override
    public void onSuccess(JSONObject resultJsonObject) {

        if (isScheduleService) {
            isScheduleService = false;
            if (resultJsonObject.optString("code").trim().equalsIgnoreCase("200")) {
                Toast.makeText(mContext, "Admin Chat Session Scheduled.", Toast.LENGTH_LONG).show();
                UserClass userClass = Util.fetchUserClass(mContext);
                userClass.scheduledChatTimeStamp = tv_scheduleChat_dateTime.getText().toString().trim();
                Util.saveUserClass(mContext, userClass);
            }
            else if (resultJsonObject.optString("code").trim().equalsIgnoreCase("400")) {

                Util.showMessageWithOk(ViewScheduleActivity.this, "" + "" + resultJsonObject.optString("msg"));

            }
            else {
                Toast.makeText(mContext, "Something went wrong please try again.", Toast.LENGTH_LONG).show();
            }
        } else {
            if (resultJsonObject.optString("code").equalsIgnoreCase("200")) {
                try {
                    JSONArray schedules = resultJsonObject.optJSONArray("schedules");
                    for (int i = 0; i < schedules.length(); i++) {
                        JSONObject userSchedule = schedules.optJSONObject(i);
                        UserClass userClass = Util.fetchUserClass(mContext);
                        userClass.scheduledDate = userSchedule.optString("scheduleDate");
                        userClass.scheduledTime = userSchedule.optString("scheduleTime");
                        userClass.scheduledChatTimeStamp = userSchedule.optString("scheduleDate") + "   " + userSchedule.optString("scheduleTime");
                        Util.saveUserClass(mContext, userClass);
                    }
                    if(Util.fetchUserClass(mContext).scheduledChatTimeStamp==null ||Util.fetchUserClass(mContext).scheduledChatTimeStamp.trim().equalsIgnoreCase("")) {
                        tv_scheduleChat_dateTime.setText("SCHEDULE MEET");
                        tv_scheduleChat_dateTime.setClickable(true);
                        tv_scheduleChat_dateTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setScheduleForAdminChat();
                            }
                        });
                    }
                        else {
                        tv_scheduleChat_dateTime.setText("" + Util.fetchUserClass(mContext).scheduledChatTimeStamp);
                        tv_scheduleChat_dateTime.setClickable(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (Util.fetchUserClass(mContext).scheduledChatTimeStamp == null || !(Util.fetchUserClass(mContext).scheduledChatTimeStamp.trim().length() > 0)) {
                        tv_scheduleChat_dateTime.setText("SCHEDULE MEET");
                        tv_scheduleChat_dateTime.setClickable(true);
                        tv_scheduleChat_dateTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setScheduleForAdminChat();
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void onError() {

    }

    private void setScheduleForAdminChat() {

        datePickerDialog.show();
    }

    private void callScheduleChatService() {
//Toast.makeText(mContext,"Month: "+changedMonth,Toast.LENGTH_SHORT).show();
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("userID", "" + Util.fetchUserClass(mContext).getUserId());
        requestMap.put("scheduleDate", "" + changedDay + "/" + changedMonth + "/" + changedYear);
        requestMap.put("scheduleTime", "" + changedHour + ":" + changedMinute);
        isScheduleService = true;
        volleyTaskManager.doPostAdminChatSchedule(requestMap, true);
    }
    /**
     * Cancel scheduled Chat
     */
    public void onCancelScheduleClick(View view) {

        if(Util.fetchUserClass(mContext).scheduledChatTimeStamp!=null&& Util.fetchUserClass(mContext).scheduledChatTimeStamp.trim().length()>0) {

            final Dialog dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            dialog.setContentView(R.layout.dialog_cancel_meeting);
            dialog.setCancelable(true);
            Button btn_submit = (Button) dialog.findViewById(R.id.btn_submit);
            final TextView tv_meetDateTime = (TextView) dialog.findViewById(R.id.tv_meetDateTime);

            UserClass userClass = Util.fetchUserClass(mContext);
            tv_meetDateTime.setText(userClass.scheduledChatTimeStamp);

            btn_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    UserClass userClass = Util.fetchUserClass(mContext);
                    userClass.scheduledChatTimeStamp = "";
                    tv_scheduleChat_dateTime.setText("SCHEDULE MEET");
                    Util.saveUserClass(mContext, userClass);
                }
            });
            dialog.show();
        }

    }
}
