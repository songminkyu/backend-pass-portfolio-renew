package ding.co.backendportfolio.chapter4._2_event_with_participant.service;

import ding.co.backendportfolio.chapter2.entity.Member;
import ding.co.backendportfolio.chapter2.repository.MemberRepository;
import ding.co.backendportfolio.chapter4._1_event.entity.Event;
import ding.co.backendportfolio.chapter4._1_event.repository.EventRepository;
import ding.co.backendportfolio.chapter4._2_event_with_participant.entity.EventParticipant;
import ding.co.backendportfolio.chapter4._2_event_with_participant.repository.EventParticipantRepository;
import ding.co.backendportfolio.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EventJoinService {
    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
/*
    1. EventParticipant insert (S Lock)
    2. Event update (X Lock)
    -> Dead lock 발생

    1. Event update (X Lock)
    2. EventParticipant insert (S Lock)
    -> Dead lock 발생 없음
*/
    @Transactional
    public void joinEvent(Long eventId, Long memberId) {
        // 1. 이벤트와 회원 정보 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 2. 이벤트 참가자 수 증가 (내부적으로 참가 가능 여부 검증)
        event.increaseParticipants();
        eventRepository.save(event);

        // 3. 참가자 정보 저장
        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .member(member)
                .build();

        participantRepository.save(participant);
    }

    //동시성 문제는 해결 안됫지만, 트랜잭션간 데드락 문제는 해결
    @Transactional
    public void improveJoinEvent(Long eventId, Long memberId){
        // 1. 이벤트와 회원 정보 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 2. 이벤트 참가자 수 증가 (내부적으로 참가 가능 여부 검증)
        // 일반적인 save()는 쓰기 지연(Write-Behind)로 인해 insertt가 먼저 실행되므로
        // saveAndFlush()를 사용하면, 해당  시점에서 즉시 Flush가 발생하여, updatte가
        // 먼저 실행 되도록 강제하여, 데드락을 방지 할 수 있다.
        // 결과적으로 트랜잭션 내에서 update -> insert 순서가 유지되어 데드락이 방지된다.
        event.increaseParticipants();
        eventRepository.saveAndFlush(event);

        // 3. 참가자 정보 저장
        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .member(member)
                .build();

        participantRepository.save(participant);
    }
} 