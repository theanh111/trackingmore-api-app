package com.printway.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackInfo {
    private String Date;
    private String StatusDescription;
    private String Details;
    private String checkpoint_status;
}
