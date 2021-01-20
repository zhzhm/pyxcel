package cn.zzm.pyxcel.executor;

import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PythonExecutorCallable implements Callable<JsonObject> {
    private String pythonCode;
    private Path workDir;
    private OutputStream outputStream;
    private final Logger LOGGER = LoggerFactory.getLogger(PythonExecutorCallable.class);

    public PythonExecutorCallable(Path workDir, String pythonCode, OutputStream outputStream) {
        this.workDir = workDir;
        this.pythonCode = pythonCode;
        this.outputStream = outputStream;
        LOGGER.debug("=====Python Code:=====\n{}\n=====================", pythonCode);
    }

    @Override
    public JsonObject call() throws Exception {
        String executionId = UUID.randomUUID().toString();
        LOGGER.info("ExecutionID is :{}", executionId);
        ProcessBuilder pb = new ProcessBuilder();
        List<String> commands = new ArrayList<>();
        commands.add("python3");
        commands.add("source.py");
        LOGGER.debug("Python3 Command line: \n{}", commands.stream().collect(Collectors.joining(" ")));
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        writeToSource();
        var process = pb.command(commands).directory(this.workDir.toFile()).start();
        final var processInput = process.getInputStream();
        if(outputStream != null){
            new Thread(()->{
                try {
                    IOUtils.copy(processInput, outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        var returnCode = process.waitFor();
        LOGGER.info(returnCode + "");
        return null;
    }

    private void writeToSource() throws IOException {
        if (!Files.exists(this.workDir)){
            Files.createDirectories(this.workDir);
        }
        var pyFilePath = this.workDir.resolve("source.py");
        if (!Files.exists(pyFilePath)) {
            Files.createFile(pyFilePath);
        }
        Files.writeString(pyFilePath, this.pythonCode);
    }
}
