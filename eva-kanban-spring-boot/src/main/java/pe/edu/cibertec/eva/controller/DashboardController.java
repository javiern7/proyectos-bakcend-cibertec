package pe.edu.cibertec.eva.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.eva.dto.ChartPoint;
import pe.edu.cibertec.eva.dto.StatusTotals;
import pe.edu.cibertec.eva.dto.UserOptionDto;
import pe.edu.cibertec.eva.entity.UserEntity;
import pe.edu.cibertec.eva.service.DashboardService;
import pe.edu.cibertec.eva.service.UserService;
import pe.edu.cibertec.eva.util.Constants;
import pe.edu.cibertec.eva.util.Functions;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard/api")
@RequiredArgsConstructor
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboard;
    private final UserService userService;

    private boolean isAdmin(UserEntity u) {
        if (u == null || u.getRole() == null) return false;
        String r = u.getRole().trim().toUpperCase();
        return Constants.ATRIBUT_ADMIN.equals(r) || Constants.ATRIBUT_ROL_ADMIN.equals(r);
    }

    /** Admin respeta userId (puede ser null => TODOS). No-admin: su id. */
    private Long resolveUserId(UserEntity current, Long userIdParam) {
        return isAdmin(current) ? userIdParam : current.getId();
    }

    @GetMapping("/snapshot")
    public StatusTotals snapshot(@SessionAttribute("user") UserEntity current,
                                 @RequestParam(required = false) Long userId) {
        log.info("Dashboard: snapshot userId={}", userId);
        Long uid = resolveUserId(current, userId);
        return dashboard.getSnapshot(uid);
    }

    @GetMapping("/weekly")
    public List<ChartPoint> weekly(@SessionAttribute("user") UserEntity current,
                                   @RequestParam String from,
                                   @RequestParam String to,
                                   @RequestParam(required = false) Long userId) {
        log.info("Dashboard: weekly userId={} from={} to={}", userId, from, to);
            Long uid = resolveUserId(current, userId);
            LocalDate fromDate = Functions.parseDate(from);
            LocalDate toDate = Functions.parseDate(to);
            return dashboard.getWeekly(fromDate, toDate, uid);
    }

    // opcional: poblar combo de usuarios para ADMIN
    @GetMapping("/users")
    public List<UserOptionDto> users(@SessionAttribute("user") UserEntity current) {
        log.info("Dashboard: users list for admin requester={}", current.getUsername());
        if (!isAdmin(current))
            return List.of();
        return userService.findAll().stream()
                .map(u -> new UserOptionDto(u.getId(), u.getUsername()))
                .toList();
    }
}
