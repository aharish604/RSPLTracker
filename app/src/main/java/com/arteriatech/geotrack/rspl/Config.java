package com.arteriatech.geotrack.rspl;

/**
 * Created by e10526 on 19-03-2016.
 */
public class Config {
    private String Type = "";
    private String Value="";
    private String Description="";
    public String getUSPDescription() {
        return USPDescription;
    }

    public void setUSPDescription(String USPDescription) {
        this.USPDescription = USPDescription;
    }

    private String USPDescription="";

    public String getFeature() {
        return Feature;
    }

    public void setFeature(String feature) {
        Feature = feature;
    }

    private String Feature="";

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }



}
