package ding.co.backendportfolio.chapter4._3_event_with_lock.facade;

import ding.co.backendportfolio.chapter4._3_event_with_lock.repository.EventWithLockRepository;
import ding.co.backendportfolio.chapter4._3_event_with_lock.service.EventWithLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NamedLockEventFacade {
    private final EventWithLockService eventWithLockService;
    private final EventWithLockRepository eventWithLockRepository;

    private static final long RETRY_DELAY_MS = 50;

    @Transactional
    public void joinEvent(Long eventId, Long memberId) throws InterruptedException {
        int retryCount = 0;
        String lockName = String.format("event_%d", eventId);

        while (true) {
            try {
                int lockResult = eventWithLockRepository.getLock(lockName, 3);
                if (lockResult <= 0) {
                    log.warn("락 획득 실패 - eventId: {}, memberId: {}", eventId, memberId);
                    Thread.sleep(RETRY_DELAY_MS);
                    continue;
                }

                eventWithLockService.joinEventWithNamedLock(eventId, memberId);
                log.info("이벤트 참가 성공 - eventId: {}, memberId: {}, 총 시도횟수: {}",
                        eventId, memberId, retryCount + 1);
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn("이벤트 참가 재시도 - eventId: {}, memberId: {}, 현재 시도횟수: {}, error: {}",
                        eventId, memberId, retryCount, e.getMessage());
                Thread.sleep(RETRY_DELAY_MS);
            } finally {
                int releaseResult = eventWithLockRepository.releaseLock(lockName);
                if (releaseResult <= 0) {
                    log.error("락 해제 실패 - eventId: {}, memberId: {}", eventId, memberId);
                }
            }
        }
    }


    /**
     * while(true) 무한루프 문제를 해결한 개선된 버전
     * - 최대 재시도 횟수를 제한하여 무한루프 방지
     * - 락 획득 실패와 비즈니스 로직 실패를 모두 카운트
     * - 프로덕션 환경에서 안전하게 사용 가능
     */
    @Transactional
    public void improvedJoinEvent(Long eventId, Long memberId) throws InterruptedException {
        String lockName = String.format("event_%d", eventId);
        int maxRetries = 30; // 최대 재시도 횟수 제한

        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            try {
                int lockResult = eventWithLockRepository.getLock(lockName, 3);
                if (lockResult <= 0) {
                    log.warn("락 획득 실패 - eventId: {}, memberId: {}, 시도횟수: {}/{}",
                            eventId, memberId, retryCount + 1, maxRetries);

                    if (retryCount == maxRetries - 1) {
                        throw new RuntimeException("락 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
                    }

                    Thread.sleep(RETRY_DELAY_MS);
                    continue;
                }

                eventWithLockService.joinEventWithNamedLock(eventId, memberId);
                log.info("이벤트 참가 성공 - eventId: {}, memberId: {}, 총 시도횟수: {}",
                        eventId, memberId, retryCount + 1);
                return;
            } catch (Exception e) {
                if (retryCount == maxRetries - 1) {
                    log.error("이벤트 참가 실패 - eventId: {}, memberId: {}, 최대 재시도 횟수({}) 초과",
                            eventId, memberId, maxRetries);
                    throw new RuntimeException("이벤트 참가에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
                }

                log.warn("이벤트 참가 재시도 - eventId: {}, memberId: {}, 현재 시도횟수: {}/{}, error: {}",
                        eventId, memberId, retryCount + 1, maxRetries, e.getMessage());
                Thread.sleep(RETRY_DELAY_MS);
            } finally {
                int releaseResult = eventWithLockRepository.releaseLock(lockName);
                if (releaseResult <= 0) {
                    log.error("락 해제 실패 - eventId: {}, memberId: {}", eventId, memberId);
                }
            }
        }
    }
}