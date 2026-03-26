package com.yellobook.mention.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yellobook.mention.domain.team.entity.ParticipantEntity;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, Long> {
}
