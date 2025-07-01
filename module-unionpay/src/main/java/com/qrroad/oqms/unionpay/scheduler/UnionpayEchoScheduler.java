package com.qrroad.oqms.unionpay.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qrroad.oqms.unionpay.dto.iso8583.ISO8583Dto;
import com.qrroad.oqms.unionpay.dto.iso8583.ISO8583Header;
import com.qrroad.oqms.unionpay.service.UnionpayConnectionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * UnionPay Echo Message Scheduler
 * 
 * ISO8583 0800/0810 Network Management 메시지를 통한 연결 상태 확인
 * - 5분 간격으로 Echo 메시지 전송
 * - 연결 상태 모니터링 및 장애 감지
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "unionpay.echo.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class UnionpayEchoScheduler {

    private final UnionpayConnectionService connectionService;
    
    // STAN(System Trace Audit Number) 생성을 위한 카운터
    private final AtomicLong stanCounter = new AtomicLong(1);
    
    // Echo 메시지 실패 카운터
    private final AtomicLong failureCount = new AtomicLong(0);
    
    // 최대 재시도 횟수
    private static final long MAX_FAILURE_COUNT = 3;
    
    // 네트워크 관리 정보 코드 - Echo Test
    private static final String ECHO_TEST_CODE = "001";

    /**
     * 5분마다 Echo 메시지 전송
     * cron: 0초 0분 5분마다 실행
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void sendEchoMessage() {
        try {
            log.debug("Starting echo message transmission");
            
            // 연결 상태 확인
            if (!connectionService.isConnected()) {
                log.warn("UnionPay connection is not active. Skipping echo message.");
                return;
            }
            
            // Echo 요청 메시지 생성
            ISO8583Dto echoRequest = createEchoRequestMessage();
            
            // 메시지 전송 및 응답 대기
            ISO8583Dto echoResponse = connectionService.sendMessage(echoRequest);
            
            // 응답 검증
            if (validateEchoResponse(echoResponse)) {
                log.info("Echo message sent successfully. STAN: {}", echoRequest.stan());
                resetFailureCount();
            } else {
                handleEchoFailure("Invalid echo response received");
            }
            
        } catch (Exception e) {
            handleEchoFailure("Echo message transmission failed: " + e.getMessage());
        }
    }

    /**
     * Echo 요청 메시지 생성 (MTI: 0800)
     */
    private ISO8583Dto createEchoRequestMessage() {        // 헤더 설정
        ISO8583Header header = ISO8583Header.builder().forEchoMessage("DEST_ID", "SRC_ID").build();
        
        // 시간 정보 생성
        LocalDateTime now = LocalDateTime.now();
        String transmissionDateTime = now.format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        String localTime = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        String localDate = now.format(DateTimeFormatter.ofPattern("MMdd"));
        
        ISO8583Dto echoMessage = ISO8583Dto.builder()
        .header(header)
        .echoRequest()
        .trDateTime(transmissionDateTime)
        .stan(generateStan())
        .localTime(localTime)
        .localDate(localDate)
        .build();
        
        log.debug("Echo request message created: MTI={}, STAN={}, F70={}", 
                 echoMessage.mti(), echoMessage.stan(), echoMessage.networkInfoCode());

        return echoMessage;
    }

    /**
     * Echo 응답 메시지 검증 (MTI: 0810)
     */
    private boolean validateEchoResponse(ISO8583Dto response) {
        if (response == null) {
            log.error("Echo response is null");
            return false;
        }
        
        // MTI 검증 - Network Management Response
        if (!"0810".equals(response.mti())) {
            log.error("Invalid response MTI: expected=0810, actual={}", response.mti());
            return false;
        }
        
        // Response Code 검증 (F39)
        String responseCode = response.respCode();
        if (!"00".equals(responseCode)) {
            log.error("Echo failed with response code: {}", responseCode);
            return false;
        }
        
        // Network Management Information Code 검증 (F70)
        if (!ECHO_TEST_CODE.equals(response.networkInfoCode())) {
            log.error("Invalid network info code in response: expected={}, actual={}", 
                     ECHO_TEST_CODE, response.networkInfoCode());
            return false;
        }
        
        return true;
    }

    /**
     * STAN(System Trace Audit Number) 생성
     * 6자리 숫자, 1-999999 범위에서 순환
     */
    private String generateStan() {
        long stan = stanCounter.getAndIncrement();
        if (stan > 999999) {
            stanCounter.set(1);
            stan = 1;
        }
        return String.format("%06d", stan);
    }

    /**
     * Echo 실패 처리
     */
    private void handleEchoFailure(String errorMessage) {
        long currentFailures = failureCount.incrementAndGet();
        log.error("Echo message failure #{}: {}", currentFailures, errorMessage);
        
        if (currentFailures >= MAX_FAILURE_COUNT) {
            log.error("Echo failures exceeded maximum count ({}). Requesting connection reset.", 
                     MAX_FAILURE_COUNT);
            connectionService.requestConnectionReset();
            resetFailureCount();
        }
    }

    /**
     * 실패 카운터 리셋
     */
    private void resetFailureCount() {
        if (failureCount.get() > 0) {
            failureCount.set(0);
            log.info("Echo failure count reset to 0");
        }
    }

    /**
     * 스케줄러 상태 조회 (모니터링용)
     */
    public EchoSchedulerStatus getSchedulerStatus() {
        return EchoSchedulerStatus.builder()
                .isEnabled(true)
                .currentStan(stanCounter.get())
                .failureCount(failureCount.get())
                .maxFailureCount(MAX_FAILURE_COUNT)
                .lastEchoTime(LocalDateTime.now())
                .build();
    }

    /**
     * Echo 스케줄러 상태 정보
     */
    @lombok.Builder
    @lombok.Getter
    public static class EchoSchedulerStatus {
        private final boolean isEnabled;
        private final long currentStan;
        private final long failureCount;
        private final long maxFailureCount;
        private final LocalDateTime lastEchoTime;
    }
}