package pe.edu.cibertec.eva.dto;


public record AuditDto(String when,         // yyyy-MM-dd HH:mm
                       String username,     // actorUsername
                       String action,
                       Long taskId,
                       String oldStatus,
                       String newStatus,
                       String details
) {
}

