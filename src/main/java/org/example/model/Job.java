package org.example.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

import static org.example.model.JobStatus.CANCELED;


@Getter
@ToString
public class Job {

    private final String jobId;
    private final PeriodExecution periodExecution;
    private volatile JobStatus status;
    private Runnable task;

    public Job(String jobId, PeriodExecution periodExecution, Runnable task, JobStatus status) {
        this.jobId = jobId;
        this.periodExecution = periodExecution;
        this.task = task;
        this.status = status;
    }

    public synchronized JobStatus getStatus() {
        return status;
    }

    private synchronized void updateStatus(JobStatus jobStatus) {
        this.status = jobStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Job job = (Job) o;
        return Objects.equals(jobId, job.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    public static JobBuilder builder() {
        return new JobBuilder();
    }

    public static class JobBuilder {

        private String jobId;
        private PeriodExecution periodExecution;
        private Runnable task;

        private JobBuilder() {
        }

        public JobBuilder jobId(String jobId) {
            this.jobId = jobId;
            return this;
        }

        public JobBuilder periodExecution(PeriodExecution periodExecution) {
            this.periodExecution = periodExecution;
            return this;
        }

        public JobBuilder task(Runnable task) {
            this.task = task;
            return this;
        }

        public Job build() {
            Job job = new Job(this.jobId, this.periodExecution, this.task, JobStatus.CREATED);
            job.task = () -> decorateTask(job);
            return job;
        }

        private void decorateTask(Job job) {
            try {
                job.updateStatus(JobStatus.RUNNING);
                this.task.run();
                if (job.periodExecution == PeriodExecution.DEFAULT) {
                    job.updateStatus(JobStatus.FINISHED);
                } else {
                    job.updateStatus(JobStatus.PENDING);
                }
            } catch (Exception e) {
                job.updateStatus(JobStatus.FAILED);
            }
        }

        public Job jobBuild(Job job) {
            this.task = job.task;
            this.jobId = job.jobId;
            this.periodExecution = job.periodExecution;

            return new Job(job.jobId, job.periodExecution, job.task, CANCELED);
        }
    }
}

