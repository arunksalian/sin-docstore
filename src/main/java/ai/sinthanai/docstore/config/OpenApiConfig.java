package ai.sinthanai.docstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("sin-docstore")
                .description("""
                    Ops endpoints for the Sinthanai docstore service.

                    **Primary API**: gRPC on port 9090 — see `src/main/proto/docstore.proto`.
                    """)
                .version("0.1.0"))
            .tags(List.of(new Tag().name("ops").description("Health and liveness endpoints")));
    }
}
