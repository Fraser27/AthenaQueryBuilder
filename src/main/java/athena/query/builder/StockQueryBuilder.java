package athena.query.builder;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.SQLQuery;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class holds logic to generate stock related athena compatible
 * queries.
 * 
 * @author fraser.sequeira
 * @date 19-Apr-2020
 *
 */
@Slf4j
@Service
@AllArgsConstructor
public class StockQueryBuilder {

	private AthenaQueryBuilder queryBuilder;

	private AthenaProperties athenaProperties;

	/**
	 * Generates an Athena Compatible query to retrieve stock data from
	 * Athena.
	 * 
	 * @author fraser.sequeira
	 * @date 19-Apr-2020
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param brands
	 * @return
	 */
	public String getQueryString(LocalDate fromDate, LocalDate toDate, List<String> brands) {
		log.debug("action=get_query_string, from_date=" + fromDate.toString() + " , to_date_time=" + toDate);
		SQLQuery<?> query = queryBuilder.getAthenaSQLQueryInstance();
		PathBuilder<StockEntity> entity = new PathBuilder<StockEntity>(StockEntity.class, athenaProperties.getTable());
		// Add brands
		query.where(entity.get(StockEntity.BRAND_NAME).in(brands));
		// Add Product Filters
		applyProductFilters(query, entity);
		// Add selections
		addSelectionFields(query, entity);
		// Order BY Shipped_timestamp desc
		query.orderBy(entity.getString(StockEntity.SHIPPED_TIMESTAMP).desc());
		query.from(entity);
		log.info("action=get_query_string, query=" + query.toString());
		return query.toString();
	}

	private void applyProductFilters(SQLQuery<?> query, PathBuilder<StockEntity> entity) {
		MutableList<String> productCategories = Lists.mutable.of("toys", "mobiles",
				"essentials");
		BooleanBuilder boolOperationPredicates = new BooleanBuilder();

		BooleanExpression predicate1 = entity.get(StockEntity.PRODUCT_CATEGORY).in(productCategories);
		BooleanExpression predicate2 = entity.get(StockEntity.PRODUCT_CATEGORY).eq("furnitures")
				.and(entity.get(StockEntity.PRODUCT_NAME).eq("sofa"));

		boolOperationPredicates.or(predicate1).or(predicate2);
		query.where(boolOperationPredicates);

	}

	private SQLQuery<Tuple> addSelectionFields(SQLQuery<?> query, PathBuilder<StockEntity> entity) {
		return query.select(entity.get(StockEntity.STOCK_ID), entity.get(StockEntity.PRODUCT_CATEGORY),
				entity.get(StockEntity.PRODUCT_NAME), entity.get(StockEntity.BRAND_NAME),
				entity.get(StockEntity.SHIPPED_TIMESTAMP));
	}
}
