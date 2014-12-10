package DecisionSupport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXDatePicker;
import DecisionSupport.perfPanel.HIST_OPTION;
import DecisionSupport.perfPanel.RATIO_OPTION;
import DecisionSupport.perfPanel.SUMMARY_MODE;
import DecisionSupport.rConnector.DataModel;
import DecisionSupport.solrConnector.OPT_CODE;

public class solrPanel extends JPanel implements ActionListener, ItemListener, ListSelectionListener {
	
	private String[] datasetStr={"Carseats", "172.19.52.130:5621 (Bcui-EngHelp)"};
	private String[] modelsStr={"SVM", "LogisticRegression", "LinearDiscriminateAnalysis", "QuadraticDiscriminateAnalysis", "DecisionTree", "RandomForest", "K-NearestNeighbor"};
	private ArrayList<String> choices=new ArrayList<String>();
	private ArrayList<String> names=new ArrayList<String>();
	private String[] columnNames;
	private ArrayList<JCheckBox> buttons;
	
	protected static final String basicLabelText = "basic q", queryLabelText = "q", rowsLabelText = "rows";
	protected static final String dbIPLabelText = "IP", dbPortLabelText = "Port";
	
	private int queryRows = 10;
	private String queryStr = "*:*";
	
	private String dataset="";  // current selected dataset
	private DataModel trainModel;
	
	private JLabel captionLabel, queryLabel, recommLabel;
	private JComboBox datasetsList, modelsList;
	private JPanel dataSelPanel, checkBoxPanel, trainPanel, queryPanel, recommPanel;
	private JScrollPane scrollPane=null;
	private JTextField basicTextField, queryTextField, rowsTextField, recommTextField;
	private JTextField dbIPTextField, dbPortTextField;
	private JXDatePicker startDate, endDate;
	private JButton generalButton, recommButton; 
	private JRadioButton opt1Button, opt2Button;
	
    private SUMMARY_MODE s_mode=SUMMARY_MODE.ABSOLUTE_M;
    private HIST_OPTION h_mode=HIST_OPTION.EMAIL_O;
    private OPT_CODE rank_mode=OPT_CODE.RESOLVED_RDG;
    private RATIO_OPTION rate_mode=RATIO_OPTION.RESOLVED_R;
    
	private Date queryStartDate=new Date(), queryEndDate=new Date();
	boolean queryPanel_initialized=false, recommPanel_initialized=false;
	
	JButton connectButton=null;
	
	private JList<String> list;
	private ListSelectionModel listSelectionModel;
	private String[] interestedUsers;
	private ArrayList<String> selectedUsers = new ArrayList<String>();
	private final int MAXBOXPERROW=5;
	
