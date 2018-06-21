package uk.gov.pay.commons.testing.pact.consumers;

import au.com.dius.pact.consumer.PactVerification;
import org.apache.http.client.fluent.Request;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class PactPublishingTest {

    @Rule
    public PactProviderRule weatherService = new PactProviderRule("weather-service", this);

    @Rule
    public PactProviderRule orderService = new PactProviderRule("order-service", this);

    @Test
    @PactVerification({"weather-service", "order-service"})
    @Pacts(pacts = {"consumer-weather-service"})
    @Pacts(pacts = {"consumer-order-service"}, publish = false)
    public void specifyIfPactShouldBeWritten() throws IOException {
        Request.Get(weatherService.getUrl() + "/weather/London").execute();
        Request.Get(orderService.getUrl() + "/orders").execute();
    }

    @AfterClass
    public static void after() {
        assertThat(new File(Paths.get("", "target", "pacts", "consumer-weather-service.json").toAbsolutePath().toString()).exists()).isTrue();
        assertThat(new File(Paths.get("", "target", "pacts", "consumer-order-service.json").toAbsolutePath().toString()).exists()).isFalse();
    }
}
