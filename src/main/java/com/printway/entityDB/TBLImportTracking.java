package com.printway.entityDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TBLImportTracking {
    private Integer id;
    private String orderId;
    private String trackingNumber;
    private Date dtimeEntered;
    private String whoEntered;
    private String trackingCarrier;
    private String trackingCompany;
    private String trackingUrl;
    private String gearmentOrderId;
}
