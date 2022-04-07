package com.example.samplediary;

public class Card {

    int _id;
    String address;
    String weather; // 데이터베이스에서 조회한 아이디 값을 넣어둠
    String location_x;
    String location_y;
    String contents;
    String mood;
    String picture;
    String createDate; // 작성 일자

    public Card(int _id, String address, String weather, String location_x, String location_y, String contents, String mood, String picture, String createDate) {
        this._id = _id;
        this.address = address;
        this.weather = weather;
        this.location_x = location_x;
        this.location_y = location_y;
        this.contents = contents;
        this.mood = mood;
        this.picture = picture;
        this.createDate = createDate;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getLocation_x() {
        return location_x;
    }

    public void setLocation_x(String location_x) {
        this.location_x = location_x;
    }

    public String getLocation_y() {
        return location_y;
    }

    public void setLocation_y(String location_y) {
        this.location_y = location_y;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
