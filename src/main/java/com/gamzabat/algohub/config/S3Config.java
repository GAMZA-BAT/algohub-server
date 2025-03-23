package com.gamzabat.algohub.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class S3Config {
	@Value("${aws_access_key}")
	private String accessKey;
	@Value("${aws_secret_key}")
	private String secretKey;
	@Value("${cloud.aws.region.static}")
	private String region;
	@Value("${aws_bucket_url}")
	private String bucketUrl;

	@Bean
	@Profile("prod")
	public AmazonS3Client amazonS3Client() {
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		return (AmazonS3Client)AmazonS3ClientBuilder.standard()
			.withRegion(region)
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.build();
	}

	@Bean
	@Profile("dev")
	public AmazonS3 minioClient() {
		return AmazonS3Client.builder()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(bucketUrl, region))
				.withPathStyleAccessEnabled(true)
				.withClientConfiguration(new ClientConfiguration().withSignerOverride("AWSS3V4SignerType"))
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.build();
	}
}