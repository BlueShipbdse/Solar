package solar;

import com.blueship.solar.util.TimeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TimeTest {

    @Test
    void hhmm() {
        final int parts = 6;
        final int minutePer = 60 / parts;
        for (int i = 0; i < 24 * parts + 1; ++i) {
            int hour = 6 + (i / parts);
            hour %= 12;
            if (hour == 0) {
                hour = 12;
            }
            int minutes = i % parts * minutePer;
            String timeString = String.format("%02d", hour) + ":" + String.format("%02d", minutes) + (i < (6 * parts) || i >= (18 * parts) ? " AM" : " PM");
            Assertions.assertEquals(timeString, TimeUtil.getTimeInHHMM((long) (i * (1000F / parts))));
        }
    }
}