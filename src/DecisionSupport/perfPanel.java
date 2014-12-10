package DecisionSupport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import DecisionSupport.rConnector.DataModel;
import DecisionSupport.solrConnector.OPT_CODE;

public class perfPanel extends JPanel implements ItemListener{

    private double[] x_data=null;
    private double[] y_data=null;
    private double acc=0;
    private double auc=0;
    private boolean dataLoaded = false;
    
    final int PAD = 80; // original point (PAD,PAD)
    final int ROC_PAD = 100;
    
    
    final static int maxCharHeight = 15;
    final static int minFontSize = 6;
 
    final static Color bg = Color.white;
    final static Color fg = Color.black;
    final static Color red = Color.red;
    final static Color white = Color.white;    
    
    private rocPanel picPane=null;
    private JPanel metricPane=null;
    private summaryPanel summaryPane=null;
    private rankPanel rankPane=null;
    private histPanel histPane=null;
	
    private JRadioButton ablButton=null;
	private JRadioButton feqButton=null;
	private ButtonGroup group=null;
    
    public enum SUMMARY_MODE {ABSOLUTE_M, FREQUENT_M}
    public enum HIST_OPTION {EMAIL_O, TR_O}
    public enum RANK_OPTION {RESOLVED_R, OPENED_R}
    public enum RATIO_OPTION {RESOLVED_R, OPENED_R}
    
    private SUMMARY_MODE s_mode=SUMMARY_MODE.ABSOLUTE_M;
    
    private final int SPAD=20;
	
    FontMetrics fontMetrics;    	
	
	perfPanel()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		
        Dimension size = new Dimension(screenWidth/2, screenHeight-SPAD*2);	
        setMaximumSize(size);
        setPreferredSize(size);
        setMinimumSize(size);	
        TitledBorder border = new TitledBorder(
                new LineBorder(Color.black),
                "Prediction Performance",
                TitledBorder.CENTER,
                TitledBorder.BELOW_TOP);
        border.setTitleColor(Color.black);
        setBorder(border);   
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // picPanel
        picPane = new rocPanel();
        size = new Dimension(screenWidth/2+300, screenHeight-SPAD*2-300);
        picPane.setPreferredSize(size);
        picPane.setMaximumSize(size);
        picPane.setPreferredSize(size);
        picPane.setMinimumSize(size);          
        //add(picPane);
        
        // summaryPanel
        summaryPane = new summaryPanel();
        size = new Dimension(screenWidth/2+300, screenHeight-SPAD*2-300);
        summaryPane.setPreferredSize(size);
        summaryPane.setMaximumSize(size);
        summaryPane.setPreferredSize(size);
        summaryPane.setMinimumSize(size);
        
        
        // histPanel
        histPane = new histPanel();
        size = new Dimension(screenWidth/2+300, screenHeight-SPAD*2-300);
        histPane.setPreferredSize(size);
        histPane.setMaximumSize(size);
        histPane.setPreferredSize(size);
        histPane.setMinimumSize(size);        
        
        
        
