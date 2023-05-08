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

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;

public class CreateReportCommand implements ProcessableCommand {

    private final String caption;
    private final File report;

    @SneakyThrows
    public CreateReportCommand(List<Subscription> reportData, List<Promotion> promotions) {
        var now = LocalDateTime.now();
        caption = String.format("Звіт станом на\n\uD83D\uDDD3 %d.%d.%d\n\uD83D\uDD50 %d:%d",
                now.getDayOfMonth(), now.getMonthValue(), now.getYear(),
                now.getHour(), now.getMinute());


        Workbook workbook = new XSSFWorkbook();

        CellStyle style = workbook.createCellStyle();

        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);


        for (Promotion promotion : promotions) {
            Sheet sheet = workbook.createSheet(promotion.getName());
            sheet.setColumnWidth(0, 9*256);
            sheet.setColumnWidth(1, 30*256);
            sheet.setColumnWidth(2, 9*256);
            sheet.setColumnWidth(3, 9*256);
            sheet.setColumnWidth(4, 9*256);
            sheet.setColumnWidth(5, 25*256);
            sheet.setColumnWidth(6, 27*256);
            sheet.setColumnWidth(7, 12*256);
            sheet.setColumnWidth(8, 12*256);
            Row header = sheet.createRow(0);
            Cell headerCell = header.createCell(0);
            headerCell.setCellValue("№");
            headerCell.setCellStyle(style);
            headerCell = header.createCell(1);
            headerCell.setCellValue("ФIО МП");
            headerCell.setCellStyle(style);
            headerCell = header.createCell(2);
            headerCell.setCellValue("Місто аптеки");
            headerCell.setCellStyle(style);
            headerCell = header.createCell(3);
            headerCell.setCellValue("Назва Аптечної Мережі");
            headerCell.setCellStyle(style);
            headerCell = header.createCell(4);
            headerCell.setCellValue("Адреса аптеки");
            headerCell.setCellStyle(style);
            headerCell = header.createCell(5);
            headerCell.setCellValue("ФIО провізора\\зав.аптекою");
            headerCell.setCellStyle(style);
            headerCell = header.createCell(6);
            headerCell.setCellValue("Телефон провізора\\зав.аптекою (хто буде отримувати розрахунок)");
            headerCell.setCellStyle(style);
            headerCell = header.createCell(7);
            headerCell.setCellValue("Кількість проданих уп.");
            headerCell.setCellStyle(style);
            headerCell = header.createCell(8);
            headerCell.setCellValue("Бонус (грн) за продаж");
            headerCell.setCellStyle(style);

            Row dataRow = null;
            Cell dataCell = null;
            int i = 1;
            for (Subscription subscription : reportData) {
                if (subscription.getPromotion().getId().equals(promotion.getId())) {
                    User user = subscription.getUser();

                    dataRow = sheet.createRow(i);
                    dataCell = dataRow.createCell(0);
                    dataCell.setCellValue(i);
                    dataCell.setCellStyle(style);
                    dataCell = dataRow.createCell(1);
                    dataCell.setCellValue("Кралін Микита Вадимович");
                    dataCell.setCellStyle(style);
                    dataCell = dataRow.createCell(2);
                    dataCell.setCellValue(user.getCityOfPharmacy());
                    dataCell.setCellStyle(style);
                    dataCell = dataRow.createCell(3);
                    dataCell.setCellValue(user.getPharmacyChain());
                    dataCell.setCellStyle(style);
                    dataCell = dataRow.createCell(4);
                    dataCell.setCellValue(user.getAddress());
                    dataCell.setCellStyle(style);
                    dataCell = dataRow.createCell(5);
                    dataCell.setCellValue(user.getFullName());
                    dataCell.setCellStyle(style);
                    dataCell = dataRow.createCell(6);
                    dataCell.setCellValue(user.getPhoneNumber());
                    dataCell.setCellStyle(style);
                    dataCell = dataRow.createCell(7);
                    dataCell.setCellValue(subscription.getCurrentQuantity());
                    dataCell.setCellStyle(style);
                    dataCell = dataRow.createCell(8);
                    dataCell.setCellValue(subscription.getCurrentBonus());
                    dataCell.setCellStyle(style);
                    i++;
                }
            }
        }

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "report.xlsx";
        try (FileOutputStream outputStream = new FileOutputStream(fileLocation)) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
        report = new File("./report.xlsx");

    }

    @Override
    public Validable process(Chat chat) {
        return SendDocument.builder()
                .document(new InputFile(report))
                .caption(caption)
                .chatId(chat.getChatId())
                .build();
    }
}
