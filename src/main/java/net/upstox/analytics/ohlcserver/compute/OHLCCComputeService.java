package net.upstox.analytics.ohlcserver.compute;

import net.upstox.analytics.ohlcserver.dto.Ohlc;
import net.upstox.analytics.ohlcserver.dto.Trade;
import net.upstox.analytics.ohlcserver.exception.OhlcComputeServiceException;
import org.springframework.util.MultiValueMap;

import java.util.concurrent.BlockingQueue;

public interface OHLCCComputeService {

    void computeOHLCRecords(BlockingQueue<Trade> tradeBlockingQueue, BlockingQueue<MultiValueMap<String, Ohlc>> eventQueue) throws OhlcComputeServiceException;
}
