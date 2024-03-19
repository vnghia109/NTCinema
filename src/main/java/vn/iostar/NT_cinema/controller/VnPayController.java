package vn.iostar.NT_cinema.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.config.VnPayConfig;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.BookingRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;
import vn.iostar.NT_cinema.repository.TicketRepository;
import vn.iostar.NT_cinema.service.BookingService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
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
    @GetMapping("/payment")
    public ResponseEntity<GenericResponse> createPayment(@RequestParam() String bookingId) throws UnsupportedEncodingException {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
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
        long amount = booking.get().getTotal()*100;
        String bankCode = "NCB";

        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";

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

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 10);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
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
    public void updateBookingPayment(@RequestParam() String bookingId, @RequestParam() String vnp_TransactionStatus, HttpServletResponse response) throws IOException {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (Objects.equals(vnp_TransactionStatus, "00")){
            if (booking.isPresent()) {
                booking.get().setPayment(true);
                bookingRepository.save(booking.get());
                bookingService.sendEmailBookingSuccess(booking.get());
                for (Seat item: booking.get().getSeats()) {
                    Ticket ticket = new Ticket();
                    ticket.setUserId(booking.get().getUserId());
                    Optional<ShowTime> showTime = showTimeRepository.findById(item.getShowTimeId());
                    Cinema cinema = showTime.get().getRoom().getCinema();
                    ticket.setCinemaName(cinema.getCinemaName());
                    ticket.setCinemaAddress(cinema.getLocation());
                    ticket.setCreateAt(new Date());
                    ticket.setMovieName(showTime.get().getMovie().getTitle());
                    ticket.setShowtime(item.getTimeShow());
                    ticket.setDuration(showTime.get().getMovie().getDuration());
                    ticket.setSeat(item.getPrice().getType().toString()+"Class: row "+item.getRow()+"/column "+item.getColumn());
                    ticket.setTicketPrice(item.getPrice().getPrice());
                    ticketRepository.save(ticket);
                }
                response.sendRedirect("http://localhost:5173/user/payment-success");
            }
        }else {
            if (booking.isPresent()){
                List<Seat> seats = booking.get().getSeats();
                for (Seat item : seats){
                    item.setStatus(true);
                }
                bookingRepository.delete(booking.get());
            }
            response.sendRedirect("http://localhost:5173/user/payment-false");
        }
    }
}
