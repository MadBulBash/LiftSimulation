import java.io.IOException;
import javax.swing.JTextArea;
import org.apache.log4j.FileAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogAppender {
    private static Logger logText;
    private static Logger logFile;
    private WriterAppender appenderText;
    private FileAppender appenderFile;
    private JTextArea textArea;
    private PatternLayout layout;
    
    public LogAppender(JTextArea textArea) {
        String pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] %m%n";
        layout = new PatternLayout(pattern);
        appenderText = new WriterAppender();
        appenderText.setLayout(layout);
        appenderText.setImmediateFlush(true);
        this.textArea = textArea;
        logText = Logger.getLogger(LogAppender.class);
        logFile = Logger.getLogger(LogAppender.class);

        try {
            appenderFile = new FileAppender(layout, "LiftSimulation.log", true);
        } catch (IOException ex) {
            return;
        }
        appenderFile.setImmediateFlush(true);
        logFile.addAppender(appenderFile);        
    }

    public synchronized void add(String txt) {
        logFile.debug(txt);
        String message = this.layout.format(new LoggingEvent(
            "org.apache.log4j.Logger", logText, Level.INFO, txt, null));
        textArea.append(message);
        textArea.setCaretPosition(textArea.getDocument().getLength() - 1);     
    }   
}
