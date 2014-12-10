package DecisionSupport;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class textAreaOutput extends OutputStream {

    private final JTextArea textArea;

    private final StringBuilder sb = new StringBuilder();
  
    
    
    public textAreaOutput(final JTextArea textArea) {
        this.textArea = textArea;
    }

    public void flush() {
    }

    public void close() {
    }

    public void write(int b) throws IOException {

    	
    	
        if (b == '\r') 
        {
            return;
        }
        else if (b == '\n') 
        {
            final String text = sb.toString() + "\n";

            textArea.append(text);
            sb.setLength(0);
        } 
        else 
        {
            sb.append((char) b);
        }
        
       
        
        
    }	
	
}


