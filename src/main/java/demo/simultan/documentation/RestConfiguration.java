package demo.simultan.documentation;

import demo.simultan.documentation.errors.BusinessLogicException;
import demo.simultan.documentation.errors.ContractViolationException;
import demo.simultan.documentation.model.BaseResponse;
import demo.simultan.documentation.service.DocumentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RestConfiguration {

    @Bean
    RouterFunction<ServerResponse> documentEndpoint(DocumentService documentService) {
        HandlerFunction<ServerResponse> handlerFunction = serverRequest ->
                documentService.get()
                        .flatMap(document -> ServerResponse.ok()
                                .bodyValue(BaseResponse.success(document)))
                        .onErrorResume(BusinessLogicException.class, throwable -> ServerResponse.status(500)
                                .bodyValue(BaseResponse.error(throwable.getCode(), throwable.getMessage(), null)))
                        .onErrorResume(ContractViolationException.class, throwable -> ServerResponse.status(400)
                                .bodyValue(BaseResponse.error(throwable.getCode(), throwable.getMessage(), null)));

        return route()
                .route(RequestPredicates.method(HttpMethod.GET)
                        .and(path("documentation")), handlerFunction)
                .build();
    }
}
