package com.yellobook.mention.domain.team.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yellobook.mention.domain.team.document.ParticipantDocument;
import com.yellobook.mention.domain.team.entity.ParticipantEntity;
import com.yellobook.mention.domain.team.repository.ParticipantEsRepository;
import com.yellobook.mention.domain.team.repository.ParticipantRepository;

@Service
public class ParticipantSyncService {

    private final ParticipantRepository participantRepository;
    private final ParticipantEsRepository participantEsRepository;
    private final EntityManager entityManager;
    private static final Logger log = LoggerFactory.getLogger(ParticipantSyncService.class);

    public ParticipantSyncService(ParticipantRepository participantRepository,
                                  ParticipantEsRepository participantEsRepository,
                                  EntityManager entityManager) {
        this.participantRepository = participantRepository;
        this.participantEsRepository = participantEsRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public void sync() {
        log.info("🚀 Elasticsearch 초고속 스트림 동기화 시작...");
        long startTime = System.currentTimeMillis();

        // 1. 기존 데이터 초기화
        participantEsRepository.deleteAll();

        int batchSize = 1000;
        AtomicInteger count = new AtomicInteger(0); // 진행률 계산용 카운터
        List<ParticipantDocument> batch = new ArrayList<>();

        try (Stream<ParticipantEntity> participantStream = participantRepository.streamAllWithMemberAndTeam()) {

            participantStream.forEach(p -> {
                // Document로 변환
                batch.add(new ParticipantDocument(
                        p.getTeam().getId(),
                        p.getMember().getId(),
                        p.getId(),
                        p.getMember().getNickname()
                ));

                // 3. 설정한 batchSize(1,000개)가 쌓이면 ES에 전송
                if (batch.size() >= batchSize) {
                    saveBatchAndClear(batch);
                    log.info("동기화 진행 중: {}명 완료...", count.addAndGet(batchSize));
                    batch.clear(); // 리스트 비우기
                }
            });

            // 4. 마지막에 남은 찌꺼기 데이터 처리
            if (!batch.isEmpty()) {
                int lastSize = batch.size();
                saveBatchAndClear(batch);
                log.info("최종 동기화 완료: 총 {}명", count.addAndGet(lastSize));
            }

        } catch (Exception e) {
            log.error("동기화 중 오류 발생: ", e);
            throw e;
        }

        long endTime = System.currentTimeMillis();
        log.info("✅ 동기화 종료! 소요 시간: {}ms", endTime - startTime);
    }

    private void saveBatchAndClear(List<ParticipantDocument> batch) {
        participantEsRepository.saveAll(batch);
        entityManager.clear();
    }
}
