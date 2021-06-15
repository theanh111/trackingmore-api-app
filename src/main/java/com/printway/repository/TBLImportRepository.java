package com.printway.repository;

import com.printway.config.DBConnection;
import com.printway.entityDB.TBLImportTracking;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TBLImportRepository {

    public TBLImportRepository() {
    }

    public List<TBLImportTracking> findAllOrderNotInTBLTracking() {
        return DBConnection.getInstance().findAllOrderNotInTBLTracking();
    }

    public List<TBLImportTracking> findAllOrderToUpdate() {
        return DBConnection.getInstance().findAllOrderToUpdate();
    }

}
