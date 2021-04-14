package study.studyquartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import study.studyquartz.dto.JobRequest;
import study.studyquartz.dto.JobStatusResponse;
import study.studyquartz.service.ScheduleService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JobController {

    private final ScheduleService scheduleService;

    @PostMapping("/job")
    public ResponseEntity<?> addScheduleJob(
        @RequestBody JobRequest jobRequest
    ) {
        //JobKey jobKey = new JobKey(jobRequest.getJobName(), jobRequest.getJobGroup());
        try {
            scheduleService.createJob(jobRequest);
        } catch (SchedulerException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/job")
    public ResponseEntity<?> getJobs() {
        try {
            JobStatusResponse jobList = scheduleService.getJobList();
            return ResponseEntity.ok().body(jobList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/job")
    public ResponseEntity<?> deleteJob(@RequestBody JobRequest jobRequest) {
        try {
            scheduleService.deleteJob(jobRequest);
            return ResponseEntity.accepted().build();
        } catch (SchedulerException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
