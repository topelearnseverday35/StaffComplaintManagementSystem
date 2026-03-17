package FinalYearProject.StaffComplaintMgmtSystem.service;

import FinalYearProject.StaffComplaintMgmtSystem.Exceptions.InvalidCredentialsException;
import FinalYearProject.StaffComplaintMgmtSystem.dto.*;
import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Roles;
import FinalYearProject.StaffComplaintMgmtSystem.jwt.JwtUtil;
import FinalYearProject.StaffComplaintMgmtSystem.repository.StaffIdentityRepo;
import FinalYearProject.StaffComplaintMgmtSystem.utils.IdGenerationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
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
//    private final UserInfoRepo userInfoRepo;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerationService idGenerationService;
    private final JwtUtil jwtUtil;


    public GeneralResponse registration(Registrationrequest request) {
        try {
            log.info("Registration request: " + request);
            log.info("Registration Process has begun");


            StaffIdentity staffIdentity = new StaffIdentity();
            staffIdentity.setStaffId(idGenerationService.StaffIdGeneration(request.getEmail()));
            staffIdentity.setStaffFirstName(request.getFirstName());
            staffIdentity.setStaffLastName(request.getLastName());
            staffIdentity.setStaffEmail(request.getEmail());
            staffIdentity.setStaffPhoneNumber(request.getPhoneNumber());
            staffIdentity.setStaffAddress(request.getAddress());
            staffIdentity.setStaffPassword(passwordEncoder.encode(request.getPassword()));
            if(request.getRole().equalsIgnoreCase("HOD")) {
                staffIdentity.setRole(Roles.HOD);
            }
            if(request.getRole().equalsIgnoreCase("LECTURER")) {
                staffIdentity.setRole(Roles.LECTURER);
            }
            if(request.getRole().equalsIgnoreCase("DEAN")) {
                staffIdentity.setRole(Roles.DEAN);
            }
            if(request.getRole().equalsIgnoreCase("PROVOST")) {
                staffIdentity.setRole(Roles.PROVOST);
            }
            repo.save(staffIdentity);
            return new GeneralResponse("You Have Been Successfully Registered", LocalDateTime.now().toString());

        } catch (Exception e){
            log.error("AN ERROR  OCCURRED - {}", e.getMessage());
            throw new RuntimeException("An error occurred while creating the staff identity - {}",e);
        }
    }

    public GeneralResponse getAllStaff() {
        try {
            log.info("getAllStaff Process has begun");

              repo.findAll();
              return new GeneralResponse("All Staff Identity has been Successfully", LocalDateTime.now().toString());
        }
        catch (Exception e){
            log.error("AN ERROR` OCCURRED - {}", e.getMessage());
            throw new RuntimeException("An error occurred while getting the staff identity - {}",e);
        }
    }

    public GeneralResponse updateStaffIdentity(UpdateStaffRequest request) {

        log.info("updateStaffIdentity Process has begun");

        // ✅ 1. Validate staffId
        if (request.getStaffId() == null || request.getStaffId().isBlank()) {
            throw new RuntimeException("StaffId needs to be provided");
        }

        // ✅ 2. Fetch user
        StaffIdentity staffIdentity = repo.findByStaffId(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // ✅ 3. Track if anything changed (nice professional touch)
        boolean updated = false;

        // ✅ 4. Update fields safely

        // Email
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            staffIdentity.setStaffEmail(request.getEmail());
            updated = true;
        }

        // Phone
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            staffIdentity.setStaffPhoneNumber(request.getPhoneNumber());
            updated = true;
        }

        // First Name
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            staffIdentity.setStaffFirstName(request.getFirstName());
            updated = true;
        }

        // Last Name
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            staffIdentity.setStaffLastName(request.getLastName());
            updated = true;
        }

        // Address
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            staffIdentity.setStaffLastName(request.getAddress());
            updated = true;
        }

        // ✅ 5. Prevent empty update request (very professional)
        if (!updated) {
            throw new RuntimeException("No valid fields provided for update");
        }

        // ✅ 6. Save
        repo.save(staffIdentity);

        // ✅ 7. Response (your template style)
        return new GeneralResponse(
                "Staff details updated successfully",
                LocalDateTime.now().toString()
        );
    }
    public GeneralResponse deleteStaffIdentity(DeleteStaffRequest deleteStaffRequest) {
//        try {
//            log.info("deleteStaffIdentity Process has begun");
//
//            Optional<UserInfo> findByStaffId = userInfoRepo.findByStaffId(deleteStaffRequest.getStaffId());
//            Optional<UserInfo> matchIdAndEmail = userInfoRepo.findByStaffIdAndEmail(deleteStaffRequest.getStaffId(), deleteStaffRequest.getEmail());
//            Optional<UserInfo>matchIdAndPhoneNumber = userInfoRepo.findByStaffIdAndPhoneNumber(deleteStaffRequest.getStaffId(), deleteStaffRequest.getPhoneNumber());
//
//            if (!(deleteStaffRequest.getStaffId().isBlank())) {
//                if(findByStaffId.isPresent()) {
//                    if(!(deleteStaffRequest.getEmail().isBlank()&& deleteStaffRequest.getPhoneNumber().isBlank())){
//                        if (!(deleteStaffRequest.getEmail().isBlank())) {
//                            if(matchIdAndEmail.isPresent()) {
//                                userInfoRepo.delete(matchIdAndEmail.get());
//                                return new GeneralResponse("Staff  has been Successfully deleted", LocalDateTime.now().toString());
//                            }
//                            throw new RuntimeException("Invalid Email or StaffId");
//                        }
//                            if(matchIdAndPhoneNumber.isPresent()) {
//                                userInfoRepo.delete(matchIdAndPhoneNumber.get());
//                                return new GeneralResponse("Staff  has been Successfully deleted", LocalDateTime.now().toString());
//                            }
//                            throw new RuntimeException("Invalid PhoneNumber or StaffId");
//                    }
//                    throw new RuntimeException("Email and Phone Number cannot be empty");
//
//            }
//                throw new RuntimeException("Staff Id Not Found");
//
//            }
//            throw new RuntimeException("StaffId Needs to be provided");
//        }
//        catch (Exception e){
//            log.error("AN ERROR OCCURRED - {}", e.getMessage());
//        }

            log.info("deleteStaffIdentity Process has begun");

            // ✅ 1. Validate staffId
            if (deleteStaffRequest.getStaffId() == null || deleteStaffRequest.getStaffId().isBlank()) {
                throw new RuntimeException("StaffId needs to be provided");
            }

            String email = deleteStaffRequest.getEmail();
            String phone = deleteStaffRequest.getPhoneNumber();

            boolean hasEmail = email != null && !email.isBlank();
            boolean hasPhone = phone != null && !phone.isBlank();

            // ✅ 2. Ensure one identifier exists
            if (!hasEmail && !hasPhone) {
                throw new RuntimeException("Provide either Email or Phone Number");
            }

            Optional<StaffIdentity> userOptional;

            // ✅ 3. Query smartly
            if (hasEmail) {
                userOptional = repo.findByStaffIdAndStaffEmail(deleteStaffRequest.getStaffId(), email);
            } else {
                userOptional = repo.findByStaffIdAndStaffPhoneNumber(deleteStaffRequest.getStaffId(), phone);
            }
            StaffIdentity user = userOptional
                    .orElseThrow(() -> new RuntimeException("Staff details do not match"));

            repo.delete(user);
            return new GeneralResponse("Staff has been successfully deleted", LocalDateTime.now().toString());
        }

    public ResponseEntity<AuthenticationResponse> LogIn(LoginRequest logInRequest) {
        log.info("LogIn Process Has started");
        log.info("LogIn request::::::::::::: {}", logInRequest);

        Optional<StaffIdentity> doesUserExist = repo.findByStaffEmail(logInRequest.getEmail());
        if(doesUserExist.isPresent()) {
            StaffIdentity getAcct = doesUserExist.get();
            if (passwordEncoder.matches(logInRequest.getPassword(), getAcct.getStaffPassword())) {
                String jwtToken = jwtUtil.generateToken(getAcct);

                Collection<? extends GrantedAuthority> authorities = getAcct.getAuthorities();
                List<String> roles = authorities.stream()
                        .map(authority -> "ROLE_" + authority.getAuthority())
                        .collect(Collectors.toList());

                // Construct user DTO
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
            return  ResponseEntity.ok()
                    .body(new AuthenticationResponse("Invalid Email or Password", LocalDateTime.now().toString(),null));
        }

        throw new InvalidCredentialsException();

    }
    public ResponseEntity<GeneralResponse> LogOut(HttpServletResponse response) {
        log.info("LogOut Process Has started");
        log.info("LogOut request::::::::::::: {}", LocalDateTime.now().toString());

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) //true in production
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.setHeader("Set-Cookie", cookie.toString());
        return new ResponseEntity<>(new GeneralResponse("You have logged out", LocalDateTime.now().toString()), HttpStatus.OK);
    }
    }


