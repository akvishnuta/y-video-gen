package com.logicsoft.yvideogen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing generated video scenes")
public class SceneGenerationResponse {

    @Schema(description = "Theme used for generation")
    private String theme;

    @Schema(description = "List of generated scenes")
    private List<String> scenes;

    @Schema(description = "Google Drive file ID if saved", example = "1a2b3c4d5e6f7g8h9i0j")
    private String googleDriveFileId;

    @Schema(description = "Google Drive file link if saved", example = "https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9i0j/view")
    private String googleDriveFileLink;

    @Schema(description = "Success message")
    private String message;
}

