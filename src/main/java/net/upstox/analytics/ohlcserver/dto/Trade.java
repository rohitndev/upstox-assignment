package net.upstox.analytics.ohlcserver.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trade {

    private String sym;

    @JsonProperty("P")
    private double price;

    @JsonProperty("Q")
    private double quantity;

    private Instant timestamp;

    @JsonSetter("TS2")
    public void setTimeStamp(long ts2){
        this.timestamp = Instant.ofEpochMilli(ts2/1000000);
    }
}
