package com.yellobook.mention.domain.team.repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.yellobook.mention.domain.team.dto.QueryTeamMember;

@Repository
public class TeamRedisRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String MENTION_PREFIX = "team:%d:mentions";
    private static final String DELIMITER = ":";
    private static final String LAST_UNICODE = "\uFFFF";

    public TeamRedisRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 1. ZSET에 key가 존재하는지 확인(Cache Miss)
    public boolean hasMentionCache(Long teamId) {
        String key = String.format(MENTION_PREFIX, teamId);
        return stringRedisTemplate.hasKey(key);
    }
    // 2. DB에서 가져온 n명의 인물들을 ZSET에 한 번에 밀어넣기
public void saveAllToZSet(Long teamId, List<QueryTeamMember> members){
        String key = String.format(MENTION_PREFIX, teamId);
        ZSetOperations<String, String> zSetOps = stringRedisTemplate.opsForZSet();

        Set<TypedTuple<String>> tuples = members.stream()
                .map(member -> {
                   String value = member.nickname() + DELIMITER + member.memberId();
                   return (TypedTuple<String>) new DefaultTypedTuple<>(value, 0.0);
                })
                .collect(Collectors.toSet());

       if(!tuples.isEmpty()){
           zSetOps.add(key, tuples);
       }

        stringRedisTemplate.expire(key, Duration.ofHours(1));
    }

    // 3. 접두사로 사전순 검색
    public List<QueryTeamMember> findMentionsByPrefixWithRedis(Long teamId, String prefix){
        String key = String.format(MENTION_PREFIX, teamId);
        ZSetOperations<String, String> zSetOps = stringRedisTemplate.opsForZSet();

        Range<String> range = Range.closed(prefix, prefix + LAST_UNICODE);
        Set<String> result = zSetOps.rangeByLex(key, range);

        //값이 없는 경우 빈 리스트 반환
        if(result == null || result.isEmpty()){
            return List.of();
        }
        return result.stream()
                .map(value -> {
                    int lastColon = value.lastIndexOf(DELIMITER);
                    String name = value.substring(0, lastColon);
                    Long memberId = Long.parseLong(value.substring(lastColon + 1));

                    return new QueryTeamMember(memberId, name);
                }).toList();
    }
}
