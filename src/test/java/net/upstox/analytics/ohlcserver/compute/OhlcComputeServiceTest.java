package net.upstox.analytics.ohlcserver.compute;

import net.upstox.analytics.ohlcserver.compute.impl.OHLCComputeServiceImpl;
import net.upstox.analytics.ohlcserver.dto.Ohlc;
import net.upstox.analytics.ohlcserver.dto.Trade;
import net.upstox.analytics.ohlcserver.exception.OhlcComputeServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.MultiValueMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OHLCComputeServiceImpl.class})
@TestPropertySource(properties = "ohlc.server.tracker.interval=15")
public class OhlcComputeServiceTest {

    @Autowired
    @Spy
    private OHLCComputeServiceImpl ohlcComputeService;

    @Test
    public void testComputeOHLCRecordsForTradesWithDifferenceGreaterThanInterval() throws InterruptedException {
        BlockingQueue<Trade> tradeBlockingQueue = new SynchronousQueue<>();
        BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue = Mockito.mock(SynchronousQueue.class);
        Thread consumer = new Thread(() -> {
            try {
                ohlcComputeService.computeOHLCRecords(tradeBlockingQueue, eventQueue);
            } catch (OhlcComputeServiceException e) {
                e.printStackTrace();
            }
        });
        consumer.start();
        Thread producer = new Thread(() -> {
            try {
                Trade t1 = Trade.builder().price(10).quantity(1).sym("TXY").build();
                t1.setTimeStamp(1538409725339216503L);
                tradeBlockingQueue.put(t1);

                Trade t2 = Trade.builder().price(10).quantity(1).sym("TXY1").build();
                t2.setTimeStamp(1538409793526889926L);

                tradeBlockingQueue.put(t2);
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        producer.start();
        producer.join();
        consumer.join();
        Mockito.verify(eventQueue, Mockito.times(2)).put(any());
    }

    @Test
    public void testComputeOHLCRecordsWithDifferenceLessThanInterval() throws InterruptedException {
        BlockingQueue<Trade> tradeBlockingQueue = new SynchronousQueue<>();
        BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue = Mockito.mock(SynchronousQueue.class);
        Thread consumer = new Thread(() -> {
            try {
                ohlcComputeService.computeOHLCRecords(tradeBlockingQueue, eventQueue);
            } catch (OhlcComputeServiceException e) {
                e.printStackTrace();
            }
        });
        consumer.start();
        Thread producer = new Thread(() -> {
            try {
                Trade t1 = Trade.builder().price(10).quantity(1).sym("TXY").build();
                t1.setTimeStamp(1538409725339216503L);
                tradeBlockingQueue.put(t1);

                Trade t2 = Trade.builder().price(10).quantity(1).sym("TXY").build();
                t2.setTimeStamp(1538409738828589281L);
                tradeBlockingQueue.put(t2);
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        producer.start();
        producer.join();
        consumer.join();
        Mockito.verify(eventQueue, Mockito.times(1)).put(any());
    }

    @Test
    public void testComputeOHLCRecordsWithDifferenceEqualToInterval() throws InterruptedException {
        BlockingQueue<Trade> tradeBlockingQueue = new SynchronousQueue<>();
        BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue = Mockito.mock(SynchronousQueue.class);
        Thread consumer = new Thread(() -> {
            try {
                ohlcComputeService.computeOHLCRecords(tradeBlockingQueue, eventQueue);
            } catch (OhlcComputeServiceException e) {
                e.printStackTrace();
            }
        });
        consumer.start();
        Thread producer = new Thread(() -> {
            try {
                Trade t1 = Trade.builder().price(10).quantity(1).sym("TXY").build();
                t1.setTimeStamp(1538409725339216503L);
                tradeBlockingQueue.put(t1);

                Trade t2 = Trade.builder().price(10).quantity(1).sym("TXY").build();
                t2.setTimeStamp(1538409740958589281L);
                tradeBlockingQueue.put(t2);
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        producer.start();
        producer.join();
        consumer.join();
        Mockito.verify(eventQueue, Mockito.times(2)).put(any());
    }


    @Test
    public void testOhlcComputeServiceException() throws InterruptedException {
        BlockingQueue<Trade> tradeBlockingQueue = (BlockingQueue<Trade>) Mockito.mock(BlockingQueue.class);
        BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue = Mockito.mock(SynchronousQueue.class);
        Mockito.doAnswer((t) -> {
            throw new InterruptedException("InterruptedException");
        }).when(tradeBlockingQueue).take();
        Assertions.assertThrows(OhlcComputeServiceException.class, () -> {
            ohlcComputeService.computeOHLCRecords(tradeBlockingQueue, eventQueue);
        });
    }
}
