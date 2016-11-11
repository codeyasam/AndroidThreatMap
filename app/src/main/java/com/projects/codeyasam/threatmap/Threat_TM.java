package com.projects.codeyasam.threatmap;

import org.json.JSONObject;

/**
 * Created by codeyasam on 11/11/16.
 */
public class Threat_TM {

    private String id;
    private String address;
    private String lat;
    private String lng;
    private String description;
    private String municipality;
    private String province;
    private String country;

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public static Threat_TM instantiateJSON(JSONObject json) {
        try {
            Threat_TM threat = new Threat_TM();
            threat.id = json.getString("id");
            threat.address = json.getString("address");
            threat.lat = json.getString("lat");
            threat.lng = json.getString("lng");
            threat.description = json.getString("description");
            threat.municipality = json.getString("municipality");
            threat.province = json.getString("province");
            threat.country = json.getString("country");
            return threat;
        } catch (Exception e) {

        }
        return null;
    }
}
