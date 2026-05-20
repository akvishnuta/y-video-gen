package com.logicsoft.yvideogen.controller;

import com.logicsoft.yvideogen.dto.SceneGenerationRequest;
import com.logicsoft.yvideogen.dto.SceneGenerationResponse;
import com.logicsoft.yvideogen.exception.GoogleDriveException;
import com.logicsoft.yvideogen.exception.SceneGenerationException;
import com.logicsoft.yvideogen.service.GoogleDriveService;
import com.logicsoft.yvideogen.service.S3StorageService;
import com.logicsoft.yvideogen.service.SceneGenerationService;
import com.logicsoft.yvideogen.service.StorageService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ideas")
@RequiredArgsConstructor
@Tag(name = "Idea Generation", description = "Video idea and scene generation endpoints")
public class IdeaGenController {

    private final SceneGenerationService sceneGenerationService;
    private final GoogleDriveService googleDriveService;
    private final S3StorageService s3StorageService;

    @PostMapping("/generate-scenes")
    @Operation(
            summary = "Generate video scenes from a theme",
            description = "Accepts an abstract theme and generates video scenes. Optionally saves the result to storage (Google Drive or S3)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scenes generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SceneGenerationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error during scene generation")
    })
    public ResponseEntity<SceneGenerationResponse> generateScenes(
            @RequestBody SceneGenerationRequest request) {

        try {
            log.info("Received request to generate scenes for theme: {} (userDescription: {})", request.getTheme(), request.getUserDescription());

            // Validate request
            if (request.getTheme() == null || request.getTheme().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new SceneGenerationResponse(
                                null,
                                null,
                                null,
                                null,
                                "Error: Theme is required"
                        )
                );
            }

            int numberOfScenes = request.getNumberOfScenes() != null ? request.getNumberOfScenes() : 5;
            if (numberOfScenes < 1) {
                numberOfScenes = 1;
            }

            // Generate scenes using AI
            List<String> generatedScenes = sceneGenerationService.generateScenes(
                    request.getTheme(),
                    numberOfScenes,
                    request.getUserDescription()
            );

            SceneGenerationResponse response = new SceneGenerationResponse();
            response.setTheme(request.getTheme());
            response.setScenes(generatedScenes);

            // Save to storage if requested
            if (request.isSaveToGoogleDrive()) {
                try {
                    StorageService storageService = selectStorageService(request);
                    UploadResult uploadResult = storageService.uploadScenes(request.getTheme(), generatedScenes);
                    
                    response.setGoogleDriveFileId(uploadResult.getFileId());
                    response.setGoogleDriveFileLink(uploadResult.getFileLink());
                    response.setMessage("Scenes generated successfully and saved to " + uploadResult.getStorageType());
                } catch (Exception e) {
                    log.warn("Failed to upload to storage, but scenes were generated successfully", e);
                    response.setMessage("Scenes generated successfully, but failed to save to storage: " + e.getMessage());
                }
            } else {
                response.setMessage("Scenes generated successfully");
            }

            return ResponseEntity.ok(response);

        } catch (SceneGenerationException e) {
            log.error("Error generating scenes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new SceneGenerationResponse(
                            request.getTheme(),
                            null,
                            null,
                            null,
                            "Error: " + e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/generate-scenes/{theme}")
    @Operation(
            summary = "Generate video scenes from a theme (simple GET endpoint)",
            description = "Accepts an abstract theme as URL parameter and generates video scenes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scenes generated successfully"),
            @ApiResponse(responseCode = "400", description = "Theme parameter is required"),
            @ApiResponse(responseCode = "500", description = "Internal server error during scene generation")
    })
    public ResponseEntity<SceneGenerationResponse> generateScenesGet(
            @PathVariable String theme,
            @RequestParam(defaultValue = "5") int numberOfScenes) {

        SceneGenerationRequest request = new SceneGenerationRequest();
        request.setTheme(theme);
        request.setNumberOfScenes(numberOfScenes);
        request.setSaveToGoogleDrive(false);
        request.setUserDescription(null);

        return generateScenes(request);
    }

    /**
     * Select appropriate storage service based on availability and configuration
     * Prefers S3 if available and enabled, falls back to Google Drive
     */
    private StorageService selectStorageService(SceneGenerationRequest request) {
        if (s3StorageService.isConfigured()) {
            log.info("Using S3 storage service");
            return s3StorageService;
        } else {
            log.info("Using Google Drive storage service");
            return googleDriveService;
        }
    }
}
