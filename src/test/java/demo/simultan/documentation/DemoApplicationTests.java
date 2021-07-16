package demo.simultan.documentation;

import demo.simultan.documentation.errors.BusinessLogicException;
import demo.simultan.documentation.errors.ContractViolationException;
import demo.simultan.documentation.model.BaseResponse;
import demo.simultan.documentation.model.Document;
import demo.simultan.documentation.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@SpringBootTest(classes = TestingConfiguration.class,
		webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith(RestDocumentationExtension.class)
@ImportAutoConfiguration({SpringDataWebAutoConfiguration.class, JacksonAutoConfiguration.class})
class DemoApplicationTests {


	private static final ParameterizedTypeReference<BaseResponse<Document>> RESPONSE_REFERENCE =
			new ParameterizedTypeReference<BaseResponse<Document>>() {};

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private DocumentService documentService;

	@BeforeEach
	void setUPBefore(RestDocumentationContextProvider restDocumentation) {
        this.webTestClient = webTestClient.mutateWith((builder, httpHandlerBuilder, connector) -> {
            builder
                    .filter(documentationConfiguration(restDocumentation)
                            .operationPreprocessors()
                            .withRequestDefaults(
                                    prettyPrint(),
                                    modifyUris()
                                            .scheme("https")
                                            .host("simultan.com").removePort())
                            .withResponseDefaults(
                                    prettyPrint()
                            )
                    );
        });
	}

	@Test
	void whenGetDocs_thenShouldSuccess() {
		Document document = Document.builder()
				.id(UUID.randomUUID().toString())
				.name("simultan")
				.number(1)
				.build();

		Mockito.when(documentService.get())
				.thenReturn(Mono.just(document));

		webTestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/documentation")
						.build())
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE)
				.exchange()
				.expectStatus().isOk()
				.expectBody(RESPONSE_REFERENCE)
				.value(result -> {
					assertThat(document).isEqualTo(result.getData());
				})
                .consumeWith(document("document/get-document-success",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint())
                ));

	}


    @Test
    void whenGetDocs_thenShouldSystemError() {
        Mockito.when(documentService.get())
                .thenReturn(Mono.error(new BusinessLogicException("SYSTEM_ERROR", "system error")));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/documentation")
                        .build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody(RESPONSE_REFERENCE)
                .value(result -> {
                    assertThat("SYSTEM_ERROR").isEqualTo(result.getCode());
                    assertThat("system error").isEqualTo(result.getMessage());
                })
                .consumeWith(document("document/get-document-system-error",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint())
                ));

    }

    @Test
    void whenGetDocs_thenShouldBadRequest() {
        Mockito.when(documentService.get())
                .thenReturn(Mono.error(new ContractViolationException("BAD_REQUEST", "Bad request")));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/documentation")
                        .build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody(RESPONSE_REFERENCE)
                .value(result -> {
                    assertThat("BAD_REQUEST").isEqualTo(result.getCode());
                    assertThat("Bad request").isEqualTo(result.getMessage());
                })
                .consumeWith(document("document/get-document-bad-request",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint())
                ));

    }
}