	private final int SPAD=20;
	
	
	solrPanel()
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
                "Data Selection",
                TitledBorder.CENTER,
                TitledBorder.BELOW_TOP);
        border.setTitleColor(Color.black);
        setBorder(border);        
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// dataset selection box panel
		dataSelPanel = new JPanel();
        size = new Dimension(screenWidth/2-SPAD, 30);
        dataSelPanel.setPreferredSize(size);
        dataSelPanel.setMaximumSize(size);
        dataSelPanel.setMinimumSize(size);       
        add(dataSelPanel);
        
        // fields panel
        checkBoxPanel = new JPanel();
        size = new Dimension(screenWidth/2-SPAD, 160);
        checkBoxPanel.setPreferredSize(size);
        checkBoxPanel.setMaximumSize(size);
        checkBoxPanel.setMinimumSize(size);
        add(checkBoxPanel);
        checkBoxPanel.setVisible(false);
        
        // solr query panel
        queryPanel = new JPanel();
        size = new Dimension(screenWidth/2-SPAD, 200);
        queryPanel.setPreferredSize(size);
        queryPanel.setMaximumSize(size);
        queryPanel.setMinimumSize(size);
        add(queryPanel);        
        queryPanel.setVisible(false);
        
        // recomm panel
        recommPanel = new JPanel();
        size = new Dimension(screenWidth/2-SPAD, 200);
        recommPanel.setPreferredSize(size);
        recommPanel.setMaximumSize(size);
        recommPanel.setMinimumSize(size);
        add(recommPanel);        
        recommPanel.setVisible(false);        
        
        // model selection panel
        trainPanel = new JPanel();
        size = new Dimension(screenWidth/2-SPAD, 40);
        trainPanel.setPreferredSize(size);
        trainPanel.setMaximumSize(size);
        trainPanel.setMinimumSize(size);
        add(trainPanel);
        trainPanel.setVisible(false);
		
        interestedUsers=featureMap.getInstance().interestedUsers();
	}
	
	public void connectDB()
	{
		dataSelPanel.setLayout(new BoxLayout(dataSelPanel,BoxLayout.LINE_AXIS));
		// add label,checkBox into checkBoxPanel
		captionLabel = new JLabel("Select database: ");
		
/*		datasetsList = new JComboBox(datasetStr);
        datasetsList.setEditable(true);
        datasetsList.addActionListener(this);	
        Dimension size = new Dimension(200, 40);
        datasetsList.setPreferredSize(size);
        datasetsList.setMaximumSize(size);
        datasetsList.setMinimumSize(size);
        
        dataSelPanel.add(captionLabel);
        dataSelPanel.add(datasetsList); */

		dbIPTextField = new JTextField();
		dbIPTextField.setPreferredSize(new Dimension(50, 10));
		dbIPTextField.setActionCommand(dbIPLabelText);
		dbIPTextField.addActionListener(this);
		dbIPTextField.setText("localhost");
        
        dbPortTextField = new JTextField();
        dbPortTextField.setPreferredSize(new Dimension(30, 10));
        dbPortTextField.setActionCommand(dbPortLabelText);
        dbPortTextField.addActionListener(this);
        dbPortTextField.setText("5621");
		
        JLabel dbIPTextFieldLabel = new JLabel(dbIPLabelText + ": ");
        dbIPTextFieldLabel.setLabelFor(dbIPTextField);
        dataSelPanel.add(dbIPTextFieldLabel);
        dataSelPanel.add(dbIPTextField);
        
        JLabel dbPortTextFieldLabel = new JLabel(dbPortLabelText + ": ");
        dbPortTextFieldLabel.setLabelFor(dbPortTextField);
        dataSelPanel.add(dbPortTextFieldLabel);
        dataSelPanel.add(dbPortTextField);        
		
        
        connectButton = new JButton("Connect");
        connectButton.setVerticalTextPosition(AbstractButton.CENTER);
        connectButton.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        connectButton.setMnemonic(KeyEvent.VK_S);
        connectButton.setActionCommand("connect");
        connectButton.addActionListener(this);
        dataSelPanel.add(connectButton); 	
        
        //comboBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
              
        revalidate();
	}
	
	
	// initialize qPanel and its permanent components
	protected void qPanel_init()
	{
    	queryPanel.removeAll();
    	
		GroupLayout layout = new GroupLayout(queryPanel);
		queryPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		queryLabel = new JLabel("Query Tab");
		
		Dimension size = new Dimension(220,25);
    	basicTextField = new JTextField(30);
    	basicTextField.setActionCommand(basicLabelText);
    	basicTextField.addActionListener(this);
    	basicTextField.setText("*.*");
    	basicTextField.setPreferredSize(size);
    	basicTextField.setMaximumSize(size);
    	basicTextField.setMinimumSize(size);
		
		
		size = new Dimension(220,25);
    	queryTextField = new JTextField(30);
        queryTextField.setActionCommand(queryLabelText);
        queryTextField.addActionListener(this);
        queryTextField.setText("*:*");
        queryTextField.setPreferredSize(size);
        queryTextField.setMaximumSize(size);
        queryTextField.setMinimumSize(size);
        
        size = new Dimension(100,25);
        rowsTextField = new JTextField(10);
        rowsTextField.setActionCommand(rowsLabelText);
        rowsTextField.addActionListener(this);
        rowsTextField.setText("10");
        rowsTextField.setPreferredSize(size);
        rowsTextField.setMaximumSize(size);
        rowsTextField.setMinimumSize(size);
        
        size = new Dimension(250,150);
        interestedUsers=featureMap.getInstance().interestedUsers();
        list = new JList<String>(interestedUsers);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listSelectionModel = list.getSelectionModel();
        listSelectionModel.addListSelectionListener(this);
        JScrollPane listPane = new JScrollPane(list);
//        list.addListSelectionListener(this);
        queryPanel.add(listPane);
        
        JLabel basicTextFieldLabel = new JLabel(basicLabelText + ":\n");
        basicTextFieldLabel.setLabelFor(basicTextField);
        queryPanel.add(basicTextFieldLabel);
        queryPanel.add(basicTextField);        
        
        JLabel queryTextFieldLabel = new JLabel(queryLabelText + ":\n");
        queryTextFieldLabel.setLabelFor(queryTextField);
        queryPanel.add(queryTextFieldLabel);
        queryPanel.add(queryTextField);
        
        JLabel rowsTextFieldLabel = new JLabel(rowsLabelText + ":\n");
        rowsTextFieldLabel.setLabelFor(rowsTextField);
        queryPanel.add(rowsTextFieldLabel);
        queryPanel.add(rowsTextField);
        
        startDate = new JXDatePicker();
        endDate = new JXDatePicker();
        startDate.setDate(featureMap.getInstance().startDate());
        startDate.setFormats(new SimpleDateFormat("dd.MM.yyyy"));
        startDate.addActionListener(this);
        queryStartDate = startDate.getDate();
        
        endDate.setDate(Calendar.getInstance().getTime());
        endDate.setFormats(new SimpleDateFormat("dd.MM.yyyy"));
        endDate.addActionListener(this);
        queryEndDate = endDate.getDate();
        
        JLabel startDateLabel = new JLabel("startDate: \n");
        queryPanel.add(startDateLabel);
        queryPanel.add(startDate);
        JLabel endDateLabel = new JLabel("endDate:   \n");
        queryPanel.add(endDateLabel);
        queryPanel.add(endDate);		
        
        generalButton = new JButton();
        generalButton.setVerticalTextPosition(AbstractButton.CENTER);
        generalButton.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        generalButton.addActionListener(this);
        queryPanel.add(generalButton); 	               
        
        
        opt1Button = new JRadioButton();
        opt1Button.setSelected(true);
        opt1Button.addActionListener(this);
    	
        opt2Button = new JRadioButton();
        opt2Button.setMnemonic(KeyEvent.VK_F);
        opt2Button.setSelected(false);
        opt2Button.setActionCommand("Frequency");
        opt2Button.addActionListener(this);
        
        ButtonGroup group = new ButtonGroup();
        group.add(opt1Button);
        group.add(opt2Button);
        
        queryPanel.add(opt1Button);
        queryPanel.add(opt2Button);
        
        layout.setHorizontalGroup(
        		   layout.createSequentialGroup()
        		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		      		.addComponent(queryLabel)
        		      		.addGroup(layout.createSequentialGroup()
        		      			.addGroup(layout.createParallelGroup()
        		      					.addComponent(basicTextFieldLabel)
        		      					.addComponent(queryTextFieldLabel)
        		      					.addComponent(rowsTextFieldLabel)
        		      					)
        		      			.addGroup(layout.createParallelGroup()	
        		      					.addComponent(basicTextField)
        		      					.addComponent(queryTextField)
        		      					.addComponent(rowsTextField)
        		      					.addComponent(opt1Button)
        		      					.addComponent(opt2Button)
        		      					.addComponent(generalButton)        		      					
        		      					)
        		      			.addGroup(layout.createParallelGroup()
        		      					.addComponent(startDateLabel)
        		      					.addComponent(endDateLabel)
        		      					)			
        		      			.addGroup(layout.createParallelGroup()
        		      					.addComponent(startDate)
        		      					.addComponent(endDate)
        		      					)
        		      			.addComponent(listPane)
        		      			)
        		      		)
        		);
        
        layout.setVerticalGroup(
        		layout.createSequentialGroup()
        			.addComponent(queryLabel)
        			.addGroup(layout.createParallelGroup()
        				.addGroup(layout.createSequentialGroup()
        						.addComponent(basicTextFieldLabel)
        						.addComponent(queryTextFieldLabel)
        						.addComponent(rowsTextFieldLabel)
        						)
        				.addGroup(layout.createSequentialGroup()
        						.addComponent(basicTextField)
        						.addComponent(queryTextField)
        						.addComponent(rowsTextField)
        						.addComponent(opt1Button)
        						.addComponent(opt2Button)
        						.addComponent(generalButton)        						
        						)
        				.addGroup(layout.createSequentialGroup()
        						.addComponent(startDateLabel)
        						.addComponent(endDateLabel)
        						)
        				.addGroup(layout.createSequentialGroup()
        						.addComponent(startDate)
        						.addComponent(endDate)
        						)
        				.addComponent(listPane)
        		     )
        		      	//.addComponent(listPane))
        		);
        
        queryPanel_initialized = true;
	}
	
	
	public void qPanel_O_query()
	{
		if (queryPanel_initialized==false)
		{
			qPanel_init();
		}
		
		//init textfield
		startDate.setDate(featureMap.getInstance().startDate());
        endDate.setDate(Calendar.getInstance().getTime());		
		
		queryTextField.setText("*:*");
		rowsTextField.setText("10");
		rowsTextField.setEnabled(true);
		
		list.clearSelection();
		list.setEnabled(true);
		
        if (scrollPane!=null)
        {
        	scrollPane.setVisible(false);
        }		
        opt1Button.setVisible(false);
        opt2Button.setVisible(false);
		
		queryLabel.setText("Query:  input query parameters and press the \"Search\" Button");
		
		generalButton.setText("Search");
		generalButton.setMnemonic(KeyEvent.VK_S);
        generalButton.setActionCommand("search");
        
        
        queryPanel.setVisible(true);
        
        revalidate();
	}
	
	
	public void qPanel_O_summary()
	{
		if (queryPanel_initialized==false)
		{
			qPanel_init();
		}		

		//init textfield
		startDate.setDate(featureMap.getInstance().startDate());
        endDate.setDate(Calendar.getInstance().getTime());		
		
		queryTextField.setText("*:*");
		rowsTextField.setText("10");
		rowsTextField.setEnabled(false);
		
		list.clearSelection();
		list.setEnabled(true);
		
        if (scrollPane!=null)
        {
        	scrollPane.setVisible(false);
        }			
        opt1Button.setText("Absolute");
        opt1Button.setVisible(true);
        opt1Button.setSelected(true);
        opt1Button.setMnemonic(KeyEvent.VK_A);
        opt1Button.setActionCommand("Absolute");
        
        opt2Button.setText("Frequency");
        opt2Button.setVisible(true);
        opt2Button.setSelected(false);
        opt2Button.setMnemonic(KeyEvent.VK_F);
        opt2Button.setActionCommand("Frequency");        
		
        s_mode = SUMMARY_MODE.ABSOLUTE_M;
		queryLabel.setText("Summary:  input parameters then press the \"Summary\" Button");
		
		generalButton.setText("Summary");
		generalButton.setMnemonic(KeyEvent.VK_S);
        generalButton.setActionCommand("Summary");
        
        
        queryPanel.setVisible(true);
        
        revalidate();		
	}
	
	
	public void qPanel_O_histogram()
	{
		if (queryPanel_initialized==false)
		{
			qPanel_init();
		}		

		//init textfield
		startDate.setDate(featureMap.getInstance().startDate());
        endDate.setDate(Calendar.getInstance().getTime());
        
		queryTextField.setText("*:*");
		rowsTextField.setText("10");
		rowsTextField.setEnabled(false);
		
		list.clearSelection();
        list.setEnabled(true);
		
		if (scrollPane!=null)
        {
        	scrollPane.setVisible(false);
        }			
        opt1Button.setText("Emails");
        opt1Button.setVisible(true);
        opt1Button.setSelected(true);
        opt1Button.setMnemonic(KeyEvent.VK_E);
        opt1Button.setActionCommand("Emails");
        
        opt2Button.setText("TRs");
        opt2Button.setVisible(true);
        opt2Button.setSelected(false);
        opt2Button.setMnemonic(KeyEvent.VK_T);
        opt2Button.setActionCommand("TRs");        
		
        h_mode = HIST_OPTION.EMAIL_O;
		queryLabel.setText("Histogram:  input parameters then press the \"Histogram\" Button");
		
		generalButton.setText("Histogram");
		generalButton.setMnemonic(KeyEvent.VK_H);
        generalButton.setActionCommand("Histogram");
        
        
        queryPanel.setVisible(true);
        
        revalidate();		
	}	
	
	
	public void qPanel_O_rank()
	{
		if (queryPanel_initialized==false)
		{
			qPanel_init();
		}		

		//init textfield
		startDate.setDate(featureMap.getInstance().startDate());
        endDate.setDate(Calendar.getInstance().getTime());
        
		list.clearSelection();
		list.setEnabled(false);
		
		queryTextField.setText("*:*");
		rowsTextField.setText("10");
		rowsTextField.setEnabled(false);
		
		if (scrollPane!=null)
        {
        	scrollPane.setVisible(false);
        }			
        opt1Button.setText("Resolved");
        opt1Button.setVisible(true);
        opt1Button.setSelected(true);
        opt1Button.setMnemonic(KeyEvent.VK_R);
        opt1Button.setActionCommand("RANK_Resolved");
        
        opt2Button.setText("Opened");
        opt2Button.setVisible(true);
        opt2Button.setSelected(false);
        opt2Button.setMnemonic(KeyEvent.VK_O);
        opt2Button.setActionCommand("RANK_Opened");        
		
        rank_mode = OPT_CODE.RESOLVED_RDG;
		queryLabel.setText("Ranking:  input parameters then press the \"Rank\" Button");
		
		generalButton.setText("Rank");
		generalButton.setMnemonic(KeyEvent.VK_R);
        generalButton.setActionCommand("Rank");
        
        
        queryPanel.setVisible(true);
        
        revalidate();		
	}		
	
	
	public void qPanel_O_ratio()
	{
		if (queryPanel_initialized==false)
		{
			qPanel_init();
		}		

		//init textfield
		startDate.setDate(featureMap.getInstance().startDate());
        endDate.setDate(Calendar.getInstance().getTime());
        
		queryTextField.setText("*:*");
		rowsTextField.setText("10");
		rowsTextField.setEnabled(false);
		
		list.clearSelection();
        list.setEnabled(false);
		
		if (scrollPane!=null)
        {
        	scrollPane.setVisible(false);
        }			
        opt1Button.setText("Resolved");
        opt1Button.setVisible(true);
        opt1Button.setSelected(true);
        opt1Button.setMnemonic(KeyEvent.VK_R);
        opt1Button.setActionCommand("RATE_Resolved");
        
        opt2Button.setText("Opened");
        opt2Button.setVisible(true);
        opt2Button.setSelected(false);
        opt2Button.setMnemonic(KeyEvent.VK_O);
        opt2Button.setActionCommand("RATE_Opened");        
		
        rate_mode = RATIO_OPTION.RESOLVED_R;
		queryLabel.setText("Ratio:  input parameters then press the \"Rate\" Button");
		
		generalButton.setText("Rate");
		generalButton.setMnemonic(KeyEvent.VK_R);
        generalButton.setActionCommand("Rate");
        
        
        queryPanel.setVisible(true);
        
        revalidate();		
	}		
	
	private void recommPanel_init()
	{
		recommLabel = new JLabel("TR ID: \n");
        recommPanel.add(recommLabel);
        
        Dimension size = new Dimension(220,25);
    	recommTextField = new JTextField(30);
    	recommTextField.addActionListener(this);
    	recommTextField.setText("");
    	recommTextField.setPreferredSize(size);
    	recommTextField.setMaximumSize(size);
    	recommTextField.setMinimumSize(size);
        recommPanel.add(recommTextField);
    	
    	recommButton = new JButton();
    	recommButton.setVerticalTextPosition(AbstractButton.CENTER);
    	recommButton.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
    	recommButton.addActionListener(this);
	
    	recommButton.setText("Recomm");
    	recommButton.setMnemonic(KeyEvent.VK_R);
    	recommButton.setActionCommand("Recomm");
        
        recommPanel.add(recommButton); 	 
        
        recommPanel_initialized = true;
	}
	
	
	public void recommPanel_O()
	{
		
		if (recommPanel_initialized==false)
		{
			recommPanel_init();
		}		
		
		queryPanel.setVisible(false);
		recommPanel.setVisible(true);
		
        revalidate();		
	}
	
	
	public void tPanel_O_train()
	{
    	// redraw train panel
    	trainPanel.removeAll();
        
        JButton trainButton = new JButton("Train");
        trainButton.setVerticalTextPosition(AbstractButton.CENTER);
        trainButton.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        trainButton.setMnemonic(KeyEvent.VK_T);
        trainButton.setActionCommand("train");
        trainButton.addActionListener(this);
        trainPanel.add(trainButton);        
        
		modelsList = new JComboBox(modelsStr);
        modelsList.setEditable(true);
        modelsList.addActionListener(this);	
        Dimension size = new Dimension(200, 40);
        modelsList.setPreferredSize(size);
        modelsList.setMaximumSize(size);
        modelsList.setMinimumSize(size);       
        trainPanel.add(modelsList);
        
        trainModel = DataModel.SVM_T; //set the default value	
        trainPanel.setVisible(true);
	}
	
	
    protected void updateFields(String dataset) 
    {
    	// retrieve IP and port
    	String text, hostname="localhost", port="5621";
    	text=dbIPTextField.getText();
    	if (!text.equals(""))
    	{
    		hostname = text;
    	}
    	text=dbPortTextField.getText();
    	if (!text.equals(""))
    	{
    		port = text;
    	}
    	
        //select fields name from dataset
    	names = new ArrayList<String>();
    	names=GUI.getInstance().solrQueryFieldNames(hostname, port);
    	
    	checkBoxPanel.removeAll();
    	checkBoxPanel.setVisible(false);
    	
    	queryPanel.removeAll();
    	queryPanel.setVisible(false);
    	
    	if (names.size() > 0)
    	{
        	columnNames = new String[names.size()];
        	//checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel,BoxLayout.LINE_AXIS));
        	// add checkbox according to names
        	buttons=new ArrayList<JCheckBox>();
        	for (int j=0; j<names.size(); j++)
    		{
        		JCheckBox button = new JCheckBox(names.get(j));
                button.setSelected(false);
                button.addItemListener(this);
        		buttons.add(j, button);
        		checkBoxPanel.add(button);
        		columnNames[j]=names.get(j);
    		}
        	
        	checkBoxPanel.setVisible(true);
        	
        	connectButton.setEnabled(false);
           
        	GUI.getInstance().enableMenus();
    	}
    	
    }	
	
    
    public String getQueryStr()
    {
		String textQ=queryTextField.getText(), textB=basicTextField.getText();
		if (textQ.equals(""))
		{
			textQ="*.*";
		}
		
		if (textB.equals(""))
		{
			textB="*.*";
		}

    	return "(" + textB + ") AND (" + textQ + ")";
    }
    
    
    public String getRecommTRID()
    {
    	return recommTextField.getText();
    }
    
    
    public int getQueryRows()
    {
    	return queryRows;
    }
    
    public Date getQueryStartDate()
    {
    	return queryStartDate;
    }
    
    public Date getQueryEndDate()
    {
    	return queryEndDate;
    }
    
    
    public void search()
    {
    	String text;
    	
    	queryStr = getQueryStr();
    	
    	text=rowsTextField.getText();
    	if (!text.equals(""))
    	{
    		queryRows = Integer.parseInt(text);
    	}
    	else
    	{
    		queryRows = 10; // default rows number
    	}
    	
    	// only return selected fields according to choices
    	
    	String fields="";
    	for (int i=0; i<choices.size(); i++)
    	{
    		fields = fields + " " + choices.get(i);
    	}
    	
    	featureMap.getInstance().loadfromSolr(dataset, queryStr, fields, queryRows);
    	fDocument fdoc = featureMap.getInstance().getFDoc();
    	// retrieve data to table
		if (scrollPane != null)
		{
			remove(scrollPane);
		}        	
    	
    	if (fdoc.getRows()!=0)
    	{
    		final JTable table = new JTable((Object[][])fdoc.getData(), fdoc.getNames().toArray(new String[fdoc.getNames().size()]));
    		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    		table.setFillsViewportHeight(true);
        
    		scrollPane = new JScrollPane(table);
    		add(scrollPane);
    	}    	
    }
    
	
    public void actionPerformed(ActionEvent e) {
    	
    	if ("comboBoxChanged".equals(e.getActionCommand()))
    	{
    		JComboBox cb = (JComboBox)e.getSource();
    		String selected=(String)cb.getSelectedItem();
    		
    		// if the first combox is changed, one dataset is selected
/*    		if (Arrays.asList(datasetStr).contains(selected))
    		{
        		if (dataset.equals(selected)==false)
        		{
        			dataset = (String)cb.getSelectedItem();
        			updateFields(dataset);
        		}    			
    		}*/
    		
    		// if the second combox is changed, one of the ML algorithm is selected
    		if (Arrays.asList(modelsStr).contains(selected))
    		{
    			if (selected.equals("SVM")==true)
    			{
    				trainModel = DataModel.SVM_T;
    			}
    			else if(selected.equals("LogisticRegression")==true)
    			{
    				trainModel = DataModel.LOGISTIC_T;
    			}
    			else if(selected.equals("LinearDiscriminateAnalysis")==true)
    			{
    				trainModel = DataModel.LDA_T;
    			}
    			else if (selected.equals("QuadraticDiscriminateAnalysis")==true)
    			{
    				trainModel = DataModel.QDA_T;
    			}
    			else if (selected.equals("DecisionTree")==true)
    			{
    				trainModel = DataModel.DTREE_T;
    			}
    			else if (selected.equals("RandomForest")==true)
    			{
    				trainModel = DataModel.RFOREST_T;
    			}
    			else if (selected.equals("K-NearestNeighbor")==true)
    			{
    				trainModel = DataModel.KNN_T;
    			}
    		}
    		

    	}
        
    	// change of ip or port
    	if (dbIPLabelText.equals(e.getActionCommand()) || dbPortLabelText.equals(e.getActionCommand()))
    	{
    		connectButton.setEnabled(true);
    	}
    	
    	// change of query text field
    	if (queryLabelText.equals(e.getActionCommand()) || basicLabelText.equals(e.getActionCommand()))
    	{

    	}
    	
    	// search button clicked
        if ("connect".equals(e.getActionCommand())) {
			updateFields(dataset);
        }        	
        
    	// search button clicked
        if ("search".equals(e.getActionCommand())) {
            search();
        }        
        
        // train button clicked
        if ("train".equals(e.getActionCommand())) {
        	
        	GUI.getInstance().updatePerformancePanel(trainModel);
        }   
        
        
        if ("Absolute".equals(e.getActionCommand()))
        {
        	s_mode = SUMMARY_MODE.ABSOLUTE_M;
        }
        else if ("Frequency".equals(e.getActionCommand()))
        {
        	s_mode = SUMMARY_MODE.FREQUENT_M;
        }
        
        // summary button clicked , the button is obsoleted
        if ("Summary".equals(e.getActionCommand())) {
        	GUI.getInstance().updateSummaryPanel(choices, s_mode);
        }        
        
        if ("Emails".equals(e.getActionCommand()))
        {
        	h_mode = HIST_OPTION.EMAIL_O;
        }
        else if ("TRs".equals(e.getActionCommand()))
        {
        	h_mode = HIST_OPTION.TR_O;
        }
        
     // hist button clicked , the button is obsoleted
        if ("Histogram".equals(e.getActionCommand())) {
        	GUI.getInstance().updateHistPanel(choices, h_mode);
        }
        
        
        if ("RANK_Resolved".equals(e.getActionCommand()))
        {
        	rank_mode = OPT_CODE.RESOLVED_RDG;
        }
        else if ("RANK_Opened".equals(e.getActionCommand()))
        {
        	rank_mode = OPT_CODE.OPENED_RDG;
        }
        
     // rank button clicked , the button is obsoleted
        if ("Rank".equals(e.getActionCommand())) {
        	GUI.getInstance().updateRankPanel(getChoicesStr(), rank_mode);
        }        
        
          
        if ("RATE_Resolved".equals(e.getActionCommand()))
        {
        	rate_mode = RATIO_OPTION.RESOLVED_R;
        }
        else if ("RATE_Opened".equals(e.getActionCommand()))
        {
        	rate_mode = RATIO_OPTION.OPENED_R;
        }
        
     // rate button clicked , the button is obsoleted
        if ("Rate".equals(e.getActionCommand())) {
        	GUI.getInstance().updateRatioPanel(getChoicesStr(), rate_mode);
        }           
        
        
        if ("Recomm".equals(e.getActionCommand())) {
        	GUI.getInstance().updateRecommPanel();
        }  
        
        
        // dataPicker selected
        if ("datePickerCommit".equals(e.getActionCommand()))
        {
        	if (e.getSource() == startDate)
        	{
        		queryStartDate = startDate.getDate();
        	}
        	else if (e.getSource() == endDate)
        	{
        		queryEndDate = endDate.getDate();
        	}
        }
        
        
        revalidate();
    }
 
     
    public void buttonInProgress()
    {
    	if (generalButton!=null)
    	{
    		generalButton.setEnabled(false);
    	}
    	if (recommButton!=null)
    	{
    		recommButton.setEnabled(false);
    	}
    }
    
    public void buttonComplete()
    {
    	if (generalButton!=null)
    	{
    		generalButton.setEnabled(true);
    	}
    	if (recommButton!=null)
    	{
    		recommButton.setEnabled(true);
    	}
    }
    
    
    public ArrayList<String> getChoices()
    {
    	return choices;
    }
    
    
    public String getChoicesStr()
    {
    	String fields="";
    	for (int i=0; i<choices.size(); i++)
    	{
    		fields = fields + " " + choices.get(i);
    	}
    	
    	return fields;
    }
    
    public String getDatasetName()
    {
    	return dataset;
    }
    
    
    public void clearChoices()
    {
    	while (choices.size()>0)
    	{
    		buttons.get(names.indexOf(choices.get(choices.size()-1))).setSelected(false);
    	}
    }
    
    public void addChoices(String choice)
    {
    	if (!choices.contains(choice))
    	{
    		buttons.get(names.indexOf(choice)).setSelected(true);
    	}
    }
    
    
    public void itemStateChanged(ItemEvent e) 
    {

        Object source = e.getItemSelectable();
 
        for (int i=0; i<names.size(); i++)
        {
        	if (source == buttons.get(i))
        	{
        		
        		if (e.getStateChange() == ItemEvent.DESELECTED) {
        			if (choices.contains(names.get(i)))
        			{
        				choices.remove(names.get(i));
        			}
                }
        		else // selected
        		{
        			if (choices.contains(i) == false)
            		{
            			choices.add(names.get(i));
            		}        			
        		}
        	}
        }
        
    }

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		String query="";
		
		selectedUsers.clear();
		
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		int minIndex = lsm.getMinSelectionIndex();
        int maxIndex = lsm.getMaxSelectionIndex();
        for (int i = minIndex; i <= maxIndex; i++) 
        {
            if (lsm.isSelectedIndex(i)) 
            {
    			if (query.length()!=0)
    			{
    				query = query + " OR ";
    			}
    			
    			query = query + "fromdisp:\"" + interestedUsers[i] + "\""; 
    			
    			selectedUsers.add(interestedUsers[i]);
            }
        }
        
        if (query.length()==0)
        {
        	queryTextField.setText("*:*");
        }
        else
        {
        	queryTextField.setText("(" + query + ")");
        }
        
		
	}    
	
	
	public ArrayList<String> getSellectedUsers()
	{
		return selectedUsers;
	}
    
	

}
