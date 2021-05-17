package com.printway.repository;

import com.printway.config.Database;
import com.printway.entityDB.TBLNewTracking;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NewTrackingRepository {
    private final String url = getDatabase().getUrl();
    private final String username = getDatabase().getUsername();
    private final String password = getDatabase().getPassword();

    public NewTrackingRepository() {
    }

    public Database getDatabase() {
        Database database = new Database();
        database.getUrl();
        database.getUsername();
        database.getPassword();
        return database;
    }

    public List<TBLNewTracking> findAllOrderNotInTBLDelivery() {
        List<TBLNewTracking> newTrackingList = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "SELECT order_id, tracking_number, partner_tracking_number " +
                    "FROM tblnewtracking " +
                    "WHERE LENGTH(partner_tracking_number) > 0 " +
                    "AND order_id NOT IN (SELECT order_number FROM tbldelivery);";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLNewTracking tblNewTracking = new TBLNewTracking();
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("partner_tracking_number"));
                newTrackingList.add(tblNewTracking);
            }
            conn.setAutoCommit(false);
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return newTrackingList;
    }

    public List<TBLNewTracking> findAllOrderToUpdate() {
        List<TBLNewTracking> newTrackingList = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "SELECT order_number as order_id, tracking_number, partner_tracking_number " +
                    "FROM tbldelivery " +
                    "WHERE length(carrier_code) > 0 " +
                    "AND length(order_number) > 0 " +
                    "AND status NOT IN ('delivered');";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLNewTracking tblNewTracking = new TBLNewTracking();
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("partner_tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("tracking_number"));
                newTrackingList.add(tblNewTracking);
            }
            conn.setAutoCommit(false);
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return newTrackingList;
    }
}
