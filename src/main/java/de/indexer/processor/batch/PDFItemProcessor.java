package de.indexer.processor.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.batch.item.ItemProcessor;

import de.indexer.dto.PdfFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PDFItemProcessor implements ItemProcessor<Path, PdfFile> {

    @Override
    public PdfFile process(Path item) {
        PdfFile pdfFile = new PdfFile(item);

        try {
            String mimeType = detectMimeType(item.toFile());
            if (!mimeType.equals("application/pdf")) {
                log.debug("Skipping file, because it is not a pdf. ({})", item);
            }

            pdfFile.setHash(calculateFileHash(item));

            if (hasIndexedFile(item)) {
                log.debug("Skipping file, it has already been indexed. ({})", item);
                return null;
            }

            pdfFile.setContent(getText(item));

            if (pdfFile.getContent().isEmpty()) {
                return null;
            }

            log.info("Result: {}", pdfFile);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Error creating pdffile represantion {}", item, e);

            return null;
        }

        return pdfFile;
    }

    private static String detectMimeType(File file) throws IOException {
        Tika tika = new Tika();
        return tika.detect(file);
    }

    private String calculateFileHash(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var fis = Files.newInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String getText(Path pdfFile) throws IOException {
        try (InputStream stream = new FileInputStream(pdfFile.toFile())) {
            Tika tika = new Tika();
            String extract = null;

            extract = tika.parseToString(stream);
            log.info("Extracting Text from {} : ({}) '{}'", pdfFile.getFileName(), tika.detect(pdfFile), extract);

            return extract;
        } catch (TikaException e) {
            log.error("Error extracting Text from {}", pdfFile, e);
        }

        return "";
    }

    public static boolean hasIndexedFile(Path originalFilePath) {
        Path directory = originalFilePath.getParent();
        if (directory == null) {
            return false;
        }

        String indexedFileName = originalFilePath.getFileName().toString() + ".indexed";
        Path indexedFilePath = directory.resolve(indexedFileName);

        return Files.exists(indexedFilePath);
    }
}