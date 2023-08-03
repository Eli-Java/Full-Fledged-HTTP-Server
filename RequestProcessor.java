import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
public class RequestProcessor implements  Runnable{
    private File rootDir;
    private String indexFileName;
    private Socket connection;
    Logger logger;
    public RequestProcessor(File rootDir, String indexFileName, Socket connection, Logger logger){
        this.rootDir = rootDir;
        this.indexFileName = indexFileName;
        this.connection = connection;
        this.logger = logger;
    }

    public void sendMimeHeader(Writer out, String responseCode, String contentType, int length) throws IOException{
        out.write(responseCode + "\r\n");
        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-length: " + length + "\r\n");
        out.write("Content-type: " + contentType + "\r\n\r\n");
        out.flush();
    }

    public void run() {
        try {
            //Writer out = new BufferedWriter(new OutputStreamWriter(
             //       connection.getOutputStream(), "US-ASCII"));
            OutputStream raw = new BufferedOutputStream(
                    connection.getOutputStream()
            );
            Writer out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(new BufferedInputStream(
                    connection.getInputStream()));

            StringBuilder request = new StringBuilder(80);
            while (true) {
                int c = in.read();
                if (c == '\r' || c == '\n' || c == -1) break;
                request.append((char) c);
            }
            String request_str = request.toString();
            logger.info(connection.getRemoteSocketAddress() + " " + request_str);
            String[] tokens = request_str.split("\\s+");
            String request_type = tokens[0];

            if (request_type.equals("GET")) {
                //create a http header
                String fileName = tokens[1];
                if (fileName.endsWith("/")) fileName += indexFileName;
                String mimeType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
                File fileToAccess = new File(rootDir, fileName.substring(1, fileName.length()));
                System.out.println(fileName.substring(1, fileName.length()));

                if (fileToAccess.canRead() && fileToAccess.getCanonicalPath().startsWith(rootDir.getPath())) {
                    byte[] fileData = Files.readAllBytes(fileToAccess.toPath());
                    if (tokens.length > 3 && tokens[2].startsWith("HTTP/")) {
                        sendMimeHeader(out, "HTTP/1.0 200 OK", mimeType, fileData.length);
                    }
                    out.write(Arrays.toString(fileData));
                    out.flush();
                }

            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}