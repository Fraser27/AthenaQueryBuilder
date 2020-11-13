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
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

/**
 * @author fraser.sequeira
 * @date 20-01-2020
 */
@RestController
@RequestMapping(path = "/")
@AllArgsConstructor
@Api(value = "Athena Query Builder")
public class AthenaQueryGenerationController {

	private StockQueryBuilder stockQueryBuilder;

    @ApiOperation(value = "Generates a sample Athena compliant query")
	@PostMapping(path = "/generate/athena/query")
	public String getAthenaQuery(
			@RequestParam(required = true) @ApiParam(value = "fromDate ISO-8601 compliant", example = "2020-01-01", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = true) @ApiParam(value = "toDate ISO-8601 compliant", example = "2020-04-14", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestBody(required = true) @ApiParam(value = "List of brands", required = true) List<String> brands) {
		return stockQueryBuilder.getQueryString(fromDate, toDate, brands);
	}

}
