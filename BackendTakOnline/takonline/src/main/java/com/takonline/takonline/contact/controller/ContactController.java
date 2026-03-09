package com.takonline.takonline.contact.controller;

import com.takonline.takonline.contact.dto.ContactRequest;
import com.takonline.takonline.contact.service.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "http://localhost:5173")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> sendContact(@Valid @RequestBody ContactRequest request) {
        try {
            contactService.sendContactEmail(request);
            return ResponseEntity.ok(Map.of("message", "Contact email sent"));
        } catch (MailAuthenticationException ex) {
            log.error("SMTP auth failed while sending contact mail", ex);
            String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "No se pudo autenticar con SMTP. Revisa MAIL_USERNAME y MAIL_PASS_TAK (App Password). Detalle: " + detail));
        } catch (IllegalStateException ex) {
            log.error("SMTP config missing", ex);
            return ResponseEntity.internalServerError().body(Map.of("message", ex.getMessage()));
        } catch (MailException ex) {
            log.error("SMTP send failed", ex);
            String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Fallo enviando correo por SMTP. Revisa host/puerto y credenciales. Detalle: " + detail));
        }
    }
}
