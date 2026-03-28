package com.yellobook.mention.domain.team.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.yellobook.mention.domain.team.document.ParticipantDocument;

@Repository
public interface ParticipantEsRepository extends ElasticsearchRepository<ParticipantDocument, String> {
    List<ParticipantDocument> findByTeamIdAndNickname(Long teamId, String nickname);
}
