package net.upstox.analytics.ohlcserver.exception;

public class OhlcEventServiceException extends Exception {

    public OhlcEventServiceException(String errorMsg){
        super(errorMsg);
    }
}
