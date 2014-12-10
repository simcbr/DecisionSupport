package DecisionSupport;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.*;        
import javax.swing.text.DefaultCaret;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import DecisionSupport.perfPanel.HIST_OPTION;
import DecisionSupport.perfPanel.RATIO_OPTION;
import DecisionSupport.perfPanel.SUMMARY_MODE;
import DecisionSupport.rConnector.DataModel;
import DecisionSupport.solrConnector.OPT_CODE;


/*
 *  The whole program GUI is windows (frame) based.  Each menu is responsible for a frame.
 */


public class GUI extends JFrame
{
	public static GUI gui = null;
	private featureMap fm = null;
	private perfPanel perfP = null;
	private solrPanel solrP = null;
	
	private enum STATUS {INIT_S, LOADDATA_S, SUMMARY_S, READY_S;}
	private STATUS state = STATUS.INIT_S;
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
	private JTextArea output;
    private JScrollPane scrollPane;
    private JProgressBar progressBar;
    
    private JMenuItem load_menuItem, query_menuItem, sta_sum_menuItem, sta_hist_menuItem, 
    				  sta_rank_menuItem, sta_rate_menuItem, recDB_build_menuItem, recDB_query_menuItem;
    
    String newline = "\n";

    GUI()
    {
    	fm = featureMap.getInstance();
    	perfP = new perfPanel();
    	solrP = new solrPanel();
    	
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DecisionSupport");
    	setExtendedState(JFrame.MAXIMIZED_BOTH);
    	setLocation(0,0); // default is 0,0 (top left corner) 
    	
        setVisible(true);    	
    }
    
    
    
    public void createMenuBar() {
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;
 
        //Create the menu bar.
        menuBar = new JMenuBar();
 
        //Build the first menu.
        menu = new JMenu("Datasets");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");
        menuBar.add(menu);
 
        //a group of JMenuItems
        load_menuItem = new JMenuItem("Load datasets",
                                 KeyEvent.VK_L);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        load_menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, ActionEvent.ALT_MASK));
        load_menuItem.getAccessibleContext().setAccessibleDescription(
                "Loading available datasets");
        load_menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (state == STATUS.INIT_S)
            	{
            		solrP.connectDB();
                	revalidate();
                	state= STATUS.LOADDATA_S;	
            	}
            	
            }
        });        
        menu.add(load_menuItem);
 
        query_menuItem = new JMenuItem("Query");
        query_menuItem.setMnemonic(KeyEvent.VK_Q);
        query_menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
            	{
            		solrP.qPanel_O_query();
                	revalidate();
                	//state= STATUS.READY_S;
            	}
            }
        });          
        menu.add(query_menuItem);
 
        
        // Quit menuitem
        menuItem = new JMenuItem("Quit");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Quit");
                setVisible(false);
                dispose();
                System.exit(0);
            }
        });
        menu.add(menuItem);        
        
        
        // Build the second menu in the menu bar.
        menu = new JMenu("Statistic");
        menu.setMnemonic(KeyEvent.VK_S);   // accelerator key
        menu.getAccessibleContext().setAccessibleDescription(
                "This menu lists the statistic functionalities.");
        menuBar.add(menu);
        
        
        sta_sum_menuItem = new JMenuItem("Summary");
        sta_sum_menuItem.setMnemonic(KeyEvent.VK_S);
        sta_sum_menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
            	{
	        		solrP.clearChoices();
	        		solrP.addChoices("fromdisp");
	        		solrP.addChoices("mtm");
	        		solrP.qPanel_O_summary();
        			perfP.initSummary();
            		revalidate();
            	}
            }
        });  
        menu.add(sta_sum_menuItem);
        
