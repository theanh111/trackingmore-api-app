package com.printway.repository;

import com.printway.config.DBConnection;
import com.printway.entityDB.TBLDelivery;
import com.printway.entityDB.TBLNewTracking;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NewTrackingRepository {
    public NewTrackingRepository() {
    }

    public List<TBLNewTracking> findAllOrderNotInTBLDelivery() {
        return DBConnection.getInstance().findAllOrderNotInTBLDelivery();
    }

    public List<TBLNewTracking> findAllOrderToUpdate() {
        return DBConnection.getInstance().findAllOrderToUpdate();
    }


    public boolean checkOrderIdInTBLNewtracking(String order_id, String dateReceived) {
        return DBConnection.getInstance().checkOrderIdInTBLNewtracking(order_id, dateReceived);
    }

    public TBLNewTracking findOneOrderToAdd(String orderId, String dateReceived) {
        return DBConnection.getInstance().findOneOrderToAdd(orderId, dateReceived);
    }


    public TBLNewTracking findOneOrderToUpdate(String orderId, String dateReceived) {
        return DBConnection.getInstance().findOneOrderToUpdate(orderId, dateReceived);
    }

}
