package com.gamzabat.algohub.feature.image.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.gamzabat.algohub.enums.ImageType;
import com.gamzabat.algohub.feature.image.exception.AwsS3Exception;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {
	private final AmazonS3 amazonS3;
	private final String DELIMITER = "_";
	@Value("${aws_bucket_name}")
	private String bucket;
	@Value("${aws_bucket_url}")
	private String bucketUrl;

	public String saveImage(ImageType type, String prefix, MultipartFile multipartFile) {
		if (multipartFile == null)
			return null;

		String filename = getImageName(type, prefix, multipartFile);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		metadata.setContentLength(multipartFile.getSize());
		try (InputStream inputStream = multipartFile.getInputStream()) {
			amazonS3.putObject(bucket, filename, inputStream, metadata);
		} catch (IOException e) {
			throw new AwsS3Exception("AWS S3 이미지 저장 중 에러가 발생했습니다.");
		}

		String imageUrl = amazonS3.getUrl(bucket, filename).toString();
		log.info("success to save image. image url : {}", imageUrl);
		return imageUrl;
	}

	public String getImageName(ImageType type, String prefix, MultipartFile multipartFile) {
		String originalFilename = multipartFile.getOriginalFilename();
		return createImagePrefix(type, prefix).concat(DELIMITER)
			.concat(Objects.requireNonNull(originalFilename));
	}

	private String createImagePrefix(ImageType imageType, String prefix) {
		return imageType.getValue() + DELIMITER + prefix;
	}

	public void deleteImage(String imageUrl) {
		String fileName = parseImageName(imageUrl);
		String file = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
		amazonS3.deleteObject(bucket, file);
		log.info("delete image from S3 bucket. image url : {}", imageUrl);
	}

	public String parseImageName(String imageUrl) {
		if (imageUrl.startsWith(bucketUrl))
			return imageUrl.substring(bucketUrl.length());
		return null;
	}

	public String createImagePrefix(Long id, String identifier) {
		return id + DELIMITER + identifier;
	}
}
