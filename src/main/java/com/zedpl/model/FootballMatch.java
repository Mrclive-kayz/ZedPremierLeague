package com.zedpl.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Named FootballMatch (table "matches") to avoid the SQL MATCH keyword. */
@Entity
@Table(name = "matches")
public class FootballMatch extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @NotNull
    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Column(length = 150)
    private String venue;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Stream stream;

    @Min(1)
    @Column(name = "match_week", nullable = false)
    private int matchWeek;

    @Min(0)
    @Column(name = "home_score", nullable = false)
    private int homeScore = 0;

    @Min(0)
    @Column(name = "away_score", nullable = false)
    private int awayScore = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.SCHEDULED;

    /** Inverse side only, no cascade; events are created via MatchEventRepository. */
    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY)
    private List<MatchEvent> events = new ArrayList<>();

    protected FootballMatch() { }

    public FootballMatch(Team homeTeam, Team awayTeam, LocalDateTime matchDate, Stream stream, int matchWeek) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchDate = matchDate;
        this.stream = stream;
        this.matchWeek = matchWeek;
    }

    /** Application-level mirror of the DB check ck_matches_different_teams (and same-stream rule). */
    @AssertTrue(message = "Home and away team must be different teams")
    private boolean isDifferentTeams() {
        return homeTeam == null || awayTeam == null || homeTeam != awayTeam
                && !(homeTeam.getId() != null && homeTeam.getId().equals(awayTeam.getId()));
    }

    @AssertTrue(message = "Both teams must belong to the match's stream")
    private boolean isTeamsInMatchStream() {
        return stream == null || homeTeam == null || awayTeam == null
                || (homeTeam.getStream() == stream && awayTeam.getStream() == stream);
    }

    public Long getId() { return id; }
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public Stream getStream() { return stream; }
    public void setStream(Stream stream) { this.stream = stream; }
    public int getMatchWeek() { return matchWeek; }
    public void setMatchWeek(int matchWeek) { this.matchWeek = matchWeek; }
    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }
    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public List<MatchEvent> getEvents() { return events; }
}
