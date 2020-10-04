package net.upstox.analytics.ohlcserver.reader;

import net.upstox.analytics.ohlcserver.dto.Trade;
import net.upstox.analytics.ohlcserver.exception.TradesReaderServiceException;

import java.util.concurrent.BlockingQueue;

public interface TradesReaderService {

    void readTrades(BlockingQueue<Trade> tradeBlockingQueue) throws TradesReaderServiceException;

}
