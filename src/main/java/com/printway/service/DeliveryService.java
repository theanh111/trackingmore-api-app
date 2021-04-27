package com.printway.service;

import com.printway.entityDB.TBLDelivery;
import com.printway.repository.DeliveryRepository;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    private DeliveryRepository deliveryRepository = new DeliveryRepository();

    public void insert(TBLDelivery delivery) {
        deliveryRepository.insert(delivery);
    }

    public void update(TBLDelivery delivery) {
        deliveryRepository.update(delivery);
    }

    public String findStatusByTrackingNumber(String trackingNumber) {
        return deliveryRepository.findStatusByTrackingNumber(trackingNumber);
    }
}
