package com.hotel.army.controller;

import com.hotel.army.model.Guest;
import com.hotel.army.model.GuestRepository;
import com.hotel.army.model.Room;
import com.hotel.army.model.RoomRepository;
import com.hotel.army.model.Session;
import com.hotel.army.model.SessionRepository;
import com.hotel.army.model.Sweet;
import com.hotel.army.service.GuestService;
import com.hotel.army.service.SweetService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SweetService sweetService;
    private final GuestService guestService;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final SessionRepository sessionRepository;

    @GetMapping("/layout")
    public String returnHome(Model model) {
        return "layout";
    }

    @GetMapping("/home")
    public String home(Model model) {
        List<Sweet> sweetList = sweetService.getAll();
        List<Session> sessionList = sessionRepository.findByIsEnded(false);

        if (!sessionList.isEmpty()) {
            for (Session session : sessionList) {
                for (Sweet sweet : sweetList) {
                    for (Room room : sweet.getRoomList()) {
                        if (!room.isAvailable()) {
                            if (Objects.equals(session.getRoom().getId(), room.getId())) {
                                // Format and set the startedAt and endedAt properties

                                session.setStartedAt(formatDate(session.getStartedAt()));
                                if (session.getEndedAt().isEmpty()) {
                                    session.setEndedAt("غير محدد");
                                } else {
                                    session.setEndedAt(formatDate(session.getEndedAt()));
                                }

                                // Set the session and guest for the room
                                room.setSession(session);
                                room.setGuest(guestRepository.findById(session.getGuest().getId())
                                        .orElseThrow(() -> new EntityNotFoundException("Guest not found with ID: " + session.getGuest().getId())));
                            }
                        }
                    }
                }
            }
        }

        model.addAttribute("sweets", sweetList);
        return "home";
    }

    private String formatDate(String date) {
        // Parse the input string date
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);

        // Format the date using a desired pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return dateTime.format(formatter);
    }


    @GetMapping("/newSession/{roomId}")
    public String newSession(@PathVariable Long roomId, Model model) {
        List<Guest> guests = this.guestService.getAll();
        model.addAttribute("roomId", roomId);
        model.addAttribute("guests", guests);
        return "new-session";
    }


    @GetMapping("/sessionInfo/{roomId}/{sessionId}")
    public String sessionInfo(@PathVariable Long roomId, @PathVariable Long sessionId, Model model) {

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + roomId));


        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + sessionId));

        session.setStartedAt(formatDate(session.getStartedAt()));

        if (!session.getEndedAt().isEmpty()) {
            session.setEndedAt(formatDate(session.getEndedAt()));
        } else {
            session.setEndedAt("غير محدد");
        }


        room.setSession(session);

        Guest guest = guestRepository.findById(session.getGuest().getId()).orElseThrow(() -> new EntityNotFoundException("guest not found with ID: " + session.getGuest().getId()));

        model.addAttribute("room", room);
        model.addAttribute("session", session);
        model.addAttribute("guest", guest);

        return "sessionInfo";
    }

    @GetMapping("/sessionExit/{sessionId}")
    public String sessionExit(@PathVariable Long sessionId, Model model) {

        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + sessionId));
        model.addAttribute("session", session);
        return "sessionExit";
    }


    @ResponseBody
    @PostMapping("/sessionExit")
    public boolean sessionExit(
            @RequestParam("id") Long id,
            @RequestParam(name = "dueAmount", defaultValue = "0") Long dueAmount,
            @RequestParam(name = "inAdvanceAmount", defaultValue = "0") Long inAdvanceAmount,
            @RequestParam(name = "discountAmount", defaultValue = "0") Long discountAmount,
            @RequestParam(name = "returnedAmount", defaultValue = "0") Long returnedAmount,
            @RequestParam(name = "paidAmount", defaultValue = "0") Long paidAmount,
            @RequestParam(name = "remainsAmount", defaultValue = "0") Long remainsAmount

    ) {


        Session session = sessionRepository.findById(Long.valueOf(id)).orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + id));

        // Retrieve room and session
        Room room = roomRepository.findById(session.getRoom().getId()).orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + session.getRoom().getId()));

        // Update room availability
        room.setAvailable(true);
        roomRepository.save(room);

        // Set values for dependent properties first
        session.setDiscountAmount(discountAmount);
        session.setReturnedAmount(returnedAmount);

        // Set values for other properties based on the dependent ones
        session.setDueAmount(dueAmount);
        session.setInAdvanceAmount(inAdvanceAmount);
        session.setRemainsAmount(remainsAmount);
        session.setPaidAmount(paidAmount);


        // Set session end time
        session.setEndedAt(String.valueOf(LocalDateTime.now(ZoneId.of("Africa/Cairo"))));
        session.setEnded(true);

        // Save the updated session
        sessionRepository.save(session);

        return true;
    }


    @GetMapping("/roomDetails/{roomId}")
    public String roomDetails(@PathVariable Long roomId, Model model) {

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + roomId));
        List<Session> sessions = sessionRepository.findByRoom(room);
        long totalDueAmount = sessions.stream()
                .mapToLong(Session::getDueAmount)
                .sum();

        for (Session session : sessions) {
            session.setStartedAt(formatDate(session.getStartedAt()));
            if (!session.getEndedAt().isEmpty()) {
                session.setEndedAt(formatDate(session.getEndedAt()));
            } else {
                session.setEndedAt("غير محدد");
            }
        }
        var sessionsBefore = sessions;
        sessions.sort(Comparator.comparing(Session::getStartedAt).reversed());
        var sessionsAfter = sessions;

        model.addAttribute("sessions", sessions);
        model.addAttribute("totalDueAmount", totalDueAmount);
        System.out.println(sessions);
        return "roomDetails";
    }

    @GetMapping("/guests")
    public String clients(Model model) {

        List<Guest> guests = guestRepository.findAll();
        model.addAttribute("guests", guests);

        return "guests";
    }

    @GetMapping("/guest/{id}")
    public String guest(@PathVariable Long id, Model model) {

        Guest guest = guestRepository.findById(id).orElseThrow();
        model.addAttribute("guest", guest);

        return "guest";
    }

    @GetMapping("/guestHistory/{id}")
    public String guestHistory(@PathVariable Long id, Model model) {
        Guest guest = guestRepository.findById(id).orElseThrow();
        List<Session> sessions = sessionRepository.findByGuest(guest);
        for (Session session : sessions) {
            session.setStartedAt(formatDate(session.getStartedAt()));
            if (session.getEndedAt().isEmpty()) {
                session.setEndedAt("غير محدد");
            } else {
                session.setEndedAt(formatDate(session.getEndedAt()));
            }
        }
        model.addAttribute("sessions", sessions);
        return "guestHistory";
    }

    @ResponseBody
    @PostMapping("/updateGuest")
    public Guest updateGuest(
            @RequestParam("id") String id,
            @RequestParam("name") String name,
            @RequestParam("mobileNumber") String mobileNumber,
            @RequestParam("nationalId") String nationalId,
            @RequestParam("maritalStatus") String maritalStatus,
            @RequestParam("age") String age,
            @RequestParam("email") String email) {

        Guest guest = guestRepository.findById(Long.valueOf(id)).orElseThrow();
        guest.setNationalId(nationalId);
        guest.setAge(Integer.parseInt(age));
        guest.setEmail(email);
        guest.setMaritalStatus(maritalStatus);
        guest.setMobileNumber(mobileNumber);
        guest.setName(name);

        return guestRepository.saveAndFlush(guest);
    }

    @ResponseBody
    @GetMapping("/checkIfUserExists/{nationalId}")
    public Guest checkIfUserExists(@PathVariable String nationalId) {
        Guest guest = this.guestService.findOneWithNationalId(nationalId);
        if (guest != null) {
            return guest;
        } else {
            return new Guest();
        }
    }


    @ResponseBody
    @PostMapping("/createGuest")
    public Guest createGuest(@RequestParam("name") String name,
                             @RequestParam("mobileNumber") String mobileNumber,
                             @RequestParam("nationalId") String nationalId,
                             @RequestParam("maritalStatus") String maritalStatus,
                             @RequestParam("age") String age,
                             @RequestParam("email") String email) {

        Guest guest = Guest.builder()
                .age(Integer.parseInt(age))
                .name(name)
                .maritalStatus(maritalStatus)
                .nationalId(nationalId)
                .mobileNumber(mobileNumber)
                .email(email)
                .build();

        return this.guestService.createGuest(guest);
    }


    @ResponseBody
    @Transactional
    @PostMapping("/submitReservation")
    public Session submitReservation(
            @RequestParam("roomId") String roomId,
            @RequestParam("guestId") String guestId,
            @RequestParam("endedAt") String endedAt,
            @RequestParam("startedAt") String startedAt,
            @RequestParam("details") String details,
            @RequestParam("saves") String saves,
            @RequestParam("advanceAmount") String advanceAmount,
            @RequestParam("dueAmount") String dueAmount,
            @RequestParam("remainsAmount") String remainsAmount
    ) {

        Room room = roomRepository.findById(Long.parseLong(roomId)).orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + Long.parseLong(roomId)));
        Guest guest = guestRepository.findById(Long.parseLong(guestId)).orElseThrow(() -> new EntityNotFoundException("Guest not found with ID: " + guestId));

        if (endedAt == null || startedAt == null || advanceAmount == null || dueAmount == null) {
            throw new IllegalArgumentException("One or more required fields are null");
        }

        room.setAvailable(false);
        roomRepository.save(room);

        Session session = Session
                .builder()
                .guest(guest)
                .room(room)
                .details(details)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .saves(saves)
                .inAdvanceAmount(!advanceAmount.isEmpty() ? Long.parseLong(advanceAmount) : 0)
                .dueAmount(!dueAmount.isEmpty() ? Long.parseLong(dueAmount) : 0)
                .remainsAmount(!remainsAmount.isEmpty() ? Long.parseLong(remainsAmount) : 0)
                .isEnded(false)
                .build();

        return sessionRepository.save(session);

    }

    @GetMapping("/accounting")
    public String accounting(Model model) {
        List<Room> roomList = roomRepository.findAll();
        model.addAttribute("roomList", roomList);
        return "accounting";
    }

    @ResponseBody
    @PostMapping("/accountingResponse")
    public boolean accountingResponse(@RequestParam("rooms") List<String> rooms,
                                      @RequestParam("fromDate") String fromDate,
                                      @RequestParam("toDate") String toDate) {

        List<Session> sessions = sessionRepository.findAll();
        List<Session> result = new ArrayList<>();
        for (Session session : sessions) {
            if (rooms.contains(session.getRoom().getId().toString())) {
//                if (LocalDate.parse(session.getStartedAt()).compareTo(LocalDate.parse(fromDate)) >= 0 &&
//                        LocalDate.parse(session.getEndedAt()).compareTo(LocalDate.parse(toDate)) >= 0) {
//                    // Your logic here
//                }

            }
        }


        return true;
    }

}
