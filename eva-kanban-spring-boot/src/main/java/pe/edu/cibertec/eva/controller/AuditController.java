package pe.edu.cibertec.eva.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.edu.cibertec.eva.dto.AuditDto;
import pe.edu.cibertec.eva.service.AuditLogService;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/audit")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportAudit(HttpServletResponse response,
                            @RequestParam(required = false) Long actorId,
                            @RequestParam(required = false) Long taskId,
                            @RequestParam(required = false) String action,
                            @RequestParam(required = false) String oldStatus,
                            @RequestParam(required = false) String newStatus,
                            // si ya usas from/to en tu paginado, usa los mismos; si no, maneja "days"
                            @RequestParam(required = false, defaultValue = "7") Integer days) throws Exception {

        // 1) Reutiliza TU servicio con los mismos filtros (pero sin paginar)
        // Si tu servicio expone search(actorId, taskId, action, oldStatus, newStatus, from, to), úsalo.
        // Si no, crea una variante que retorne toda la lista con "days".
        List<AuditDto> rows = auditLogService.search(actorId, taskId, action, oldStatus, newStatus, days);
        // ^ ajusta el nombre / firma a tu servicio real

        try (Workbook wb = new XSSFWorkbook()) {
            CreationHelper ch = wb.getCreationHelper();
            Sheet sh = wb.createSheet("Auditoría");

            // --- Estilos breves ---
            CellStyle th = wb.createCellStyle();
            Font fBold = wb.createFont();
            fBold.setBold(true);
            th.setFont(fBold);
            th.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            th.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            th.setBorderBottom(BorderStyle.THIN);
            th.setBorderTop(BorderStyle.THIN);
            th.setBorderLeft(BorderStyle.THIN);
            th.setBorderRight(BorderStyle.THIN);

            CellStyle td = wb.createCellStyle();
            td.setBorderBottom(BorderStyle.THIN);
            td.setBorderTop(BorderStyle.THIN);
            td.setBorderLeft(BorderStyle.THIN);
            td.setBorderRight(BorderStyle.THIN);

            // --- Header ---
            Row h = sh.createRow(0);
            String[] headers = {"Fecha", "Usuario", "Acción", "Tarea", "Anterior", "Nuevo", "Detalle"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(th);
            }

            // --- Cuerpo (usa tu record AuditDto con Strings) ---
            int r = 1;
            if (rows != null) for (AuditDto row : rows) {
                Row rr = sh.createRow(r++);
                rr.createCell(0).setCellValue(row.when() != null ? row.when() : "");
                rr.getCell(0).setCellStyle(td);
                rr.createCell(1).setCellValue(row.username() != null ? row.username() : "");
                rr.getCell(1).setCellStyle(td);
                rr.createCell(2).setCellValue(row.action() != null ? row.action() : "");
                rr.getCell(2).setCellStyle(td);
                rr.createCell(3).setCellValue(row.taskId() != null ? row.taskId() : 0);
                rr.getCell(3).setCellStyle(td);
                rr.createCell(4).setCellValue(row.oldStatus() != null ? row.oldStatus() : "");
                rr.getCell(4).setCellStyle(td);
                rr.createCell(5).setCellValue(row.newStatus() != null ? row.newStatus() : "");
                rr.getCell(5).setCellStyle(td);
                rr.createCell(6).setCellValue(row.details() != null ? row.details() : "");
                rr.getCell(6).setCellStyle(td);
            }

            for (int i = 0; i < 7; i++) sh.autoSizeColumn(i);

            // 2) Streaming binario correcto (evita 0 KB)
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"auditoria.xlsx\"");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            wb.write(response.getOutputStream());
            response.flushBuffer();
        }
    }
}
