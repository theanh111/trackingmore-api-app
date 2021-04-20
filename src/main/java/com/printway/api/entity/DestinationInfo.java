package com.printway.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationInfo {
    private String carrier_code;
    private TrackInfo[] trackinfo;
}
