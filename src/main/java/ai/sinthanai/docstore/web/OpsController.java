package ai.sinthanai.docstore.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "ops", description = "Health and liveness endpoints")
public class OpsController {

    @GetMapping("/ping")
    @Operation(
        summary = "Liveness check",
        description = "Returns `{\"status\":\"ok\"}` if the service is running.",
        responses = @ApiResponse(
            responseCode = "200",
            content = @Content(examples = @ExampleObject(value = "{\"status\":\"ok\",\"service\":\"sin-docstore\"}"))
        )
    )
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "sin-docstore"));
    }
}
