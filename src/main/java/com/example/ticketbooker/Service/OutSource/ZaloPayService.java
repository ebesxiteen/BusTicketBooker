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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.ticketbooker.DTO.OutSource.ZaloPaymentRequest;
import com.example.ticketbooker.DTO.OutSource.ZaloPaymentResponse;
import com.example.ticketbooker.DTO.OutSource.ZaloPaymentStatusResponse;
import com.example.ticketbooker.Util.Utils.HMACUtil;

@Service
public class ZaloPayService {
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

    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

public ZaloPaymentResponse requestPayment(ZaloPaymentRequest requestInfo) throws Exception {
    ZaloPaymentResponse responseInfo = new ZaloPaymentResponse();

    Random rand = new Random();
    int randomId = rand.nextInt(1000000);
    String appTransId = getCurrentTimeString("yyMMdd") + "_" + randomId;

    // 1. FIX PORT: Đổi 8080 thành 8000 (theo screenshot của bạn)
    // Lưu ý: redirecturl không được chứa ký tự đặc biệt hoặc encoding sai
    Map<String, Object> embedData = new HashMap<>();
    embedData.put("redirecturl", "http://localhost:8000/greenbus/thankyou?paymentStatus=1"); 
    
    // 2. FIX JSON: Chuyển embed_data thành String TRƯỚC khi đưa vào Map order
    // Điều này đảm bảo chuỗi dùng để tính MAC và chuỗi gửi đi là Y HỆT nhau
    String embedDataJson = new JSONObject(embedData).toString();

    // 3. FIX ITEM: Item cũng phải là String JSON chuẩn
    String itemJson = "[]";

    Map<String, Object> order = new HashMap<>() {{
        put("app_id", appId);
        put("app_trans_id", appTransId);
        put("app_time", System.currentTimeMillis());
        put("app_user", requestInfo.getAppUser());
        put("amount", requestInfo.getAmount());
        put("description", requestInfo.getDescription());
        put("expire_duration_seconds", expireTime);
        put("bank_code", ""); // Để trống để user tự chọn (QR, Thẻ, ZaloPay Wallet)
        put("item", itemJson);
        put("embed_data", embedDataJson); // Dùng chuỗi String đã convert ở trên
    }};

    // Tạo chuỗi dữ liệu để tính MAC (Đúng thứ tự quy định của ZaloPay)
    String data = order.get("app_id") + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|"
            + order.get("amount") + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|"
            + order.get("item");
            
    // Tính toán MAC
    order.put("mac", HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, key, data));

    // Debug Log: In ra console để kiểm tra nếu vẫn lỗi
    System.out.println("Data to MAC: " + data);
    System.out.println("Generated MAC: " + order.get("mac"));

    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost postRequest = new HttpPost(createEndpoint);

    List<NameValuePair> params = new ArrayList<>();
    for (Map.Entry<String, Object> e : order.entrySet()) {
        params.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
    }
    postRequest.setEntity(new UrlEncodedFormEntity(params));

    CloseableHttpResponse res = client.execute(postRequest);
    BufferedReader rd = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
    StringBuilder resultJsonStr = new StringBuilder();
    String line;

    while ((line = rd.readLine()) != null) {
        resultJsonStr.append(line);
    }

    System.out.println("ZaloPay Response: " + resultJsonStr);

    JSONObject dataObject = new JSONObject(resultJsonStr.toString());

    int returnCode = dataObject.optInt("return_code", -1);
    String detailMsg = dataObject.optString("sub_return_message", 
                       dataObject.optString("return_message", "Lỗi không xác định"));
    
    // Fix lấy order_url: API v2 trả về 'order_url'
    String orderUrl = dataObject.optString("order_url", null);

    responseInfo.setReturnCode(returnCode);
    responseInfo.setDetailMessage(detailMsg);
    responseInfo.setReturnUrl(orderUrl);
    responseInfo.setPaymentId(appTransId);

    return responseInfo;
}
    public ZaloPaymentStatusResponse requestPaymentStatus(String paymentId) throws Exception {
        ZaloPaymentStatusResponse response = new ZaloPaymentStatusResponse();
        String data = appId +"|"+ paymentId +"|"+ key;
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, key, data);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("app_id", appId));
        params.add(new BasicNameValuePair("app_trans_id", paymentId));
        params.add(new BasicNameValuePair("mac", mac));

        URIBuilder uri = new URIBuilder(queryEndpoint);
        uri.addParameters(params);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(uri.build());
        post.setEntity(new UrlEncodedFormEntity(params));

        CloseableHttpResponse res = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
        StringBuilder resultJsonStr = new StringBuilder();
        String line;

        while ((line = rd.readLine()) != null) {
            resultJsonStr.append(line);
        }

        JSONObject result = new JSONObject(resultJsonStr.toString());
        for (String key : result.keySet()) {
            System.out.format("%s = %s\n", key, result.get(key));
        }

        response.setReturnCode(result.getInt("return_code"));
        response.setReturnMessage(result.getString("sub_return_message"));
        response.setProcessing(result.getBoolean("is_processing"));

        return response;
    }
}