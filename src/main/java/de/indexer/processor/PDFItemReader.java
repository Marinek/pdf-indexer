package de.indexer.processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.springframework.batch.item.ItemReader;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PDFItemReader implements ItemReader<Path> {

    private final Queue<Path> fileQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Path> directoryQueue = new ConcurrentLinkedQueue<>();
    private final MimeType selectedMimeType;

    public PDFItemReader(Path startPath) {
        this(startPath, null);
    }
    
    public PDFItemReader(Path startPath, MimeType selectMimeType) {
        directoryQueue.add(startPath);
        selectedMimeType = selectMimeType;
    }

    @Override
    public Path read() throws Exception {
        log.info("Start reading directorys: {}", directoryQueue.size());
        if (fileQueue.isEmpty() && !directoryQueue.isEmpty()) {
            do {
                Path currentDir = directoryQueue.poll();
                if (currentDir != null) {
                    Files.list(currentDir).forEach(path -> {
                        if (Files.isDirectory(path)) {
                            directoryQueue.add(path);
                        } else {
                            try {
                                if(selectedMimeType == null || detectMimeType(path).equals(selectedMimeType.getName())) {
                                    fileQueue.add(path);
                                }
                            } catch (IOException e) {
                                log.error("Skipping file, because no mimetype could be detected: {}", path, e);
                            }
                        }
                    });
                }

            } while (directoryQueue.size() > 0 && fileQueue.size()==0);
        }

        return fileQueue.poll();
    }

    private static String detectMimeType(Path file) throws IOException {
        Tika tika = new Tika();
        return tika.detect(file);
    }
}