/*        //  statistic-summary submenu
        submenu = new JMenu("Summary");     
        menu.add(submenu);     

        menuItem = new JMenuItem("Absolute");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
        		{
	        		solrP.clearChoices();
	        		solrP.addChoices("fromdisp");
	        		solrP.addChoices("mtm");
	        		solrP.qPanel_O_summary();
        			perfP.initSummary();
        			updateSummaryPanel(solrP.getChoices(), SUMMARY_MODE.ABSOLUTE_M);
            		revalidate();
        		}
        	}
        });           
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("Frequency");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
        		{
        			solrP.clearChoices();
        			solrP.addChoices("fromdisp");
        			solrP.addChoices("mtm");
        			perfP.initSummary();
        			updateSummaryPanel(solrP.getChoices(), SUMMARY_MODE.FREQUENT_M);
            		revalidate();
        		}
        	}
        });           
        submenu.add(menuItem); */       
        
        
        
        sta_hist_menuItem = new JMenuItem("Histogram");
        sta_hist_menuItem.setMnemonic(KeyEvent.VK_H);
        sta_hist_menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
            	{
	        		solrP.clearChoices();
	        		solrP.addChoices("fromdisp");
	        		solrP.addChoices("mtm");
	        		solrP.qPanel_O_histogram();
            		revalidate();
            	}
            }
        });  
        menu.add(sta_hist_menuItem);        
        
        
/*        //  statistic-histogram submenu
        submenu = new JMenu("Histogram");     
        menu.add(submenu);     

        menuItem = new JMenuItem("Emails");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
        		{
        			//perfP.initSummary();
        			solrP.clearChoices();
        			solrP.addChoices("fromdisp");
        			solrP.addChoices("mtm");
        			updateHistPanel(solrP.getChoices(), HIST_OPTION.EMAIL_O);
            		revalidate();
        		}
        	}
        });           
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("TRs");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
        		{
        			//perfP.initSummary();
        			solrP.clearChoices();
        			solrP.addChoices("conv");
        			solrP.addChoices("mtm");
        			updateHistPanel(solrP.getChoices(), HIST_OPTION.TR_O);
            		revalidate();
        		}
        	}
        });           
        submenu.add(menuItem);*/          
        
        sta_rank_menuItem = new JMenuItem("Rank");
        sta_rank_menuItem.setMnemonic(KeyEvent.VK_R);
        sta_rank_menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
            	{
	        		solrP.clearChoices();
	        		solrP.addChoices("conv");
	        		solrP.addChoices("mtm");
	        		solrP.qPanel_O_rank();
            		revalidate();
            	}
            }
        });  
        menu.add(sta_rank_menuItem);         
        
        
        
/*        //  statistic-Rank submenu
        submenu = new JMenu("Rank");     
        menu.add(submenu);     

        menuItem = new JMenuItem("Resolved");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
        		{
        			//perfP.initSummary();
        			solrP.clearChoices();
        			solrP.addChoices("conv");
        			solrP.addChoices("mtm");
        			updateRankPanel(solrP.getChoicesStr(), RANK_OPTION.RESOLVED_R);
            		revalidate();
        		}
        	}
        });           
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("Opened");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
        		{
        			//perfP.initSummary();
        			solrP.clearChoices();
        			solrP.addChoices("conv");
        			solrP.addChoices("mtm");
        			updateRankPanel(solrP.getChoicesStr(), RANK_OPTION.OPENED_R);
            		revalidate();
        		}
        	}
        });           
        submenu.add(menuItem);   */     
        
        
        
        sta_rate_menuItem = new JMenuItem("Ratio");
        sta_rate_menuItem.setMnemonic(KeyEvent.VK_R);
        sta_rate_menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
            	{
	        		solrP.clearChoices();
	        		solrP.addChoices("conv");
	        		solrP.addChoices("mtm");
	        		solrP.qPanel_O_ratio();
            		revalidate();
            	}
            }
        });  
        menu.add(sta_rate_menuItem);          
        
