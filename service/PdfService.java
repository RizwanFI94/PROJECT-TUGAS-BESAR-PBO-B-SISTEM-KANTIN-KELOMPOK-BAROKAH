package service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfService {

    public static String generateStruk(String pembeli, String isiStruk) {

        try {
            File dir = new File("struk");
            if (!dir.exists()) dir.mkdirs();

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String path = "struk/STRUK_" + time + ".pdf";

            PDRectangle receiptSize = new PDRectangle(220, 500);

            PDDocument doc = new PDDocument();
            PDPage page = new PDPage(receiptSize);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = receiptSize.getHeight() - 30;

            y = centerText(cs, "KANTIN BAROKAH", 14, y);
            y = centerText(cs, "STRUK PEMBELIAN", 11, y - 10);

            drawLine(cs, 15, y);
            y -= 20;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.newLineAtOffset(15, y);

            cs.showText("Pembeli : " + pembeli);
            cs.newLineAtOffset(0, -12);
            cs.showText("Tanggal : " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            cs.endText();

            y -= 25;
            drawLine(cs, 15, y);
            y -= 15;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(15, y);

            for (String line : isiStruk.split("\n")) {
                cs.showText(line);
                cs.newLineAtOffset(0, -12);
                y -= 12;
            }

            cs.endText();

            drawLine(cs, 15, y);
            y -= 25;

            y = centerText(cs, "Terima kasih telah berbelanja!", 10, y);
            y = centerText(cs, "Semoga harimu menyenangkan", 9, y - 12);

            cs.close();
            doc.save(path);
            doc.close();

            Desktop.getDesktop().open(new File(path));
            return path;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static float centerText(PDPageContentStream cs, String text, int size, float y)
            throws Exception {

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, size);
            float textWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000) * size;

            float startX = (220 - textWidth) / 2;
            cs.newLineAtOffset(startX, y);
            cs.showText(text);
            cs.endText();

            return y - 12;
    }

    private static void drawLine(PDPageContentStream cs, float x, float y)
            throws Exception {

            cs.setStrokingColor(Color.BLACK);
            cs.moveTo(x, y);
            cs.lineTo(205, y);
            cs.stroke();
    }
}