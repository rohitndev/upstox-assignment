This is a maven project


TO START APPLICATION:

IN IDE:
Run net.upstox.analytics.ohlcserver.OhlcServerApplication
Kindly ensure trades.json file is present inside trade-data folder

AS JAR:

A.Build
1. mvn clean install
2. Once jar is built , kindly ensure both the jar and the folder trades-data exist in the same directory
3. Place trades.json inside trades-data folder

B. RUN
1. In CMD: java -jar ohlcserver-0.0.1-SNAPSHOT.jar

