package athena.query.builder;
/*
 * Copyright (C) 2020 ATHENA Query DSL AUTHOR; Fraser Sequeira
 * All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * */
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.springframework.stereotype.Service;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;
import static athena.query.builder.Utils.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a helper class to generate an Athena query using a default queryDSL
 * template.
 * 
 * @author fraser.sequeira
 *
 */
@Slf4j
@Data
@Service
public final class AthenaQueryBuilder {
	// Default template to be used for Athena Query Generation
	private final SQLTemplates template = H2Templates.builder().printSchema().quote().newLineToSingleSpace().build();

	/**
	 * Get default SQLQuery instance based on H2Templates
	 * 
	 * @return
	 */
	public SQLQuery<?> getAthenaSQLQueryInstance() {
		SQLQuery<?> query = new SQLQuery<>(new Configuration(template));
		query.setUseLiterals(true);
		return query;
	}

	/**
	 * Get a SQLQuery instance with custom configuration.
	 * 
	 * @param config
	 * @return
	 */
	public SQLQuery<?> getAthenaSQLQueryInstance(Configuration config) {
		SQLQuery<?> query = new SQLQuery<>(config);
		query.setUseLiterals(true);
		return query;
	}
	
	/**
	 * Apply Datefilters to query instance.
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param query
	 * @param year
	 * @param month
	 * @param day
	 * @return hasDateFilters true if dateFilters are applied else false
	 */
	public boolean applyDateFiltersToQuery(LocalDate fromDate, LocalDate toDate, SQLQuery<?> query,
			PathBuilder<Object> year, PathBuilder<Object> month, PathBuilder<Object> day) {
		boolean hasDateFilters = false;
		if (year == null || month == null || day == null) {
			log.warn("action=apply_date_filters_to_query, message=Year_or_Month_or_Day_Path_doesnt_exist");
			return false;
		}
		List<DateFilter> dateFilters = getDateFilters(fromDate, toDate);
		if (!isEmpty(dateFilters)) {
			BooleanBuilder bool = new BooleanBuilder();

			dateFilters.forEach(filter -> {
				if (filter.hasYearMonthDay()) {
					bool.or(year.eq(filter.getYear()).and(month.in(filter.getMonths())).and(day.in(filter.getDays())));

				} else if (filter.hasOnlyYearMonth()) {
					bool.or(year.eq(filter.getYear()).and(month.in(filter.getMonths())));

				} else if (filter.hasOnlyYear()) {
					bool.or(year.eq(filter.getYear()));
				}
			});

			if (bool.hasValue()) {
				hasDateFilters = true;
				query.where(bool);
				log.debug("action=apply_date_filters_to_query, date_filter_query=" + query.toString());
			} else {
				log.error("action=apply_date_filters_to_query, message=athena_query_doesnt_contain_date_partition_keys");
			}
		}
		return hasDateFilters;
	}
	
