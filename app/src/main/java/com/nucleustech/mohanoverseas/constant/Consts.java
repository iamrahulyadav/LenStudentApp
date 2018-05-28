package com.nucleustech.mohanoverseas.constant;

/**
 * Created by ritwik.rai on 28/11/17.
 */

public class Consts {

    public static String BASE_URL = "http://inontoesevents.in/mohan/api/";

    public static String GET_OTP = BASE_URL + "sendOTP";

    public static String GET_STUDENT_LIST = BASE_URL + "studentslist";

    public static String GET_PROFILE = BASE_URL + "fetchProfile";

    public static String GET_ADMIN_PROFILE = BASE_URL + "getAdmin";

    public static String UPDATE_STUDENT_PROFILE = BASE_URL + "updateStudentsProfile";

    public static String UPLOAD_STUDENT_CV = BASE_URL + "cvUpload";

    public static String SCHEDULE_ADMIN_CHAT = BASE_URL + "scheduleMeeting";

    public static String FETCH_SUGGESTED_COLLEGES = BASE_URL + "fetchSuggestedUniversities";

    public static String FETCH_APPLIED_COLLEGES = BASE_URL + "fetchAppliedUniversities";

    public static String CANCEL_SCHEDULE_MEETING= BASE_URL + "scheduleMeetingCancel";


    public static final String[] fileTypes = { "pdf", "doc", "docx", "xls", " xlsx", "txt", "csv", "jpg", "png", "gif" };


}
