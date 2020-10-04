package net.upstox.analytics.ohlcserver.events;

import net.upstox.analytics.ohlcserver.dto.Ohlc;
import net.upstox.analytics.ohlcserver.exception.OhlcEventServiceException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.concurrent.BlockingQueue;

public interface OHLCEventService {

    void pushEventsToSubscribers(BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue) throws OhlcEventServiceException;

    void handleSessionSubscribeEvent(SessionSubscribeEvent event);

    void addSubscribedTopics(String topic);

    void removeSubscribedTopics(String topic);

}
