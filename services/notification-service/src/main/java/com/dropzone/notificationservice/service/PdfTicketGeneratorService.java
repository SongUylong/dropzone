package com.dropzone.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Slf4j
@Service
public class PdfTicketGeneratorService {

    public byte[] generateTicketPdf(String eventName,
                                    String categoryName,
                                    String seatNumber,
                                    String eventDate,
                                    String ticketId,
                                    byte[] qrCodeImageBytes) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A5);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                float currentY = pageHeight - 50;

                // 1. DROPZONE Header
                content.beginText();
                content.setFont(fontBold, 26);
                float headerWidth = fontBold.getStringWidth("DROPZONE") / 1000 * 26;
                content.newLineAtOffset((pageWidth - headerWidth) / 2, currentY);
                content.showText("DROPZONE");
                content.endText();

                currentY -= 50;

                // 2. Event Name (e.g. Coldplay Concert)
                content.beginText();
                content.setFont(fontBold, 20);
                float eventWidth = fontBold.getStringWidth(eventName) / 1000 * 20;
                content.newLineAtOffset((pageWidth - eventWidth) / 2, currentY);
                content.showText(eventName);
                content.endText();

                currentY -= 35;

                // 3. Ticket Category (e.g. VIP)
                content.beginText();
                content.setFont(fontRegular, 16);
                float catWidth = fontRegular.getStringWidth(categoryName) / 1000 * 16;
                content.newLineAtOffset((pageWidth - catWidth) / 2, currentY);
                content.showText(categoryName);
                content.endText();

                currentY -= 30;

                // 4. Seat Number (e.g. Seat A102)
                content.beginText();
                content.setFont(fontRegular, 14);
                float seatWidth = fontRegular.getStringWidth(seatNumber) / 1000 * 14;
                content.newLineAtOffset((pageWidth - seatWidth) / 2, currentY);
                content.showText(seatNumber);
                content.endText();

                currentY -= 25;

                // 5. Date (e.g. October 10)
                content.beginText();
                content.setFont(fontRegular, 14);
                float dateWidth = fontRegular.getStringWidth(eventDate) / 1000 * 14;
                content.newLineAtOffset((pageWidth - dateWidth) / 2, currentY);
                content.showText(eventDate);
                content.endText();

                currentY -= 170; // Position for QR code image

                // 6. QR Code Image
                if (qrCodeImageBytes != null && qrCodeImageBytes.length > 0) {
                    PDImageXObject qrImage = PDImageXObject.createFromByteArray(document, qrCodeImageBytes, "QR");
                    float qrSize = 150;
                    float qrX = (pageWidth - qrSize) / 2;
                    content.drawImage(qrImage, qrX, currentY, qrSize, qrSize);
                }

                currentY -= 35;

                // 7. Ticket ID (e.g. Ticket ID: DZ-928231)
                String ticketIdLabel = "Ticket ID: " + ticketId;
                content.beginText();
                content.setFont(fontBold, 14);
                float idWidth = fontBold.getStringWidth(ticketIdLabel) / 1000 * 14;
                content.newLineAtOffset((pageWidth - idWidth) / 2, currentY);
                content.showText(ticketIdLabel);
                content.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            log.info("[PDF Generator] Generated {} bytes PDF ticket for Ticket ID: {}", outputStream.size(), ticketId);
            return outputStream.toByteArray();
        }
    }
}