	/**
	 * Computes the DateFilters between two localDates to generate an athena Query
	 * with optimized date based partitioning keys
	 * 
	 * <pre>
	 * <b>Difference in Days:</b>
	 *     If startDate = 2020-04-09 and endDate = 2020-04-19
	 *     DateFilter objects will be as follows: 
	 *         Year -> 2020, Month -> 04, Days -> 09,10,11,12,13,14,15,16,17,18,19
	 * 
	 * <b>Difference in Months:</b>
	 *     If startDate = 2020-02-19 and endDate = 2020-04-19
	 *     DateFilter objects will be as follows:
	 *         Year -> 2020, Month -> 02, Days -> 19,20,21,22,23,24,25,26,27,28,29
	 *         Year -> 2020, Month -> 03 
	 *         Year -> 2020, Month -> 04, Days -> 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19
	 * 
	 * <b>Difference in Years:</b>
	 *     If startDate = 2018-02-17 and endDate = 2020-04-19
	 *     DateFilter objects will be as follows:
	 *         Year -> 2018, Month -> 02, Days -> 17,18,19,20,21,22,23,24,25,26,27,28
	 *         Year -> 2018, Months -> 03,04,05,06,07,08,09,10,11,12
	 *         Year -> 2019
	 *         Year -> 2020, Months -> 01,02,03
	 *         Year -> 2020, Month -> 04, Days -> 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19
	 * </pre>
	 * 
	 * @author fraser.sequeira
	 * 
	 * @param startDate
	 * @param endDate
	 * @return DateFilter list
	 */
	public List<DateFilter> getDateFilters(LocalDate startDate, LocalDate endDate) {
		List<DateFilter> filters = new ArrayList<DateFilter>();
		log.debug("action=get_date_filters, start_date=" + startDate.toString() + ", end_date=" + endDate.toString());
		int firstDayOfStartMonth = startDate.with(firstDayOfMonth()).getDayOfMonth();
		int lastDayOfStartMonth = startDate.with(lastDayOfMonth()).getDayOfMonth();
		int dayOfStartMonth = startDate.getDayOfMonth();
		int startMonth = startDate.getMonthValue();

		int dayOfEndMonth = endDate.getDayOfMonth();
		int endMonth = endDate.getMonthValue();
		int lastDayOfEndMonth = endDate.with(lastDayOfMonth()).getDayOfMonth();
		int firstDayOfEndMonth = endDate.with(firstDayOfMonth()).getDayOfMonth();

		// Find in between years add whole year as partition keys
		// for years on the start and end verify if we need to add months or days
		int yearsInBetween = (int) ChronoUnit.YEARS.between(startDate.plusYears(1).withDayOfYear(1), endDate);
		if (yearsInBetween > 0) {
			log.debug("action=get_date_filters, message=get_filters_spanning_multiple_years, difference_in_years=" + yearsInBetween);
			filters = getFiltersSpanningYears(startDate.getYear(), endDate.getYear(), firstDayOfStartMonth,
					lastDayOfStartMonth, dayOfStartMonth, startMonth, dayOfEndMonth, endMonth, lastDayOfEndMonth,
					firstDayOfEndMonth, yearsInBetween);

		} else {

			int monthsInBetween = (int) ChronoUnit.MONTHS.between(startDate.plusMonths(1).withDayOfMonth(1), endDate);

			if (monthsInBetween > 0) {
				log.debug("action=get_date_filters, message=get_filters_spanning_multiple_months, difference_in_months=" + monthsInBetween);
				filters = getFiltersSpanningMonths(startDate.getYear(), endDate.getYear(), firstDayOfStartMonth,
						lastDayOfStartMonth, dayOfStartMonth, startMonth, dayOfEndMonth, endMonth, lastDayOfEndMonth,
						firstDayOfEndMonth, monthsInBetween);
			} else {
				log.debug("action=get_date_filters, message=get_filters_spanning_multiple_days");
				// Difference is only in days in the same month
				filters = getFiltersSpanningDays(startDate.getYear(), endDate.getYear(), dayOfStartMonth,
						lastDayOfStartMonth, startMonth, dayOfEndMonth, firstDayOfEndMonth, endMonth,
						lastDayOfEndMonth);
			}
		}
		return filters;
	}

	/**
	 * If startDate = 2020-04-09 and endDate = 2020-04-19
	 * 
	 * DateFilter objects will be as follows:
	 * 
	 * Year -> 2020, Month -> 04, Days -> 09,10,11,12,13,14,15,16,17,18,19
	 * 
	 * @author fraser.sequeira
	 * 
	 * @param startYear
	 * @param dayOfStartMonth
	 * @param startMonth
	 * @param dayOfEndMonth
	 * @param firstDayOfEndMonth
	 * @param endMonth
	 * @param lastDayOfEndMonth
	 * @return
	 */
	private List<DateFilter> getFiltersSpanningDays(int startYear, int endYear, int dayOfStartMonth,
			int lastDayOfStartMonth, int startMonth, int dayOfEndMonth, int firstDayOfEndMonth, int endMonth,
			int lastDayOfEndMonth) {
		List<DateFilter> filters = new ArrayList<DateFilter>();
		String fromMonthString = startMonth < 10 ? "0" + startMonth : String.valueOf(startMonth);
		String endMonthString = endMonth < 10 ? "0" + endMonth : String.valueOf(endMonth);
		if (startYear == endYear && startMonth == endMonth) {

			if (dayOfStartMonth == 1 && dayOfEndMonth == lastDayOfEndMonth) {
				filters.add(new DateFilter(startYear, FastList.newListWith(fromMonthString), null));
			} else {
				List<String> days = getDays(dayOfStartMonth, dayOfEndMonth);
				filters.add(new DateFilter(startYear, FastList.newListWith(fromMonthString), days));

			}

		} else {
			// Difference less than a month but spans 2 months
			List<String> days = Lists.mutable.empty();
			if (dayOfStartMonth == 1 && startMonth != endMonth) {
				filters.add(new DateFilter(startYear, FastList.newListWith(fromMonthString), null));
			} else {
				days = getDays(dayOfStartMonth, lastDayOfStartMonth);
				filters.add(new DateFilter(startYear, FastList.newListWith(fromMonthString), days));
			}
			days = getDays(firstDayOfEndMonth, dayOfEndMonth);
			filters.add(new DateFilter(endYear, FastList.newListWith(endMonthString), days));
		}
		return filters;
	}

