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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@SpringBootTest(classes = TestingConfiguration.class,
		webEnvironment = RANDOM_PORT, properties = {
		"demo.simultan.test.rest.document.scheme=https",
		"demo.simultan.test.rest.document.host=simultan.com"
})
@AutoConfigureWebTestClient
@ExtendWith(RestDocumentationExtension.class)
@ImportAutoConfiguration({SpringDataWebAutoConfiguration.class, JacksonAutoConfiguration.class,
		ReactiveWebServerFactoryAutoConfiguration.class,
		HttpHandlerAutoConfiguration.class,
		WebFluxAutoConfiguration.class,
		RestConfiguration.class})
class DemoApplicationTests {

	private final static String INTEGER = "Integer";
	private final static String OBJECT = "Object";
	private final static String STRING = "String";
	private final static String DATE = "Date";
	private final static String TIME = "Time";
	private final static String ENUMS = "Enums";
	private final static String BIG_DECIMAL = "BigDecimal";
	private final static String BOOLEAN = "Boolean";

	@Value("${demo.simultan.test.rest.document.scheme}")
	private String scheme;

	@Value("${demo.simultan.test.rest.document.host}")
	private String host;

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
                                            .scheme(scheme)
                                            .host(host).removePort())
                            .withResponseDefaults(
                                    prettyPrint()
                            )
                    );
        });
	}

	@Test
	void whenCreateDocs_thenShouldSuccess() {
		Document document = Document.builder()
				.id(UUID.randomUUID().toString())
				.name("simultan")
				.number(1)
				.build();

		Mockito.when(documentService.create(any()))
				.thenReturn(Mono.just(document));

		webTestClient.post()
				.uri(uriBuilder -> uriBuilder
						.path("/documentation")
						.build())
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE)
				.bodyValue(document)
				.exchange()
				.expectStatus().isOk()
				.expectBody(RESPONSE_REFERENCE)
				.value(result -> {
					assertThat(document).isEqualTo(result.getData());
				})
				.consumeWith(document("document/create-document-success",
						Preprocessors.preprocessRequest(prettyPrint()),
						Preprocessors.preprocessResponse(prettyPrint()),
						requestFields(),
						responseFields()
				));

	}

	@Test
	void whenGetDocs_thenShouldSuccess() {
		Document document = Document.builder()
				.name("simultan")
				.number(1)
				.build();

		Document documentResult = document.toBuilder()
				.id(UUID.randomUUID().toString())
				.build();
		Mockito.when(documentService.get())
				.thenReturn(Mono.just(documentResult));

		webTestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/documentation")
						.build())
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE)
				.exchange()
				.expectStatus().isOk()
				.expectBody(RESPONSE_REFERENCE)
				.value(result -> {
					assertThat(documentResult).isEqualTo(result.getData());
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

	private RequestFieldsSnippet requestFields() {
		return PayloadDocumentation.requestFields()
				.and(PayloadDocumentation.fieldWithPath("id").description("id request of body").type(STRING))
				.and(PayloadDocumentation.fieldWithPath("number").description("number request of body").type(INTEGER))
				.and(PayloadDocumentation.fieldWithPath("name").description("name request of body").type(STRING));
	}

	private ResponseFieldsSnippet responseFields() {
		String data = "data";
		return PayloadDocumentation.responseFields(
				PayloadDocumentation.fieldWithPath("code").description("Error code description")
						.type(STRING),
				PayloadDocumentation.fieldWithPath("message").description("Error message description")
						.type(STRING),
				PayloadDocumentation.fieldWithPath("errors").description("Details error message")
						.type(STRING))
				.and(PayloadDocumentation.fieldWithPath(data).description("Payload response")
						.type(OBJECT).optional())
				.and(payloadResponse(data));
	}

	private static List<FieldDescriptor> payloadResponse(String data) {
		return new ArrayList<>(PayloadDocumentation.applyPathPrefix(data,
				Arrays.asList(
						PayloadDocumentation.fieldWithPath(".id").description("ID of document")
								.type(STRING),
						PayloadDocumentation.fieldWithPath(".number").description("Number of document")
								.type(INTEGER),
						PayloadDocumentation.fieldWithPath(".name").description("Name of document").type(STRING)
				)
		));
	}

}

