package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 超时订单
     */
    @Scheduled(cron = "0 * * * * ? *")
//    @Scheduled(cron = "1/5 * * * * ?")
    public void executeTask(){
        log.info("定时处理超时订单：{}",new Date());

        LocalDateTime Time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, Time);

        if(ordersList != null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }

    /**
     * 处理一直属于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0/5 * * * * ?")
    public void processDelivertOrder(){
        log.info("定时处理处于派送中的订单：{}",LocalDateTime.now());

        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-60);

        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, localDateTime);

        if(ordersList != null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }


    }
}