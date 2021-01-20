package cn.zzm.pyxcel.controller;

import cn.zzm.pyxcel.bean.MultipartBody;
import cn.zzm.pyxcel.service.FileService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Path("/rest/file")
public class FileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @ConfigProperty(name = "pyxcel.repository")
    String repositoryPath;

    @Inject
    FileService fileService;

    @POST
    @Path("/upload")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String upload(@HeaderParam("sid") String sid, @MultipartForm MultipartBody fileBody) throws IOException {
        LOGGER.debug("Upload file: {}, size: {}, sid: {}", fileBody.fileName, fileBody.file.available(), sid);
        fileService.uploadFile(sid + File.separator + fileBody.fileName, fileBody.file);
        return "ok";
    }

    @GET
    @Path("/list/{dir:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray list(@HeaderParam("sid") String sid, @PathParam("dir") String dir) throws IOException {
        LOGGER.debug(dir);

        var files = new JsonArray();
        fileService.list(StringUtils.isEmpty(dir)?sid:sid + File.separator + dir).forEach(path -> {
            var pathJson = new JsonObject();
            pathJson.put("name", path.getFileName().toString());
            pathJson.put("isFile", !Files.isDirectory(path));
            pathJson.put("absPath", path.toString().substring(repositoryPath.length()+sid.length()+2));
            files.add(pathJson);
        });
        return files;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray listAll(@HeaderParam("sid") String sid) {
        try{
            return list(sid, null);
        }catch (IOException e){
            throw new WebApplicationException(e.getMessage(), 404);
        }
    }


    @DELETE
    @Path("/delete/{fileName}")
    public String remove(@HeaderParam("sid") String sid, @PathParam("fileName") String fileName){
        try {
            fileService.delete(sid + File.separator + fileName);
            return "ok";
        } catch (IOException e) {
            throw new WebApplicationException(e.getMessage(), 500);
        }

    }
}