	/**
	 * If startDate = 2020-02-19 and endDate = 2020-04-19 DateFilter objects will be
	 * as follows:
	 * 
	 * Year -> 2020, Month -> 02, Days -> 19,20,21,22,23,24,25,26,27,28,29 Year ->
	 * 2020, Month -> 03 Year -> 2020, Month -> 04, Days ->
	 * 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19
	 * 
	 * 
	 * @author fraser.sequeira
	 * 
	 * @param startYear
	 * @param endYear
	 * @param firstDayOfStartMonth
	 * @param lastDayOfStartMonth
	 * @param dayOfStartMonth
	 * @param startMonth
	 * @param dayOfEndMonth
	 * @param endMonth
	 * @param lastDayOfEndMonth
	 * @param firstDayOfEndMonth
	 * @param monthsInBetween
	 * @return
	 */
	private List<DateFilter> getFiltersSpanningMonths(int startYear, int endYear, int firstDayOfStartMonth,
			int lastDayOfStartMonth, int dayOfStartMonth, int startMonth, int dayOfEndMonth, int endMonth,
			int lastDayOfEndMonth, int firstDayOfEndMonth, int monthsInBetween) {

		List<DateFilter> filters = new ArrayList<DateFilter>();
		if (monthsInBetween > 0) {
			if (startYear == endYear) {
				List<String> months = getMonths(startMonth + 1, startMonth + monthsInBetween);
				filters.add(new DateFilter(startYear, months, null));
			} else {
				if (startMonth + 1 > 12) {
					List<String> months = getMonths(1, monthsInBetween);
					filters.add(new DateFilter(endYear, months, null));
				} else {
					List<String> months = getMonths(startMonth + 1, 12);
					filters.add(new DateFilter(startYear, months, null));
					int monthsInNextYear = Math.abs(((12 - startMonth) - monthsInBetween));
					if (monthsInNextYear > 0) {
						months = getMonths(1, monthsInNextYear);
						filters.add(new DateFilter(endYear, months, null));
					}

				}
			}
		}

		String fromMonthString = startMonth < 10 ? "0" + startMonth : String.valueOf(startMonth);
		String toMonthString = endMonth < 10 ? "0" + endMonth : String.valueOf(endMonth);

		if (dayOfStartMonth == firstDayOfStartMonth) {
			filters.add(new DateFilter(startYear, FastList.newListWith(fromMonthString), null));
		} else {
			List<String> days = getDays(dayOfStartMonth, lastDayOfStartMonth);
			filters.add(new DateFilter(startYear, FastList.newListWith(fromMonthString), days));
		}

		if (dayOfEndMonth == lastDayOfEndMonth) {
			filters.add(new DateFilter(endYear, FastList.newListWith(toMonthString), null));
		} else {
			List<String> days = getDays(firstDayOfEndMonth, dayOfEndMonth);
			filters.add(new DateFilter(endYear, FastList.newListWith(toMonthString), days));
		}
		return filters;
	}

