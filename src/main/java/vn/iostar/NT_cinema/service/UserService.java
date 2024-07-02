package vn.iostar.NT_cinema.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.exception.AlreadyExistException;
import vn.iostar.NT_cinema.exception.NotFoundException;
import vn.iostar.NT_cinema.exception.UserNotFoundException;
import vn.iostar.NT_cinema.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleService roleService;
    @Autowired
    CinemaRepository cinemaRepository;
    @Autowired
    EmailVerificationService emailVerificationService;
    @Autowired
    PasswordResetOtpRepository passwordResetOtpRepository;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    Environment env;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    StaffRepository staffRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    ManagerService managerService;

    public Optional<User> findByUserName(String userName) {
        Optional<User> user = userRepository.findByUserName(userName);
        if (user.isPresent()){
            return user;
        }
        return userRepository.findByEmail(userName);
    }

    public <S extends User> S save(S entity) {
        return userRepository.save(entity);
    }

    public ResponseEntity<GenericResponse> userRegister(RegisterRequest registerRequest) {
        Optional<User> userOptional = userRepository.findByEmail(registerRequest.getEmail());
        if (userOptional.isPresent())
            throw new AlreadyExistException("Email này đã được đăng ký rồi.");

        userOptional = userRepository.findByPhone(registerRequest.getPhone());
        if (userOptional.isPresent())
            throw new AlreadyExistException("Số điện thoại này đã được đăng ký rồi.");

        userOptional = userRepository.findByUserName(registerRequest.getUserName());
        if (userOptional.isPresent())
            throw new AlreadyExistException("Tên đăng nhập này đã được sự dụng.");

        if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 32)
            throw new IllegalArgumentException("Mật khẩu cần dài từ 8 đến 32 ký tự.");

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))
            throw new AlreadyExistException("Mật khẩu và mật khẩu nhập lại không giống nhau.");

        User user = new Viewer();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setCreatedAt(new Date());
        user.setUserName(registerRequest.getUserName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(roleService.findByRoleName("VIEWER"));

        save(user);

        emailVerificationService.sendOtp(registerRequest.getEmail());

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Quá trình đăng ký hoàn tất. Hãy xác nhận mã OTP.")
                        .result(null)
                        .statusCode(200)
                        .build()
        );
    }

    public ResponseEntity<GenericResponse> addManager(ManagerRequest request) {
        try {
            Manager user = new Manager();
            if (userRepository.findByUserName(request.getUserName()).isPresent())
                throw new AlreadyExistException("Tên đăng nhập đã được sử dụng.");

            if (userRepository.findByPhone(request.getPhone()).isPresent())
                throw new AlreadyExistException("Số điện thoại đã được sử dụng.");

            if (userRepository.findByEmail(request.getEmail()).isPresent())
                throw new AlreadyExistException("Email đã đăng ký. Vui lòng đăng ký email khác.");

            Optional<Cinema> cinema = cinemaRepository.findById(request.getCinemaId());
            if (cinema.isEmpty())
                throw new NotFoundException("Rạp phim không tồn tại.");
            Optional<Manager> managerByCinema = managerService.getManagerByCinema(cinema.get());
            if (managerByCinema.isPresent())
                throw new AlreadyExistException("Rạp phim đã có quản lý. Vui lòng chon rạp khác.");

            user.setUserName(request.getUserName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            user.setPhone(request.getPhone());
            user.setRole(roleService.findByRoleName("MANAGER"));
            user.setActive(true);
            user.setCinema(cinema.get());

            User manager = save(user);

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Đăng ký tài khoản cho quản lý thành công!")
                            .result(manager)
                            .statusCode(200)
                            .build()
            );

        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> addStaff(StaffReq request) {
        try {
            Staff user = new Staff();
            if (userRepository.findByUserName(request.getUserName()).isPresent())
                throw new AlreadyExistException("Tên đăng nhập đã được sử dụng.");

            if (userRepository.findByPhone(request.getPhone()).isPresent())
                throw new AlreadyExistException("Số điện thoại đã được sử dụng.");

            if (userRepository.findByEmail(request.getEmail()).isPresent())
                throw new AlreadyExistException("Email đã được sử dụng.");

            Optional<Cinema> cinema = cinemaRepository.findById(request.getCinemaId());
            if (cinema.isEmpty())
                throw new NotFoundException("Rạp phim không tồn tại.");

            user.setUserName(request.getUserName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            user.setPhone(request.getPhone());
            user.setRole(roleService.findByRoleName("STAFF"));
            user.setActive(true);
            user.setCinema(cinema.get());

            User staff = save(user);

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Đăng ký tài khoản cho nhân viên thành công!")
                            .result(staff)
                            .statusCode(200)
                            .build()
            );

        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }


    public ResponseEntity<GenericResponse> addViewer(ViewerReq request) {
        try {
            User user = new User();
            if (userRepository.findByPhone(request.getPhone()).isPresent())
                throw new AlreadyExistException("Số điện thoại đã được sử dụng.");
            if (userRepository.findByEmail(request.getEmail()).isPresent())
                throw new AlreadyExistException("Email đã được sử dụng.");

            if (!request.getEmail().isEmpty()){
                user.setEmail(request.getEmail());
                user.setUserName(request.getEmail());
            }else {
                user.setEmail(null);
            }
            if (!request.getPhone().isEmpty()){
                user.setPhone(request.getPhone());
                user.setUserName(request.getPhone());
            }else {
                user.setPhone(null);
            }

            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            user.setRole(roleService.findByRoleName("VIEWER"));
            user.setActive(true);

            User viewer = save(user);

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Đăng ký tài khoản cho người dùng thành công!")
                            .result(viewer)
                            .statusCode(200)
                            .build()
            );

        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getProfile(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new RuntimeException("User not found");

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Lấy hồ sơ người dùng thành công!")
                        .result(user.get())
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    public ResponseEntity<GenericResponse> changePassword(String userId, ChangePasswordRequest request) {
        try {

            if (request.getNewPassword().length() < 8 || request.getNewPassword().length() > 32)
                throw new AlreadyExistException("Mật khẩu phải có độ dài từ 8-32 ký tự.");

            if (!request.getNewPassword().equals(request.getConfirmNewPassword()))
                throw new AlreadyExistException("Mật khẩu và nhập lại mật khẩu không trùng khớp.");

            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty())
                throw new NotFoundException("Tài khoản này không tồn tại.");

            User user = userOptional.get();
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
                throw new AlreadyExistException("Sai mật khẩu hiện tại.");

            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
                throw new AlreadyExistException("Mật khẩu mới không thể giống mật khẩu cũ được.");


            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            save(user);

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Đổi mật khẩu thành công!")
                            .result(null)
                            .statusCode(200)
                            .build()
            );
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }

    public ResponseEntity<GenericResponse> updateUser(UserReq request, String userId) {
        try {
            Optional<User> optionalViewer = userRepository.findById(userId);
            if (optionalViewer.isPresent()){
                User user = optionalViewer.get();
                if (userRepository.existsUserByPhone(request.getPhone())){
                    throw new AlreadyExistException("Số điện thoại bạn muốn thay đổi đã được đăng ký bởi tài khoản khác.");
                }
                if (userRepository.existsUserByEmail(request.getEmail())){
                    throw new AlreadyExistException("Email đã được đăng ký bởi tài khoản khác.");
                }
                if(userRepository.existsUserByUserName(request.getUserName())){
                    throw new AlreadyExistException("Tên đăng nhập đã được sử dụng bởi tài khoản khác.");
                }

                if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                    user.setPhone(request.getPhone());
                }
                if (request.getFullName() != null && !request.getFullName().isEmpty()) {
                    user.setFullName(request.getFullName());
                }
                if (request.getImage() != null){
                    if (optionalViewer.get().getAvatar() != null){
                        cloudinaryService.deleteImage(optionalViewer.get().getAvatar());
                    }
                    String image = cloudinaryService.uploadImage(request.getImage());
                    user.setAvatar(image);
                }
                if (request.getCountry() != null && request.getDistrict() != null && request.getProvince() != null && request.getStreet() != null) {
                    Optional<Address> optionalAddress = addressRepository.findByStreetAndProvinceAndDistrictAndCountry(
                            request.getStreet(),
                            request.getProvince(),
                            request.getDistrict(),
                            request.getCountry());
                    if (optionalAddress.isPresent()){
                        user.setAddress(optionalAddress.get());
                    }else {
                        Address addressRq = new Address(request.getStreet(),
                                request.getProvince(),
                                request.getDistrict(),
                                request.getCountry());
                        Address address = addressRepository.save(addressRq);
                        user.setAddress(address);
                    }
                }
                if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                    user.setEmail(request.getEmail());
                }
                if (request.getDob() != null){
                    user.setDob(request.getDob());
                }
                if (request.getUserName() != null && !request.getUserName().isEmpty()) {
                    user.setUserName(request.getUserName());
                }

                user.setUpdatedAt(new Date());

                userRepository.save(user);

                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Cập nhật trang cá nhân thành công!")
                        .result(user)
                        .statusCode(200)
                        .build());
            }else {
                throw new NotFoundException("Cập nhật thất bại. Không tìm thấy người dùng.");
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void createPasswordResetOtpForUser(User user, String otp) {
        PasswordResetOtp myOtp;
        if (passwordResetOtpRepository.findByUser(user).isPresent()) {
            myOtp = (PasswordResetOtp) passwordResetOtpRepository.findByUser(user).get();
            myOtp.updateOtp(otp);
        } else {

            myOtp = new PasswordResetOtp(otp, user);
        }
        passwordResetOtpRepository.save(myOtp);
    }

    public ResponseEntity<GenericResponse> forgotPassword(String email){
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty())
            throw new NotFoundException("Không tìm thấy người dùng.");
        try {

            String otp = emailVerificationService.generateOtp();
            createPasswordResetOtpForUser(user.get(), otp);
            String subject = "Change Password For TNCinemas";
            Context context = new Context();
            context.setVariable("otpCode",otp);
            String content = templateEngine.process("forgot-password",context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setSubject(subject);
            helper.setText(content,true);
            helper.setTo(user.get().getEmail());
            helper.setFrom(Objects.requireNonNull(env.getProperty("spring.mail.username")),"TNCinemas Admin");

            javaMailSender.send(message);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Vui lòng kiểm tra email để đổi lại mật khẩu mới!")
                    .result("Gửi mã Otp thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deleteUnverifiedAccounts() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        Date twentyFourHoursAgo = calendar.getTime();

        List<User> unverifiedAccounts = userRepository.findByIsActiveFalseAndCreatedAtBefore(twentyFourHoursAgo);
        userRepository.deleteAll(unverifiedAccounts);
    }
    @PostConstruct
    public void init() {
        deleteUnverifiedAccounts();
    }
    @Scheduled(fixedDelay = 86400000) // 24 hours
    public void scheduledDeleteUnverifiedAccounts() {
        deleteUnverifiedAccounts();
    }

    public ResponseEntity<?> validateOtp(String token) {
        try {
            String result = validatePasswordResetOtp(token);
            if (result == null){
                return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                        .success(true)
                        .message("Xác thực otp thành công.")
                        .result(null)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }else {
                throw new IllegalArgumentException(result);
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> resetPassword(String token, PasswordResetRequest passwordResetRequest) {
        try {
                Optional<PasswordResetOtp> user = passwordResetOtpRepository.findByOtp(token);
                if (user.isEmpty()){
                    throw new NotFoundException("Không tìm thấy người dùng.");
                }
                changeUserPassword(user.get().getUser(), passwordResetRequest.getNewPassword()
                        , passwordResetRequest.getConfirmPassword());
                return ResponseEntity.ok(
                        GenericResponse.builder()
                                .success(true)
                                .message("Đổi mật khẩu thành công!")
                                .result(null)
                                .statusCode(200)
                                .build()
                );

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void changeUserPassword(User user, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword))
            throw new RuntimeException("Mật khẩu và nhập lại mật khẩu không trùng khớp.");
        user.setPassword(passwordEncoder.encode(newPassword));
        save(user);
    }

    public String validatePasswordResetOtp(String otp) {

        Optional<PasswordResetOtp> passOtp = passwordResetOtpRepository.findByOtp(otp);
        Calendar cal = Calendar.getInstance();

        if (passOtp.isEmpty()) {
            return "Invalid token/link";
        }
        if (passOtp.get().getExpiryDate().before(cal.getTime())) {
            return "Token/link expired";
        }
        return null;
    }

    public Page<User> findAllByRole(String roleName, Pageable pageable) {
        Role role = roleService.findByRoleName(roleName);
        return userRepository.findAllByRole(role, pageable);
    }

    public ResponseEntity<GenericResponse> getAllUser(String role, Pageable pageable) {
        try {
            Page<User> users = switch (role) {
                case "ALL" -> userRepository.findAllByOrderByLastLoginAtDesc(pageable);
                case "MANAGER" -> findAllByRole("MANAGER", pageable);
                case "STAFF" -> findAllByRole("STAFF", pageable);
                case "ADMIN" -> findAllByRole("ADMIN", pageable);
                case "VIEWER" -> findAllByRole("VIEWER", pageable);
                default -> throw new IllegalArgumentException("Role không hợp lệ.");
            };

            Map<String, Object> map = new HashMap<>();
            map.put("content", users.getContent().stream().sorted(Comparator.comparing(User::getLastLoginAt, Comparator.nullsLast(Comparator.naturalOrder()))).toList());
            map.put("pageNumber", users.getPageable().getPageNumber() + 1);
            map.put("pageSize", users.getSize());
            map.put("totalPages", users.getTotalPages());
            map.put("totalElements", users.getTotalElements());

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Lấy tất cả người dùng thành công!")
                            .result(map)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getUser(String id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty())
                throw new NotFoundException("Người dùng không tồn tại!");
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin người dùng thành công!")
                            .result(user.get())
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> adminUpdateUser(String userId, UpdateUserReq request) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                if (Objects.equals(user.getRole().getRoleName(), "ADMIN")){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                            .success(false)
                            .message("Cập nhật thất bại! Không thể thay đổi tài khoản admin được.")
                            .result(null)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());
                }
                if (userRepository.existsUserByPhone(request.getPhone())){
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Số điện thoại bạn muốn thay đổi đã được đăng ký bởi tài khoản khác.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
                if (userRepository.existsUserByEmail(request.getEmail())){
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Email đã được đăng ký bởi tài khoản khác.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
                if(userRepository.existsUserByUserName(request.getUserName())){
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Tên đăng nhập đã được sử dụng bởi tài khoản khác.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
                if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                    user.setPhone(request.getPhone());
                }
                if (request.getFullName() != null && !request.getFullName().isEmpty()) {
                    user.setFullName(request.getFullName());
                }
                if (request.getCountry() != null && request.getDistrict() != null && request.getProvince() != null && request.getStreet() != null) {
                    Optional<Address> optionalAddress = addressRepository.findByStreetAndProvinceAndDistrictAndCountry(
                            request.getStreet(),
                            request.getProvince(),
                            request.getDistrict(),
                            request.getCountry());
                    if (optionalAddress.isPresent()){
                        user.setAddress(optionalAddress.get());
                    }else {
                        Address addressRq = new Address(request.getStreet(),
                                request.getProvince(),
                                request.getDistrict(),
                                request.getCountry());
                        Address address = addressRepository.save(addressRq);
                        user.setAddress(address);
                    }
                }
                if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                    user.setEmail(request.getEmail());
                }
                if (request.getDob() != null){
                    user.setDob(request.getDob());
                }
                if (request.getUserName() != null && !request.getUserName().isEmpty()) {
                    user.setUserName(request.getUserName());
                }
                if (request.getRole() != null && !request.getRole().isEmpty()){
                    user.setRole(roleService.findByRoleName(request.getRole()));
                }
                user.setUpdatedAt(new Date());

                userRepository.save(user);

                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Cập nhật thành công!")
                        .result(user)
                        .statusCode(200)
                        .build());
            }else {
                throw new NotFoundException("Cập nhật thất bại.");
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> updateIsDeleteUser(String id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()){
                if (Objects.equals(user.get().getRole().getRoleName(), "ADMIN")){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                            .success(false)
                            .message("Cập nhật thất bại! Bạn không thể xóa tài khoản admin.")
                            .result(null)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());
                }

                user.get().setDelete(!user.get().isDelete());
                userRepository.save(user.get());
                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message(user.get().isDelete() ? "Tài khoản đã bị vô hiệu." : "Khôi phục thành công.")
                        .result(user.get())
                        .statusCode(200)
                        .build());
            }else {
                throw new NotFoundException("Người dùng không tồn tại!");
            }

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getAllPersonnel(boolean sortByRole, String userName, Pageable pageable) {
        try {
            List<Role> roles = new ArrayList<>();
            roles.add(roleService.findByRoleName("MANAGER"));
            roles.add(roleService.findByRoleName("STAFF"));

            Page<User> users = userRepository.findAllByRoleIn(roles, pageable);
            List<User> list;
            if (sortByRole) {
                list = users.getContent().stream().sorted(Comparator.comparing(User::getUserId).reversed().thenComparing(user -> getRolePriority(user.getRole())))
                        .toList();
            }else {
                list = users.getContent().stream().sorted(Comparator.comparing(User::getUserId).reversed())
                        .toList();
            }
            if (userName != null && !userName.isBlank()) {
                list = list.stream().filter(user -> user.getUserName().contains(userName)).toList();
                users = new PageImpl<>(list, pageable, list.size());
            }
            Map<String, Object> result = new HashMap<>();
            result.put("content", list);
            result.put("pageNumber", users.getPageable().getPageNumber() + 1);
            result.put("pageSize", users.getSize());
            result.put("totalPages", users.getTotalPages());
            result.put("totalElements", users.getTotalElements());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy sanh sách nhân sự thành công!")
                    .result(result)
                    .statusCode(200)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private int getRolePriority(Role role) {
        // Xác định ưu tiên của vai trò
        return switch (role.getRoleName()) {
            case "ADMIN" -> 1;
            case "MANAGER" -> 2;
            case "STAFF" -> 3;
            default -> Integer.MAX_VALUE; // Mặc định các vai trò không biết sắp xếp ở cuối danh sách

        };
        }

    public ResponseEntity<GenericResponse> getAllViewer(String userName, Pageable pageable) {
        try {
            List<Role> roles = new ArrayList<>();
            roles.add(roleService.findByRoleName("VIEWER"));
            Page<User> users = userRepository.findAllByRoleIn(roles, pageable);
            List<User> list = users.getContent().stream().sorted(Comparator.comparing(User::getUserId).reversed()).toList();
            if (userName != null && !userName.isBlank()) {
                list = list.stream().filter(user -> user.getUserName().contains(userName)).toList();
                users = new PageImpl<>(list, pageable, list.size());
            }
            Map<String, Object> result = new HashMap<>();
            result.put("content", list);
            result.put("pageNumber", users.getPageable().getPageNumber() + 1);
            result.put("pageSize", users.getSize());
            result.put("totalPages", users.getTotalPages());
            result.put("totalElements", users.getTotalElements());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách người xem thành công!")
                    .result(result)
                    .statusCode(200)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getAllStaff(String managerId, PageRequest pageable) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            if (manager.isEmpty())
                throw new UserNotFoundException();
            Page<Staff> users = staffRepository.findAllByRoleAndCinema(roleService.findByRoleName("STAFF"), manager.get().getCinema(), pageable);
            Map<String, Object> result = new HashMap<>();
            result.put("content", users.getContent().stream().sorted(Comparator.comparing(User::getUserId).reversed())
                    .collect(Collectors.toList()));
            result.put("pageNumber", users.getPageable().getPageNumber() + 1);
            result.put("pageSize", users.getSize());
            result.put("totalPages", users.getTotalPages());
            result.put("totalElements", users.getTotalElements());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách nhân viên thành công!")
                    .result(result)
                    .statusCode(200)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> updateStaff(String id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                user.get().setDelete(!user.get().isDelete());
                userRepository.save(user.get());
                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Xóa nhân viên thành công!")
                        .result(user.get())
                        .statusCode(200)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Nhân viên không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> searchViewers(String keyWord) {
        try {
            List<User> users = userRepository.searchByKeyWord(keyWord);
            users.removeIf(user -> !user.getRole().getRoleName().equals("VIEWER"));
            List<UserRes> userRes = users.stream().map(UserRes::new).toList();
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Tìm kiếm người xem!")
                    .result(userRes)
                    .statusCode(200)
                    .build());
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
