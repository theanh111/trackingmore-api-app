package com.printway.entityDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TBLTracking {
    private String trackingNumber;
    private String orderId;
    private String events;
    private String status;
    private Date dtimeEntered;
    private String whoEntered;
    private String lastEvents;
    private String transitTime;
    private String carrierCode;
    private String trackingUrl;

    public TBLTracking createTBLTrackingObject(String tracking_number, String order_id, String events, String status, Date dtime_entered, String last_events, String transit_time, String carrier_code, String tracking_url) {
        TBLTracking tblTracking = new TBLTracking();
        tblTracking.setWhoEntered("admin");
        tblTracking.setTrackingNumber(tracking_number);
        tblTracking.setOrderId(order_id);
        tblTracking.setEvents(events);
        tblTracking.setStatus(status);
        tblTracking.setTransitTime(transit_time);
        tblTracking.setLastEvents(last_events);
        tblTracking.setDtimeEntered(dtime_entered);
        tblTracking.setCarrierCode(carrier_code);
        tblTracking.setTrackingUrl(tracking_url);
        return tblTracking;
    }
}
