package vn.iostar.NT_cinema.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.NotiTarget;
import vn.iostar.NT_cinema.constant.NotiType;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.exception.UserNotFoundException;
import vn.iostar.NT_cinema.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationUserRepository notificationUserRepository;
    @Autowired
    FCMService fcmService;
    @Autowired
    UserTokenRepository userTokenRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    RoleService roleService;
    @Autowired
    ManagerService managerService;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    FoodInventoryRepository foodInventoryRepository;
    @Autowired
    CinemaRepository cinemaRepository;
    @Autowired
    StaffRepository staffRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    BookingRepository bookingRepository;

    public Notification createNotification(NotiType type, String title, String message, NotiTarget target, Object detailData) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTarget(target);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notification.setDetailData(detailData);
        return notificationRepository.save(notification);
    }
    public void sendNotificationToAllViewer(String type, String title, String message, Object detailData) throws FirebaseMessagingException {
        Notification notification = createNotification(NotiType.valueOf(type), title, message, NotiTarget.VIEWER, detailData);
        List<Role> roles = new ArrayList<>();
        roles.add(roleService.findByRoleName("VIEWER"));
        List<User> users = userRepository.findAllByRoleIn(roles);

        List<NotificationUser> notificationsUsers = new ArrayList<>();
        for (User user : users) {
            NotificationUser notificationUser = new NotificationUser();
            notificationUser.setUser(user);
            notificationUser.setNotification(notification);
            notificationUser.setRead(false);
            notificationUser.setCreatedAt(LocalDateTime.now());
            notificationUser.setUpdatedAt(LocalDateTime.now());
            notificationsUsers.add(notificationUser);
        }

        notificationUserRepository.saveAll(notificationsUsers);

        for (User user : users) {
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(user.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), title, message);
        }
    }

    public void bookingTicketSuccessNotification(Booking booking) throws FirebaseMessagingException {
        Optional<User> user = userRepository.findById(booking.getUserId());
        if (user.isPresent()) {
            sendNotiForBookingSuccessToUser(booking, user.get());

            String message2 = "Khách hàng " + user.get().getFullName() + " vừa đặt vé thành công.";

            Notification notification = createNotification(NotiType.BOOKING_SUCCESS,
                    "Giao dịch mới: Đặt vé.",
                    message2,
                    NotiTarget.ADMIN_MANAGER,
                    new TicketDetailRes(booking, user.get()));

            List<Staff> staffs = staffRepository.findAllByRoleAndCinema(roleService.findByRoleName("STAFF"), booking.getSeats().get(0).getShowTime().getRoom().getCinema());
            for (User item : staffs) {
                notificationUserRepository.save(new NotificationUser(item, notification));
                Optional<UserTokenFCM> tokenStaff = userTokenRepository.findByUserId(item.getUserId());
                if (tokenStaff.isPresent())
                    fcmService.sendNotification(tokenStaff.get().getToken(), "Giao dịch mới: Đặt vé.", message2);
            }
            List<User> users = userRepository.findAllByRole(roleService.findByRoleName("ADMIN"));
            for (User item : users) {
                notificationUserRepository.save(new NotificationUser(item, notification));
                Optional<UserTokenFCM> tokenAdmin = userTokenRepository.findByUserId(item.getUserId());
                if (tokenAdmin.isPresent())
                    fcmService.sendNotification(tokenAdmin.get().getToken(), "Giao dịch mới: Đặt vé.", message2);
            }
            Optional<Manager> manager = managerService.getManagerByCinema(booking.getSeats().get(0).getShowTime().getRoom().getCinema());
            if (manager.isPresent()) {
                notificationUserRepository.save(new NotificationUser(manager.get(), notification));
                Optional<UserTokenFCM> tokenManager = userTokenRepository.findByUserId(manager.get().getUserId());
                if (tokenManager.isPresent())
                    fcmService.sendNotification(tokenManager.get().getToken(), "Giao dịch mới: Đặt vé.", message2);
            }
        }
    }

    public void sellTicketSuccessNotification(Booking booking, User staff) throws FirebaseMessagingException {
        Optional<User> user = userRepository.findById(booking.getUserId());
        if (user.isPresent()) {
            sendNotiForBookingSuccessToUser(booking, user.get());
        }

        String message2 = "Nhân viên "+staff.getFullName()+" bán vé cho phim "+booking.getSeats().get(0).getShowTime().getMovie().getTitle()+" thành công";

        Notification notification = createNotification(NotiType.BOOKING_SUCCESS,
                "Giao dịch mới: Bán vé.",
                message2,
                NotiTarget.ADMIN_MANAGER,
                new TicketDetailRes(booking, user.orElse(null)));
        //Gửi thông báo cho staff
        notificationUserRepository.save(new NotificationUser(staff, notification));
        Optional<UserTokenFCM> tokenStaff = userTokenRepository.findByUserId(staff.getUserId());
        if (tokenStaff.isPresent())
            fcmService.sendNotification(tokenStaff.get().getToken(), "Giao dịch mới: Bán vé.", message2);
        //Gửi thông báo cho manager
        Optional<Manager> manager = managerService.getManagerByCinema(booking.getSeats().get(0).getShowTime().getRoom().getCinema());
        if (manager.isPresent()) {
            notificationUserRepository.save(new NotificationUser(manager.get(), notification));
            Optional<UserTokenFCM> tokenManager = userTokenRepository.findByUserId(manager.get().getUserId());
            if (tokenManager.isPresent())
                fcmService.sendNotification(tokenManager.get().getToken(), "Giao dịch mới: Bán vé.", message2);
        }
    }

    public void sendNotiForBookingSuccessToUser(Booking booking, User user) throws FirebaseMessagingException {
        String message1 = "Bạn vừa đặt vé thành công cho phim " + booking.getSeats().get(0).getShowTime().getMovie().getTitle() + ". ";

        Notification notificationUser = createNotification(NotiType.BOOKING_SUCCESS,
                "Thông báo đặt vé.",
                message1,
                NotiTarget.VIEWER,
                new TicketDetailRes(booking, user));
        notificationUserRepository.save(new NotificationUser(user, notificationUser));
        Optional<UserTokenFCM> tokenUser = userTokenRepository.findByUserId(booking.getUserId());
        if (tokenUser.isPresent())
            fcmService.sendNotification(tokenUser.get().getToken(), "Thông báo đặt vé", message1);
    }

    public void ticketStatusNotification(Booking booking) throws FirebaseMessagingException {
        Optional<User> user = userRepository.findById(booking.getUserId());
        if (user.isPresent()) {
            StringBuilder messageUser = new StringBuilder("Bạn vừa ");
            if(booking.getTicketStatus().equals(TicketStatus.CANCELLED)) {
                messageUser.append("hủy vé thành công.");
                Notification notificationUser = createNotification(NotiType.TICKET_STATUS,
                        "Thông báo hủy vé.",
                        messageUser.toString(),
                        NotiTarget.VIEWER,
                        new TicketDetailRes(booking, user.get()));
                notificationUserRepository.save(new NotificationUser(user.get(), notificationUser));
                Optional<UserTokenFCM> token = userTokenRepository.findByUserId(booking.getUserId());
                if (token.isPresent())
                    fcmService.sendNotification(token.get().getToken(), "Thông báo hủy vé.", messageUser.toString());

                //Gửi cho admin, manager
                String message = "Khách hàng "+ user.get().getFullName()+" vừa hủy vé.";
                Notification notification = createNotification(NotiType.TICKET_STATUS,
                        "Giao dịch mới: Hủy vé.",
                        message,
                        NotiTarget.ADMIN_MANAGER,
                        new TicketDetailRes(booking, user.get()));

                List<User> users = userRepository.findAllByRole(roleService.findByRoleName("ADMIN"));
                for (User item : users) {
                    notificationUserRepository.save(new NotificationUser(item, notification));
                    Optional<UserTokenFCM> tokenAdmin = userTokenRepository.findByUserId(item.getUserId());
                    if (tokenAdmin.isPresent())
                        fcmService.sendNotification(tokenAdmin.get().getToken(), "Giao dịch mới: Hủy vé.", message);
                }
                Optional<Manager> manager = managerService.getManagerByCinema(booking.getSeats().get(0).getShowTime().getRoom().getCinema());
                if (manager.isPresent()){
                    notificationUserRepository.save(new NotificationUser(manager.get(), notification));
                    Optional<UserTokenFCM> tokenManager = userTokenRepository.findByUserId(manager.get().getUserId());
                    if (tokenManager.isPresent())
                        fcmService.sendNotification(tokenManager.get().getToken(), "Giao dịch mới: Hủy vé.", message);
                }
            } else if(booking.getTicketStatus().equals(TicketStatus.CONFIRMED)) {
                messageUser.append("xác thực vé vào cửa thành công.");
                Notification notification = createNotification(NotiType.TICKET_STATUS,
                        "Thông báo xác thực vé.",
                        messageUser.toString(),
                        NotiTarget.VIEWER,
                        new TicketDetailRes(booking, user.get()));
                notificationUserRepository.save(new NotificationUser(user.get(), notification));
                Optional<UserTokenFCM> token = userTokenRepository.findByUserId(booking.getUserId());
                if (token.isPresent())
                    fcmService.sendNotification(token.get().getToken(), "Thông báo xác thực vé.", messageUser.toString());
            }
        }
    }

    public void reviewNotification(Review review, User user) throws FirebaseMessagingException {
        String message = "Khách hàng " + user.getFullName() + " đã thêm một đánh giá mới cho phim " + review.getMovieName() + ".";
        Notification notification = createNotification(NotiType.REVIEW,
                "Đánh giá mới.",
                message,
                NotiTarget.ADMIN_MANAGER,
                review);
        List<User> users = userRepository.findAllByRole(roleService.findByRoleName("ADMIN"));
        for (User item : users) {
            notificationUserRepository.save(new NotificationUser(item, notification));
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(item.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "Đánh giá mới.", message);
        }
        for (User item : managerService.findAll()) {
            notificationUserRepository.save(new NotificationUser(item, notification));
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(item.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "Đánh giá mới.", message);
        }
    }

    public void promotionNotification(PromotionFixed promotion) throws FirebaseMessagingException {
        sendNotificationToAllViewer("PROMOTION", promotion.getName(), promotion.getDescription(), promotion);
    }

    @Scheduled(cron = "0 0 5 * * *", zone = "GMT+7")
    public void lowStockNotification() throws FirebaseMessagingException {
        List<Food> foods = foodRepository.findAllByStatusIsTrue();
        List<Cinema> cinemas = cinemaRepository.findAll();
        for (Food food : foods) {
            for (Cinema cinema : cinemas) {
                Optional<Manager> manager = managerService.getManagerByCinema(cinema);
                if (manager.isPresent()){
                    Optional<FoodInventory> inventory = foodInventoryRepository.findByFoodAndCinema(food, cinema);
                    if ((inventory.isPresent() && inventory.get().getQuantity() <= 100) || inventory.isEmpty()) {
                        String message = "Sản phầm " + food.getName() + (inventory.isEmpty()?" chưa nhập hàng về.":" còn lại rất ít.");
                        Notification notification = createNotification(NotiType.LOW_STOCK,
                                "Thông báo sản phẩm.",
                                message,
                                NotiTarget.ADMIN_MANAGER,
                                inventory.orElse(null));
                        notificationUserRepository.save(new NotificationUser(manager.get(), notification));
                        Optional<UserTokenFCM> token = userTokenRepository.findByUserId(manager.get().getUserId());
                        if (token.isPresent())
                            fcmService.sendNotification(token.get().getToken(), "Thông báo sản phẩm.", message);
                    }
                }
            }
        }
    }

    @Scheduled(fixedDelay = 120000) // 2 phut
    public void checkShowtimeReminders() throws FirebaseMessagingException {
        List<Booking> bookings = bookingRepository.findAllByIsPaymentIsTrue();
        List<BookingSchedule> bookingSchedules = bookings.stream().map(BookingSchedule::new).toList();
        LocalTime oneHour = LocalTime.now().plusHours(1);
        for (BookingSchedule item : bookingSchedules) {
            if (item.getSchedule().getDate().equals(LocalDate.now()) && !item.getSchedule().getStartTime().isAfter(oneHour) && item.getSchedule().getStartTime().isAfter(oneHour.minusMinutes(3))) {
                Optional<User> user = userRepository.findById(item.getBooking().getUserId());
                if (user.isPresent()) {
                    StringBuilder message = new StringBuilder("Chỉ dưới 1 tiếng nữa là bộ phim " + item.getBooking().getSeats().get(0).getShowTime().getMovie().getTitle() + " bạn mong chờ sẽ bắt đầu tại rạp "+item.getBooking().getSeats().get(0).getShowTime().getRoom().getCinema().getCinemaName()+". ");
                    message.append("Đừng quên chuẩn bị tinh thần để thưởng thức những thước phim hấp dẫn nhé! Hẹn gặp bạn tại rạp!");
                    Notification notification = createNotification(NotiType.TICKET_REMINDER,
                            item.getBooking().getSeats().get(0).getShowTime().getMovie().getTitle()+" sắp chiếu!(chưa đầy 1 tiếng nữa)",
                            message.toString(),
                            NotiTarget.VIEWER,
                            new TicketDetailRes(item.getBooking(), user.get()));
                    notificationUserRepository.save(new NotificationUser(user.get(), notification));
                    Optional<UserTokenFCM> token = userTokenRepository.findByUserId(item.getBooking().getUserId());
                    if (token.isPresent())
                        fcmService.sendNotification(token.get().getToken(), "Phim sắp chiếu.", message.toString());
                }
            }
        }
    }

    public ResponseEntity<GenericResponse> getNotifications(String userId, Pageable pageable) {
        try {
            Page<NotificationUser> notifications = notificationUserRepository.findAllByUser_UserIdOrderByNotificationUserIdDesc(userId, pageable);
            Map<String, Object> result = new HashMap<>();
            result.put("content", notifications.getContent().stream().map(NotificationRes::new).collect(Collectors.toList()));
            result.put("pageNumber", notifications.getPageable().getPageNumber()+1);
            result.put("pageSize", notifications.getSize());
            result.put("totalPages", notifications.getTotalPages());
            result.put("totalElements", notifications.getTotalElements());
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách thông báo thành công!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getNotification(String notificationUserId) {
        try {
            Optional<NotificationUser> notification = notificationUserRepository.findById(notificationUserId);
            if (notification.isPresent()) {
                notification.get().setRead(true);
                notificationUserRepository.save(notification.get());
                return ResponseEntity.ok()
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Lấy thông báo thành công!")
                                .result(new NotificationRes(notification.get()))
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy thông báo!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> adminSendNotification(NotificationReq notificationReq) {
        try {
            List<User> usersToNotify;
            Notification notification;
            switch (notificationReq.getSendTo()) {
                case "ALL":
                    notification = switch (notificationReq.getRole()) {
                        case "MANAGER" -> {
                            usersToNotify = userRepository.findAllByRole(roleService.findByRoleName("MANAGER"));
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.ADMIN_MANAGER, usersToNotify);
                        }
                        case "VIEWER" -> {
                            usersToNotify = userRepository.findAllByRole(roleService.findByRoleName("VIEWER"));
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.VIEWER, usersToNotify);
                        }
                        case "STAFF" -> {
                            usersToNotify = userRepository.findAllByRole(roleService.findByRoleName("STAFF"));
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.STAFF, usersToNotify);
                        }
                        default -> throw new IllegalArgumentException("Đối tượng gửi thông báo không hợp lệ.");
                    };
                    return ResponseEntity.ok()
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Gửi thông báo thành công!")
                                    .result(notification)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                case "SPECIFIC":
                    notification = switch (notificationReq.getRole()) {
                        case "MANAGER" -> {
                            usersToNotify = userRepository.findAllByUserIdIn(notificationReq.getUserIds());
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.ADMIN_MANAGER, usersToNotify);
                        }
                        case "VIEWER" -> {
                            usersToNotify = userRepository.findAllByUserIdIn(notificationReq.getUserIds());
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.VIEWER, usersToNotify);
                        }
                        case "STAFF" -> {
                            usersToNotify = userRepository.findAllByUserIdIn(notificationReq.getUserIds());
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.STAFF, usersToNotify);
                        }
                        default -> throw new IllegalArgumentException("Đối tượng gửi thông báo không hợp lệ.");
                    };
                    return ResponseEntity.ok()
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Gửi thông báo thành công!")
                                    .result(notification)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                default:
                    throw new IllegalArgumentException("Đối tượng gửi thông báo không hợp lệ.");
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> managerSendNotification(NotificationReq notificationReq, String managerId) {
        try {
            List<User> usersToNotify;
            Notification notification;
            switch (notificationReq.getSendTo()) {
                case "ALL":
                    notification = switch (notificationReq.getRole()) {
                        case "VIEWER" -> {
                            usersToNotify = userRepository.findAllByRole(roleService.findByRoleName("VIEWER"));
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.VIEWER, usersToNotify);
                        }
                        case "STAFF" -> {
                            Optional<Manager> manager = managerRepository.findById(managerId);
                            if (manager.isEmpty())
                                throw new UserNotFoundException("Không tìm thấy quản lý. Vui lòng đăng nhập lại!");
                            usersToNotify = new ArrayList<>(staffRepository.findAllByRoleAndCinema(roleService.findByRoleName("STAFF"), manager.get().getCinema()));
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.STAFF, usersToNotify);
                        }
                        default -> throw new IllegalArgumentException("Đối tượng gửi thông báo không hợp lệ.");
                    };
                    return ResponseEntity.ok()
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Gửi thông báo thành công!")
                                    .result(notification)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                case "SPECIFIC":
                    notification = switch (notificationReq.getRole()) {
                        case "VIEWER" -> {
                            usersToNotify = userRepository.findAllByUserIdIn(notificationReq.getUserIds());
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.VIEWER, usersToNotify);
                        }
                        case "STAFF" -> {
                            usersToNotify = userRepository.findAllByUserIdIn(notificationReq.getUserIds());
                            yield sendNotificationToUsers(NotiType.valueOf(notificationReq.getType()), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.STAFF, usersToNotify);
                        }
                        default -> throw new IllegalArgumentException("Đối tượng gửi thông báo không hợp lệ.");
                    };
                    return ResponseEntity.ok()
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Gửi thông báo thành công!")
                                    .result(notification)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                default:
                    throw new IllegalArgumentException("Đối tượng gửi thông báo không hợp lệ.");
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Notification sendNotificationToUsers(NotiType type, String title, String message, NotiTarget notiTarget, List<User> users) throws FirebaseMessagingException {
        Notification notification = createNotification(type, title, message, notiTarget, null);

        List<NotificationUser> notificationsUsers = new ArrayList<>();
        for (User user : users) {
            NotificationUser notificationUser = new NotificationUser();
            notificationUser.setUser(user);
            notificationUser.setNotification(notification);
            notificationUser.setRead(false);
            notificationUser.setCreatedAt(LocalDateTime.now());
            notificationUser.setUpdatedAt(LocalDateTime.now());
            notificationsUsers.add(notificationUser);
        }

        notificationUserRepository.saveAll(notificationsUsers);

        for (User user : users) {
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(user.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), title, message);
        }

        return notification;
    }

    public ResponseEntity<GenericResponse> notReadCount(String userId) {
        try {
            long count = notificationUserRepository.countAllByUser_UserIdAndReadIsFalse(userId);

            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy số lượng thông báo chưa đọc thành công!")
                            .result(count)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> readAllNotification(String userId) {
        try {
            List<NotificationUser> notifications = notificationUserRepository.findAllByUser_UserIdAndReadIsFalse(userId);
            notifications.forEach(item -> item.setRead(true));
            notificationUserRepository.saveAll(notifications);
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Đã đọc tất cả thông báo!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