/*        //  statistic-Ratio submenu
        submenu = new JMenu("Ratio");     
        menu.add(submenu);     

        menuItem = new JMenuItem("Resolved");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
        		{
        			//perfP.initSummary();
        			solrP.clearChoices();
        			solrP.addChoices("conv");
        			solrP.addChoices("mtm");
        			updateRatioPanel(solrP.getChoicesStr(), RATIO_OPTION.RESOLVED_R);
            		revalidate();
        		}
        	}
        });           
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("Opened");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
        		{
        			//perfP.initSummary();
        			solrP.clearChoices();
        			solrP.addChoices("conv");
        			solrP.addChoices("mtm");
        			updateRatioPanel(solrP.getChoicesStr(), RATIO_OPTION.OPENED_R);
            		revalidate();
        		}
        	}
        });           
        submenu.add(menuItem);  */       
        
        // Build the second menu in the menu bar.
        menu = new JMenu("Recommendation");
        menu.setMnemonic(KeyEvent.VK_R);   // accelerator key
        menu.getAccessibleContext().setAccessibleDescription(
                "This menu lists the recommendation functionalities.");
        menuBar.add(menu);
        
        
        recDB_build_menuItem = new JMenuItem("Build TR DB");
        recDB_build_menuItem.setMnemonic(KeyEvent.VK_B);
        recDB_build_menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
            	{
	        		fm.buildTRDB();
	        		// build TB will start a thread, disable menu first
	        		disableMenus();

            		revalidate();
            	}
            }
        });  
        menu.add(recDB_build_menuItem);        
        
        
        recDB_query_menuItem = new JMenuItem("Similar TR");
        recDB_query_menuItem.setMnemonic(KeyEvent.VK_B);
        recDB_query_menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (state==STATUS.LOADDATA_S || state==STATUS.READY_S)
            	{
            		solrP.clearChoices();
	        		solrP.recommPanel_O();
            		revalidate();
            	}
            }
        });  
        menu.add(recDB_query_menuItem);           
        
        

        // Build the third menu in the menu bar.
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);   // accelerator key
        menu.getAccessibleContext().setAccessibleDescription(
                "This menu shows the software information.");
        menuBar.add(menu);
        
        menuItem = new JMenuItem("About");
        menuItem.setMnemonic(KeyEvent.VK_A);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
            	JOptionPane.showMessageDialog(null, "DecisionSupport version 1.0");
            }
        });
 
        setJMenuBar(menuBar);
        
        disableMenus();
    }
    
    
    public void disableMenus()
    {
    	query_menuItem.setEnabled(false);
    	sta_sum_menuItem.setEnabled(false);
    	sta_hist_menuItem.setEnabled(false);
    	sta_rank_menuItem.setEnabled(false);
    	sta_rate_menuItem.setEnabled(false);
    	recDB_build_menuItem.setEnabled(false);
    }
    
    public void enableMenus()
    {
    	query_menuItem.setEnabled(true);
    	sta_sum_menuItem.setEnabled(true);
    	sta_hist_menuItem.setEnabled(true);
    	sta_rank_menuItem.setEnabled(true);
    	sta_rate_menuItem.setEnabled(true);
    	recDB_build_menuItem.setEnabled(true);
    }
    
    
    
    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Item event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")"
                   + newline
                   + "    New state: "
                   + ((e.getStateChange() == ItemEvent.SELECTED) ?
                     "selected":"unselected");
        output.append(s + newline);
        output.setCaretPosition(output.getDocument().getLength());
    }    
    
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")";
        output.append(s + newline);
        output.setCaretPosition(output.getDocument().getLength());
    }    
    
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex+1);
    }    
    
    
    public void createContentPane() {
    	
    	final JPanel backgrd = new JPanel(new BorderLayout());
        backgrd.setBorder( BorderFactory.createLineBorder(Color.black) );    	    	
    	
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
 
        //Create a scrolled text area.
        output = new JTextArea(5, 30);
        output.setEditable(false);
        
        // scroll the textarea to the bottom
        DefaultCaret caret = (DefaultCaret)output.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        // add output to textAreaOutput
        System.setOut(new PrintStream(new textAreaOutput(output)));
        scrollPane = new JScrollPane(output);
        
        //Add the text area to the content pane.
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.setBorder( BorderFactory.createLineBorder(Color.black) );        
        
        
        // progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        contentPane.add(progressBar, BorderLayout.SOUTH);
        
        backgrd.add(contentPane, BorderLayout.SOUTH);
  	
         
        JPanel splitPane = new JPanel();
        backgrd.add( splitPane, BorderLayout.CENTER );        
 
        splitPane.setBorder(BorderFactory.createTitledBorder(""));
        splitPane.setLayout(new BoxLayout(splitPane, BoxLayout.X_AXIS));
        splitPane.add(solrP);
        splitPane.add(perfP);
         
        setContentPane(backgrd);         
    	
    }    

    
    
    // progress bar update
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        } 
    }
    
    
    public ArrayList<String> solrQueryFieldNames(String hostname, String port)
    {
    	String[] names;
    	fm.connectSolr(hostname, port);
    	fm.loadfromSolr(hostname, "*:*", "", 1); // retrieve all fields
    	return fm.getFDoc().getNames();
    }
    
    
    /*
     * This function will be triggered when there is a new solr query
     */
    public void updatePerformancePanel(DataModel model)
    {
		rResult ret=null;
		
		try 
		{
			//fm.clearIndex();
			//fm.loadLibrarytoSolr("Carseats");
			ret=fm.trainModel(model);
		} 
		catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
		
		perfP.loadMLData(ret, model);
				
    }
    
    
    public void updateSummaryPanel(ArrayList<String> choices, SUMMARY_MODE mode)
    {
    	if (choices.size() == 0)
    	{
    		return;
    	}
    	
    	rResult ret=null;
    	
		//fm.clearIndex();
		//fm.loadLibrarytoSolr("Carseats");
		try {
			ret=fm.statSummary(solrP.getSellectedUsers(), solrP.getQueryStr(), solrP.getQueryRows(), choices, solrP.getQueryStartDate(), solrP.getQueryEndDate(),  mode);
		} catch (RserveException | REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
		
		if (ret!=null)
		{
			perfP.loadSummaryData(ret, mode);
		}
    }
    
    
    public void loadHistPanel(rResult ret, HIST_OPTION option)
    {
		if (ret!=null)
		{
			perfP.loadHistData(ret, option);
			revalidate();
			enableMenus();
			solrP.buttonComplete();
		}    	
		else
		{
			disableMenus();
			solrP.buttonInProgress();
		}
    }
    
    
    public void updateHistPanel(ArrayList<String> choices, HIST_OPTION option)
    {
    	if (choices.size() == 0)
    	{
    		return;
    	}
    	
    	rResult ret=null;
    	
		//fm.clearIndex();
		//fm.loadLibrarytoSolr("Carseats");
		try {
			ret=fm.statHist(solrP.getQueryStr(), solrP.getQueryRows(), choices, solrP.getQueryStartDate(), solrP.getQueryEndDate(),  option);
		} catch (RserveException | REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
		
		loadHistPanel(ret, option);
    }
       
    
    
    
    
    public void updateRankPanel(String fields, OPT_CODE option)
    {
    	if (fields.length() == 0)
    	{
    		return;
    	}
    	
    	rResult ret=null;
    	
		//fm.clearIndex();
		//fm.loadLibrarytoSolr("Carseats");
		try {
			ret=fm.statRank(solrP.getQueryStr(), solrP.getQueryStartDate(), solrP.getQueryEndDate(),  fields, option);
		} catch (RserveException | REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
		
		if (ret!=null)
		{
			perfP.loadRankData(ret, option);
		}
    }    
    
    
    
    public void updateRecommPanel()
    {
    	rResult ret=null;
    	
    	ret = fm.queryTRDB(solrP.getRecommTRID());
    	
    	loadRecommPanel(ret);
    }
    
    
    public void loadRatioPanel(rResult ret, RATIO_OPTION option)
    {
		if (ret!=null)
		{
			perfP.loadRatioData(ret, option);
			revalidate();
			enableMenus();
			solrP.buttonComplete();
		} 	
		else
		{
			disableMenus();
			solrP.buttonInProgress();
		}
    }    
    
    
    
    public void loadRecommPanel(rResult ret)
    {
		if (ret!=null)
		{
			perfP.loadRankData(ret, OPT_CODE.RECOMM_RDG);
			revalidate();
			enableMenus();
			solrP.buttonComplete();
		} 	
		else
		{
			disableMenus();
			solrP.buttonInProgress();
		}
    }     
    
    
    
    public void updateRatioPanel(String fields, RATIO_OPTION option)
    {
    	if (fields.length() == 0)
    	{
    		return;
    	}
    	
    	rResult ret=null;
    	
		//fm.clearIndex();
		//fm.loadLibrarytoSolr("Carseats");
		try {
			ret=fm.statRatio(solrP.getQueryStr(), solrP.getQueryStartDate(), solrP.getQueryEndDate(), option);
		} catch (RserveException | REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
		
		loadRatioPanel(ret, option);
		
    }        
    
    
    
    public static GUI getInstance()
    {
    	if (gui==null)
    	{
    		gui=new GUI();
    	}
    	return gui;
    }
    
    
    private static void createAndShowGUI() {
        //Create and set up the content pane.
        GUI gui = GUI.getInstance();
        gui.createMenuBar();
        gui.createContentPane();
    }    
    
     
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
    	System.out.println("let's start!");
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

	
}
