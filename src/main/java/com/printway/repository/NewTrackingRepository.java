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

    public List<TBLNewTracking> findAll() {
        List<TBLNewTracking> newTrackingList = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            String sqlQuery = "SELECT order_id, tracking_number, partner_tracking_number " +
                    "FROM tblnewtracking WHERE shipped = 1 " +
                    "AND (LENGTH(partner_tracking_number) > 0 OR partner_tracking_number IS NOT NULL);";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TBLNewTracking tblNewTracking = new TBLNewTracking();
                tblNewTracking.setOrderId(resultSet.getString("order_id"));
                tblNewTracking.setTrackingNumber(resultSet.getString("tracking_number"));
                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("partner_tracking_number"));
                newTrackingList.add(tblNewTracking);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return newTrackingList;
    }
}
