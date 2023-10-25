package org.example.executor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.example.model.Job;
import org.example.model.PeriodExecution;

public class JobExecutor {

    private final ScheduledExecutorService executorService;
    private final ConcurrentHashMap<Job, ScheduledFuture<?>> jobHolder;

    public JobExecutor(int threadCount) {
        this.executorService = Executors.newScheduledThreadPool(threadCount);
        this.jobHolder = new ConcurrentHashMap<>();
    }


    @SneakyThrows
    public void startJob(Job job) {
        if (PeriodExecution.DEFAULT == job.getPeriodExecution()) {
            ScheduledFuture<?> future = executorService.schedule(job.getTask(), 0, TimeUnit.MINUTES);
            jobHolder.put(job, future);

        } else {
            ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(job.getTask(), 0, job
                    .getPeriodExecution()
                    .getTime(), TimeUnit.HOURS);
            jobHolder.put(job, scheduledFuture);

        }
    }

    public Job cancelJob(Job job) {
        ScheduledFuture<?> scheduledFuture = jobHolder.get(job);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(Boolean.TRUE);

            jobHolder.remove(job);
            return Job
                    .builder()
                    .jobBuild(job);
        }
        return job;
    }
}
