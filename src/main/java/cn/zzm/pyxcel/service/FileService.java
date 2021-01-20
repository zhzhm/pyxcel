package cn.zzm.pyxcel.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class FileService {
    private static Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    @ConfigProperty(name = "pyxcel.repository")
    String repositoryPath;

    public void uploadFile(String fileName, InputStream fileStream) throws IOException {
        var path = Paths.get(repositoryPath + File.separator + fileName);

        if(!Files.exists(path.getParent())){
            Files.createDirectories(path.getParent());
        }
        LOGGER.debug(path.toString());
        var fileOutputStream = new FileOutputStream(path.toFile());
        IOUtils.copy(fileStream, fileOutputStream);
        fileOutputStream.close();
        fileStream.close();
    }

    public List<Path> list(String relativeDir) throws IOException {
        return Files.list(Paths.get(this.repositoryPath + File.separator + relativeDir)).collect(Collectors.toList());
    }

    public void delete(String relativePath) throws IOException {
        if(StringUtils.isBlank(relativePath)) throw new FileNotFoundException("Must specify file name!");
        Files.delete(Paths.get(this.repositoryPath + File.separator + relativePath));
    }
}