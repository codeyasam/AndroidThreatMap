package com.projects.codeyasam.threatmap;

import org.json.JSONObject;

/**
 * Created by codeyasam on 11/12/16.
 */
public class Notif_TM {

    private String id;
    private String client_id;
    private String office_id;
    private String address;
    private String municipality;
    private String province;
    private String country;
    private String lat;
    private String lng;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getOffice_id() {
        return office_id;
    }

    public void setOffice_id(String office_id) {
        this.office_id = office_id;
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

    public static Notif_TM instantiateJSON(JSONObject json) {
        try {
            Notif_TM notifObj = new Notif_TM();
            notifObj.setId(json.getString("id"));
            notifObj.setClient_id(json.getString("client_id"));
            notifObj.setOffice_id(json.getString("office_id"));
            notifObj.setAddress(json.getString("address"));
            notifObj.setMunicipality(json.getString("municipality"));
            notifObj.setProvince(json.getString("province"));
            notifObj.setLat(json.getString("lat"));
            notifObj.setLng(json.getString("lng"));
            return notifObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
