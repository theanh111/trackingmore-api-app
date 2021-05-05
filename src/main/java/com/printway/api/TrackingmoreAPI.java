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

    public void getTrackingInfo() {
        try {
            List<TBLNewTracking> listNewTracking = newTrackingService.findAll();
            System.out.println("** Total to scan: " + listNewTracking.size());
            for (int i = 0; i < listNewTracking.size(); i++) {
                System.out.println(i + ") Order ID: " + listNewTracking.get(i).getOrderId() + " - Tracking Number: " + listNewTracking.get(i).getPartnerTrackingNumber());
                String tblDeliveryStatus = deliveryService.findStatusByTrackingNumber(listNewTracking.get(i).getPartnerTrackingNumber());
                if (tblDeliveryStatus == null) {
                    String carrierCode = getTrackingCarrier(listNewTracking.get(i).getPartnerTrackingNumber());
                    getTrackingResult(carrierCode, tblDeliveryStatus, listNewTracking.get(i));
                }
                if (tblDeliveryStatus != null && !tblDeliveryStatus.equals("delivered") && !tblDeliveryStatus.equals("notfound")) {
                    String carrierCode = getTrackingCarrier(listNewTracking.get(i).getPartnerTrackingNumber());
                    getTrackingResult(carrierCode, tblDeliveryStatus, listNewTracking.get(i));
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void getTrackingResult(String carrier, String tblDeliveryStatus, TBLNewTracking tblNewTracking) {
        try {
            Date dateCurrent = new Date(System.currentTimeMillis());
            String trackingNumber = tblNewTracking.getPartnerTrackingNumber();
            String partnerTrackingNumber = tblNewTracking.getTrackingNumber();
            String orderId = tblNewTracking.getOrderId();
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(TRACKING_MORE_API + GET_RESULT + carrier + "/" + trackingNumber);
            httpGet.setHeader(HEADER_KEY, APP_KEY);
            HttpResponse httpResponse = client.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            JSONObject responseObj = new JSONObject(result);
            JSONObject metaObject = new JSONObject(responseObj.get("meta").toString());
            if (Integer.parseInt(metaObject.get("code").toString()) == 4031) {
                createTrackingOnTrackingMore(trackingNumber, carrier);
                client.close();
            }
            if (Integer.parseInt(metaObject.get("code").toString()) == 200) {
                if (tblDeliveryStatus == null) {
                    checkTrackingInfoToSave(responseObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                }
                if (tblDeliveryStatus != null) {
                    if (!tblDeliveryStatus.equals("delivered") && !tblDeliveryStatus.equals("notfound")) {
                        checkTrackingInfoToUpdate(responseObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
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
            client.execute(httpPost);
            client.close();
            System.out.println("- Created Tracking [" + trackingNumber + "] to Trackingmore.com");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
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
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject carrier = new JSONObject(jsonArray.getJSONObject(i).toString());
                    String code = carrier.get("code").toString();
                    if (code.equals("dhlglobalmail")) {
                        carrierCode = "dhlglobalmail";
                    }
                }
            }
            client.close();
            return carrierCode;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return carrierCode;
    }

    public void checkTrackingInfoToSave(JSONObject responseObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        try {
            JSONObject dataObj = new JSONObject(responseObj.get("data").toString());
            JSONObject originObj = new JSONObject(dataObj.get("origin_info").toString());
            JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
            boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
            boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
            if (!originTrackinfoIsNull && !destinationTrackinfoIsNull) {
                insertBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "]");
            }
            if (originTrackinfoIsNull && !destinationTrackinfoIsNull
                    || !originTrackinfoIsNull && destinationTrackinfoIsNull) {
                if (!originTrackinfoIsNull) {
                    insertOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                    System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "]");
                }
                if (!destinationTrackinfoIsNull) {
                    insertDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                    System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "]");
                }
            }
            if (originTrackinfoIsNull && destinationTrackinfoIsNull) {
                insertBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                System.out.println("- SAVED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void checkTrackingInfoToUpdate(JSONObject responseObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        try {
            JSONObject dataObj = new JSONObject(responseObj.get("data").toString());
            JSONObject originObj = new JSONObject(dataObj.get("origin_info").toString());
            JSONObject destinationObj = new JSONObject(dataObj.get("destination_info").toString());
            boolean originTrackinfoIsNull = originObj.get("trackinfo") == JSONObject.NULL;
            boolean destinationTrackinfoIsNull = destinationObj.get("trackinfo") == JSONObject.NULL;
            if (!originTrackinfoIsNull && !destinationTrackinfoIsNull) {
                updateBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                System.out.println("- UPDATED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "]");
            }
            if (originTrackinfoIsNull && !destinationTrackinfoIsNull
                    || !originTrackinfoIsNull && destinationTrackinfoIsNull) {
                if (!originTrackinfoIsNull) {
                    updateOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                    System.out.println("- UPDATED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "]");
                }
                if (!destinationTrackinfoIsNull) {
                    updateDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                    System.out.println("- UPDATED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "]");
                }
            }
            if (originTrackinfoIsNull && destinationTrackinfoIsNull) {
                updateBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
                System.out.println("- UPDATED TRACKINFO TO TBLDELIVERY: Order ID [" + orderId + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public TBLDelivery getOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        JSONArray originArray = originObj.getJSONArray("trackinfo");
        JSONObject events = new JSONObject();
        events.put("origin", originArray);
        String carrierCode = "";
        String status = "";
        String transitTime = "";
        String lastEvent = "";
        try {
            carrierCode = dataObj.getString("carrier_code");
            status = dataObj.getString("status");
            transitTime = dataObj.getString("itemTimeLength");
            lastEvent = dataObj.getString("lastEvent");
            return tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public TBLDelivery getDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        JSONArray destinationArray = destinationObj.getJSONArray("trackinfo");
        JSONObject events = new JSONObject();
        events.put("destination", destinationArray);
        String carrierCode = "";
        String status = "";
        String transitTime = "";
        String lastEvent = "";
        try {
            carrierCode = dataObj.getString("carrier_code");
            status = dataObj.getString("status");
            transitTime = dataObj.getString("itemTimeLength");
            lastEvent = dataObj.getString("lastEvent");
            return tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public TBLDelivery getBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
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
                status = dataObj.getString("status");
                tblDelivery = tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                System.exit(0);
            }
        }
        if (!originTrackinfoIsNull && !destinationTrackinfoIsNull) {
            JSONArray originArray = originObj.getJSONArray("trackinfo");
            JSONArray destinationArray = destinationObj.getJSONArray("trackinfo");
            events.put("origin", originArray);
            events.put("destination", destinationArray);
            String carrierCode = "";
            String status = "";
            String transitTime = "";
            String lastEvent = "";
            try {
                carrierCode = dataObj.getString("carrier_code");
                status = dataObj.getString("status");
                transitTime = dataObj.getString("itemTimeLength");
                lastEvent = dataObj.getString("lastEvent");
                tblDelivery = tblDeliveryObj.createDeliveryObject(trackingNumber, orderId, partnerTrackingNumber, events.toString(), status, dateCurrent, lastEvent, transitTime, carrierCode);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                System.exit(0);
            }
        }
        return tblDelivery;
    }

    public void insertBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        TBLDelivery tblDelivery = getBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.insert(tblDelivery);
    }

    public void insertOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        TBLDelivery tblDelivery = getOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.insert(tblDelivery);
    }

    public void insertDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        TBLDelivery tblDelivery = getDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.insert(tblDelivery);
    }

    public void updateBothEvent(JSONObject dataObj, JSONObject originObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        TBLDelivery tblDelivery = getBothEvent(dataObj, originObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.update(tblDelivery);
    }

    public void updateOriginEvent(JSONObject dataObj, JSONObject originObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        TBLDelivery tblDelivery = getOriginEvent(dataObj, originObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.update(tblDelivery);
    }

    public void updateDestinationEvent(JSONObject dataObj, JSONObject destinationObj, String trackingNumber, String orderId, String partnerTrackingNumber, Date dateCurrent) {
        TBLDelivery tblDelivery = getDestinationEvent(dataObj, destinationObj, trackingNumber, orderId, partnerTrackingNumber, dateCurrent);
        deliveryService.update(tblDelivery);
    }
}
