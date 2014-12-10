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
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import DecisionSupport.perfPanel.RANK_OPTION;
import DecisionSupport.perfPanel.SUMMARY_MODE;
import DecisionSupport.solrConnector.OPT_CODE;

public class rankPanel extends JPanel{
	
    private ArrayList<Map.Entry<String,Double>> ranks=null;
    private double average = 0;
    private boolean dataLoaded = false;
    
    private OPT_CODE mode=OPT_CODE.FREE;
    
    final int PAD = 80; // original point (PAD,PAD)
    final int ROC_PAD = 100;
    
    
    final static int maxCharHeight = 15;
    final static int minFontSize = 6;
 
    final static Color bg = Color.white;
    final static Color fg = Color.black;
    final static Color red = Color.red;
    final static Color white = Color.white;    
    
	
    FontMetrics fontMetrics;    

    
    rankPanel()
    {
		
        TitledBorder border = new TitledBorder(
                null,
                "Ranking",
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

    public void loadData(rResult ret, OPT_CODE mode)
    {
    	if (ret==null)
    	{
    		return;
    	}
    	
    	if (mode == OPT_CODE.OPENED_RDG)
    	{
    		ranks = ret.getRanksOpened();
    		average = ret.getAvgOpened();
    	}
    	else if (mode == OPT_CODE.RESOLVED_RDG)
    	{
    		ranks = ret.getRanksClosed();
    		average = ret.getAvgClosed();
    	}
    	else if (mode == OPT_CODE.RECOMM_RDG)
    	{
    		ranks = ret.getDistance();
    	}
    	
    	this.mode = mode;
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
        
        
        int xLength = d.width - 4*ROC_PAD;
        int yLength = d.height - 2*ROC_PAD;
        
        // draw boarder        
        g2.draw(new Rectangle2D.Double(PAD, PAD, d.width - 2*PAD, d.height - 2*PAD));
        
        if (ranks!=null && ranks.size()>0)
        {
	        // draw X-axis
	        double maxHist = ranks.get(ranks.size()-1).getValue();
	        for (int i=0; i<11; i++)
	        {
	        	int x0 = (int)(ROC_PAD + 0.1*i*xLength);
	        	int y0 = ROC_PAD + yLength + (ROC_PAD-PAD), y1 = ROC_PAD + yLength + (ROC_PAD-PAD) + 10;
	        	g2.draw(new Line2D.Double(x0, y0, x0, y1));
	        	String axis = String.format("%.2f", 0.1*i*maxHist);
	        	g2.drawString(axis, x0-8, y0+20);
	        }
	        
	        
	        // draw bar for top 10 names' hist
	        for (int i=0; i<Math.min(ranks.size(), 10); i++)
	        {
	        	int y0 = ROC_PAD + (int)((1.0*i/10)*yLength) - PAD/8;
	        	int x0 = ROC_PAD;
	        	g2.fillRect(x0,y0,(int)(1.0*ranks.get(ranks.size()-i-1).getValue()/maxHist*xLength), PAD/3);
	        	
	        	// name
	        	g2.drawString(ranks.get(ranks.size()-i-1).getKey(), x0+(int)(1.0*ranks.get(ranks.size()-i-1).getValue()/maxHist*xLength)+5, ROC_PAD + (int)((1.0*i/10)*yLength) + PAD/6 );
	        }        	
	        
	        
	     // draw Xlabel
	        g2.setFont(new Font(Font.SERIF, Font.BOLD, 20));
	        if (mode == OPT_CODE.OPENED_RDG || mode == OPT_CODE.RESOLVED_RDG)
	        {
	        	g2.drawString("Time (Days)", ROC_PAD + xLength/2 - 50, ROC_PAD + yLength + (PAD));	
	        }
	        	        
	        
        }
        
        
    }

}

