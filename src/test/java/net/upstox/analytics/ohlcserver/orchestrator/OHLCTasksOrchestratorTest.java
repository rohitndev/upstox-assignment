package net.upstox.analytics.ohlcserver.orchestrator;

import net.upstox.analytics.ohlcserver.compute.OHLCCComputeService;
import net.upstox.analytics.ohlcserver.events.OHLCEventService;
import net.upstox.analytics.ohlcserver.exception.OhlcComputeServiceException;
import net.upstox.analytics.ohlcserver.exception.OhlcEventServiceException;
import net.upstox.analytics.ohlcserver.exception.TradesReaderServiceException;
import net.upstox.analytics.ohlcserver.reader.TradesReaderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.BlockingQueue;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "ohlc.server.start.delay=1")
@ContextConfiguration(classes = OHLCTasksOrchestrator.class)
public class OHLCTasksOrchestratorTest {

    @MockBean
    private TradesReaderService tradesReaderService;
    @MockBean
    private OHLCCComputeService ohlccComputeService;
    @MockBean
    private OHLCEventService ohlcEventService;

    @InjectMocks
    @Autowired
    private OHLCTasksOrchestrator ohlcTasksOrchestrator;

    @Test
    public void testInitiateWorkers() throws InterruptedException, TradesReaderServiceException, OhlcComputeServiceException, OhlcEventServiceException {
        Mockito.doNothing().when(tradesReaderService).readTrades(any(BlockingQueue.class));
        Mockito.doNothing().when(ohlccComputeService).computeOHLCRecords(any(BlockingQueue.class),any(BlockingQueue.class));
        Mockito.doNothing().when(ohlcEventService).pushEventsToSubscribers(any(BlockingQueue.class));
        ohlcTasksOrchestrator.initiateWorkers();
        Mockito.verify(tradesReaderService, Mockito.times(1)).readTrades(any(BlockingQueue.class));
        Mockito.verify(ohlccComputeService, Mockito.times(1)).computeOHLCRecords(any(BlockingQueue.class),any(BlockingQueue.class));
        Mockito.verify(ohlcEventService, Mockito.times(1)).pushEventsToSubscribers(any(BlockingQueue.class));
    }
}
