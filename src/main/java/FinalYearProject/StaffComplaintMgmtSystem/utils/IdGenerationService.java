package FinalYearProject.StaffComplaintMgmtSystem.utils;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
@Service
@Slf4j
@RequiredArgsConstructor
public class IdGenerationService {

    public String StaffIdGeneration(String email) {
        try {
            SecureRandom rand = new SecureRandom();
            int max = 9999;
            int min = 1000;
            int randomNumber = rand.nextInt((max - min) + 1) + min;
            log.info("Generating Number Part of StaffId {}", randomNumber);
            String numberPart = String.valueOf(randomNumber);
            String stringPart = email.substring(0, 2);
            String staffId = stringPart.toUpperCase() + numberPart;
            log.info("Generated StaffId - {}", staffId);
            return staffId;


        } catch (Exception e) {
            log.error("Error Generating staffId : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }
}
