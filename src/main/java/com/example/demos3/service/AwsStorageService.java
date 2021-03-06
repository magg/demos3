package com.example.demos3.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * AWS S3 Abstraction to upload, get, delete files from an specific bucket.
 */
@Slf4j
@Component
public class AwsStorageService implements StorageService
{

    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;
    @Value("${aws.region}")
    private String region;


    /**
     * Init service client.
     */
    @PostConstruct
    private void initialize()
    {
        log.debug("Initializing Amazon S3 client.");
        s3Client = getAmazonS3Client();
    }


    private S3Client getAmazonS3Client()
    {
        return S3Client
            .builder()
            .region(Region.of(region))
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();
    }


    /**
     * {@inheritDoc}
     */
    public String createAsset(File file, AssetType assetType) {

        try {
            PutObjectResponse putObjectResult = s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(assetType.getPrefix() + file.getName())
                    .contentType(Files.probeContentType(file.toPath()))
                    .contentLength(file.length())
                    .build(),
                RequestBody.fromFile(file));
            final URL reportUrl = s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(file.getName()).build());
            log.debug("Object {} put to S3 and accessible by URL {}", putObjectResult, reportUrl);
            return reportUrl.toString();
        } catch (SdkException ex) {
            log.error("Unexpected error putting file in S3 StorageService", ex);
            throw new StorageException("Unexpected error putting file in S3 StorageService", ex);
        } catch (IOException ex) {
            log.error("Unexpected error getting content-type from file", ex);
            throw new StorageException("Unexpected error getting content-type from file", ex);
        }
    }

    @Override
    public byte[] downloadAsset(String name, AssetType assetType) {
        ResponseInputStream<GetObjectResponse> fullObject = null;
        byte[] bytes = null;
        try {
            // Get an object and print its contents.
            System.out.println("Downloading an object");
            fullObject = s3Client.getObject(GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(assetType.getPrefix() + name)
                .build());
            System.out.println("Content-Type: " + fullObject.response().contentType());

            bytes = fullObject.readAllBytes();

        } catch (SdkServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            log.error("server error {}", e.getMessage());
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            log.error("client error {}", e.getMessage());
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
            if (fullObject != null) {
                try {
                    fullObject.close();
                } catch (IOException e) {
                    log.error("closing IOException: " + e.getMessage());
                }
            }
        }
        return bytes;
    }

}
