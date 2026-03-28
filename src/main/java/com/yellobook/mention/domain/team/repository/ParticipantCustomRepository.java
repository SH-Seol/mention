package com.yellobook.mention.domain.team.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.yellobook.mention.domain.team.dto.QueryTeamMember;

public interface ParticipantCustomRepository {
    List<QueryTeamMember> findMentionsByNamePrefix(String prefix, Long teamId, Pageable pageable);
    List<QueryTeamMember> findParticipants(Long teamId);
}
