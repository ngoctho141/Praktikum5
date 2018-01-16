/* (C) 2017, Gudrun Schiedermeier
 * Oracle Corporation Java 1.8.0_92, Linux i386 4.8.14
 * mozart (Intel Core i7-4600U CPU/2701 MHz, 4 Cores, 11776 MB RAM)
 */

import praktikum5.Server;
import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gudrun Schiedermeier, gschied@haw-landshut.de
 * @version 2017-01-06
 */
public class ServerTest {
    /** Server port. */
    public static final int PORT = 4938;

    /** Server hostname. */
    public static final String SERVER_HOST = "localhost";

    /** Directory where processes run. */
    public static final Path TEMPDIR = Paths.get(System.getProperty("java.io.tmpdir"));

    /** Filename of server's persistent datastore. */
    private static final Path datastoreFile = TEMPDIR.resolve("datastore.ser");

    /** Grace period for server to shutdown and startup. Depends on system! */
    private static final long TIMEOUT_MILLIS = 1_000;

    /** Type of server's main class. */
    public static final Class<Server> serverMainType = Server.class;

    /** Compiled server main class file. */
    private static Path serverClassfile;

    /** Directory holding server's main class top-level package. */
    private static Path classpath;

    /** Flag: run silently or print whats going on. */
    private static boolean quiet = System.getProperty("verbose") == null;

    /** Pattern for HTTP response first line. */
    public static final Pattern RESONSE_1ST_LINE_PATTERN = Pattern.compile("HTTP/1.\\S+ (\\d+) .+");

    /**
     * Print messages or supress them.
     *
     * @param format printf format string.
     * @param args   format args, if any.
     */
    private static void log(String format, Object... args) {
        if(quiet)
            return;
        System.out.printf(format, args);
        System.out.flush();
    }

