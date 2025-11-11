package com.getwork.auth.service;

import com.getwork.auth.dto.LoginUserDto;
import com.getwork.auth.dto.RegisterUserDto;
import com.getwork.auth.dto.VerifyUserDto;
import com.getwork.auth.entity.User;
import com.getwork.auth.repo.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDto input) {
        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify your account.");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setIsEmailVerified(true);
                user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
                user.setStatus(User.Status.ACTIVE);
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationExpiration(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(10));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }
    private void sendVerificationEmail(User user) { //TODO: Update with company logo
        String subject = "Account Verification for GET WORK";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<!doctype html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "  <meta charset=\"utf-8\">"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "  <title>Verify your email</title>"
                + "  <style>"
                + "    /* Reset */"
                + "    body { margin:0; padding:0; -webkit-font-smoothing:antialiased; font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; background:#f3f4f6; }"
                + "    a { color: inherit; text-decoration: none; }"
                + "    .container { max-width:630px; margin:32px auto; padding:20px; }"
                + "    .card { background:#ffffff; border-radius:12px; box-shadow:0 6px 18px rgba(15,23,42,0.08); overflow:hidden; }"
                + "    .header { background: linear-gradient(90deg,#111827 0%, #0f172a 100%); padding:22px 24px; display:flex; align-items:center; gap:16px; }"
                + "    .logo { width:48px; height:48px; border-radius:8px; background-color:#ffffff; display:flex; align-items:center; justify-content:center; font-weight:bold; color:#0f172a; }"
                + "    .brand-title { color:#ffffff; font-size:18px; font-weight:600; }"
                + "    .content { padding:28px 32px; color:#0b1220; }"
                + "    .preheader { display:none !important; visibility:hidden; mso-hide:all; font-size:1px; color:#f3f4f6; line-height:1px; max-height:0; max-width:0; opacity:0; overflow:hidden; }"
                + "    h1 { margin:0 0 12px 0; font-size:20px; color:#0f172a; }"
                + "    p { margin:0 0 16px 0; font-size:15px; color:#374151; }"
                + "    .code-box { margin:16px 0 18px 0; padding:18px; background: linear-gradient(180deg,#fafafa,#ffffff); border:1px solid #edf2f7; border-radius:10px; text-align:center; }"
                + "    .code { display:inline-block; font-family: 'Courier New', Courier, monospace; font-size:28px; letter-spacing:4px; font-weight:700; color:#0b63ff; padding:6px 14px; background: rgba(11,99,255,0.06); border-radius:8px; }"
                + "    .cta { display:block; width:100%; text-align:center; margin-top:6px; }"
                + "    .btn { display:inline-block; padding:12px 22px; border-radius:10px; background:#0b63ff; color:#ffffff; font-weight:600; font-size:15px; }"
                + "    .muted { color:#6b7280; font-size:13px; }"
                + "    .footer { padding:18px 32px; background:#fbfbfd; border-top:1px solid #eef2f7; font-size:13px; color:#6b7280; }"
                + "    .small { font-size:12px; color:#9ca3af; }"
                + "    /* Responsive */"
                + "    @media only screen and (max-width:520px) {"
                + "      .container { padding:12px; }"
                + "      .content { padding:20px; }"
                + "      .header { padding:16px; }"
                + "      .brand-title { font-size:16px; }"
                + "      .code { font-size:24px; letter-spacing:3px; }"
                + "      .btn { width:100%; padding:12px; }"
                + "    }"
                + "  </style>"
                + "</head>"
                + "<body>"
                + "  <!-- Preheader text (appears in inbox preview) -->"
                + "  <span class=\"preheader\">Use this verification code to activate your Get-Work account. It expires in 10 minutes.</span>"
                + ""
                + "  <div class=\"container\">"
                + "    <div class=\"card\">"
                + "      <div class=\"header\">"
                + "        <!-- Replace src with your logo URL -->"
                + "        <div class=\"logo\">G</div>"
                + "        <div>"
                + "          <div class=\"brand-title\">Get-Work</div>"
                + "          <div style=\"color:#9ca3af; font-size:13px; margin-top:2px;\">Account verification</div>"
                + "        </div>"
                + "      </div>"
                + ""
                + "      <div class=\"content\">"
                + "        <h1>Verify your email address</h1>"
                + "        <p class=\"muted\">Thanks for creating a Get-Work account. Enter the verification code below to confirm your email address and activate your account.</p>"
                + ""
                + "        <div class=\"code-box\">"
                + "          <div style=\"font-size:13px; color:#6b7280; margin-bottom:8px;\">Your verification code</div>"
                + "          <div class=\"code\">" + verificationCode + "</div>"
                + "          <div style=\"margin-top:12px; color:#6b7280; font-size:13px;\">This code will expire in <strong>10 minutes</strong>.</div>"
                + "        </div>"
                + ""
                + "        <div class=\"cta\">"
                + "          <!-- Replace the href with your verification link if you have one -->"
                + "          <a class=\"btn\" href=\"https://get-work.com/verify?code=" + verificationCode + "\">Verify your account</a>"
                + "        </div>"
                + ""
                + "        <p style=\"margin-top:18px;\" class=\"muted\">If you didn't request this code, you can safely ignore this email. For help, contact <a href=\"mailto:arbaazsha01@gmail.com\">arbaazsha01@gmail.com</a>.</p>"
                + "      </div>"
                + ""
                + "      <div class=\"footer\">"
                + "        <div style=\"display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap;\">"
                + "          <div class=\"small\">Get-Work • City Raichur, 584101 , Karnataka, India</div>"
                + "          <div class=\"small\">&bull; <a href=\"https://your-app.example.com/terms\">Terms</a> &bull; <a href=\"https://your-app.example.com/privacy\">Privacy</a></div>"
                + "        </div>"
                + "        <div style=\"margin-top:10px;\" class=\"small\">If you did not create an account using this email, please ignore this message.</div>"
                + "      </div>"
                + "    </div>"
                + "  </div>"
                + "</body>"
                + "</html>";


        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

//    private void sendVerificationEmail(User user) {
//        String subject = "Account Verification for Get-Work App";
//
//        emailService.sendVerificationEmail(
//                user.getEmail(),
//                subject,
//                user.getUsername()
//        );
//    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}