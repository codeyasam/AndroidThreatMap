package com.projects.codeyasam.threatmap;

import org.json.JSONObject;

/**
 * Created by codeyasam on 11/12/16.
 */
public class Office_TM {

    private String id;
    private String name;
    private String address;
    private String municipality;
    private String province;
    private String country;
    private String lat;
    private String lng;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public static Office_TM instantiateJSON(JSONObject json) {
        try {
            Office_TM officeObj = new Office_TM();
            officeObj.setId(json.getString("id"));
            officeObj.setAddress(json.getString("address"));
            officeObj.setName(json.getString("name"));
            officeObj.setMunicipality(json.getString("municipality"));
            officeObj.setProvince(json.getString("province"));
            officeObj.setCountry(json.getString("country"));
            officeObj.setLat(json.getString("lat"));
            officeObj.setLng(json.getString("lng"));
            return officeObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
