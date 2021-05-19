package com.printway.repository;

import com.printway.config.DBConnection;
import com.printway.entityDB.TBLDelivery;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class DeliveryRepository {
    public static DBConnection connection = DBConnection.getInstance();

    public DeliveryRepository() {
    }

    public void insert(TBLDelivery delivery) {
        try {
            String sqlQuery = "INSERT INTO tbldelivery (tracking_number, order_number, partner_tracking_number, events, status, dtime_entered, last_events, transit_time, carrier_code) " +
                    "VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE order_number = ?;";
            PreparedStatement statement = connection.getConnection().prepareStatement(sqlQuery);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(TBLDelivery delivery) {
        try {
            String sqlQuery = "UPDATE tbldelivery SET tracking_number = ?, partner_tracking_number = ?, status = ?, events = ?, last_events = ?, transit_time = ?, carrier_code = ? " +
                    "WHERE order_number = ?;";
            PreparedStatement statement = connection.getConnection().prepareStatement(sqlQuery);
            statement.setString(1, delivery.getTrackingNumber());
            statement.setString(2, delivery.getPartnerTrackingNumber());
            statement.setString(3, delivery.getStatus());
            statement.setString(4, delivery.getEvents());
            statement.setString(5, delivery.getLastEvents());
            statement.setString(6, delivery.getTransitTime());
            statement.setString(7, delivery.getCarrierCode());
            statement.setString(8, delivery.getOrderNumber());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String findStatusByTrackingNumber(String trackingNumber) {
        String status = null;
        try {
            String sqlQuery = "SELECT status FROM tbldelivery WHERE tracking_number = ?;";
            PreparedStatement statement = connection.getConnection().prepareStatement(sqlQuery);
            statement.setString(1, trackingNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                status = resultSet.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }
}
