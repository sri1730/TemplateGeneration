package com.example.TemplateGeneration.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

@Service
public class DocumentService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void processExcelAndSendEmails(MultipartFile excelFile, String template) {
        try (InputStream inputStream = excelFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                Map<String, Object> model = new HashMap<>();
                System.out.println("getPhysicalNumberOfCells"+ headerRow.getPhysicalNumberOfCells());
                for (int j = 0; j < headerRow.getPhysicalNumberOfCells(); j++) {
                    String header = headerRow.getCell(j).getStringCellValue();
                    String value = row.getCell(j).toString();
                    System.out.println("header"+ header + "value" +value);
                    model.put(header, value);
                }

                String email = model.get("email").toString();
                if(!email.isEmpty()) {
                    String processedHtml = processTemplate(template, model);
                    byte[] pdfBytes = generatePdfFromHtml(processedHtml);

                    sendEmailWithAttachment(email, "Your Document", "Please find attached.", pdfBytes);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String processTemplate(String templateContent, Map<String, Object> model) {
        VelocityContext context = new VelocityContext(model);
        StringWriter writer = new StringWriter();

        VelocityEngine engine = new VelocityEngine();
        engine.init();
        engine.evaluate(context, writer, "logTag", templateContent);

        return writer.toString();
    }

    private void sendEmailWithAttachment(String to, String subject, String body, byte[] pdfBytes) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        System.out.println("sendEmailWithAttachment started");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.setFrom("noreply@demomailtrap.co");
        helper.addAttachment("document.pdf", new ByteArrayResource(pdfBytes));
        System.out.println("sendEmailWithAttachment END");
        javaMailSender.send(message);
    }

    public byte[] generatePdfFromHtml(String htmlContent) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Convert to well-formed XHTML using jsoup

            Document doc = Jsoup.parse(htmlContent);
            doc.outputSettings()
                    .syntax(Document.OutputSettings.Syntax.xml)
                    .escapeMode(Entities.EscapeMode.xhtml)
                    .charset("UTF-8");

            String xhtml = doc.html();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(xhtml, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        }
    }
}
