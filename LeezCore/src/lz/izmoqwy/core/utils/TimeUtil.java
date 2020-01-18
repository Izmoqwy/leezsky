package lz.izmoqwy.core.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

public class TimeUtil {

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class TimeValue {

		private int length;
		private ChronoUnit unit;

		public long toMillis() {
			return unit.getDuration().toMillis() * length;
		}

		public String toLocale() {
			return length + Objects.requireNonNull(getChronoFieldFromUnit(unit)).getDisplayName(Locale.FRENCH);
		}

	}

	public static TimeValue parseTime(String toParse) throws ParseException {
		int time = NumberFormat.getInstance().parse(toParse).intValue();

		ChronoUnit unit;
		if (toParse.endsWith("s") || toParse.endsWith("sec") || toParse.endsWith("secs"))
			unit = ChronoUnit.SECONDS;
		else if (toParse.endsWith("m") || toParse.endsWith("min") || toParse.endsWith("mins"))
			unit = ChronoUnit.MINUTES;
		else if (toParse.endsWith("h") || toParse.endsWith("hour") || toParse.endsWith("hours"))
			unit = ChronoUnit.HOURS;
		else if (toParse.endsWith("d") || toParse.endsWith("day") || toParse.endsWith("days"))
			unit = ChronoUnit.DAYS;
		else if (toParse.endsWith("w") || toParse.endsWith("week") || toParse.endsWith("weeks"))
			unit = ChronoUnit.WEEKS;
		else if (toParse.endsWith("mo") || toParse.endsWith("month") || toParse.endsWith("months"))
			unit = ChronoUnit.MONTHS;
		else if (toParse.endsWith("y") || toParse.endsWith("year") || toParse.endsWith("years"))
			unit = ChronoUnit.YEARS;
		else
			return null;

		return new TimeValue(time, unit);
	}

	public static ChronoField getChronoFieldFromUnit(ChronoUnit chronoUnit) {
		switch (chronoUnit) {
			case WEEKS:
				return ChronoField.ALIGNED_WEEK_OF_MONTH;
			case DAYS:
				return ChronoField.DAY_OF_WEEK;
			case HOURS:
				return ChronoField.HOUR_OF_DAY;
			case SECONDS:
				return ChronoField.SECOND_OF_MINUTE;
			case MINUTES:
				return ChronoField.MINUTE_OF_HOUR;
			case MONTHS:
				return ChronoField.MONTH_OF_YEAR;
			case YEARS:
				return ChronoField.YEAR;
		}
		return null;
	}

}
