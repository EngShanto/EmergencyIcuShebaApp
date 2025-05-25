package com.example.icusheba;


public class Booking {
    public String id;
    private String Name; // Matches Firestore field "Name"
    private String Email; // Matches Firestore field "Email"
    private String Address; // Matches Firestore field "Address"
    private String Seat_Type; // Matches Firestore field "Seat Type"
    private String Date; // Matches Firestore field "Date"
    private String Time; // Matches Firestore field "Time"
    private String Status; // Matches Firestore field "Status"
    private String Number; // Matches Firestore field "Number"
    private int SerialNumber; // Matches Firestore field "SerialNumber"
    private String Bed_Fee; // Matches Firestore field "Bed Fee"
    private String Number_Of_Seat; // Matches Firestore field "Number Of Seat"
    private String fcmToken;
    public Booking() {}

    // Getters and Setters
    public String getName() { return Name; }
    public void setName(String name) { this.Name = name; }

    public String getAddress() { return Address; }
    public void setAddress(String address) { this.Address = address; }

    public String getDate() { return Date; }
    public void setDate(String date) { this.Date = date; }

    public String getTime() { return Time; }
    public void setTime(String time) { this.Time = time; }

    public String getStatus() { return Status; }
    public void setStatus(String status) { this.Status = status; }

    public String getNumber() { return Number; }
    public void setNumber(String number) { this.Number = number; }

    public int getSerialNumber() { return SerialNumber; }
    public void setSerialNumber(int serialNumber) { this.SerialNumber = serialNumber; }

    public String getBed_Fee() { return Bed_Fee; }
    public void setBed_Fee(String bedFee) { this.Bed_Fee = bedFee; }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getSeat_Type() {
        return Seat_Type;

    }
    public void setSeat_Type(String seatType) {
        this.Seat_Type = seatType;
    }

    public String getNumber_Of_Seat() {
        return Number_Of_Seat;

    }
    public void setNumber_Of_Seat(String numberOfSeat) {
        this.Number_Of_Seat = numberOfSeat;
    }

    public String getEmail() {
        return Email;
    }
    public void setEmail(String email) {
        this.Email = email;
    }

    public String getFcmToken() {
        return fcmToken;

    }
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

}
