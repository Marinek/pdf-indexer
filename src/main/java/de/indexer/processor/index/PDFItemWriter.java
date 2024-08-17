package de.indexer.processor.index;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.indexer.dto.PdfFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PDFItemWriter implements ItemWriter<PdfFile> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private IndexWriter indexWriter;


    public PDFItemWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

    @Override
    public void write(@NonNull Chunk<? extends PdfFile> chunk) throws Exception {
        for (PdfFile file : chunk) {
            addDocumentToIndex(file);
            createMetaFile(file);
        }
    }

    private void createMetaFile(PdfFile file) {
        log.debug("Creating metafile for: {}", file);
        try {
            Path metaFile = Paths.get(file.getPath().toString() + ".indexed");
            objectMapper.writeValue(metaFile.toFile(), file);

            log.info("Metafile created for: {}", file);

        } catch (IOException e) {
            log.error("Error creating metafile for: {}", file, e);
        }
    }

    private void addDocumentToIndex(PdfFile file) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("path", file.getPath().toString(), Field.Store.YES));
        doc.add(new StringField("filename", file.getPath().getFileName().toString(), Field.Store.YES));
        doc.add(new TextField("content", file.getContent(), Field.Store.YES));
        indexWriter.addDocument(doc);
        indexWriter.commit();
    }

   
}