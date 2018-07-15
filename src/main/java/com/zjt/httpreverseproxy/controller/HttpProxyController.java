package com.zjt.httpreverseproxy.controller;

import com.zjt.httpreverseproxy.constants.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhujuntao
 * @create 2018-7-15 22:47
 * @desc 说明 代理控制类
 */
@RestController
public class HttpProxyController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 代理所有Post 和 Get请求
     *
     * @param httpRequest
     * @param httpResponse
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST})
    public void doProxyBusiness(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            String basePath = httpRequest.getScheme() + "://" + "127.0.0.1:12007";
            logger.info("BasePath:{}", basePath);
            String uri = httpRequest.getRequestURI();
            logger.info("URI:{}", uri);
            StringBuilder urlString = new StringBuilder(basePath + uri);
            String queryString = httpRequest.getQueryString();
            logger.info("queryString:" + queryString);

            if (!Objects.isNull(queryString) && !"".equals(queryString)) {
                urlString.append("?").append(queryString);
            }

            // 创建Url对象
            URL url = new URL(urlString.toString());
            logger.info("转发请求URL:{}", url.toString());

            // 建立连接
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            String methodName = httpRequest.getMethod();
            logger.info("method:{}", methodName);
            con.setRequestMethod(methodName);

            // Post请求，参数要放在正文内
            // 设置可以向HttpURLConnection输出, 默认情况下是false;
            con.setDoOutput(true);

            // 设置可以从HttpURLConnection读入，默认情况下是true;
            con.setDoInput(true);

            // 不让系统自动处理重定向
            HttpURLConnection.setFollowRedirects(false);

            // 设置不适用缓存
            con.setUseCaches(false);

            // 获取Headers
            Enumeration<String> headers = httpRequest.getHeaderNames();
            while (headers.hasMoreElements()) {
                String headerName = headers.nextElement();
                String value = httpRequest.getHeader(headerName);
                logger.info("Header:{}:{}", headerName, value);
                con.setRequestProperty(headerName, value);
            }

            // 建立连接
            con.connect();

            // Post请求处理
            if (Constant.POST.equalsIgnoreCase(methodName)) {
                postMethodBusiness(httpRequest, con);
            }

            httpResponse.setStatus(con.getResponseCode());
            logger.info("responseCode:{}", con.getResponseCode());

            Map<String, List<String>> headerFieldsMap = con.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : headerFieldsMap.entrySet()) {
                String key = entry.getKey();
                if (key == null) {
                    continue;
                }
                httpResponse.setHeader(key, entry.getValue().get(0));
            }

            BufferedInputStream clientRequestBuffer = new BufferedInputStream(con.getInputStream());
            BufferedOutputStream proxyRequestBuffer = new BufferedOutputStream(httpResponse.getOutputStream());
            try {
                int oneByte;
                while ((oneByte = clientRequestBuffer.read()) != -1) {
                    proxyRequestBuffer.write(oneByte);
                }
                proxyRequestBuffer.flush();
            } finally {
                proxyRequestBuffer.close();
                clientRequestBuffer.close();
            }
            con.disconnect();
        } catch (Exception e) {
            logger.error("请求转发异常！", e);
        }
    }

    private void postMethodBusiness(HttpServletRequest httpRequest, HttpURLConnection con) throws Exception {
        // 获取ContentType
        String contentType = httpRequest.getContentType();
        logger.info("ContentType:{}", contentType);
        if (contentType.toLowerCase().indexOf("application/x-www-form") != -1) {
            Map<String, String[]> params = httpRequest.getParameterMap();
            StringBuilder queryString = new StringBuilder();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                for (String value : values) {
                    queryString.append(key).append("=").append(value).append("&");
                }
            }
            queryString = queryString.deleteCharAt(queryString.length() - 1);
            logger.info("QueryString:{}", queryString);
            BufferedOutputStream proxyRequestBuffer = new BufferedOutputStream(con.getOutputStream());
            proxyRequestBuffer.write(queryString.toString().getBytes());
            proxyRequestBuffer.flush();
            proxyRequestBuffer.close();
            return;
        }

        BufferedInputStream clientRequestBuffer = new BufferedInputStream(httpRequest.getInputStream());
        BufferedOutputStream proxyRequestBuffer = new BufferedOutputStream(con.getOutputStream());
        try {
            int oneByte;
            while ((oneByte = clientRequestBuffer.read()) != -1) {
                proxyRequestBuffer.write(oneByte);
            }
            proxyRequestBuffer.flush();
        } finally {
            proxyRequestBuffer.close();
            clientRequestBuffer.close();
        }
    }

}
