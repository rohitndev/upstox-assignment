package net.upstox.analytics.ohlcserver.compute.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.upstox.analytics.ohlcserver.compute.OHLCCComputeService;
import net.upstox.analytics.ohlcserver.dto.Ohlc;
import net.upstox.analytics.ohlcserver.dto.Trade;
import net.upstox.analytics.ohlcserver.exception.OhlcComputeServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Getter
public class OHLCComputeServiceImpl implements OHLCCComputeService {

    @Value("${ohlc.server.tracker.interval}")
    private int timeInterval;

    /**
     * This method constructs a bar of stocks with OHLC information for a given time interval
     * @param tradeBlockingQueue - Input Queue from which the service reads trade information
     * @param eventQueue - Output Queue to which the service sends the stocks which lie within the time interval
     * @throws OhlcComputeServiceException - if any interruption occurs during computing ohlc recprds
     */

    @Override
    @Async
    public void computeOHLCRecords(BlockingQueue<Trade> tradeBlockingQueue, BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue) throws OhlcComputeServiceException {
        try {
            int barNumCount = 1;
            MultiValueMap<String, Ohlc> currentStockOHLCRecords = new LinkedMultiValueMap<>();
            Trade currentTrade = tradeBlockingQueue.take();
            Instant barStartTime = currentTrade.getTimestamp();
            addStockInformation(currentStockOHLCRecords, currentTrade, barNumCount);
            currentTrade = tradeBlockingQueue.poll(10, TimeUnit.SECONDS);
            while (currentTrade != null) {
                long timeLapse = Duration.between(barStartTime, currentTrade.getTimestamp()).getSeconds();
                if (timeLapse > timeInterval) {
                    log.info("Closing current bar {}", barNumCount);
                    pushInformationToEventQueue(currentStockOHLCRecords, eventQueue);
                    barStartTime = currentTrade.getTimestamp();
                    barNumCount++;
                    addStockInformation(currentStockOHLCRecords, currentTrade, barNumCount);
                } else if (timeLapse == timeInterval) {
                    log.info("Closing current bar {}", barNumCount);
                    addStockInformation(currentStockOHLCRecords, currentTrade, barNumCount);
                    pushInformationToEventQueue(currentStockOHLCRecords, eventQueue);
                    barStartTime = currentTrade.getTimestamp();
                    barNumCount++;
                } else {
                    addStockInformation(currentStockOHLCRecords, currentTrade, barNumCount);
                }
                currentTrade = tradeBlockingQueue.poll(10, TimeUnit.SECONDS);
            }
            //Final Event Push
            pushInformationToEventQueue(currentStockOHLCRecords, eventQueue);
        } catch (InterruptedException e) {
            log.error("Error occurred while computing ohlc information from trades");
            throw new OhlcComputeServiceException("Error occurred while computing ohlc information from trades");
        }
    }

    private void pushInformationToEventQueue(MultiValueMap<String, Ohlc> currentStockOHLCRecords, BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue) throws InterruptedException {
        updateClosingRecordForStocks(currentStockOHLCRecords);
        eventQueue.put(new LinkedMultiValueMap<>(currentStockOHLCRecords));
        currentStockOHLCRecords.clear();
    }


    private void updateClosingRecordForStocks(MultiValueMap<String, Ohlc> currentStockOHLCRecords) {
        currentStockOHLCRecords.forEach((stockName, records) -> {
            records.get(records.size() - 1).setClose(records.get(records.size() - 1).getLastTradePrice());
        });
    }

    private void addStockInformation(MultiValueMap<String, Ohlc> currentStockOHLCRecords, Trade trade, int barNumCount) {
        if (currentStockOHLCRecords.containsKey(trade.getSym())) {
            List<Ohlc> ohlcRecords = currentStockOHLCRecords.get(trade.getSym());
            currentStockOHLCRecords.add(trade.getSym(), buildOHLCInformationFromLastTrade(trade, ohlcRecords.get(ohlcRecords.size() - 1), barNumCount));
        } else {
            currentStockOHLCRecords.add(trade.getSym(), Ohlc.builder().symbol(trade.getSym()).open(trade.getPrice()).barNum(barNumCount).low(trade.getPrice())
                    .high(trade.getPrice())
                    .volume(trade.getQuantity()).lastTradePrice(trade.getPrice()).close(0).build());
        }
    }

    private Ohlc buildOHLCInformationFromLastTrade(Trade trade, Ohlc lastOHLC, int barNumCount) {
        return Ohlc.builder().symbol(trade.getSym()).lastTradePrice(trade.getPrice()).open(lastOHLC.getOpen()).barNum(barNumCount).low(Math.min(trade.getPrice(), lastOHLC.getLow()))
                .high(Math.max(trade.getPrice(), lastOHLC.getHigh()))
                .volume(lastOHLC.getVolume() + trade.getQuantity()).close(0).build();
    }


}
