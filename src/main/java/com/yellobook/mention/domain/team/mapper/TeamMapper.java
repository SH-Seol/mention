package com.yellobook.mention.domain.team.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.yellobook.mention.domain.team.dto.QueryTeamMember;
import com.yellobook.mention.domain.team.dto.TeamMemberListResponse;

@Component
@Mapper(componentModel = "spring")
public interface TeamMapper {
    default TeamMemberListResponse toTeamMemberListResponse(List<QueryTeamMember> members) {
        return new TeamMemberListResponse(members);
    }
}
