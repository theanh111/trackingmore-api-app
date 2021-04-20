//package com.printway.restcontroller;
//
//import com.google.gson.Gson;
//import com.printway.api.config.TrackingmoreAPI;
//import com.printway.api.entityAPI.Shipment;
//import com.printway.entityDB.TBLDelivery;
//import com.printway.service.newtracking.NewTrackingService;
//import com.printway.service.trackinginfo.TrackingInfoService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.concurrent.TimeUnit;
//
//@RestController
//@RequestMapping(value = "/shipment/")
//public class ShipmentController {
//    @Autowired
//    private TrackingmoreAPI trackingmoreAPI;
//
//    @Autowired
//    private TrackingInfoService trackingInfoService;
//
//    @Autowired
//    private NewTrackingService newTrackingService;
//
//    @RequestMapping(value = "save", method = RequestMethod.GET)
//    public ResponseEntity<?> saveShipment() throws IOException, ParseException, InterruptedException {
//        System.out.println("Start: " + System.currentTimeMillis());
//        Date systemCurrentTime = new Date(System.currentTimeMillis());
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String dateString = formatter.format(systemCurrentTime);
//        Date dateCurrent = formatter.parse(dateString);
//        Iterator<String> listTrackingNumberIterator = newTrackingService.findAllTrackingNumber().iterator();
//        Iterator<String> listShipmentIdIterator = trackingInfoService.findAllShipmentId().iterator();
//        String trackingNumber;
//        String shipmentId;
//        while (listTrackingNumberIterator.hasNext()) {
//            trackingNumber = listTrackingNumberIterator.next();
//            if (trackingNumber != null && !trackingNumber.equals("")) {
//                Shipment shipment = trackingmoreAPI.getTrackingInfo(trackingNumber);
//                TimeUnit.SECONDS.sleep(2);
//                while (listShipmentIdIterator.hasNext()) {
//                    shipmentId = listShipmentIdIterator.next();
//                    if (shipmentId.equals(trackingNumber)) {
//                        if (trackingInfoService.findById(trackingNumber).isPresent()) {
//                            updateTrackingInfo(trackingNumber, shipment, dateCurrent);
//                        }
//                    }
//                }
//                saveNewTrackingInfo(trackingNumber, shipment, dateCurrent);
//            }
//        }
//        System.out.println("End: " + System.currentTimeMillis());
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    public void saveNewTrackingInfo(String trackingNumber, Shipment shipment, Date dateCurrent) {
//        Gson gson = new Gson();
//        TBLDelivery TBLDelivery = new TBLDelivery();
//        TBLDelivery.setShipmentId(trackingNumber);
//        TBLDelivery.setStatus(gson.toJson(shipment.getStatus()));
//        TBLDelivery.setEvents(gson.toJson(shipment.getEvents()));
//        TBLDelivery.setDtimeEntered(dateCurrent);
//        trackingInfoService.save(TBLDelivery);
//    }
//
//    public void updateTrackingInfo(String trackingNumber, Shipment shipment, Date dateCurrent) {
//        Gson gson = new Gson();
//        TBLDelivery TBLDeliveryHasId = trackingInfoService.findById(trackingNumber).get();
//        TBLDeliveryHasId.setStatus(gson.toJson(shipment.getStatus()));
//        TBLDeliveryHasId.setEvents(gson.toJson(shipment.getEvents()));
//        TBLDeliveryHasId.setDtimeEntered(dateCurrent);
//        trackingInfoService.save(TBLDeliveryHasId);
//    }
//}
