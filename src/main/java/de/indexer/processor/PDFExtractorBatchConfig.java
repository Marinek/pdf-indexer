package de.indexer.processor;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.index.IndexWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import de.indexer.dto.PdfFile;
import de.indexer.processor.batch.PDFItemProcessor;
import de.indexer.processor.batch.PDFItemReader;
import de.indexer.processor.batch.PDFItemWriter;

@Configuration
public class PDFExtractorBatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private IndexWriter indexWriter;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${pdf.file.path}")
    private String pdfFilesPath;

    @Bean
    public Job processDirectoryJob(Step processDirectoryStep) {
        return new JobBuilder("processDirectoryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(processDirectoryStep)
                .build();
    }

    @Bean
    public JobLauncher jobLauncher() throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public Step processDirectoryStep(ItemReader<Path> reader, ItemProcessor<Path, PdfFile> processor,
            ItemWriter<PdfFile> writer) {
        return new StepBuilder("processDirectoryStep", jobRepository)
                .<Path, PdfFile>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Path> directoryItemReader() {
        return new PDFItemReader(Paths.get(pdfFilesPath));
    }

    @Bean
    public ItemProcessor<Path, PdfFile> directoryItemProcessor() {
        return new PDFItemProcessor();
    }

    @Bean
    public ItemWriter<PdfFile> directoryItemWriter() {
        return new PDFItemWriter(indexWriter);
    }

}
