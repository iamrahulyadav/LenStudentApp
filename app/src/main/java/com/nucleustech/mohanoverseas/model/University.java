package com.nucleustech.mohanoverseas.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ritwik.rai on 22/12/17.
 */

public class University implements Serializable {

    public String universityId;

    public String universityName;

    public String address;

    public String about;

    public String underGradDetails;

    public String postGradDetails;

    public ArrayList<CertificateCourse> certificateCourses= new ArrayList<>();

    public String moreInfo="";
}
