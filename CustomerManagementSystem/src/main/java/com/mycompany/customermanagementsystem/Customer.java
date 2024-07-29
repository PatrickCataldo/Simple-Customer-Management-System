/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.customermanagementsystem;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author School Account
 */
public class Customer {
    // Attributes
    int CustomerID;
    String FirstName;
    String LastName;
    String EmailAddress;
    String PhoneNum;
    String StreetName;
    String City;
    String State;
    String Country;
    String Zip;
    String DateOfBirth;
    // Constructor
    Customer(int CustomerID, String FirstName, String LastName, 
            String EmailAddress, String PhoneNum, String StreetName, 
            String City, String State, String Country, String Zip, 
            String DateOfBirth){
        this.CustomerID = CustomerID;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.EmailAddress = EmailAddress;
        this.PhoneNum = PhoneNum;
        this.StreetName = StreetName;
        this.City = City;
        this.State = State;
        this.Country = Country;
        this.Zip = Zip;
        this.DateOfBirth = DateOfBirth;
    }
    // Getters
    public int getCustomerID() {
        return CustomerID;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getEmailAddress() {
        return EmailAddress;
    }

    public String getPhoneNum() {
        return PhoneNum;
    }

    public String getStreetName() {
        return StreetName;
    }

    public String getCity() {
        return City;
    }

    public String getState() {
        return State;
    }

    public String getCountry() {
        return Country;
    }

    public String getZip() {
        return Zip;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }
    public int getAge() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthday = LocalDate.parse(DateOfBirth, formatter);;
        Period period = Period.between(birthday, currentDate);
        return period.getYears();
    }
    
}
