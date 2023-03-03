package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;

public class CreateReportCommand implements ProcessableCommand {

    private final String caption;
    private final File report;

    @SneakyThrows
    public CreateReportCommand(List<User> reportData) {
        var now = LocalDateTime.now();
        caption = String.format("Звіт станом на\n\uD83D\uDDD3 %d.%d.%d\n\uD83D\uDD50 %d:%d",
                now.getDayOfMonth(), now.getMonthValue(), now.getYear(),
                now.getHour(), now.getMinute());


        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Звіт");
        Row header = sheet.createRow(0);
        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("№");
        headerCell = header.createCell(1);
        headerCell.setCellValue("ФIО МП");
        headerCell = header.createCell(2);
        headerCell.setCellValue("Місто аптеки");
        headerCell = header.createCell(3);
        headerCell.setCellValue("Назва Аптечної Мережі");
        headerCell = header.createCell(4);
        headerCell.setCellValue("Адреса аптеки");
        headerCell = header.createCell(5);
        headerCell.setCellValue("ФIО провізора\\зав.аптекою");
        headerCell = header.createCell(6);
        headerCell.setCellValue("Телефон провізора\\зав.аптекою ( хто буде отримувати розрахунок)");
        headerCell = header.createCell(7);
        headerCell.setCellValue("Кількість проданих уп.");
        headerCell = header.createCell(8);
        headerCell.setCellValue("Бонус ( грн) за продаж");

        Row dataRow = null;
        Cell dataCell = null;
        int i = 1;
        for (User user : reportData) {
            dataRow = sheet.createRow(i);
            dataCell = dataRow.createCell(0);
            dataCell.setCellValue(i);
            dataCell = dataRow.createCell(1);
            dataCell.setCellValue("Кралін Микита Вадимович");
            dataCell = dataRow.createCell(2);
            dataCell.setCellValue(user.getCityOfPharmacy());
            dataCell = dataRow.createCell(3);
            dataCell.setCellValue(user.getPharmacyChain());
            dataCell = dataRow.createCell(4);
            dataCell.setCellValue(user.getAddress());
            dataCell = dataRow.createCell(5);
            dataCell.setCellValue(user.getFullName());
            dataCell = dataRow.createCell(6);
            dataCell.setCellValue(user.getPhoneNumber());
            dataCell = dataRow.createCell(7);
            dataCell.setCellValue(user.getSoldPackages());
            dataCell = dataRow.createCell(8);
            dataCell.setCellValue(user.getScore());
            i++;
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
