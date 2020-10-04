package net.upstox.analytics.ohlcserver.events.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.upstox.analytics.ohlcserver.dto.Ohlc;
import net.upstox.analytics.ohlcserver.events.OHLCEventService;
import net.upstox.analytics.ohlcserver.exception.OhlcEventServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@Getter
public class OHLCEventServiceImpl implements OHLCEventService {

    private final Set<String> subscribedStocks;
    private final SimpMessagingTemplate simpMessagingTemplate;


    @Autowired
    public OHLCEventServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.subscribedStocks = new CopyOnWriteArraySet<>();
    }

    /**
     * THis service reads from the output queue for OhlcComputeService and sends event notifications to the topics subscribed by clients
     * @param eventQueue - Queue from which the service reads the stocks present in the current bar
     * @throws OhlcEventServiceException - if interruption occurs during pushing notifications to subscribers
     */

    @Override
    @Async
    public void pushEventsToSubscribers(BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue) throws OhlcEventServiceException {
        try {
            AtomicInteger bar = new AtomicInteger(1);
            MultiValueMap<String, Ohlc> events = eventQueue.take();
            while (!CollectionUtils.isEmpty(events)) {
                log.info("Pushing events to all subscribers");
                for (String subscribedStock : subscribedStocks) {
                    log.info("Checking if trades are present for subscribed stock {}", subscribedStock);
                    if (events.containsKey(subscribedStock)) {
                        log.info("Trades present for stock {}", subscribedStock);
                        events.get(subscribedStock).forEach(ohlc -> simpMessagingTemplate.convertAndSend("/ohlc/".concat(subscribedStock), ohlc));
                    } else {
                        log.info("No trades present for stock {}", subscribedStock);
                        simpMessagingTemplate.convertAndSend("/ohlc/".concat(subscribedStock), Ohlc.builder().symbol(subscribedStock).barNum(bar.get()).build());
                    }
                }
                bar.getAndIncrement();
                events = eventQueue.poll(20, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            log.error("Error occurred while pushing ohlc events tp subscribers");
            throw new OhlcEventServiceException("Error occurred while pushing ohlc events tp subscribers");
        }
    }


    public void addSubscribedTopics(String topic) {
        subscribedStocks.add(topic);
    }

    public void removeSubscribedTopics(String topic) {
        subscribedStocks.remove(topic);
    }

    /**
     * Registers stocks for which clients want to receive notifications
     * @param event - Subscription Event sent by client
     */

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpDestination = (String) message.getHeaders().get("simpDestination");
        String topic = simpDestination.split("/")[2];
        log.info("Destination Topic Subscribed: {}", simpDestination);
        addSubscribedTopics(topic);
    }


}
