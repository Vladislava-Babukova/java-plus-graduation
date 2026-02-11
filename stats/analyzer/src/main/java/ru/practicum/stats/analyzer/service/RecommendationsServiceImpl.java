package ru.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.stats.analyzer.dal.dao.InteractionRepository;
import ru.practicum.stats.analyzer.dal.dao.SimilarityRepository;
import ru.practicum.stats.analyzer.dal.dto.EventRatingDto;
import ru.practicum.stats.analyzer.dal.model.interaction.Interaction;
import ru.practicum.stats.analyzer.dal.model.similarity.Similarity;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationsServiceImpl implements RecommendationsService {

    private final SimilarityRepository similarityRepository;

    private final InteractionRepository interactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {

        List<Interaction> interactions = interactionRepository
                .findAllById_UserIdOrderByActionDateTimeDesc(request.getUserId(), Limit.of(request.getMaxResults()));

        log.debug("Получены N мероприятий недавно просмотренных пользователем: {}", interactions);

        if (interactions.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> interactedEventIds = interactions.stream()
                .map(i -> i.getId().getEventId())
                .collect(Collectors.toSet());


        List<Similarity> notInteractedSimilarities = similarityRepository
                .findAllByEvent1InAndEvent2NotInOrderBySimilarityDesc(interactedEventIds, Limit.of(request.getMaxResults()));

        log.debug("Найдены похожие N, с котороми пользователь еще не взаимодействовал: {}", notInteractedSimilarities);

        Set<Long> notInteractedEventIds = notInteractedSimilarities.stream()
                .map(s -> interactedEventIds.contains(s.getId().getEvent1()) ? s.getId().getEvent2() : s.getId().getEvent1())
                .collect(Collectors.toSet());

        log.debug("Преобразованны к списку ID, с котороми пользователь еще не взаимодействовал: {}", notInteractedEventIds);


        return notInteractedEventIds.stream()
                .map(predictedEventId -> {
                    List<Similarity> topKSimilarInteractedEvents = similarityRepository
                            .findAllByEvent1EqualsPredictedAndEvent2InInteractedOrderBySimilarityDesc(predictedEventId, interactedEventIds, request.getMaxResults());

                    log.debug("Для мероприятия {} найдены K наиболее похожих уже просмотренных мероприятий пользователем {}", predictedEventId, topKSimilarInteractedEvents);

                    if (topKSimilarInteractedEvents.isEmpty()) {
                        return RecommendedEventProto.newBuilder()
                                .setEventId(predictedEventId)
                                .setScore(0.0)
                                .build();
                    }

                    Map<Long, Double> neighborsSimilarities = topKSimilarInteractedEvents.stream()
                            .collect(Collectors.toMap(
                                    s -> s.getId().getEvent1().equals(predictedEventId) ? s.getId().getEvent2() : s.getId().getEvent1(),
                                    Similarity::getSimilarity)
                            );

                    log.debug("Коэффициенты подобия ближайших соседей для мероприятия {}: {}", predictedEventId, neighborsSimilarities);


                    Map<Long, Double> neighborsRatings = topKSimilarInteractedEvents.stream()
                            .map(s -> s.getId().getEvent1().equals(predictedEventId) ? s.getId().getEvent2() : s.getId().getEvent1())
                            .map(likedEventId -> {
                                        Optional<Interaction> interactionOpt = interactions.stream()
                                                .filter(i -> i.getId().getEventId().equals(likedEventId))
                                                .findFirst();
                                        if (interactionOpt.isEmpty()) {
                                            log.error("Interaction not found for eventId: {}", likedEventId);
                                        }
                                        return interactionOpt;
                                    }
                            )
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toMap(i -> i.getId().getEventId(), Interaction::getRating));

                    log.debug("Оценки ближайших соседей для мероприятия {}: {}", predictedEventId, neighborsRatings);


                    double numerator = neighborsSimilarities.entrySet().stream()
                            .mapToDouble(s -> s.getValue() * neighborsRatings.getOrDefault(s.getKey(), 0.0))
                            .sum();

                    double denominator = neighborsSimilarities.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .sum();

                    log.debug("Numerator: {}, Denominator: {}", numerator, denominator);

                    double prediction;
                    if (denominator == 0 || Math.abs(denominator) < 0.000001) {
                        log.debug("Denominator is zero or too small for event {}. Using default prediction.", predictedEventId);
                        prediction = 0.0;
                    } else {
                        prediction = numerator / denominator;
                    }

                    return RecommendedEventProto.newBuilder()
                            .setEventId(predictedEventId)
                            .setScore(prediction)
                            .build();
                })
                .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed()) // сортировка по убыванию рейтинга
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<Interaction> interactions = interactionRepository.findAllById_UserId(request.getUserId());

        log.debug("Получены все мероприятия просмотренные пользователем: {}", interactions);

        Set<Long> interactedEventIds = interactions.stream()
                .map(i -> i.getId().getEventId())
                .collect(Collectors.toSet());

        return similarityRepository.findAllByEventIdAndNotInInteractedOrderBySimilarityDesc(request.getEventId(), interactedEventIds, request.getMaxResults())
                .stream()
                .map(s -> {
                            Long similarEvent = s.getId().getEvent1().equals(request.getEventId()) ? s.getId().getEvent2() : s.getId().getEvent1();

                            return RecommendedEventProto.newBuilder()
                                    .setEventId(similarEvent)
                                    .setScore(s.getSimilarity())
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedEventProto> getInteractionsCounts(InteractionsCountRequestProto request) {
        return interactionRepository.findGroupedRatingsAsDto(request.getEventIdList()).stream()
                .map(EventRatingDto::toProto)
                .toList();
    }

}
