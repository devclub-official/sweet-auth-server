package com.ptpt.authservice.swagger;

import com.ptpt.authservice.controller.request.EncryptedUserRequestBody;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.controller.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserControllerDocs {

    @Operation(
            summary = "사용자 등록 API",
            description = "새로운 사용자를 등록합니다.",
            tags = {"사용자 API"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "사용자 등록 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            )
    })
    ResponseEntity<CustomApiResponse<UserResponse>> createNewUser(@RequestBody EncryptedUserRequestBody requestBody);
}
