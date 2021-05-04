package com.printway.repository;

import com.printway.config.Database;
import com.printway.entityDB.TBLDelivery;
import org.springframework.stereotype.Repository;

import java.sql.*;

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
            String sqlQuery = "INSERT INTO tbldelivery (tracking_number, order_number, partner_tracking_number, events, status, dtime_entered, last_events, transit_time, carrier_code) " +
                    "VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE order_number = ?;";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, delivery.getTrackingNumber());
            statement.setString(2, delivery.getOrderNumber());
            statement.setString(3, delivery.getPartnerTrackingNumber());
            statement.setString(4, delivery.getEvents());
            statement.setString(5, delivery.getStatus());
            statement.setTimestamp(6, new Timestamp(delivery.getDtimeEntered().getTime()));
            statement.setString(7, delivery.getLastEvents());
            statement.setString(8, delivery.getTransitTime());
            statement.setString(9, delivery.getCarrierCode());
            statement.setString(10, delivery.getOrderNumber());
            statement.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void update(TBLDelivery delivery) {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "UPDATE tbldelivery SET status = ?, events = ?, dtime_entered = ?, last_events = ?, transit_time = ? WHERE tracking_number = ?;";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, delivery.getStatus());
            statement.setString(2, delivery.getEvents());
            statement.setTimestamp(3, new Timestamp(delivery.getDtimeEntered().getTime()));
            statement.setString(4, delivery.getLastEvents());
            statement.setString(5, delivery.getTransitTime());
            statement.setString(6, delivery.getTrackingNumber());
            statement.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public String findStatusByTrackingNumber(String trackingNumber) {
        String status = null;
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "SELECT status FROM tbldelivery WHERE tracking_number = ?;";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, trackingNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                status = resultSet.getString("status");
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return status;
    }
}
