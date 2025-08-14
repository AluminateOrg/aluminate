package com.aluminate.aluminate_organization_backend.service.auth;

import com.aluminate.aluminate_organization_backend.config.GlobalAuth.GlobalBackendAuthClient;
import com.aluminate.aluminate_organization_backend.config.exception.InvalidEmailException;
import com.aluminate.aluminate_organization_backend.config.util.Jwt;
import com.aluminate.aluminate_organization_backend.config.util.RSAEncryptionUtil;
import com.aluminate.aluminate_organization_backend.dto.MemberDTO;
import com.aluminate.aluminate_organization_backend.dto.login.*;
import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.AdminRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.repository.OrganizationRepository;
import com.aluminate.aluminate_organization_backend.service.CustomUserDetailsService;
import com.aluminate.aluminate_organization_backend.service.csrf.CsrfTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwt jwt;
    private final CustomUserDetailsService customUserDetailsService;
    private final GlobalBackendAuthClient globalBackendAuthClient;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final CsrfTokenService csrfTokenService;


    @Value("${encryption.organization.private-key}")
    private String orgPrivateKeyENV;

    @Value("${encryption.global.public-key}")
    private String globalPublicKeyENV;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PrivateKey orgPrivateKey;
    private PublicKey globalPublicKey;

    @PostConstruct
    public void initKeys() throws Exception {
        this.orgPrivateKey = RSAEncryptionUtil.privateKeyFromPem(orgPrivateKeyENV);
        this.globalPublicKey = RSAEncryptionUtil.publicKeyFromPem(globalPublicKeyENV);
    }

    public AuthService(MemberRepository memberRepository,
                       PasswordEncoder passwordEncoder,
                       Jwt jwt,
                       CustomUserDetailsService customUserDetailsService,
                       GlobalBackendAuthClient globalBackendAuthClient,
                       AdminRepository adminRepository,
                       OrganizationRepository organizationRepository,
                          CsrfTokenService csrfTokenService
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.customUserDetailsService = customUserDetailsService;
        this.globalBackendAuthClient = globalBackendAuthClient;
        this.adminRepository = adminRepository;
        this.organizationRepository = organizationRepository;
        this.csrfTokenService = csrfTokenService;
    }

    public LoginResponse login(String email, String password) {
        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            if (userDetails instanceof Member) {
                return handleMemberLogin((Member) userDetails, password, email);
            } else if (userDetails instanceof Admin) {
                return handleAdminLogin((Admin) userDetails, password, email);
            } else {
                throw new InvalidEmailException("Invalid user type for email: " + email);
            }
        } catch (UsernameNotFoundException e) {
            return handleGlobalBackendLogin(email, password);
        }
    }

    private LoginResponse handleMemberLogin(Member member, String password, String email) {
        logger.info("Identified as member: {}", email);

        validateMember(member, password, email);

        MemberDTO memberDTO = createMemberDTO(member);
        Map<String, Object> claims = createClaims(email);

        return new LoginResponse(
                jwt.generateToken(claims, member),
                memberDTO
        );
    }

    private LoginResponse handleAdminLogin(Admin admin, String password, String email) {
        logger.info("Identified as admin: {}", email);

        validateAdmin(admin, password, email);

        AdminDTO adminDTO = createAdminDTO(admin);
        Map<String, Object> claims = createClaims(email);

        return new LoginResponse(
                jwt.generateToken(claims, admin),
                adminDTO
        );
    }
    @Transactional
    protected LoginResponse handleGlobalBackendLogin(String email, String password) {
        logger.info("User not found locally, checking global backend for email: {}", email);

        try {
            //encrypt the login request
            String encryptedRequest = RSAEncryptionUtil.encrypt(
                    objectMapper.writeValueAsString(new GlobalAuthRequest(email, password)),
                    globalPublicKey
            );

            ResponseEntity<GlobalAuthResponse> response = globalBackendAuthClient.verifyAdminCredentials(
                    encryptedRequest
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                GlobalAuthResponse globalResponse = response.getBody();
                logger.info("Global backend authentication successful for email: {}", email);

                Map<String, Object> claims = new HashMap<>();
                claims.put("email", email);

                AdminGlobalDTO adminGlobalDTO = globalResponse.getAdmin();
                if (adminGlobalDTO == null) {
                    throw new InvalidEmailException("No admin found in global backend for email: " + email);
                }
                OrganizationGlobalDTO organizationDTO = globalResponse.getOrganization();
                if (organizationDTO == null) {
                    throw new InvalidEmailException("No organization found in global backend for email: " + email);
                }


                // create a new record for the admin in the local database if needed
                Admin admin = Admin.builder()
                        .name(adminGlobalDTO.getName())
                        .email(adminGlobalDTO.getEmail())
                        .phone(adminGlobalDTO.getPhone())
                        .password(adminGlobalDTO.getPassword())
                        .build();


                Organization organization = Organization.builder()
                        .organizationName(organizationDTO.getOrganizationName())
                        .maxMemberCount(organizationDTO.getMaxMemberCount())
                        .currentMemberCount(organizationDTO.getCurrentMemberCount())
                        .status(organizationDTO.getStatus())
                        .isMembershipFree(organizationDTO.isMembershipFree())
                        .isDeleted(organizationDTO.isDeleted())
                        .admin(admin)
                        .build();

                //save
                admin.setOrganization(organization);
                adminRepository.save(admin);


                //save
                organizationRepository.save(organization);

                //create AdminDTO
                AdminDTO adminDTO = new AdminDTO(
                        adminGlobalDTO.getName(),
                        adminGlobalDTO.getEmail(),
                        adminGlobalDTO.getPhone()
                );

                return new LoginResponse(
                        jwt.generateToken(claims, admin),
                        adminDTO
                );

            }
            else{
                logger.error("Global backend authentication failed for email: {}", email);
            }
        } catch (Exception ex) {
            logger.error("Global backend authentication failed- went to catch {}", email);
            throw new RuntimeException("Global Server not reachable or error occurred while processing the request", ex);
        }

        throw new InvalidEmailException("Invalid credentials for email: " + email);
    }

    private void validateMember(Member member, String password, String email) {
        if (!member.isActive()) {
            throw new RuntimeException("Member is not active");
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            logger.error("Invalid password for email: {}", email);
            throw new RuntimeException("Invalid password");
        }

        Organization org = member.getOrganization();
        if (org == null) {
            throw new RuntimeException("Member does not belong to any organization");
        }
        if (org.getStatus() != Status.ACTIVE) {
            throw new RuntimeException("Organization is not active");
        }
    }

    private void validateAdmin(Admin admin, String password, String email) {
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            logger.error("Invalid password for email: {}", email);
            throw new RuntimeException("Invalid password");
        }

        Organization org = admin.getOrganization();
        if (org == null) {
            throw new RuntimeException("Admin does not belong to any organization");
        }
        if (org.getStatus() != Status.ACTIVE) {
            throw new RuntimeException("Organization is not active");
        }
    }

    private MemberDTO createMemberDTO(Member member) {
        return new MemberDTO(
                member.getId(),
                member.getName(),
                member.getNic(),
                member.getPhone(),
                member.getEmail(),
                member.getRegNo(),
                member.getAddress(),
                member.getPhotoUrl(),
                member.getDegree(),
                member.getCompany(),
                member.getPosition(),
                member.getLinkedinUrl(),
                member.getGithubUrl(),
                member.getWebsiteUrl(),
                member.getBatch()
        );
    }

    private AdminDTO createAdminDTO(Admin admin) {
        return new AdminDTO(
                admin.getName(),
                admin.getEmail(),
                admin.getPhone()
        );
    }

    private Map<String, Object> createClaims(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        return claims;
    }


    public void setAuthCookies(HttpServletResponse httpResponse, String token) {
        String sessionId = UUID.randomUUID().toString();
        String csrfToken = csrfTokenService.generateAndStoreToken(sessionId);

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        ResponseCookie csrfCookie = ResponseCookie.from("csrf-token", csrfToken)
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        ResponseCookie sessionCookie = ResponseCookie.from("sessionId", sessionId)
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        httpResponse.addHeader("Set-Cookie", jwtCookie.toString());
        httpResponse.addHeader("Set-Cookie", csrfCookie.toString());
        httpResponse.addHeader("Set-Cookie", sessionCookie.toString());
    }

}