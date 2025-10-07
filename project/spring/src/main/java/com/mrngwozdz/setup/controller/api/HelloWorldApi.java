package com.mrngwozdz.setup.controller.api;

import com.mrngwozdz.setup.controller.model.response.HelloWorldResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Hello", description = "Hello World API")
public interface HelloWorldApi {

    @Operation(
            summary = "Get hello message",
            description = "Returns a simple hello message",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(schema = @Schema(implementation = HelloWorldResponse.class))
                    )
            }
    )
    @GetMapping("/hello")
    ResponseEntity<HelloWorldResponse> sayHello();

}