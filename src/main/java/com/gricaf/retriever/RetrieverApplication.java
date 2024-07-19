package com.gricaf.retriever;

import com.gricaf.retriever.jobscheduling.job.Retriever;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RetrieverApplication {

	public static void main(String[] args) {
		SpringApplication.run(RetrieverApplication.class, args);
	}

    @Bean(initMethod="retrieveData")
    public Retriever getFunnyBean() {
        return new Retriever();
    }
}
