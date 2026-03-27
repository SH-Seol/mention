package com.yellobook.mention.domain.team.service;

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
import com.yellobook.mention.domain.team.repository.TeamRedisRepository;

@Service
public class TeamService {
    private final TeamMapper teamMapper;
    private final ParticipantCustomRepository participantRepository;
    private final TeamRedisRepository redisRepository;
    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    public TeamService(TeamMapper teamMapper, ParticipantCustomRepository participantRepository
    , TeamRedisRepository redisRepository) {
        this.teamMapper = teamMapper;
        this.participantRepository = participantRepository;
        this.redisRepository = redisRepository;
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

    //redis 이용해서 조회
    public TeamMemberListResponse searchParticipantsWithRedis(Long teamId, String name){
        long start = System.currentTimeMillis();
        //1. 캐시에 있는지 먼저 조회
        boolean hasCache = redisRepository.hasMentionCache(teamId);

        // cache miss인 경우 db를 조회하여 모든 값을 redis에 저장한다.
        if(!hasCache){
            List<QueryTeamMember> members = participantRepository.findParticipants(teamId);
            redisRepository.saveAllToZSet(teamId, members);
        }
        // redis에서 값을 가져온다.
        List<QueryTeamMember> filtered = redisRepository.findMentionsByPrefixWithRedis(teamId, name);

        long end = System.currentTimeMillis();
        log.info("redis 검색 완료 - 찾은 인원: {}명, 소요 시간: {}ms", filtered.size(), end - start);
        return teamMapper.toTeamMemberListResponse(
                Objects.requireNonNullElse(filtered, List.of())
        );
    }
}
