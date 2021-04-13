package study.studyquartz;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class StudyQuartzApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyQuartzApplication.class, args);
    }

}
