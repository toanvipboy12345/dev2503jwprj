package com.devmaster.dev2503jwprj.service;

import com.devmaster.dev2503jwprj.config.VNPayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    private static final Logger logger = LoggerFactory.getLogger(VNPayService.class);

    public String createPaymentUrl(String orderId, BigDecimal totalMoney, String ipAddress) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_TmnCode = VNPayConfig.vnp_TmnCode; // Đúng tên biến
            String vnp_CurrCode = "VND";
            String vnp_TxnRef = orderId;
            String vnp_OrderInfo = "Thanh toan don hang " + orderId;
            String vnp_OrderType = "other";
            String vnp_Locale = "vn";
            String vnp_ReturnUrl = VNPayConfig.vnp_ReturnUrl;
            String vnp_IpAddr = ipAddress;

            // Chuyển đổi totalMoney từ BigDecimal sang Long (VND, không có thập phân)
            long amount = totalMoney.multiply(new BigDecimal("100")).longValue();

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Tạo checksum
            String signData = VNPayConfig.hashAllFields(vnp_Params);
            vnp_Params.put("vnp_SecureHash", signData);

            // Tạo URL
            StringBuilder url = new StringBuilder(VNPayConfig.vnp_PayUrl);
            boolean first = true;
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                if (first) {
                    url.append("?");
                    first = false;
                } else {
                    url.append("&");
                }
                url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                   .append("=")
                   .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            }

            logger.info("Generated VNPay URL: {}", url.toString());
            return url.toString();
        } catch (Exception e) {
            logger.error("Error creating VNPay URL: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay", e);
        }
    }
}