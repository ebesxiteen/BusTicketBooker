package com.example.ticketbooker.Service.OutSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.ticketbooker.DTO.OutSource.ZaloPaymentRequest;
import com.example.ticketbooker.DTO.OutSource.ZaloPaymentResponse;
import com.example.ticketbooker.DTO.OutSource.ZaloPaymentStatusResponse;
import com.example.ticketbooker.Util.Utils.HMACUtil;

@Service
public class ZaloPayService {
    private static final Logger log = LoggerFactory.getLogger(ZaloPayService.class);

    @Value("${zalo.appId}")
    private String appId;

    @Value("${zalo.key1}")
    private String key;

    @Value("${zalo.expireDuration}")
    private String expireTime;

    @Value("${zalo.createEndpoint}")
    private String createEndpoint;

    @Value("${zalo.queryEndpoint}")
    private String queryEndpoint;

    @Value("${app.base-url}")
    private String appBaseUrl;

    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    public ZaloPaymentResponse requestPayment(ZaloPaymentRequest requestInfo) throws Exception {
        ZaloPaymentResponse responseInfo = new ZaloPaymentResponse();

        int randomId = new Random().nextInt(1_000_000);
        String appTransId = getCurrentTimeString("yyMMdd") + "_" + randomId;

        Map<String, Object> embedData = new HashMap<>();
        embedData.put("redirecturl", appBaseUrl + "/greenbus/thankyou?paymentStatus=1");

        String embedDataJson = new JSONObject(embedData).toString();
        String itemJson = "[]";

        Map<String, Object> order = new HashMap<>();
        order.put("app_id", appId);
        order.put("app_trans_id", appTransId);
        order.put("app_time", System.currentTimeMillis());
        order.put("app_user", requestInfo.getAppUser());
        order.put("amount", requestInfo.getAmount());
        order.put("description", requestInfo.getDescription());
        order.put("expire_duration_seconds", expireTime);
        order.put("bank_code", "");
        order.put("item", itemJson);
        order.put("embed_data", embedDataJson);

        String data = order.get("app_id") + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|"
                + order.get("amount") + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|"
                + order.get("item");
        order.put("mac", HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, key, data));

        HttpPost postRequest = new HttpPost(createEndpoint);
        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, Object> entry : order.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }
        postRequest.setEntity(new UrlEncodedFormEntity(params));

        try (CloseableHttpClient client = createHttpClient();
             CloseableHttpResponse response = client.execute(postRequest)) {
            String resultJson = readResponse(response);
            log.debug("ZaloPay create response for {}: {}", appTransId, resultJson);

            JSONObject dataObject = new JSONObject(resultJson);
            responseInfo.setReturnCode(dataObject.optInt("return_code", -1));
            responseInfo.setDetailMessage(dataObject.optString("sub_return_message",
                    dataObject.optString("return_message", "Unknown error")));
            responseInfo.setReturnUrl(dataObject.optString("order_url", null));
            responseInfo.setPaymentId(appTransId);
        }

        return responseInfo;
    }

    public ZaloPaymentStatusResponse requestPaymentStatus(String paymentId) throws Exception {
        ZaloPaymentStatusResponse response = new ZaloPaymentStatusResponse();
        String data = appId + "|" + paymentId + "|" + key;
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, key, data);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("app_id", appId));
        params.add(new BasicNameValuePair("app_trans_id", paymentId));
        params.add(new BasicNameValuePair("mac", mac));

        URIBuilder uri = new URIBuilder(queryEndpoint);
        uri.addParameters(params);

        HttpPost post = new HttpPost(uri.build());
        post.setEntity(new UrlEncodedFormEntity(params));

        try (CloseableHttpClient client = createHttpClient();
             CloseableHttpResponse httpResponse = client.execute(post)) {
            String resultJson = readResponse(httpResponse);
            log.debug("ZaloPay query response for {}: {}", paymentId, resultJson);

            JSONObject result = new JSONObject(resultJson);
            response.setReturnCode(result.getInt("return_code"));
            response.setReturnMessage(result.getString("sub_return_message"));
            response.setProcessing(result.getBoolean("is_processing"));
        }

        return response;
    }

    protected CloseableHttpClient createHttpClient() {
        return HttpClients.createDefault();
    }

    private String readResponse(CloseableHttpResponse response) throws Exception {
        StringBuilder resultJson = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                resultJson.append(line);
            }
        }
        return resultJson.toString();
    }
}
