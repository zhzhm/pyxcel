package cn.zzm.pyxcel.controller;

import cn.zzm.pyxcel.SocketOutputStream;
import cn.zzm.pyxcel.executor.PythonExecutorCallable;
import io.vertx.core.json.Json;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

@Path("/rest/python")
public class ExecutionController {
    @ConfigProperty(name = "pyxcel.repository")
    String repositoryPath;

    @Path("/execute")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String executePython(String pythonCodeJson, @HeaderParam("sid") String sid, @HeaderParam("wsId") String wsSessionId){
        var sourceObject = Json.decodeValue(pythonCodeJson, HashMap.class);
        var socketSession = ConsoleSocketController.getSession(wsSessionId);
        var wsOutputStream = socketSession != null ? new SocketOutputStream(socketSession) : System.out;
        var executor = new PythonExecutorCallable(Paths.get(this.repositoryPath + File.separator + sid), sourceObject.get("pythonCode").toString(), wsOutputStream);
        try {
            executor.call();
        } catch (Exception e) {
            throw new WebApplicationException(e.getMessage(), 500);
        }
        return "OK";
    }
}
