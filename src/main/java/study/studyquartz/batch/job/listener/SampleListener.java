package study.studyquartz.batch.job.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.batch.core.BatchStatus.COMPLETED;
import static org.springframework.batch.core.BatchStatus.FAILED;
import static org.springframework.batch.core.BatchStatus.STARTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SampleListener extends JobExecutionListenerSupport {

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == COMPLETED) {
            log.info("스프링 배치 잡 종료 - {}", jobExecution.getEndTime());
        }
        if (jobExecution.getStatus() == FAILED) {
            log.info("스프링 배치 잡 실패.");
            List<Throwable> exceptions = jobExecution.getAllFailureExceptions();
            for (Throwable t : exceptions) {
                log.error(t.getMessage());
            }
        }
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == STARTED) {
            log.info("스프링 배치 잡 시작 {}", jobExecution.getJobParameters());
        }
    }
}
