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
@Schema(description = "Response from Google Drive test operations")
public class GoogleDriveTestResponse {

    @Schema(description = "Success status of the operation")
    private boolean success;

    @Schema(description = "Google Drive file ID", example = "1a2b3c4d5e6f7g8h9i0j")
    private String fileId;

    @Schema(description = "Google Drive file link", example = "https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9i0j/view")
    private String fileLink;

    @Schema(description = "File name uploaded to Google Drive")
    private String fileName;

    @Schema(description = "Status message or error details")
    private String message;

    @Schema(description = "Timestamp of the operation")
    private String timestamp;
}

