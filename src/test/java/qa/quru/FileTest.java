package qa.quru;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import qa.quru.model.PhoneModel;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

public class FileTest {

    @Test
    public void jsonTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/phones.json");
        List<PhoneModel> phoneList = mapper.readValue(file, new TypeReference<>() {});
        assertThat(phoneList).hasSize(2);
        assertThat(phoneList.get(0).getManufacturer()).isEqualTo("OnePlus");
        assertThat(phoneList.get(0).getModel()).isEqualTo("OnePlus 10 Pro");

    }

    @Test
    public void zipTest() throws Exception {
        ClassLoader classLoader = FileTest.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream("sample.zip")) {
            assert stream != null;
            try (ZipInputStream zis = new ZipInputStream(stream)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String name = entry.getName();
                    switch (name) {
                        case "qa_guru.csv" -> {
                            CSVReader csvReader = new CSVReader(new InputStreamReader(zis));
                            List<String[]> content = csvReader.readAll();
                            assertEquals(3, content.size());
                            final String[] firstRow = content.get(0);
                            final String[] secondRow = content.get(1);
                            final String[] thirdRow = content.get(2);
                            Assertions.assertArrayEquals(new String[]{"Teacher", "lesson"}, firstRow);
                            Assertions.assertArrayEquals(new String[]{"Tuchs", "Files"}, secondRow);
                            Assertions.assertArrayEquals(new String[]{"Vasenkov", "REST Assured"}, thirdRow);
                        }
                        case "sample.pdf" -> {
                            PDF pdf = new PDF(zis);
                            assertEquals(31, pdf.numberOfPages);
                            assertTrue(pdf.text.contains("Java Code"));
                        }
                        case "sample-xlsx-file.xlsx" -> {
                            XLS xls = new XLS(zis);
                            Assertions.assertEquals("First Name",
                                    xls.excel.getSheetAt(0)
                                            .getRow(0)
                                            .getCell(1)
                                            .getStringCellValue());
                        }
                    }
                }
            }
        }
    }
}