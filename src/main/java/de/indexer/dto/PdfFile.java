package de.indexer.dto;

import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.qos.logback.core.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PdfFile {

    private Path path;

    @JsonIgnore
    private String content;

    private String hash;

    public PdfFile (Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path + "- " + hash + ": " + content != null ? content.substring(0, Math.min(50, content.length())) : "<no content>" ;
    }

}