	/**
	 * If startDate = 2018-02-17 and endDate = 2020-04-19
	 * 
	 * DateFilter objects will be as follows:
	 * 
	 * Year -> 2018, Month -> 02, Days -> 17,18,19,20,21,22,23,24,25,26,27,28 Year
	 * -> 2018, Months -> 03,04,05,06,07,08,09,10,11,12 Year -> 2019 Year -> 2020,
	 * Months -> 01,02,03 Year -> 2020, Month -> 04, Days ->
	 * 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19
	 * 
	 * @author fraser.sequeira
	 * 
	 * @param startYear
	 * @param endYear
	 * @param firstDayOfStartMonth
	 * @param lastDayOfStartMonth
	 * @param dayOfStartMonth
	 * @param startMonth
	 * @param dayOfEndMonth
	 * @param endMonth
	 * @param lastDayOfEndMonth
	 * @param firstDayOfEndMonth
	 * @param yearsInBetween
	 * @return
	 */
	private List<DateFilter> getFiltersSpanningYears(int startYear, int endYear, int firstDayOfStartMonth,
			int lastDayOfStartMonth, int dayOfStartMonth, int startMonth, int dayOfEndMonth, int endMonth,
			int lastDayOfEndMonth, int firstDayOfEndMonth, int yearsInBetween) {

		List<DateFilter> filters = new ArrayList<DateFilter>();

		if (yearsInBetween > 0) {
			List<String> years = getYears(startYear + 1, startYear + yearsInBetween);
			// apply year wise partitions keys for all years in between
			if (!years.isEmpty()) {
				years.forEach(year -> {
					filters.add(new DateFilter(year, null, null));
				});
			}
		}

		String fromMonthString = startMonth < 10 ? "0" + startMonth : String.valueOf(startMonth);
		String toMonthString = endMonth < 10 ? "0" + endMonth : String.valueOf(endMonth);

		log.debug("action=get_filters_spanning_years, from_month=" + fromMonthString + ", to_month=" + toMonthString);

		// If the month is selected is from 1st of Jan add the whole year as a key do
		// not drill down to month level
		if (startMonth == Month.JANUARY.getValue() && dayOfStartMonth == firstDayOfStartMonth) {
			// We can add this whole year to be filtered since it starts from 1st of Jan
			filters.add(new DateFilter(startYear, null, null));
		} else {

			// Starts from 1st day of some month add monthly partitions upto last month
			if (dayOfStartMonth == firstDayOfStartMonth) {
				List<String> months = getMonths(startMonth, Month.DECEMBER.getValue());
				filters.add(new DateFilter(startYear, months, null));

			} else if (dayOfStartMonth != firstDayOfStartMonth) {
				// Add days in current Month and all months in between upto december
				List<String> fromDays = getDays(dayOfStartMonth, lastDayOfStartMonth);
				filters.add(new DateFilter(startYear, FastList.newListWith(fromMonthString), fromDays));

				// Add all months upto Dec of fromDate year
				if (startMonth < Month.DECEMBER.getValue()) {
					List<String> months = getMonths(startMonth + 1, Month.DECEMBER.getValue());
					filters.add(new DateFilter(startYear, months, null));

				}
			}
		}

		if (endMonth == Month.DECEMBER.getValue() && dayOfEndMonth == lastDayOfEndMonth) {
			filters.add(new DateFilter(endYear, null, null));
		} else {
			if (dayOfEndMonth == lastDayOfEndMonth) {
				List<String> months = getMonths(Month.JANUARY.getValue(), endMonth);
				filters.add(new DateFilter(endYear, months, null));
			} else if (dayOfEndMonth != lastDayOfEndMonth) {
				List<String> toDays = getDays(firstDayOfEndMonth, dayOfEndMonth);
				filters.add(new DateFilter(endYear, FastList.newListWith(toMonthString), toDays));

				// Add all months upto Dec of fromDate year
				if (endMonth > Month.JANUARY.getValue()) {
					List<String> months = getMonths(Month.JANUARY.getValue(), endMonth - 1);
					filters.add(new DateFilter(endYear, months, null));

				}
			}
		}
		return filters;
	}

	/**
	 * @param fromYear
	 * @param endYear
	 * @return
	 */
	private List<String> getYears(int fromYear, int endYear) {
		List<String> years = new ArrayList<String>();
		for (Integer i = fromYear; i <= endYear; i++) {
			years.add(i.toString());
		}
		return years;
	}

	/**
	 * @param initialDay
	 * @param endDay
	 * @return
	 */
	private List<String> getDays(int initialDay, int endDay) {
		List<String> fromDays = FastList.newList();
		// Add days of current Month
		for (Integer i = initialDay; i <= endDay; i++) {
			addAsString(fromDays, i);
		}
		return fromDays;
	}

	/**
	 * @param startMonth
	 * @param endMonth
	 * @return
	 */
	private List<String> getMonths(int startMonth, int endMonth) {
		List<String> fromMonths = new ArrayList<String>();
		for (Integer month = startMonth; month <= endMonth; month++) {
			addAsString(fromMonths, month);
		}
		return fromMonths;
	}

	/**
	 * @param fromMonths
	 * @param val
	 */
	private void addAsString(List<String> fromMonths, Integer val) {
		if (val < 10) {
			fromMonths.add("0" + val.toString());
		} else {
			fromMonths.add(val.toString());
		}
	}

}
