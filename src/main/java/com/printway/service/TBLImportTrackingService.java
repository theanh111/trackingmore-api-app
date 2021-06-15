package com.printway.service;

import com.printway.entityDB.TBLImportTracking;
import com.printway.repository.TBLImportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TBLImportTrackingService {

    private TBLImportRepository TBLImportRepository = new TBLImportRepository();

    public List<TBLImportTracking> findAllOrderNotInTBLTracking() {
        return TBLImportRepository.findAllOrderNotInTBLTracking();
    }

    public List<TBLImportTracking> findAllOrderToUpdate() {
        return TBLImportRepository.findAllOrderToUpdate();
    }
}