        // rankPanel
        rankPane = new rankPanel();
        size = new Dimension(screenWidth/2+300, screenHeight-SPAD*2-300);
        rankPane.setPreferredSize(size);
        rankPane.setMaximumSize(size);
        rankPane.setPreferredSize(size);
        rankPane.setMinimumSize(size);   
        
        
        // other metrics panel
        metricPane = new JPanel();
        size = new Dimension(screenWidth/2, 100);
        metricPane.setPreferredSize(size);
        metricPane.setMaximumSize(size);
        metricPane.setPreferredSize(size);
        metricPane.setMinimumSize(size);
        metricPane.setLayout(new BoxLayout(metricPane,BoxLayout.Y_AXIS));
        //add(metricPane);		
	}
	
	
    protected void loadMLData(rResult ret, DataModel model)
    {
    	acc=ret.getAcc();
    	auc=ret.getAuc();

    	// redraw the roc
    	picPane.loadData(ret);
    	
    	// update label
    	metricPane.removeAll();
    	
    	JLabel modelL = new JLabel("Model: " + model.getStr());
    	JLabel aucL = new JLabel("AUC: " + auc);
    	JLabel accL = new JLabel("ACC: " + acc);
    	modelL.setFont(new Font(Font.SERIF, Font.PLAIN, 14));
    	aucL.setFont(new Font(Font.SERIF, Font.PLAIN, 14));
    	accL.setFont(new Font(Font.SERIF, Font.PLAIN, 14));
    	metricPane.add(modelL);
    	metricPane.add(aucL);
    	metricPane.add(accL);
    	modelL.setAlignmentX(Component.LEFT_ALIGNMENT);
    	aucL.setAlignmentX(Component.LEFT_ALIGNMENT);
    	accL.setAlignmentX(Component.LEFT_ALIGNMENT);
    	
    	add(picPane);
    	add(metricPane);
    }
    
    
    public SUMMARY_MODE getSummaryMode()
    {
    	return s_mode;
    }
    
    public void initSummary()
    {
    	removeAll();
    	
    	summaryPane.init();
    	add(summaryPane);
    	
    	// update metric labels
    	metricPane.removeAll();
    
        
        add(metricPane);
        
        // init s_mode
        s_mode = SUMMARY_MODE.ABSOLUTE_M;
    }
    
    protected void loadSummaryData(rResult ret, SUMMARY_MODE mode)
    {
    	// redraw the roc
    	summaryPane.loadData(ret, mode);
    	summaryPane.setVisible(true);
    	//add(summaryPane);
    }    
    
    
    protected void loadHistData(rResult ret, HIST_OPTION option)
    {
    	removeAll();
    	add(histPane);
    	histPane.loadData(ret);
    	histPane.setVisible(true);
    	
    	metricPane.removeAll();
    	JLabel label=null;
        if (option == HIST_OPTION.EMAIL_O)
        {
        	label = new JLabel("#Emails processing along the time");
        }
        else
        {
        	label = new JLabel("#TRs processing along the time");
        }
        
    	label.setFont(new Font(Font.SERIF, Font.PLAIN, 20));
    	metricPane.add(label);
    	add(metricPane);    	
    }
    
    
    protected void loadRatioData(rResult ret, RATIO_OPTION option)
    {
    	removeAll();
    	add(histPane);
    	histPane.loadData(ret);
    	
    	
    	metricPane.removeAll();
    	JLabel label=null;
        if (option == RATIO_OPTION.OPENED_R)
        {
        	if (histPane.isDataLoaded())
        	{
        		histPane.setVisible(true);
        		label = new JLabel("TR opened ratio along the time");
        	}
        	else
        	{
        		histPane.setVisible(false);
        		label = new JLabel("No relevant data found");
        	}
        }
        else
        {
        	if (histPane.isDataLoaded())
        	{
        		histPane.setVisible(true);
        		label = new JLabel("TR closed ratio along the time");
        	}
        	else
        	{
        		histPane.setVisible(false);
        		label = new JLabel("No relevant data found");
        	}
        }
        
    	label.setFont(new Font(Font.SERIF, Font.PLAIN, 20));
    	metricPane.add(label);
    	add(metricPane);    	
    }    
    
    
    
    protected void loadRankData(rResult ret, OPT_CODE mode)
    {
    	removeAll();
    	add(rankPane);
    	rankPane.loadData(ret, mode);
    	rankPane.setVisible(true);
    	
    	metricPane.removeAll();
    	JLabel label=null;
        if (mode == OPT_CODE.OPENED_RDG)
        {
        	label = new JLabel("Average life time of opened TR" + String.format("%.2f", ret.getAvgOpened()) );
        }
        else if (mode == OPT_CODE.RESOLVED_RDG)
        {
        	label = new JLabel("Average life time of resolved TR " + String.format("%.2f", ret.getAvgClosed()) );
        }
        else if (mode == OPT_CODE.RECOMM_RDG)
        {
        	label = new JLabel("Similarity");
        }
        
    	label.setFont(new Font(Font.SERIF, Font.PLAIN, 20));
    	metricPane.add(label);
    	add(metricPane);
    }
    
    
    
    
    public void itemStateChanged(ItemEvent e) 
    {

        Object source = e.getItemSelectable();
 
        if (source == ablButton)
        {
        	// the default mode is absolute
        	s_mode = SUMMARY_MODE.ABSOLUTE_M;
        }
        
        if (source == feqButton)
        {
        	if (e.getStateChange() == ItemEvent.DESELECTED) {
        		s_mode = SUMMARY_MODE.ABSOLUTE_M;
            }
        	else
        	{
        		s_mode = SUMMARY_MODE.FREQUENT_M;
        	}
        }
        
    }      
    
	
}
