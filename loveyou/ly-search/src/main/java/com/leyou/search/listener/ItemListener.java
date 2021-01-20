package com.leyou.search.listener;

import com.leyou.common.constants.MQConstants;
import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;

    //商品上架添加索引库
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.SEARCH_ITEM_UP),
            exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_UP_KEY
    ))
    public void indexCreate(Long spuId){
        searchService.indexCreate(spuId);
    }

    //商品下架删除索引库
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.SEARCH_ITEM_DOWN),
            exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_DOWN_KEY
    ))
    public void indexDelete(Long spuId){
        searchService.indexDelete(spuId);
    }

}
