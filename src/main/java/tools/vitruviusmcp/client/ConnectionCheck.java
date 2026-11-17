package tools.vitruviusmcp.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import tools.vitruv.framework.remote.client.VitruvClient;
import tools.vitruv.framework.remote.client.VitruvClientFactory;

/**
 * Task 1 compile check: proves the Vitruv-Server client library is on the classpath.
 * Creating the client does not contact the server, so Vitruv-Server does not need to run yet.
 * Creates a temporary folder, where the client stores the files of views it opens, 
 * builds a VitruvClient pointing at localhost:8080, and prints some details
 */
public class ConnectionCheck {

  private static final String HOST = "localhost";
  private static final int PORT = 8080;

  public static void main(String[] args) throws IOException {
    // The client stores files of opened views here
    Path tempDir = Files.createTempDirectory("vitruv-client");

    VitruvClient client = VitruvClientFactory.create(HOST, PORT, tempDir);

    System.out.println("Client created");
    System.out.println("  server:   http://" + HOST + ":" + PORT);
    System.out.println("  temp dir: " + tempDir);
    System.out.println("  class:    " + client.getClass().getName());
  }
}