package athena.query.builder;

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
