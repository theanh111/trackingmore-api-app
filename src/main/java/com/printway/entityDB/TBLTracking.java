//package com.printway.entityDB;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.Type;
//
//import javax.persistence.*;
//import java.util.Date;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(name = "tbltracking")
//public class TBLTracking {
//    @Id
//    @Column(name = "order_id")
//    private String orderId;
//
//    @Id
//    @Column(name = "tracking_number")
//    private String trackingNumber;
//
//    @Column(name = "partner_tracking_number")
//    private String partnerTrackingNumber;
//
//    @Column(name = "dtime_entered")
//    private Date dtimeEntered;
//
//    @Column(name = "who_entered")
//    private String whoEntered;
//
//    @Column(name = "events")
//    @Type(type = "text")
//    private String events;
//
//    @Column(name = "status")
//    private String status;
//
//    @Column(name = "last_events")
//    private String lastEvents;
//
//    @Column(name = "transit_time")
//    private String transitTime;
//
//}
