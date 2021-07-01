package com.printway.entityDB;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TBLNeworders {
    private String order_id;
    private String date_received;
    private String dtime_entered;
}
