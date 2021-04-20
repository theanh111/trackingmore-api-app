package com.printway.repository;

import com.printway.config.Database;
import com.printway.entityDB.TBLDelivery;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DeliveryRepository {
    private final String url = getDatabase().getUrl();
    private final String username = getDatabase().getUsername();
    private final String password = getDatabase().getPassword();

    public DeliveryRepository() {
    }

    public Database getDatabase() {
        Database database = new Database();
        database.getUrl();
        database.getUsername();
        database.getPassword();
        return database;
    }

    public void insert(TBLDelivery delivery) {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "INSERT INTO tbldelivery (tracking_number, order_number, partner_tracking_number, events, status, dtime_entered, last_events, transit_time) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE order_id = ?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, delivery.getTrackingNumber());
            statement.setString(2, delivery.getOrderNumber());
            statement.setString(3, delivery.getPartnerTrackingNumber());
            statement.setString(4, delivery.getEvents());
            statement.setString(5, delivery.getStatus());
            statement.setTimestamp(6, new Timestamp(delivery.getDtimeEntered().getTime()));
            statement.setString(7, delivery.getLastEvents());
            statement.setString(8, delivery.getTransitTime());
            statement.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void update(TBLDelivery delivery) {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "UPDATE tbldelivery SET status = ?, events = ?, dtime_entered = ? WHERE tracking_number = ?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, delivery.getStatus());
            statement.setString(2, delivery.getEvents());
            statement.setTimestamp(3, new Timestamp(delivery.getDtimeEntered().getTime()));
            statement.setString(4, delivery.getTrackingNumber());
            statement.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public String findStatusByTrackingNumber(String trackingNumber) {
        String status = null;
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "SELECT status FROM tbldelivery WHERE tracking_number = ?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, trackingNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                status = resultSet.getString("status");
            }
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return status;
    }

    public List<String> findAllTrackingNumber() {
        List<String> statusList = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "SELECT tracking_number FROM tbldelivery";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String trackingNumber = resultSet.getString("tracking_number");
                statusList.add(trackingNumber);
            }
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return statusList;
    }

    public TBLDelivery findOne(String trackingNumber) {
        TBLDelivery delivery = new TBLDelivery();
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "SELECT * FROM tbldelivery WHERE tracking_number = ?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, trackingNumber);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                delivery.setTrackingNumber(resultSet.getString("tracking_number"));
                delivery.setEvents(resultSet.getString("events"));
                delivery.setStatus(resultSet.getString("status"));
                delivery.setDtimeEntered(resultSet.getDate("dtime_entered"));
                delivery.setWhoEntered(resultSet.getString("who_entered"));
            }
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return delivery;
    }
}
