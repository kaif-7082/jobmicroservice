package com.kaif.jobms.job.implementation;

import com.kaif.jobms.Exception.CompanyNotFoundException;
import com.kaif.jobms.job.Job;
import com.kaif.jobms.job.JobRepository;
import com.kaif.jobms.job.clients.CompanyClient;
import com.kaif.jobms.job.dto.JobDTO;
import com.kaif.jobms.job.dto.createJobRequestDto;
import com.kaif.jobms.job.external.Company;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.kaif.jobms.job.JobNotificationService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplementationTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyClient companyClient;

    @InjectMocks
    private JobServiceImplementation jobService;
    @Mock // <--- 1. Add this mock
    private JobNotificationService jobNotificationService;

    // --- TEST 1: Create Job (Success) ---
    @Test
    void testCreateJob_Success() {
        // 1. ARRANGE
        createJobRequestDto request = new createJobRequestDto();
        request.setCompanyId(1L);
        request.setTitle("Developer");
        request.setDescription("Code stuff");
        request.setLocation("Remote");
        request.setMinSalary(500);
        request.setMaxSalary(1000);

        Company mockCompany = new Company();
        mockCompany.setId(1L);

        // Stubbing
        when(companyClient.getCompany(1L)).thenReturn(mockCompany);


        // 2. ACT
        jobService.createJob(request);

        // 3. ASSERT
        verify(companyClient, times(1)).getCompany(1L);
        verify(jobRepository, times(1)).save(any(Job.class));

        // Optional: Verify the notification was sent
        verify(jobNotificationService, times(1)).notifyCompany(anyString(), eq(1L));
    }

    // --- TEST 2: Create Job (Company Not Found) ---
    @Test
    void testCreateJob_CompanyNotFound() {
        // 1. ARRANGE
        createJobRequestDto request = new createJobRequestDto();
        request.setCompanyId(99L); // Non-existent ID

        // Stub: Throw 404 Not Found when calling company client
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(companyClient.getCompany(99L)).thenThrow(notFound);

        // 2. ACT & ASSERT
        // Expect a CompanyNotFoundException
        assertThrows(CompanyNotFoundException.class, () -> jobService.createJob(request));

        // Verify we NEVER saved the job because validation failed
        verify(jobRepository, never()).save(any(Job.class));
    }

    // --- TEST 3: Get Job By ID (Success) ---
    @Test
    void testGetJobById_Success() {
        // 1. ARRANGE
        Long jobId = 100L;
        Job mockJob = new Job();
        mockJob.setId(jobId);
        mockJob.setTitle("Tester");
        mockJob.setCompanyId(1L);

        Company mockCompany = new Company();
        mockCompany.setId(1L);
        mockCompany.setName("Test Corp");

        // Stub Repository
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(mockJob));
        // Stub Feign Client
        when(companyClient.getCompany(1L)).thenReturn(mockCompany);

        // 2. ACT
        JobDTO result = jobService.getJobById(jobId);

        // 3. ASSERT
        assertNotNull(result);
        assertEquals("Tester", result.getTitle());
        assertNotNull(result.getCompany());
        assertEquals("Test Corp", result.getCompany().getName());
    }

    // --- TEST 4: Get Job By ID (Job Not Found) ---
    @Test
    void testGetJobById_NotFound() {
        // 1. ARRANGE
        when(jobRepository.findById(50L)).thenReturn(Optional.empty());

        // 2. ACT
        JobDTO result = jobService.getJobById(50L);

        // 3. ASSERT
        assertNull(result);
    }

    // --- TEST 5: Delete Job (Success) ---
    @Test
    void testDeleteJob_Success() {
        // 1. ARRANGE
        Long jobId = 10L;
        // No exceptions means success for void methods in mocks

        // 2. ACT
        boolean result = jobService.deleteJobById(jobId);

        // 3. ASSERT
        assertTrue(result);
        verify(jobRepository, times(1)).deleteById(jobId);
    }

    // --- TEST 6: Update Job (Success) ---
    @Test
    void testUpdateJob_Success() {
        // 1. ARRANGE
        Long jobId = 5L;
        createJobRequestDto updateRequest = new createJobRequestDto();
        updateRequest.setTitle("Updated Title");
        updateRequest.setCompanyId(1L); // Same company ID

        Job existingJob = new Job();
        existingJob.setId(jobId);
        existingJob.setTitle("Old Title");
        existingJob.setCompanyId(1L);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(existingJob));

        // 2. ACT
        boolean result = jobService.updateJob(jobId, updateRequest);

        // 3. ASSERT
        assertTrue(result);
        // Verify the job object was actually updated before saving
        assertEquals("Updated Title", existingJob.getTitle());
        verify(jobRepository, times(1)).save(existingJob);
    }
}