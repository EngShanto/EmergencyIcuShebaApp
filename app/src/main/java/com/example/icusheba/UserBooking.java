package com.example.icusheba;

public class UserBooking {
    private String Address;
    private String Bed_Fee;
    private String Date;
    private String Email;
    private String Hospital_Name;
    private String Name;
    private String Hospital_Number;
    private String Number_Of_Seat;
    private String Seat_Type;
    private int SerialNumber;
    private String Status;
    private String Time;
    private String Hospital_Address;

    public UserBooking() {
        // Default constructor required for Firestore
    }

    public String getAddress() { return Address; }
    public void setAddress(String address) { Address = address; }

    public String getBed_Fee() { return Bed_Fee; }
    public void setBed_Fee(String bedFee) { Bed_Fee = bedFee; }

    public String getDate() { return Date; }
    public void setDate(String date) { Date = date; }

    public String getEmail() { return Email; }
    public void setEmail(String email) { Email = email; }

    public String getHospital_Name() { return Hospital_Name; }
    public void setHospital_Name(String hospitalName) { Hospital_Name = hospitalName; }
    public String getHospital_Address(){return Hospital_Address;}
    public void setHospital_Address(String hospitalAddress){Hospital_Address = hospitalAddress;}

    public String getName() { return Name; }
    public void setName(String name) { Name = name; }

    public String getHospital_Number() { return Hospital_Number; }
    public void setHospital_Number(String number) { Hospital_Number = number; }

    public String getNumber_Of_Seat() { return Number_Of_Seat; }
    public void setNumber_Of_Seat(String number_Of_Seat) { Number_Of_Seat = number_Of_Seat; }

    public String getSeat_Type() { return Seat_Type; }
    public void setSeat_Type(String seatType) { Seat_Type = seatType; }

    public int getSerialNumber() { return SerialNumber; }
    public void setSerialNumber(int serialNumber) { SerialNumber = serialNumber; }

    public String getStatus() { return Status; }
    public void setStatus(String status) { Status = status; }

    public String getTime() { return Time; }
    public void setTime(String time) { Time = time; }
}
