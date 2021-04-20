package com.printway.api.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class Data {
    private String id;
    private String tracking_number;
    private String carrier_code;
    private String status;
    private String created_at;
    private String updated_at;
    private String original_country;
    private String destination_country;
    private String itemTimeLength;
    private OriginInfo origin_info;
    private DestinationInfo destination_info;
    private String lastEvent;
}
