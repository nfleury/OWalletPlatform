package com.coinwallet.interceptor;


import com.alibaba.fastjson.JSON;
import com.coinwallet.rechage.entity.LogOperateApi;
import com.coinwallet.rechage.rabbit.RabbitRechargeConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class ContextRootInterceptor extends HandlerInterceptorAdapter {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        request.setAttribute("s1", System.currentTimeMillis());


        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept,Authorization,,X-File-Name");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        return true;
    }


    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long s1 = (long) request.getAttribute("s1");
        String actPath = request.getRequestURI();
        String in = (String) request.getAttribute("in");
        String seed = (String) request.getAttribute("seed");
        Integer merchantId = (Integer) request.getAttribute("merchantId");

        int consumetime = (int) (System.currentTimeMillis() - s1);

        LogOperateApi logOperateApi = new LogOperateApi();
        logOperateApi.setActpath(actPath);
        logOperateApi.setCreatetime(new Date());
        logOperateApi.setConsumetime(consumetime);
        logOperateApi.setIn(in);
        logOperateApi.setSeed(seed);
        logOperateApi.setMerchantId(merchantId);

        rabbitTemplate.convertAndSend(RabbitRechargeConfig.OPERATION_API_LOG,JSON.toJSONString(logOperateApi));


    }


}