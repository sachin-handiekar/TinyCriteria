package com.sachinhandiekar.tinycriteria;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TinyCriteriaTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<TestEntity> criteriaQuery;
    @Mock
    private Root<TestEntity> root;
    @Mock
    private TypedQuery<TestEntity> typedQuery;
    @Mock
    private Path<Object> path;
    @Mock
    private Path stringPath;
    @Mock
    private Predicate predicate;

    private TinyCriteria<TestEntity> tinyCriteria;

    // Test entity class for our tests
    static class TestEntity {
        private Long id;
        private String name;
        private int age;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @BeforeEach
    void setUp() {
        // Setup common mock behavior with lenient stubs
        lenient().when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        lenient().when(criteriaBuilder.createQuery(TestEntity.class)).thenReturn(criteriaQuery);
        lenient().when(criteriaQuery.from(TestEntity.class)).thenReturn(root);
        lenient().when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        lenient().when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        tinyCriteria = new TinyCriteria<>(entityManager, TestEntity.class);
    }

    @Test
    void testEqualsRestriction() {
        // Given
        String name = "John";
        Predicate equalPredicate = mock(Predicate.class);

        // Setup the path and predicate behavior
        when(root.get("name")).thenReturn(path);
        when(criteriaBuilder.equal(path, name)).thenReturn(equalPredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(equalPredicate);

        // When
        tinyCriteria.eq("name", name);
        List<TestEntity> result = tinyCriteria.list();

        // Then
        verify(root).get("name");
        verify(criteriaBuilder).equal(path, name);
        verify(criteriaQuery).where(equalPredicate);
        assertThat(result).isEmpty();
    }

    @Test
    void testGreaterThanRestriction() {
        // Given
        int age = 18;
        Path path = mock(Path.class);
        Predicate gtPredicate = mock(Predicate.class);

        lenient().when(root.get("age")).thenReturn(path);
        lenient().when(criteriaBuilder.greaterThan(path, age)).thenReturn(gtPredicate);
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(gtPredicate);

        // When
        tinyCriteria.gt("age", age);
        List<TestEntity> result = tinyCriteria.list();

        // Then
        verify(root).get("age");
        verify(criteriaBuilder).greaterThan(path, age);
        verify(criteriaQuery).where(gtPredicate);
        assertThat(result).isEmpty();
    }

    @Test
    void testInRestriction() {
        // Given
        List<String> names = Arrays.asList("John", "Jane");

        // Setup the path and predicate behavior
        when(root.get("name")).thenReturn(path);
        when(path.in(names)).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        tinyCriteria.in("name", names);
        List<TestEntity> result = tinyCriteria.list();

        // Then
        verify(path).in(names);
        verify(criteriaQuery).where(predicate);
        assertThat(result).isEmpty();
    }

    @Test
    void testLikeRestriction() {
        // Given
        String pattern = "%John%";
        Predicate likePredicate = mock(Predicate.class);

        // Setup the path and predicate behavior
        when(root.get("name")).thenReturn(stringPath);
        when(criteriaBuilder.like(stringPath, pattern)).thenReturn(likePredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(likePredicate);

        // When
        tinyCriteria.like("name", pattern);
        List<TestEntity> result = tinyCriteria.list();

        // Then
        verify(criteriaBuilder).like(stringPath, pattern);
        verify(criteriaQuery).where(likePredicate);
        assertThat(result).isEmpty();
    }

    @Test
    void testBetweenRestriction() {
        // Given
        int start = 18;
        int end = 30;
        Path path = mock(Path.class);
        Predicate betweenPredicate = mock(Predicate.class);

        lenient().when(root.get("age")).thenReturn(path);
        lenient().when(criteriaBuilder.between(path, start, end)).thenReturn(betweenPredicate);
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(betweenPredicate);

        // When
        tinyCriteria.between("age", start, end);
        List<TestEntity> result = tinyCriteria.list();

        // Then
        verify(root).get("age");
        verify(criteriaBuilder).between(path, start, end);
        verify(criteriaQuery).where(betweenPredicate);
        assertThat(result).isEmpty();
    }

    @Test
    void testPagination() {
        // Given

        // When
        tinyCriteria.setFirstResult(10)
                .setMaxResults(20)
                .list();

        // Then
        verify(typedQuery).setFirstResult(10);
        verify(typedQuery).setMaxResults(20);
    }

    @Test
    void testDistinct() {
        // Given

        // When
        tinyCriteria.setDistinct(true)
                .list();

        // Then
        verify(criteriaQuery).distinct(true);
    }

    @Test
    void testCount() {
        // Given
        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        Expression<Long> countExpression = mock(Expression.class);

        lenient().when(criteriaBuilder.createQuery(Long.class)).thenReturn(countQuery);
        lenient().when(countQuery.from(TestEntity.class)).thenReturn(root);
        lenient().when(criteriaBuilder.count(root)).thenReturn(countExpression);
        lenient().when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        lenient().when(countTypedQuery.getSingleResult()).thenReturn(10L);

        // When
        long count = tinyCriteria.count();

        // Then
        assertThat(count).isEqualTo(10L);
    }
}
