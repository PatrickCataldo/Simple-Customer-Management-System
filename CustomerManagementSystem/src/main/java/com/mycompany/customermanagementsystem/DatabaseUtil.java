/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.customermanagementsystem;

/**
 *
 * @author School Account
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseUtil {
    private static final String URL = "jdbc:sqlite:CustomerManagementSystemDB.db";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    
    public static void initializeDatabase() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS Customer ("
                + "CustomerID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                + "FirstName TEXT NOT NULL,"
                + "LastName TEXT NOT NULL,"
                + "EmailAddress TEXT NOT NULL UNIQUE,"
                + "PhoneNum TEXT NOT NULL,"
                + "StreetName TEXT NOT NULL,"
                + "City TEXT NOT NULL,"
                + "State TEXT NOT NULL,"
                + "Country TEXT NOT NULL,"
                + "Zip TEXT NOT NULL,"
                + "DateOfBirth TEXT NOT NULL,"
                + "CHECK (LENGTH(FirstName) <= 255),"
                + "CHECK (LENGTH(LastName) <= 255),"
                + "CHECK (LENGTH(EmailAddress) <= 255),"
                + "CHECK (PhoneNum GLOB '[0-9][0-9][0-9]-[0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]'),"
                + "CHECK (LENGTH(StreetName) <= 255),"
                + "CHECK (LENGTH(City) <= 255),"
                + "CHECK (LENGTH(State) <= 2),"
                + "CHECK (LENGTH(Country) <= 2),"
                + "CHECK (LENGTH(Zip) <= 5),"
                + "CHECK (DateOfBirth GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]')"
                + ");";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
    public static List<Customer> populateCustomerTable() throws SQLException {
        String query = "SELECT * FROM Customer;";
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(query)){
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int CustomerID = resultSet.getInt("CustomerID");
                    String FirstName = resultSet.getString("FirstName");
                    String LastName = resultSet.getString("LastName");
                    String EmailAddress = resultSet.getString("EmailAddress");
                    String PhoneNum = resultSet.getString("PhoneNum");
                    String StreetName = resultSet.getString("StreetName");
                    String City = resultSet.getString("City");
                    String State = resultSet.getString("State");
                    String Country = resultSet.getString("Country");
                    String Zip = resultSet.getString("Zip");
                    String DateOfBirth = resultSet.getString("DateOfBirth");
                    Customer customer = new Customer(CustomerID, FirstName, LastName, EmailAddress, PhoneNum, StreetName, City, State, Country, Zip, DateOfBirth);
                    customers.add(customer);
                }
            } catch (SQLException e) {
                
            }
            return customers;
    }
    public static void addCustomer(String FirstName, String LastName, 
            String EmailAddress, String PhoneNum, String StreetName, 
            String City, String State, String Country, String Zip, 
            String DateOfBirth) throws SQLException {
        String query = "INSERT INTO Customer (FirstName, LastName, EmailAddress, "
                + "PhoneNum, StreetName, City,  State, Country, Zip, "
                + "DateOfBirth) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
                ){
            statement.setString(1, FirstName);
            statement.setString(2, LastName);
            statement.setString(3, EmailAddress);
            statement.setString(4, PhoneNum);
            statement.setString(5, StreetName);
            statement.setString(6, City);
            statement.setString(7, State);
            statement.setString(8, Country);
            statement.setString(9, Zip);
            statement.setString(10, DateOfBirth);
            statement.executeUpdate(); 
        }    
    }
    public static void updateCustomer(int CustomerID, String FirstName, 
            String LastName, String EmailAddress, String PhoneNum, 
            String StreetName, String City, String State, String Country, 
            String Zip,  String DateOfBirth) throws SQLException {
        String query = "UPDATE Customer SET FirstName = ?, LastName = ?, "
                + "EmailAddress = ?, PhoneNum = ?, StreetName = ?, "
                + "City = ?, State = ?, Country = ?, Zip = ?, "
                + "DateOfBirth = ? WHERE CustomerID = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
                ){
            statement.setString(1, FirstName);
            statement.setString(2, LastName);
            statement.setString(3, EmailAddress);
            statement.setString(4, PhoneNum);
            statement.setString(5, StreetName);
            statement.setString(6, City);
            statement.setString(7, State);
            statement.setString(8, Country);
            statement.setString(9, Zip);
            statement.setString(10, DateOfBirth);
            statement.setInt(11, CustomerID);
            statement.executeUpdate(); 
        } 
    }
    public static void deleteCustomer(int CustomerID) throws SQLException {
        String query = "DELETE FROM Customer WHERE CustomerID = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
                ){
            statement.setInt(1, CustomerID);
            statement.executeUpdate(); 
        }
    }
    public static Map<String, Integer> getAgeDistribution() throws SQLException {
        Map<String, Integer> ageDistribution = new HashMap<>();

        // Initialize age brackets
        String[] brackets = {"Under 18", "18-24", "25-34", "35-44", "45-54", "55-64", "65 or over"};
        for (String bracket : brackets) {
            ageDistribution.put(bracket, 0);
        }

        // Fetch all customers from the database
        List<Customer> customers = populateCustomerTable();

        // Calculate the age for each customer and update the distribution map
        for (Customer customer : customers) {
            int age = customer.getAge();
            String bracket = getAgeBracket(age);
            ageDistribution.put(bracket, ageDistribution.get(bracket) + 1);
        }

        return ageDistribution;
    }
    private static String getAgeBracket(int age) {
        if (age >= 18 && age <= 24) return "18-24";
        if (age >= 25 && age <= 34) return "25-34";
        if (age >= 35 && age <= 44) return "35-44";
        if (age >= 45 && age <= 54) return "45-54";
        if (age >= 55 && age <= 64) return "55-64";
        if (age >= 65) return "65 or over";
        return "Under 18";
    }
    public static Map<String, Integer> getCustomerCountPerRegion() throws SQLException {
        Map<String, Integer> customerCountPerRegion = new HashMap<>();

        // Initialize region counts
        String[] regions = {"Northeast", "Southeast", "Midwest", "Northwest", "Southwest", "Other"};
        for (String region : regions) {
            customerCountPerRegion.put(region, 0);
        }

        // Define state to region mapping
        Map<String, String> stateToRegion = new HashMap<>();
        stateToRegion.put("ME", "Northeast");
        stateToRegion.put("NH", "Northeast");
        stateToRegion.put("VT", "Northeast");
        stateToRegion.put("MA", "Northeast");
        stateToRegion.put("RI", "Northeast");
        stateToRegion.put("CT", "Northeast");
        stateToRegion.put("NY", "Northeast");
        stateToRegion.put("NJ", "Northeast");
        stateToRegion.put("PA", "Northeast");
        stateToRegion.put("DE", "Southeast");
        stateToRegion.put("MD", "Southeast");
        stateToRegion.put("VA", "Southeast");
        stateToRegion.put("DC", "Southeast");
        stateToRegion.put("WV", "Southeast");
        stateToRegion.put("NC", "Southeast");
        stateToRegion.put("SC", "Southeast");
        stateToRegion.put("GA", "Southeast");
        stateToRegion.put("FL", "Southeast");
        stateToRegion.put("KY", "Southeast");
        stateToRegion.put("TN", "Southeast");
        stateToRegion.put("AL", "Southeast");
        stateToRegion.put("MS", "Southeast");
        stateToRegion.put("OH", "Midwest");
        stateToRegion.put("IN", "Midwest");
        stateToRegion.put("IL", "Midwest");
        stateToRegion.put("MI", "Midwest");
        stateToRegion.put("WI", "Midwest");
        stateToRegion.put("MN", "Midwest");
        stateToRegion.put("IA", "Midwest");
        stateToRegion.put("MO", "Midwest");
        stateToRegion.put("ND", "Midwest");
        stateToRegion.put("SD", "Midwest");
        stateToRegion.put("NE", "Midwest");
        stateToRegion.put("KS", "Midwest");
        stateToRegion.put("MT", "Northwest");
        stateToRegion.put("WY", "Northwest");
        stateToRegion.put("ID", "Northwest");
        stateToRegion.put("WA", "Northwest");
        stateToRegion.put("OR", "Northwest");
        stateToRegion.put("CA", "Northwest");
        stateToRegion.put("NV", "Southwest");
        stateToRegion.put("UT", "Southwest");
        stateToRegion.put("AZ", "Southwest");
        stateToRegion.put("NM", "Southwest");
        stateToRegion.put("CO", "Southwest");
        stateToRegion.put("TX", "Southwest");
        stateToRegion.put("OK", "Southwest");
        stateToRegion.put("AR", "Southwest");
        stateToRegion.put("LA", "Southwest");
        stateToRegion.put("HI", "Other");
        stateToRegion.put("AK", "Other");

        // Fetch all customers from the database
        List<Customer> customers = populateCustomerTable();

        // Calculate the region for each customer and update the distribution map
        for (Customer customer : customers) {
            String state = customer.getState();
            String region = stateToRegion.getOrDefault(state, "Other");
            customerCountPerRegion.put(region, customerCountPerRegion.get(region) + 1);
        }

        return customerCountPerRegion;
    }
    
}