    /**
     * Find server main classfile.
     * Depends on the run environment.
     * Please modify rootDir, maybe "user.home" or "user.dir".
     *
     * @exception IOException when classfile is missing.
     */
    @BeforeClass
    public static void locateServerClassfile() throws IOException {
        final Path rootDir = Paths.get(System.getProperty("java.io.tmpdir"));   // please change if required!
        // walk file tree, looking for server's main classfile
        Files.walkFileTree(rootDir,
                           new SimpleFileVisitor<Path>() {
                               @Override
                               public FileVisitResult visitFile(Path path, BasicFileAttributes notUsed) throws IOException {
                                   if((serverMainType.getSimpleName() + ".class").equals(path.getFileName().toString()))
                                       serverClassfile = path.toAbsolutePath();
                                   return serverClassfile == null? CONTINUE: TERMINATE;
                               }

                               @Override
                               public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                                   return CONTINUE;
                               }
                           });
        log("found Server classfile: %s%n", serverClassfile);
        // classpath = absolute pathname sans FQCN
        final int classpathNameCount = serverClassfile.getNameCount() - serverMainType.getName().split("\\.").length;
        classpath = Paths.get("/").resolve(serverClassfile.subpath(0, classpathNameCount));
        log("using classpath: %s%n", classpath);
    }

    private static void removeDatastore() throws IOException {
        Files.deleteIfExists(datastoreFile);
    }

    private static void restartServer() throws IOException, InterruptedException {
        stopServer();
        startServer();
    }

    /**
     * Start the server in the background und wait until it accepts conections.
     * This requires a new JVM, because the server might use mutable static data.
     */
    private static void startServer() throws IOException, InterruptedException {
        log("starting server ... ");
        runCommand(false, "java", "-cp", classpath.toString(), serverMainType.getName());
        waitForConnection(true);
    }

    /**
     * Stop the server, if it is running at all.
     */
    private static void stopServer() throws IOException, InterruptedException {
        try(Socket socket = new Socket(SERVER_HOST, PORT)) {
            log("stopping server ... ");
            if(runCommand(true, "jkill", "Server") != 0)
                throw new IOException("failed to stop the server");
            waitForConnection(false);
        } catch(SocketException e) {
            // was not not running - fine
        }
    }

    private static void waitForConnection(boolean stateWanted) throws IOException, InterruptedException {// wait for server to die
        boolean stateObserved;
        long startLoop = System.currentTimeMillis();
        do {
            if(System.currentTimeMillis() - startLoop > TIMEOUT_MILLIS)
                throw new SocketException("timeout waiting for server to " + (stateWanted? "startup": "shutdown"));
            try(Socket notused = new Socket(SERVER_HOST, PORT)) {
                stateObserved = true;
            } catch(SocketException e) {
                stateObserved = false;
            }
            if(stateObserved != stateWanted)
                Thread.sleep(100);
        }
        while(stateObserved != stateWanted);
        log("connection is " + (stateObserved? "up": "down"));
    }

    /**
     * Run a system command.
     * @param waitForExit true = wait for command to terminate; false = start command in background.
     * @param token Program name and commandline arguments.
     * @return Exit code or 0, if started in background.
     */
    private static int runCommand(boolean waitForExit, String... token) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(token)
                .directory(TEMPDIR.toFile())
                .redirectErrorStream(true)
                .redirectOutput(INHERIT)
                .start();
        return waitForExit? process.waitFor(): 0;
    }

    /**
     * Call a RESTful service and assert response code.
     * Ignore response body.
     * @param method HTTP method.
     * @param resource Resource without leading slash.
     * @param responsecode Expected HTTP response code.
     */
    private void testRestCall(String method, String resource, int responsecode) throws IOException {
        testRestCall(method, resource, responsecode, null);
    }

    /**
     * Call a RESTful service and assert response code and body.
     * @param method HTTP method.
     * @param resource Resource without leading slash.
     * @param responsecode Expected HTTP response code.
     * @param responsebody Expected first line of response body.
     * null, if response body is ignored.
     */
    private void testRestCall(String method, String resource, int wantResponseCode, String wantResponseBody) throws IOException {
        try(Socket socket = new Socket(SERVER_HOST, PORT);
            OutputStream outputStream = socket.getOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);
            PrintWriter printWriter = new PrintWriter(writer);
            InputStream inputStream = socket.getInputStream();
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)) {
            // send request
            printWriter.printf("%s /%s HTTP/1.0%n", method, resource);
            printWriter.println();
            printWriter.flush();

            // read response
            String response1stLine = bufferedReader.readLine();
            while(!bufferedReader.readLine().isEmpty()) // skip response header fields
                ;
            String responsebody = null;
            if(wantResponseBody != null)
                responsebody = bufferedReader.readLine();

            // analyze response
            Matcher matcher = RESONSE_1ST_LINE_PATTERN.matcher(response1stLine);
            assertTrue("response line matches: " + response1stLine, matcher.matches());
            assertEquals("response code", wantResponseCode, Integer.parseInt(matcher.group(1)));
            if(wantResponseBody != null)
                assertEquals("response body", wantResponseBody, responsebody);
        }
    }

    @Before
    public void before() throws IOException, InterruptedException {
        removeDatastore();
        restartServer();
    }

    @Test
    public void smokeTest() {
        assertTrue(true);
    }

    @Test
    public void createNewMap() throws IOException {
        testRestCall("POST", "foo", 201);
    }

    @Test
    public void createNewMapTwice() throws IOException {
        testRestCall("POST", "foo", 201);
        testRestCall("POST", "foo", 409);
    }

    @Test
    public void createSomeEntries() throws IOException {
        createNewMap();
        testRestCall("PUT", "foo/1/one", 200);
        testRestCall("PUT", "foo/2/two", 200);
        testRestCall("PUT", "foo/3/three", 200);
    }

    @Test
    public void retrieveSomeEntries() throws IOException {
        createSomeEntries();
        testRestCall("GET", "foo/1", 200, "one");
        testRestCall("GET", "foo/2", 200, "two");
        testRestCall("GET", "foo/3", 200, "three");
    }

    @Test
    public void retrieveSomeEntriesMultipleTimes() throws IOException {
        createSomeEntries();
        for(int repeats = 0; repeats < 3; repeats++) {
            testRestCall("GET", "foo/1", 200, "one");
            testRestCall("GET", "foo/2", 200, "two");
            testRestCall("GET", "foo/3", 200, "three");
        }
    }

    @Test
    public void retrieveMissingEntry() throws IOException {
        createSomeEntries();
        testRestCall("GET", "foo/4", 404);
    }

    @Test
    public void createAndRetrieveMultipleMapsAndMultipleEntries() throws IOException {
        final String[] idlist = {"foo", "bar", "baz"};
        for(String id : idlist) {
            testRestCall("POST", id, 201);
            final String[] keylist = {id + '1', id + '2', id + '3'};
            for(String key : keylist)
                testRestCall("PUT", id + '/' + key + '/' + key + '*', 200);
            for(String key : keylist)
                testRestCall("GET", id + '/' + key, 200, key + '*');

        }
    }

    @Test
    public void deleteEntryFromMissingMap() throws IOException {
        testRestCall("DELETE", "foo/4", 404);
    }

    @Test
    public void deleteMissingEntry() throws IOException {
        createNewMap();
        testRestCall("DELETE", "foo/4", 204);
    }

    @Test
    public void deleteEntries() throws IOException {
        createSomeEntries();
        testRestCall("DELETE", "foo/1", 204);
        testRestCall("DELETE", "foo/2", 204);
        testRestCall("DELETE", "foo/3", 204);
        testRestCall("GET", "foo/1", 404);
        testRestCall("GET", "foo/2", 404);
        testRestCall("GET", "foo/3", 404);
    }

    @Test
    public void deleteAllEntries() throws IOException {
        createSomeEntries();
        testRestCall("DELETE", "foo/*", 204);
        testRestCall("GET", "foo/1", 404);
        testRestCall("GET", "foo/2", 404);
        testRestCall("GET", "foo/3", 404);
    }

    @Test
    public void deleteMap() throws IOException {
        createNewMap();
        testRestCall("DELETE", "foo", 204);
        testRestCall("GET", "foo/1", 404);
        testRestCall("GET", "foo/2", 404);
        testRestCall("GET", "foo/3", 404);
    }

    @Test
    public void recreateDeletedMap() throws IOException {
        createNewMap();
        testRestCall("DELETE", "foo", 204);
        testRestCall("POST", "foo", 201);
    }

    @Test
    public void deleteMapTwice() throws IOException {
        createNewMap();
        testRestCall("DELETE", "foo", 204);
        testRestCall("DELETE", "foo", 404);
    }

    @Test
    public void getPersistentData() throws IOException, InterruptedException {
        createSomeEntries();
        restartServer();
        testRestCall("GET", "foo/1", 200, "one");
        testRestCall("GET", "foo/2", 200, "two");
        testRestCall("GET", "foo/3", 200, "three");
    }
}
