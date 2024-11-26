# TinyCriteria

A lightweight and fluent wrapper for JPA Criteria API that simplifies database queries in Java applications. TinyCriteria provides an intuitive builder pattern interface for constructing type-safe JPA queries.

## Features

- Fluent API for building JPA Criteria queries
- Type-safe query construction
- Support for all common query operations:
  - Equality and inequality comparisons
  - Greater than/less than operations
  - Between ranges
  - Like patterns
  - Collection membership (IN clause)
  - Null checks
  - AND/OR conditions
- Pagination support
- Distinct queries
- Count queries
- Join operations

## Requirements

- Java 17 or higher
- Jakarta Persistence API 3.2.0 or higher
- A JPA implementation (e.g., Hibernate)

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.sachinhandiekar</groupId>
    <artifactId>TinyCriteria</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Usage Examples

### Creating a TinyCriteria Instance

```java
EntityManager entityManager = // get your EntityManager instance
TinyCriteria<User> criteria = new TinyCriteria<>(entityManager, User.class);
```

### Basic Query Construction

```java
// Simple equals condition
List<User> users = criteria
    .eq("name", "John")
    .list();

// Multiple conditions with method chaining
List<User> activeAdultUsers = criteria
    .eq("active", true)
    .gt("age", 18)
    .list();

// Using not equals
List<User> nonAdminUsers = criteria
    .ne("role", "ADMIN")
    .list();
```

### Advanced Filtering

```java
// Complex conditions
List<User> filteredUsers = criteria
    .gt("age", 18)                    // Age > 18
    .lt("age", 65)                    // AND Age < 65
    .eq("active", true)               // AND active = true
    .like("email", "%@company.com")   // AND email ends with @company.com
    .isNotNull("phoneNumber")         // AND phoneNumber is not null
    .list();

// Using OR conditions
Predicate isAdmin = entityManager.getCriteriaBuilder().equal(criteria.getRoot().get("role"), "ADMIN");
Predicate isModerator = entityManager.getCriteriaBuilder().equal(criteria.getRoot().get("role"), "MODERATOR");

List<User> staffUsers = criteria
    .or(isAdmin, isModerator)         // role = 'ADMIN' OR role = 'MODERATOR'
    .eq("active", true)               // AND active = true
    .list();
```

### Working with Dates

```java
Date startDate = // your start date
Date endDate = // your end date

List<Order> recentOrders = criteria
    .between("orderDate", startDate, endDate)
    .list();

// Orders from last 7 days
Date sevenDaysAgo = Date.from(LocalDateTime.now().minusDays(7).toInstant(ZoneOffset.UTC));
List<Order> lastWeekOrders = criteria
    .ge("orderDate", sevenDaysAgo)
    .list();
```

### Pattern Matching

```java
// Case-sensitive LIKE
List<User> gmailUsers = criteria
    .like("email", "%@gmail.com")
    .list();

// Case-insensitive LIKE
List<User> johnUsers = criteria
    .ilike("name", "john%")  // Matches "John", "JOHN", "johnny", etc.
    .list();
```

### Collection Membership

```java
// IN clause with a collection
List<String> roles = Arrays.asList("ADMIN", "MANAGER", "SUPERVISOR");
List<User> managementUsers = criteria
    .in("role", roles)
    .list();

// NOT IN can be achieved using subqueries
Subquery<String> subquery = criteria.subquery(String.class);
Root<Role> roleRoot = subquery.from(Role.class);
subquery.select(roleRoot.get("name"))
        .where(criteria.getCriteriaBuilder().equal(roleRoot.get("type"), "SYSTEM"));

List<User> nonSystemUsers = criteria
    .add(criteria.getCriteriaBuilder().not(criteria.getRoot().get("role").in(subquery)))
    .list();
```

### Joins

```java
// Inner join
List<User> usersWithOrders = criteria
    .join("orders", JoinType.INNER)
    .gt("orders.amount", 1000.0)
    .list();

// Left join with multiple conditions
List<User> usersWithRecentOrders = criteria
    .join("orders", JoinType.LEFT)
    .join("orders.items", JoinType.INNER)
    .gt("orders.orderDate", startDate)
    .lt("orders.orderDate", endDate)
    .eq("orders.items.category", "Electronics")
    .list();
```

### Pagination

```java
// Get second page (20 items per page)
List<User> secondPageUsers = criteria
    .setFirstResult(20)   // Skip first 20 results
    .setMaxResults(20)    // Get next 20 results
    .list();

// Get top 5 users by age
List<User> topUsers = criteria
    .addOrder("age", false)  // false for descending order
    .setMaxResults(5)
    .list();
```

### Distinct Results

```java
// Get distinct user names
List<User> distinctUsers = criteria
    .setDistinct(true)
    .list();

// Count distinct users
long distinctCount = criteria
    .setDistinct(true)
    .count();
```

### Aggregate Queries

```java
// Count total users
long totalUsers = criteria.count();

// Count users matching criteria
long activeUsersCount = criteria
    .eq("active", true)
    .count();

// Get single result
User user = criteria
    .eq("email", "john@example.com")
    .uniqueResult();
```

### Working with Nested Properties

```java
// Query using nested properties
List<User> usersInNewYork = criteria
    .eq("address.city", "New York")
    .eq("address.country", "USA")
    .list();

// Deep nesting
List<Order> ordersByUserCity = criteria
    .eq("user.address.city", "London")
    .list();
```

### Custom Predicates

```java
CriteriaBuilder builder = criteria.getCriteriaBuilder();
Root<User> root = criteria.getRoot();

// Create custom predicate
Predicate customPredicate = builder.or(
    builder.equal(root.get("status"), "ACTIVE"),
    builder.and(
        builder.equal(root.get("status"), "PENDING"),
        builder.greaterThan(root.get("lastActivityDate"), someDate)
    )
);

// Add custom predicate to criteria
List<User> users = criteria
    .add(customPredicate)
    .list();
```

## Best Practices

1. **Resource Management**
   - Always use TinyCriteria within a transaction
   - Close EntityManager when done

2. **Performance**
   - Use pagination for large result sets
   - Add necessary indexes on your database
   - Use joins instead of separate queries when possible

3. **Null Handling**
   - TinyCriteria automatically skips null values in conditions
   - Use isNull() and isNotNull() explicitly when needed

4. **Type Safety**
   - Use the generic type parameter to ensure type safety
   - Use proper field names to avoid runtime errors

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
