package com.logicsoft.yvideogen.controller;

import com.logicsoft.yvideogen.dto.GoogleDriveTestRequest;
import com.logicsoft.yvideogen.dto.GoogleDriveTestResponse;
import com.logicsoft.yvideogen.exception.GoogleDriveException;
import com.logicsoft.yvideogen.service.GoogleDriveService;
import com.logicsoft.yvideogen.service.UploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/api/v1/google-drive-test")
@RequiredArgsConstructor
@Tag(name = "Google Drive Test", description = "Endpoints to test Google Drive upload functionality")
public class GoogleDriveTestController {

    private final GoogleDriveService googleDriveService;

    @PostMapping("/upload")
    @Operation(
            summary = "Upload test file to Google Drive",
            description = "Uploads a test file with custom content to Google Drive. Useful for validating Google Drive configuration and connectivity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GoogleDriveTestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error or Google Drive connection failed")
    })
    public ResponseEntity<GoogleDriveTestResponse> uploadTestFile(
            @RequestBody(required = false) GoogleDriveTestRequest request) {

        try {
            log.info("Received request to upload test file to Google Drive");

            // Set defaults if request is null or incomplete
            if (request == null) {
                request = new GoogleDriveTestRequest();
            }

            String fileName = request.getFileName();
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "test_file_" + System.currentTimeMillis() + ".txt";
            }

            String fileContent = request.getFileContent();
            if (fileContent == null || fileContent.trim().isEmpty()) {
                fileContent = "This is a test file uploaded at " + LocalDateTime.now() + 
                             "\n\nThis file was created to test Google Drive upload functionality.";
            }

            // Create temporary file with test content
            File tempFile = File.createTempFile("gd_test_", ".txt");
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
                writer.write(fileContent);
            }

            log.info("Created temporary test file at: {}", tempFile.getAbsolutePath());

            // Upload to Google Drive using the existing service
            ArrayList<String> dummyScenes = new ArrayList<>();
            dummyScenes.add(fileContent);
            
            UploadResult uploadResult = 
                    googleDriveService.uploadScenesToGoogleDrive("test", dummyScenes);

            // Clean up temporary file
            boolean deleted = tempFile.delete();
            if (deleted) {
                log.debug("Temporary test file cleaned up successfully");
            }

            GoogleDriveTestResponse response = new GoogleDriveTestResponse();
            response.setSuccess(true);
            response.setFileId(uploadResult.getFileId());
            response.setFileLink(uploadResult.getFileLink());
            response.setFileName(fileName);
            response.setMessage("Test file uploaded successfully to Google Drive");
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            log.info("Successfully uploaded test file. File ID: {}", uploadResult.getFileId());
            return ResponseEntity.ok(response);

        } catch (GoogleDriveException e) {
            log.error("Google Drive error during test upload", e);
            GoogleDriveTestResponse response = new GoogleDriveTestResponse();
            response.setSuccess(false);
            response.setMessage("Google Drive error: " + e.getMessage());
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (IOException e) {
            log.error("IO error during test file creation", e);
            GoogleDriveTestResponse response = new GoogleDriveTestResponse();
            response.setSuccess(false);
            response.setMessage("IO Error: " + e.getMessage());
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            log.error("Unexpected error during test upload", e);
            GoogleDriveTestResponse response = new GoogleDriveTestResponse();
            response.setSuccess(false);
            response.setMessage("Unexpected error: " + e.getMessage());
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/upload-simple")
    @Operation(
            summary = "Simple GET endpoint to test Google Drive upload",
            description = "Creates and uploads a simple test file to Google Drive using default values. Useful for quick testing via browser or curl."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error or Google Drive connection failed")
    })
    public ResponseEntity<GoogleDriveTestResponse> uploadSimpleTestFile() {
        log.info("Received simple GET request to upload test file");
        return uploadTestFile(null);
    }

    @PostMapping("/upload-with-text")
    @Operation(
            summary = "Upload test file with custom text content",
            description = "Uploads a test file to Google Drive with custom text content provided as request parameter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Content is required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GoogleDriveTestResponse> uploadWithText(
            @RequestParam(value = "content") String content,
            @RequestParam(value = "fileName", required = false) String fileName) {

        if (content == null || content.trim().isEmpty()) {
            GoogleDriveTestResponse response = new GoogleDriveTestResponse();
            response.setSuccess(false);
            response.setMessage("Content parameter is required");
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            return ResponseEntity.badRequest().body(response);
        }

        GoogleDriveTestRequest request = new GoogleDriveTestRequest();
        request.setFileContent(content);
        if (fileName != null && !fileName.isEmpty()) {
            request.setFileName(fileName);
        }

        return uploadTestFile(request);
    }

    @GetMapping("/status")
    @Operation(
            summary = "Check Google Drive configuration status",
            description = "Checks if Google Drive credentials are properly configured without attempting to upload."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuration status retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Error checking configuration")
    })
    public ResponseEntity<GoogleDriveTestResponse> checkStatus() {
        try {
            log.info("Checking Google Drive configuration status");
            
            GoogleDriveTestResponse response = new GoogleDriveTestResponse();
            response.setSuccess(true);
            response.setMessage("Google Drive service is configured and ready. Use /api/v1/google-drive-test/upload endpoint to test actual file upload.");
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking Google Drive status", e);
            GoogleDriveTestResponse response = new GoogleDriveTestResponse();
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}



