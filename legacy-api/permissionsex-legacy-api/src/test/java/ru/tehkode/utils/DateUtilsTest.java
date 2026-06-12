package ru.tehkode.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateUtilsTest {

    @Test
    public void testParseIntervalBySeconds() {
        int result = DateUtils.parseInterval("1000");

        Assertions.assertEquals(1000, result);
    }

    @Test
    public void testParseIntervalByPhrase() {
        int result = DateUtils.parseInterval("1 year 2 months 3 weeks 4 days 5 hours 6 minutes 7 seconds");

        // 7 + 6*60 + 5*3600 + 4*3600*24 + 3*7*3600*24 + 2*30*3600*24 + 1*12*30*3600*24
        Assertions.assertEquals(38466367,  result);
    }

    @Test
    public void testParseIntervalByShortcutPhrase() {
        int result = DateUtils.parseInterval("3 w 4 d 5 h 6 m 7 s");

        // 7 + 6*60 + 5*3600 + 4*3600*24 + 3*7*3600*24
        Assertions.assertEquals(2178367,  result);
    }

    @Test
    public void testParseIntervalByFloatPhrase() {
        int result = DateUtils.parseInterval("7 s 1.5 m");

        // 7 + 1.5 * 60 = 7 + 90
        Assertions.assertEquals(97,  result);
    }

    @Test
    public void testParseIntervalWithTightSyntax() {
        int result = DateUtils.parseInterval("7s 6m 1day");

        // 7 + 6 * 60 + 3600*24
        Assertions.assertEquals(86767,  result);
    }
}
