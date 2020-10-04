package net.upstox.analytics.ohlcserver.orchestrator;

import lombok.extern.slf4j.Slf4j;
import net.upstox.analytics.ohlcserver.compute.OHLCCComputeService;
import net.upstox.analytics.ohlcserver.dto.Ohlc;
import net.upstox.analytics.ohlcserver.dto.Trade;
import net.upstox.analytics.ohlcserver.events.OHLCEventService;
import net.upstox.analytics.ohlcserver.exception.OhlcComputeServiceException;
import net.upstox.analytics.ohlcserver.exception.OhlcEventServiceException;
import net.upstox.analytics.ohlcserver.exception.TradesReaderServiceException;
import net.upstox.analytics.ohlcserver.reader.TradesReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OHLCTasksOrchestrator {

    private final TradesReaderService tradesReaderService;
    private final OHLCCComputeService ohlccComputeService;
    private final OHLCEventService ohlcEventService;


    @Value("${ohlc.server.start.delay}")
    private int delay;

    @Autowired
    public OHLCTasksOrchestrator(TradesReaderService tradesReaderService, OHLCCComputeService ohlccComputeService, OHLCEventService ohlcEventService) {
        this.tradesReaderService = tradesReaderService;
        this.ohlccComputeService = ohlccComputeService;
        this.ohlcEventService = ohlcEventService;
    }


    /**
     * Orchestrator is responsible for initiating different process such as reading trade information ,computing ohlc information and
     * notifying events to subscribers
     * @throws InterruptedException - if interruption occurs while processing
     */

    @EventListener(ApplicationReadyEvent.class)
    public void initiateWorkers() throws InterruptedException {
        //DELAY FOR READING DATA AND ALLOWING MOCK CLIENT TO CONNECT
        TimeUnit.SECONDS.sleep(delay);
        try {
            BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue = new SynchronousQueue<>();
            BlockingQueue<Trade> tradesQueue = new SynchronousQueue<>();
            ohlcEventService.pushEventsToSubscribers(eventQueue);
            ohlccComputeService.computeOHLCRecords(tradesQueue, eventQueue);
            tradesReaderService.readTrades(tradesQueue);
        } catch (TradesReaderServiceException e) {
            log.error("Error in reading trade information from data file");
        } catch (OhlcEventServiceException e) {
            log.error("Error in pushing ohlc events to subscribers");
        } catch (OhlcComputeServiceException e) {
            log.error("Error in computing ohlc events");
        }
    }


}
