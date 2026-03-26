package com.yellobook.mention.domain.team.dto;

import java.util.List;

public record TeamMemberListResponse(
        List<QueryTeamMember> members
) {
}
