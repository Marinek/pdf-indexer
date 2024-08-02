package de.indexer.index;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.indexer.processor.JobStarter;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PdfIndexController {

    @Autowired
    private LuceneConfig luceneConfig;

    @Autowired
    private Analyzer analyzer;

    @Autowired
    private IndexWriter indexWriter;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobStarter indexJobStarter;

    @Value("${pdf.file.path}")
    private String directoryPath;

    @GetMapping("/regenerateIndex")
    public String regenerateIndex() throws IOException {
        try {
            
            Files.walkFileTree(Path.of(directoryPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".indexed")) {
                        Files.delete(file);
                        System.out.println("Deleted: " + file);
                    }
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Error deleteting files!", e);
        }
        
        indexWriter.deleteAll();
        indexWriter.commit();
        
        indexJobStarter.startJob();
        
        return "OK!";
    }
    
    @GetMapping("/info")
    public String info() {
        // Get your Tika Config, eg
        TikaConfig config = TikaConfig.getDefaultConfig();
        // Get the registry
        MediaTypeRegistry registry = config.getMediaTypeRegistry();
        // List
        String typeStr = "";
        for (MediaType type : registry.getTypes()) {
            typeStr += type.toString();
        }

        return typeStr;
    }

    @GetMapping("/search")
    public ArrayNode searchIndex(@RequestParam("query") String queryStr) {
        try {
            DirectoryReader reader = DirectoryReader.open(luceneConfig.luceneDirectory());
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(queryStr);

            ArrayNode resultArray = objectMapper.createArrayNode();

            TopDocs results = searcher.search(query, 10);
            ScoreDoc[] hits = results.scoreDocs;

            for (ScoreDoc hit : hits) {
                Document doc = searcher.doc(hit.doc);

                ObjectNode result = objectMapper.createObjectNode();
                result.put("path", doc.get("path"));
                result.put("filename", doc.get("filename"));
                resultArray.add(result);
            }

            reader.close();
            return resultArray;
        } catch (IOException | ParseException e) {
            log.error("Queryerror!!", e);
        }
        return null;
    }
}