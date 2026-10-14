package com.zedpl.repository;

import com.zedpl.model.Stream;
import com.zedpl.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByStreamOrderByNameAsc(Stream stream);

    Optional<Team> findByName(String name);
}
