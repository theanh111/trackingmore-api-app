package com.printway.entityDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbldelivery")
public class TBLDelivery {
    @Id
    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "partner_tracking_number")
    private String partnerTrackingNumber;

    @Column(name = "events")
    @Type(type = "text")
    private String events;

    @Column(name = "status")
    private String status;

    @Column(name = "dtime_entered")
    private Date dtimeEntered;

    @Column(name = "who_entered")
    private String whoEntered;

    @Column(name = "last_events")
    private String lastEvents;

    @Column(name = "transit_time")
    private String transitTime;

    @Column(name = "carrier_code")
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
