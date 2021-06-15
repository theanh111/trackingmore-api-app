package com.printway.service;

import com.printway.entityDB.TBLTracking;
import com.printway.repository.TBLTrackingRepository;
import org.springframework.stereotype.Service;

@Service
public class TBLTrackingService {

    private TBLTrackingRepository TBLTrackingRepository = new TBLTrackingRepository();

    public void insert(TBLTracking delivery) {
        TBLTrackingRepository.insert(delivery);
    }

    public void update(TBLTracking delivery) {
        TBLTrackingRepository.update(delivery);
    }

    public String findStatusByTrackingNumber(String trackingNumber) {
        return TBLTrackingRepository.findStatusByTrackingNumber(trackingNumber);
    }
}
