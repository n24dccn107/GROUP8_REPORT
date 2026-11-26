package org.example.library_management.service;

import org.example.library_management.dto.LibrarianLoginRequest;
import org.example.library_management.dto.LoginResponse;
import org.example.library_management.dto.MemberLoginRequest;
import org.example.library_management.dto.MemberRegistrationRequest;
import org.example.library_management.entity.AccountEntity;
import org.example.library_management.entity.LibrarianEntity;
import org.example.library_management.entity.MemberEntity;
import org.example.library_management.repository.AccountRepository;
import org.example.library_management.repository.LibrarianRepository;
import org.example.library_management.repository.MemberRepository;
import org.example.library_management.security.JwtPrincipal;
import org.example.library_management.security.JwtService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {
    private static final String MEMBER_ROLE = "Member";
    private static final String LIBRARIAN_ROLE = "Librarian";
    private static final String ACTIVE_STATUS = "Active";

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final LibrarianRepository librarianRepository;
    private final AccountStatusService accountStatusService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JdbcTemplate jdbc;

    public AuthService(AccountRepository accountRepository,
                       MemberRepository memberRepository,
                       LibrarianRepository librarianRepository,
                       AccountStatusService accountStatusService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       JdbcTemplate jdbc) {
        this.accountRepository = accountRepository;
        this.memberRepository = memberRepository;
        this.librarianRepository = librarianRepository;
        this.accountStatusService = accountStatusService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jdbc = jdbc;
    }

    @Transactional
    public Map<String, Object> registerMember(MemberRegistrationRequest request) {
        if (request == null || isBlank(request.memberId()) || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã thành viên và mật khẩu là bắt buộc.");
        }
        String memberId = request.memberId().trim();
        if (memberId.length() > 10 || request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Mã thành viên tối đa 10 ký tự; mật khẩu cần ít nhất 8 ký tự.");
        }

        MemberEntity member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mã thành viên."));
        if (member.getExpiryDate().isBefore(currentDatabaseDate())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Thẻ thành viên đã hết hạn, không thể đăng ký tài khoản.");
        }
        if (accountRepository.findByPersonIdAndRole(member.getPersonId(), MEMBER_ROLE).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã thành viên đã có tài khoản.");
        }

        try {
            jdbc.update("""
                    INSERT INTO Account (person_id, role, password_hash, status, created_at)
                    VALUES (?, 'Member', ?, 'Active', CURRENT_TIMESTAMP)
                    """, member.getPersonId(), passwordEncoder.encode(request.password()));
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã thành viên đã có tài khoản.");
        }
        return Map.of("memberId", memberId, "role", MEMBER_ROLE, "status", ACTIVE_STATUS);
    }

    @Transactional
    public LoginResponse loginMember(MemberLoginRequest request) {
        if (request == null || isBlank(request.memberId()) || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "memberId và password không được để trống.");
        }

        MemberEntity member = memberRepository.findByMemberId(request.memberId().trim())
                .orElseThrow(this::invalidCredentials);
        AccountEntity account = accountRepository
                .findByPersonIdAndRole(member.getPersonId(), MEMBER_ROLE)
                .orElseThrow(this::invalidCredentials);

        if (member.getExpiryDate().isBefore(currentDatabaseDate())) {
            accountStatusService.disableAccount(account.getAccountId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Thẻ thành viên đã hết hạn.");
        }
        if (!isActive(account) || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw invalidCredentials();
        }

        accountRepository.updateLastLogin(account.getAccountId(), LocalDateTime.now());
        return createResponse(account, MEMBER_ROLE, member.getMemberId());
    }

    @Transactional
    public LoginResponse loginLibrarian(LibrarianLoginRequest request) {
        if (request == null || isBlank(request.librarianId()) || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "librarianId và password không được để trống.");
        }

        LibrarianEntity librarian = librarianRepository.findByLibrarianId(request.librarianId().trim())
                .orElseThrow(this::invalidCredentials);
        AccountEntity account = accountRepository
                .findByPersonIdAndRole(librarian.getPersonId(), LIBRARIAN_ROLE)
                .orElseThrow(this::invalidCredentials);

        if (!isActive(account) || librarian.getLeaveDate() != null
                || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw invalidCredentials();
        }

        accountRepository.updateLastLogin(account.getAccountId(), LocalDateTime.now());
        return createResponse(account, LIBRARIAN_ROLE, librarian.getLibrarianId());
    }

    @Transactional
    public boolean isPrincipalActive(JwtPrincipal principal) {
        AccountEntity account = accountRepository.findById(principal.accountId()).orElse(null);
        if (account == null || !isActive(account) || !principal.role().equals(account.getRole())) {
            return false;
        }

        if (MEMBER_ROLE.equals(principal.role())) {
            MemberEntity member = memberRepository.findByPersonId(account.getPersonId()).orElse(null);
            if (member == null) return false;
            if (member.getExpiryDate().isBefore(currentDatabaseDate())) {
                accountStatusService.disableAccount(account.getAccountId());
                return false;
            }
            return member.getMemberId().equals(principal.identifier());
        }

        if (LIBRARIAN_ROLE.equals(principal.role())) {
            LibrarianEntity librarian = librarianRepository.findByPersonId(account.getPersonId()).orElse(null);
            return librarian != null && librarian.getLeaveDate() == null
                    && librarian.getLibrarianId().equals(principal.identifier());
        }
        return false;
    }

    private LocalDate currentDatabaseDate() {
        return jdbc.queryForObject("SELECT CURRENT_DATE", LocalDate.class);
    }

    private LoginResponse createResponse(AccountEntity account, String role, String identifier) {
        String token = jwtService.createToken(account.getAccountId(), role, identifier);
        return new LoginResponse(token, "Bearer", role, identifier, jwtService.getExpirationSeconds());
    }

    private boolean isActive(AccountEntity account) {
        return ACTIVE_STATUS.equalsIgnoreCase(account.getStatus());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mã đăng nhập hoặc mật khẩu không hợp lệ.");
    }
}
