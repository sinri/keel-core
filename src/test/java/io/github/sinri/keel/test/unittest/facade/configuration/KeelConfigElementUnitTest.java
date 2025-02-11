package io.github.sinri.keel.test.unittest.facade.configuration;

import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Test;

public class KeelConfigElementUnitTest extends KeelUnitTest {
    @Test
    public void test1() {
        KeelConfigElement keelConfigElement = new KeelConfigElement("");
        keelConfigElement.loadPropertiesFile("test.sample.properties");
        getUnitTestLogger().info(x -> x.message("config").context(keelConfigElement.toJsonObject()));
    }
}
