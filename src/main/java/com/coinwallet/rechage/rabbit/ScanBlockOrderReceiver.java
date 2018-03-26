package com.coinwallet.rechage.rabbit;

import com.alibaba.fastjson.JSON;
import com.coinwallet.rechage.dao.ScheduleBlockNumMapper;
import com.coinwallet.rechage.entity.ScanBlockInfo;
import com.coinwallet.rechage.service.CheckRechargeOrderOnScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class ScanBlockOrderReceiver {

    Logger logger = LoggerFactory.getLogger(ScanBlockOrderReceiver.class);

    @Autowired
    private CheckRechargeOrderOnScanService checkRechargeOrderOnScanService;

    @Autowired
    private ScheduleBlockNumMapper scheduleBlockNumMapper;

    @RabbitListener(queues = RabbitRechargeConfig.SCAN_BLOCK_ORDER,containerFactory = "myConnectionFactory")
    public void scanBlockOrder(String msg) {
        try {

            ScanBlockInfo scanBlockInfo = JSON.parseObject(msg, new com.alibaba.fastjson.TypeReference<ScanBlockInfo>() {
            });
            checkRechargeOrderOnScanService.checkScanOrder(scanBlockInfo);
            if (scanBlockInfo.getScheduleBlockNum() != null) {
                scheduleBlockNumMapper.updateByPrimaryKeySelective(scanBlockInfo.getScheduleBlockNum());
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }


}
