package com.printway.service;

import com.printway.entityDB.TBLNewTracking;
import com.printway.repository.NewTrackingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewTrackingService {

    private NewTrackingRepository newTrackingRepository = new NewTrackingRepository();

    public List<TBLNewTracking> findAllOrderNotInTBLDelivery() {
        return newTrackingRepository.findAllOrderNotInTBLDelivery();
    }

    public List<TBLNewTracking> findAllOrderToUpdateInTBLDelivery() {
        return newTrackingRepository.findAllOrderToUpdate();
    }
}