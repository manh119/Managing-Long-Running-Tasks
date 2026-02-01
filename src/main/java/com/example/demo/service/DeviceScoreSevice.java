package com.example.demo.service;

import com.example.demo.entity.DeviceScore;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceScoreSevice {
    final int MAX_RETRY = 3;

    // demo 1s call api Open API để upload device score
    public void executeUpload(DeviceScore deviceScore) {
        int currentRetry = deviceScore.getRetryCount();
        boolean success = false;

        while (currentRetry < MAX_RETRY && !success) {
            currentRetry++;
            //success = tryUpload(deviceScore, currentRetry);
            try {
                Thread.sleep(2000); // 1 second
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!success && currentRetry < MAX_RETRY) {
                //sleepInterval();
            }
        }
        deviceScore.setStatus("SUCCESS"); // INIT, SUCCESS, FAIL
        deviceScore.setRetryCount(currentRetry);
    }
}
