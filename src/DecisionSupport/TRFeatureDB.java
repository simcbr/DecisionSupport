package DecisionSupport;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import DecisionSupport.solrConnector.OPT_CODE;

/**
 * 
 * @author bcui
 * this class is responsible for build features for each TR 
 *
 */
public class TRFeatureDB {
	
	
	private solrConnector solr=null;
	private HashMap<String,String> TRMap;
	private Date lastUpdated=null;
	ArrayList<String> fieldNames=new ArrayList<String>();
	ArrayList<String> repFieldNames=new ArrayList<String>();
	ArrayList<String> dictFieldNames=new ArrayList<String>();
	private final int REPNUM=20;
	
	TRFeatureDB(solrConnector solr)
	{
		this.solr = solr;
	}

	
	private void TRDBNames()
	{
		fieldNames.clear();
		
		// add field names
		fieldNames.add("uniqueitemid");
		fieldNames.add("contentid");
		fieldNames.add("TRID");
		fieldNames.add("frequency");
		fieldNames.add("bodyWords");
		fieldNames.add("mandatoryWords");
		
		repFieldNames.clear();
		repFieldNames.add("uniqueitemid");
		repFieldNames.add("contentid");
		repFieldNames.add("rep-TRID");
		repFieldNames.add("repWords");
		
		
		dictFieldNames.clear();
		dictFieldNames.add("uniqueitemid");
		dictFieldNames.add("contentid");
		dictFieldNames.add("dictWord");
		dictFieldNames.add("TRList");
		
	}
	
	
	// read the last updated date
	private void initialize() throws IOException
	{
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream("TRDB.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Open TRDB.txt failed");
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		String strLine;

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
			try {
				lastUpdated = sdf.parse(strLine);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Parse lastUpdated date <" + strLine.split(":")[1] + "> failed.");
				lastUpdated = null;
			}
		}

