package com.yellobook.mention.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jakarta.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yellobook.mention.domain.enums.TeamMemberRole;
import com.yellobook.mention.domain.member.entity.MemberEntity;
import com.yellobook.mention.domain.member.repository.MemberRepository;
import com.yellobook.mention.domain.team.entity.ParticipantEntity;
import com.yellobook.mention.domain.team.entity.TeamEntity;
import com.yellobook.mention.domain.team.repository.ParticipantRepository;
import com.yellobook.mention.domain.team.repository.TeamRepository;
import com.yellobook.mention.domain.team.service.ParticipantSyncService;

@Component
public class DummyDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DummyDataInitializer.class);

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantSyncService participantSyncService;
    private final EntityManager entityManager;

    public DummyDataInitializer(TeamRepository teamRepository,
                                MemberRepository memberRepository,
                                ParticipantRepository participantRepository,
                                ParticipantSyncService participantSyncService,
                                EntityManager entityManager) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.participantRepository = participantRepository;
        this.participantSyncService = participantSyncService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 이미 데이터가 있다면 실행하지 않음 (팀 기준으로 체크)
        if (teamRepository.count() > 0) {
            log.info("이미 더미 데이터가 존재하여 초기화를 건너뜁니다. (다시 세팅하려면 DB를 비워주세요!)");
            participantSyncService.sync();
            return;
        }

        log.info("🔥 지옥의 트래픽 시뮬레이션을 위한 '데이터 쏠림(Mega Team)' 더미 삽입을 시작합니다...");
        long startTime = System.currentTimeMillis();

        // 1. 1,000개의 팀 생성 (메모리상에 리스트로 보관)
        List<TeamEntity> teams = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            TeamEntity team = new TeamEntity(
                    "mention 테스트팀 " + i,
                    i + "번 멘션 기능 테스트 팀입니다.",
                    "010-1234-5678",
                    "경기도 안양시",
                    true
            );
            teams.add(team);
            entityManager.flush();
            entityManager.clear();
        }
        // DB에 1,000개 팀 먼저 일괄 저장
        teamRepository.saveAll(teams);

        // 2. 이름 풀 세팅
        String[] lastNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오",
                "서", "신", "권", "설", "고", "피", "남궁", "유", "홍", "길"};
        String[] firstNames = {"민준", "서연", "도윤", "서윤", "시우", "지우", "지호", "하은", "지훈", "지아", "길동",
                "철수", "영희", "진호", "희찬", "지한", "주은", "주찬", "시온","소여", "지민", "형준", "동현", "지현", "지연", "윤지"};
        Random random = new Random();

        List<MemberEntity> members = new ArrayList<>();
        List<ParticipantEntity> participants = new ArrayList<>();
        int batchSize = 10000;

        int globalMemberId = 1;
        int totalInserted = 0;

        // 3. 데이터 쏠림(Data Skew) 매핑: 상위 10개 팀은 100,000명, 나머지는 10명
        for (int i = 0; i < teams.size(); i++) {
            TeamEntity savedTeam = teams.get(i);

            // 상위 10개 팀은 각 100,000명, 나머지는 10명씩
            int memberCountForThisTeam = (i < 10) ? 100000 : 10;

            for (int m = 1; m <= memberCountForThisTeam; m++) {
                String nickname = lastNames[random.nextInt(lastNames.length)]
                        + firstNames[random.nextInt(firstNames.length)]
                        + random.nextInt(1000000);

                MemberEntity member = new MemberEntity(
                        nickname,
                        "안녕하세요 " + nickname + "입니다.",
                        "test" + globalMemberId + "@yellobook.com",
                        null,
                        "oauth_" + globalMemberId,
                        "KAKAO"
                );
                members.add(member);

                participants.add(new ParticipantEntity(
                        savedTeam,
                        member,
                        TeamMemberRole.ORDERER
                ));

                globalMemberId++;
                totalInserted++;

                // 배치 저장 및 메모리 비우기 (중요!)
                if (members.size() >= batchSize) {
                    saveAndFlush(members, participants);
                    log.info("... 현재 {}만 건 삽입 완료 ...", totalInserted / 10000);
                }
            }
        }

        // 4. 마지막 루프를 돌고 남아있는 찌꺼기 데이터들 최종 저장
        if (!members.isEmpty()) {
            saveAndFlush(members, participants);
        }

        long endTime = System.currentTimeMillis();
        log.info("🔥 대성공! 1,000개의 팀과 총 {}명의 극단적 쏠림 데이터를 삽입했습니다. (소요 시간: {}ms)", totalInserted, (endTime - startTime));

        log.info("이어서 Elasticsearch로의 데이터 동기화를 시작합니다...");
        participantSyncService.sync();
    }

    private void saveAndFlush(List<MemberEntity> members, List<ParticipantEntity> participants) {
        memberRepository.saveAll(members);
        participantRepository.saveAll(participants);
        entityManager.flush(); // DB에 즉시 반영
        entityManager.clear(); // 영속성 컨텍스트 비우기 (OOM 방지)
        members.clear();
        participants.clear();
    }
}