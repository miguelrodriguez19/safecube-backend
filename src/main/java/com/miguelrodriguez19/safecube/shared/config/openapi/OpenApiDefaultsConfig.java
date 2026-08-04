package com.miguelrodriguez19.safecube.shared.config.openapi;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDefaultsConfig {

  private static final String APPLICATION_JSON = "application/json";
  private static final String WILDCARD_MEDIA_TYPE = "*/*";
  private static final String ERROR_RESPONSE_REF = "#/components/schemas/ErrorResponse";

  private static final List<String> PUBLIC_PATH_PREFIXES = List.of("/v3/api-docs/", "/swagger-ui/");

  private static final List<String> PUBLIC_EXACT_PATHS =
      List.of(
          "/auth/register",
          "/auth/login",
          "/auth/refresh",
          "/actuator/health",
          "/error",
          "/v3/api-docs",
          "/v3/api-docs.yaml",
          "/swagger-ui",
          "/swagger-ui.html");

  @Bean
  public OperationCustomizer defaultApiResponses() {
    return (operation, handlerMethod) -> {
      final var responses = operation.getResponses();

      // 400 - Bad Request (validation / domain)
      responses.addApiResponse(
          "400", new ApiResponse().description("Bad request").content(errorResponseContent()));

      // 401 - Unauthorized (auth errors, no body)
      responses.putIfAbsent("401", new ApiResponse().description("Unauthorized"));

      // 403 - Forbidden (account disabled, forbidden action)
      responses.putIfAbsent("403", new ApiResponse().description("Forbidden"));

      // 404 - Not found (resource / account / vault)
      responses.putIfAbsent("404", new ApiResponse().description("Not found"));

      // 409 - Conflict (already exists or idempotency key reused with different content)
      responses.putIfAbsent("409", new ApiResponse().description("Conflict"));

      // 500 - Internal error
      responses.addApiResponse(
          "500",
          new ApiResponse().description("Internal server error").content(errorResponseContent()));

      return operation;
    };
  }

  @Bean
  public OpenApiCustomizer openApiContractHardening() {
    return openApi -> {
      if (openApi.getPaths() == null) {
        return;
      }

      openApi
          .getPaths()
          .forEach(
              (path, pathItem) ->
                  pathItem
                      .readOperations()
                      .forEach(
                          operation -> {
                            if (isPublicPath(path)) {
                              operation.setSecurity(List.of());
                            }

                            final var responses = operation.getResponses();
                            if (responses == null) {
                              return;
                            }

                            responses
                                .values()
                                .forEach(
                                    response -> {
                                      normalizeJsonMediaType(response);
                                      dropVoidContent(response);
                                    });
                          }));

      final var components = openApi.getComponents();
      if (components == null || components.getSchemas() == null) {
        return;
      }

      components.getSchemas().remove("Void");
      components.getSchemas().remove("com.miguelrodriguez19.safecube.shared.result.Void");
    };
  }

  private Content errorResponseContent() {
    return new Content()
        .addMediaType(
            APPLICATION_JSON, new MediaType().schema(new Schema<>().$ref(ERROR_RESPONSE_REF)));
  }

  private boolean isPublicPath(final String path) {
    if (PUBLIC_EXACT_PATHS.contains(path)) {
      return true;
    }

    return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
  }

  private void normalizeJsonMediaType(final ApiResponse response) {
    final var content = response.getContent();
    if (content == null) {
      return;
    }

    final var wildcard = content.remove(WILDCARD_MEDIA_TYPE);
    if (wildcard != null) {
      content.putIfAbsent(APPLICATION_JSON, wildcard);
    }
  }

  private void dropVoidContent(final ApiResponse response) {
    final var content = response.getContent();
    if (content == null) {
      return;
    }

    content.entrySet().removeIf(entry -> isVoidSchema(entry.getValue()));
    if (content.isEmpty()) {
      response.setContent(null);
    }
  }

  private boolean isVoidSchema(final MediaType mediaType) {
    if (mediaType == null || mediaType.getSchema() == null) {
      return false;
    }

    final var ref = mediaType.getSchema().get$ref();
    return "#/components/schemas/Void".equals(ref)
        || "#/components/schemas/com.miguelrodriguez19.safecube.shared.result.Void".equals(ref);
  }
}
