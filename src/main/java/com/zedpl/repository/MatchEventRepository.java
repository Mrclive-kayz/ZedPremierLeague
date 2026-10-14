package com.zedpl.repository;

import com.zedpl.model.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {

    List<MatchEvent> findByMatchIdOrderByMinuteAscAdditionalTimeAscIdAsc(Long matchId);
}
