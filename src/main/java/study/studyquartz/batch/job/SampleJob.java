package study.studyquartz.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import study.studyquartz.batch.job.listener.SampleListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SampleJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final SampleListener listener;

    @Bean
    @JobScope
    public Step sampleStep() {
        return stepBuilderFactory
            .get("SAMPLE STEP")
            .tasklet((contribution, chunkContext) -> {
                log.info("SAMPLE STEP - TASKLET");
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Job sampleSpringJob() {
        return jobBuilderFactory.get("샘플 스프링 배치 잡!")
            .preventRestart()
            .listener(listener)
            .start(sampleStep())
            .build();
    }
}
