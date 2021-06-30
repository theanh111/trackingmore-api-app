package com.printway.api;

import com.printway.config.DBConnection;
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
    private final String[] CARRIERS = {"dhl", "ups", "usps", "dhlglobalmail", "fedex", "dhl-germany", "dhl-poland", "dhl-es"};

    private TBLDelivery tblDeliveryObj = new TBLDelivery();
    private DeliveryService deliveryService = new DeliveryService();
    private NewTrackingService newTrackingService = new NewTrackingService();

//    public void getTrackingInfoByOrderId(String orderId, String dateReceived) {
//        try {
//            DBConnection.getInstance().getConnection().setAutoCommit(false);
//            boolean checkOrderIdNewTracking = newTrackingService.checkOrderIdInTBLNewtracking(orderId, dateReceived);
//            if (checkOrderIdNewTracking) {
//                System.out.println("- Order ID: [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] is EXIST IN TBL NEWTRACKING!");
//                boolean checkOrderIdDelivery = deliveryService.checkOrderIdInTBLDelivery(orderId, dateReceived);
//                if (checkOrderIdDelivery) {
//                    System.out.println("- Order ID: [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] is EXIST IN TBL DELIVERY!");
//                    TBLNewTracking tblNewTracking = newTrackingService.findOneOrderToUpdate(orderId, dateReceived);
//                    List<TBLNewTracking> listNewTracking = new ArrayList<>();
//                    listNewTracking.add(tblNewTracking);
//                    System.out.println("**Total orders have trackingnumber in TBLNewtracking to scan: " + listNewTracking.size());
//                    System.out.println("* Checking to Update...");
//                    for (int i = 0; i < listNewTracking.size(); i++) {
//                        try {
//                            TBLNewTracking tblNewTracking1 = listNewTracking.get(i);
//                            String trackingNumber = listNewTracking.get(i).getTrackingNumber();
//                            String dateReceivedTBLNewTracking = listNewTracking.get(i).getDate_received();
//                            String tblDeliveryStatus = deliveryService.findStatusByTrackingNumber(trackingNumber, dateReceivedTBLNewTracking);
//                            if (tblDeliveryStatus != null && !tblDeliveryStatus.equals("delivered")) {
//                                String carrierCode = getTrackingCarrier(tblNewTracking1);
//                                getTrackingResult(carrierCode, tblDeliveryStatus, listNewTracking.get(i));
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            continue;
//                        }
//                    }
//                } else {
//                    System.out.println("- Order ID: [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] is NOT EXIST IN TBL DELIVERY!");
//                    TBLNewTracking tblNewTracking = newTrackingService.findOneOrderToAdd(orderId, dateReceived);
//                    List<TBLNewTracking> listNewTracking = new ArrayList<>();
//                    listNewTracking.add(tblNewTracking);
//                    System.out.println("** Total orders have trackingnumber in TBLNewtracking to scan: " + listNewTracking.size());
//                    System.out.println("* Checking to Add New...");
//                    for (int i = 0; i < listNewTracking.size(); i++) {
//                        try {
//                            TBLNewTracking tblNewTracking1 = listNewTracking.get(i);
//                            String trackingNumber = tblNewTracking1.getTrackingNumber();
//                            System.out.println("- TBLNewtracking Tracking Number: " + trackingNumber);
//                            String dateReceivedTBLNewTracking = listNewTracking.get(i).getDate_received();
//                            System.out.println("- TBLNewtracking Date Received: " + dateReceivedTBLNewTracking);
//                            String tblDeliveryStatus = deliveryService.findStatusByTrackingNumber(trackingNumber, dateReceivedTBLNewTracking);
//                            if (tblDeliveryStatus == null) {
//                                String carrierCode = getTrackingCarrier(tblNewTracking1);
//                                getTrackingResult(carrierCode, tblDeliveryStatus, listNewTracking.get(i));
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            continue;
//                        }
//                    }
//                }
//            } else {
//                System.out.println("- CAN'T FIND Order ID: [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] IN TBLNEWTRACKING!");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void getTrackingInfo() {
        try {
            DBConnection.getInstance().getConnection().setAutoCommit(false);

//            List<TBLNewTracking> listNewTracking = newTrackingService.findAllOrderNotInTBLDelivery();
//            System.out.println("** Total orders have trackingnumber in TBLNewtracking to scan: " + listNewTracking.size());
//            System.out.println("* Checking to Add New...");
//            for (int i = 0; i < listNewTracking.size(); i++) {
//                try {
//                    TBLNewTracking tblNewTracking = listNewTracking.get(i);
//                    String orderId = tblNewTracking.getOrderId();
//                    String trackingNumber = tblNewTracking.getTrackingNumber();
//                    String partnerTrackingNumber = tblNewTracking.getPartnerTrackingNumber();
//                    System.out.println("- Order ID: " + orderId);
//                    System.out.println("- Tracking Number: " + trackingNumber);
//                    System.out.println("- Partner Tracking Number: " + partnerTrackingNumber);
//                    String dateReceived = tblNewTracking.getDate_received();
//                    String tblDeliveryStatus = deliveryService.findStatusByTrackingNumber(trackingNumber, dateReceived);
//                    System.out.println("- Status: " + tblDeliveryStatus);
//                    if (tblDeliveryStatus == null) {
//                        String carrierCode = getTrackingCarrier(tblNewTracking);
//                        getTrackingResult(carrierCode, tblDeliveryStatus, listNewTracking.get(i));
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    continue;
//                }
//                System.out.println();
//            }

            List<TBLNewTracking> listNewTracking = newTrackingService.findAllOrderToUpdateInTBLDelivery();
            System.out.println("** Total orders in TBLDelivery to scan: " + listNewTracking.size());
            System.out.println("* Checking to Update...");
            for (int i = 0; i < listNewTracking.size(); i++) {
                try {
                    TBLNewTracking tblNewTracking = listNewTracking.get(i);
                    String orderId = tblNewTracking.getOrderId();
                    String trackingNumber = tblNewTracking.getTrackingNumber();
                    String partnerTrackingNumber = tblNewTracking.getPartnerTrackingNumber();
                    System.out.println("- Order ID: " + orderId);
                    System.out.println("- Tracking Number: " + trackingNumber);
                    System.out.println("- Partner Tracking Number: " + partnerTrackingNumber);
                    String dateReceivedTBLNewTracking = tblNewTracking.getDate_received();
                    System.out.println("- Date Received: " + dateReceivedTBLNewTracking);
                    String tblDeliveryStatus = deliveryService.findStatusByTrackingNumber(trackingNumber, dateReceivedTBLNewTracking);
                    System.out.println("- Status: " + tblDeliveryStatus);
                    if (tblDeliveryStatus != null && !tblDeliveryStatus.equals("delivered")) {
                        String carrierCode = getTrackingCarrier(tblNewTracking);
                        getTrackingResult(carrierCode, tblDeliveryStatus, tblNewTracking);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTrackingResult(String carrier, String tblDeliveryStatus, TBLNewTracking tblNewTracking) {
        try {
            Date dateCurrent = new Date(System.currentTimeMillis());
            String trackingNumber = tblNewTracking.getTrackingNumber();
            String partnerTrackingNumber = tblNewTracking.getPartnerTrackingNumber();
            String orderId = tblNewTracking.getOrderId();
            String dateReceived = tblNewTracking.getDate_received();
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(TRACKING_MORE_API + GET_RESULT + carrier + "/" + trackingNumber);
            httpGet.setHeader(HEADER_KEY, APP_KEY);
            HttpResponse httpResponse = client.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            JSONObject responseObj = new JSONObject(result);
            if (responseObj.get("meta") instanceof JSONObject) {
                JSONObject metaObject = new JSONObject(responseObj.get("meta").toString());
                if (metaObject.get("code").toString().equals("4031")) {
                    System.out.println("- [" + orderId + " --- " + dateReceived + " --- " + trackingNumber + " --- " + partnerTrackingNumber + " --- " + carrier + "] is NOT on Trackingmore.com!");
                    createTrackingOnTrackingMore(trackingNumber, carrier);
                }
                if (metaObject.get("code").toString().equals("200")) {
//                    if (tblDeliveryStatus == null) {
//                        checkTrackingInfoToSave(responseObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                    }
                    if (tblDeliveryStatus != null) {
                        if (!tblDeliveryStatus.equals("delivered")) {
                            checkTrackingInfoToUpdate(responseObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
                        }
                    }
                }
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTrackingOnTrackingMore(String trackingNumber, String carrierCode) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(TRACKING_MORE_API + CREATE_TRACKING);
            JSONObject bodyRequest = new JSONObject();
            bodyRequest.put("tracking_number", trackingNumber);
            bodyRequest.put("carrier_code", carrierCode);
            httpPost.setEntity(new StringEntity(bodyRequest.toString(), ContentType.APPLICATION_JSON));
            httpPost.setHeader(HEADER_KEY, APP_KEY);
            HttpResponse httpResponse = client.execute(httpPost);
            String content = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
            JSONObject response = new JSONObject(content);
            if (response.has("meta")) {
                JSONObject meta = new JSONObject(response.get("meta").toString());
                if (meta.has("code")) {
                    if (meta.get("code").toString().equals("200")) {
                        System.out.println("- Created Tracking [" + trackingNumber + "] to Trackingmore.com");
                    }
                    if (meta.get("code").toString().equals("4032")) {
                        System.out.println("- Cannot detect courier on Trackingmore.com!");
                    }
                    if (meta.get("code").toString().equals("4016")) {
                        System.out.println("- Tracking already exists on Trackingmore.com!");
                    }
                }
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTrackingCarrier(TBLNewTracking tblNewTracking) {
        String carrierCode = null;
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(TRACKING_MORE_API + DETECT_COURIER);
            JSONObject bodyRequest = new JSONObject();
            bodyRequest.put("tracking_number", tblNewTracking.getTrackingNumber());
            httpPost.setEntity(new StringEntity(bodyRequest.toString(), ContentType.APPLICATION_JSON));
            httpPost.setHeader(HEADER_KEY, APP_KEY);
            HttpResponse httpResponse = client.execute(httpPost);
            String content = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
            JSONObject jsonObject = new JSONObject(content);
            JSONObject meta = new JSONObject(jsonObject.get("meta").toString());
            if (Integer.parseInt(meta.get("code").toString()) == 200) {
                if (jsonObject.has("data")) {
                    if (jsonObject.get("data") instanceof JSONArray) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        if (tblNewTracking.getPartnerTrackingNumber() != null) {
                            if (tblNewTracking.getPartnerTrackingNumber().startsWith("PW")) {
                                System.out.println("- From USPS: " + tblNewTracking.getPartnerTrackingNumber());
                                carrierCode = "usps";
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject carrier = new JSONObject(jsonArray.getJSONObject(i).toString());
                                    String code = carrier.get("code").toString();
                                    carrierCode = carrierCodeCheck(code);
                                }
                            }
                        } else {
                            if (tblNewTracking.getPartnerTrackingNumber() == null) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject carrier = new JSONObject(jsonArray.getJSONObject(i).toString());
                                    String code = carrier.get("code").toString();
                                    if (code.equals("usps")) {
                                        carrierCode = "usps";
                                        break;
                                    }
                                    if (code.equals("dhlglobalmail")) {
                                        carrierCode = "dhlglobalmail";
                                        break;
                                    }
                                    carrierCode = carrierCodeCheck(code);
                                }
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject carrier = new JSONObject(jsonArray.getJSONObject(i).toString());
                                    String code = carrier.get("code").toString();
//                                if (code.equals("usps")) {
//                                    carrierCode = "usps";
//                                    break;
//                                }
                                    carrierCode = carrierCodeCheck(code);
                                }
                            }


                        }
                        System.out.println("- Carrier: " + carrierCode);
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

//    public void checkTrackingInfoToSave(JSONObject responseObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
//        try {
//            if (responseObj.has("data")) {
//                if (responseObj.get("data") instanceof JSONObject) {
//                    JSONObject dataObj = new JSONObject(responseObj.get("data").toString());
//                    if (dataObj.has("origin_info")) {
//                        if (dataObj.get("origin_info") instanceof JSONObject) {
//                            JSONObject originObj = new JSONObject(dataObj.get("origin_info").toString());
//                            if (dataObj.has("destination_info")) {
//                                if (dataObj.get("destination_info") instanceof JSONObject) {
//                                    JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
//                                    boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
//                                    boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
//                                    if (!originTrackinfoIsNull && !destinationTrackinfoIsNull) {
//                                        insertBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                                        System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
//                                    }
//                                    if (originTrackinfoIsNull && !destinationTrackinfoIsNull
//                                            || !originTrackinfoIsNull && destinationTrackinfoIsNull) {
//                                        if (!originTrackinfoIsNull) {
//                                            insertOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                                            System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
//                                        }
//                                        if (!destinationTrackinfoIsNull) {
//                                            insertDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                                            System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
//                                        }
//                                    }
//                                    if (originTrackinfoIsNull && destinationTrackinfoIsNull) {
//                                        insertBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                                        System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    if (dataObj.has("origin_info") && !dataObj.has("destination_info")) {
//                        if (dataObj.get("origin_info") instanceof JSONObject) {
//                            JSONObject originObj = new JSONObject(dataObj.get("origin_info").toString());
//                            boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
//                            if (!originTrackinfoIsNull) {
//                                insertOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                                System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
//                            }
//                            if (originTrackinfoIsNull) {
//                                insertOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                                System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
//                            }
//                        }
//                    }
//                    if (!dataObj.has("origin_info") && dataObj.has("destination_info")) {
//                        if (dataObj.get("destination_info") instanceof JSONObject) {
//                            JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
//                            boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
//                            if (!destinationTrackinfoIsNull) {
//                                insertDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                                System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
//                            }
//
//                            if (destinationTrackinfoIsNull) {
//                                insertDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//                                System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void checkTrackingInfoToUpdate(JSONObject responseObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
        try {
            if (responseObj.has("data")) {
                if (responseObj.get("data") instanceof JSONObject) {
                    JSONObject dataObj = new JSONObject(responseObj.get("data").toString());
                    if (dataObj.has("origin_info")) {
                        if (dataObj.get("origin_info") instanceof JSONObject) {
                            JSONObject originObj = new JSONObject(dataObj.get("origin_info").toString());
                            if (dataObj.has("destination_info")) {
                                if (dataObj.get("destination_info") instanceof JSONObject) {
                                    JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
                                    boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
                                    boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
                                    if (!originTrackinfoIsNull && !destinationTrackinfoIsNull) {
                                        updateBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
                                        System.out.println("- UPDATED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
                                    }
                                    if (originTrackinfoIsNull && !destinationTrackinfoIsNull
                                            || !originTrackinfoIsNull && destinationTrackinfoIsNull) {
                                        if (!originTrackinfoIsNull) {
                                            updateOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
                                            System.out.println("- UPDATED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
                                        }
                                        if (!destinationTrackinfoIsNull) {
                                            updateDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
                                            System.out.println("- UPDATED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
                                        }
                                    }
                                    if (originTrackinfoIsNull && destinationTrackinfoIsNull) {
                                        updateBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
                                        System.out.println("- UPDATED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "] - DATE RECEIVED: [" + dateReceived + "] - TRACKING NUMBER: [" + trackingNumber + "]");
                                    }
                                }
                            }

                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TBLDelivery getOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
        if (originObj.get("trackinfo") instanceof JSONArray) {
            JSONArray originArray = originObj.getJSONArray("trackinfo");
            JSONObject events = new JSONObject();
            events.put("origin", originArray);
            String carrierCode = "";
            String status = "";
            String transitTime = "";
            String lastEvent = "";
            try {
                if (dataObj.get("itemTimeLength") != JSONObject.NULL) {
                    transitTime = dataObj.getString("itemTimeLength");
                } else {
                    transitTime = "No Info";
                }
                if (dataObj.get("carrier_code") == JSONObject.NULL || dataObj.get("carrier_code").equals("")) {
                    carrierCode = "No Info";
                } else {
                    carrierCode = dataObj.getString("carrier_code");
                }
                if (dataObj.get("lastEvent") == JSONObject.NULL || dataObj.get("lastEvent").equals("")) {
                    lastEvent = "No Info";
                } else {
                    lastEvent = dataObj.getString("lastEvent");
                }
                if (dataObj.get("status") == JSONObject.NULL || dataObj.get("status").equals("")) {
                    status = "No Info";
                } else {
                    status = dataObj.getString("status");
                }
                return tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, dateReceived);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        if (originObj.get("trackinfo") == JSONObject.NULL) {
            JSONObject events = new JSONObject();
            events.put("origin", "No Info");
            String carrierCode = "";
            String status = "";
            String transitTime = "";
            String lastEvent = "";
            try {
                if (dataObj.get("itemTimeLength") != JSONObject.NULL) {
                    transitTime = dataObj.getString("itemTimeLength");
                } else {
                    transitTime = "No Info";
                }
                if (dataObj.get("carrier_code") == JSONObject.NULL || dataObj.get("carrier_code").equals("")) {
                    carrierCode = "No Info";
                } else {
                    carrierCode = dataObj.getString("carrier_code");
                }
                if (dataObj.get("lastEvent") == JSONObject.NULL || dataObj.get("lastEvent").equals("")) {
                    lastEvent = "No Info";
                } else {
                    lastEvent = dataObj.getString("lastEvent");
                }
                if (dataObj.get("status") == JSONObject.NULL || dataObj.get("status").equals("")) {
                    status = "No Info";
                } else {
                    status = dataObj.getString("status");
                }
                return tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, dateReceived);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        return null;
    }

    public TBLDelivery getDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
        if (destinationObj.get("trackinfo") instanceof JSONArray) {
            JSONArray destinationArray = destinationObj.getJSONArray("trackinfo");
            JSONObject events = new JSONObject();
            events.put("destination", destinationArray);
            String carrierCode = "";
            String status = "";
            String transitTime = "";
            String lastEvent = "";
            try {
                if (dataObj.get("itemTimeLength") != JSONObject.NULL) {
                    transitTime = dataObj.getString("itemTimeLength");
                } else {
                    transitTime = "No Info";
                }
                if (dataObj.get("carrier_code") == JSONObject.NULL || dataObj.get("carrier_code").equals("")) {
                    carrierCode = "No Info";
                } else {
                    carrierCode = dataObj.getString("carrier_code");
                }
                if (dataObj.get("lastEvent") == JSONObject.NULL || dataObj.get("lastEvent").equals("")) {
                    lastEvent = "No Info";
                } else {
                    lastEvent = dataObj.getString("lastEvent");
                }
                if (dataObj.get("status") == JSONObject.NULL || dataObj.get("status").equals("")) {
                    status = "No Info";
                } else {
                    status = dataObj.getString("status");
                }
                return tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, dateReceived);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        if (destinationObj.get("trackinfo") == JSONObject.NULL) {
            JSONObject events = new JSONObject();
            events.put("destination", "No Info");
            String carrierCode = "";
            String status = "";
            String transitTime = "";
            String lastEvent = "";
            try {
                if (dataObj.get("itemTimeLength") != JSONObject.NULL) {
                    transitTime = dataObj.getString("itemTimeLength");
                } else {
                    transitTime = "No Info";
                }
                if (dataObj.get("carrier_code") == JSONObject.NULL || dataObj.get("carrier_code").equals("")) {
                    carrierCode = "No Info";
                } else {
                    carrierCode = dataObj.getString("carrier_code");
                }
                if (dataObj.get("lastEvent") == JSONObject.NULL || dataObj.get("lastEvent").equals("")) {
                    lastEvent = "No Info";
                } else {
                    lastEvent = dataObj.getString("lastEvent");
                }
                if (dataObj.get("status") == JSONObject.NULL || dataObj.get("status").equals("")) {
                    status = "No Info";
                } else {
                    status = dataObj.getString("status");
                }
                return tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, dateReceived);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        return null;
    }

    public TBLDelivery getBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
        TBLDelivery tblDelivery = new TBLDelivery();
        JSONObject events = new JSONObject();
        boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
        boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
        if (originTrackinfoIsNull && destinationTrackinfoIsNull) {
            events.put("origin", "No Info");
            events.put("destination", "No Info");
            String carrierCode = "";
            String status = "";
            String transitTime = "";
            String lastEvent = "";
            try {
                if (dataObj.get("itemTimeLength") != JSONObject.NULL) {
                    transitTime = dataObj.getString("itemTimeLength");
                } else {
                    transitTime = "No Info";
                }
                if (dataObj.get("carrier_code") == JSONObject.NULL || dataObj.get("carrier_code").equals("")) {
                    carrierCode = "No Info";
                } else {
                    carrierCode = dataObj.getString("carrier_code");
                }
                if (dataObj.get("lastEvent") == JSONObject.NULL || dataObj.get("lastEvent").equals("")) {
                    lastEvent = "No Info";
                } else {
                    lastEvent = dataObj.getString("lastEvent");
                }
                if (dataObj.get("status") == JSONObject.NULL || dataObj.get("status").equals("")) {
                    status = "No Info";
                } else {
                    status = dataObj.getString("status");
                }

                tblDelivery = tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, dateReceived);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        if (!originTrackinfoIsNull && !destinationTrackinfoIsNull) {
            if (originObj.get("trackinfo") instanceof JSONArray && destinationObj.get("trackinfo") instanceof JSONArray) {
                JSONArray originArray = originObj.getJSONArray("trackinfo");
                JSONArray destinationArray = destinationObj.getJSONArray("trackinfo");
                events.put("origin", originArray);
                events.put("destination", destinationArray);
                String carrierCode = "";
                String status = "";
                String transitTime = "";
                String lastEvent = "";
                try {
                    if (dataObj.get("itemTimeLength") != JSONObject.NULL) {
                        transitTime = dataObj.getString("itemTimeLength");
                    } else {
                        transitTime = "No Info";
                    }
                    if (dataObj.get("carrier_code") == JSONObject.NULL || dataObj.get("carrier_code").equals("")) {
                        carrierCode = "No Info";
                    } else {
                        carrierCode = dataObj.getString("carrier_code");
                    }
                    if (dataObj.get("lastEvent") == JSONObject.NULL || dataObj.get("lastEvent").equals("")) {
                        lastEvent = "No Info";
                    } else {
                        lastEvent = dataObj.getString("lastEvent");
                    }
                    if (dataObj.get("status") == JSONObject.NULL || dataObj.get("status").equals("")) {
                        status = "No Info";
                    } else {
                        status = dataObj.getString("status");
                    }
                    tblDelivery = tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, dateReceived);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        }
        return tblDelivery;
    }

//    public void insertBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
//        TBLDelivery tblDelivery = getBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//        deliveryService.insert(tblDelivery);
//    }
//
//    public void insertOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
//        TBLDelivery tblDelivery = getOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//        deliveryService.insert(tblDelivery);
//    }
//
//    public void insertDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
//        TBLDelivery tblDelivery = getDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
//        deliveryService.insert(tblDelivery);
//    }

    public void updateBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
        TBLDelivery tblDelivery = getBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
        deliveryService.update(tblDelivery);
    }

    public void updateOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
        TBLDelivery tblDelivery = getOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
        deliveryService.update(tblDelivery);
    }

    public void updateDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent, String dateReceived) {
        TBLDelivery tblDelivery = getDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent, dateReceived);
        deliveryService.update(tblDelivery);
    }

    public String carrierCodeCheck(String code) {
        String carrierCodeReturn = null;
        if (code.equals("dhlglobalmail")) {
            carrierCodeReturn = "dhlglobalmail";
        }
        if (code.equals("usps")) {
            carrierCodeReturn = "usps";
        }
        if (code.equals("dhl")) {
            carrierCodeReturn = "dhl";
        }
        if (code.equals("trakpak")) {
            carrierCodeReturn = "trakpak";
        }
        if (code.equals("yunexpress")) {
            carrierCodeReturn = "yunexpress";
        }
        if (code.equals("yanwen")) {
            carrierCodeReturn = "yanwen";
        }
        if (code.equals("bartolini")) {
            carrierCodeReturn = "bartolini";
        }
        if (code.equals("sfb2c")) {
            carrierCodeReturn = "sfb2c";
        }
        if (code.equals("fedex")) {
            carrierCodeReturn = "fedex";
        }
        if (code.equals("ups")) {
            carrierCodeReturn = "ups";
        }
        if (code.equals("tnt")) {
            carrierCodeReturn = "tnt";
        }
        if (code.equals("china-post")) {
            carrierCodeReturn = "china-post";
        }
        if (code.equals("china-ems")) {
            carrierCodeReturn = "china-ems";
        }
        if (code.equals("hong-kong-post")) {
            carrierCodeReturn = "hong-kong-post";
        }
        if (code.equals("singapore-post")) {
            carrierCodeReturn = "singapore-post";
        }
        if (code.equals("swiss-post")) {
            carrierCodeReturn = "swiss-post";
        }
        if (code.equals("postnl-parcels")) {
            carrierCodeReturn = "postnl-parcels";
        }
        if (code.equals("canada-post")) {
            carrierCodeReturn = "canada-post";
        }
        if (code.equals("quickway")) {
            carrierCodeReturn = "quickway";
        }
        if (code.equals("ydhex")) {
            carrierCodeReturn = "ydhex";
        }
        if (code.equals("yunlu")) {
            carrierCodeReturn = "yunlu";
        }
        if (code.equals("ldxpress")) {
            carrierCodeReturn = "ldxpress";
        }
        if (code.equals("yht")) {
            carrierCodeReturn = "yht";
        }
        if (code.equals("speedpak")) {
            carrierCodeReturn = "speedpak";
        }
        if (code.equals("utec")) {
            carrierCodeReturn = "utec";
        }
        if (code.equals("gls")) {
            carrierCodeReturn = "gls";
        }
        if (code.equals("dpd")) {
            carrierCodeReturn = "dpd";
        }
        if (code.equals("aramex")) {
            carrierCodeReturn = "aramex";
        }
        if (code.equals("toll")) {
            carrierCodeReturn = "toll";
        }
        if (code.equals("4px")) {
            carrierCodeReturn = "4px";
        }
        if (code.equals("flytexpress")) {
            carrierCodeReturn = "flytexpress";
        }
        if (code.equals("oneworldexpress")) {
            carrierCodeReturn = "oneworldexpress";
        }
        if (code.equals("kerry-logistics")) {
            carrierCodeReturn = "kerry-logistics";
        }
        if (code.equals("xru")) {
            carrierCodeReturn = "xru";
        }
        if (code.equals("dpex")) {
            carrierCodeReturn = "dpex";
        }
        if (code.equals("ruston")) {
            carrierCodeReturn = "ruston";
        }
        if (code.equals("upu")) {
            carrierCodeReturn = "upu";
        }
        if (code.equals("bluedart")) {
            carrierCodeReturn = "bluedart";
        }
        if (code.equals("dtdc")) {
            carrierCodeReturn = "dtdc";
        }
        if (code.equals("gojavas")) {
            carrierCodeReturn = "gojavas";
        }
        if (code.equals("first-flight")) {
            carrierCodeReturn = "first-flight";
        }
        if (code.equals("rosan")) {
            carrierCodeReturn = "rosan";
        }
        if (code.equals("wishpost")) {
            carrierCodeReturn = "wishpost";
        }
        if (code.equals("sto")) {
            carrierCodeReturn = "sto";
        }
        if (code.equals("yto")) {
            carrierCodeReturn = "yto";
        }
        if (code.equals("zto")) {
            carrierCodeReturn = "zto";
        }
        if (code.equals("dsv")) {
            carrierCodeReturn = "dsv";
        }
        if (code.equals("echo")) {
            carrierCodeReturn = "echo";
        }
        return carrierCodeReturn;
    }
}
