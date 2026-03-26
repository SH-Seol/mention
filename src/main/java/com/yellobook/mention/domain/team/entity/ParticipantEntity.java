package com.yellobook.mention.domain.team.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.yellobook.mention.common.BaseEntity;
import com.yellobook.mention.domain.enums.TeamMemberRole;
import com.yellobook.mention.domain.member.entity.MemberEntity;

@Entity
@Table(name = "participants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_participant", columnNames = {"team_id", "member_id"})
        }
)
public class ParticipantEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamMemberRole teamMemberRole;

    protected ParticipantEntity() {
    }

    public ParticipantEntity(TeamEntity team, MemberEntity member, TeamMemberRole teamMemberRole) {
        isValid(team, member, teamMemberRole);
        this.team = team;
        this.member = member;
        this.teamMemberRole = teamMemberRole;
    }

    private void isValid(TeamEntity team, MemberEntity member, TeamMemberRole role) {
        if (team == null || member == null || role == null) {
            throw new IllegalArgumentException("null값이 존재합니다.");
        }
    }
}

