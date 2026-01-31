package ru.practicum.event.dao;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.api.request.dto.RequestDto;
import ru.practicum.event.dto.AdminEventDto;
import ru.practicum.event.dto.EventParams;
import ru.practicum.event.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventSpecifications {

    public static Specification<Event> adminSpecification(AdminEventDto adminEventDto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (adminEventDto.getUsers() != null) {
                predicates.add(root.get("initiator").get("id").in(adminEventDto.getUsers()));
            }

            if (adminEventDto.getStates() != null) {
                predicates.add(root.get("state").in(adminEventDto.getStates()));
            }

            if (adminEventDto.getCategories() != null) {
                predicates.add(root.get("category").get("id").in(adminEventDto.getCategories()));
            }

            if (adminEventDto.getRangeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), adminEventDto.getRangeStart()));
            }

            if (adminEventDto.getRangeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), adminEventDto.getRangeEnd()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> publicSpecification(EventParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.getText() != null && !params.getText().isEmpty()) {
                String searchPattern = "%" + params.getText().toLowerCase() + "%";
                Predicate annotationPredicate = cb.like(cb.lower(root.get("annotation")), searchPattern);
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(annotationPredicate, descriptionPredicate));
            }

            if (params.getCategories() != null) {
                predicates.add(root.get("category").get("id").in(params.getCategories()));
            }

            if (params.getPaid() != null) {
                predicates.add(cb.equal(root.get("paid"), params.getPaid()));
            }

            if (params.getRangeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), params.getRangeStart()));
            }

            if (params.getRangeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), params.getRangeEnd()));
            }

            if (params.getOnlyAvailable()) {
                Join<Event, RequestDto> requestJoin = root.join("requests", JoinType.LEFT);
                requestJoin.on(cb.equal(requestJoin.get("status"), "CONFIRMED"));
                query.groupBy(root.get("id"));

                Predicate unlimitedPredicate = cb.equal(root.get("participantLimit"), 0);
                Predicate hasFreeSeatsPredicate = cb.greaterThan(root.get("participantLimit"), cb.count(requestJoin));
                query.having(cb.or(unlimitedPredicate, hasFreeSeatsPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
