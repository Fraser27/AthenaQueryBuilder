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
import java.util.List;
import lombok.Data;
import static athena.query.builder.Utils.*;
/**
 * Filter POJO to hold date information to be applied to Athena Query
 * 
 * @author fraser.sequeira
 * 
 * @see QueryBuilder#getDateFilters(java.time.LocalDate, java.time.LocalDate)
 */
@Data
public class DateFilter {

	private String year;
	private List<String> months;
	private List<String> days;

	public DateFilter(String year, List<String> months, List<String> days) {
		super();
		this.year = year;
		this.months = months;
		this.days = days;
	}

	public DateFilter(int year, List<String> months, List<String> days) {
		super();
		this.year = String.valueOf(year);
		this.months = months;
		this.days = days;
	}

	public boolean hasYearMonthDay() {
		return this.year != null && !isEmpty(months) && !isEmpty(days);
	}

	public boolean hasOnlyYearMonth() {
		return this.year != null && !isEmpty(months) && isEmpty(days);
	}

	public boolean hasOnlyYear() {
		return this.year != null && isEmpty(months) && isEmpty(days);
	}

}
