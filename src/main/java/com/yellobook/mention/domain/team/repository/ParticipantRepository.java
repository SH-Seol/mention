package com.yellobook.mention.domain.team.repository;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yellobook.mention.domain.team.entity.ParticipantEntity;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, Long> {
    @Query("select p from ParticipantEntity p join fetch p.member join fetch p.team")
    Stream<ParticipantEntity> streamAllWithMemberAndTeam();
}
