package ding.co.backendportfolio.chapter4._3_event_with_lock.facade;

import ding.co.backendportfolio.chapter4._3_event_with_lock.service.EventWithLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptimisticLockEventFacade {
    private final EventWithLockService eventWithLockService;

    private static final long RETRY_DELAY_MS = 50;

    public void joinEvent(Long eventId, Long memberId) throws InterruptedException {
        int retryCount = 0;

        //서버에서는 무한반복 시도인 while (true) 상당히 위험 하므로 실제 서비스 코드에는 존재 해서는 안됨.
        //이유는 이즈케이프 코드가 제대로 구현 안되어 있다면 서버에 엄청난 리소스 또는 행이 걸리는 문제가 생김
        //학습용도이기때문에 while (true) 구문 사용
        while (true) {
            try {
                eventWithLockService.joinEventOptimistic(eventId, memberId);
                log.info("이벤트 참가 성공 - eventId: {}, memberId: {}, 총 시도횟수: {}",
                        eventId, memberId, retryCount + 1);
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn("이벤트 참가 재시도 - eventId: {}, memberId: {}, 현재 시도횟수: {}, error: {}",
                        eventId, memberId, retryCount, e.getMessage());
                Thread.sleep(RETRY_DELAY_MS);
            }
        }
    }

    /**
     * while(true) 무한루프 문제를 해결한 개선된 버전
     * - 최대 재시도 횟수를 제한하여 무한루프 방지
     * - 프로덕션 환경에서 안전하게 사용 가능
     */
    public void improvedJoinEvent(Long eventId, Long memberId) throws InterruptedException {
        int maxRetries = 30; // 최대 재시도 횟수 제한

        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            try {
                eventWithLockService.joinEventOptimistic(eventId, memberId);
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
            }
        }
    }
} 