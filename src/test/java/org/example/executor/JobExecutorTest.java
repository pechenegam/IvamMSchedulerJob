package org.example.executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.example.model.Job;
import org.example.model.JobStatus;
import org.example.model.PeriodExecution;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class JobExecutorTest {
    @Test
    void jobDefault() throws InterruptedException {
        JobExecutor jobExecutor = new JobExecutor(1);
        final CountDownLatch firstCountDownLatch = new CountDownLatch(1);
        final CountDownLatch secondCountDownLatch = new CountDownLatch(1);
        Job job = Job.builder()
                .periodExecution(PeriodExecution.DEFAULT)
                .task(() -> {
                    firstCountDownLatch.countDown();
                    try {
                        secondCountDownLatch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        assertEquals(JobStatus.CREATED, job.getStatus());
        jobExecutor.startJob(job);
        firstCountDownLatch.await();
        assertEquals(JobStatus.RUNNING, job.getStatus());
        secondCountDownLatch.countDown();
        assertTimeout(Duration.ofSeconds(2), () -> {
            while (job.getStatus() != JobStatus.FINISHED) {
                Thread.sleep(100);
            }
        });

        assertEquals(JobStatus.FINISHED, job.getStatus());
    }

    @Test
    void scheduleJob()  throws InterruptedException, ExecutionException {
        JobExecutor jobExecutor = new JobExecutor(1);
        final CountDownLatch firstCountDownLatch = new CountDownLatch(1);
        final CountDownLatch secondCountDownLatch = new CountDownLatch(1);
        CompletableFuture<Void> jobFuture = new CompletableFuture<>();
        Job job = Job.builder()
                .periodExecution(PeriodExecution.SIX)
                .task(() -> {
                    firstCountDownLatch.countDown();
                    try {
                        secondCountDownLatch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    jobFuture.complete(null);
                })
                .build();
        assertEquals(JobStatus.CREATED, job.getStatus());
        jobExecutor.startJob(job);
        firstCountDownLatch.await();
        assertEquals(JobStatus.RUNNING, job.getStatus());
        secondCountDownLatch.countDown();
        jobFuture.get();
        assertEquals(JobStatus.PENDING, job.getStatus());

    }

}

