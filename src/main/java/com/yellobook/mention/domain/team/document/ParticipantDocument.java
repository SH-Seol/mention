package com.yellobook.mention.domain.team.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "participants")
public class ParticipantDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long teamId;

    @Field(type = FieldType.Long)
    private Long memberId;

    @Field(type = FieldType.Long)
    private Long participantId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String nickname;

    public ParticipantDocument(Long teamId, Long memberId, Long participantId, String nickname) {
        this.teamId = teamId;
        this.memberId = memberId;
        this.participantId = participantId;
        this.nickname = nickname;
    }

    public String getId() {
        return id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public String getNickname() {
        return nickname;
    }
}
