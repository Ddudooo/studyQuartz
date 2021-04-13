package study.studyquartz.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import study.studyquartz.batch.job.SampleJob;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class QuartzJob extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final SampleJob sampleJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("JOB START!");
            String jobName = context.getJobDetail().getKey().getName();
            log.info("{} started!", jobName);
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            JobParameters jobParameter = new JobParametersBuilder()
                .addString("JobID", now.format(DateTimeFormatter.ofPattern("YYYYMMddHHmmss")))
                .toJobParameters();
            jobLauncher.run(sampleJob.sampleSpringJob(), jobParameter);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("JOB FINISH!");
        }
    }
}
