package com.aluminate.aluminate_organization_backend.service;

import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class CsvParserService {
    public List<MemberRowDTO> parseCSV(MultipartFile file) throws IOException {
        List<MemberRowDTO> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                MemberRowDTO row = new MemberRowDTO();
                row.setNic(record.get("NIC").trim());
                row.setName(record.get("Name").trim());
                row.setEmail(record.get("Email").trim());
                row.setPhone(record.get("Phone").trim());
                row.setRegNo(record.get("RegNo").trim());
                row.setBatch(Integer.parseInt(record.get("Batch").trim()));
                rows.add(row);
            }
        }
        return rows;
    }
}
