package com.printway.entityDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tblnewtracking")
public class TBLNewTracking {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "order_id")
    private String orderId;
    @Column(name = "order_code")
    private String orderCode;
    @Column(name = "shipping_name")
    private String shippingName;
    @Column(name = "shipping_address1")
    private String shippingAddress1;
    @Column(name = "shipping_address2")
    private String shippingAddress2;
    @Column(name = "shipping_city")
    private String shippingCity;
    @Column(name = "shipping_zip")
    private String shippingZip;
    @Column(name = "shipping_province")
    private String shippingProvince;
    @Column(name = "shipping_country")
    private String shippingCountry;
    @Column(name = "shipping_phone")
    private String shippingPhone;
    @Column(name = "product_type")
    private String productType;
    @Column(name = "variant")
    private String variant;
    @Column(name = "total_packages")
    private Integer totalPackages;
    @Column(name = "partner_tracking_number")
    private String partnerTrackingNumber;
    @Column(name = "tracking_number")
    private String trackingNumber;
    @Column(name = "multran_log")
    private String multranLog;
    @Column(name = "folder_label")
    private String foldeLabel;
    @Column(name = "partner_id")
    private String partnerId;
    @Column(name = "labeled")
    private Integer labeled;
    @Column(name = "shipped")
    private Integer shipped;
    @Column(name = "noted")
    private String noted;
    @Column(name = "url_label")
    private String urlLabel;
    @Column(name = "status_tracking")
    private Integer statusTracking;
    @Column(name = "dtime_entered")
    private Date dtimeEntered;
    @Column(name = "who_entered")
    private String whoEntered;
    @Column(name = "box_id")
    private String boxId;
    @Column(name = "box_shipped")
    private Integer boxShipped;
    @Column(name = "dtime_shipped")
    private Date dtimeShipped;
    @Column(name = "dtime_labeled")
    private Date dtimeLabeled;
    @Column(name = "total_print_quantity")
    private Integer totalPrintQuantity;
    @Column(name = "who_last_changed")
    private String whoLastChanged;
    @Column(name = "dtime_last_changed")
    private Date dtimeLastChanged;
    @Column(name = "material")
    private String material;
    @Column(name = "canceled")
    private String canceled;
}
