package FinalYearProject.StaffComplaintMgmtSystem.service;

import FinalYearProject.StaffComplaintMgmtSystem.Exceptions.InvalidCredentialsException;
import FinalYearProject.StaffComplaintMgmtSystem.dto.*;
import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Roles;
import FinalYearProject.StaffComplaintMgmtSystem.jwt.JwtUtil;
import FinalYearProject.StaffComplaintMgmtSystem.repository.StaffIdentityRepo;
import FinalYearProject.StaffComplaintMgmtSystem.utils.IdGenerationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class Registration {

    private final StaffIdentityRepo repo;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerationService idGenerationService;
    private final JwtUtil jwtUtil;

    public GeneralResponse registration(Registrationrequest request) {
        try {
            log.info("Registration request: {}", request);

            // Prevent duplicate email registration
            if (repo.findByStaffEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("A staff member with this email already exists");
            }

            StaffIdentity staffIdentity = new StaffIdentity();
            staffIdentity.setStaffId(idGenerationService.StaffIdGeneration(request.getEmail()));
            staffIdentity.setStaffFirstName(request.getFirstName());
            staffIdentity.setStaffLastName(request.getLastName());
            staffIdentity.setStaffEmail(request.getEmail());
            staffIdentity.setStaffPhoneNumber(request.getPhoneNumber());
            staffIdentity.setStaffAddress(request.getAddress());
            staffIdentity.setStaffPassword(passwordEncoder.encode(request.getPassword()));

            Roles resolvedRole;
            try {
                resolvedRole = Roles.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role provided: " + request.getRole() + ". Valid roles: LECTURER, HOD, DEAN, PROVOST");
            }
            staffIdentity.setRole(resolvedRole);

            repo.save(staffIdentity);
            return new GeneralResponse("You Have Been Successfully Registered", LocalDateTime.now().toString());

        } catch (RuntimeException e) {
            log.error("Registration error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected registration error: {}", e.getMessage());
            throw new RuntimeException("An error occurred while creating the staff identity", e);
        }
    }

    public GeneralResponse getAllStaff() {
        try {
            log.info("getAllStaff Process has begun");
            List<StaffIdentity> allStaff = repo.findAll();
            log.info("Found {} staff members", allStaff.size());
            return new GeneralResponse("All Staff retrieved successfully. Count: " + allStaff.size(), LocalDateTime.now().toString());
        } catch (Exception e) {
            log.error("Error fetching all staff: {}", e.getMessage());
            throw new RuntimeException("An error occurred while getting the staff identity", e);
        }
    }

    public GeneralResponse updateStaffIdentity(UpdateStaffRequest request) {
        log.info("updateStaffIdentity Process has begun");

        if (request.getStaffId() == null || request.getStaffId().isBlank()) {
            throw new RuntimeException("StaffId needs to be provided");
        }

        StaffIdentity staffIdentity = repo.findByStaffId(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + request.getStaffId()));

        boolean updated = false;

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            staffIdentity.setStaffEmail(request.getEmail());
            updated = true;
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            staffIdentity.setStaffPhoneNumber(request.getPhoneNumber());
            updated = true;
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            staffIdentity.setStaffFirstName(request.getFirstName());
            updated = true;
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            staffIdentity.setStaffLastName(request.getLastName());
            updated = true;
        }

        // BUG FIX: was incorrectly calling setStaffLastName() for address
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            staffIdentity.setStaffAddress(request.getAddress());
            updated = true;
        }

        if (!updated) {
            throw new RuntimeException("No valid fields provided for update");
        }

        repo.save(staffIdentity);

        return new GeneralResponse("Staff details updated successfully", LocalDateTime.now().toString());
    }

    public GeneralResponse deleteStaffIdentity(DeleteStaffRequest deleteStaffRequest) {
        log.info("deleteStaffIdentity Process has begun");

        if (deleteStaffRequest.getStaffId() == null || deleteStaffRequest.getStaffId().isBlank()) {
            throw new RuntimeException("StaffId needs to be provided");
        }

        String email = deleteStaffRequest.getEmail();
        String phone = deleteStaffRequest.getPhoneNumber();

        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();

        if (!hasEmail && !hasPhone) {
            throw new RuntimeException("Provide either Email or Phone Number to verify identity");
        }

        Optional<StaffIdentity> userOptional;

        if (hasEmail) {
            userOptional = repo.findByStaffIdAndStaffEmail(deleteStaffRequest.getStaffId(), email);
        } else {
            userOptional = repo.findByStaffIdAndStaffPhoneNumber(deleteStaffRequest.getStaffId(), phone);
        }

        StaffIdentity user = userOptional
                .orElseThrow(() -> new RuntimeException("Staff details do not match. Verify staffId and credentials."));

        repo.delete(user);
        return new GeneralResponse("Staff has been successfully deleted", LocalDateTime.now().toString());
    }

    public ResponseEntity<AuthenticationResponse> LogIn(LoginRequest logInRequest) {
        log.info("LogIn Process Has started for: {}", logInRequest.getEmail());

        if (logInRequest.getEmail() == null || logInRequest.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (logInRequest.getPassword() == null || logInRequest.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        Optional<StaffIdentity> doesUserExist = repo.findByStaffEmail(logInRequest.getEmail());

        if (doesUserExist.isPresent()) {
            StaffIdentity getAcct = doesUserExist.get();

            if (passwordEncoder.matches(logInRequest.getPassword(), getAcct.getStaffPassword())) {
                String jwtToken = jwtUtil.generateToken(getAcct);

                Collection<? extends GrantedAuthority> authorities = getAcct.getAuthorities();
                List<String> roles = authorities.stream()
                        .map(authority -> "ROLE_" + authority.getAuthority())
                        .collect(Collectors.toList());

                UserInfoDTO userDto = new UserInfoDTO(
                        getAcct.getId(),
                        getAcct.getUsername(),
                        getAcct.getStaffEmail(),
                        roles
                );

                List<Object> user = new ArrayList<>();
                user.add(userDto);

                return ResponseEntity.ok(
                        new AuthenticationResponse(jwtToken, "You have logged in successfully", user)
                );
            }

            // Wrong password — return 401 instead of 200
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponse(null, "Invalid Email or Password", null));
        }

        throw new InvalidCredentialsException();
    }

    public ResponseEntity<GeneralResponse> LogOut(HttpServletResponse response) {
        log.info("LogOut Process Has started at: {}", LocalDateTime.now());

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // set true in production
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.setHeader("Set-Cookie", cookie.toString());

        return new ResponseEntity<>(
                new GeneralResponse("You have logged out successfully", LocalDateTime.now().toString()),
                HttpStatus.OK
        );
    }
}
