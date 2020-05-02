package athena.query.builder;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Load Configuration properties
 * 
 * @author fraser.sequeira
 * @date 17-Dec-2018
 */
@Component
@ConfigurationProperties("aws.athena")
@Data
public class AthenaProperties {
	private String table;
}
