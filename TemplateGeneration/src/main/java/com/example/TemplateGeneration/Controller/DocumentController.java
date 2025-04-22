package com.example.TemplateGeneration.Controller;

import com.example.TemplateGeneration.Dto.ResponsDTO;
import com.example.TemplateGeneration.Service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {


    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    private String uploadedTemplateContent = "";

    @PostMapping(value="/upload-template")
    public ResponseEntity<ResponsDTO> uploadTemplate(@RequestParam("file") MultipartFile file) throws IOException {
        uploadedTemplateContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok( new ResponsDTO(HttpStatus.CREATED,"Template uploaded successfully."));
    }

    @PostMapping(value="/upload-excel")
    public ResponseEntity<ResponsDTO> uploadExcel(@RequestParam("file") MultipartFile file) {
        if (uploadedTemplateContent.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponsDTO(HttpStatus.BAD_REQUEST,"Please upload a template first."));
        }

        documentService.processExcelAndSendEmails(file, uploadedTemplateContent);
        return ResponseEntity.ok(new ResponsDTO(HttpStatus.CREATED,"Document under process."));
    }
}

