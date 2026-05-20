package com.logicsoft.yvideogen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate video scenes from a theme")
public class SceneGenerationRequest {

    @Schema(description = "Abstract theme for video generation", example = "The future of artificial intelligence in healthcare")
    private String theme;

    @Schema(description = "Additional user-provided description to guide scene generation", example = "Focus on a single elderly protagonist who loves gardening and quiet mornings")
    private String userDescription;

    @Schema(description = "Number of scenes to generate", example = "5")
    private Integer numberOfScenes;

    @Schema(description = "Save to Google Drive", example = "true")
    private boolean saveToGoogleDrive;
}

