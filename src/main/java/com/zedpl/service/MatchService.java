package com.zedpl.service;

import com.zedpl.dto.MatchEventResponse;
import com.zedpl.dto.MatchResponse;
import com.zedpl.dto.PagedResponse;
import com.zedpl.exception.ResourceNotFoundException;
import com.zedpl.exception.ResourceOwnershipException;
import com.zedpl.model.FootballMatch;
import com.zedpl.model.MatchEvent;
import com.zedpl.model.MatchStatus;
import com.zedpl.model.Stream;
import com.zedpl.repository.FootballMatchRepository;
import com.zedpl.repository.MatchEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for match and match-event queries.
 */
@Service
@Transactional(readOnly = true)
public class MatchService {

    private final FootballMatchRepository matchRepository;
    private final MatchEventRepository eventRepository;

    public MatchService(FootballMatchRepository matchRepository,
                        MatchEventRepository eventRepository) {
        this.matchRepository = matchRepository;
        this.eventRepository = eventRepository;
    }

    // ------------------------------------------------------------------ //
    // Matches                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Returns a paginated list of matches with optional filters.
     * All filter parameters are optional (null = no constraint).
     *
     * @param stream    filter by league stream (A or B)
     * @param status    filter by match status
     * @param teamId    filter by team (home or away)
     * @param matchWeek filter by match week number
     * @param from      lower bound on match date (inclusive)
     * @param to        upper bound on match date (inclusive)
     * @param pageable  pagination and sorting
     */
    public PagedResponse<MatchResponse> findAll(
            Stream stream,
            MatchStatus status,
            Long teamId,
            Integer matchWeek,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {

        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt = (to != null) ? to.atTime(23, 59, 59) : null;

        Page<FootballMatch> page = matchRepository.findByFilters(
                stream, status, teamId, matchWeek, fromDt, toDt, pageable);
        return PagedResponse.from(page.map(MatchResponse::from));
    }

    /** Returns a single match by ID, throwing 404 if not found. */
    public MatchResponse findById(Long id) {
        FootballMatch match = matchRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Match", id));
        return MatchResponse.from(match);
    }

    /**
     * Returns upcoming fixtures (status = SCHEDULED), ordered by match date ascending.
     * Defaults to a maximum of 10 results when size is not specified.
     */
    public PagedResponse<MatchResponse> findUpcoming(Pageable pageable) {
        Pageable sorted = withSort(pageable, Sort.by("matchDate").ascending());
        Page<FootballMatch> page = matchRepository.findByStatusOrderByMatchDateAsc(
                MatchStatus.SCHEDULED, sorted);
        return PagedResponse.from(page.map(MatchResponse::from));
    }

    /**
     * Returns recently played matches (status = FULL_TIME), newest first.
     */
    public PagedResponse<MatchResponse> findRecent(Pageable pageable) {
        Pageable sorted = withSort(pageable, Sort.by("matchDate").descending());
        Page<FootballMatch> page = matchRepository.findByStatusOrderByMatchDateDesc(
                MatchStatus.FULL_TIME, sorted);
        return PagedResponse.from(page.map(MatchResponse::from));
    }

    // ------------------------------------------------------------------ //
    // Match events                                                        //
    // ------------------------------------------------------------------ //

    /**
     * Returns all events for a given match, ordered by minute (and additional
     * time), then by insertion order to break ties.
     */
    public List<MatchEventResponse> findEventsByMatchId(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw ResourceNotFoundException.forEntity("Match", matchId);
        }
        return eventRepository.findByMatchIdOrderByMinuteAscAdditionalTimeAscIdAsc(matchId)
                .stream()
                .map(MatchEventResponse::from)
                .toList();
    }

    /**
     * Returns a single match event, verifying it belongs to the specified match.
     *
     * @throws ResourceNotFoundException   when the event does not exist
     * @throws ResourceOwnershipException  when the event exists but belongs to a different match
     */
    public MatchEventResponse findEventByMatchAndEventId(Long matchId, Long eventId) {
        if (!matchRepository.existsById(matchId)) {
            throw ResourceNotFoundException.forEntity("Match", matchId);
        }
        MatchEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("MatchEvent", eventId));

        if (!event.getMatch().getId().equals(matchId)) {
            throw new ResourceOwnershipException(
                    "Event " + eventId + " does not belong to match " + matchId);
        }
        return MatchEventResponse.from(event);
    }

    // ------------------------------------------------------------------ //
    // Helpers                                                             //
    // ------------------------------------------------------------------ //

    /** Returns a Pageable with the given sort, preserving page and size. */
    private Pageable withSort(Pageable pageable, Sort sort) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
