package com.zedpl.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_events")
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private FootballMatch match;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Optional: some events may not be tied to a known player. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private MatchEventType eventType;

    @Min(0) @Max(130)
    @Column(nullable = false)
    private int minute;

    @Min(0)
    @Column(name = "additional_time")
    private Integer additionalTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MatchEvent() { }

    public MatchEvent(FootballMatch match, Team team, Player player, MatchEventType eventType, int minute) {
        this.match = match;
        this.team = team;
        this.player = player;
        this.eventType = eventType;
        this.minute = minute;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public FootballMatch getMatch() { return match; }
    public void setMatch(FootballMatch match) { this.match = match; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public MatchEventType getEventType() { return eventType; }
    public void setEventType(MatchEventType eventType) { this.eventType = eventType; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public Integer getAdditionalTime() { return additionalTime; }
    public void setAdditionalTime(Integer additionalTime) { this.additionalTime = additionalTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
