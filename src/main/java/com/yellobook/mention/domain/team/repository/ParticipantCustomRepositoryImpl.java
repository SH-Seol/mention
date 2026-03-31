package com.yellobook.mention.domain.team.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yellobook.mention.domain.member.entity.QMemberEntity;
import com.yellobook.mention.domain.team.dto.QueryTeamMember;
import com.yellobook.mention.domain.team.entity.QParticipantEntity;

@Repository
public class ParticipantCustomRepositoryImpl implements ParticipantCustomRepository {
    private final JPAQueryFactory queryFactory;

    public ParticipantCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<QueryTeamMember> findMentionsByNamePrefix(String prefix, Long teamId, Pageable pageable) {
        QMemberEntity member = QMemberEntity.memberEntity;
        QParticipantEntity participant = QParticipantEntity.participantEntity;

        return queryFactory
                .select(Projections.constructor(QueryTeamMember.class,
                        member.id,
                        member.nickname
                ))
                .from(participant)
                .join(participant.member, member)
                .where(member.nickname.startsWith(prefix)
                        .and(participant.team.id.eq(teamId)))
                .orderBy(member.nickname.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<QueryTeamMember> findParticipants(Long teamId){
        QMemberEntity member = QMemberEntity.memberEntity;
        QParticipantEntity participant = QParticipantEntity.participantEntity;

        return queryFactory
                .select(
                        Projections.constructor(QueryTeamMember.class,
                                member.id,
                                member.nickname
                        )
                )
                .from(participant)
                .join(participant.member, member)
                .where(participant.team.id.eq(teamId))
                .fetch();
    }
}

