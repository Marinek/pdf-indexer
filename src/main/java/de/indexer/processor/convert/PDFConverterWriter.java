package de.indexer.processor.convert;

import java.nio.file.Path;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

public class PDFConverterWriter implements ItemWriter<Path>  {

    @Override
    public void write(@NonNull Chunk<? extends Path> chunk) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'write'");
    }

}
