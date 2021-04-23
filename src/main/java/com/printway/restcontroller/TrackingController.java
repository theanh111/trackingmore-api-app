package com.printway.restcontroller;

import com.printway.api.TrackingmoreAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/tracking/")
public class TrackingController {
    @Autowired
    private TrackingmoreAPI trackingmoreAPI;


    @RequestMapping(value = "update", method = RequestMethod.GET)
    public ResponseEntity<?> updateDelivery() {
        try {
            trackingmoreAPI.check();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done Update Delivery");
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "save", method = RequestMethod.GET)
    public ResponseEntity<?> saveShipment() {
        try {
            trackingmoreAPI.check();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done Save");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
