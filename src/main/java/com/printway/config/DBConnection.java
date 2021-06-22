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
            String sqlQuery = "INSERT INTO tbldelivery (tracking_number, order_number, partner_tracking_number, events, status, dtime_entered, last_events, transit_time, carrier_code, date_received) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?) ";
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
            statement.setString(10, delivery.getDate_received());
            statement.execute();
            this.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(TBLDelivery delivery) {
        try {
            String sqlQuery = "UPDATE tbldelivery SET tracking_number = ?, partner_tracking_number = ?, status = ?, events = ?, last_events = ?, transit_time = ?, carrier_code = ? " +
                    "WHERE order_number = ? AND date_received = ?;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, delivery.getTrackingNumber());
            statement.setString(2, delivery.getPartnerTrackingNumber());
            statement.setString(3, delivery.getStatus());
            statement.setString(4, delivery.getEvents());
            statement.setString(5, delivery.getLastEvents());
            statement.setString(6, delivery.getTransitTime());
            statement.setString(7, delivery.getCarrierCode());
            statement.setString(8, delivery.getOrderNumber());
            statement.setString(9, delivery.getDate_received());
            statement.executeUpdate();
            this.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String findStatusByTrackingNumber(String trackingNumber, String dateReceived) {
        String status = null;
        try {
            String sqlQuery = "SELECT status FROM tbldelivery WHERE tracking_number = ? AND date_received = ?;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, trackingNumber);
            statement.setString(2, dateReceived);
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
            String sqlQuery = "SELECT T.order_id, T.tracking_number, T.partner_tracking_number, T.date_received, T.dtime_entered " +
                    "FROM tblnewtracking T " +
                    "WHERE LENGTH(T.partner_tracking_number) > 0 " +
                    "AND T.order_id NOT IN (SELECT order_number FROM tbldelivery) " +
                    "AND (LENGTH(T.date_received) > 0 OR T.date_received IS NOT NULL) ORDER BY T.dtime_entered DESC";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLNewTracking tblNewTracking = new TBLNewTracking();
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("partner_tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("tracking_number"));
                tblNewTracking.setDate_received(resultSet.getDate("date_received").toString());
                newTrackingList.add(tblNewTracking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTrackingList;
    }

    public TBLNewTracking findOneOrderToUpdate(String orderId, String dateReceived) {
        TBLNewTracking tblNewTracking = new TBLNewTracking();
        try {
            String sqlQuery = "SELECT order_id, tracking_number, partner_tracking_number, date_received " +
                    "FROM tblnewtracking " +
                    "WHERE order_id = ? " +
                    "AND date_received = ? " +
                    "AND length(partner_tracking_number) > 0 ";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, orderId);
            statement.setString(2, dateReceived);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("partner_tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("tracking_number"));
                tblNewTracking.setDate_received(resultSet.getDate("date_received").toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tblNewTracking;
    }

    public TBLNewTracking findOneOrderToAdd(String orderId, String dateReceived) {
        TBLNewTracking tblNewTracking = new TBLNewTracking();
        try {
            String sqlQuery = "SELECT order_id, tracking_number, partner_tracking_number, date_received " +
                    "FROM tblnewtracking " +
                    "WHERE order_id = ? " +
                    "AND date_received = ? " +
                    "AND length(partner_tracking_number) > 0 ";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, orderId);
            statement.setString(2, dateReceived);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("partner_tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("tracking_number"));
                tblNewTracking.setDate_received(resultSet.getDate("date_received").toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tblNewTracking;
    }

    public List<TBLNewTracking> findAllOrderToUpdate() {
        List<TBLNewTracking> newTrackingList = new ArrayList<>();
        try {
            String sqlQuery = "SELECT order_number as order_id, tracking_number, partner_tracking_number, date_received, dtime_entered " +
                    "FROM tbldelivery " +
                    "WHERE length(carrier_code) > 0 " +
                    "AND length(order_number) > 0 " +
                    "AND status NOT IN ('delivered') ORDER BY dtime_entered DESC;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLNewTracking tblNewTracking = new TBLNewTracking();
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("partner_tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("tracking_number"));
                tblNewTracking.setDate_received(resultSet.getDate("date_received").toString());
                newTrackingList.add(tblNewTracking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTrackingList;
    }


    public boolean checkOrderIdInTBLDelivery(String order_id, String dateReceived) {
        try {
            String orderId = null;
            String sqlQuery = "SELECT order_number as order_id FROM tbldelivery WHERE order_number = ? AND date_received = ?;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, order_id);
            statement.setString(2, dateReceived);
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

    public boolean checkOrderIdInTBLNewtracking(String order_id, String dateReceived) {
        try {
            String orderId = null;
            String sqlQuery = "SELECT order_id FROM tblnewtracking WHERE order_id = ? AND date_received = ?;";
            PreparedStatement statement = this.connection.prepareStatement(sqlQuery);
            statement.setString(1, order_id);
            statement.setString(2, dateReceived);
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