package demo.simultan.documentation.service;

import demo.simultan.documentation.model.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DocumentService {

    public Mono<Document> get() {
        return Mono.empty();
    }

    public Mono<Document> create(Document document) {
        return Mono.just(document);
    }
}
