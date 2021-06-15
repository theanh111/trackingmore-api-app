package com.printway.repository;

import com.printway.config.DBConnection;
import com.printway.entityDB.TBLTracking;
import org.springframework.stereotype.Repository;

@Repository
public class TBLTrackingRepository {

    public TBLTrackingRepository() {
    }

    public void insert(TBLTracking tblTracking) {
        DBConnection.getInstance().insert(tblTracking);
    }


    public void update(TBLTracking tblTracking) {
        DBConnection.getInstance().update(tblTracking);
    }


    public String findStatusByTrackingNumber(String trackingNumber) {
        return DBConnection.getInstance().findStatusByTrackingNumber(trackingNumber);
    }

}
