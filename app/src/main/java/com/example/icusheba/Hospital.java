package com.example.icusheba;

public class Hospital {
    private String Hospital_Name;
    private String Hospital_Address;
    private String Hospital_Number;
    private String imageUrl;

    public Hospital() {}

    public Hospital(String name, String address, String number, String imageUrl) {
        this.Hospital_Name = name;
        this.Hospital_Address = address;
        this.Hospital_Number = number;
        this.imageUrl = imageUrl;
    }

    public String getHospital_Name() { return Hospital_Name; }
    public void setHospital_Name(String name) { this.Hospital_Name = name; }
    public String getHospital_Address() { return Hospital_Address; }
    public void setHospital_Address(String address) { this.Hospital_Address = address; }
    public String getHospital_Number() { return Hospital_Number; }
    public void setHospital_Number(String number) { this.Hospital_Number = number; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
