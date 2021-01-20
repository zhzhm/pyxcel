package cn.zzm.pyxcel.controller;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/session")
public class HomepageController {

    private static byte[] indexHtmlBuf;

    static {
        var in = HomepageController.class.getResourceAsStream("/META-INF/resources/index.html");
        try {
            indexHtmlBuf = IOUtils.readFully(in, in.available());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Path("/{sid: .+}")
    public Response homePage(@Context HttpServletResponse response) throws IOException {
        var out = response.getOutputStream();

        IOUtils.write(indexHtmlBuf, out);
        out.flush();
        out.close();
        return null;
    }
}
