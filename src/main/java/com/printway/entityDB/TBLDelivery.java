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
}
