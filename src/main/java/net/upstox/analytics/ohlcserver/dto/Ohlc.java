package net.upstox.analytics.ohlcserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ohlc {

    @Builder.Default
    public String event = "ohlc_notify";

    @JsonProperty("o")
    private double open;

    @JsonProperty("h")
    private double high;

    @JsonProperty("l")
    private double low;

    @JsonProperty("c")
    private double close;

    @JsonProperty("bar_num")
    private long barNum;

    @JsonProperty("v")
    private double volume;

    @JsonIgnore
    private double lastTradePrice;

    private String symbol;

}
