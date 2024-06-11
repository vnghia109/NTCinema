package vn.iostar.NT_cinema.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.NotiTarget;
import vn.iostar.NT_cinema.constant.NotiType;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.NotificationRepository;
import vn.iostar.NT_cinema.repository.NotificationUserRepository;
import vn.iostar.NT_cinema.repository.UserRepository;
import vn.iostar.NT_cinema.repository.UserTokenRepository;

import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public void sendNotificationToAllUsers(String type, String title, String message) {
        Notification notification = createNotification(NotiType.valueOf(type), title, message, NotiTarget.ALL);
        List<User> users = userRepository.findAll();

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
    }

    public void bookingTicketSuccessNotification(Booking booking) throws FirebaseMessagingException {
        StringBuilder message = new StringBuilder("Bạn vừa đặt vé thành công cho " + booking.getSeats().size() + " ghế: ");
        for (Seat seat : booking.getSeats()) {
            message.append(seat.convertToUnicode()).append(seat.getColumn()).append(", ");
        }
        LocalDateTime start = LocalDateTime.of(booking.getSeats().get(0).getSchedule().getDate(), booking.getSeats().get(0).getSchedule().getStartTime());
        LocalDateTime end = start.plusMinutes(Long.parseLong(booking.getSeats().get(0).getShowTime().getMovie().getDuration()));
        message.append("cho phim ").append(booking.getSeats().get(0).getShowTime().getMovie().getTitle())
                .append(" bắt đầu chiếu lúc ").append(start)
                .append(" kết thúc vào ").append(end);
        createNotification(NotiType.TICKET_CONFIRM,
                "ĐẶT VÉ THÀNH CÔNG",
                message.toString(),
                NotiTarget.USER);
        Optional<UserTokenFCM> token = userTokenRepository.findByUserId(booking.getUserId());
        if (token.isPresent())
            fcmService.sendNotification(token.get().getToken(), "ĐẶT VÉ THÀNH CÔNG", message.toString());
    }

    @Scheduled(fixedDelay = 60000) //1 minutes
    public void checkShowtimeReminders() throws FirebaseMessagingException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);
//        Criteria criteria = Criteria.where("isPayment").is(true)
//                .and("ticketStatus").ne(TicketStatus.CANCELLED)
//                .and("seats.0.schedule.date").is(now.toLocalDate())
//                .and("seats.0.schedule.startTime").gte(oneHourLater.minusMinutes(1).toLocalTime()).lt(oneHourLater.toLocalTime());
//        Query query = Query.query(criteria);
//        List<Booking> bookings = mongoTemplate.find(query, Booking.class);
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
                        Criteria.where("scheduleInfo.startTime").gte(oneHourLater.minusMinutes(1).toLocalTime()), // Bắt đầu từ bây giờ
                        Criteria.where("scheduleInfo.startTime").lte(oneHourLater.toLocalTime()) // Đến 1 tiếng sau
                ));

        // Bước 4: Project để chỉ lấy các trường cần thiết (userId, showtimeId, ...)
        ProjectionOperation projectFields = project("userId", "seats", "seats.schedule.startTime"); // Lấy giờ bắt đầu

        Aggregation aggregation = newAggregation(lookupSchedules, unwindScheduleInfo, matchCriteria, projectFields);

        List<Booking> bookings = mongoTemplate.aggregate(aggregation, Booking.class, Booking.class).getMappedResults();

        for (Booking booking : bookings) {
            StringBuilder message = new StringBuilder("Chỉ còn 1 tiếng nữa là bộ phim " + booking.getSeats().get(0).getShowTime().getMovie().getTitle() + " bạn mong chờ sẽ bắt đầu tại rạp "+booking.getSeats().get(0).getShowTime().getRoom().getCinema().getCinemaName()+". ");
            message.append("Đừng quên chuẩn bị tinh thần để thưởng thức những thước phim hấp dẫn nhé! Hẹn gặp bạn tại rạp!");
            createNotification(NotiType.TICKET_REMINDER,
                    booking.getSeats().get(0).getShowTime().getMovie().getTitle()+" sắp chiếu!(còn 1 tiếng nữa)",
                    message.toString(),
                    NotiTarget.USER);
            Optional<UserTokenFCM> token = userTokenRepository.findByUserId(booking.getUserId());
            if (token.isPresent())
                fcmService.sendNotification(token.get().getToken(), "PHIM CỦA BẠN SẮP ĐẾN GIỜ CHIẾU", message.toString());
        }
    }
}
