package io.github.sinri.keel.test.unittest.core.cron;

import io.github.sinri.keel.core.cron.KeelCronExpression;
import io.github.sinri.keel.core.cron.ParsedCalenderElements;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CronExpressionUnitTest extends KeelUnitTest {
    @Test
    public void testCronExpression() {
        KeelCronExpression keelCronExpression = new KeelCronExpression("3 5,6 * * *");
        boolean match = keelCronExpression.match(new ParsedCalenderElements(3, 6, 1, 1, 1));
        Assertions.assertTrue(match);
    }
}
