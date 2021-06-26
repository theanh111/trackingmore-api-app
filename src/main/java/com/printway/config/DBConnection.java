package com.printway.config;

import com.printway.entityDB.TBLTracking;
import com.printway.entityDB.TBLImportTracking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static DBConnection dbConnection;
    private Connection connection;

    private DBConnection() {

        String url = "jdbc:mysql://128.199.143.87/pw_backoffice";
        String username = "theanh";
        String password = "1Q2w3e4r5t@@";

//        String url = "jdbc:mysql://192.168.1.188/orderdesk";
//        String username = "theanh";
//        String password = "pW@@theanh123";

//        String url = "jdbc:mysql://localhost:3306/pw_backoffice";
//        String username = "root";
//        String password = "P@ssw0rd";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
            System.out.println("CONNECTED TO DATABASE 87 SUCCESSFULLY!");
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

    public void insert(TBLTracking tblTracking) {
        try {
            if (tblTracking.getGearmentOrderId() != null) {
                Date dateCurrent = new Date(System.currentTimeMillis());
                String sqlQuery = "INSERT INTO tbltracking (order_id, tracking_number, events, status, dtime_entered, last_events, transit_time, carrier_code, tracking_url, who_entered) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
                statement.setString(1, tblTracking.getOrderId());
                statement.setString(2, tblTracking.getTrackingNumber());
                statement.setString(3, tblTracking.getEvents());
                statement.setString(4, tblTracking.getStatus());
                statement.setTimestamp(5, new Timestamp(dateCurrent.getTime()));
                statement.setString(6, tblTracking.getLastEvents());
                statement.setString(7, tblTracking.getTransitTime());
                statement.setString(8, tblTracking.getCarrierCode());
                statement.setString(9, tblTracking.getTrackingUrl());
                statement.setString(10, tblTracking.getWhoEntered());
                statement.execute();
                System.out.println("- SQL: " + statement);
                this.connection.commit();
            } else {
                Date dateCurrent = new Date(System.currentTimeMillis());
                String sqlQuery = "INSERT INTO tbltracking (order_id, tracking_number, events, status, dtime_entered, last_events, transit_time, carrier_code, tracking_url, who_entered, tracking_type) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
                statement.setString(1, tblTracking.getOrderId());
                statement.setString(2, tblTracking.getTrackingNumber());
                statement.setString(3, tblTracking.getEvents());
                statement.setString(4, tblTracking.getStatus());
                statement.setTimestamp(5, new Timestamp(dateCurrent.getTime()));
                statement.setString(6, tblTracking.getLastEvents());
                statement.setString(7, tblTracking.getTransitTime());
                statement.setString(8, tblTracking.getCarrierCode());
                statement.setString(9, "https://www.trackingmore.com/en/" + tblTracking.getTrackingNumber());
                statement.setString(10, tblTracking.getWhoEntered());
                statement.setString(11, "I");
                statement.execute();
                System.out.println("- SQL for IMPORT Tracking: " + statement);
                this.connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(TBLTracking tblTracking) {
        try {
            String sqlQuery = "UPDATE tbltracking SET status = ?, events = ?, last_events = ?, transit_time = ?, carrier_code = ? " +
                    "WHERE order_id = ?;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, tblTracking.getStatus());
            statement.setString(2, tblTracking.getEvents());
            statement.setString(3, tblTracking.getLastEvents());
            statement.setString(4, tblTracking.getTransitTime());
            statement.setString(5, tblTracking.getCarrierCode());
            statement.setString(6, tblTracking.getOrderId());
            statement.executeUpdate();
            this.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String findStatusByTrackingNumber(String trackingNumber) {
        String status = null;
        try {
            String sqlQuery = "SELECT status FROM tbltracking WHERE tracking_number = ?;";
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

    public List<TBLImportTracking> findAllOrderNotInTBLTracking() {
        List<TBLImportTracking> newTrackingList = new ArrayList<>();
        try {
            String sqlQuery = "SELECT IT.order_id, IT.tracking_number, IT.tracking_url, IT.tracking_carrier, IT.dtime_entered, IT.gearment_order_id " +
                    "FROM tblimporttracking IT " +
                    "WHERE LENGTH(IT.tracking_number) > 0 " +
                    "AND IT.order_id NOT IN (SELECT T.order_id FROM tbltracking T) ORDER BY IT.dtime_entered DESC";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLImportTracking tblImportTracking = new TBLImportTracking();
                tblImportTracking.setOrderId(resultSet.getString("order_id"));
                tblImportTracking.setTrackingNumber(resultSet.getString("tracking_number"));
                tblImportTracking.setTrackingCarrier(resultSet.getString("tracking_carrier"));
                tblImportTracking.setTrackingUrl(resultSet.getString("tracking_url"));
                if (resultSet.getString("gearment_order_id") != null) {
                    tblImportTracking.setGearmentOrderId(resultSet.getString("gearment_order_id"));
                }
                newTrackingList.add(tblImportTracking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTrackingList;
    }

    public List<TBLImportTracking> findAllOrderToUpdate() {
        List<TBLImportTracking> newTrackingList = new ArrayList<>();
        try {
            String sqlQuery = "SELECT order_id, tracking_number, dtime_entered " +
                    "FROM tbltracking " +
                    "WHERE length(carrier_code) > 0 " +
                    "AND length(order_id) > 0 " +
                    "AND (LENGTH(link_label) = 0 OR link_label IS NULL) " +
                    "AND (LENGTH(partner_tracking_number) = 0 OR partner_tracking_number IS NULL) " +
                    "AND status NOT IN ('delivered') ORDER BY dtime_entered DESC;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLImportTracking tblImportTracking = new TBLImportTracking();
                tblImportTracking.setOrderId(resultSet.getString("order_id"));
                tblImportTracking.setTrackingNumber(resultSet.getString("tracking_number"));
                newTrackingList.add(tblImportTracking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTrackingList;
    }
}