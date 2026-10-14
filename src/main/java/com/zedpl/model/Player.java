package com.zedpl.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "players")
public class Player extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Min(1) @Max(99)
    @Column(name = "shirt_number")
    private Integer shirtNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlayerPosition position;

    @Column(length = 100)
    private String nationality;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    protected Player() { }

    public Player(String firstName, String lastName, PlayerPosition position, Team team) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.team = team;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Integer getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(Integer shirtNumber) { this.shirtNumber = shirtNumber; }
    public PlayerPosition getPosition() { return position; }
    public void setPosition(PlayerPosition position) { this.position = position; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}
