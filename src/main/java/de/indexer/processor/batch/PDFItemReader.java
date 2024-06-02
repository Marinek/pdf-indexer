package de.indexer.processor.batch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.batch.item.ItemReader;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PDFItemReader implements ItemReader<Path> {

    private final Queue<Path> fileQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Path> directoryQueue = new ConcurrentLinkedQueue<>();

    public PDFItemReader(Path startPath) {
        directoryQueue.add(startPath);
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
                            fileQueue.add(path);
                        }
                    });
                }

            } while (directoryQueue.size() > 0 && fileQueue.size()==0);
        }

        return fileQueue.poll();
    }
}
