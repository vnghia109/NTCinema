package vn.iostar.NT_cinema.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.NotiTarget;
import vn.iostar.NT_cinema.constant.NotiType;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.NotificationReq;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

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

    public Notification createNotification(NotiType type, String title, String message, NotiTarget target) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTarget(target);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
    public void sendNotificationToAllViewer(String type, String title, String message) throws FirebaseMessagingException {
        Notification notification = createNotification(NotiType.valueOf(type), title, message, NotiTarget.VIEWER);
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
            StringBuilder message1 = new StringBuilder("Bạn vừa đặt vé thành công cho " + booking.getSeats().size() + " ghế: ");
            for (Seat seat : booking.getSeats()) {
                message1.append(seat.convertToUnicode()).append(seat.getColumn()).append(", ");
            }
            LocalDateTime start = LocalDateTime.of(booking.getSeats().get(0).getSchedule().getDate(), booking.getSeats().get(0).getSchedule().getStartTime());
            LocalDateTime end = start.plusMinutes(Long.parseLong(booking.getSeats().get(0).getShowTime().getMovie().getDuration()));
            message1.append("của phim ").append(booking.getSeats().get(0).getShowTime().getMovie().getTitle())
                    .append(" bắt đầu chiếu lúc ").append(start)
                    .append(" kết thúc vào ").append(end);
            Notification notification = createNotification(NotiType.BOOKING_SUCCESS,
                    "ĐẶT VÉ THÀNH CÔNG",
                    message1.toString(),
                    NotiTarget.VIEWER);
            notificationUserRepository.save(new NotificationUser(user.get(), notification));
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(booking.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "ĐẶT VÉ THÀNH CÔNG", message1.toString());
        }
        StringBuilder message2 = new StringBuilder("Vừa bán một vé thành công cho " + booking.getSeats().size() + " ghế: ");
        for (Seat seat : booking.getSeats()) {
            message2.append(seat.convertToUnicode()).append(seat.getColumn()).append(" ");
        }
        LocalDateTime start = LocalDateTime.of(booking.getSeats().get(0).getSchedule().getDate(), booking.getSeats().get(0).getSchedule().getStartTime());
        LocalDateTime end = start.plusMinutes(Long.parseLong(booking.getSeats().get(0).getShowTime().getMovie().getDuration()));
        message2.append("của phim ").append(booking.getSeats().get(0).getShowTime().getMovie().getTitle())
                .append(", suất chiếu ").append(start)
                .append(" - ").append(end);
        Notification notification = createNotification(NotiType.BOOKING_SUCCESS,
                "MỘT VÉ VỪA BÁN",
                message2.toString(),
                NotiTarget.ADMIN_MANAGER);
        List<Role> roles = new ArrayList<>();
        roles.add(roleService.findByRoleName("ADMIN"));
        List<User> users = userRepository.findAllByRoleIn(roles);
        for (User item : users) {
            notificationUserRepository.save(new NotificationUser(item, notification));
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(item.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "MỘT VÉ VỪA BÁN", message2.toString());
        }
        Optional<Manager> manager = managerService.getManagerByCinema(booking.getSeats().get(0).getShowTime().getRoom().getCinema());
        if (manager.isPresent()) {
            notificationUserRepository.save(new NotificationUser(manager.get(), notification));
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(manager.get().getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "MỘT VÉ VỪA BÁN", message2.toString());
        }
    }

    public void ticketStatusNotification(Booking booking) throws FirebaseMessagingException {
        Optional<User> user = userRepository.findById(booking.getUserId());
        if (user.isPresent()) {
            StringBuilder message = new StringBuilder("Bạn vừa ");
            if(booking.getTicketStatus().equals(TicketStatus.CANCELLED)) {
                message.append("hủy vé xem phim cho ").append(booking.getSeats().size()).append(" ghế: ");
                for (Seat seat : booking.getSeats()) {
                    message.append(seat.convertToUnicode()).append(seat.getColumn()).append(", ");
                }
                message.append("của phim ").append(booking.getSeats().get(0).getShowTime().getMovie().getTitle());
                Notification notification = createNotification(NotiType.TICKET_STATUS,
                        "HỦY VÉ",
                        message.toString(),
                        NotiTarget.VIEWER);
                notificationUserRepository.save(new NotificationUser(user.get(), notification));
                Optional<UserTokenFCM> token = userTokenRepository.findByUserId(booking.getUserId());
                if (token.isPresent())
                    fcmService.sendNotification(token.get().getToken(), "ĐẶT VÉ THÀNH CÔNG", message.toString());

                StringBuilder message2 = new StringBuilder("Một vé xem phim đã bị hủy. Thông tin chi tiết: Khách hàng "+ user.get().getFullName());
                message2.append(", ").append(booking.getSeats().size()).append(" ghế: ");
                for (Seat seat : booking.getSeats()) {
                    message2.append(seat.convertToUnicode()).append(seat.getColumn()).append(" ");
                }
                message2.deleteCharAt(message2.length() - 1);
                LocalDateTime start = LocalDateTime.of(booking.getSeats().get(0).getSchedule().getDate(), booking.getSeats().get(0).getSchedule().getStartTime());
                LocalDateTime end = start.plusMinutes(Long.parseLong(booking.getSeats().get(0).getShowTime().getMovie().getDuration()));
                message2.append(", phim ").append(booking.getSeats().get(0).getShowTime().getMovie().getTitle())
                        .append(", suất chiếu ").append(start)
                        .append(" - ").append(end);
                Notification notification1 = createNotification(NotiType.TICKET_STATUS,
                        "MỘT VÉ ĐÃ BỊ HỦY",
                        message2.toString(),
                        NotiTarget.ADMIN_MANAGER);
                List<Role> roles = new ArrayList<>();
                roles.add(roleService.findByRoleName("ADMIN"));
                List<User> users = userRepository.findAllByRoleIn(roles);
                for (User item : users) {
                    notificationUserRepository.save(new NotificationUser(item, notification1));
                    Optional<UserTokenFCM> tokenAdmin = userTokenRepository.findByUserId(item.getUserId());
                    if (tokenAdmin.isPresent())
                        fcmService.sendNotification(tokenAdmin.get().getToken(), "MỘT VÉ ĐÃ BỊ HỦY", message2.toString());
                }
                Optional<Manager> manager = managerService.getManagerByCinema(booking.getSeats().get(0).getShowTime().getRoom().getCinema());
                if (manager.isPresent()){
                    notificationUserRepository.save(new NotificationUser(manager.get(), notification1));
                    Optional<UserTokenFCM> tokenManager = userTokenRepository.findByUserId(manager.get().getUserId());
                    if (tokenManager.isPresent())
                        fcmService.sendNotification(tokenManager.get().getToken(), "MỘT VÉ ĐÃ BỊ HỦY", message2.toString());
                }
            } else if(booking.getTicketStatus().equals(TicketStatus.CONFIRMED)) {
                message.append("xác thực vé vào cửa thành công cho ").append(booking.getSeats().size()).append(" ghế: ");
                for (Seat seat : booking.getSeats()) {
                    message.append(seat.convertToUnicode()).append(seat.getColumn()).append(", ");
                }
                LocalDateTime start = LocalDateTime.of(booking.getSeats().get(0).getSchedule().getDate(), booking.getSeats().get(0).getSchedule().getStartTime());
                LocalDateTime end = start.plusMinutes(Long.parseLong(booking.getSeats().get(0).getShowTime().getMovie().getDuration()));
                message.append("của phim ").append(booking.getSeats().get(0).getShowTime().getMovie().getTitle())
                        .append(" bắt đầu chiếu lúc ").append(start)
                        .append(" kết thúc vào ").append(end);
                Notification notification = createNotification(NotiType.TICKET_STATUS,
                        "XÁC THỰC VÉ VÀO CỬA",
                        message.toString(),
                        NotiTarget.VIEWER);
                notificationUserRepository.save(new NotificationUser(user.get(), notification));
                Optional<UserTokenFCM> token = userTokenRepository.findByUserId(booking.getUserId());
                if (token.isPresent())
                    fcmService.sendNotification(token.get().getToken(), "ĐẶT VÉ THÀNH CÔNG", message.toString());
            }
        }
    }

    public void reviewNotification(Review review, User user) throws FirebaseMessagingException {
        String message = "Người dùng " + user.getFullName() + " đã thêm một đánh giá mới cho phim " + review.getMovieName() + ". Nội dung đánh giá: " + review.getComment() +
                ", số sao: " + review.getRating()+".";
        Notification notification = createNotification(NotiType.REVIEW,
                "ĐÁNH GIÁ MỚI",
                message,
                NotiTarget.ADMIN_MANAGER);
        List<Role> roles = new ArrayList<>();
        roles.add(roleService.findByRoleName("ADMIN"));
        List<User> users = userRepository.findAllByRoleIn(roles);
        for (User item : users) {
            notificationUserRepository.save(new NotificationUser(item, notification));
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(item.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "ĐÁNH GIÁ MỚI", message);
        }
        for (User item : managerService.findAll()) {
            notificationUserRepository.save(new NotificationUser(item, notification));
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(item.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "ĐÁNH GIÁ MỚI", message);
        }
    }

    public void promotionNotification(PromotionFixed promotion) throws FirebaseMessagingException {
        sendNotificationToAllViewer("PROMOTION", promotion.getName(), promotion.getDescription());
    }

    @Scheduled(cron = "0 0 5 * * *", zone = "GMT+7")
    public void lowStockNotification() throws FirebaseMessagingException {
        List<Food> foods = foodRepository.findAll();
        List<Cinema> cinemas = cinemaRepository.findAll();
        for (Food food : foods) {
            for (Cinema cinema : cinemas) {
                Optional<Manager> manager = managerService.getManagerByCinema(cinema);
                if (manager.isPresent()){
                    Optional<FoodInventory> inventory = foodInventoryRepository.findByFoodAndCinema(food, cinema);
                    if ((inventory.isPresent() && inventory.get().getQuantity() <= 100) || inventory.isEmpty()) {
                        StringBuilder message = new StringBuilder("Sản phầm " + food.getName() + " sắp hết hàng. ")
                                .append("Số lượng còn lại: ").append(inventory.map(FoodInventory::getQuantity).orElse(0));
                        Notification notification = createNotification(NotiType.LOW_STOCK,
                                "SẢN PHẨM SẮP HẾT HÀNG",
                                message.toString(),
                                NotiTarget.ADMIN_MANAGER);
                            notificationUserRepository.save(new NotificationUser(manager.get(), notification));
                            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(manager.get().getUserId());
                            if (token.isPresent())
                                fcmService.sendNotification(token.get().getToken(), "SẢN PHẨM SẮP HẾT HÀNG", message.toString());
                    }
                }
            }
        }
    }

    @Scheduled(fixedDelay = 60000) //1 minutes
    public void checkShowtimeReminders() throws FirebaseMessagingException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);
        // Bước 1: Lookup để join với collection 'schedules'
        LookupOperation lookupSchedules = LookupOperation.newLookup()
                .from("schedules")
                .localField("seats.schedule._id")
                .foreignField("_id")
                .as("scheduleInfo");

        // Bước 2: Unwind để làm phẳng mảng scheduleInfo
        UnwindOperation unwindScheduleInfo = unwind("scheduleInfo");

        // Bước 3: Match để lọc các điều kiện
        MatchOperation matchCriteria = match(new Criteria()
                .andOperator(
                        Criteria.where("isPayment").is(true),
                        Criteria.where("ticketStatus").ne(TicketStatus.CANCELLED),
                        Criteria.where("scheduleInfo.date").is(now.toLocalDate()),
                        Criteria.where("scheduleInfo.startTime").gte(oneHourLater.minusMinutes(5).toLocalTime()), // Bắt đầu từ bây giờ
                        Criteria.where("scheduleInfo.startTime").lte(oneHourLater.toLocalTime()) // Đến 1 tiếng sau
                ));

        // Bước 4: Project để chỉ lấy các trường cần thiết (userId, showtimeId, ...)
        ProjectionOperation projectFields = project("userId", "seats", "seats.schedule.startTime"); // Lấy giờ bắt đầu

        Aggregation aggregation = newAggregation(lookupSchedules, unwindScheduleInfo, matchCriteria, projectFields);

        List<Booking> bookings = mongoTemplate.aggregate(aggregation, Booking.class, Booking.class).getMappedResults();

        for (Booking booking : bookings) {
            StringBuilder message = new StringBuilder("Chỉ dưới 1 tiếng nữa là bộ phim " + booking.getSeats().get(0).getShowTime().getMovie().getTitle() + " bạn mong chờ sẽ bắt đầu tại rạp "+booking.getSeats().get(0).getShowTime().getRoom().getCinema().getCinemaName()+". ");
            message.append("Đừng quên chuẩn bị tinh thần để thưởng thức những thước phim hấp dẫn nhé! Hẹn gặp bạn tại rạp!");
            createNotification(NotiType.TICKET_REMINDER,
                    booking.getSeats().get(0).getShowTime().getMovie().getTitle()+" sắp chiếu!(còn 1 tiếng nữa)",
                    message.toString(),
                    NotiTarget.VIEWER);
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(booking.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "PHIM CỦA BẠN SẮP ĐẾN GIỜ CHIẾU", message.toString());
        }
    }

    public ResponseEntity<GenericResponse> getNotifications(String userId, Pageable pageable) {
        try {
            Page<NotificationUser> notifications = notificationUserRepository.findAllByUser_UserId(userId, pageable);
            Map<String, Object> result = new HashMap<>();
            result.put("content", notifications.getContent().stream().map(NotificationUser::getNotification).collect(Collectors.toList()));
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Lỗi máy chủ. " + e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
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
                                .result(notification.get())
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Lỗi máy chủ. " + e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
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
                            usersToNotify = userRepository.findAllByRole_RoleName("MANAGER");
                            yield sendNotificationToUsers(notificationReq.getType(), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.ADMIN_MANAGER, usersToNotify);
                        }
                        case "VIEWER" -> {
                            usersToNotify = userRepository.findAllByRole_RoleName("VIEWER");
                            yield sendNotificationToUsers(notificationReq.getType(), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.VIEWER, usersToNotify);
                        }
                        case "STAFF" -> {
                            usersToNotify = userRepository.findAllByRole_RoleName("STAFF");
                            yield sendNotificationToUsers(notificationReq.getType(), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.STAFF, usersToNotify);
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
                            yield sendNotificationToUsers(notificationReq.getType(), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.ADMIN_MANAGER, usersToNotify);
                        }
                        case "VIEWER" -> {
                            usersToNotify = userRepository.findAllByUserIdIn(notificationReq.getUserIds());
                            yield sendNotificationToUsers(notificationReq.getType(), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.VIEWER, usersToNotify);
                        }
                        case "STAFF" -> {
                            usersToNotify = userRepository.findAllByUserIdIn(notificationReq.getUserIds());
                            yield sendNotificationToUsers(notificationReq.getType(), notificationReq.getTitle(), notificationReq.getMessage(), NotiTarget.STAFF, usersToNotify);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Lỗi máy chủ. " + e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public Notification sendNotificationToUsers(NotiType type, String title, String message, NotiTarget notiTarget, List<User> users) throws FirebaseMessagingException {
        Notification notification = createNotification(type, title, message, notiTarget);

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
}
