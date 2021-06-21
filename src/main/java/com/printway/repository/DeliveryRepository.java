package com.printway.repository;

import com.printway.config.DBConnection;
import com.printway.entityDB.TBLDelivery;
import org.springframework.stereotype.Repository;


@Repository
public class DeliveryRepository {

    public DeliveryRepository() {
    }

    public void insert(TBLDelivery delivery) {
        DBConnection.getInstance().insert(delivery);
    }

    public void update(TBLDelivery delivery) {
        DBConnection.getInstance().update(delivery);
    }

    public String findStatusByTrackingNumber(String trackingNumber, String dateReceived) {
        return DBConnection.getInstance().findStatusByTrackingNumber(trackingNumber, dateReceived);
    }

    public boolean checkOrderIdInTBLDelivery(String order_id, String dateReceived) {
        return DBConnection.getInstance().checkOrderIdInTBLDelivery(order_id, dateReceived);
    }



}
