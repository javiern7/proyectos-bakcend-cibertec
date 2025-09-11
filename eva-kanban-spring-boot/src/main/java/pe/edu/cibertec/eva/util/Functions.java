package pe.edu.cibertec.eva.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;


public final class Functions {

    private Functions() {
    }

    /* ==== helpers de colección ==== */
    public static void addIfNotNull(List<Predicate> list, Predicate p) {
        if (p != null) list.add(p);
    }

    /* ==== equals null-safe (ignora String en blanco) ==== */
    public static <T> Predicate eq(Root<T> root, CriteriaBuilder cb, String field, Object value) {
        if (value == null) return null;
        if (value instanceof String s && s.isBlank()) return null;
        return cb.equal(root.get(field), value);
    }

    /* ==== like case-insensitive (ignora null/blanco) ==== */
    public static <T> Predicate likeIgnoreCase(Root<T> root, CriteriaBuilder cb, String field, String value) {
        if (value == null || value.isBlank()) return null;
        return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    /* ==== IN (ignora null/empty) ==== */
    public static <T> Predicate in(Root<T> root, CriteriaBuilder cb, String field, Collection<?> values) {
        if (values == null || values.isEmpty()) return null;
        Path<Object> path = root.get(field);
        return path.in(values);
    }

    /* ==== rango de fechas LocalDateTime (ignora nulls) ==== */
    public static <T> Predicate between(Root<T> root, CriteriaBuilder cb, String field,
                                        LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) return null;
        Path<LocalDateTime> p = root.get(field);
        if (from != null && to != null) return cb.between(p, from, to);
        if (from != null) return cb.greaterThanOrEqualTo(p, from);
        return cb.lessThanOrEqualTo(p, to);
    }

    /* ==== desde “days” (>= hoy - days) sobre LocalDateTime ==== */
    public static <T> Predicate fromLastDays(Root<T> root, CriteriaBuilder cb, String field, Integer days) {
        if (days == null || days <= 0) return null;
        LocalDateTime since = LocalDate.now().minusDays(days).atStartOfDay();
        return cb.greaterThanOrEqualTo(root.get(field), since);
    }

    /* ==== boolean TRUE (boxed) ==== */
    public static <T> Predicate isTrue(Root<T> root, CriteriaBuilder cb, String field, Boolean value) {
        if (!Boolean.TRUE.equals(value)) return null;
        return cb.isTrue(root.get(field));
    }

    public static LocalDate parseDate(String s) {
        if (s == null) return null;
        try { return LocalDate.parse(s); } catch (Exception ignored) {}
        try { return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/uuuu")); }
        catch (Exception ignored) { return null; }
    }

}
