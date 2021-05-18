package my.java.project.spotify.exceptionlogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ErrorLogger {
    public static void logClientError(Exception e) {

        String textToLog = getCurrTimeFormatted() + System.lineSeparator() + System.lineSeparator()
                + "Exception message: " + e.getMessage()
                + System.lineSeparator() + System.lineSeparator()
                + "Exception stacktrace: " + Arrays.toString(e.getStackTrace())
                + System.lineSeparator() + System.lineSeparator()
                + "---------------------------------------------"
                + System.lineSeparator() + System.lineSeparator();

        File logFile = new File("clientLogFile.txt");
        try {
            logFile.createNewFile();
            Files.write(Paths.get("clientLogFile.txt"), textToLog.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException loggerExc) {
            loggerExc.printStackTrace();
        }
    }



        private static String getCurrTimeFormatted() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateString = dtf.format(now);
        return "| " + dateString + " | ";
    }
}