		//Close the input stream
		br.close();
		
		
		TRDBNames();
	}
	
	
	private void outputLastUpdated()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String s = sdf.format(lastUpdated);

		byte data[] = s.getBytes();
		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(Paths.get("TRDB.txt")))) {
		    out.write(data, 0, data.length);
		} catch (IOException x) {
		    System.err.println(x);
		}

	}
	
	
	/*
	 * The information stored includes: normal words (select depends on TF.IDF) and other mandatory feature words
	 */
	public void storeDB(String contentid, String docName, HashMap<String, Integer>map, String mandatory) throws SolrServerException, IOException
	{
		
		HashMap<Integer, String> bodyStrs = new HashMap<Integer, String>();
		for (Map.Entry<String, Integer> entry : map.entrySet())
		{
			int freq = entry.getValue();
			
			if (bodyStrs.containsKey(freq))
			{
				bodyStrs.put(freq, bodyStrs.get(freq) + " " + entry.getKey());
			}
			else
			{
				bodyStrs.put(freq, entry.getKey());
			}
		}
		
		
		ArrayList<Object> data = new ArrayList<Object>();
		for (Map.Entry<Integer, String> entry : bodyStrs.entrySet())
		{
			data.clear();
			
			String uniqueid ="recom-"+docName+"-"+Integer.toString(entry.getKey()); 
			data.add(Integer.toString(uniqueid.hashCode()));
			data.add(contentid);
			data.add(docName);
			data.add(entry.getKey());
			data.add(entry.getValue());
			
			if (entry.getKey() == 1)
			{
				data.add(mandatory);
			}
			
			solr.addDocument(data, fieldNames);
			
		}
		
		
		solr.commit();
	}
	
	
	
	
	protected void updateDict(HashMap<String, String> dict, Set<String> TRs) throws SolrServerException, IOException
	{
		ArrayList<Object> data = new ArrayList<Object>();
		int i=0;
		for (Map.Entry<String, String> entry:dict.entrySet())
		{
			System.out.println("updateDict: " + Integer.toString(i) + " " + Integer.toString(dict.size()));
			i++;
			
			fDocument fdoc = solr.retrieveDcuments("dictWord_s:" + entry.getKey(), "TRList_s", null, SolrQuery.ORDER.asc, solr.maxRows);			
			
			String tmp ="";
			if (fdoc.getRows() >0)
			{
				tmp = (String) fdoc.getRawData(0, 0);	
			}
			String[] words = (tmp).split("\\s+");
			Set<String> TRset = new HashSet<String>(Arrays.asList(words)); 
			String[] newWords = entry.getValue().split("\\s+");
			for (String TR:newWords)
			{
				TRset.add(TR);
			}
			
			// merge these related TR into TRs
			TRs.addAll(TRset);
			
			String newTRStr="";
			for (String TR:TRset)
			{
				newTRStr = newTRStr + " " + TR;
			}
    		
			data.clear();
			String uniqueid ="dict-" + entry.getKey();
			data.add(Integer.toString(uniqueid.hashCode()));
			data.add("");  // no contentid
			data.add(entry.getKey());
			data.add(newTRStr);
			
			solr.addDocument(data, dictFieldNames);
		}
		
		solr.commit();
	}
	
	
	
	public Set<String> retrieveTRs() throws IOException
	{

		if (lastUpdated==null)
		{
			try {
				initialize();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// if we can not get lastUpdated date then use the first date from database
		if (lastUpdated==null)
		{
			lastUpdated = featureMap.getInstance().startDate();
		}
		
		// read TRs from lastUpdated to now
		Date now=new Date();
		rResult ret=solr.retrieveGroups("fromdisp:crm AND -conv:RESOLV AND -conv:RESOLVED", "contentid", "conv_sort", lastUpdated, now, "1", solr.maxGroups);
		int len=ret.getNames().length;
		
		Set<String> TRIDlist = new HashSet<String>();
		HashMap<String, String> dict = new HashMap<String, String>();
		
		if (len>0) // len TRs
		{
			// retrieve content 
			Document doc;
			Element body, ele;
			Elements eles;
			
			
			String contentid="", TRID = "", contactName = "", incidentStatus="", orgName="", severity="", productVer="", subTime="", productType="", escalation = "", 
				summary = "", SP="", failedType="", failedReason="",  serveOS="", serveCPU="", serveSP="";
			
			for (int i=0; i<len; i++)
			{
				contentid = ret.getValues()[i];
				doc = Jsoup.connect(solr.contentURL(contentid)).get();
				body = doc.body();
				
				// this is one way to retrieve the information
				/*eles = body.getElementsContainingText("Contact Name");
				ele = eles.get(eles.size()-1).nextElementSibling();
				contactName = ele.text();*/
				
				eles = body.getElementsContainingText("Reference Number");
				if (eles !=null && eles.size()>0)
				{
					ele = eles.get(eles.size()-1);
					TRID = ele.text().split(":")[1];
					TRID = TRID.replaceAll("\\s+","");
				}
				else
				{
					continue;
				}
				
				eles = body.getElementsByAttributeValueContaining("class", "label");
				for (int j=0; j<eles.size(); j++)
				{
					if (eles.get(j).text().contains("Contact Name:"))
					{
						ele = eles.get(j).nextElementSibling();
						contactName = ele.text();
						contactName = contactName.replaceAll("\\s+", "");
					}
					else if (eles.get(j).text().contains("Incident Status:"))
					{
						ele = eles.get(j).nextElementSibling();
						incidentStatus = ele.text();
					}
					else if (eles.get(j).text().contains("Organization Name:"))
					{
						ele = eles.get(j).nextElementSibling();
						orgName = ele.text();
					}
					else if (eles.get(j).text().contains("Severity:"))
					{
						ele = eles.get(j).nextElementSibling();
						severity = ele.text();
					}
					else if (eles.get(j).text().contains("Product Version:"))
					{
						ele = eles.get(j).nextElementSibling();
						productVer = ele.text();
					}
					else if (eles.get(j).text().contains("Submission Date and Time (UTC):"))
					{
						ele = eles.get(j).nextElementSibling();
						subTime = ele.text();
					}
					else if (eles.get(j).text().contains("Product Type (Agent):"))
					{
						ele = eles.get(j).nextElementSibling();
						productType = ele.text();
					}
					else if (eles.get(j).text().contains("Escalation Purpose:"))
					{
						ele = eles.get(j).nextElementSibling();
						escalation = ele.text();
					}
					else
					{
						
					}
				}
				
				eles = body.getElementsContainingText("Incident Summary:");
				if (eles !=null && eles.size()>0)
				{
					ele = eles.get(eles.size()-1).nextElementSibling();
					summary = ele.text();
					summary = summary.split("Alias Escalating to")[0];
					summary = summary.split("Issue Summary:")[1];
				}
				
				
				eles = body.getElementsContainingText("SP Installed:");
				// the html is not well formatted, so can not use sibling element
				if (eles !=null && eles.size()>0)
				{
					ele = eles.get(eles.size()-1).parent();
					String[] strList = ele.toString().split("SP Installed:");
					strList = strList[1].split("</span>");
					strList = strList[1].split("<br />");
					SP = strList[0];
				}
				
				eles = body.getElementsContainingText("Failed Job Type:");
				// the html is not well formatted, so can not use sibling element
				if (eles !=null && eles.size()>0)
				{
					ele = eles.get(eles.size()-1).parent();
					String[] strList = ele.toString().split("Failed Job Type:");
					strList = strList[1].split("</span>");
					strList = strList[1].split("<br />");
					failedType = strList[0];
				}
				
				eles = body.getElementsContainingText("Failed Job Reason:");
				if (eles !=null && eles.size()>0)
				{
					ele = eles.get(eles.size()-1).parent();
					String[] strList = ele.toString().split("Failed Job Reason:");
					strList = strList[1].split("</span>");
					strList = strList[1].split("<br />");
					failedReason = strList[0];
				}
				
				eles = body.getElementsContainingText("Operating System:");
				if (eles !=null && eles.size()>0)
				{
					ele = eles.get(eles.size()-1).parent();
					String[] strList = ele.toString().split("Operating System:");
					strList = strList[1].split("</span>");
					strList = strList[1].split("<br />");
					serveOS = strList[0];
				}
				
				eles = body.getElementsMatchingText("CPU:");
				if (eles !=null && eles.size()>0)
				{
					ele = eles.get(eles.size()-1).parent();
					String[] strList = ele.toString().split("CPU:");
					strList = strList[1].split("</span>");
					strList = strList[1].split("<br />");
					serveCPU = strList[0];
				}
				
				eles = body.getElementsMatchingText("SP:");
				if (eles !=null && eles.size()>0)
				{
					ele = eles.get(eles.size()-1).parent();
					String[] strList = ele.toString().split("SP:");
					strList = strList[1].split("</span>");
					strList = strList[1].split("<br />");
					serveSP = strList[0];
				}
				
				// parse TF.IDF
				summary = summary + " " + failedReason + " " + failedType + " " + escalation;
				String mandatory = contactName + " " + incidentStatus + " " + orgName + " " + severity + " " + productVer 
						+ " " + subTime + " " + productType + " " + SP + " " + serveOS + " " + serveCPU + " " + serveSP;
				mandatory = mandatory.toLowerCase();
				
				// let's move mandatory to summary
				summary = summary + " " + mandatory;
				mandatory="";
				
				HashMap<String, Integer> map = stopWords.getInstance().segment(summary);
				// add terms into dict
				for (Map.Entry<String, Integer> entry : map.entrySet())
				{
					if (dict.containsKey(entry.getKey()))
					{
						String list = dict.get(entry.getKey()); 
						if (!list.contains(TRID))
						{
							dict.put(entry.getKey(), list + " " + TRID);
						}
					}
					else
					{
						dict.put(entry.getKey(), TRID);
					}
				}
				
				try {
					System.out.println("building TR: " + TRID + ". " + Integer.toString(i) + "/" + Integer.toString(len) + " TRs");
					storeDB(contentid, TRID, map, mandatory);
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				TRIDlist.add(TRID);
				
			}// iterate for all TRs 
			
			
			// store dict
			try {
				updateDict(dict, TRIDlist);
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// update lastUpdated date
			lastUpdated = now;
			outputLastUpdated();
		}
		
		return TRIDlist;
		
	}
	
	
	
    public void retrieveTRs_thread()
    {

    	Thread t = new Thread() { // Create an anonymous inner class extends Thread
    		@Override
    		public void run() {
    			try {
					Set<String> TRIDlist = retrieveTRs();
					buildReps(TRIDlist);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			featureMap.getInstance().notifyBuildTRDB();
    			System.out.println("Exit retrieveTRs_thread");
    		}
    	};
    	t.start();
    }
    
    
    
    public void buildReps(Set<String> TRIDlist)
    {
    	
    	// num of documents we have in total
    	//ArrayList<String> TRs =solr.retrieveGroupsNames("frequency_i:[1 TO *]", "TRID_s");
    	long totalTRs = TRIDlist.size();
    	int i=0;
    	for (String TR:TRIDlist)
    	{
    		System.out.println("building representative_words TR: " + TR + ". " + Integer.toString(i) + "/" + Long.toString(totalTRs) + " TRs");
    		updateTFIDF(TR, totalTRs);
    		i++;
    	}
    }
    
    
    public void updateTFIDF(String TRID, long totalTRs)
    {
    	if (TRID.equals(""))
    	{
    		return;
    	}
    	
    	TreeMap<Double, String> tfidf = new TreeMap<Double, String>();
    	
    	// retrieve bodywords of TRID
    	fDocument fdoc = solr.retrieveDcuments("TRID_s:" + TRID, "frequency_i bodyWords_s", "frequency_i", SolrQuery.ORDER.asc, solr.maxRows);
    	
    	
    	//calculate TF.IDF for each word
    	for (int i=0; i<fdoc.getRows(); i++)
    	{
    		String tmp = (String) fdoc.getRawData(i, 1);
    		String[] words = (tmp).split("\\s+");
    		
    		for (int j=0; j<words.length; j++)
    		{
    			// find how many times this word appear in other documents
    			//long times = solr.retrieveDcumentsNum("bodyWords_s:/.*" + words[j] + ".*/");
    			fDocument fdoc2 = solr.retrieveDcuments("dictWord_s:" + words[j], "TRList_s", null, SolrQuery.ORDER.asc, solr.maxRows);
    			long times=0;
    			if (fdoc2.getRows() > 0)
    			{
    				String raw = (String) fdoc2.getRawData(0, 0);
    				String[] rawStrs = raw.split("\\s+");
    				times = rawStrs.length;
    			}
    			
    			if (times==0)
    			{
    				System.out.println("");
    			}
    			double value = (Math.log(1.0*totalTRs/times)/Math.log(2))*(1.0*(j+1)/(fdoc.getRows()));
    			if (tfidf.containsKey(value))
    			{
    				tfidf.put(value, tfidf.get(value) + " " +words[j]);	
    			}
    			else
    			{
    				tfidf.put(value, words[j]);
    			}
    		}
    	}
    	
    	// representive words of this document
    	String rep = "";
    	int i=0;

    	NavigableMap<Double, String> nmap = tfidf.descendingMap();
    	outloop:
    	for (Map.Entry<Double, String> entry : nmap.entrySet())
    	{
    		String[] strs = entry.getValue().split("\\s+");
    		for (int j=0; j<strs.length; j++)
    		{
    			rep = rep + " " + strs[j];
    			i = i+1;
    			if (i==REPNUM)
    			{
    				break outloop;
    			}
    		}
    	}
    	
    	
    	// add the representative words of the document into DB
    	ArrayList<Object> data = new ArrayList<Object>();
    	String uniqueid = "recom-rep-"+TRID; 
		data.add(Integer.toString(uniqueid.hashCode()));
		data.add("recom-rep-"+TRID);
		data.add(TRID);
		data.add(rep);

		
		try {
			solr.addDocument(data, repFieldNames);
			solr.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    
    private Set<String> getRepWords(String TRID)
    {
    	Set<String> ret=new HashSet<String>();
    	String qStr = "rep-TRID_s:" + TRID + " OR (TRID_s:" + TRID + " AND frequency_i:1)&fl=mandatoryWords_s repWords_s";
    	String[] fields={"mandatoryWords_s","repWords_s"};
    	fDocument fdoc = solr.retrieveDcumentsArb(qStr, fields, "frequency_i", SolrQuery.ORDER.asc, solr.maxRows);
    	
    	for (int i=0; i<fdoc.getRows(); i++)
    	{
    		for (int j=0; j<fdoc.getCols(); j++)
    		{
    			String tmp =(String)fdoc.getRawData(i, j); 
    			if (tmp!=null && tmp.length() > 0)
    			{
    				String[] strs = tmp.split("\\s+");
    				for (int k=0; k<strs.length; k++)
    				{
    					ret.add(strs[k]);
    				}
    			}
    		}
    	}
    	
    	return ret;
    }
    
    
    private int overlapping(Set<String> set1, Set<String> set2)
    {
    	int ret=0;
    	
    	if (set1.size() < set2.size())
    	{
    		for (String obj:set1)
    		{
    			if (set2.contains(obj))
    			{
    				ret++;
    			}
    		}
    	}
    	else
    	{
    		for (String obj:set2)
    		{
    			if (set1.contains(obj))
    			{
    				ret++;
    			}
    		}    		
    	}
    	
    	return ret;
    }
    
    
    
    public double jaccardDistance(String TR1, String TR2)
    {
    	double ret=0;
    	
    	// get representative words and mandatory words from TR1
    	Set<String> set1 = getRepWords(TR1);
    	Set<String> set2 = getRepWords(TR2);
    	
    	int and = overlapping(set1, set2);
    	int or = set1.size() + set2.size() - and;
    	
    	ret = 1 - 1.0*and/or;
    	
    	return ret;
    }
	

    public rResult queryTR(String TRID)
    {
    	rResult ret=new rResult();
    	
    	ArrayList<String> TRs =solr.retrieveGroupsNames("frequency_i:[1 TO *]", "TRID_s");
    	TreeMap<Double, String> map = new TreeMap<Double, String>();
    	
    	for (int i=0; i<TRs.size(); i++)
    	{
    		//System.out.println("jaccard TR: " + TRs.get(i) + ". " + Integer.toString(i) + "/" + Long.toString(TRs.size()) + " TRs");
    		double score = jaccardDistance(TRID, TRs.get(i));
    		System.out.println("jaccard TR: " + TRs.get(i) + ". " + Integer.toString(i) + "/" + Long.toString(TRs.size()) + " TRs" + ", score " + Double.toString(score));
    		map.put(1-score, TRs.get(i));
    	}
    	
    	ArrayList<Map.Entry<String, Double>> similarity = new ArrayList<Map.Entry<String, Double>>();
    	for (Map.Entry<Double, String> entry: map.entrySet())
    	{
    		if (entry.getKey()>0)
    		{
    			similarity.add(new AbstractMap.SimpleEntry<String, Double>(entry.getValue(), entry.getKey()));
    		}
    	}
    	
    	ret.setDistance(similarity);
		return ret;
    }
    
    
    public void queryTR_thread(final String TRID)
    {

    	Thread t = new Thread() { // Create an anonymous inner class extends Thread
    		@Override
    		public void run() {
    			rResult ret=queryTR(TRID);
    			featureMap.getInstance().notifySolr(ret, OPT_CODE.RECOMM_RDG);;
    			System.out.println("Exit queryTR_thread");
    		}
    	};
    	t.start();
    }    
    
    
    
}
