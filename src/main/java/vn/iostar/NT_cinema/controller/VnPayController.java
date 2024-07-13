package vn.iostar.NT_cinema.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.config.VnPayConfig;
import vn.iostar.NT_cinema.dto.FoodWithCount;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.exception.NotFoundException;
import vn.iostar.NT_cinema.repository.*;
import vn.iostar.NT_cinema.service.BookingService;
import vn.iostar.NT_cinema.service.NotificationService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1/vnpay")
public class VnPayController {
    @Autowired
    BookingService bookingService;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    SeatRepository seatRepository;
    @Autowired
    FoodInventoryRepository foodInventoryRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    PromotionCodeRepository promotionCodeRepository;
    @Autowired
    PromotionCodeUsageRepository promotionCodeUsageRepository;
    @Autowired
    NotificationService notificationService;

    @GetMapping("/payment")
    public ResponseEntity<GenericResponse> createPayment(@RequestParam() String bookingId, HttpServletRequest request) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty())
            throw new NotFoundException("Không tìm thấy đơn đặt.");
        if (booking.get().isPayment()){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Hóa đơn đã được thanh toán.")
                            .result(booking.get())
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
        }
        String orderType = "other";
        long amount = booking.get().getTotal().intValue()* 100L;
        String bankCode = "NCB";

        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = VnPayConfig.getIpAddress(request);

        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VnPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VnPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl+"?bookingId="+bookingId);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+8"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 75);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VnPayConfig.vnp_PayUrl + "?" + queryUrl;

        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Thanh toán thành công!")
                        .result(paymentUrl)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @GetMapping("/callback")
    public void updateBookingPayment(@RequestParam() String bookingId, @RequestParam() String vnp_TransactionStatus, HttpServletResponse response) throws IOException, FirebaseMessagingException {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (Objects.equals(vnp_TransactionStatus, "00")){
            if (booking.isPresent()) {
                booking.get().setPayment(true);
                bookingRepository.save(booking.get());
                bookingService.sendEmailBookingSuccess(booking.get());
                bookingService.handleBookingChange(booking.get());
                notificationService.bookingTicketSuccessNotification(booking.get());

                for (Seat item: booking.get().getSeats()) {
                    Ticket ticket = new Ticket();
                    ticket.setUserId(booking.get().getUserId());
                    ShowTime showTime = item.getShowTime();
                    Cinema cinema = showTime.getRoom().getCinema();
                    ticket.setCinemaName(cinema.getCinemaName());
                    ticket.setCinemaAddress(cinema.getLocation());
                    ticket.setCreateAt(new Date());
                    ticket.setMovieName(showTime.getMovie().getTitle());
                    ticket.setDate(item.getSchedule().getDate());
                    ticket.setStartTime(item.getSchedule().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    ticket.setDuration(showTime.getMovie().getDuration());
                    ticket.setSeat(item.getPrice().getType().toString()+" Class: "+item.convertToUnicode()+item.getColumn());
                    ticket.setTicketPrice(item.getPrice().getPrice());
                    ticketRepository.save(ticket);
                }

                for (FoodWithCount item: booking.get().getFoods()) {
                    Food food = item.getFood();
                    food.setQuantity(food.getQuantity() - item.getCount());
                    foodRepository.save(food);
                    Cinema cinema = booking.get().getSeats().get(0).getShowTime().getRoom().getCinema();
                    Optional<FoodInventory> foodInventory = foodInventoryRepository.findByFoodAndCinema(food, cinema);
                    if (foodInventory.isPresent()) {
                        foodInventory.get().setQuantity(foodInventory.get().getQuantity() - item.getCount());
                        foodInventory.get().setUpdateAt(new Date());
                        foodInventoryRepository.save(foodInventory.get());
                    }
                }
                if (booking.get().getPromotionCode() != null) {
                    PromotionCodeUsage usage = new PromotionCodeUsage();
                    usage.setUserId(booking.get().getUserId());
                    usage.setPromotionCodeId(booking.get().getPromotionCode().getPromotionCodeId());
                    usage.setDateUsed(LocalDate.now());
                    promotionCodeUsageRepository.save(usage);
                }
                response.sendRedirect("http://localhost:5173/user/payment-success");
            }
        }else {
            if (booking.isPresent()){
                List<Seat> seats = booking.get().getSeats();
                for (Seat item : seats){
                    item.setStatus(true);
                    seatRepository.save(item);
                }
                bookingRepository.delete(booking.get());
            }
            response.sendRedirect("http://localhost:5173/user/payment-false");
        }
    }
}
