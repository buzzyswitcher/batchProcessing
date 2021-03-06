package ru.volkov.batch.processing.validate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.volkov.batch.processing.common.processors.ValidatingProcessor;
import ru.volkov.batch.processing.common.writers.XmlWriterConfiguration;
import ru.volkov.batch.processing.domain.Customer;

@Configuration
public class ValidateJobConfiguration {

    private StepBuilderFactory stepBuilderFactory;
    private JobBuilderFactory jobBuilderFactory;
    private ItemReader<Customer> jdbcItemReader;
    private ItemWriter<Customer> xmlWriter;


    public ValidateJobConfiguration(
            StepBuilderFactory stepBuilderFactory,
            JobBuilderFactory jobBuilderFactory,
            @Qualifier("jdbcItemReader") ItemReader<Customer> jdbcItemReader,
            @Qualifier("xmlWriter") ItemWriter<Customer> xmlWriter) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jdbcItemReader = jdbcItemReader;
        this.xmlWriter = xmlWriter;
    }

    @Bean
    public ValidatingItemProcessor<Customer> validateProcessor() {
        ValidatingItemProcessor<Customer> processor = new ValidatingItemProcessor<>();
        processor.setValidator(new ValidatingProcessor());
        processor.setFilter(true);
        return processor;
    }

    @Bean
    public Step validatingStep() throws Exception {
        return stepBuilderFactory.get("validatingStep")
                .<Customer, Customer>chunk(10)
                .reader(jdbcItemReader)
                .processor(validateProcessor())
                .writer(xmlWriter)
                .build();
    }

    @Bean
    public Job validatingJob() throws Exception {
        return jobBuilderFactory.get("validatingJob")
                .start(validatingStep())
                .build();
    }
}
