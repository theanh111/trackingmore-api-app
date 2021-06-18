package com.printway.api;

import com.printway.config.DBConnection;
import com.printway.entityDB.TBLTracking;
import com.printway.entityDB.TBLImportTracking;
import com.printway.service.TBLTrackingService;
import com.printway.service.TBLImportTrackingService;
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

    private TBLTracking tblTrackingObj = new TBLTracking();
    private TBLTrackingService tblTrackingService = new TBLTrackingService();
    private TBLImportTrackingService tblImportTrackingService = new TBLImportTrackingService();

    public void getTrackingInfo() {
        try {
            DBConnection.getInstance().getConnection().setAutoCommit(false);

            List<TBLImportTracking> tblImportTrackingList = tblImportTrackingService.findAllOrderNotInTBLTracking();
            System.out.println("** Total orders have trackingnumber in TBLImportTracking to scan: " + tblImportTrackingList.size());
            System.out.println("* Checking to Add New...");
            for (int i = 0; i < tblImportTrackingList.size(); i++) {
                try {
                    System.out.println("- Order ID: " + tblImportTrackingList.get(i).getOrderId());
                    String tblTrackingStatus = tblTrackingService.findStatusByTrackingNumber(tblImportTrackingList.get(i).getTrackingNumber());
                    if (tblTrackingStatus == null) {
                        String carrierCode = getTrackingCarrier(tblImportTrackingList.get(i).getTrackingNumber());
                        getTrackingResult(carrierCode, tblTrackingStatus, tblImportTrackingList.get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }

//            List<TBLImportTracking> tblImportTrackingList = tblImportTrackingService.findAllOrderToUpdate();
//            System.out.println("** Total orders in TBLImportTracking to scan: " + tblImportTrackingList.size());
//            System.out.println("* Checking to Update...");
//            for (int i = 0; i < tblImportTrackingList.size(); i++) {
//                try {
//                    String tblTrackingStatus = tblTrackingService.findStatusByTrackingNumber(tblImportTrackingList.get(i).getTrackingNumber());
//                    if (tblTrackingStatus != null && !tblTrackingStatus.equals("delivered")) {
//                        String carrierCode = getTrackingCarrier(tblImportTrackingList.get(i).getTrackingNumber());
//                        getTrackingResult(carrierCode, tblTrackingStatus, tblImportTrackingList.get(i));
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    continue;
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTrackingResult(String carrier, String tblDeliveryStatus, TBLImportTracking tblImportTracking) {
        try {
            Date dateCurrent = new Date(System.currentTimeMillis());
            String trackingNumber = tblImportTracking.getTrackingNumber();
            String orderId = tblImportTracking.getOrderId();
            String trackingUrl = tblImportTracking.getTrackingUrl();
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
                    System.out.println("- [" + orderId + " --- " + trackingNumber + "] is NOT on Trackingmore.com!");
                    createTrackingOnTrackingMore(trackingNumber, carrier);
                }
                if (metaObject.get("code").toString().equals("200")) {
                    if (tblDeliveryStatus == null) {
                        checkTrackingInfoToSave(responseObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                    }
//                    if (tblDeliveryStatus != null) {
//                        if (!tblDeliveryStatus.equals("delivered")) {
//                            checkTrackingInfoToUpdate(responseObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                        }
//                    }
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

    public String getTrackingCarrier(String trackingNumber) {
        String carrierCode = null;
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(TRACKING_MORE_API + DETECT_COURIER);
            JSONObject bodyRequest = new JSONObject();
            bodyRequest.put("tracking_number", trackingNumber);
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
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject carrier = new JSONObject(jsonArray.getJSONObject(i).toString());
                            String code = carrier.get("code").toString();
                            carrierCode = carrierCodeCheck(code);
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

    public void checkTrackingInfoToSave(JSONObject responseObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
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
                                        insertBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                                        System.out.println("- SAVED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
                                    }
                                    if (originTrackinfoIsNull && !destinationTrackinfoIsNull
                                            || !originTrackinfoIsNull && destinationTrackinfoIsNull) {
                                        if (!originTrackinfoIsNull) {
                                            insertOriginEvent(dataObj, originObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                                            System.out.println("- SAVED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
                                        }
                                        if (!destinationTrackinfoIsNull) {
                                            insertDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                                            System.out.println("- SAVED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
                                        }
                                    }
                                    if (originTrackinfoIsNull && destinationTrackinfoIsNull) {
                                        insertBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                                        System.out.println("- SAVED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
                                    }
                                }
                            }
                        }
                    }
                    if (dataObj.has("origin_info") && !dataObj.has("destination_info")) {
                        if (dataObj.get("origin_info") instanceof JSONObject) {
                            JSONObject originObj = new JSONObject(dataObj.get("origin_info").toString());
                            boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
                            if (!originTrackinfoIsNull) {
                                insertOriginEvent(dataObj, originObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                                System.out.println("- SAVED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
                            }
                            if (originTrackinfoIsNull) {
                                insertOriginEvent(dataObj, originObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                                System.out.println("- SAVED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
                            }
                        }
                    }
                    if (!dataObj.has("origin_info") && dataObj.has("destination_info")) {
                        if (dataObj.get("destination_info") instanceof JSONObject) {
                            JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
                            boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
                            if (!destinationTrackinfoIsNull) {
                                insertDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                                System.out.println("- SAVED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
                            }

                            if (destinationTrackinfoIsNull) {
                                insertDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
                                System.out.println("- SAVED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void checkTrackingInfoToUpdate(JSONObject responseObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
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
//                                        updateBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                                        System.out.println("- UPDATED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
//                                    }
//                                    if (originTrackinfoIsNull && !destinationTrackinfoIsNull
//                                            || !originTrackinfoIsNull && destinationTrackinfoIsNull) {
//                                        if (!originTrackinfoIsNull) {
//                                            updateOriginEvent(dataObj, originObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                                            System.out.println("- UPDATED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
//                                        }
//                                        if (!destinationTrackinfoIsNull) {
//                                            updateDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                                            System.out.println("- UPDATED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
//                                        }
//                                    }
//                                    if (originTrackinfoIsNull && destinationTrackinfoIsNull) {
//                                        updateBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                                        System.out.println("- UPDATED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
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
//                                updateOriginEvent(dataObj, originObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                                System.out.println("- UPDATED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
//                            }
//                            if (originTrackinfoIsNull) {
//                                updateOriginEvent(dataObj, originObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                                System.out.println("- UPDATED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
//                            }
//                        }
//                    }
//                    if (!dataObj.has("origin_info") && dataObj.has("destination_info")) {
//                        if (dataObj.get("destination_info") instanceof JSONObject) {
//                            JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
//                            boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
//                            if (!destinationTrackinfoIsNull) {
//                                updateDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                                System.out.println("- UPDATED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
//                            }
//
//                            if (destinationTrackinfoIsNull) {
//                                updateDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//                                System.out.println("- UPDATED TRACKINFO TO TBLTRACKING: Order ID [" + orderId + "]");
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public TBLTracking getOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
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
                return tblTrackingObj.createTBLTrackingObject(trackingNumber, orderId, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, trackingUrl);
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
                return tblTrackingObj.createTBLTrackingObject(trackingNumber, orderId, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, trackingUrl);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        return null;
    }

    public TBLTracking getDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
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
                return tblTrackingObj.createTBLTrackingObject(trackingNumber, orderId, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, trackingUrl);
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
                return tblTrackingObj.createTBLTrackingObject(trackingNumber, orderId, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, trackingUrl);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        return null;
    }

    public TBLTracking getBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
        TBLTracking tblTracking = new TBLTracking();
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

                tblTracking = tblTrackingObj.createTBLTrackingObject(trackingNumber, orderId, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, trackingUrl);
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
                    tblTracking = tblTrackingObj.createTBLTrackingObject(trackingNumber, orderId, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode, trackingUrl);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        }
        return tblTracking;
    }

    public void insertBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
        TBLTracking tblTracking = getBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
        tblTrackingService.insert(tblTracking);
    }

    public void insertOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
        TBLTracking tblTracking = getOriginEvent(dataObj, originObj, trackingNumber, orderId, dateCurrent, trackingUrl);
        tblTrackingService.insert(tblTracking);
    }

    public void insertDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
        TBLTracking tblTracking = getDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
        tblTrackingService.insert(tblTracking);
    }

//    public void updateBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
//        TBLTracking tblTracking = getBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//        tblTrackingService.update(tblTracking);
//    }
//
//    public void updateOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
//        TBLTracking tblTracking = getOriginEvent(dataObj, originObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//        tblTrackingService.update(tblTracking);
//    }
//
//    public void updateDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, Date dateCurrent, String trackingUrl) {
//        TBLTracking tblTracking = getDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, dateCurrent, trackingUrl);
//        tblTrackingService.update(tblTracking);
//    }

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
