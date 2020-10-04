package net.upstox.analytics.ohlcserver.reader.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.upstox.analytics.ohlcserver.dto.Trade;
import net.upstox.analytics.ohlcserver.exception.TradesReaderServiceException;
import net.upstox.analytics.ohlcserver.reader.TradesReaderService;
import net.upstox.analytics.ohlcserver.util.ResourceReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TradesReaderServiceImpl implements TradesReaderService {

    private static final String RESOURCE_LOCATION_TEMPLATE = "file:%s".concat(File.separator).concat("trade-data").concat(File.separator).concat("trades.json");

    private final ObjectMapper objectMapper;
    private final ResourceReader resourceReader;

    @Autowired
    public TradesReaderServiceImpl(ResourceReader resourceReader){
        this.resourceReader = resourceReader;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Service reads information from the given data file and transfers it to OhlcComputeService for processing
     * @param tradeBlockingQueue - Output queue for service to which trade information is sent
     * @throws TradesReaderServiceException -if error occurs while reading trade information from file
     */

    @Override
    @Async
    public void readTrades(BlockingQueue<Trade> tradeBlockingQueue) throws TradesReaderServiceException {
        log.info("Initializing reading of trade information");
        Resource tradeDataResource = resourceReader.loadResource(String.format(RESOURCE_LOCATION_TEMPLATE,System.getProperty("user.dir")));
        try(InputStream inputStream = tradeDataResource.getInputStream(); InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
            String line = bufferedReader.readLine();
            while(line!=null){
                tradeBlockingQueue.put(objectMapper.readValue(line, Trade.class));
                line = bufferedReader.readLine();
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error occurred while reading from trades file : {}",e.getMessage());
            throw new TradesReaderServiceException("Error in reading trade information from file");
        }
        log.info("Trade information has been completely read");
    }
}
