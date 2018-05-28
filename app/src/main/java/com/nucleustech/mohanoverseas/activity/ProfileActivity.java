package com.nucleustech.mohanoverseas.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nucleustech.mohanoverseas.student.R;
import com.nucleustech.mohanoverseas.adapter.CircleTransform;
import com.nucleustech.mohanoverseas.adapter.SuggestedCollegesAdapter;
import com.nucleustech.mohanoverseas.constant.Consts;
import com.nucleustech.mohanoverseas.model.FileDetails;
import com.nucleustech.mohanoverseas.model.University;
import com.nucleustech.mohanoverseas.model.UserClass;
import com.nucleustech.mohanoverseas.util.FileOpen;
import com.nucleustech.mohanoverseas.util.FilePath;
import com.nucleustech.mohanoverseas.util.Util;
import com.nucleustech.mohanoverseas.volley.MultipartPostCallback;
import com.nucleustech.mohanoverseas.volley.MultipartPostRequest;
import com.nucleustech.mohanoverseas.volley.ServerResponseCallback;
import com.nucleustech.mohanoverseas.volley.VolleyTaskManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ritwik.rai on 09/12/17.
 */

public class ProfileActivity extends AppCompatActivity implements ServerResponseCallback, MultipartPostCallback {

    private Context mContext;
    private VolleyTaskManager volleyTaskManager;
    TextView name, email, tv_phone, tv_aboutMe, tv_toeflScore, tv_greScore;
    ImageView image;
    private boolean isFetchProfileService = false;
    private boolean isFetchSuggestedColleges = false;
    private boolean isFetchAppliedColleges = false;
    private boolean isUploadCVService = false;
    private boolean isDownloadCVService = false;
    private boolean isUpdateUserService = false;
    private boolean isScheduleService = false;
    TextView tv_scheduleChat_dateTime;
    private static final int GALLERY_INTENT_CODE = 987;
    private ProgressDialog pDialog;
    private String storagePath = "";
    private FileDetails uploadedFile;

    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    int changedYear, changedMonth, changedDay, changedHour, changedMinute, changedSecond;
    private RecyclerView recyclerAppliedUniversities, recyclerSuggestedUniversities;
    private ArrayList<University> universities;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mContext = ProfileActivity.this;
        dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        timeFormatter = new SimpleDateFormat("hh:mm:ss", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Profile");

        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        image = (ImageView) findViewById(R.id.iv_userProfile);
        tv_aboutMe = (TextView) findViewById(R.id.tv_aboutMe);
        tv_greScore = (TextView) findViewById(R.id.tv_greScore);
        tv_toeflScore = (TextView) findViewById(R.id.tv_toeflScore);
        tv_scheduleChat_dateTime = (TextView) findViewById(R.id.tv_scheduleChat_dateTime);
        recyclerSuggestedUniversities = (RecyclerView) findViewById(R.id.recyclerSuggestedUniversities);
        recyclerAppliedUniversities = (RecyclerView) findViewById(R.id.recyclerAppliedUniversities);

        volleyTaskManager = new VolleyTaskManager(mContext);


        UserClass userClass = Util.fetchUserClass(mContext);
        if (userClass != null) {
            name.setText("" + userClass.displayName);
            email.setText("" + userClass.getEmail());
            tv_phone.setText("" + userClass.getPhone());
            Glide.with(image.getContext()).load(userClass.profileUrl).centerCrop().transform(new CircleTransform(image.getContext())).override(50, 50).into(image);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("userID", "" + userClass.getUserId());

            isFetchProfileService = true;
            volleyTaskManager.doPostFetchProfile(hashMap, true);
        }

        datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                changedYear = year;
                changedMonth = monthOfYear;
                changedDay = dayOfMonth;
                timePickerDialog.show();
                // tv_scheduleChat_dateTime.setText(dateFormatter.format(newDate.getTime()));

            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));


        timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        newTime = Calendar.getInstance();
                        newTime.set(changedYear, changedMonth, changedDay, hourOfDay, minute);
                        changedHour = hourOfDay;
                        changedMinute = minute;

                        //tv_scheduleChat_dateTime.setText(changedDay + "/" + changedMonth + "/" + changedYear + "    " + changedHour + ":" + changedMinute + ":" + "00");

                        tv_scheduleChat_dateTime.setText(dateFormatter.format(newDate.getTime()) + "   " + timeFormatter.format(newTime.getTime()));

                        tv_scheduleChat_dateTime.setClickable(false);
                        callScheduleChatService();
                    }
                }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);


    }

    @Override
    public void onSuccess(JSONObject resultJsonObject) {

        if (isFetchProfileService) {
            isFetchProfileService = false;
            Log.e("FetchProfile", "Response: " + resultJsonObject);
            if (resultJsonObject.optString("code").trim().equalsIgnoreCase("200")) {

                JSONObject jsonObject = resultJsonObject.optJSONObject("userData");
                UserClass userClass = Util.fetchUserClass(mContext);
                if (userClass == null) {
                    userClass = new UserClass();
                }
                userClass.setUserId("" + jsonObject.optString("userID"));
                // userClass.displayName = "" + jsonObject.optString("name");
                //userClass.setEmail("" + jsonObject.optString("emailID"));
                userClass.aboutMe = "" + jsonObject.optString("aboutMe");
                userClass.greScore = "" + jsonObject.optString("greScore");
                userClass.toeflScore = "" + jsonObject.optString("toeflScore");
                userClass.uplodedCVUrl = "" + jsonObject.optString("uploadedCV");


                ArrayList<University> appliedUniversities = new ArrayList<>();
                ArrayList<University> suggestedUniversities = new ArrayList<>();

                userClass.appliedUniversities = appliedUniversities;
                userClass.suggestedUniversities = suggestedUniversities;
                Util.saveUserClass(mContext, userClass);
                String aboutMeText = "" + jsonObject.optString("aboutMe");
                if (aboutMeText != null && !(aboutMeText.equalsIgnoreCase("null")))
                    tv_aboutMe.setText(aboutMeText);

                if (!userClass.greScore.trim().isEmpty() && userClass.greScore.length() > 0) {
                    tv_greScore.setText(userClass.greScore);
                    tv_greScore.setClickable(false);

                } else if (userClass.greScore.trim().isEmpty() && !(userClass.greScore.length() > 0)) {
                    tv_greScore.setText("ADD SCORE");
                    tv_greScore.setClickable(true);
                    tv_greScore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onEditTestScoresClick(view);
                        }
                    });
                }
                if (!userClass.toeflScore.trim().isEmpty() && userClass.toeflScore.length() > 0) {
                    tv_toeflScore.setText(userClass.toeflScore);
                    tv_toeflScore.setClickable(false);
                } else if (userClass.toeflScore.trim().isEmpty() && !(userClass.toeflScore.length() > 0)) {
                    tv_toeflScore.setText("ADD SCORE");
                    tv_toeflScore.setClickable(true);
                    tv_toeflScore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onEditTestScoresClick(view);
                        }
                    });
                }
                if (userClass.scheduledChatTimeStamp == null || (userClass.scheduledChatTimeStamp.trim().isEmpty() && !(userClass.scheduledChatTimeStamp.trim().length() > 0))) {
                    tv_scheduleChat_dateTime.setText("SCHEDULE MEET");
                    tv_scheduleChat_dateTime.setClickable(true);
                    tv_scheduleChat_dateTime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setScheduleForAdminChat();
                        }
                    });
                } else if (!userClass.scheduledChatTimeStamp.isEmpty() && userClass.scheduledChatTimeStamp.length() > 0) {
                    tv_scheduleChat_dateTime.setText(userClass.scheduledChatTimeStamp);
                    tv_scheduleChat_dateTime.setClickable(false);
                }

                fetchAppliedColleges();


            }
        } else if (isUpdateUserService) {
            isUpdateUserService = false;
            Log.e("UpdateProfile", "Response: " + resultJsonObject);
            if (resultJsonObject.optString("code").trim().equalsIgnoreCase("200")) {
                JSONObject jsonObject = resultJsonObject.optJSONObject("userData");
                UserClass userClass = Util.fetchUserClass(mContext);
                if (userClass == null) {
                    userClass = new UserClass();
                }
                userClass.aboutMe = "" + jsonObject.optString("aboutMe");
                userClass.greScore = "" + jsonObject.optString("greScore");
                userClass.toeflScore = "" + jsonObject.optString("toeflScore");
                Util.saveUserClass(mContext, userClass);

                if (!userClass.greScore.trim().isEmpty() && userClass.greScore.length() > 0) {
                    tv_greScore.setText(userClass.greScore);
                    tv_greScore.setClickable(false);


                } else if (userClass.greScore.trim().isEmpty() && !(userClass.greScore.length() > 0)) {
                    tv_greScore.setText("ADD SCORE");
                }
                if (!userClass.toeflScore.trim().isEmpty() && userClass.toeflScore.length() > 0) {
                    tv_toeflScore.setText(userClass.toeflScore);
                    tv_toeflScore.setClickable(false);


                } else if (userClass.toeflScore.trim().isEmpty() && !(userClass.toeflScore.length() > 0)) {
                    tv_toeflScore.setText("ADD SCORE");
                }
                String aboutMeText = userClass.aboutMe;
                if (aboutMeText != null && !(aboutMeText.equalsIgnoreCase("null")))
                    tv_aboutMe.setText(userClass.aboutMe);
            }


        } else if (isScheduleService) {
            isScheduleService = false;
            if (resultJsonObject.optString("code").trim().equalsIgnoreCase("200")) {
                Toast.makeText(mContext, "Admin Chat Session Scheduled.", Toast.LENGTH_LONG).show();
                UserClass userClass = Util.fetchUserClass(mContext);
                userClass.scheduledChatTimeStamp = tv_scheduleChat_dateTime.getText().toString().trim();
                Util.saveUserClass(mContext, userClass);
            } else if (resultJsonObject.optString("code").trim().equalsIgnoreCase("400")) {

                Util.showMessageWithOk(ProfileActivity.this, "" + "" + resultJsonObject.optString("msg"));

            } else {
                Toast.makeText(mContext, "Something went wrong please try again.", Toast.LENGTH_LONG).show();
            }
        } else if (isFetchSuggestedColleges) {
            isFetchSuggestedColleges = false;

            if (resultJsonObject.optString("code").trim().equalsIgnoreCase("200")) {
                universities = new ArrayList<>();
                JSONArray universitiesArray = resultJsonObject.optJSONArray("universities");
                for (int i = 0; i < universitiesArray.length(); i++) {
                    JSONObject jsonObject = universitiesArray.optJSONObject(i);
                    University university = new University();
                    university.universityId = jsonObject.optString("universityID");
                    university.universityName = jsonObject.optString("universityName");
                    university.address = jsonObject.optString("universityAddress");
                    universities.add(university);
                }

                // SET SUGGESTED UNIVERSITY ADAPTER
                SuggestedCollegesAdapter adapter = new SuggestedCollegesAdapter(mContext, universities);
                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerSuggestedUniversities.setLayoutManager(llm);
                //list.setAdapter( adapter );
                recyclerSuggestedUniversities.setAdapter(adapter);

                adapter.setOnItemClickListener(new SuggestedCollegesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, University obj, int position) {
                        //Toast.makeText(mContext, "Position: " + position, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, UniversityActivity.class);
                        intent.putExtra("universityID", obj.universityId);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        } else if (isFetchAppliedColleges) {
            isFetchAppliedColleges = false;
            if (resultJsonObject.optString("code").trim().equalsIgnoreCase("200")) {
                universities = new ArrayList<>();
                JSONArray universitiesArray = resultJsonObject.optJSONArray("universities");
                for (int i = 0; i < universitiesArray.length(); i++) {
                    JSONObject jsonObject = universitiesArray.optJSONObject(i);
                    University university = new University();
                    university.universityId = jsonObject.optString("universityID");
                    university.universityName = jsonObject.optString("universityName");
                    university.address = jsonObject.optString("universityAddress");
                    universities.add(university);
                }

                // SET SUGGESTED UNIVERSITY ADAPTER
                SuggestedCollegesAdapter adapter = new SuggestedCollegesAdapter(mContext, universities);
                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerAppliedUniversities.setLayoutManager(llm);
                //list.setAdapter( adapter );
                recyclerAppliedUniversities.setAdapter(adapter);

                adapter.setOnItemClickListener(new SuggestedCollegesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, University obj, int position) {
                        //Toast.makeText(mContext, "Position: " + position, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, UniversityActivity.class);
                        intent.putExtra("universityID", obj.universityId);
                        startActivity(intent);
                        finish();
                    }
                });


            }
            fetchSuggestedColleges();

        } else if (isUploadCVService) {
            isUploadCVService = false;

        } else if (isDownloadCVService) {
            isDownloadCVService = false;

        }
    }

    private void fetchAppliedColleges() {
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("userID", "" + Util.fetchUserClass(mContext).getUserId());
        isFetchAppliedColleges = true;
        volleyTaskManager.doPostFetchAppliedColleges(requestMap, true);
    }


    @Override
    public void onError() {

    }

    private void fetchSuggestedColleges() {
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("userID", "" + Util.fetchUserClass(mContext).getUserId());
        isFetchSuggestedColleges = true;
        volleyTaskManager.doPostFetchSuggestedColleges(requestMap, true);

    }

    public void onEditAboutMeClick(View view) {

        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_edit_aboutme);
        dialog.setTitle("About Me");
        dialog.setCancelable(false);
        Button btn_submit = (Button) dialog.findViewById(R.id.btn_submit);
        final EditText et_aboutMe = (EditText) dialog.findViewById(R.id.et_aboutMe);
        UserClass userClass = Util.fetchUserClass(mContext);
        et_aboutMe.setText("" + userClass.aboutMe);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aboutMe = et_aboutMe.getText().toString().trim();
                dialog.dismiss();
                UserClass userClass = Util.fetchUserClass(mContext);
                userClass.aboutMe = aboutMe;

                updateUser(userClass);
            }
        });
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void updateUser(UserClass userClass) {

        HashMap<String, String> requestMap = new HashMap<>();

        requestMap.put("userID", "" + userClass.getUserId());
        requestMap.put("about", "" + userClass.aboutMe);
        requestMap.put("gre", "" + userClass.greScore);
        requestMap.put("toefl", "" + userClass.toeflScore);

        isUpdateUserService = true;
        volleyTaskManager.doPostUpdateProfile(requestMap, true);

    }

    public void onEditTestScoresClick(View view) {

        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_edit_testscore);
        dialog.setCancelable(false);
        dialog.setTitle("Test Scores");
        Button btn_submit = (Button) dialog.findViewById(R.id.btn_submit);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);

        final EditText et_toeflScore = (EditText) dialog.findViewById(R.id.et_toeflScore);
        final EditText et_greScore = (EditText) dialog.findViewById(R.id.et_greScore);
        UserClass userClass = Util.fetchUserClass(mContext);
        et_greScore.setText(userClass.greScore);
        et_toeflScore.setText(userClass.toeflScore);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gre = et_greScore.getText().toString().trim();
                String toeflScore = et_toeflScore.getText().toString().trim();
                /*if (TextUtils.isEmpty(aboutMe)) {
                    Toast.makeText(OTPActivity.this, "Please enter the OTP.", Toast.LENGTH_SHORT).show();
                } else if (receiverOTP.length() < 6) {
                    Toast.makeText(OTPActivity.this, "Please enter the correct OTP.", Toast.LENGTH_SHORT).show();
                } else if (!receiverOTP.equalsIgnoreCase(otpp)) {
                    Toast.makeText(OTPActivity.this, "Please enter the correct OTP.", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    gotoNextActivity();
                }*/
                dialog.dismiss();
                UserClass userClass = Util.fetchUserClass(mContext);
                userClass.toeflScore = toeflScore;
                userClass.greScore = gre;
                updateUser(userClass);
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //Download Previous uploaded cv
    public void onDownloadResumeClick(View view) {

        //Some data could not be loaded please check your internet and try again.
        new DownloadFileFromURL().execute(Util.fetchUserClass(mContext).uplodedCVUrl);

    }

    //Upload a new cv
    public void onUploadResumeClick(View view) {
        System.out.println(Build.VERSION.SDK_INT);
        Intent galleryintent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            galleryintent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            galleryintent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            galleryintent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        galleryintent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        galleryintent.setType("*/*");
        startActivityForResult(galleryintent, GALLERY_INTENT_CODE);

    }

    /**
     * Reschedule Chat Session
     */
    public void onRescheduleClick(View view) {
        setScheduleForAdminChat();

    }

    /**
     * Cancel scheduled Chat
     */
    public void onCancelScheduleClick(View view) {

        if (Util.fetchUserClass(mContext).scheduledChatTimeStamp != null && Util.fetchUserClass(mContext).scheduledChatTimeStamp.trim().length() > 0) {

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
                    //TODO CANCEL MEETING (Not Need- shared preference syncs itself, it will just be overhead.
                    /*HashMap<String,String> requestMap= new HashMap<>();
                    requestMap.put("meetingID",""+schedule.meetingId);
                    isCancelMeetingSchedule= true;
                    volleyTaskManager.doPostCancelSchedule(requestMap,true);*/


                    UserClass userClass = Util.fetchUserClass(mContext);
                    userClass.scheduledChatTimeStamp = "";
                    tv_scheduleChat_dateTime.setText("SCHEDULE MEET");

                    Util.saveUserClass(mContext, userClass);
                }
            });
            dialog.show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GALLERY_INTENT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri selectedFileUri = data.getData();
                    String selectedFilePath = FilePath.getPath(this, selectedFileUri);
                    Uri uri = data.getData();
                    int actionType = Util.checkFileExtention(mContext, uri);
                    Log.e(" ", "uri" + uri);
                    String uriString = uri.toString();
                    File myFile = new File(uriString);

                    Log.e(" ", "uri" + uri);
                    String displayName = null;
                    FileDetails file = null;
                    // int sizeIndex = 0;
                    try {
                        InputStream input = mContext.getContentResolver().openInputStream(uri);
                        if (uriString.startsWith("content://")) {
                            Cursor cursor = null;
                            try {
                                cursor = getContentResolver().query(uri, null, null, null, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                    // sizeIndex =
                                    // cursor.getColumnIndex(OpenableColumns.SIZE);
                                }
                            } finally {
                                cursor.close();
                            }
                        } else if (uriString.startsWith("file://")) {
                            displayName = myFile.getName();
                        }

                        file = new FileDetails();
                        file.setFileName(displayName);
                        file.setFilePath(uriString);
                        file.setSelectedFilePath(selectedFilePath);
                        // file.setFileSize(sizeIndex);
                        file.setInputStream(input);
                        file.setActionType(actionType);

                    } catch (FileNotFoundException e) {

                        e.printStackTrace();
                    }
                    Log.e("Display Name: ", "displayName=--" + displayName);

                    /*if (onFileChooseCallBack != null) {

                        System.out.println("Get on file choose callback");
                        onFileChooseCallBack.onFileChoose(file);
                    }*/

                    //Multipart POst Upload file
                    uploadFile(file);
                }
                break;
        }
    }

    private void uploadFile(FileDetails file) {

        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("userID", "" + Util.fetchUserClass(mContext).getUserId());
        uploadedFile = file;
        MultipartPostRequest request = new MultipartPostRequest(mContext, Consts.UPLOAD_STUDENT_CV,
                requestMap, file, "userfile");
        request.mListener = this;
        request.execute();
    }

    @Override
    public void onMultipartPost(String response) {
        Log.e("Response", "" + response);
        String responseStatus = "";
        try {
            JSONObject requestJsonObject = new JSONObject(response);
            responseStatus = requestJsonObject.optString("code").trim();

            if (responseStatus != null && responseStatus.trim().equalsIgnoreCase("200")) {

                Toast.makeText(mContext, "CV posted succesfully.", Toast.LENGTH_SHORT).show();
                UserClass userClass = Util.fetchUserClass(mContext);
                Log.e("Filename", "Oh Lord: file name: " + uploadedFile.getFileName());
                userClass.uplodedCVUrl = getBaseImageURl(userClass.uplodedCVUrl) + "/" + uploadedFile.getFileName();
                Util.saveUserClass(mContext, userClass);
                Log.e("FInalURL", "Final URL: " + userClass.uplodedCVUrl);

            } else if (responseStatus != null && responseStatus.trim().equalsIgnoreCase("400")) {
                Util.showMessageWithOk(ProfileActivity.this, "" + requestJsonObject.optString("msg"));
            } else {

                Toast.makeText(mContext, "Error occured while uploading cv.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

            Toast.makeText(mContext, "Error occured while uploading cv.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUrl(String url) {
        Log.e("FileName", "Filename URl: " + url);
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        Log.e("FIlename", "filename:" + fileName);
        return fileName;

    }

    private String getBaseImageURl(String url) {

        Log.e("baseURl", "URL: " + url);
        String baseURL = "";
        try {
            baseURL = url.trim().substring(0, url.trim().lastIndexOf("/"));
            //baseURL = url.substring(url.lastIndexOf('/', 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("SubURl", "SubURL: " + baseURL);
        return baseURL;
    }

    /**
     * Background Async Task to download file
     */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Downloading file. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(true);
            if (!pDialog.isShowing())
                pDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream("/sdcard/" + getFileNameFromUrl(Util.fetchUserClass(mContext).uplodedCVUrl));

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            if (pDialog.isShowing())
                pDialog.dismiss();

            // Displaying downloaded image into image view
            // Reading image path from sdcard
            String imagePath = Environment.getExternalStorageDirectory().toString() + "/" + getFileNameFromUrl(Util.fetchUserClass(mContext).uplodedCVUrl);
            // setting downloaded into image view
            //my_image.setImageDrawable(Drawable.createFromPath(imagePath));
            Log.e("Stored Path", "StoredPath: " + imagePath);
            if (imagePath != null && imagePath.length() > 0) {
                File file = new File(imagePath);
                try {
                    FileOpen.openFile(mContext, file);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                }

            }
        }

    }

    /**
     * Set Admin chat session
     */
    private void setScheduleForAdminChat() {

        datePickerDialog.show();
    }

    private void callScheduleChatService() {

        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("userID", "" + Util.fetchUserClass(mContext).getUserId());
        requestMap.put("scheduleDate", "" + changedDay + "/" + changedMonth + "/" + changedYear);
        requestMap.put("scheduleTime", "" + changedHour + ":" + changedMinute);
        isScheduleService = true;
        volleyTaskManager.doPostAdminChatSchedule(requestMap, true);
    }
}
