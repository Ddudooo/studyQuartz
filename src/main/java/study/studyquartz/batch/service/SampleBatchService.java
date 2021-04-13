package study.studyquartz.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SampleBatchService {

    public void executeSample() {
        log.info("SAMPLE SERVICE EXECUTE!");
    }
}
