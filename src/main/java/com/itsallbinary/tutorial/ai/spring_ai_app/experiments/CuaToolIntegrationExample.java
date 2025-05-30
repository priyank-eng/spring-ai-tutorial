package com.itsallbinary.tutorial.ai.spring_ai_app.experiments;


import com.itsallbinary.tutorial.ai.spring_ai_app.common.CommonHelper;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CuaToolIntegrationExample {

    private static final Logger logger = LoggerFactory.getLogger(CuaToolIntegrationExample.class);

    private final ChatClient chatClient;

    private static final String CUSTOM_USER_TEXT_ADVISE = """

			Context information is below, surrounded by ---------------------

			---------------------
			{question_answer_context}
			---------------------

			Given the context and provided history information and not prior knowledge,
			reply to the user comment. If the answer is not in the context, inform
			the user that context doesn't have answer but I can answer based on general knowledge.
			Then reply to user with general knowledge using prior knowledge
			""";


    public CuaToolIntegrationExample(
            @Qualifier("openAiChatModel") ChatModel openAiChatModel) {

        ChatClient.Builder chatClientBuilder = ChatClient.builder(openAiChatModel);

        /**
         * In memory Vector database
         */
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(new LocalEmbeddingModel()).build();
        // MySpaceCompany internal knowledge
        List<Document> documents = List.of(
                new Document("MySpaceCompany is planning to send a satellite to Jupiter in 2030.", Map.of("planet", "Jupiter")),
                new Document("MySpaceCompany is planning to send a satellite to Mars in 2026.", Map.of("planet", "Mars")),
                new Document("MySpaceCompany is helps control climate change through various programs.", Map.of("planet", "Earth"))
        );
        // Store data in the vector store
        vectorStore.add(documents);

        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore,
                SearchRequest.builder().build(), CUSTOM_USER_TEXT_ADVISE);


        this.chatClient = chatClientBuilder
                /*
                Add advisor with in memory chat for storing context
                 */
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
//                        , questionAnswerAdvisor
                        /*
                        Add logger to log details
                         */
                        , new SimpleLoggerAdvisor()
                )
                /*
                Add tool for getting current status
                 */
//                .defaultTools(new Tutorial_4_0_PromptWithContextRagAndTools.PlanStatusServiceTool())
//                .defaultTools("computer")
                .build();
    }

    @GetMapping(CommonHelper.URL_PREFIX_FOR_EXPERIMENTS + "cua")
    String generation(String userInput) {

        String aIResponse = this.chatClient.prompt()
                .user(userInput)
                .tools("computer")
                .call()
                .content();

        return CommonHelper.surroundMessage(
                getClass(),
                userInput,
                aIResponse
        );
    }

    /**
     * Imitates a Tool which will call a API/Service to fetch current status.
     */
    @Slf4j
    public static class PlanStatusServiceTool {

        @Tool(description = "Returns current status of a plan of MySpaceCompany, provided planet name as input")
        String getCurrentStatus(String nameOfPlanet) {
            logger.info("MySpaceCompany - Fetching status of plan for planet " + nameOfPlanet);
            String status = "Nothing planned for this planet = " + nameOfPlanet;
            switch (StringUtils.lowerCase(StringUtils.trim(nameOfPlanet))){
                case "jupiter":
                    status = "Implementation is currently at 20%";
                    break;
                case "mars":
                    status = "Implementation is currently at 80%";
                    break;
                case "earth":
                    status = "This is always in ongoing";
                    break;
            }
            logger.info("MySpaceCompany - status =  " + status);
            return status;
        }
    }


}
