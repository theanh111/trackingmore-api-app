package com.printway.service;

import com.printway.entityDB.TBLDelivery;
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

    public boolean checkOrderIdInTBLNewtracking(String order_id, String dateReceived) {
        return newTrackingRepository.checkOrderIdInTBLNewtracking(order_id, dateReceived);
    }

    public TBLNewTracking findOneOrderToAdd(String orderId, String dateReceived) {
        return newTrackingRepository.findOneOrderToAdd(orderId, dateReceived);
    }

    public TBLNewTracking findOneOrderToUpdate(String orderId, String dateReceived) {
        return newTrackingRepository.findOneOrderToUpdate(orderId, dateReceived);
    }
}