package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.Contacts;
import com.example.bulksmsAPI.Models.DTO.ContactsDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.ContactsRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook; // For .xls files
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ContactsService {
    @Autowired
    private ContactsRepository contactsRepository; // Corrected variable name for consistency

    @Autowired
    private UserRepository userRepository; // Corrected variable name for consistency


    public Contacts addContacts(Long usuarioId, ContactsDTO contactsDTO) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        // Convert ContactsDTO to Contacts entity
        Contacts contact = new Contacts();
        contact.setId(usuarioId);
        contact.setName(contactsDTO.getName());
        contact.setPhoneNumber(contactsDTO.getPhoneNumber());
        contact.setGroup(contactsDTO.getGroup());

        return contactsRepository.save(contact);
    }

    public List<Contacts> listContacts(Long usuarioId) {
        return contactsRepository.findByUsuarioId(usuarioId);
    }

    public void updateContacts(Long contactsId, ContactsDTO contactsDTO) {
        Contacts contact = contactsRepository.findById(contactsId)
                .orElseThrow(() -> new RuntimeException("Contacts Not Found"));

        contact.setName(contactsDTO.getName());
        contact.setPhoneNumber(contactsDTO.getPhoneNumber());
        contact.setGroup(contactsDTO.getGroup());

        contactsRepository.save(contact);
    }

    public void deleteContacts(Long contactsId) {
        contactsRepository.deleteById(contactsId);
    }

   public void importContactsFromExcel(Long usuarioId, MultipartFile file) throws IOException {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        List<Contacts> contactsList = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook;

            if (file.getOriginalFilename().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (file.getOriginalFilename().endsWith(".xls")) { // Handle .xls files
                workbook = new HSSFWorkbook(inputStream);
            } else if (file.getOriginalFilename().endsWith(".csv")) { // Handle CSV
                workbook = createWorkbookFromCSV(inputStream);
            } else {
                throw new IllegalArgumentException("Invalid file type. Only .xlsx, .xls, and .csv files are supported.");
            }



            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row if present (optional)
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip the first row (header)
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                Contacts contact = new Contacts();
                contact.setUsuario(usuario); // Associate with the user

                // Example: Assuming the Excel has columns: Name, Phone, Email
                String name = "";
                String phoneNumber= "";


                if (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    name = getCellValue(cell);
                    contact.setName(name);
                }

                if (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    phoneNumber = getCellValue(cell);

                }

                if (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();


                }

                contactsList.add(contact);
            }
        }
        contactsRepository.saveAll(contactsList);
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()); // Handle numeric values
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return String.valueOf(cell.getCellFormula());
            default:
                return ""; // Or handle other cell types as needed
        }
    }

    private Workbook createWorkbookFromCSV(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(); // Create a new workbook
        Sheet sheet = workbook.createSheet("Sheet1"); // Create a sheet

        try (java.util.Scanner scanner = new java.util.Scanner(inputStream)) {
            int rowNum = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] cells = line.split(","); // Split by comma (adjust delimiter if needed)

                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < cells.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(cells[i]);
                }
            }
        }
        return workbook;
    }


}