package com.printway.api;

import com.google.gson.Gson;
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
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
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

    private DeliveryService deliveryService = new DeliveryService();
    private NewTrackingService newTrackingService = new NewTrackingService();

//    private String[] listTrackingNumber = {"420750729361269903506434265898", "420571049361269903506434271615"};

    public void check() {
        List<TBLNewTracking> listNewTracking = newTrackingService.findAll();

        for (int i = 0; i < listNewTracking.size(); i++) {
            try {
                String carrierCode = getTrackingCarrier(listNewTracking.get(i).getPartnerTrackingNumber());
                getTrackingResult(carrierCode, listNewTracking.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getTrackingResult(String carrier, TBLNewTracking tblNewTracking) throws IOException {
        System.out.println("== Result: " + tblNewTracking.getPartnerTrackingNumber());
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(TRACKING_MORE_API + GET_RESULT + carrier + "/" + tblNewTracking.getPartnerTrackingNumber());
        httpGet.setHeader(HEADER_KEY, APP_KEY);
        HttpResponse httpResponse = client.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        String result = null;
        try {
            result = EntityUtils.toString(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(tblNewTracking.getPartnerTrackingNumber() + ": " + result);
        JSONObject jsonObject = new JSONObject(result);
        JSONObject meta = new JSONObject(jsonObject.get("meta").toString());
        if (Integer.parseInt(meta.get("code").toString()) == 4031) {
            createTracking(tblNewTracking.getPartnerTrackingNumber(), carrier);
        }
        System.out.println(jsonObject.get("data"));
        TBLDelivery tblDelivery = new TBLDelivery();
        tblDelivery.setTrackingNumber(tblNewTracking.getPartnerTrackingNumber());
        tblDelivery.setPartnerTrackingNumber(tblNewTracking.getTrackingNumber());
        tblDelivery.setOrderNumber(tblNewTracking.getOrderId());
        tblDelivery.setEvents("5265265");
        deliveryService.insert();
        System.out.println("--------------------------------------------");
        client.close();
    }

    public void createTracking(String trackingNumber, String carrierCode) throws IOException {
        System.out.println("Create");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(TRACKING_MORE_API + CREATE_TRACKING);
        JSONObject payload = new JSONObject();
        payload.put("tracking_number", trackingNumber);
        payload.put("carrier_code", carrierCode);
        httpPost.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        httpPost.setHeader(HEADER_KEY, APP_KEY);
        HttpResponse httpResponse = client.execute(httpPost);
        System.out.println("=== POST [" + trackingNumber + " ] TRACKING TO TRACKING MORE ===");
        System.out.println("--------------------------------------------");
        client.close();
    }

    public String getTrackingCarrier(String trackingNumber) throws Exception {
        System.out.println("== Get Tracking Carrier: " + trackingNumber);
        String carrierCode = "";
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
    }
}
