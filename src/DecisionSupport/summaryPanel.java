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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import DecisionSupport.perfPanel.RANK_OPTION;
import DecisionSupport.perfPanel.SUMMARY_MODE;

public class summaryPanel extends JPanel{
	
    private ArrayList<Integer> hist=null;
    private ArrayList<Double> freq=null;
    private ArrayList<String> names=null;
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

    
    summaryPanel()
    {
		
        TitledBorder border = new TitledBorder(
                null,
                "Summary",
                TitledBorder.CENTER,
                TitledBorder.BELOW_TOP);
        //border.setTitleColor(Color.black);
        setBorder(border);   
        
    }
    
    
    public void init()
    {
    	dataLoaded=false;  
    	revalidate();
    }

    public void loadData(rResult ret, SUMMARY_MODE mode)
    {
    	freq=null;
    	if (ret==null)
    	{
    		return;
    	}
    	hist = new ArrayList<Integer> (Arrays.asList(ret.getHist()));
    	
    	if (mode == SUMMARY_MODE.FREQUENT_M)
    	{
    		ArrayList<Integer> periods = new ArrayList<Integer> (Arrays.asList(ret.getPeriods()));
    		freq = new ArrayList<Double>();
    		for (int i=0; i<hist.size(); i++)
    		{
    			freq.add(1.0*hist.get(i)/periods.get(i));
    		}
    		
        	ArrayList<String> tmp = new ArrayList<String> (Arrays.asList(ret.getNames()));
        	names = new ArrayList<String>();

        	// sort the freq and names in descending order
        	ArrayList<Double> store = new ArrayList<Double>(freq); // may need to be new ArrayList(nfit)
        	Collections.sort(freq, Collections.reverseOrder());
        	for (int i=0; i<freq.size(); i++)
        	{
        	    int index = store.indexOf(freq.get(i));
        	    names.add(tmp.get(index));
        	    store.remove(index);
        	    tmp.remove(index);
        	}    		
    	}
    	else
    	{
        	ArrayList<String> tmp = new ArrayList<String> (Arrays.asList(ret.getNames()));
        	names = new ArrayList<String>();

        	// sort the hist and names in descending order
        	ArrayList<Integer> store = new ArrayList<Integer>(hist); // may need to be new ArrayList(nfit)
        	Collections.sort(hist, Collections.reverseOrder());
        	for (int i=0; i<hist.size(); i++)
        	{
        	    int index = store.indexOf(hist.get(i));
        	    names.add(tmp.get(index));
        	    store.remove(index);
        	    tmp.remove(index);
        	}
    	}
    	
    	
    	dataLoaded=true;
    	repaint();
    }
        
    
    
    protected void paintComponent(Graphics g) 
    {
    	super.paintComponent(g);
    	if (dataLoaded == false)
    	{
    		return;
    	}
    	
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Dimension d = getSize();
        
        
        int xLength = d.width - 2*ROC_PAD;
        int yLength = d.height - 2*ROC_PAD;
        
        // draw boarder        
        g2.draw(new Rectangle2D.Double(PAD, PAD, d.width - 2*PAD, d.height - 2*PAD));

        
        if (freq==null && hist!=null)
        {
	        // draw X-axis
	        int maxHist = hist.get(0) + 1;
	        int hb = Math.min(maxHist, 10);
	        for (int i=0; i<hb+1; i++)
	        {
	        	int x0 = (int)(ROC_PAD + 1.0*i/(hb)*xLength);
	        	int y0 = ROC_PAD + yLength + (ROC_PAD-PAD), y1 = ROC_PAD + yLength + (ROC_PAD-PAD) + 10;
	        	g2.draw(new Line2D.Double(x0, y0, x0, y1));
	        	g2.drawString(String.valueOf((int)(1.0*i/hb*maxHist+0.5)), x0-8, y0+20);
	        }
	        
	        
	        // draw bar for top 10 names' hist
	        for (int i=0; i<Math.min(hist.size(), 10); i++)
	        {
	        	int y0 = ROC_PAD + (int)((1.0*i/10)*yLength) - PAD/8;
	        	int x0 = ROC_PAD;
	        	g2.fillRect(x0,y0,(int)(1.0*hist.get(i)/maxHist*xLength), PAD/3);
	        	
	        	// name
	        	g2.drawString(names.get(i), x0+(int)(1.0*hist.get(i)/maxHist*xLength)+5, ROC_PAD + (int)((1.0*i/10)*yLength) + PAD/6 );
	        }
        
        }
        else if (freq!=null)
        {
	        // draw X-axis
	        double maxHist = freq.get(0);
	        for (int i=0; i<11; i++)
	        {
	        	int x0 = (int)(ROC_PAD + 0.1*i*xLength);
	        	int y0 = ROC_PAD + yLength + (ROC_PAD-PAD), y1 = ROC_PAD + yLength + (ROC_PAD-PAD) + 10;
	        	g2.draw(new Line2D.Double(x0, y0, x0, y1));
	        	String axis = String.format("%.2f", 0.1*i*maxHist);
	        	g2.drawString(axis, x0-8, y0+20);
	        }
	        
	        
	        // draw bar for top 10 names' hist
	        for (int i=0; i<Math.min(freq.size(), 10); i++)
	        {
	        	int y0 = ROC_PAD + (int)((1.0*i/10)*yLength) - PAD/8;
	        	int x0 = ROC_PAD;
	        	g2.fillRect(x0,y0,(int)(1.0*freq.get(i)/maxHist*xLength), PAD/3);
	        	
	        	// name
	        	g2.drawString(names.get(i), x0+(int)(1.0*freq.get(i)/maxHist*xLength)+5, ROC_PAD + (int)((1.0*i/10)*yLength) + PAD/6 );
	        }        	
        }
        
        
    }
     


}
