package net.upstox.analytics.ohlcserver.events;

import net.upstox.analytics.ohlcserver.dto.Ohlc;
import net.upstox.analytics.ohlcserver.dto.Trade;
import net.upstox.analytics.ohlcserver.events.impl.OHLCEventServiceImpl;
import net.upstox.analytics.ohlcserver.exception.OhlcComputeServiceException;
import net.upstox.analytics.ohlcserver.exception.OhlcEventServiceException;
import net.upstox.analytics.ohlcserver.exception.TradesReaderServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OHLCEventServiceImpl.class, SimpMessagingTemplate.class, ExecutorSubscribableChannel.class})
public class OhlcEventServiceTest {

    @Autowired
    private OHLCEventServiceImpl ohlcEventService;

    @Test
    public void testHandleSessionSubscribeEvent(){
        SessionSubscribeEvent sessionSubscribeEvent = Mockito.mock(SessionSubscribeEvent.class);
        GenericMessage genericMessage = Mockito.mock(GenericMessage.class);
        MessageHeaders messageHeaders = Mockito.mock(MessageHeaders.class);
        Mockito.when(sessionSubscribeEvent.getMessage()).thenReturn(genericMessage);
        Mockito.when(genericMessage.getHeaders()).thenReturn(messageHeaders);
        Mockito.when(messageHeaders.get(anyString())).thenReturn("/ohlc/xyz");
        ohlcEventService.handleSessionSubscribeEvent(sessionSubscribeEvent);
        Assertions.assertEquals(1,ohlcEventService.getSubscribedStocks().size());
    }

    @Test
    public void testRemoveSubscribedTopics(){
        SimpMessagingTemplate simpMessagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        OHLCEventServiceImpl ohlcEventService = new OHLCEventServiceImpl(simpMessagingTemplate);
        ohlcEventService.addSubscribedTopics("XYZ");
        Assertions.assertEquals(1,ohlcEventService.getSubscribedStocks().size());
        ohlcEventService.removeSubscribedTopics("XYZ");
        Assertions.assertEquals(0,ohlcEventService.getSubscribedStocks().size());
    }

    @Test
    public void testPushEventsToSubscribersForException() throws InterruptedException {
        BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue = Mockito.mock(SynchronousQueue.class);
        Mockito.doAnswer((t) -> {
            throw new InterruptedException("InterruptedException");
        }).when(eventQueue).take();
        Assertions.assertThrows(OhlcEventServiceException.class, () -> {
            ohlcEventService.pushEventsToSubscribers(eventQueue);
        });

    }

    @Test
    public void testPushEventsToSubscribers() throws InterruptedException {
        SimpMessagingTemplate simpMessagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        OHLCEventServiceImpl ohlcEventService = new OHLCEventServiceImpl(simpMessagingTemplate);
        ohlcEventService.addSubscribedTopics("test1");
        BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue = new SynchronousQueue<>();
        Mockito.doNothing().when(simpMessagingTemplate).convertAndSend(anyString(),any(Ohlc.class));
        Thread consumer = new Thread(() -> {
            try {
                ohlcEventService.pushEventsToSubscribers(eventQueue);
            } catch ( OhlcEventServiceException e) {
                e.printStackTrace();
            }
        });
        consumer.start();
        Thread producer = new Thread(() -> {
            try {
                MultiValueMap<String, Ohlc> currentStockOHLCRecords = new LinkedMultiValueMap<>();
                currentStockOHLCRecords.add("test1", Ohlc.builder().symbol("test1").lastTradePrice(10).close(1).volume(1).high(10).low(2).build());
                eventQueue.put(currentStockOHLCRecords);
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        producer.start();
        producer.join();
        consumer.join();
        Mockito.verify(simpMessagingTemplate, Mockito.times(1)).convertAndSend(anyString(),any(Ohlc.class));
    }

}
