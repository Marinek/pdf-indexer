package de.indexer.processor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class JobStarter {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job processDirectoryJob;

    @PostConstruct
    public void startJob() {
        try {
             JobParameters params = new JobParametersBuilder()
            .addString("runScenario", String.valueOf(System.currentTimeMillis()))
            .toJobParameters();
            
            jobLauncher.run(processDirectoryJob, params);
        } catch (Exception e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }
}
