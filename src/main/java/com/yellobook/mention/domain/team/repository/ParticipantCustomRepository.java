package com.yellobook.mention.domain.team.repository;

import java.util.List;

import com.yellobook.mention.domain.team.dto.QueryTeamMember;

public interface ParticipantCustomRepository {
    List<QueryTeamMember> findMentionsByNamePrefix(String prefix, Long teamId);
    List<QueryTeamMember> findParticipants(Long teamId);
}
