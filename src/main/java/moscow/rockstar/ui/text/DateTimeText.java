package moscow.rockstar.ui.text;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import moscow.rockstar.ui.localization.Localization;

/** Text values used by the client chrome and main-menu status bar. */
public final class DateTimeText {
    private static final String[] DAY_TRANSLATION_KEYS = {
        "time.days.monday",
        "time.days.tuesday",
        "time.days.wednesday",
        "time.days.thursday",
        "time.days.friday",
        "time.days.saturday",
        "time.days.sunday"
    };

    private static final String[] MONTH_TRANSLATION_KEYS = {
        "time.months.january",
        "time.months.february",
        "time.months.march",
        "time.months.april",
        "time.months.may",
        "time.months.june",
        "time.months.july",
        "time.months.august",
        "time.months.september",
        "time.months.october",
        "time.months.november",
        "time.months.december"
    };

    private DateTimeText() {
    }

    public static String currentDate() {
        LocalDate date = LocalDate.now();
        String dayName = Localization.translate(DAY_TRANSLATION_KEYS[date.getDayOfWeek().getValue() - 1]);
        String monthName = Localization.translate(MONTH_TRANSLATION_KEYS[date.getMonthValue() - 1]);
        return String.format("%s, %d %s", dayName, date.getDayOfMonth(), monthName);
    }

    public static String currentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}
