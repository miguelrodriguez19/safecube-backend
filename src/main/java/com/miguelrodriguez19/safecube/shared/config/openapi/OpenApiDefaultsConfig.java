package com.miguelrodriguez19.safecube.shared.config.openapi;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDefaultsConfig {

  @Bean
  public OperationCustomizer defaultApiResponses() {
    return (operation, handlerMethod) -> {
      final var responses = operation.getResponses();

      // 400 - Bad Request (validation / domain)
      responses.putIfAbsent(
          "400", new ApiResponse().description("Bad request").content(errorResponseContent()));

      // 401 - Unauthorized (auth errors, no body)
      responses.putIfAbsent("401", new ApiResponse().description("Unauthorized"));

      // 403 - Forbidden (account disabled, forbidden action)
      responses.putIfAbsent("403", new ApiResponse().description("Forbidden"));

      // 404 - Not found (resource / account / vault)
      responses.putIfAbsent("404", new ApiResponse().description("Not found"));

      // 409 - Conflict (already exists, stale update)
      responses.putIfAbsent("409", new ApiResponse().description("Conflict"));

      // 500 - Internal error
      responses.putIfAbsent(
          "500",
          new ApiResponse().description("Internal server error").content(errorResponseContent()));

      return operation;
    };
  }

  private Content errorResponseContent() {
    final var schema =
        new ObjectSchema()
            .addProperty("error", new Schema<String>().type("string"))
            .addProperty(
                "fields",
                new Schema<Map<String, String>>()
                    .type("object")
                    .additionalProperties(new Schema<String>().type("string")));

    return new Content().addMediaType("application/json", new MediaType().schema(schema));
  }
}
