package com.sachinhandiekar.tinycriteria;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A lightweight and fluent wrapper for JPA Criteria API that simplifies database queries.
 * This class provides an intuitive builder pattern interface for constructing type-safe JPA queries.
 * It supports common query operations such as filtering, sorting, pagination, and aggregation.
 *
 * <p>Example usage:</p>
 * <pre>
 * TinyCriteria&lt;User&gt; criteria = new TinyCriteria&lt;&gt;(entityManager, User.class);
 * List&lt;User&gt; users = criteria
 *     .eq("active", true)
 *     .gt("age", 18)
 *     .like("email", "%@gmail.com")
 *     .setMaxResults(10)
 *     .list();
 * </pre>
 *
 * @param <T> The entity type for which the criteria query is created
 * @author Sachin Handiekar
 * @version 1.0
 */
public class TinyCriteria<T> {
    private final EntityManager entityManager;
    private final Class<T> entityClass;
    private final CriteriaBuilder builder;
    private final CriteriaQuery<T> query;
    private final Root<T> root;
    private final List<Predicate> predicates;
    private final List<Order> orders;
    private Integer firstResult;
    private Integer maxResults;
    private final Map<String, Join<T, ?>> joins;
    private boolean distinct;

    /**
     * Creates a new TinyCriteria instance for the specified entity type.
     *
     * @param entityManager The JPA EntityManager to use for query execution
     * @param entityClass The entity class for which to create queries
     * @throws IllegalArgumentException if entityManager or entityClass is null
     */
    public TinyCriteria(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
        this.builder = entityManager.getCriteriaBuilder();
        this.query = builder.createQuery(entityClass);
        this.root = query.from(entityClass);
        this.predicates = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.joins = new HashMap<>();
        this.distinct = false;
    }

    /**
     * Sets the distinct flag on the query to eliminate duplicate results.
     *
     * @param distinct true to eliminate duplicates, false otherwise
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Adds an equals restriction to the query.
     * The restriction is only added if the value is not null.
     *
     * @param propertyName The name of the entity property to compare
     * @param value The value to compare against
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> eq(String propertyName, Object value) {
        if (value != null) {
            predicates.add(builder.equal(getPath(propertyName), value));
        }
        return this;
    }

    /**
     * Adds a not equals restriction to the query.
     * The restriction is only added if the value is not null.
     *
     * @param propertyName The name of the entity property to compare
     * @param value The value to compare against
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> ne(String propertyName, Object value) {
        if (value != null) {
            predicates.add(builder.notEqual(getPath(propertyName), value));
        }
        return this;
    }

    /**
     * Adds a greater than restriction to the query.
     * The restriction is only added if the value is not null.
     *
     * @param propertyName The name of the entity property to compare
     * @param value The value to compare against
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> gt(String propertyName, Comparable value) {
        if (value != null) {
            predicates.add(builder.greaterThan(getPath(propertyName), value));
        }
        return this;
    }

    /**
     * Adds a greater than or equal restriction to the query.
     * The restriction is only added if the value is not null.
     *
     * @param propertyName The name of the entity property to compare
     * @param value The value to compare against
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> ge(String propertyName, Comparable value) {
        if (value != null) {
            predicates.add(builder.greaterThanOrEqualTo(getPath(propertyName), value));
        }
        return this;
    }

    /**
     * Adds a less than restriction to the query.
     * The restriction is only added if the value is not null.
     *
     * @param propertyName The name of the entity property to compare
     * @param value The value to compare against
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> lt(String propertyName, Comparable value) {
        if (value != null) {
            predicates.add(builder.lessThan(getPath(propertyName), value));
        }
        return this;
    }

    /**
     * Adds a less than or equal restriction to the query.
     * The restriction is only added if the value is not null.
     *
     * @param propertyName The name of the entity property to compare
     * @param value The value to compare against
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> le(String propertyName, Comparable value) {
        if (value != null) {
            predicates.add(builder.lessThanOrEqualTo(getPath(propertyName), value));
        }
        return this;
    }

    /**
     * Adds a between restriction to the query.
     * The restriction is only added if both start and end values are not null.
     *
     * @param propertyName The name of the entity property to compare
     * @param start The lower bound value
     * @param end The upper bound value
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> between(String propertyName, Comparable start, Comparable end) {
        if (start != null && end != null) {
            predicates.add(builder.between(getPath(propertyName), start, end));
        }
        return this;
    }

    /**
     * Adds a like pattern matching restriction to the query.
     * The restriction is only added if the value is not null.
     * Use % for wildcard matching (e.g., "name%" matches names starting with 'name').
     *
     * @param propertyName The name of the entity property to compare
     * @param value The pattern to match against
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> like(String propertyName, String value) {
        if (value != null) {
            predicates.add(builder.like(getPath(propertyName), value));
        }
        return this;
    }

    /**
     * Adds a case-insensitive like pattern matching restriction to the query.
     * The restriction is only added if the value is not null.
     * Use % for wildcard matching (e.g., "name%" matches names starting with 'name').
     *
     * @param propertyName The name of the entity property to compare
     * @param value The pattern to match against (case-insensitive)
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> ilike(String propertyName, String value) {
        if (value != null) {
            predicates.add(builder.like(builder.lower(getPath(propertyName)), value.toLowerCase()));
        }
        return this;
    }

    /**
     * Adds an IN restriction to the query.
     * The restriction is only added if the collection is not null and not empty.
     *
     * @param propertyName The name of the entity property to check
     * @param values The collection of values to match against
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> in(String propertyName, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            predicates.add(getPath(propertyName).in(values));
        }
        return this;
    }

    /**
     * Adds an IS NULL restriction to the query.
     *
     * @param propertyName The name of the entity property to check for null
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> isNull(String propertyName) {
        predicates.add(builder.isNull(getPath(propertyName)));
        return this;
    }

    /**
     * Adds an IS NOT NULL restriction to the query.
     *
     * @param propertyName The name of the entity property to check for non-null
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> isNotNull(String propertyName) {
        predicates.add(builder.isNotNull(getPath(propertyName)));
        return this;
    }

    /**
     * Adds a conjunction (AND) of the given predicates to the query.
     * The conjunction is only added if the predicates array is not null and not empty.
     *
     * @param predicates The predicates to combine with AND
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> and(Predicate... predicates) {
        if (predicates != null && predicates.length > 0) {
            this.predicates.add(builder.and(predicates));
        }
        return this;
    }

    /**
     * Adds a disjunction (OR) of the given predicates to the query.
     * The disjunction is only added if the predicates array is not null and not empty.
     *
     * @param predicates The predicates to combine with OR
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> or(Predicate... predicates) {
        if (predicates != null && predicates.length > 0) {
            this.predicates.add(builder.or(predicates));
        }
        return this;
    }

    /**
     * Creates a subquery of the specified type.
     *
     * @param <U> The type of the subquery result
     * @param type The class representing the subquery type
     * @return A new Subquery instance
     */
    public <U> Subquery<U> subquery(Class<U> type) {
        return query.subquery(type);
    }

