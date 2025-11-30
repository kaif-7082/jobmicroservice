package com.kaif.jobms.job.implementation;

import com.kaif.jobms.job.Job;
import com.kaif.jobms.job.JobRepository;
import com.kaif.jobms.job.JobService;
import com.kaif.jobms.job.dto.*;
import com.kaif.jobms.job.external.Company;
import com.kaif.jobms.job.mapper.JobMapper; // Import your manual mapper
import com.kaif.jobms.Exception.CompanyNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.kaif.jobms.job.clients.CompanyClient;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.kaif.jobms.job.JobNotificationService;

@Slf4j
@Service
@Transactional // Keeps your data consistent
public class JobServiceImplementation implements JobService {

    private JobRepository jobRepository;


    private CompanyClient companyClient;


    private JobNotificationService jobNotificationService;

    public JobServiceImplementation(JobRepository jobRepository, CompanyClient companyClient,JobNotificationService jobNotificationService) {
        this.jobRepository = jobRepository;
        this.companyClient = companyClient;
        this.jobNotificationService = jobNotificationService;
    }

    @Override
    public List<JobDTO> findAll(Long companyId) {
        log.info("Executing findAll jobs");
        List<Job> jobs;
        if (companyId != null) {
            log.info("Filtering by companyId: {}", companyId);
            jobs = jobRepository.findByCompanyId(companyId);
        } else {
            jobs = jobRepository.findAll();
        }
        log.info("Found {} jobs", jobs.size());
        return jobs.stream().map(this::converttoDto).collect(Collectors.toList());
    }

    private JobDTO converttoDto(Job job) {
        Company company = null;
        try {
            company = companyClient.getCompany(job.getCompanyId());
        } catch (Exception e) {
            log.warn("Could not fetch company details for job {}: {}", job.getId(), e.getMessage());
        }

        return JobMapper.mapToJobWithCompanyDTO(job, company);
    }

    @Override
    @CircuitBreaker(name="companyBreaker")
    public void createJob(createJobRequestDto createRequest) {
        log.info("Attempting to create job for company: {}", createRequest.getCompanyId());

        try {
            // Step 1: Validate Company using Feign Client
            // If the company doesn't exist, Feign will throw an exception
            companyClient.getCompany(createRequest.getCompanyId());

        } catch (feign.FeignException.NotFound e) {
            // This catches 404 Not Found specifically
            throw new CompanyNotFoundException("Company not found with id: " + createRequest.getCompanyId());
        } catch (Exception e) {
            // Catches connection refused, 500 errors, etc.
            log.error("Error communicating with company service", e);
            throw new RuntimeException("Error communicating with company service", e);
        }

        // Step 2: Create Job using manual mapping
        Job job = mapToEntity(createRequest);
        jobRepository.save(job);
        log.info("Successfully created job with id: {}", job.getId());

        // 3. TRIGGER BACKGROUND TASK
        // This happens instantly. The user gets the response immediately.
        // The "email" will send 5 seconds later in the background.
        jobNotificationService.notifyCompany(job.getTitle(), job.getCompanyId());
    }
    @Override
    public JobDTO getJobById(Long jobId) {
        log.info("Finding job by id: {}", jobId);
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            return converttoDto(job);
        }
        log.warn("Job not found: {}", jobId);
        return null;
    }

    @Override
    public boolean deleteJobById(Long jobId) {
        log.info("Attempting to delete job with id: {}", jobId);
        try {
            jobRepository.deleteById(jobId);
            log.info("Successfully deleted job with id: {}", jobId);
            return true;
        } catch (Exception e) {
            log.warn("Failed to delete job {}: {}", jobId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateJob(Long jobId, createJobRequestDto updatedJob) {
        log.info("Attempting to update job with id: {}", jobId);
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            log.warn("Job not found: {}", jobId);
            return false;
        }

        Job job = jobOpt.get();

        // Manual update of fields
        job.setTitle(updatedJob.getTitle());
        job.setDescription(updatedJob.getDescription());
        job.setMinSalary(updatedJob.getMinSalary());
        job.setMaxSalary(updatedJob.getMaxSalary());
        job.setLocation(updatedJob.getLocation());

        jobRepository.save(job);
        log.info("Successfully updated job with id: {}", jobId);
        return true;
    }


    @Override
    public List<Job> findjobswithSorting(Long companyId, String field) {
        log.info("Finding jobs for company {} with sorting on field: {}", companyId, field);
        Sort sort = Sort.by(Sort.Direction.DESC, field);
        return jobRepository.findByCompanyId(companyId, sort);
    }

    @Override
    public List<Job> findJobsByLocation(Long companyId, String location) {
        log.info("Finding jobs for company {} by location: {}", companyId, location);
        return jobRepository.findByCompanyIdAndLocation(companyId, location);
    }

    @Override
    public List<Job> findJobsByMinSalaryGreaterThan(Long companyId, Integer salary) {
        log.info("Finding jobs for company {} with min salary > {}", companyId, salary);
        return jobRepository.findByCompanyIdAndMinSalaryGreaterThan(companyId, salary);
    }

    @Override
    public List<Job> searchJobs(Long companyId, String query) {
        log.info("Searching jobs for company {} with query: {}", companyId, query);
        return jobRepository.searchJobsByCompany(companyId, query);
    }

    @Override
    public Page<Job> findJobsWithPagination(Long companyId, int page, int pageSize) {
        log.info("Finding jobs for company {} with pagination - page: {}, pageSize: {}", companyId, page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize);
        return jobRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    public List<LocationCount> getLocationCounts() {
        log.info("Getting global location stats");
        return jobRepository.getLocationCounts();
    }



    private Job mapToEntity(createJobRequestDto createRequest) {
        Job job = new Job();
        job.setTitle(createRequest.getTitle());
        job.setDescription(createRequest.getDescription());
        job.setMinSalary(createRequest.getMinSalary());
        job.setMaxSalary(createRequest.getMaxSalary());
        job.setLocation(createRequest.getLocation());
        job.setCompanyId(createRequest.getCompanyId());
        return job;
    }

    @Override
    public JobDTOv2 getJobByIdV2(Long jobId) {
        log.info("Finding job (v2) by id: {}", jobId);

        // 1. Fetch Data (Same as before)
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return null;

        Company company = null;
        try {
            company = companyClient.getCompany(job.getCompanyId());
        } catch (Exception e) { /* ignore */ }

        // 2. Map to New Structure (Manual Mapping)
        JobDTOv2 dto = new JobDTOv2();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        dto.setCompany(company);

        // 3. Create the Complex Salary Object
        Compensation comp = new Compensation();
        comp.setMinimum(job.getMinSalary());
        comp.setMaximum(job.getMaxSalary());
        comp.setCurrency("USD");    // Default value
        comp.setFrequency("YEARLY"); // Default value

        dto.setCompensation(comp);

        return dto;
    }
}