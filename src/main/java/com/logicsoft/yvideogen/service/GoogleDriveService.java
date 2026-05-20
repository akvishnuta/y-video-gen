package com.logicsoft.yvideogen.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.logicsoft.yvideogen.exception.GoogleDriveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class GoogleDriveService implements StorageService {

    private static final String APPLICATION_NAME = "Y Video Gen";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.drive.credentials.file:}")
    private String credentialsFile;

    @Value("${google.drive.folder.id:}")
    private String parentFolderId;

    private Drive driveService;

    public GoogleDriveService() {
    }

    private Drive getDriveService() throws GoogleDriveException {
        if (driveService != null) {
            return driveService;
        }

        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential = GoogleCredential.fromStream(
                    new FileInputStream(credentialsFile)
            ).createScoped(Collections.singleton(DriveScopes.DRIVE));

            driveService = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            return driveService;
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to initialize Google Drive service", e);
            throw new GoogleDriveException("Failed to initialize Google Drive service: " + e.getMessage(), e);
        }
    }

    @Override
    public UploadResult uploadContent(String content, String fileName) throws GoogleDriveException {
        try {
            log.info("Uploading content to Google Drive with filename: {}", fileName);

            // Validate credentials are configured
            if (credentialsFile == null || credentialsFile.isEmpty()) {
                throw new GoogleDriveException("Google Drive credentials file path not configured. Set 'google.drive.credentials.file' property.");
            }

            if (!new java.io.File(credentialsFile).exists()) {
                throw new GoogleDriveException("Google Drive credentials file not found at: " + credentialsFile);
            }

            Drive drive = getDriveService();

            // Create temporary text file with content
            java.io.File tempFile = java.io.File.createTempFile("upload_" + fileName.hashCode(), ".txt");
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
                writer.write(content);
            }

            // Create file metadata
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setMimeType("text/plain");

            // Set parent folder if configured
            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                fileMetadata.setParents(Collections.singletonList(parentFolderId));
            }

            // Upload file
            FileContent fileContent = new FileContent("text/plain", tempFile);
            File uploadedFile = drive.files().create(fileMetadata, fileContent)
                    .setFields("id, webViewLink")
                    .execute();

            log.info("Successfully uploaded file to Google Drive. File ID: {}", uploadedFile.getId());

            // Clean up temporary file
            tempFile.delete();

            // Return result with file ID and shareable link
            return UploadResult.builder()
                    .fileId(uploadedFile.getId())
                    .fileLink(uploadedFile.getWebViewLink())
                    .fileName(fileMetadata.getName())
                    .storageType("GoogleDrive")
                    .build();

        } catch (IOException e) {
            log.error("Error uploading content to Google Drive", e);
            throw new GoogleDriveException("Failed to upload to Google Drive: " + e.getMessage(), e);
        }
    }

    @Override
    public UploadResult uploadScenes(String theme, List<String> scenes) throws GoogleDriveException {
        return uploadScenesToGoogleDrive(theme, scenes);
    }

    public UploadResult uploadScenesToGoogleDrive(String theme, List<String> scenes) throws GoogleDriveException {
        try {
            log.info("Uploading scenes to Google Drive for theme: {}", theme);

            // Validate credentials are configured
            if (credentialsFile == null || credentialsFile.isEmpty()) {
                throw new GoogleDriveException("Google Drive credentials file path not configured. Set 'google.drive.credentials.file' property.");
            }

            if (!new java.io.File(credentialsFile).exists()) {
                throw new GoogleDriveException("Google Drive credentials file not found at: " + credentialsFile);
            }

            Drive drive = getDriveService();

            // Create temporary text file with scenes
            java.io.File tempFile = java.io.File.createTempFile("scenes_" + theme.hashCode(), ".txt");
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
                writer.write("Video Scenes for Theme: " + theme + "\n");
                writer.write("Generated on: " + new java.util.Date() + "\n");
                writer.write("=".repeat(50) + "\n\n");

                for (int i = 0; i < scenes.size(); i++) {
                    writer.write("Scene " + (i + 1) + ":\n");
                    writer.write(scenes.get(i) + "\n\n");
                }
            }

            // Create file metadata
            File fileMetadata = new File();
            fileMetadata.setName("video_scenes_" + theme.replaceAll("[^a-zA-Z0-9]", "_") + ".txt");
            fileMetadata.setMimeType("text/plain");

            // Set parent folder if configured
            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                fileMetadata.setParents(Collections.singletonList(parentFolderId));
            }

            // Upload file
            FileContent fileContent = new FileContent("text/plain", tempFile);
            File uploadedFile = drive.files().create(fileMetadata, fileContent)
                    .setFields("id, webViewLink")
                    .execute();

            log.info("Successfully uploaded file to Google Drive. File ID: {}", uploadedFile.getId());

            // Clean up temporary file
            tempFile.delete();

            // Return result with file ID and shareable link
            return UploadResult.builder()
                    .fileId(uploadedFile.getId())
                    .fileLink(uploadedFile.getWebViewLink())
                    .fileName(fileMetadata.getName())
                    .storageType("GoogleDrive")
                    .build();

        } catch (IOException e) {
            log.error("Error uploading scenes to Google Drive", e);
            throw new GoogleDriveException("Failed to upload to Google Drive: " + e.getMessage(), e);
        }
    }

    /**
     * @deprecated Use {@link UploadResult} instead. This class is kept for backward compatibility.
     */
    @Deprecated
    public static class GoogleDriveUploadResult {
        public String fileId;
        public String fileLink;
        public String fileName;

        public GoogleDriveUploadResult(String fileId, String fileLink, String fileName) {
            this.fileId = fileId;
            this.fileLink = fileLink;
            this.fileName = fileName;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String fileId;
            private String fileLink;
            private String fileName;

            public Builder fileId(String fileId) {
                this.fileId = fileId;
                return this;
            }

            public Builder fileLink(String fileLink) {
                this.fileLink = fileLink;
                return this;
            }

            public Builder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public GoogleDriveUploadResult build() {
                return new GoogleDriveUploadResult(fileId, fileLink, fileName);
            }
        }
    }
}

