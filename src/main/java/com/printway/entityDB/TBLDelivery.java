package com.printway.entityDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TBLDelivery {
    private String trackingNumber;
    private String orderNumber;
    private String partnerTrackingNumber;
    private String events;
    private String status;
    private Date dtimeEntered;
    private String whoEntered;
    private String lastEvents;
    private String transitTime;
    private String carrierCode;

    public TBLDelivery createDeliveryObject(String tracking_number, String order_number, String partner_tracking_number, String events, String status, Date dtime_entered, String last_events, String transit_time,String carrier_code) {
        TBLDelivery tblDelivery = new TBLDelivery();
        tblDelivery.setTrackingNumber(tracking_number);
        tblDelivery.setPartnerTrackingNumber(partner_tracking_number);
        tblDelivery.setOrderNumber(order_number);
        tblDelivery.setEvents(events);
        tblDelivery.setStatus(status);
        tblDelivery.setTransitTime(transit_time);
        tblDelivery.setLastEvents(last_events);
        tblDelivery.setDtimeEntered(dtime_entered);
        tblDelivery.setCarrierCode(carrier_code);
        return tblDelivery;
    }
}
