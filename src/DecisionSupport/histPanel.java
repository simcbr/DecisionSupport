package DecisionSupport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class histPanel extends JPanel{
	
	private Date[] dates=null;
    private int[] counts=null;

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

    
    histPanel()
    {
		
        TitledBorder border = new TitledBorder(
                null,
                "Histogram",
                TitledBorder.CENTER,
                TitledBorder.BELOW_TOP);
        //border.setTitleColor(Color.black);
        setBorder(border);   
        
    }
    
    
    public boolean isDataLoaded()
    {
    	return dataLoaded;
    }

    public void loadData(rResult ret)
    {
        dataLoaded=false;
    	Date[] tmp_dates = ret.getDates();
    	int[] tmp_counts = ret.getCounts();
    	
    	int ind;
    	for (ind=0; ind<tmp_counts.length; ind++)
    	{
    		if (tmp_counts[ind]!=0)
    		{
    			break;
    		}
    	}
    	
    	dates = new Date[tmp_counts.length - ind];
    	counts = new int[tmp_counts.length - ind];
    	
    	System.arraycopy(tmp_dates, ind, dates, 0, tmp_counts.length-ind);
    	System.arraycopy(tmp_counts, ind, counts, 0, tmp_counts.length-ind);
    	
    	
    	if (dates.length >0 && counts.length>0)
    	{
    		dataLoaded=true;
    		
    	}
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
        int[] datesDiff = new int[dates.length];
        for (int i=0; i<dates.length; i++)
        {
        	long tmp = dates[i].getTime() - dates[0].getTime();
        	datesDiff[i] = (int)(tmp/(1000*60*60*24));  // difference in days
        }
        
        int maxD=datesDiff[datesDiff.length-1];
        int maxX=(maxD+10)/10*10;
        long timesX=maxX/10;
        long tmp = dates[0].getTime();
        for (int i=0; i<11; i++)
        {
        	int x0 = (int)(ROC_PAD + 0.1*i*xLength);
        	int y0 = ROC_PAD + yLength + (ROC_PAD-PAD), y1 = ROC_PAD + yLength + (ROC_PAD-PAD) + 10;
        	g2.draw(new Line2D.Double(x0, y0, x0, y1));
        	
        	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        	tmp = tmp + timesX*(1000*60*60*24);
        	String axis = sdf.format(new Date(tmp));
        	g2.drawString(axis, x0-8, y0+20);
        }
        
        
        // draw Y-axis
        int maxC=0;
        for (int i=0; i<counts.length; i++)
        {
        	if (maxC<counts[i])
        	{
        		maxC=counts[i];
        	}
        }
        
        int maxY = (maxC+10)/10*10;
        int timesY = maxY/10;
        for (int i=0; i<11; i++)
        {
        	int x0 = PAD - 10, x1 = PAD;
        	int y0 = ROC_PAD + (int)((1-1.0*i/10)*yLength);
        	g2.draw(new Line2D.Double(x0, y0, x1, y0));
        	String axis = String.format("%.0f", 0.1*i*maxY);
        	g2.drawString(axis, x0-20, y0+5);
        }
        
        // draw Xlabel
        g2.setFont(new Font(Font.SERIF, Font.BOLD, 20));
        g2.drawString("Date(dd/mm)", ROC_PAD + xLength/2 - 50, ROC_PAD + yLength + (PAD));

        
        // draw GeneralPath (polyline)
		 
        //int x2Points[] = new int[datesDiff.length];
        //int y2Points[] = new int[counts.length];
        
        int x2Points[] = new int[datesDiff[datesDiff.length-1]+1];
        int y2Points[] = new int[datesDiff[datesDiff.length-1]+1];

        for (int i=0; i<datesDiff[datesDiff.length-1]+1; i++)
        {
        	x2Points[i] = (int) (ROC_PAD + (int)(1.0*i/maxX*xLength));
        	int ind=Arrays.binarySearch(datesDiff, i);
        	if ( ind < 0)
        	{
        		y2Points[i] = (int) (ROC_PAD + yLength);
        	}
        	else
        	{
        		y2Points[i] = (int) (ROC_PAD + (1-1.0*counts[ind]/maxY)*yLength);
        	}
        	
        }
        
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, datesDiff.length);
        polyline.moveTo (x2Points[0], y2Points[0]);
        for ( int index = 1; index <datesDiff[datesDiff.length-1]+1; index++ ) {
            polyline.lineTo(x2Points[index], y2Points[index]);
        };
 
        g2.draw(polyline);
        
    }
    
    


}



