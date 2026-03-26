package com.yellobook.mention.domain.team.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yellobook.mention.domain.team.dto.QueryTeamMember;
import com.yellobook.mention.domain.team.dto.TeamMemberListResponse;
import com.yellobook.mention.domain.team.mapper.TeamMapper;
import com.yellobook.mention.domain.team.repository.ParticipantCustomRepository;

@Service
public class TeamService {
    private final TeamMapper teamMapper;
    private final ParticipantCustomRepository participantRepository;
    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    public TeamService(TeamMapper teamMapper, ParticipantCustomRepository participantRepository) {
        this.teamMapper = teamMapper;
        this.participantRepository = participantRepository;
    }

    public TeamMemberListResponse searchParticipants(Long teamId, String name) {

        long start = System.currentTimeMillis();
        List<QueryTeamMember> mentions = participantRepository.findMentionsByNamePrefix(name, teamId);
        long end = System.currentTimeMillis();

        log.info("검색 완료 - 찾은 인원: {}명, 소요 시간: {}ms", mentions.size(), end - start);

        //mentions가 null인 경우, emptyList를 반환하도록 설정
        return teamMapper.toTeamMemberListResponse(
                Objects.requireNonNullElse(mentions, Collections.emptyList())
        );
    }
}
