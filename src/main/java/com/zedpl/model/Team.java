package com.zedpl.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank
    @Size(max = 20)
    @Column(name = "short_name", nullable = false, length = 20)
    private String shortName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 150)
    private String stadium;

    @Column(length = 100)
    private String city;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Stream stream;

    @Min(1800) @Max(2100)
    @Column(name = "founded_year")
    private Integer foundedYear;

    /** Inverse side only, no cascade: players are managed through their own repository. */
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<Player> players = new ArrayList<>();

    protected Team() { }

    public Team(String name, String shortName, Stream stream) {
        this.name = name;
        this.shortName = shortName;
        this.stream = stream;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getStadium() { return stadium; }
    public void setStadium(String stadium) { this.stadium = stadium; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Stream getStream() { return stream; }
    public void setStream(Stream stream) { this.stream = stream; }
    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }
    public List<Player> getPlayers() { return players; }
}
