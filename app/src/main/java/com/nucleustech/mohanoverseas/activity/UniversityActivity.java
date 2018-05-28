package com.nucleustech.mohanoverseas.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.nucleustech.mohanoverseas.student.R;
import com.nucleustech.mohanoverseas.adapter.CourseAdapter;
import com.nucleustech.mohanoverseas.model.CertificateCourse;
import com.nucleustech.mohanoverseas.model.Course;
import com.nucleustech.mohanoverseas.model.University;
import com.nucleustech.mohanoverseas.util.AlertDialogCallBack;
import com.nucleustech.mohanoverseas.util.Util;
import com.nucleustech.mohanoverseas.volley.ServerResponseCallback;
import com.nucleustech.mohanoverseas.volley.VolleyTaskManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ritwik.rai on 23/12/17.
 */

public class UniversityActivity extends AppCompatActivity implements ServerResponseCallback {

    private VolleyTaskManager volleyTaskManager;
    private String universityID = "";
    private Context mContext;
    private University university;
    private ArrayList<Course> allCourses = new ArrayList<>();
    private ListView lv_courses, lv_undergradcourses, lv_postgradcourses;
    private TextView tv_aboutUs;
    private TextView tv_collegeName, tv_collegeAddress;
    private ArrayList<Course> underGradCourses = new ArrayList<>();
    private ArrayList<Course> postGradCourses = new ArrayList<>();
    private LinearLayout llAboutUS, llUnderGrad, llPostGrad;
    private TextView tv_menu_postgrad, tv_menu_undergrad, tv_menu_aboutus;
    private View v_line_aboutme, v_line_undergrad, v_line_postgrad;
    private TextView tv_requestInfo;
    private boolean isFetchUniversity = false;
    private boolean isSuggestUniversity = false;
    private TextView tv_underGradDetails, tv_postGradDetails;
    private TextView tv_aboutUs_moreInfo;

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
        setContentView(R.layout.activity_university_profile);
        mContext = UniversityActivity.this;


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" ");


        universityID = getIntent().getStringExtra("universityID");
        Log.e("universityID", "universityID: " + universityID);

        lv_courses = (ListView) findViewById(R.id.lv_courses);
        lv_undergradcourses = (ListView) findViewById(R.id.lv_undergradcourses);
        lv_postgradcourses = (ListView) findViewById(R.id.lv_postgradcourses);
        tv_aboutUs = (TextView) findViewById(R.id.tv_aboutUs);
        tv_collegeName = (TextView) findViewById(R.id.tv_collegeName);
        tv_collegeAddress = (TextView) findViewById(R.id.tv_collegeAddress);


        llAboutUS = (LinearLayout) findViewById(R.id.llAboutUS);
        llUnderGrad = (LinearLayout) findViewById(R.id.llUnderGrad);
        llPostGrad = (LinearLayout) findViewById(R.id.llPostGrad);
        volleyTaskManager = new VolleyTaskManager(mContext);

        tv_menu_aboutus = (TextView) findViewById(R.id.tv_menu_aboutus);
        tv_menu_undergrad = (TextView) findViewById(R.id.tv_menu_undergrad);
        tv_menu_postgrad = (TextView) findViewById(R.id.tv_menu_postgrad);

        v_line_aboutme = (View) findViewById(R.id.v_line_aboutme);
        v_line_undergrad = (View) findViewById(R.id.v_line_undergrad);
        v_line_postgrad = (View) findViewById(R.id.v_line_postgrad);

        tv_requestInfo = (TextView) findViewById(R.id.tv_requestInfo);
        tv_aboutUs_moreInfo = (TextView) findViewById(R.id.tv_aboutUs_moreInfo);

        tv_underGradDetails = (TextView) findViewById(R.id.tv_underGradDetails);
        tv_postGradDetails = (TextView) findViewById(R.id.tv_postGradDetails);

        if (universityID.length() > 0) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put("uid", "" + universityID);
            isFetchUniversity = true;
            volleyTaskManager.doPostFetchUniversity(requestMap, true);
        } else {
            Util.showCallBackMessageWithOkCallback(mContext, "Please reopen the University page again.", new AlertDialogCallBack() {
                @Override
                public void onSubmit() {
                    finish();
                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

    @Override
    public void onSuccess(JSONObject resultJsonObject) {
        if (isFetchUniversity) {
            isFetchUniversity = false;
            if (resultJsonObject.optString("code").trim().equalsIgnoreCase("200")) {
                JSONObject universityJsonObject = resultJsonObject.optJSONObject("university");
                University university = new University();
                university.universityId = universityJsonObject.optString("universityID");
                university.universityName = universityJsonObject.optString("universityName");
                university.about = universityJsonObject.optString("universityAbout");
                university.address = universityJsonObject.optString("universityAddress");
                university.moreInfo = universityJsonObject.optString("moreInfo");

                JSONArray degreesOffered = universityJsonObject.optJSONArray("degreesOffered");
                ArrayList<CertificateCourse> degrees = new ArrayList<>();
                allCourses = new ArrayList<>();

                for (int i = 0; i < degreesOffered.length(); i++) {
                    JSONObject degreesJsonObject = degreesOffered.optJSONObject(i);
                    CertificateCourse degree = new CertificateCourse();
                    degree.certificateName = degreesJsonObject.optString("degreeType");
                    degree.certificateDetails = degreesJsonObject.optString("degreeDetail");

                    if (i == 0)
                        university.underGradDetails = degree.certificateDetails;
                    else if (i == 1)
                        university.postGradDetails = degree.certificateDetails;

                    Toast.makeText(mContext, "UG: " + university.underGradDetails, Toast.LENGTH_SHORT).show();
                    JSONArray coursesOffered = degreesJsonObject.optJSONArray("coursesOffered");
                    Log.e("coursesOffered", "coursesOffered: " + coursesOffered);

                    Log.e("coursesOffered", "coursesOffered: " + degreesJsonObject.optString("coursesOffered"));
                    ArrayList<Course> courses = new ArrayList<>();
                    for (int j = 0; j < coursesOffered.length(); j++) {
                        JSONObject coursesJson = coursesOffered.optJSONObject(j);
                        Course course = new Course();
                        course.courseName = coursesJson.optString("courseName");
                        course.courseDetails = coursesJson.optString("courseDetail");
                        course.moreInfo = coursesJson.optString("moreInfo");
                        courses.add(course);
                        allCourses.add(course);
                        if (i == 0) {
                            underGradCourses.add(course);
                        } else if (i == 1) {
                            postGradCourses.add(course);
                        }
                    }
                    degree.courses = courses;
                    degrees.add(degree);

                }
                university.certificateCourses = degrees;

                this.university = university;

                loadView();
            }
        } else if (isSuggestUniversity) {
            isSuggestUniversity = false;
            if (resultJsonObject.optString("code").equalsIgnoreCase("200")) {
                Util.showCallBackMessageWithOkCallback(mContext, "Request sent successfully.", new AlertDialogCallBack() {
                    @Override
                    public void onSubmit() {

                        startActivity(new Intent(mContext, ProfileActivity.class));
                        finish();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            } else if (resultJsonObject.optString("code").equalsIgnoreCase("404")) {
                Util.showMessageWithOk(UniversityActivity.this, "" + resultJsonObject.optString("msg"));
            } else {
                Toast.makeText(mContext, "Error! Something went wrong.", Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    public void onError() {

    }

    private void loadView() {
        tv_aboutUs.setText("" + university.about);
        //tv_aboutUs.setMovementMethod(LinkMovementMethod.getInstance());
        tv_collegeAddress.setText("" + university.address);
        tv_collegeName.setText("" + university.universityName);
        tv_underGradDetails.setText("" + university.underGradDetails);
        tv_postGradDetails.setText("" + university.postGradDetails);
        Log.e("moreInfo", "moreInfo" + university.moreInfo);
        Toast.makeText(mContext, "moreInfo" + university.moreInfo, Toast.LENGTH_SHORT).show();
        if (university.moreInfo.trim().equalsIgnoreCase("")) {
            tv_aboutUs_moreInfo.setVisibility(View.GONE);
        } else {
            tv_aboutUs_moreInfo.setVisibility(View.VISIBLE);
            tv_aboutUs_moreInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + university.moreInfo.trim()));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(mContext, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        CourseAdapter courseAdapter = new CourseAdapter(mContext, allCourses);
        lv_courses.setAdapter(courseAdapter);
        lv_courses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Course course = allCourses.get(i);

                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.dialog_course_details);
                dialog.setCancelable(true);
                TextView tv_courseName = (TextView) dialog.findViewById(R.id.tv_courseName);
                TextView tv_courseDetails = (TextView) dialog.findViewById(R.id.tv_courseDetails);
                final TextView tv_course_moreInfo = (TextView) dialog.findViewById(R.id.tv_course_moreInfo);
                Log.e("moreInfo", "moreInfo" + course.moreInfo);
                Toast.makeText(mContext, "moreInfo" + course.moreInfo, Toast.LENGTH_SHORT).show();
                if (course.moreInfo.trim().equalsIgnoreCase("")) {
                    tv_course_moreInfo.setVisibility(View.GONE);
                } else {
                    tv_course_moreInfo.setVisibility(View.VISIBLE);
                    tv_course_moreInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + course.moreInfo.trim()));
                                startActivity(browserIntent);
                            } catch (Exception e) {
                                Toast.makeText(mContext, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


                tv_courseDetails.setText(course.courseDetails);
                tv_courseName.setText(course.courseName);
                dialog.show();
            }
        });

        CourseAdapter courseAdapterUnderGrad = new CourseAdapter(mContext, underGradCourses);

        lv_undergradcourses.setAdapter(courseAdapterUnderGrad);
        lv_undergradcourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Course course = underGradCourses.get(i);

                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.dialog_course_details);
                dialog.setCancelable(true);
                TextView tv_courseName = (TextView) dialog.findViewById(R.id.tv_courseName);
                TextView tv_courseDetails = (TextView) dialog.findViewById(R.id.tv_courseDetails);
                final TextView tv_course_moreInfo = (TextView) dialog.findViewById(R.id.tv_course_moreInfo);
                Log.e("moreInfo", "moreInfo" + course.moreInfo);
                if (course.moreInfo.trim().equalsIgnoreCase("")) {
                    tv_course_moreInfo.setVisibility(View.GONE);
                } else {
                    tv_course_moreInfo.setVisibility(View.VISIBLE);
                    tv_course_moreInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + course.moreInfo.trim()));
                                startActivity(browserIntent);
                            } catch (Exception e) {
                                Toast.makeText(mContext, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                tv_courseDetails.setText(course.courseDetails);
                tv_courseName.setText(course.courseName);
                dialog.show();
            }
        });

        CourseAdapter courseAdapterPostGrad = new CourseAdapter(mContext, postGradCourses);

        lv_postgradcourses.setAdapter(courseAdapterPostGrad);
        lv_postgradcourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Course course = postGradCourses.get(i);

                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.dialog_course_details);
                dialog.setCancelable(true);
                TextView tv_courseName = (TextView) dialog.findViewById(R.id.tv_courseName);
                TextView tv_courseDetails = (TextView) dialog.findViewById(R.id.tv_courseDetails);
                final TextView tv_course_moreInfo = (TextView) dialog.findViewById(R.id.tv_course_moreInfo);
                Log.e("moreInfo", "moreInfo" + course.moreInfo);
                if (course.moreInfo.trim().equalsIgnoreCase("")) {
                    tv_course_moreInfo.setVisibility(View.GONE);
                } else {
                    tv_course_moreInfo.setVisibility(View.VISIBLE);
                    tv_course_moreInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + course.moreInfo.trim()));
                                startActivity(browserIntent);
                            } catch (Exception e) {
                                Toast.makeText(mContext, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                tv_courseDetails.setText(course.courseDetails);
                tv_courseName.setText(course.courseName);
                dialog.show();
            }
        });


        tv_menu_aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v_line_aboutme.setVisibility(View.VISIBLE);
                v_line_undergrad.setVisibility(View.GONE);
                v_line_postgrad.setVisibility(View.GONE);
                llAboutUS.setVisibility(View.VISIBLE);
                llUnderGrad.setVisibility(View.GONE);
                llPostGrad.setVisibility(View.GONE);

            }
        });
        tv_menu_undergrad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v_line_aboutme.setVisibility(View.GONE);
                v_line_undergrad.setVisibility(View.VISIBLE);
                v_line_postgrad.setVisibility(View.GONE);
                llAboutUS.setVisibility(View.GONE);
                llUnderGrad.setVisibility(View.VISIBLE);
                llPostGrad.setVisibility(View.GONE);
            }
        });
        tv_menu_postgrad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v_line_aboutme.setVisibility(View.GONE);
                v_line_undergrad.setVisibility(View.GONE);
                v_line_postgrad.setVisibility(View.VISIBLE);
                llAboutUS.setVisibility(View.GONE);
                llUnderGrad.setVisibility(View.GONE);
                llPostGrad.setVisibility(View.VISIBLE);
            }
        });

        tv_requestInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Util.fetchUserClass(mContext) != null) {
                    HashMap<String, String> requestMap = new HashMap<>();
                    requestMap.put("userID", "" + Util.fetchUserClass(mContext).getUserId());
                    requestMap.put("universityID", "" + universityID);
                    isSuggestUniversity = true;
                    volleyTaskManager.doPostApplyUniversity(requestMap, true);
                }
            }
        });
    }


}