    /**
     * Adds a join to the query if it doesn't already exist.
     *
     * @param propertyName The name of the entity property to join with
     * @param joinType The type of join (e.g., LEFT, INNER, RIGHT)
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> join(String propertyName, JoinType joinType) {
        if (!joins.containsKey(propertyName)) {
            joins.put(propertyName, root.join(propertyName, joinType));
        }
        return this;
    }

    /**
     * Retrieves a previously created join.
     *
     * @param propertyName The name of the entity property that was joined
     * @return The Join instance if it exists, null otherwise
     */
    public Join<T, ?> getJoin(String propertyName) {
        return joins.get(propertyName);
    }

    /**
     * Adds an order clause to the query.
     *
     * @param propertyName The name of the entity property to order by
     * @param ascending true for ascending order, false for descending
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> addOrder(String propertyName, boolean ascending) {
        orders.add(ascending ? builder.asc(getPath(propertyName)) : builder.desc(getPath(propertyName)));
        return this;
    }

    /**
     * Sets the first result for pagination.
     *
     * @param firstResult The position of the first result to retrieve (zero-based)
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param maxResults The maximum number of results to retrieve
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Adds a custom predicate to the query.
     * The predicate is only added if it is not null.
     *
     * @param predicate The predicate to add
     * @return this TinyCriteria instance for method chaining
     */
    public TinyCriteria<T> add(Predicate predicate) {
        if (predicate != null) {
            predicates.add(predicate);
        }
        return this;
    }

    /**
     * Gets the path for nested properties.
     * Supports dot notation for nested properties (e.g., "user.address.street").
     *
     * @param propertyPath The property path using dot notation
     * @return The Path instance representing the property
     */
    private <X> Path<X> getPath(String propertyPath) {
        String[] pathParts = propertyPath.split("\\.");
        Path<?> path = root;

        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (joins.containsKey(part)) {
                path = joins.get(part);
            } else {
                path = path.get(part);
            }
        }

        @SuppressWarnings("unchecked")
        Path<X> finalPath = (Path<X>) path.get(pathParts[pathParts.length - 1]);
        return finalPath;
    }

    /**
     * Executes the query and returns the results as a list.
     * This method applies all the restrictions, ordering, and pagination settings
     * that have been configured.
     *
     * @return A List containing the query results
     */
    public List<T> list() {
        if (!predicates.isEmpty()) {
            query.where(builder.and(predicates.toArray(new Predicate[0])));
        }
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }
        if (distinct) {
            query.distinct(true);
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        if (firstResult != null) {
            typedQuery.setFirstResult(firstResult);
        }
        if (maxResults != null) {
            typedQuery.setMaxResults(maxResults);
        }
        return typedQuery.getResultList();
    }

    /**
     * Executes the query and returns a single result.
     * Returns null if no results are found.
     * This method applies all the restrictions and ordering that have been configured.
     *
     * @return The single result or null if none found
     */
    public T uniqueResult() {
        if (!predicates.isEmpty()) {
            query.where(builder.and(predicates.toArray(new Predicate[0])));
        }
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }
        if (distinct) {
            query.distinct(true);
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        List<T> results = typedQuery.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Executes a count query to get the total number of matching records.
     * This method applies all the restrictions that have been configured.
     *
     * @return The number of records matching the criteria
     */
    public long count() {
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        countQuery.select(builder.count(countRoot));

        if (!predicates.isEmpty()) {
            countQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        }
        if (distinct) {
            countQuery.distinct(true);
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * Gets the underlying CriteriaBuilder instance.
     * This can be used for creating custom predicates or expressions.
     *
     * @return The CriteriaBuilder instance
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return builder;
    }

    /**
     * Gets the underlying CriteriaQuery instance.
     * This can be used for advanced query customization.
     *
     * @return The CriteriaQuery instance
     */
    public CriteriaQuery<T> getCriteriaQuery() {
        return query;
    }

    /**
     * Gets the underlying Root instance.
     * This can be used for creating custom path expressions.
     *
     * @return The Root instance
     */
    public Root<T> getRoot() {
        return root;
    }
}
