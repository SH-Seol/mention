package com.yellobook.mention.domain.team.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yellobook.mention.domain.team.dto.TeamMemberListResponse;
import com.yellobook.mention.domain.team.service.TeamService;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/members/search")
    public ResponseEntity<TeamMemberListResponse> searchMembers(
            @RequestParam("name") String name,
            Long teamId
    ) {
        TeamMemberListResponse response = teamService.searchParticipants(teamId, name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members/redis/search")
    public ResponseEntity<TeamMemberListResponse> searchMembersWithRedis(
            @RequestParam("name") String name,
            Long teamId
    ){
        TeamMemberListResponse response = teamService.searchParticipantsWithRedis(teamId, name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members/es/search")
    public ResponseEntity<TeamMemberListResponse> searchMembersWithEs(
            @RequestParam("name") String name,
            Long teamId
    ){
        TeamMemberListResponse response = teamService.searchParticipantsWithEs(teamId, name);
        return ResponseEntity.ok(response);
    }
}
