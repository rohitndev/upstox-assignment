package net.upstox.analytics.ohlcserver.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ResourceReader.class })
public class ResourceReaderTest {

    @Autowired
    private ResourceReader resourceReader;

    @Test
    public void testLoadResource(){
        Assertions.assertNotNull(resourceReader.loadResource("classpath:trades.json"));
    }
}
