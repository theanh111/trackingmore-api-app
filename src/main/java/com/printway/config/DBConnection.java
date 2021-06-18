package com.printway.config;

import com.printway.entityDB.TBLDelivery;
import com.printway.entityDB.TBLNewTracking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static DBConnection dbConnection;
    private Connection connection;

    private DBConnection() {
        String url = "jdbc:mysql://192.168.1.188/orderdesk";
        String username = "theanh";
        String password = "pW@@theanh123";

//        String url = "jdbc:mysql://localhost:3306/orderdesk";
//        String username = "root";
//        String password = "P@ssw0rd";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
            System.out.println("CONNECTED TO DATABASE SUCCESSFULLY!");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DBConnection getInstance() {
        if (dbConnection == null) {
            dbConnection = new DBConnection();
        }

        return dbConnection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert(TBLDelivery delivery) {
        try {
            String sqlQuery = "INSERT INTO tbldelivery (tracking_number, order_number, partner_tracking_number, events, status, dtime_entered, last_events, transit_time, carrier_code) " +
                    "VALUES (?,?,?,?,?,?,?,?,?) ";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, delivery.getTrackingNumber());
            statement.setString(2, delivery.getOrderNumber());
            statement.setString(3, delivery.getPartnerTrackingNumber());
            statement.setString(4, delivery.getEvents());
            statement.setString(5, delivery.getStatus());
            statement.setTimestamp(6, new Timestamp(delivery.getDtimeEntered().getTime()));
            statement.setString(7, delivery.getLastEvents());
            statement.setString(8, delivery.getTransitTime());
            statement.setString(9, delivery.getCarrierCode());
            statement.execute();
            this.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(TBLDelivery delivery) {
        try {
            String sqlQuery = "UPDATE tbldelivery SET tracking_number = ?, partner_tracking_number = ?, status = ?, events = ?, last_events = ?, transit_time = ?, carrier_code = ? " +
                    "WHERE order_number = ?;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, delivery.getTrackingNumber());
            statement.setString(2, delivery.getPartnerTrackingNumber());
            statement.setString(3, delivery.getStatus());
            statement.setString(4, delivery.getEvents());
            statement.setString(5, delivery.getLastEvents());
            statement.setString(6, delivery.getTransitTime());
            statement.setString(7, delivery.getCarrierCode());
            statement.setString(8, delivery.getOrderNumber());
            statement.executeUpdate();
            this.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String findStatusByTrackingNumber(String trackingNumber) {
        String status = null;
        try {
            String sqlQuery = "SELECT status FROM tbldelivery WHERE tracking_number = ?;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
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

    public List<TBLNewTracking> findAllOrderNotInTBLDelivery() {
        List<TBLNewTracking> newTrackingList = new ArrayList<>();
        try {
            String sqlQuery = "SELECT order_id, tracking_number, partner_tracking_number " +
                    "FROM tblnewtracking " +
                    "WHERE LENGTH(partner_tracking_number) > 0 " +
                    "AND order_id NOT IN (SELECT order_number FROM tbldelivery);";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLNewTracking tblNewTracking = new TBLNewTracking();
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("partner_tracking_number"));
                newTrackingList.add(tblNewTracking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTrackingList;
    }

    public TBLNewTracking findOneOrderToUpdate(String orderId) {
        TBLNewTracking newTracking = new TBLNewTracking();
        try {
            String sqlQuery = "SELECT order_number as order_id, tracking_number, partner_tracking_number " +
                    "FROM tbldelivery " +
                    "WHERE order_number = ? " +
                    "AND length(carrier_code) > 0 " +
                    "AND length(order_number) > 0 " +
                    "AND status NOT IN ('delivered');";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, orderId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                newTracking.setOrderId(resultSet.getString("order_id"));
                newTracking.setTrackingNumber(resultSet.getString("partner_tracking_number"));
                newTracking.setPartnerTrackingNumber(resultSet.getString("tracking_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTracking;
    }

    public List<TBLNewTracking> findAllOrderToUpdate() {
        List<TBLNewTracking> newTrackingList = new ArrayList<>();
        try {
            String sqlQuery = "SELECT order_number as order_id, tracking_number, partner_tracking_number " +
                    "FROM tbldelivery " +
                    "WHERE length(carrier_code) > 0 " +
                    "AND length(order_number) > 0 " +
                    "AND status NOT IN ('delivered');";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLNewTracking tblNewTracking = new TBLNewTracking();
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("partner_tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("tracking_number"));
                newTrackingList.add(tblNewTracking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTrackingList;
    }


    public boolean checkOrderIdInTBLDelivery(String order_id) {
        try {
            String orderId = null;
            String sqlQuery = "SELECT order_number as order_id FROM tbldelivery WHERE order_number = ?;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, order_id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                orderId = resultSet.getString("order_id");
            }
            if (orderId != null) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}