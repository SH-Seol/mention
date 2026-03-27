package com.yellobook.mention.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

@Component
public class DummyDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DummyDataInitializer.class);

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;

    public DummyDataInitializer(TeamRepository teamRepository,
                                MemberRepository memberRepository,
                                ParticipantRepository participantRepository) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.participantRepository = participantRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 이미 데이터가 있다면 실행하지 않음
        if (memberRepository.count() > 0) {
            log.info("이미 더미 데이터가 존재하여 초기화를 건너뜁니다.");
            return;
        }

        log.info("더미 데이터 삽입을 시작합니다...");
        long startTime = System.currentTimeMillis();

        // 1. 테스트용 팀 생성
        TeamEntity team = new TeamEntity(
                "mention 테스트팀",
                "멘션 기능 테스트를 위한 팀입니다.",
                "010-1234-5678",
                "경기도 안양시",
                true
        );
        teamRepository.save(team);

        // 2. 10000명의 랜덤 유저 및 팀 참여(Participant) 데이터 생성
        String[] lastNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "설", "고", "피", "남궁", "유"}; //20개
        String[] firstNames = {"민준", "서연", "도윤", "서윤", "시우", "지우", "지호", "하은",
                "지훈", "지아", "길동", "철수", "영희", "진호", "희찬", "지한", "주은", "주찬", "시온","소여", "지민"}; //21개
        Random random = new Random();

        List<MemberEntity> members = new ArrayList<>();
        List<ParticipantEntity> participants = new ArrayList<>();

        for (int i = 1; i <= 100000; i++) {
            String randomName = lastNames[random.nextInt(lastNames.length)] + firstNames[random.nextInt(firstNames.length)];
            // 이름 뒤에 숫자를 붙여 닉네임 중복 및 멘션 다양성 확보 (예: 김길동7, 이서연12)
            String nickname = randomName + random.nextInt(1000);
            String email = "test" + i + "@yellobook.com";

            MemberEntity member = new MemberEntity(
                    nickname,
                    "안녕하세요 " + nickname + "입니다.",
                    email,
                    null,
                    "oauth_" + i,
                    "KAKAO"
            );
            members.add(member);
        }

        // saveAll을 사용하여 Bulk Insert (JPA 성능 최적화)
        memberRepository.saveAll(members);

        // 3. 생성된 유저들을 방금 만든 팀에 전부 소속시킴
        for (MemberEntity member : members) {
            ParticipantEntity participant = new ParticipantEntity(
                    team,
                    member,
                    TeamMemberRole.ORDERER
            );
            participants.add(participant);
        }
        participantRepository.saveAll(participants);

        long endTime = System.currentTimeMillis();
        log.info("성공적으로 100,000명의 유저와 팀 매핑 데이터를 삽입했습니다. (소요 시간: {}ms)", (endTime - startTime));
    }
}
