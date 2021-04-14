package study.studyquartz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import study.studyquartz.batch.QuartzJob;
import study.studyquartz.dto.JobRequest;
import study.studyquartz.dto.JobResponse;
import study.studyquartz.dto.JobStatusResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final SchedulerFactoryBean schedulerFactoryBean;

    private Scheduler scheduler;

    @PostConstruct
    public void init() {
        scheduler = schedulerFactoryBean.getScheduler();
    }


    public void createJob(JobRequest jobRequest) throws SchedulerException {
        JobDetail jobDetail = createJobDetail(jobRequest.getJobName(), jobRequest.getJobGroup());
        Trigger trigger = createTrigger(jobRequest);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private JobDetail createJobDetail(String jobName, String jobGroup) {
        JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
            .withIdentity(JobKey.jobKey(jobName, jobGroup))
            .build();

        return jobDetail;
    }

    private Trigger createTrigger(JobRequest jobRequest) {
        if (jobRequest.isJobTypeSimple()) {
            return createSimpleTrigger(jobRequest.getJobName(), jobRequest.getJobGroup(),
                jobRequest.getStartDateAt());
        } else {
            return createCronTrigger(jobRequest.getJobName(), jobRequest.getJobGroup(),
                jobRequest.getCronExpression());
        }
    }

    private SimpleTrigger createSimpleTrigger(String jobName, String jobGroup,
        LocalDateTime startDateAt) {
        return (SimpleTrigger) TriggerBuilder.newTrigger()
            .withIdentity(jobName, jobGroup)
            .startAt(Date.from(startDateAt.atZone(ZoneId.of("Asia/Seoul")).toInstant()))
            .build();
    }

    private CronTrigger createCronTrigger(String jobName, String jobGroup, String cron) {
        return TriggerBuilder.newTrigger()
            .withIdentity(jobName, jobGroup)
            .withSchedule(CronScheduleBuilder.cronSchedule(cron))
            .build();
    }

    /**
     * 스케쥴러 시작
     */
    public void start() throws SchedulerException {
        if (scheduler != null && !scheduler.isStarted()) {
            scheduler.start();
        }
    }

    /**
     * 스케쥴러 종료
     */
    public void shutdown() throws SchedulerException, InterruptedException {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 스케쥴러 클리어
     */
    public void clear() throws SchedulerException {
        scheduler.clear();
    }

    /**
     * 스케쥴러 리스너 등록
     */
    public void addListener(JobListener jobListener) throws SchedulerException {
        scheduler.getListenerManager().addJobListener(jobListener);
    }

    public void deleteJob(JobRequest jobRequest) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobRequest.getJobName(), jobRequest.getJobGroup());
        try {
            boolean isDeleted = scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public JobStatusResponse getJobList() throws SchedulerException {
        try {
            List<JobResponse> jobs = new ArrayList<>();
            int numOfRunningJobs = 0;
            int numOfGroups = 0;
            int numOfAllJobs = 0;
            for (String groupName : scheduler.getJobGroupNames()) {
                log.info("groupName {}", groupName);
                numOfGroups++;
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    log.info("jobKey - {}", jobKey.toString());
                    List<? extends Trigger> triggers = getTriggersOfJob(jobKey);
                    if (CollectionUtils.isEmpty(triggers)) {
                        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                        JobResponse jobResponse = JobResponse.builder()
                            .jobName(jobKey.getName())
                            .groupName(jobKey.getGroup())
                            .build();
                        jobResponse.setJobStatus("NONE TRIGGER.");
                        jobs.add(jobResponse);
                    } else {
                        JobResponse jobResponse = JobResponse.builder()
                            .jobName(jobKey.getName())
                            .groupName(jobKey.getGroup())
                            .scheduleTime(getScheduleTime(triggers))
                            .lastFiredTime(getLastFiredTime(triggers))
                            .nextFireTime(getNextFireTime(triggers))
                            .build();
                        if (isJobRunning(jobKey)) {
                            jobResponse.setJobStatus("RUNNING");
                            numOfRunningJobs++;
                        } else {
                            String jobState = getJobState(jobKey);
                            jobResponse.setJobStatus(jobState);
                        }
                        jobs.add(jobResponse);
                    }
                    numOfAllJobs++;
                }

            }
            JobStatusResponse jobStatusResponse = new JobStatusResponse();
            jobStatusResponse.setNumOfAllJobs(numOfAllJobs);
            jobStatusResponse.setNumOfRunningJobs(numOfRunningJobs);
            jobStatusResponse.setNumOfGroups(numOfGroups);
            jobStatusResponse.setJobs(jobs);
            return jobStatusResponse;
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private String getNextFireTime(List<? extends Trigger> triggers) {
        try {
            return triggers.get(0).getNextFireTime().toString();
        } catch (Exception e) {
            //데이터 없을 경우
            return "";
        }
    }

    private String getLastFiredTime(List<? extends Trigger> triggers) {
        try {
            return triggers.get(0).getPreviousFireTime().toString();
        } catch (Exception e) {
            //데이터 없을 경우
            return "";
        }
    }

    private String getScheduleTime(List<? extends Trigger> triggers) {
        try {
            return triggers.get(0).getStartTime().toString();
        } catch (Exception e) {
            //데이터 없을 경우
            return "";
        }
    }

    private List<? extends Trigger> getTriggersOfJob(JobKey jobKey) {
        try {
            return scheduler.getTriggersOfJob(jobKey);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public boolean isJobRunning(JobKey jobKey) {
        try {
            List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler()
                .getCurrentlyExecutingJobs();
            if (currentJobs != null) {
                for (JobExecutionContext jobCtx : currentJobs) {
                    if (jobKey.getName().equals(jobCtx.getJobDetail().getKey().getName())) {
                        return true;
                    }
                }
            }
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] error occurred while checking job with jobKey : {}", jobKey,
                e);
        }
        return false;
    }

    public String getJobState(JobKey jobKey) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());

            if (triggers != null && triggers.size() > 0) {
                for (Trigger trigger : triggers) {
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    if (Trigger.TriggerState.NORMAL.equals(triggerState)) {
                        return "SCHEDULED";
                    }
                    return triggerState.name().toUpperCase();
                }
            }
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] Error occurred while getting job state with jobKey : {}",
                jobKey, e);
        }
        return null;
    }
}
