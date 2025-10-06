package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.*;
import com.example.bulksmsAPI.Models.DTO.AuthResponse;
import com.example.bulksmsAPI.Models.DTO.BillingAddressDTO;
import com.example.bulksmsAPI.Models.DTO.PendingUserValidationDTO;
import com.example.bulksmsAPI.Models.DTO.profileDTO;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.ResetTokenRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import com.example.bulksmsAPI.Security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResetTokenRepository resetTokenRepository;
    private final String FRONTEND_URL = "http://localhost:4200/reset-password";



    private static final String CLIENT_ID = "790410421867-v8dtetqbt6tfk4eoip774ciojbmfoo5r.apps.googleusercontent.com";

    public User registerUser(String email, String password, String driverLicense, BillingAddress billingAddress, LegalIdType legalIdType, String legalIdNumber) throws IOException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles("USER");

        // Si se proporciona legalIdType y legalIdNumber, usarlos
        if (legalIdType != null && legalIdNumber != null && !legalIdNumber.isEmpty()) {
            user.setLegalIdType(legalIdType);
            user.setLegalIdNumber(legalIdNumber);
        }


        user.setBillingAddress(billingAddress);
        billingAddress.setUser(user);

        User savedUser = userRepository.save(user);

        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setUser(savedUser);
        creditAccount.setBalance(0);
        creditAccountRepository.save(creditAccount);

        return savedUser;
    }

    public User createProfile(profileDTO profileDTO) {
        if (userRepository.findByEmail(profileDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = new User();
        user.setUsername(profileDTO.getUsername());
        user.setEmail(profileDTO.getEmail());
        user.setPassword(passwordEncoder.encode(profileDTO.getPassword()));
        user.setRoles(profileDTO.getRoles());

        User savedUser = userRepository.save(user);

        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setUser(savedUser);
        creditAccount.setBalance(0);
        creditAccountRepository.save(creditAccount);

        return savedUser;
    }

    public User authUser(String email, String password) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return userRepository.findByEmail(email).orElse(null);

        } catch (Exception ex) {
            throw new RuntimeException("Incorrect credentials");
        }
    }

    public AuthResponse googleAuthUser(String idTokenString) {
        try {
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                Optional<User> optionalUser = userRepository.findByEmail(email);
                User user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                } else {
                    user = new User();
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("google-auth"));
                    user.setRoles("USER");
                    user.setAccountValidated(ValidationStatus.PENDING);
                    User savedUser = userRepository.save(user);

                    CreditAccount creditAccount = new CreditAccount();
                    creditAccount.setUser(savedUser);
                    creditAccount.setBalance(0);
                    creditAccountRepository.save(creditAccount);

                    user = savedUser;
                }
                String token = jwtUtil.generateToken(user);
                return new AuthResponse(token, String.valueOf(user.getId()), user.getRoles(), user.getAccountValidated(), user.getSecretKey());
            } else {
                throw new RuntimeException("Invalid Google ID Token");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Google Authentication failed", e);
        }
    }

    public byte[] enable2FA(String email, String appName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        String secretKey = twoFactorAuthService.generateSecretKey();
        user.setSecretKey(secretKey);
        userRepository.save(user);
        return twoFactorAuthService.generateQRCode(secretKey, email, appName);
    }

    public boolean verify2FA(String email, int verificationCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return twoFactorAuthService.verifyCode(user.getSecretKey(), verificationCode);
    }

    public void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    public String createResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetTokenRepository.save(resetToken);

        return token;
    }
    public void validateResetToken(String token) {
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }
    }
    // Method to request password reset
    public void resetPassword(String token, String newPassword) {
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the token after successful password reset
        resetTokenRepository.delete(resetToken);
    }
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User createUser(String email, String password, String roles) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);


        User savedUser = userRepository.save(user);

        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setUser(savedUser);
        creditAccount.setBalance(0);
        creditAccountRepository.save(creditAccount);

        return savedUser;
    }

    // Edit user details
    public User editUser(Long userId, String email, String password, String driverLicense, String roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        if (roles != null && !roles.isEmpty()) {
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    // List all users
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    // Get all pending validation users with images
    public List<PendingUserValidationDTO> getPendingValidationUsers() throws IOException {
        List<User> pendingUsers = userRepository.findByAccountValidated(ValidationStatus.PENDING);
        List<PendingUserValidationDTO> result = new ArrayList<>();

        for (User user : pendingUsers) {
            PendingUserValidationDTO dto = new PendingUserValidationDTO();
            dto.setUserId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setLegalIdType(user.getLegalIdType());
            dto.setLegalIdNumber(user.getLegalIdNumber());
            dto.setDriverLicense(user.getLegalIdNumber()); // Deprecated - para compatibilidad
            dto.setValidationImageName(user.getValidationImageName());

            // Convertir imagen a Base64 si existe
            if (user.getValidationImageName() != null) {
                try {
                    byte[] imageBytes = fileStorageService.loadFileAsBytes(user.getValidationImageName());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    dto.setValidationImageBase64(base64Image);
                } catch (Exception e) {
                    dto.setValidationImageBase64(null);
                }
            }

            // Agregar billing address si existe
            if (user.getBillingAddress() != null) {
                BillingAddressDTO addressDTO = new BillingAddressDTO(
                        user.getBillingAddress().getId(),
                        user.getBillingAddress().getAddressLine1(),
                        user.getBillingAddress().getAddressLine2(),
                        user.getBillingAddress().getCity(),
                        user.getBillingAddress().getState(),
                        user.getBillingAddress().getZipCode(),
                        user.getBillingAddress().getCountry(),
                        user.getId()
                );
                dto.setBillingAddress(addressDTO);
            }

            result.add(dto);
        }

        return result;
    }

    // Delete user by ID
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    // Upload validation image
    public User uploadValidationImage(Long userId, org.springframework.web.multipart.MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Delete old image if exists
        if (user.getValidationImageName() != null) {
            fileStorageService.deleteFile(user.getValidationImageName());
        }

        // Store new image
        String filename = fileStorageService.storeFile(file, userId);
        user.setAccountValidated(ValidationStatus.PENDING);
        user.setValidationImageName(filename);
        user.setValidationImagePath(fileStorageService.getFilePath(filename).toString());

        return userRepository.save(user);
    }

    // Get validation image
    public byte[] getValidationImage(Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getValidationImageName() == null) {
            throw new RuntimeException("No validation image found for user");
        }

        return fileStorageService.loadFileAsBytes(user.getValidationImageName());
    }

    // Validate user account (admin function)
    public User validateUserAccount(Long userId ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        String email = user.getEmail(); // Get email from user

        emailService.sendAccountApprovedEmail(email);
        user.setAccountValidated(ValidationStatus.APPROVED);

        return userRepository.save(user);
    }

    //deny user account (admin function)
    public User denyUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        String email = user.getEmail(); // Get email from user

        emailService.sendAccountRejectedEmail(email);

        user.setAccountValidated(ValidationStatus.REJECTED);
        return userRepository.save(user);
    }
}
