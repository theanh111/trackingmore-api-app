package com.printway.repository;

import com.printway.config.DBConnection;
import com.printway.entityDB.TBLNewTracking;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NewTrackingRepository {
    public static DBConnection connection = DBConnection.getInstance();

    public NewTrackingRepository() {
    }

    public List<TBLNewTracking> findAllOrderNotInTBLDelivery() {
        return DBConnection.getInstance().findAllOrderNotInTBLDelivery();
    }

//    public List<TBLNewTracking> findAllOrderNotInTBLDelivery() {
//        List<TBLNewTracking> newTrackingList = new ArrayList<>();
//        try {
//            String sqlQuery = "SELECT order_id, tracking_number, partner_tracking_number " +
//                    "FROM tblnewtracking " +
//                    "WHERE LENGTH(partner_tracking_number) > 0 " +
//                    "AND order_id NOT IN (SELECT order_number FROM tbldelivery);";
//            PreparedStatement statement = connection.getConnection().prepareStatement(sqlQuery);
//            ResultSet resultSet = statement.executeQuery();
//            while (resultSet.next()) {
//                TBLNewTracking tblNewTracking = new TBLNewTracking();
//                tblNewTracking.setOrderId(resultSet.getString("order_id"));
//                tblNewTracking.setTrackingNumber(resultSet.getString("tracking_number"));
//                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("partner_tracking_number"));
//                newTrackingList.add(tblNewTracking);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return newTrackingList;
//    }
//

    public List<TBLNewTracking> findAllOrderToUpdate() {
        return DBConnection.getInstance().findAllOrderToUpdate();
    }

    public TBLNewTracking findOneOrderToUpdate(String orderId) {
        return DBConnection.getInstance().findOneOrderToUpdate(orderId);
    }

//    public List<TBLNewTracking> findAllOrderToUpdate() {
//        List<TBLNewTracking> newTrackingList = new ArrayList<>();
//        try {
//            String sqlQuery = "SELECT order_number as order_id, tracking_number, partner_tracking_number " +
//                    "FROM tbldelivery " +
//                    "WHERE length(carrier_code) > 0 " +
//                    "AND length(order_number) > 0 " +
//                    "AND status NOT IN ('delivered');";
//            PreparedStatement statement = connection.getConnection().prepareStatement(sqlQuery);
//            ResultSet resultSet = statement.executeQuery();
//            while (resultSet.next()) {
//                TBLNewTracking tblNewTracking = new TBLNewTracking();
//                tblNewTracking.setOrderId(resultSet.getString("order_id"));
//                tblNewTracking.setTrackingNumber(resultSet.getString("partner_tracking_number"));
//                tblNewTracking.setPartnerTrackingNumber(resultSet.getString("tracking_number"));
//                newTrackingList.add(tblNewTracking);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return newTrackingList;
//    }
}
