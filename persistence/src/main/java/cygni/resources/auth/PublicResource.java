package cygni.resources.auth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/api/public")
public class PublicResource {

    @GET
    public String serve() {
        return "public";
    }
}
