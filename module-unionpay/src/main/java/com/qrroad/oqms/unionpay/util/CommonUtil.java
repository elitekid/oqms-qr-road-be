package com.qrroad.oqms.unionpay.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.qrroad.oqms.unionpay.dto.MsgInfo;
import com.qrroad.oqms.unionpay.enums.CommonField;
import com.qrroad.oqms.unionpay.enums.MessageId;
import com.qrroad.oqms.unionpay.enums.MessageType;
import io.github.aochoae.checkdigit.LuhnCheckDigit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CommonUtil {
    public static String key;

    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static final int MAX_DECRYPT_BLOCK = 117;
    private static AtomicInteger index = new AtomicInteger(1);


    //현재시간 (ex : 20241007163130001)
    public static String getCurrentDateTimeMillis() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    //현재시간 (ex : 20241007163130)
    public static String getCurrentDateTimes() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
    //현재시간 (ex : 20241007)
    public static String getCurrentDate() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    public static String getCurrentDate(String format ) {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern(format));
    }

    //현재시간 (ex : 173301)
    public static String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("HHmmss"));
    }

    //전일자 (ex : 20241006)
    public static String getYesterdayDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    //서명 생성
    public static String signUmps(String qbSigPrivateKey) throws Exception {
        // 데이터 생성
        String str = "00000000";

        // 1. SHA-256 해시 생성
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
        byte[] hashData = messageDigest.digest();

        // 2. Base64로 인코딩된 비공개 키를 디코딩
        ClassPathResource resource = new ClassPathResource(qbSigPrivateKey);

        byte[] keyBytes;
        try (InputStream inputStream = resource.getInputStream()) {
            String key = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            keyBytes = Base64.getDecoder().decode(key);
        }

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);

        // 3. RSA 비공개 키 생성
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);

        // 4. RSA 알고리즘을 사용하여 서명 생성
        Signature signature = Signature.getInstance("NONEWithRSA");
        signature.initSign(privateK);
        signature.update(hashData);
        byte[] sign = signature.sign();

        // 6. 서명을 Base64로 인코딩하여 반환
        return Base64.getEncoder().encodeToString(sign);
    }

    //서명 생성
    public static String signUmps(byte[] data, String qbSigPrivateKey) {
        try {
            // 1. SHA-256 해시 생성
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            byte[] hashData = messageDigest.digest();

            // 2. Base64로 인코딩된 비공개 키 문자열 처리
            String key = qbSigPrivateKey
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", ""); // 키 내 공백 제거
            byte[] keyBytes = Base64.getDecoder().decode(key);

            // 3. RSA 비공개 키 생성
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

            // 4. RSA 서명 생성
            Signature signature = Signature.getInstance("NONEWithRSA"); // 표준 알고리즘
            signature.initSign(privateKey);
            signature.update(hashData);
            byte[] signedData = signature.sign();

            // 5. 서명을 Base64로 인코딩하여 반환
            return Base64.getEncoder().encodeToString(signedData);
        } catch (Exception e) {
            throw new RuntimeException("Signing failed: " + e.getMessage(), e);
        }
    }

    // 바이트 배열을 16진수 문자열로 변환하는 메서드
    public static String byte2hex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex +" ");
        }
        return hexString.toString();
    }

    //서명 검증
    public static boolean verifyUmps(String publicKey, String sign) throws  Exception {

        String str = "00000000";
        byte[] data = new byte[8];  // 문자열 길이만큼 배열 크기 설정
        byte[] strBytes = str.getBytes();
        System.arraycopy(strBytes, 0, data, 0, strBytes.length);

        //데이터의 해시 계산 (SHA-256)
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(data);
        byte[] hashData = messageDigest.digest();

        //공개 키를 X.509 형식으로 디코딩
        byte[] keyBytes = Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicK = keyFactory.generatePublic(keySpec);

        //서명 객체 생성 및 검증 준비
        Signature signature = Signature.getInstance("NONEWithRSA");
        signature.initVerify(publicK);
        signature.update(hashData);

        return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
    }

    //서명 검증
    public static boolean verifyUmps(byte[] data, String publicKey, String sign) {
        try {
            log.info("verifySign Start");
            log.info("sign : {}", sign);
            log.info("publicKey : {}", publicKey);
            //데이터의 해시 계산 (SHA-256)
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            byte[] hashData = messageDigest.digest();

            //공개 키를 X.509 형식으로 디코딩
            byte[] keyBytes = Base64.getDecoder().decode(publicKey.getBytes());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicK = keyFactory.generatePublic(keySpec);

            //서명 객체 생성 및 검증 준비
            Signature signature = Signature.getInstance("NONEWithRSA");
            signature.initVerify(publicK);
            signature.update(hashData);
            return signature.verify(Base64.getDecoder().decode(sign.getBytes()));

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    //서명 검증
    public static void verifyUmps(Object obj, String publicKey) {
        String sign = "";
        try {
            // signature 추출 및 검증 준비
            Class<?> clazz = obj.getClass();
            Field certificateSignature = clazz.getDeclaredField("certificateSignature");
            certificateSignature.setAccessible(true);
            Object certification = certificateSignature.get(obj);

            if (certification != null) {
                Field signatureField = certification.getClass().getDeclaredField("signature");
                signatureField.setAccessible(true);
                sign = (String) signatureField.get(certification);
                signatureField.set(certification, "00000000");
            }

            byte[] data = Objects.requireNonNull(CommonUtil.convertToJsonWithoutNullsAndEmptyStrings(obj)).getBytes();

            log.info("verifySign Start");
            log.info("sign : {}", sign);
            log.info("publicKey : {}", publicKey);

            //데이터의 해시 계산 (SHA-256)
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            byte[] hashData = messageDigest.digest();

            //공개 키를 X.509 형식으로 디코딩
            byte[] keyBytes = Base64.getDecoder().decode(publicKey.getBytes());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicK = keyFactory.generatePublic(keySpec);

            //서명 객체 생성 및 검증 준비
            Signature signature = Signature.getInstance("NONEWithRSA");
            signature.initVerify(publicK);
            signature.update(hashData);
            if(signature.verify(Base64.getDecoder().decode(sign.getBytes()))) {
                throw new CertificateException("Failed to Sign");
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    //필드 암호화
    public static String encryptByPublicKey(byte[] data, String publicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicK = keyFactory.generatePublic(x509KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;

        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    //필드 복호화
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey.getBytes());
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;

        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet,MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    public static String extractPublicKeyFromCert(String cert) throws CertificateException {
        // 인증서 내용으로부터 X.509 인증서 객체 생성
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        // 서명 인증서에서 공개 키 추출
        byte[] signatureCertBytes = Base64.getDecoder().decode(cert);
        InputStream signatureCertInputStream = new ByteArrayInputStream(signatureCertBytes);
        X509Certificate signatureCert = (X509Certificate) certificateFactory.generateCertificate(signatureCertInputStream);
        PublicKey signaturePublicKey = signatureCert.getPublicKey();
        return Base64.getEncoder().encodeToString(signaturePublicKey.getEncoded());
    }

    /**
     * TLV 형태의 payload 디코딩
     *
     * @param payload  페이로드
     * @return Map<String, String> 파싱된 결과
     */
    public static Map<String, String> parseTLV(String payload) {
        // 1. 입력값 검증
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be null or empty.");
        }

        Map<String, String> parsedData = new HashMap<>();
        int index = 0;

        // 2. 전체 길이가 충분한지 검사 (태그, 길이, 값 최소 4자리 이상)
        if (payload.length() < 4) {
            throw new IllegalArgumentException("Payload is too short to be a valid TLV encoded string.");
        }

        while (index < payload.length()) {
            // 태그 추출 (2바이트)
            String tag = payload.substring(index, index + 2);
            index += 2;

            // 길이 추출 (2바이트)
            int length = Integer.parseInt(payload.substring(index, index + 2));
            index += 2;

            // 값 추출
            String value = payload.substring(index, index + length);
            index += length;

            // 파싱된 데이터 저장
            parsedData.put(tag, value);
        }
        return parsedData;
    }

    /**
     * 객체를 Map<String, Object>로 변환하는 메소드
     *
     * @param obj 변환할 객체
     * @return 변환된 Map
     */
    public static Map<String, Object> convertToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        try {
        	convertDtoToMap(obj, map, true);
        } catch (IllegalAccessException e) {
            log.error("Error converting object to map", e);
        }
        return map;
    }
    
    /**
     * 객체를 Map<String, Object>로 변환하는 메소드
     * 
     * @param map 변환된 결과 담을 Map
     * @param obj 변환할 객체
     * @return 변환된 Map
     */
    public static Map<String, Object> convertToMap(Map<String, Object> map, Object obj) {
    	try {
        	convertDtoToMap(obj, map, true);
        } catch (IllegalAccessException e) {
            log.error("Error converting object to map", e);
        }
    	return map;
    }
    
    
    /**
     * 객체를 재귀적으로 탐색하여 Map에 추가하는 메소드
     *
     * @param obj 변환할 객체
     * @param map 결과를 저장할 Map
     * @throws IllegalAccessException 필드 접근 불가 예외
     */
    private static void convertDtoToMap(Object obj, Map<String, Object> map, boolean doRecursive) throws IllegalAccessException {
        if (obj == null) {
            return;
        }
        
        Class<?> objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            String key = field.getName();
            
            if (doRecursive && isPrimitiveOrString(value) && value != null) {
                map.put(key, value);
            } else if (doRecursive) {
            	convertDtoToMap(value, map, doRecursive);
            } else {
            	map.put(key, value);
            }
        }
    }

    /**
     * 객체가 원시 타입이거나 문자열인지 확인하는 메소드
     *
     * @param obj 확인할 객체
     * @return 원시 타입이거나 문자열이면 true, 아니면 false
     */
    private static boolean isPrimitiveOrString(Object obj) {
        if (obj == null) {
            return true;
        }
        Class<?> clazz = obj.getClass();
        return clazz.isPrimitive() || clazz == String.class || clazz == Integer.class || clazz == Long.class ||
                clazz == Double.class || clazz == Float.class || clazz == Boolean.class || clazz == Byte.class ||
                clazz == Short.class || clazz == Character.class;
    }

    /**
     * Dto간 field value 이동(카피) 유틸 
     * 
     * 항상 방향은 src -> target
     * src에 있는 필드가 target에 없으면 무시됨
     * 
     * @param src
     * @param target
     */
    public static void copyDtoFields(Object src, Object target) {
    	
    	if(src == null || target == null) {
    		throw new IllegalArgumentException("src and target must be non=null");
    	}
    	
    	try {
    		Map<String,Object> srcMap = new HashMap<>();
        	convertDtoToMap(src, srcMap, false);
        	
    		Field[] fields = target.getClass().getDeclaredFields();
    		
			for (Field field : fields) {
				field.setAccessible(true);
				Object value = srcMap.get(field.getName());
				if(value != null) {
					//System.out.println("@@@@ " + field.getName() + " , isArray: " + field.getType().isArray() + ": " + value );
					if(field.getType().isArray()) { // 배열은 고유한 클래스타입으로, 타입 체크 정확히 처리해야 함
						Class<?> componentType = value.getClass().getComponentType();
				        Object copiedValue = Array.newInstance(componentType, Array.getLength(value));
				        System.arraycopy(value, 0, copiedValue, 0, Array.getLength(value));
				        field.set(target, copiedValue);
					} else {
						field.set(target, value);
					}
				}
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void printJSONData(String bookmark, Object data) {
    	try {
    		ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(data);
    		log.info("["+bookmark+"]" + json);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    public static byte[] hex2byte(String s) {
		s = s.replace(" ", "");
		return s.length() % 2 == 0 ? hex2byte(s.getBytes(), 0, s.length() >>1) : hex2byte("0" + s);
	}

	public static byte[] hex2byte(byte[] b, int offset, int len) {
		byte[] d = new byte[len];

		for(int i=0; i<len * 2; i++) {
			int shift = i % 2 == 1 ? 0 : 4;
			d[i >> 1] = (byte) (d[i >> 1] | Character.digit((char) b[offset + i], 16) << shift );
		}
		return d;
	}

    // 객체 to JSON (공백/줄바꿈 제거)
    public static String convertToJsonWithoutNullsAndEmptyStrings(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();

        // null 값 및 빈 문자열을 제외하도록 설정
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // null 값 제외
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false); // 공백 및 줄바꿈 없이 처리

        try {
            // 객체를 JSON으로 변환
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 객체에서 주어진 메서드 이름을 통해 값을 추출하는 일반화된 리플렉션 메서드.
     *
     * @param obj            값을 추출할 대상 객체
     * @param methodName     호출할 메서드 이름
     * @param parameterTypes 메서드 파라미터 타입 (없으면 빈 배열)
     * @param parameters     메서드에 넘길 파라미터들 (없으면 빈 배열)
     * @return 메서드 호출 결과
     */
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        try {
            // 메서드를 객체에서 찾아서 호출
            Method method = obj.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(obj, parameters);
        } catch (Exception e) {
            // 예외 발생 시 로그를 찍고 null을 반환
            System.err.println("Error invoking method " + methodName + " on object " + obj.getClass().getSimpleName() + ": " + e.getMessage());
            return null; // 예외 처리 후 null 반환
        }
    }

    /**
     * 객체에서 특정 메서드를 호출하여 원하는 값을 추출하는 메서드 (매개변수 없는 경우)
     *
     * @param obj        값을 추출할 객체
     * @param methodName 호출할 메서드 이름
     * @return 메서드 호출 결과
     */
    public static Object getValueByMethod(Object obj, String methodName) {
        return invokeMethod(obj, methodName, new Class<?>[]{}, new Object[]{});
    }

    /**
     * 객체에서 특정 메서드를 호출하여 값을 추출하고, 그 값에서 또 다른 메서드를 호출하는 방식
     * 예시: `getTrxInfo().getTrxCl()`처럼
     *
     * @param obj          값을 추출할 객체
     * @param firstMethod  첫 번째 메서드 이름 (예: "getTrxInfo")
     * @param secondMethod 두 번째 메서드 이름 (예: "getTrxCl")
     * @return 최종적으로 호출된 메서드의 반환 값
     */
    public static Object getValueByMethods(Object obj, String firstMethod, String secondMethod) {
        // 첫 번째 메서드를 호출하여 결과 얻기
        Object firstResult = getValueByMethod(obj, firstMethod);

        // 첫 번째 결과가 null이 아닌 경우 두 번째 메서드 호출
        if (firstResult != null) {
            return getValueByMethod(firstResult, secondMethod);
        }
        return null;
    }

    /**
     * 객체에서 특정 set 메서드를 호출하여 값을 설정하는 방식
     *
     * @param obj          값을 설정할 객체
     * @param methodName   호출할 set 메서드 이름 (예: "setSignature")
     * @param parameter    메서드에 넘길 파라미터 (예: 서명 값)
     * @return 메서드 호출 결과
     */
    public static Object setProperty(Object obj, String methodName, Object parameter) {
        // 파라미터 타입을 추론
        Class<?>[] parameterTypes = new Class<?>[]{parameter.getClass()};

        // set 메서드 호출
        return invokeMethod(obj, methodName, parameterTypes, new Object[]{parameter});
    }

    //1초마다 인덱스1로 초기화
    @Scheduled(fixedRate = 1000)
    public static void indexInit() {
        index.set(1);
    }

    public static String createMsgId(MessageId messageIdType) {
        //1초마다 인덱스 1로 초기화
        indexInit();

        //msgId (ex : U0001034420240920111152682001)
        return messageIdType + CommonField.WALLETID.getValue() + getCurrentDateTimeMillis() + String.format("%03d", index.getAndIncrement());
    }

    public static MsgInfo createMsgInfo(MessageId messageIdType, MessageType messageType) {
        String requestTimes = getCurrentDateTimes();

        return MsgInfo.builder()
                .versionNo(CommonField.VERSIONNO.getValue())
                .msgID(CommonUtil.createMsgId(messageIdType))
                .timeStamp(requestTimes)
                .msgType(String.valueOf(messageType))
                .walletID(CommonField.WALLETID.getValue())
                .build();
    }

    //카드번호 생성
    public static String generateCardNo() {

        String UPIBin = "62630952";
        StringBuilder cardNumberBuilder = new StringBuilder(UPIBin);

        for (int i = 0; i < 7; i++) {
            int randomDigit = (int) (Math.random() * 10);
            cardNumberBuilder.append(randomDigit);
        }

        // 마지막 자리를 Luhn 알고리즘으로 계산
        String cardNumberWithoutLastDigit = cardNumberBuilder.toString();
        String checkDigit = calculateLuhnCheckDigit(cardNumberWithoutLastDigit);
        cardNumberBuilder.append(checkDigit);

        return cardNumberBuilder.toString();
    }

    //Luhn 알고리즘 카드번호 체크
    static String calculateLuhnCheckDigit(String number) {
        LuhnCheckDigit luhnCheckDigit = new LuhnCheckDigit();
        try {
            return luhnCheckDigit.generate(number);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating Luhn check digit", e);
        }
    }

    //거래번호(txnID) 생성 (ex : A0001034420240920111152682001)
    public static String generateTnxID() {
        return "A" + CommonField.WALLETID.getValue() + getCurrentDateTimeMillis() + String.format("%03d", index.getAndIncrement());
    }

    /**
     * 주어진 문자열이 유효한 URL 형식인지를 검사합니다.
     * @param str 검사할 문자열
     * @return URL이 유효하면 true, 아니면 false
     */
    public static boolean isValidUrl(String str) {
        // URL 이 "http://" 또는 "https://"로 시작하는지 확인
        if (str == null || str.isEmpty()) {
            return false;
        }

        return str.startsWith("http://") || str.startsWith("https://");
    }

    public static String encodeUrl(String origin) {
        return URLEncoder.encode(origin, StandardCharsets.UTF_8);
    }

}