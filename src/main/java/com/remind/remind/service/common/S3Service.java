package com.remind.remind.service.common;

import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Template s3Template;
    private final String bucketName;

    public S3Service(S3Template s3Template, @Value("${spring.cloud.aws.s3.bucket}") String bucketName) {
        this.s3Template = s3Template;
        this.bucketName = bucketName;
    }

    /**
     * 파일을 S3에 업로드합니다.
     * @param file 업로드할 파일
     * @param dirName 저장할 디렉토리 이름 (예: "doctors/certifications")
     * @return 업로드된 파일의 S3 URL
     */
    public String uploadFile(MultipartFile file, String dirName) {
        // 파일 이름이 중복되지 않도록 UUID 생성하여 결합
        String fileName = dirName + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            // S3에 파일 업로드 실행
            var s3Resource = s3Template.upload(bucketName, fileName, inputStream, null);
            
            // 업로드된 파일의 공개 URL 주소 반환
            return s3Resource.getURL().toString();
        } catch (IOException e) {
            throw new BaseException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
