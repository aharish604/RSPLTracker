package com.arteriatech.geotrack.rspl;

/**
 * Created by e10854 on 28-12-2017.
 */

public class SalesPersonBean {
    private String SPNo = "";
    private String FirstName = "";
    private String LastName = "";
    private String SPCategoryID = "";
    private String SPCategoryDesc = "";
    private String DesignationID = "";
    private String DesignationDesc = "";
    private String Address1 = "";
    private String Address2 = "";
    private String Address3 = "";
    private String Address4 = "";
    private String PostalCode = "";
    private String EmailID = "";
    private String MobileNo = "";
    private String SPGUID = "";

    public String getExternalRefID() {
        return ExternalRefID;
    }

    public void setExternalRefID(String externalRefID) {
        ExternalRefID = externalRefID;
    }

    private String ExternalRefID = "";

    public String getSPNo() {
        return SPNo;
    }

    public void setSPNo(String SPNo) {
        this.SPNo = SPNo;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getSPCategoryID() {
        return SPCategoryID;
    }

    public void setSPCategoryID(String SPCategoryID) {
        this.SPCategoryID = SPCategoryID;
    }

    public String getSPCategoryDesc() {
        return SPCategoryDesc;
    }

    public void setSPCategoryDesc(String SPCategoryDesc) {
        this.SPCategoryDesc = SPCategoryDesc;
    }

    public String getDesignationID() {
        return DesignationID;
    }

    public void setDesignationID(String designationID) {
        DesignationID = designationID;
    }

    public String getDesignationDesc() {
        return DesignationDesc;
    }

    public void setDesignationDesc(String designationDesc) {
        DesignationDesc = designationDesc;
    }

    public String getAddress1() {
        return Address1;
    }

    public void setAddress1(String address1) {
        Address1 = address1;
    }

    public String getAddress2() {
        return Address2;
    }

    public void setAddress2(String address2) {
        Address2 = address2;
    }

    public String getAddress3() {
        return Address3;
    }

    public void setAddress3(String address3) {
        Address3 = address3;
    }

    public String getAddress4() {
        return Address4;
    }

    public void setAddress4(String address4) {
        Address4 = address4;
    }

    public String getPostalCode() {
        return PostalCode;
    }

    public void setPostalCode(String postalCode) {
        PostalCode = postalCode;
    }

    public String getEmailID() {
        return EmailID;
    }

    public void setEmailID(String emailID) {
        EmailID = emailID;
    }

    public String getMobileNo() {
        return MobileNo;
    }

    public void setMobileNo(String mobileNo) {
        MobileNo = mobileNo;
    }

    public String getSPGUID() {
        return SPGUID;
    }

    public void setSPGUID(String SPGUID) {
        this.SPGUID = SPGUID;
    }
}