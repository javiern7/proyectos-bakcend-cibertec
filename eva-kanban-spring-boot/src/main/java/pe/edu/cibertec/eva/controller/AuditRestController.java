package pe.edu.cibertec.eva.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.cibertec.eva.dto.AuditDto;
import pe.edu.cibertec.eva.service.AuditLogService;

@RestController
@RequestMapping("/api/audit")
public class AuditRestController {
    private final AuditLogService auditLogService;

    public AuditRestController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Page<AuditDto> list(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String oldStatus,
            @RequestParam(required = false) String newStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "when"));
        // Nota: el sort real se hace en repo por createdAt; aquí el DTO ya trae "when".
        return auditLogService.search(days, actorId, taskId, action, oldStatus, newStatus,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
