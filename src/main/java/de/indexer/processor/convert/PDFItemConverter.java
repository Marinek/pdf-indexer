package de.indexer.processor.convert;

import java.nio.file.Path;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class PDFItemConverter implements ItemProcessor<Path, Path> {

    @Override
    @Nullable
    public Path process(@NonNull Path item) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }

}
