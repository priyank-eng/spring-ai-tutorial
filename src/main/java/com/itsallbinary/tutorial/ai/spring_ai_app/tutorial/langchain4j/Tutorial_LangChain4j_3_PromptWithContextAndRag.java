package com.itsallbinary.tutorial.ai.spring_ai_app.tutorial.langchain4j;


import com.itsallbinary.tutorial.ai.spring_ai_app.common.CommonHelper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Tutorial_LangChain4j_3_PromptWithContextAndRag {

    private final ChatService chatService;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public Tutorial_LangChain4j_3_PromptWithContextAndRag(ChatLanguageModel chatLanguageModel) {
        this.embeddingStore = new InMemoryEmbeddingStore<>();

        this.chatService = AiServices.builder(ChatService.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // Retains last 10 messages
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore)) // ✅ Add RAG-based retriever
                .build();
    }

    @PostConstruct
    public void initializeKnowledgeBase() {
        List<Document> documents = List.of(
                Document.from("MySpaceCompany is planning to send a satellite to Jupiter in 2030."),
                Document.from("MySpaceCompany is planning to send a satellite to Mars in 2026."),
                Document.from("MySpaceCompany helps control climate change through various programs.")
        );

        // Ingest documents into the embedding store
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
    }

    @GetMapping(CommonHelper.URL_PREFIX_FOR_LANGCHAIN4J + "tutorial/3")
    public String generation(@RequestParam String userInput) {
        // Chat service automatically retrieves relevant context due to contentRetriever()
        String aiResponse = chatService.chat("session-1", userInput);

        return CommonHelper.surroundMessage(
                getClass(),
                userInput,
                aiResponse
        );
    }
}
