package com.printway.service;


import com.printway.entityDB.TBLDelivery;
import com.printway.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<String> findAllTrackingNumber() {
        return deliveryRepository.findAllTrackingNumber();
    }

    public TBLDelivery findOne(String trackingNumber) {
        return deliveryRepository.findOne(trackingNumber);
    }
}
