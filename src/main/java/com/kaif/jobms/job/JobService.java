
package com.kaif.jobms.job;

import com.kaif.jobms.job.dto.JobDTO;
import com.kaif.jobms.job.dto.JobDTOv2;
import com.kaif.jobms.job.dto.LocationCount;
import com.kaif.jobms.job.dto.createJobRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;

// Interface updated to include all methods from the monolith
public interface JobService {

    List<JobDTO> findAll(Long companyId);

    void createJob(createJobRequestDto createRequest);

    JobDTO getJobById(Long jobId);

    boolean deleteJobById(Long jobId);

    boolean updateJob(Long jobId, createJobRequestDto updatedJob);

    List<Job> findjobswithSorting(Long companyId, String field);

    List<Job> findJobsByLocation(Long companyId, String location);

    List<Job> findJobsByMinSalaryGreaterThan(Long companyId, Integer salary);

    List<Job> searchJobs(Long companyId, String query);

    Page<Job> findJobsWithPagination(Long companyId, int page, int pageSize);

    List<LocationCount> getLocationCounts();

    JobDTOv2 getJobByIdV2(Long jobId);
}