package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.util.ApplicationConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.REPORT_DATE_INFO;

public class CreateReportCommand implements ProcessableCommand {

    private final String caption;
    private final File report;

    @Override
    public Validable process(Chat chat) {
        return SendDocument.builder()
                .document(new InputFile(report))
                .caption(caption)
                .chatId(chat.getChatId())
                .build();
    }

    @SneakyThrows
    public CreateReportCommand(List<Subscription> reportData, List<Promotion> promotions) {
        final var now = LocalDateTime.now();
        caption = String.format(REPORT_DATE_INFO,
                now.getDayOfMonth(), now.getMonthValue(), now.getYear(),
                now.getHour(), now.getMinute());


        final var workbook = new XSSFWorkbook();

        final var cellStyle = workbook.createCellStyle();

        cellStyle.setBorderTop(BorderStyle.MEDIUM);
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);
        cellStyle.setBorderRight(BorderStyle.MEDIUM);
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);

        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);


        for (Promotion promotion : promotions) {
            final var sheet = workbook.createSheet(promotion.getName());
            sheet.setColumnWidth(0, mapCharacterWidth(5));
            sheet.setColumnWidth(1, mapCharacterWidth(30));
            sheet.setColumnWidth(2, mapCharacterWidth(9));
            sheet.setColumnWidth(3, mapCharacterWidth(9));
            sheet.setColumnWidth(4, mapCharacterWidth(9));
            sheet.setColumnWidth(5, mapCharacterWidth(25));
            sheet.setColumnWidth(6, mapCharacterWidth(27));
            sheet.setColumnWidth(7, mapCharacterWidth(12));
            sheet.setColumnWidth(8, mapCharacterWidth(12));

            final var headerRow = sheet.createRow(0);
            final var headerNames = ReportHeaderName.values();
            for (int i = 0; i < headerNames.length; i++) {
                final var headerCell = headerRow.createCell(i);
                headerCell.setCellValue(headerNames[i].getName());
                headerCell.setCellStyle(cellStyle);
            }

            Row dataRow;
            Cell dataCell;
            var i = 1;
            for (Subscription subscription : reportData) {
                if (subscription.getPromotion().getId().equals(promotion.getId())) {
                    final var user = subscription.getUser();

                    dataRow = sheet.createRow(i);
                    dataCell = dataRow.createCell(0);
                    dataCell.setCellValue(i);
                    dataCell.setCellStyle(cellStyle);
                    dataCell = dataRow.createCell(1);
                    dataCell.setCellValue("Кралін Микита Вадимович");
                    dataCell.setCellStyle(cellStyle);
                    dataCell = dataRow.createCell(2);
                    dataCell.setCellValue(user.getCityOfPharmacy());
                    dataCell.setCellStyle(cellStyle);
                    dataCell = dataRow.createCell(3);
                    dataCell.setCellValue(user.getPharmacyChain());
                    dataCell.setCellStyle(cellStyle);
                    dataCell = dataRow.createCell(4);
                    dataCell.setCellValue(user.getAddress());
                    dataCell.setCellStyle(cellStyle);
                    dataCell = dataRow.createCell(5);
                    dataCell.setCellValue(user.getFullName());
                    dataCell.setCellStyle(cellStyle);
                    dataCell = dataRow.createCell(6);
                    dataCell.setCellValue(user.getPhoneNumber());
                    dataCell.setCellStyle(cellStyle);
                    dataCell = dataRow.createCell(7);
                    dataCell.setCellValue(subscription.getCurrentQuantity());
                    dataCell.setCellStyle(cellStyle);
                    dataCell = dataRow.createCell(8);
                    dataCell.setCellValue(subscription.getCurrentBonus());
                    dataCell.setCellStyle(cellStyle);
                    i++;
                }
            }
        }

        final var currDir = new File(".");
        final var path = currDir.getAbsolutePath();
        final var fileLocation = path.substring(0, path.length() - 1) + "report.xlsx";
        try (FileOutputStream outputStream = new FileOutputStream(fileLocation)) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
        report = new File("./report.xlsx");

    }

    private int mapCharacterWidth(int characterWidth) {
        return characterWidth * 256;
    }
}
