import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//The following is a simple one-class program that takes two hypothetical employees and generates a list of compatible
//times for scheduling a meeting, taking into account their spread timezones.
//This is intended to illustrate the author's familiarity in working with timezones, particularly ZoneId and streams.

public class Main
{

	public static void main(String[] args)
	{
		//vars for our timezones
		//these are hardcoded to be NY and Sydney, but we can actually change the ZoneIds and it should still work
		//of course, the variable names would want to change too, but they are named "newYork" and "sydney" for clarity
		ZoneId newYork = ZoneId.of("America/New_York");
		ZoneId sydney = ZoneId.of("Australia/Sydney");
		Locale locNY = new Locale("en", "NY");
		Locale locAU = new Locale("en", "AU");

		//add 2 days because we dont want to schedule a meeting same day for either guy
		//adding only 1 day might be same-day for sydney guy, it must be 2 days.
		LocalDate eastBaseDay = LocalDate.now(newYork).plusDays(2);

		//generate a list using a stream of the next 10 days, not counting today, according to NY time
		List<LocalDate> eastDays = eastBaseDay.datesUntil(eastBaseDay.plusDays(10))
				.filter(d -> (d.getDayOfWeek() != DayOfWeek.SATURDAY) && (d.getDayOfWeek() != DayOfWeek.SUNDAY))
				.toList();

		//instantiate new lists to hold all the possible hours in the next 10 days
		List<ZonedDateTime> eastHours = new ArrayList<>();
		List<ZonedDateTime> compatHours = new ArrayList<>();

		//now we have a list of hours 7am - 8pm for valid hours to work
		//we could optionally do our time check here, but I made it 2 distinct loops for clarity
		//the first loop generates valid hours for New York
		for(var d : eastDays)
		{
			//generate hours within the day 7am - 8pm
			for(int i = 7; i < 21; i++)
			{
				LocalTime time = LocalTime.of(i, 0, 0, 0);
				LocalDateTime dt = LocalDateTime.of(d, time);
				ZonedDateTime zdt = ZonedDateTime.of(dt, newYork);
				eastHours.add(zdt);
			}
		}

		//the second loop does the time conversion and checking
		//we already filtered to new york's working hours from filling the list
		for(var z : eastHours)
		{
			DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
			ZonedDateTime testSydney = z.withZoneSameInstant(sydney);

			//just need to check if the converted time is between 7 and 8 inclusively
			if(testSydney.getHour() > 6 && testSydney.getHour() < 21
					&& (testSydney.getDayOfWeek() != DayOfWeek.SATURDAY) && (testSydney.getDayOfWeek() != DayOfWeek.SUNDAY))
			{
				compatHours.add(z); //add it to the list of compatible zones for completion sake
				System.out.println(
						"American guy [" + newYork + "] : " + z.format(dtf.withLocale(locNY)) +
						" <---> Sydney guy [" + sydney + "] : " + testSydney.format(dtf.withLocale(locAU))
				);
			}
		}

	}
}
