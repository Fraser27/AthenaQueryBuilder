package athena.query.builder;

import org.springframework.stereotype.Service;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a helper class to generate an Athena query using a default queryDSL
 * template.
 * 
 * @author fraser.sequeira
 * @date 19-Apr-2020
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
}
