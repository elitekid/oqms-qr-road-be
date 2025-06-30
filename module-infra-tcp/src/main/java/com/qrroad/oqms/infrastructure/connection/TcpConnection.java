package com.qrroad.oqms.infrastructure.connection;

import com.qrroad.oqms.infrastructure.config.PackagerFactory;
import com.qrroad.oqms.infrastructure.config.TcpProperties;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

@Slf4j
@Component
public class TcpConnection {
    private final TcpProperties properties;
    private final PackagerFactory packagerFactory;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private volatile boolean connected = false;
    private LocalDateTime lastActivity;

    public TcpConnection(TcpProperties properties, PackagerFactory packagerFactory) {
        this.properties = properties;
        this.packagerFactory = packagerFactory;
        this.lastActivity = LocalDateTime.now();
        
        log.info("TcpConnection initialized with standard ISO8583 packager");
    }

    public synchronized void connect(String appName) throws IOException {
        if (connected) {
            return;
        }

        TcpProperties.TcpAppConfig appConfig = properties.getApps().get(appName);
        if (appConfig == null) {
            // Use default configuration if app-specific config not found
            appConfig = new TcpProperties.TcpAppConfig();
            appConfig.setHost(properties.getDefaultHost());
            appConfig.setPort(properties.getDefaultPort());
            appConfig.setConnectTimeoutMs(properties.getConnectTimeoutMs());
            appConfig.setReadTimeoutMs(properties.getReadTimeoutMs());
            appConfig.setKeepAlive(properties.isKeepAlive());
            appConfig.setMessageHeaderLength(properties.getMessageHeaderLength());
        }
        
        try {
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(appConfig.getHost(), appConfig.getPort()),
                          appConfig.getConnectTimeoutMs());
            socket.setSoTimeout(appConfig.getReadTimeoutMs());
            socket.setKeepAlive(appConfig.isKeepAlive());
            
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            connected = true;
            lastActivity = LocalDateTime.now();
            
            log.info("TCP connection established to {}:{}", appConfig.getHost(), appConfig.getPort());
        } catch (IOException e) {
            log.error("Failed to connect to {}:{}", appConfig.getHost(), appConfig.getPort(), e);
            disconnect();
            throw e;
        }
    }

    public synchronized void disconnect() {
        connected = false;
        closeQuietly(inputStream);
        closeQuietly(outputStream);
        closeQuietly(socket);
        log.info("TCP connection closed");
    }

    public ISOMsg sendAndReceive(ISOMsg requestMsg) throws Exception {
        if (!connected) {
            throw new IllegalStateException("Connection is not established");
        }

        try {
            sendMessage(requestMsg);
            return receiveMessage();
        } catch (Exception e) {
            log.error("Error during message exchange", e);
            disconnect();
            throw e;
        }
    }

    private void sendMessage(ISOMsg msg) throws Exception {
        byte[] msgBytes = msg.pack();
        byte[] lengthHeader = createLengthHeader(msgBytes.length);
        
        outputStream.write(lengthHeader);
        outputStream.write(msgBytes);
        outputStream.flush();
        
        lastActivity = LocalDateTime.now();
        log.debug("Sent ISO8583 message: MTI={}, STAN={}",
                 msg.getMTI(), msg.getString(11));
    }

    private ISOMsg receiveMessage() throws Exception {
        // 길이 헤더 읽기
        byte[] lengthHeader = new byte[properties.getMessageHeaderLength()];
        readFully(lengthHeader);
        int messageLength = parseMessageLength(lengthHeader);
        
        // 메시지 본문 읽기
        byte[] messageBytes = new byte[messageLength];
        readFully(messageBytes);
        
        // ISO8583 메시지 언팩
        ISOMsg msg = new ISOMsg();
        msg.setPackager(packagerFactory.getStandardPackager());
        msg.unpack(messageBytes);
        
        lastActivity = LocalDateTime.now();
        log.debug("Received ISO8583 message: MTI={}, STAN={}",
                 msg.getMTI(), msg.getString(11));
        
        return msg;
    }

    private byte[] createLengthHeader(int length) {
        if (properties.getMessageHeaderLength() == 2) {
            return new byte[]{(byte) (length >> 8), (byte) length};
        } else {
            return new byte[]{
                (byte) (length >> 24),
                (byte) (length >> 16),
                (byte) (length >> 8),
                (byte) length
            };
        }
    }

    private int parseMessageLength(byte[] header) {
        if (header.length == 2) {
            return ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);
        } else {
            return ((header[0] & 0xFF) << 24) |
                   ((header[1] & 0xFF) << 16) |
                   ((header[2] & 0xFF) << 8) |
                   (header[3] & 0xFF);
        }
    }

    private void readFully(byte[] buffer) throws IOException {
        int totalRead = 0;
        while (totalRead < buffer.length) {
            int read = inputStream.read(buffer, totalRead, buffer.length - totalRead);
            if (read == -1) {
                throw new IOException("Unexpected end of stream");
            }
            totalRead += read;
        }
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.warn("Error closing resource", e);
            }
        }
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
}