package com.yellobook.mention.domain.team.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.yellobook.mention.domain.team.document.ParticipantDocument;
import com.yellobook.mention.domain.team.dto.QueryTeamMember;
import com.yellobook.mention.domain.team.dto.TeamMemberListResponse;
import com.yellobook.mention.domain.team.mapper.TeamMapper;
import com.yellobook.mention.domain.team.repository.ParticipantCustomRepository;
import com.yellobook.mention.domain.team.repository.ParticipantEsRepository;
import com.yellobook.mention.domain.team.repository.TeamRedisRepository;

@Service
public class TeamService {
    private final TeamMapper teamMapper;
    private final ParticipantCustomRepository participantRepository;
    private final TeamRedisRepository redisRepository;
    private final ParticipantEsRepository participantEsRepository;
    private final ElasticsearchOperations esOperations;
    private static final Logger log = LoggerFactory.getLogger(TeamService.class);
    private static final int PAGESIZE = 500;

    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);

    public TeamService(TeamMapper teamMapper, ParticipantCustomRepository participantRepository
    , TeamRedisRepository redisRepository, ParticipantEsRepository participantEsRepository
    , ElasticsearchOperations esOperations) {
        this.teamMapper = teamMapper;
        this.participantRepository = participantRepository;
        this.redisRepository = redisRepository;
        this.participantEsRepository = participantEsRepository;
        this.esOperations = esOperations;
    }

    public TeamMemberListResponse searchParticipants(Long teamId, String name) {
        long start = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(0, PAGESIZE);
        List<QueryTeamMember> mentions = participantRepository.findMentionsByNamePrefix(name, teamId, pageable);

        long end = System.currentTimeMillis();

        log.info("검색 완료 - 팀 id: {} 찾은 인원: {}명, 소요 시간: {}ms", teamId, mentions.size(), end - start);

        //mentions가 null인 경우, emptyList를 반환하도록 설정
        return teamMapper.toTeamMemberListResponse(
                Objects.requireNonNullElse(mentions, Collections.emptyList())
        );
    }

    //redis 이용해서 조회
    public TeamMemberListResponse searchParticipantsWithRedis(Long teamId, String name){
        long start = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(0, PAGESIZE);
        //1. 캐시에 있는지 먼저 조회
        boolean hasCache = redisRepository.hasMentionCache(teamId);

        // cache miss인 경우 db를 조회하여 모든 값을 redis에 저장한다.
        if(!hasCache){
            missCount.incrementAndGet();
            List<QueryTeamMember> members = participantRepository.findParticipants(teamId);
            redisRepository.saveAllToZSet(teamId, members);
        }
        else{
            hitCount.incrementAndGet();
        }

        double totalRequests = hitCount.get() + missCount.get();
        double hitRatio = (hitCount.get() / totalRequests) * 100.0;

        // redis에서 값을 가져온다.
        List<QueryTeamMember> filtered = redisRepository.findMentionsByPrefixWithRedis(teamId, name, pageable);

        long end = System.currentTimeMillis();
        log.info("redis 검색 완료 - 팀 id: {}, 찾은 인원: {}명, 소요 시간: {}ms, hit ratio: {}%", teamId, filtered.size(), end - start, hitRatio);
        return teamMapper.toTeamMemberListResponse(
                Objects.requireNonNullElse(filtered, List.of())
        );
    }

    //elasticsearch 이용해서 조회
    public TeamMemberListResponse searchParticipantsWithEs(Long teamId, String name){
        long start = System.currentTimeMillis();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .filter(m -> m.term(t -> t.field("teamId").value(teamId)))
                        .must(m -> m.matchPhrasePrefix(p -> p
                                .field("nickname")
                                .query(name)
                        ))
                )).withPageable(PageRequest.of(0, PAGESIZE))
                .build();

        SearchHits<ParticipantDocument> searchHits = esOperations.search(query, ParticipantDocument.class);

        log.info("검색 쿼리 실행 결과: {} 건 발견", searchHits.getTotalHits());

        List<QueryTeamMember> filtered = searchHits.getSearchHits().stream()
                .map(hit ->
                        new QueryTeamMember(hit.getContent().getMemberId(),
                                hit.getContent().getNickname()))
                .toList();
        long end = System.currentTimeMillis();
        log.info("elasticsearch 검색 완료 - 팀 id: {}, 찾은 인원: {}명, 소요 시간: {}ms", teamId, filtered.size(), end - start);

        return teamMapper.toTeamMemberListResponse(
                Objects.requireNonNullElse(filtered, List.of())
        );
    }
}
