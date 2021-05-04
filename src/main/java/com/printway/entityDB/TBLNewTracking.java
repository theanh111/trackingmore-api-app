package com.printway.entityDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TBLNewTracking {
    private Integer id;
    private String orderId;
    private String orderCode;
    private String shippingName;
    private String shippingAddress1;
    private String shippingAddress2;
    private String shippingCity;
    private String shippingZip;
    private String shippingProvince;
    private String shippingCountry;
    private String shippingPhone;
    private String productType;
    private String variant;
    private Integer totalPackages;
    private String partnerTrackingNumber;
    private String trackingNumber;
    private String multranLog;
    private String foldeLabel;
    private String partnerId;
    private Integer labeled;
    private Integer shipped;
    private String noted;
    private String urlLabel;
    private Integer statusTracking;
    private Date dtimeEntered;
    private String whoEntered;
    private String boxId;
    private Integer boxShipped;
    private Date dtimeShipped;
    private Date dtimeLabeled;
    private Integer totalPrintQuantity;
    private String whoLastChanged;
    private Date dtimeLastChanged;
    private String material;
    private String canceled;
}
