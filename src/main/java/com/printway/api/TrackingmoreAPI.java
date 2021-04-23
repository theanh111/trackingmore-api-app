package com.printway.api;


import com.printway.entityDB.TBLDelivery;
import com.printway.entityDB.TBLNewTracking;
import com.printway.service.DeliveryService;
import com.printway.service.NewTrackingService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TrackingmoreAPI {
    private final String TRACKING_MORE_API = "https://api.trackingmore.com";
    private final String HEADER_KEY = "Trackingmore-Api-Key";
    private final String APP_KEY = "fdb1ff44-e345-410a-9516-ecb832935cbd";
    private final String BACKUP_APP1_KEY = "a73c5e98-a120-4654-9e4b-21f3154106aa";
    private final String DETECT_COURIER = "/v2/carriers/detect";
    private final String CREATE_TRACKING = "/v2/trackings/post";
    private final String GET_RESULT = "/v2/trackings/";
    private final String[] CARRIERS = {"dhl", "ups", "usps", "dhlglobalmail"};

    private TBLDelivery tblDeliveryObj = new TBLDelivery();
    private DeliveryService deliveryService = new DeliveryService();
    private NewTrackingService newTrackingService = new NewTrackingService();

    public void check() {
        try {
            List<TBLNewTracking> listNewTracking = newTrackingService.findAll();
            for (int i = 0; i < listNewTracking.size(); i++) {
                System.out.println(i + ") == TBLNEWTRACKING == [ orderId = "
                        + listNewTracking.get(i).getOrderId()
                        + " ], [ partnerTrackingNumber = "
                        + listNewTracking.get(i).getPartnerTrackingNumber()
                        + " ], [ trackingNumber = "
                        + listNewTracking.get(i).getTrackingNumber()
                        + " ]"
                );
            }
            System.out.println("--------------------------------------------");
            for (int i = 0; i < listNewTracking.size(); i++) {
                String tblDeliveryStatus = deliveryService.findStatusByTrackingNumber(listNewTracking.get(i).getPartnerTrackingNumber());
                if (tblDeliveryStatus != null && !tblDeliveryStatus.equals("delivered")) {
                    String carrierCode = getTrackingCarrier(listNewTracking.get(i).getPartnerTrackingNumber());
                    getTrackingResult(carrierCode, listNewTracking.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getTrackingResult(String carrier, TBLNewTracking tblNewTracking) {
        try {
            Date dateCurrent = new Date(System.currentTimeMillis());
            String trackingNumber = tblNewTracking.getPartnerTrackingNumber();
            String partnerTrackingNumber = tblNewTracking.getTrackingNumber();
            String orderId = tblNewTracking.getOrderId();
            String statusTBLDelivery = deliveryService.findStatusByTrackingNumber(trackingNumber);
            System.out.println("TBL Delivery Status: " + statusTBLDelivery);
            System.out.println("== Getting Result: " + trackingNumber);
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(TRACKING_MORE_API + GET_RESULT + carrier + "/" + trackingNumber);
            httpGet.setHeader(HEADER_KEY, APP_KEY);
            HttpResponse httpResponse = client.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            JSONObject responseObj = new JSONObject(result);
            JSONObject metaObject = new JSONObject(responseObj.get("meta").toString());
            if (Integer.parseInt(metaObject.get("code").toString()) == 4031) {
                createTracking(trackingNumber, carrier);
                client.close();
            }
            if (Integer.parseInt(metaObject.get("code").toString()) == 200) {
                if (statusTBLDelivery != null) {
                    if (!statusTBLDelivery.equals("delivered")) {
                        checkTrackingInfoToUpdate(responseObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                    }
                }
                if (statusTBLDelivery == null) {
                    checkTrackingInfoToSave(responseObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTracking(String trackingNumber, String carrierCode) {
        try {
            System.out.println("Create");
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(TRACKING_MORE_API + CREATE_TRACKING);
            JSONObject payload = new JSONObject();
            payload.put("tracking_number", trackingNumber);
            payload.put("carrier_code", carrierCode);
            httpPost.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
            httpPost.setHeader(HEADER_KEY, APP_KEY);
            client.execute(httpPost);
            System.out.println("=== POST [" + trackingNumber + " ] TRACKING TO TRACKING MORE ===");
            System.out.println("--------------------------------------------");
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTrackingCarrier(String trackingNumber) {
        String carrierCode = "";
        try {
            System.out.println("== Getting Tracking Carrier: " + trackingNumber);
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(TRACKING_MORE_API + DETECT_COURIER);
            JSONObject payload = new JSONObject();
            payload.put("tracking_number", trackingNumber);
            httpPost.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
            httpPost.setHeader(HEADER_KEY, APP_KEY);
            HttpResponse httpResponse = client.execute(httpPost);
            String content = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
            JSONObject jsonObject = new JSONObject(content);
            JSONObject meta = new JSONObject(jsonObject.get("meta").toString());
            if (Integer.parseInt(meta.get("code").toString()) == 200) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                List<JSONObject> list = new ArrayList(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getJSONObject(i));
                    JSONObject carrier = new JSONObject(jsonArray.getJSONObject(i).toString());
                    String carrier_code = carrier.get("code").toString();
                    for (int j = 0; j < CARRIERS.length; j++) {
                        if (CARRIERS[j].equals("dhlglobalmail") && carrier_code.equals(CARRIERS[j])) {
                            carrierCode = "dhlglobalmail";
                        }
                    }
                }
            }
            client.close();
            return carrierCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return carrierCode;
    }

    public void checkTrackingInfoToSave(JSONObject responseObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        JSONObject dataObj = new JSONObject(responseObj.get("data").toString());
        JSONObject originObj = new JSONObject(dataObj.get("origin_info").toString());
        JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
        boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
        boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
        if (!originTrackinfoIsNull && !destinationTrackinfoIsNull) {
            insertBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        }
        if (originTrackinfoIsNull && !destinationTrackinfoIsNull
                || !originTrackinfoIsNull && destinationTrackinfoIsNull) {
            System.out.println(" -- ORIGIN or DESTINATION TrackInfo Is Null --");
            if (originObj.get("trackinfo") != JSONObject.NULL && destinationObj.get("trackinfo") == JSONObject.NULL) {
                insertOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
            }
            if (originObj.get("trackinfo") == JSONObject.NULL && destinationObj.get("trackinfo") != JSONObject.NULL) {
                insertDestinationEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
            }
        }
    }

    public void checkTrackingInfoToUpdate(JSONObject responseObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        JSONObject dataObj = new JSONObject(responseObj.get("data").toString());
        JSONObject originObj = new JSONObject(dataObj.get("origin_info").toString());
        JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
        boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
        boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
        if (!originTrackinfoIsNull && !destinationTrackinfoIsNull) {
            updateBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        }
        if (originTrackinfoIsNull && !destinationTrackinfoIsNull
                || !originTrackinfoIsNull && destinationTrackinfoIsNull) {
            System.out.println(" -- ORIGIN or DESTINATION TrackInfo Is Null --");
            if (originObj.get("trackinfo") != JSONObject.NULL && destinationObj.get("trackinfo") == JSONObject.NULL) {
                updateOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
            }
            if (originObj.get("trackinfo") == JSONObject.NULL && destinationObj.get("trackinfo") != JSONObject.NULL) {
                updateDestinationEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
            }

        }
    }

    public void insertBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        TBLDelivery tblDelivery = getBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.insert(tblDelivery);
        System.out.println("*** INSERTED TO DB ***");
        System.out.println("--------------------------------------------");
    }

    public void insertOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        System.out.println("destinationObj TrackInfo NULL == SAVE originObj");
        TBLDelivery tblDelivery = getOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.insert(tblDelivery);
        System.out.println("*** INSERTED TO DB [Save Only ORIGIN]  ***");
        System.out.println("--------------------------------------------");
    }

    public void insertDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        System.out.println("originObj TrackInfo NULL == SAVE destinationObj");
        TBLDelivery tblDelivery = getDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.insert(tblDelivery);
        System.out.println("*** INSERTED TO DB [Save Only DESTINATION]  ***");
        System.out.println("--------------------------------------------");
    }

    public void updateBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        TBLDelivery tblDelivery = getBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.update(tblDelivery);
        System.out.println("*** UPDATE TO DB ***");
        System.out.println("--------------------------------------------");
    }

    public void updateOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        System.out.println("destinationObj TrackInfo NULL == SAVE originObj");
        TBLDelivery tblDelivery = getOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.update(tblDelivery);
        System.out.println("*** UPDATE TO DB [UPDATE Only ORIGIN]  ***");
        System.out.println("--------------------------------------------");
    }

    public void updateDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        System.out.println("originObj TrackInfo NULL == SAVE destinationObj");
        TBLDelivery tblDelivery = getDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.update(tblDelivery);
        System.out.println("*** UPDATE TO DB [UPDATE Only DESTINATION]  ***");
        System.out.println("--------------------------------------------");
    }

    public TBLDelivery getOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        JSONArray originArray = originObj.getJSONArray("trackinfo");
        JSONObject events = new JSONObject();
        events.put("origin", originArray);
        String carrierCode = "";
        String status = "";
        String transitTime = "";
        String lastEvent = "";
        String carrierCodeOrigin = "";
        try {
            carrierCode = dataObj.getString("carrier_code");
            status = dataObj.getString("status");
            transitTime = dataObj.getString("itemTimeLength");
            lastEvent = dataObj.getString("lastEvent");
            carrierCodeOrigin = originObj.getString("carrier_code");
            System.out.println(" - Status: " + status);
            System.out.println(" - Transit Time: " + transitTime);
            System.out.println(" - Carrier Code");
            System.out.println();
            System.out.println("** Origin - Tracking Info **");
            System.out.println(" - [Origin] Carrier Code: " + carrierCodeOrigin);
            System.out.println(" - [Origin] Tracking Info: ");
            for (int i = 0; i < originArray.length(); i++) {
                System.out.println(originArray.getJSONObject(i));
            }
            System.out.println(" - Last Event: " + lastEvent);
        } catch (JSONException jsonException) {
            System.out.println("JSON error");
        }

        TBLDelivery tblDelivery = tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode);
        return tblDelivery;
    }

    public TBLDelivery getDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        JSONArray destinationArray = destinationObj.getJSONArray("trackinfo");
        JSONObject events = new JSONObject();
        events.put("destination", destinationArray);
        String carrierCode = "";
        String status = "";
        String transitTime = "";
        String lastEvent = "";
        String carrierCodeDestination = "";
        try {
            carrierCode = dataObj.getString("carrier_code");
            status = dataObj.getString("status");
            transitTime = dataObj.getString("itemTimeLength");
            lastEvent = dataObj.getString("lastEvent");
            carrierCodeDestination = destinationObj.getString("carrier_code");

            System.out.println(" - Status: " + status);
            System.out.println(" - Transit Time: " + transitTime);
            System.out.println(" - Carrier Code");
            System.out.println();
            System.out.println(" ** Destination - Tracking Info ** ");
            System.out.println(" - [Destination] Carrier Code : " + carrierCodeDestination);
            System.out.println(" - [Destination] Tracking Info : ");
            for (int i = 0; i < destinationArray.length(); i++) {
                System.out.println(destinationArray.getJSONObject(i));
            }
            System.out.println();
            System.out.println(" - Last Event: " + lastEvent);
        } catch (JSONException jsonException) {
            System.out.println("JSON error");
        }

        TBLDelivery tblDelivery = tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode);
        return tblDelivery;
    }

    public TBLDelivery getBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        JSONArray originArray = originObj.getJSONArray("trackinfo");
        JSONArray destinationArray = destinationObj.getJSONArray("trackinfo");
        JSONObject events = new JSONObject();
        events.put("origin", originArray);
        events.put("destination", destinationArray);
        String carrierCode = "";
        String status = "";
        String transitTime = "";
        String lastEvent = "";
        String carrierCodeOrigin = "";
        String carrierCodeDestination = "";
        try {
            carrierCode = dataObj.getString("carrier_code");
            status = dataObj.getString("status");
            transitTime = dataObj.getString("itemTimeLength");
            lastEvent = dataObj.getString("lastEvent");
            carrierCodeOrigin = originObj.getString("carrier_code");
            carrierCodeDestination = destinationObj.getString("carrier_code");

            System.out.println(" - Status: " + status);
            System.out.println(" - Transit Time: " + transitTime);
            System.out.println(" - Carrier Code");
            System.out.println();
            System.out.println("** Origin - Tracking Info **");
            System.out.println(" - [Origin] Carrier Code: " + carrierCodeOrigin);
            System.out.println(" - [Origin] Tracking Info: ");
            for (int i = 0; i < originArray.length(); i++) {
                System.out.println(originArray.getJSONObject(i));
            }
            System.out.println();
            System.out.println("** Destination - Tracking Info **");
            System.out.println(" - [Destination] Carrier Code: " + carrierCodeDestination);
            System.out.println(" - [Destination] Tracking Info: ");
            for (int i = 0; i < destinationArray.length(); i++) {
                System.out.println(destinationArray.getJSONObject(i));
            }
            System.out.println();
            System.out.println(" - Last Event: " + lastEvent);
        } catch (JSONException jsonException) {
            System.out.println("JSON error");
        }
        TBLDelivery tblDelivery = tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode);
        return tblDelivery;
    }
}
