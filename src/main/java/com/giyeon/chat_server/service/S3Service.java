package com.giyeon.chat_server.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.giyeon.chat_server.properties.DataSourceProperty;
import com.giyeon.chat_server.properties.S3Property;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png", "gif");
    private final S3Property s3Property;


    public String upload(MultipartFile image) {
        //입력받은 이미지 파일이 빈 파일인지 검증
        if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
            throw new IllegalArgumentException("이미지를 넣어 다시 시도해주세요.");
        }
        //uploadImage를 호출하여 S3에 저장된 이미지의 public url을 반환한다.
        return this.uploadImage(image);
    }

    private String uploadImage(MultipartFile image) {
        this.validateImageFileExtension(image.getOriginalFilename());
        try {
            return this.uploadImageToS3(image);
        } catch (IOException e) {
            throw new RuntimeException("S3에 파일 저장시도 실패");
        }
    }

    private void validateImageFileExtension(String filename) {
        // 마지막 .이 존재하는 인덱스 값
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("확장자가 존재하지 않습니다.");
        }

        String extention = filename.substring(lastDotIndex + 1).toLowerCase();

        if (!allowedExtentionList.contains(extention)) {
            throw new IllegalArgumentException("\"jpg\", \"jpeg\", \"png\", \"gif\" 확장자를 이용해 주세요");
        }
    }


    private String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename(); //원본 파일 명
        String extention = originalFilename.substring(originalFilename.lastIndexOf(".")); //확장자 명

        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFilename; //변경된 파일 명


        InputStream is = image.getInputStream();
        byte[] imgByte = IOUtils.toByteArray(is); //image를 byte[]로 변환

        ObjectMetadata metadata = new ObjectMetadata(); //metadata 생성
        metadata.setContentType("image/" + extention);
        metadata.setContentLength(imgByte.length);

        //S3에 요청할 때 사용할 byteInputStream 생성
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imgByte);

        try{
            //S3로 putObject 할 때 사용할 요청 객체
            //생성자 : bucket 이름, 파일 명, byteInputStream, metadata
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(s3Property.getS3().getBucket(), s3FileName, byteArrayInputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead);

            //실제로 S3에 이미지 데이터를 넣는 부분이다.
            amazonS3.putObject(putObjectRequest); // put image to S3
        }catch (Exception e){
            throw new RuntimeException("S3에 파일 저장시도 실패");
        }finally {
            byteArrayInputStream.close();
            is.close();
        }

        return amazonS3.getUrl(s3Property.getS3().getBucket(), s3FileName).toString();
    }
}
