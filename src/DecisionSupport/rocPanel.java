package DecisionSupport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.solr.client.solrj.SolrServerException;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

public class rocPanel extends JPanel{


    private double[] x_data=null;
    private double[] y_data=null;
    private boolean dataLoaded = false;
    
    final int PAD = 80; // original point (PAD,PAD)
    final int ROC_PAD = 100;
    
    
    final static int maxCharHeight = 15;
    final static int minFontSize = 6;
 
    final static Color bg = Color.white;
    final static Color fg = Color.black;
    final static Color red = Color.red;
    final static Color white = Color.white;    
    
	
    FontMetrics fontMetrics;    

    
    rocPanel()
    {
		
        TitledBorder border = new TitledBorder(
                null,
                "ROC",
                TitledBorder.CENTER,
                TitledBorder.BELOW_TOP);
        //border.setTitleColor(Color.black);
        setBorder(border);   
        
    }
    

    public void loadData(rResult ret)
    {
    	x_data = ret.getX();
    	y_data = ret.getY();
    	
    	dataLoaded=true;
    	repaint();
    }
        
    
    protected void paintComponent(Graphics g) 
    {
    	if (dataLoaded == false)
    	{
    		return;
    	}
    	
    	super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Dimension d = getSize();
        
        
        
        int xLength = d.width - 2*ROC_PAD;
        int yLength = d.height - 2*ROC_PAD;
        
        // draw boarder        
        g2.draw(new Rectangle2D.Double(PAD, PAD, d.width - 2*PAD, d.height - 2*PAD));

        // draw X-axis
        for (int i=0; i<11; i++)
        {
        	int x0 = (int)(ROC_PAD + i*0.1*xLength);
        	int y0 = ROC_PAD + yLength + (ROC_PAD-PAD), y1 = ROC_PAD + yLength + (ROC_PAD-PAD) + 10;
        	g2.draw(new Line2D.Double(x0, y0, x0, y1));
        	g2.drawString(String.valueOf(1.0*i/10), x0-8, y0+20);
        }
        
        // draw Y-axis
        for (int i=0; i<11; i++)
        {
        	int x0 = PAD - 10, x1 = PAD;
        	int y0 = ROC_PAD + (int)((1-1.0*i/10)*yLength);
        	g2.draw(new Line2D.Double(x0, y0, x1, y0));
        	g2.drawString(String.valueOf(1.0*i/10), x0-20, y0+5);
        }
        
        // draw Xlabel
        g2.setFont(new Font(Font.SERIF, Font.BOLD, 20));
        g2.drawString("False Positive Rate", ROC_PAD + xLength/2 - 50, ROC_PAD + yLength + (PAD));

        
        // draw GeneralPath (polyline)
		 
        int x2Points[] = new int[x_data.length];
        int y2Points[] = new int[y_data.length];
        
        for (int i=0; i<x_data.length; i++)
        {
        	x2Points[i] = (int) (ROC_PAD + x_data[i]*xLength);
        	y2Points[i] = (int) (ROC_PAD + (1-y_data[i])*yLength);
        }
        
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x_data.length);
        polyline.moveTo (x2Points[0], y2Points[0]);
        for ( int index = 1; index < x_data.length; index++ ) {
            polyline.lineTo(x2Points[index], y2Points[index]);
        };
 
        g2.draw(polyline);
        
        // draw Ylabel
/*        AffineTransform at = new AffineTransform();
        at.setToRotation(Math.toRadians(-90),d.width/2,d.height/2);
        g2.setTransform(at);
        g2.drawString("True Positive Rate", ROC_PAD + yLength/2 - 50, ROC_PAD/2);  // after transform, the axis system also reversed.        
        
        at.setToRotation(Math.toRadians(90),d.width/2,d.height/2);  // rotate back
        g2.setTransform(at);*/
    }
     
    
     
/*    public static void main(String[] args) 
    {
		featureMap fm = new featureMap();
		fDocument fdoc = new fDocument();
		rResult ret=null;
		
		try 
		{
			//fm.clearIndex();
			//fm.loadLibrarytoSolr("Carseats");
			fdoc = fm.loadfromSolr("Carseats", "*:*");
			ret=fm.trainModel(fdoc);
		} 
		catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	
    	
        JFrame f = new JFrame();
        rocPanel roc = new rocPanel();
        roc.loadData(ret);
        
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(roc);
        f.setSize(400,400);
        f.setLocation(200,200);
        f.setVisible(true);        
            
    }	*/
	
}
