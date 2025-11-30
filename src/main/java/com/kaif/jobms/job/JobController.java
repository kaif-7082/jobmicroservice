package com.kaif.jobms.job;

import com.kaif.jobms.job.dto.JobDTO;
import com.kaif.jobms.job.dto.JobDTOv2;
import com.kaif.jobms.job.dto.LocationCount;
import com.kaif.jobms.job.dto.createJobRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kaif.jobms.job.JobConstants.*;

@Slf4j
@RestController
@RequestMapping("/jobs")
@Tag(name = "Job Controller", description = "Endpoints for managing job listings")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(summary = "Get All Jobs", description = "Fetches a list of all jobs. Optionally filters by Company ID.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<JobDTO>> findAll(@RequestParam(required = false) Long companyId) {
        log.info("GET /jobs?companyId={}", companyId);
        return ResponseEntity.ok(jobService.findAll(companyId));
    }

    @Operation(summary = "Create Job", description = "Creates a new job posting. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> createJob(@Valid @RequestBody createJobRequestDto createRequest) {
        log.info("POST /jobs - Request to create new job: {}", createRequest.getTitle());
        jobService.createJob(createRequest);
        return new ResponseEntity<>("Job created", HttpStatus.CREATED);
    }

    @Operation(summary = "Get Job by ID", description = "Fetches details of a specific job by its unique ID.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(ID_PATH)
    public ResponseEntity<JobDTO> findJobById(@PathVariable Long jobId) {
        log.info("GET /jobs/{}", jobId);
        JobDTO jobDto = jobService.getJobById(jobId);
        if (jobDto != null)
            return new ResponseEntity<>(jobDto, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Delete Job", description = "Deletes a job posting by its ID. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(ID_PATH)
    public ResponseEntity<String> deleteJobById(@PathVariable Long jobId) {
        log.info("DELETE /jobs/{}", jobId);
        boolean deleted = jobService.deleteJobById(jobId);
        if (deleted) return new ResponseEntity<>("Job deleted", HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Update Job", description = "Updates an existing job posting. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(ID_PATH)
    public ResponseEntity<String> updateJob(@PathVariable Long jobId, @Valid @RequestBody createJobRequestDto updatedJob) {
        log.info("PUT /jobs/{}", jobId);
        boolean updated = jobService.updateJob(jobId, updatedJob);
        if (updated) return new ResponseEntity<>("Job updated", HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get Sorted Jobs", description = "Fetches jobs sorted by a specific field (e.g., minSalary, title).")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(SORTED_PATH)
    public ResponseEntity<List<Job>> findSortedJobs(@RequestParam(required = false) Long companyId, @PathVariable String field) {
        return ResponseEntity.ok(jobService.findjobswithSorting(companyId, field));
    }

    @Operation(summary = "Filter Jobs by Location", description = "Fetches jobs located in a specific city or region.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(LOCATION_PATH)
    public ResponseEntity<List<Job>> findJobsByLocation(@RequestParam(required = false) Long companyId, @PathVariable String location) {
        return ResponseEntity.ok(jobService.findJobsByLocation(companyId, location));
    }

    @Operation(summary = "Filter Jobs by Salary", description = "Fetches jobs with a minimum salary greater than the specified amount.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(SALARY_PATH)
    public ResponseEntity<List<Job>> findJobsByMinSalaryGreaterThan(@RequestParam(required = false) Long companyId, @RequestParam Integer min) {
        return ResponseEntity.ok(jobService.findJobsByMinSalaryGreaterThan(companyId, min));
    }

    @Operation(summary = "Search Jobs", description = "Searches for jobs matching a keyword in title, description, or location.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(SEARCH_PATH)
    public ResponseEntity<List<Job>> searchJobs(@RequestParam Long companyId, @RequestParam String query) {
        return ResponseEntity.ok(jobService.searchJobs(companyId, query));
    }

    @Operation(summary = "Get Paginated Jobs", description = "Fetches jobs in pages. Useful for large lists.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(PAGINATION_PATH)
    public ResponseEntity<Page<Job>> getJobsWithPagination(@RequestParam(required = false) Long companyId, @PathVariable int page, @PathVariable int pageSize) {
        return ResponseEntity.ok(jobService.findJobsWithPagination(companyId, page, pageSize));
    }

    @Operation(summary = "Get Location Stats", description = "Returns a count of jobs available in each location.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(STATS_LOCATION_PATH)
    public ResponseEntity<List<LocationCount>> getLocationCounts() {
        return ResponseEntity.ok(jobService.getLocationCounts());
    }

    @Operation(summary = "Get Job by ID (V2)", description = "Fetches job details with the new Salary Structure (Currency/Frequency).")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/v2/{jobId}")
    public ResponseEntity<JobDTOv2> findJobByIdV2(@PathVariable Long jobId) {
        JobDTOv2 jobDto = jobService.getJobByIdV2(jobId);
        if (jobDto != null) return new ResponseEntity<>(jobDto, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}