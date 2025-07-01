package com.qrroad.oqms.unionpay.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qrroad.oqms.unionpay.dto.iso8583.ISO8583Dto;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * UnionPay 연결 관리 서비스
 * 
 * TCP/IP 소켓 연결을 통한 ISO8583 메시지 송수신
 * - 연결 상태 관리
 * - 메시지 송수신
 * - 연결 복구 메커니즘
 * 
 * @author escapetree82
 * @version 1.0
 * @since 2025.07.01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnionpayConnectionService {

    @Value("${unionpay.host:localhost}")
    private String unionPayHost;
    
    @Value("${unionpay.port:8080}")
    private int unionPayPort;
    
    @Value("${unionpay.timeout:30000}")
    private int timeoutMs;
    
    @Value("${unionpay.reconnect.enabled:true}")
    private boolean reconnectEnabled;
    
    @Value("${unionpay.reconnect.interval:5000}")
    private int reconnectIntervalMs;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private volatile boolean connected = false;
    private final ReentrantLock connectionLock = new ReentrantLock();

    /**
     * 서비스 초기화 - 연결 설정
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing UnionPay connection service: {}:{}", unionPayHost, unionPayPort);
        connectToUnionPay();
    }

    /**
     * 서비스 종료 - 연결 해제
     */
    @PreDestroy
    public void destroy() {
        log.info("Shutting down UnionPay connection service");
        disconnect();
    }

    /**
     * UnionPay 서버 연결
     */
    private void connectToUnionPay() {
        connectionLock.lock();
        try {
            if (connected) {
                log.warn("Already connected to UnionPay");
                return;
            }

            log.info("Connecting to UnionPay: {}:{}", unionPayHost, unionPayPort);
            
            socket = new Socket(unionPayHost, unionPayPort);
            socket.setSoTimeout(timeoutMs);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            
            connected = true;
            log.info("Successfully connected to UnionPay: {}:{}", unionPayHost, unionPayPort);
            
        } catch (Exception e) {
            log.error("Failed to connect to UnionPay: {}:{}", unionPayHost, unionPayPort, e);
            connected = false;
            
            if (reconnectEnabled) {
                scheduleReconnect();
            }
        } finally {
            connectionLock.unlock();
        }
    }

    /**
     * 연결 해제
     */
    private void disconnect() {
        connectionLock.lock();
        try {
            connected = false;
            
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Error closing input stream", e);
                }
                inputStream = null;
            }
            
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.warn("Error closing output stream", e);
                }
                outputStream = null;
            }
            
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.warn("Error closing socket", e);
                }
                socket = null;
            }
            
            log.info("Disconnected from UnionPay");
            
        } finally {
            connectionLock.unlock();
        }
    }

    /**
     * 재연결 스케줄링
     */
    private void scheduleReconnect() {
        CompletableFuture.delayedExecutor(reconnectIntervalMs, TimeUnit.MILLISECONDS)
            .execute(() -> {
                if (!connected) {
                    log.info("Attempting to reconnect to UnionPay...");
                    connectToUnionPay();
                }
            });
    }

    /**
     * 연결 상태 확인
     */
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * ISO8583 메시지 전송 및 응답 수신
     */
    public ISO8583Dto sendMessage(ISO8583Dto request) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to UnionPay");
        }

        connectionLock.lock();
        try {
            // 메시지 전송
            sendISOMessage(request);
            log.debug("Message sent: MTI={}, STAN={}", request.mti(), request.stan());
            
            // 응답 수신
            ISO8583Dto response = receiveISOMessage();
            log.debug("Response received: MTI={}, STAN={}", response.mti(), response.stan());
            
            return response;
            
        } catch (SocketException e) {
            log.error("Socket error during message transmission", e);
            handleConnectionFailure();
            throw e;
        }finally {
            connectionLock.unlock();
        }
    }

    /**
     * ISO8583 메시지 전송
     */
    private void sendISOMessage(ISO8583Dto message) throws IOException {
        // ISO8583 메시지를 바이트 배열로 변환
        byte[] messageBytes = convertToBytes(message);
        
        // 길이 헤더 생성 (2바이트 또는 4바이트, UnionPay 스펙에 따라)
        byte[] lengthHeader = createLengthHeader(messageBytes.length);
        
        // 전송
        outputStream.write(lengthHeader);
        outputStream.write(messageBytes);
        outputStream.flush();
    }

    /**
     * ISO8583 메시지 수신
     */
    private ISO8583Dto receiveISOMessage() throws IOException {
        // 길이 헤더 읽기
        byte[] lengthHeader = new byte[4]; // 4바이트 길이 헤더 가정
        inputStream.readFully(lengthHeader);
        
        int messageLength = bytesToInt(lengthHeader);
        
        // 메시지 본문 읽기
        byte[] messageBytes = new byte[messageLength];
        inputStream.readFully(messageBytes);
        
        // 바이트 배열을 ISO8583Dto로 변환
        return convertToISO8583Dto(messageBytes);
    }

    /**
     * ISO8583Dto를 바이트 배열로 변환
     */
    private byte[] convertToBytes(ISO8583Dto message) {
        // 실제 구현에서는 ISO8583 패커저를 사용하여 변환
        // 여기서는 간단한 예시로 toString() 결과를 바이트로 변환
        return message.toString().getBytes();
    }

    /**
     * 바이트 배열을 ISO8583Dto로 변환
     */
    private ISO8583Dto convertToISO8583Dto(byte[] messageBytes) {
        // 실제 구현에서는 ISO8583 패커저를 사용하여 파싱
        // 여기서는 간단한 예시
        ISO8583Dto response = ISO8583Dto.builder()
            .mti("0810")
            .respCode("00")
            .networkInfoCode("001")
            .build();
        
        return response;
    }

    /**
     * 길이 헤더 생성
     */
    private byte[] createLengthHeader(int length) {
        // 4바이트 빅엔디안 길이 헤더
        return new byte[] {
            (byte) (length >>> 24),
            (byte) (length >>> 16),
            (byte) (length >>> 8),
            (byte) length
        };
    }

    /**
     * 바이트 배열을 정수로 변환
     */
    private int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }

    /**
     * 연결 실패 처리
     */
    private void handleConnectionFailure() {
        log.warn("Connection failure detected. Disconnecting and scheduling reconnect.");
        disconnect();
        
        if (reconnectEnabled) {
            scheduleReconnect();
        }
    }

    /**
     * 연결 재설정 요청 (Echo 스케줄러에서 호출)
     */
    public void requestConnectionReset() {
        log.info("Connection reset requested");
        disconnect();
        connectToUnionPay();
    }
}