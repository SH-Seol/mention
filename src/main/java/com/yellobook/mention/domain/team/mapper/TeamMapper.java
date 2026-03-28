package com.yellobook.mention.domain.team.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.yellobook.mention.domain.team.dto.QueryTeamMember;
import com.yellobook.mention.domain.team.dto.TeamMemberListResponse;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    default TeamMemberListResponse toTeamMemberListResponse(List<QueryTeamMember> members) {
        return new TeamMemberListResponse(members);
    }
}
