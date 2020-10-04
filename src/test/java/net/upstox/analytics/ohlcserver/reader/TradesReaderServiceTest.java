package net.upstox.analytics.ohlcserver.reader;

import net.upstox.analytics.ohlcserver.dto.Trade;
import net.upstox.analytics.ohlcserver.exception.TradesReaderServiceException;
import net.upstox.analytics.ohlcserver.reader.impl.TradesReaderServiceImpl;
import net.upstox.analytics.ohlcserver.util.ResourceReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
public class TradesReaderServiceTest {

    @MockBean
    private ResourceReader resourceReader;
    private TradesReaderService readerService;

    @BeforeEach
    public void setup() {
        ClassPathResource classPathResource = new ClassPathResource("trades.json");
        Mockito.when(resourceReader.loadResource(anyString())).thenReturn(classPathResource);
        readerService = new TradesReaderServiceImpl(resourceReader);
    }

    @Test
    public void testReadTrades() throws InterruptedException {
        BlockingQueue<Trade> tradeBlockingQueue = new SynchronousQueue<>();
        AtomicReference<Trade> trade = new AtomicReference<>();
        Thread consumer = new Thread(() -> {
            try {
                trade.set(tradeBlockingQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        consumer.start();
        Thread producer = new Thread(() -> {
            try {
                readerService.readTrades(tradeBlockingQueue);
            } catch (TradesReaderServiceException e) {
                e.printStackTrace();
            }
        });
        producer.start();
        consumer.join();
        producer.interrupt();
        Assertions.assertNotNull(trade.get());
    }

    @Test
    public void testReadTradesForException() throws InterruptedException, IOException {
        BlockingQueue<Trade> tradeBlockingQueue = (BlockingQueue<Trade>) Mockito.mock(BlockingQueue.class);
        Mockito.doAnswer((t) -> {throw new IOException("IO Exception");}).when(tradeBlockingQueue).put(any(Trade.class));
        Assertions.assertThrows(TradesReaderServiceException.class, () -> {
            readerService.readTrades(tradeBlockingQueue);
        });
    }
}
