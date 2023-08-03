import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class HTTPServer {
    private static final Logger logger = Logger.getLogger("HttpServerLogger");
    private static final int NUM_THREADS = 50;
    private static final String INDEX_FILE = "index.html";
    private final File rootDir;
    private final int PORT = 3000;

    public HTTPServer(File rootDir){
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("Root directory does not exist or is not a directory.");
        }
        this.rootDir = rootDir;
    }

    public void start() throws IOException{
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        try(ServerSocket server = new ServerSocket(PORT)){
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document Root: " + rootDir);

            Socket client = server.accept();
            RequestProcessor rp = new RequestProcessor(rootDir, INDEX_FILE, client, logger);
            executor.submit(rp);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args){
        // get the Document root
        File root;
        try {
             root = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Supply a document root");
            return;
        }

        try {
            HTTPServer webserver = new HTTPServer(root);
            webserver.start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Server could not start", ex);
        }
    }
}