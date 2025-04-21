package com.example.TemplateGeneration.Controller;

import com.example.TemplateGeneration.Service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/upload-template")
    public ResponseEntity<String> uploadTemplate(@RequestParam("file") MultipartFile file) throws IOException {
        uploadedTemplateContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok("Template uploaded successfully.");
    }

    @PostMapping("/upload-excel")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        if (uploadedTemplateContent.isEmpty()) {
            return ResponseEntity.badRequest().body("Upload template first.");
        }

        documentService.processExcelAndSendEmails(file, uploadedTemplateContent);
        return ResponseEntity.ok("Document under process.");
    }
}

