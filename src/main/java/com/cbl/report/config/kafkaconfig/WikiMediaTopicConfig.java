package com.cbl.report.config.kafkaconfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class WikiMediaTopicConfig {

    @Bean
    public NewTopic wikiMediaStreamTopic() {
        return TopicBuilder
                .name("wikimedia_stream")
                .build();
    }
}
