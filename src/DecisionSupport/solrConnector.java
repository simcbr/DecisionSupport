package DecisionSupport;


/*import solr related libraries*/
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;



public class solrConnector 
{

	private static String HOSTNAME="127.0.0.1";//"172.19.52.130";
	
	private String serverAddress=HOSTNAME;
	private String serverPort="8893";//"8983";
	private SolrServer server = null;
	
	private boolean solrStatus = false;

	public final String maxPerGroup=Integer.toString(Integer.MAX_VALUE);
	public final int maxGroups=Integer.MAX_VALUE;
	public final int maxFacetLimit=-1;
	public final int maxRows=Integer.MAX_VALUE;
	
	public enum OPT_CODE{
		FREE, TR_RDG, OPENED_RDG, RESOLVED_RDG, RECOMM_RDG
	}
	
	// SwingWorker parameter blocks
	// retrieveDateGroups: String qstr, String fields, String groupField, String groupSort, Date start, Date end, String limit, int groups
	private String rDG_qstr, rDG_fields, rDG_groupField, rDG_groupSort, rDG_limit;
	private Date rDG_start, rDG_end;
	private int rDG_groups;
	private OPT_CODE opt_mode;
	
	
	
	
	/*
	 * set solr server status
	 */
	private void setSolrActive(boolean status)
	{
		solrStatus = status;
	}
	
	
	/*
	 * ping solr serevr to update its status
	 */
	private void pingSolr() throws SolrServerException
	{
	    ModifiableSolrParams params = new ModifiableSolrParams();
	    params.set("qt", "/admin/cores");
	    params.set("action", "STATUS");
		
	    QueryResponse response = solrQuery(params);
	    if(response != null && response.getStatus() == 0)
	    {
		    NamedList<Object> results = response.getResponse();
		    @SuppressWarnings("unchecked")
	              NamedList<Object> report = (NamedList<Object>)results.get("status");
		    Iterator<Map.Entry<String, Object>> coreIterator = report.iterator();
		    List<String> cores = new ArrayList<String>(report.size());
		    while(coreIterator.hasNext())
		    {
		    	Map.Entry<String, Object> core = coreIterator.next();
		    	cores.add(core.getKey());
		    }
		    
		    //registerCores(cores);
	    	setSolrActive(true);
	    }
	    else
	    {
	    	setSolrActive(false);
	    }
	}	
	
	
	solrConnector(String hostname, String port)
	{
		setServer(hostname, port);
	}
	
	
	public void setServer(String hostname, String port)
	{
		if (hostname != null)
		{
			serverAddress = hostname;
		}
		
		if (port != null)
		{
			serverPort = port;
		}
		
		String address = "http://" + serverAddress + ":" + serverPort + "/solr/";
		
		server = new HttpSolrServer(address);	
	}
	
	public String contentURL(String contentID)
	{
		return "http://" + serverAddress + ":" + serverPort + "/viewsourceservlet/?docid=" + contentID; 
	}
	
	
	public SolrServer getSolrServer()
	{
		return this.server;
	}
	
	
	public QueryResponse solrQuery(ModifiableSolrParams params) throws SolrServerException
	{
	   	try
	   	{
		    QueryResponse response = server.query(params);
		    if(response.getStatus() != 0)
		    {
		    	setSolrActive(false);
		    }

		    return response;
		}
		catch(SolrServerException e)
		{
			setSolrActive(false);
			throw e;
		}
	}
	
	/*****************************************************
	 * 
	 * @param fdoc
	 * @throws SolrServerException
	 * @throws IOException
	 * 
	 * every time add a document, solr creates relevant index
	 *****************************************************/
	public void addDocuments(fDocument fdoc) throws SolrServerException, IOException
	{
/*		for(int i=0;i<1000;++i) 
		{
			SolrInputDocument doc = new SolrInputDocument();
		    doc.addField("cat", "book");
		    doc.addField("id", "book-" + i);
		    doc.addField("name", "The Legend of the Hobbit part " + i);
		    server.add(doc);
		    if(i%100==0)
		    {
		    	server.commit();  // periodically flush
		    }
		}
		server.commit();*/
		
		Object[][] data = fdoc.getData();
		ArrayList<String> keys = fdoc.getNames();
		int rows = data.length;
		int cols = keys.size();
		
		for (int i=0; i<rows; i++)
		{
			SolrInputDocument doc = new SolrInputDocument();
			
			doc.addField("id", i);
			
			for (int j=0; j<cols; j++)
			{
				doc.addField(keys.get(j)+"_d", data[i][j]);
			}
		    server.add(doc);
		    if(i%100==0)
		    {
		    	server.commit();  // periodically flush
		    }			
		}
		
		server.commit();
	}
	
	
	public void commit()
	{
		try {
			server.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addDocument(ArrayList<Object> data, ArrayList<String> names) throws SolrServerException, IOException
	{
		SolrInputDocument doc = new SolrInputDocument();
		
		doc.addField(names.get(0), data.get(0));  // uniqueitemid
		doc.addField(names.get(1), data.get(1));  // contentid
		
		for (int i=2; i<data.size(); i++)
		{
			Object obj = data.get(i);
			if (obj instanceof String)
			{
				doc.addField(names.get(i)+"_s", obj);
			}
			else if (obj instanceof Double)
			{
				doc.addField(names.get(i)+"_d", obj);
			}
			else if (obj instanceof Integer)
			{
				doc.addField(names.get(i)+"_i", obj);
			} 

		}
		
		server.add(doc);
	}
	
	
	/*****************************************************
	 * 
	 * @param qstr
	 * @param rows
	 * @return  fDocument query from solr
	 ****************************************************/
	public fDocument retrieveDcuments(String qstr, String fields, String sortField, SolrQuery.ORDER order, int rows)
	{
		Object[][] rawData=null;
		fDocument fdoc=new fDocument();
		
		SolrQuery query = new SolrQuery();
	    query.set("q", qstr);
	    query.setFields(fields);
	    if (sortField!=null)
	    {
	    	query.setSort(sortField, order);
	    }
	    query.setRows(rows);
	    
	    
	    QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());
		}
		
		if (response != null)
		{
			SolrDocumentList results = response.getResults();
			rows = results.size(); // change it to the actual rows returned
			int cols = 0;
			//System.out.println("retrieve " + results.size() + " documents");
			for (int i = 0; i < rows; ++i)  // for each document
			{
				//System.out.println(results.get(i));
				SolrDocument doc = results.get(i); 
				if (cols==0)
				{
					cols = doc.size();
					fdoc.setNames(new ArrayList<String>(doc.getFieldNames()));
					rawData = new Object[rows][cols]; // ignore the first column id and the last column _version_ (let fDocument handle it)
				}
				
				for (int j=0; j<cols; j++) 
				{
					Object value = doc.getFieldValue(fdoc.getName(j));
					rawData[i][j] = value;
				}
				//data[i] = doc.getFieldValueMap().values().toArray(double[]);
				
			}
			
			fdoc.setCols(fdoc.getNames().size());
			fdoc.setRows(rows);
			fdoc.setData(rawData);
		}

		return fdoc;
	}
	
	
	
	public fDocument retrieveDcumentsArb(String qstr, String[] fields, String sortField, SolrQuery.ORDER order, int rows)
	{
		Object[][] rawData=null;
		fDocument fdoc=new fDocument();
		
		SolrQuery query = new SolrQuery();
	    query.set("q", qstr);
	    query.setFields(fields);
	    if (sortField!=null)
	    {
	    	query.setSort(sortField, order);
	    }
	    query.setRows(rows);
	    
	    
	    QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());
		}
		
		if (response != null)
		{
			SolrDocumentList results = response.getResults();
			rows = results.size(); // change it to the actual rows returned
			int cols = fields.length;
			fdoc.setNames(fields);
			rawData = new Object[rows][cols];
			//System.out.println("retrieve " + results.size() + " documents");
			for (int i = 0; i < rows; ++i)  // for each document
			{
				//System.out.println(results.get(i));
				SolrDocument doc = results.get(i); 
				
				for (int j=0; j<cols; j++) 
				{
					Object value = doc.getFieldValue(fdoc.getName(j));
					rawData[i][j] = value;
				}
				//data[i] = doc.getFieldValueMap().values().toArray(double[]);
				
			}
			
			fdoc.setCols(fdoc.getNames().size());
			fdoc.setRows(rows);
			fdoc.setData(rawData);
		}

		return fdoc;		
	}
	
	
	public long retrieveDcumentsNum(String qstr)
	{
		long ret=0;
		
		SolrQuery query = new SolrQuery();
	    query.set("q", qstr);
	    query.setRows(0);
	    
	    
	    QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());
		}
		
		if (response != null)
		{
			SolrDocumentList results = response.getResults();
			ret = results.getNumFound(); // change it to the actual rows returned
		}

		return ret;
	}
	
	
	public ArrayList<String> retrieveGroupsNames(String qstr, String groupField)
	{
		ArrayList<String> ret=new ArrayList<String>();
		
		SolrQuery query = new SolrQuery();
	    query.set("q", qstr);
	    query.set("group.field", groupField);
	    query.set("group","true");
	    query.set("group.ngroups", "true");
	    query.set("group.limit", 0);
	    query.setRows(-1);
	    
	    QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response != null)
		{
			List<GroupCommand> gcList = response.getGroupResponse().getValues();
			// we only have one group command
			List<Group> gList = gcList.get(0).getValues();
			
			for (Group g:gList)
			{
				ret.add(g.getGroupValue());
			}
			
		}

		return ret;
	}	
	
	
	
	private boolean insensitiveContain(ArrayList<String> strList, String obj)
	{
		boolean ret=false;
		
		for (String str:strList)
		{
			if (str.equalsIgnoreCase(obj))
			{
				ret=true;
				break;
			}
		}
		
		return ret;
	}
	
	public rResult retrieveFacets(ArrayList<String> selectedUsers, String qstr, String fields, String facetField, Date start, Date end, int facetlimit)
	{
		rResult ret=null;
		
		SolrQuery query = new SolrQuery();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		qstr = qstr + " AND (mtm:[" + sdf.format(start) + " TO " + sdf.format(end) + "])";
	    query.set("q", qstr);
	    query.setFields(fields);
	    query.addFacetField(facetField); // only one facet field
	    query.setFacetLimit(facetlimit);
		
	    int days = (int)((end.getTime()-start.getTime())/(24*60*60*1000));
	    		
	    QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response != null)
		{
			ret = new rResult();
			List<FacetField> ffList = response.getFacetFields();
	
			ArrayList<String> interestedList = null;
			if (selectedUsers.size()==0)
			{
				interestedList = new ArrayList<String>();
				String[] interested=featureMap.getInstance().interestedUsers();
				
				Collections.addAll(interestedList, interested);	
			}
			else
			{
				interestedList = selectedUsers;
			}
			 
			
			ArrayList<Integer> hist=new ArrayList<Integer>();
			ArrayList<Integer> periods=new ArrayList<Integer>();
			ArrayList<String> names=new ArrayList<String>();
			
			for(FacetField ff : ffList)
			{
				if (ff.getName().equals("fromdisp_sort"))
				{
					List<Count> counts = ff.getValues();
				    for(Count c : counts)
				    {
				    	if (insensitiveContain(interestedList, c.getName()))
				    	{
				    		names.add(c.getName());
							hist.add((int) c.getCount());
							periods.add(days);	
				    	}
									        
				    }			
				}
				

					
			}
			ret.setHist(hist.toArray(new Integer[hist.size()]));
			ret.setNames(names.toArray(new String[names.size()]));
			ret.setPeriods(periods.toArray(new Integer[periods.size()]));
		}
			    
	    
	    
	    return ret;
	}
	
	
	
	public rResult retrieveDateFacets(String qstr, String fields, String facetField, Date start, Date end, String gap, int rows)
	{
		rResult ret=null;
		
		SolrQuery query = new SolrQuery();
	    query.set("q", qstr);
	    query.setFields(fields);
	    
	    query.addDateRangeFacet(facetField, start, end, gap);
	    query.setRows(rows);
	    
	    
	    QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response != null)
		{
			List<RangeFacet> rfList = response.getFacetRanges();
	
			for(RangeFacet rf : rfList)
			{
				if (rf.getName().equals("mtm"))
				{
					ret=new rResult();
					SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					List<RangeFacet.Count>	rfCounts = rf.getCounts();
					Date[] dates=new Date[rfCounts.size()];
					int[] counts=new int[rfCounts.size()];					
					int i=0;
					
					for (RangeFacet.Count count : rfCounts)
					{
						try {
							dates[i] = formater.parse(count.getValue());
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						counts[i] = count.getCount();
						i++;
					}

					ret.setDates(dates);
					ret.setCounts(counts);
					
				}
			}
		}
		
		return ret;
	}	
	
	
	
	public rResult retrieveRankGroups(String qstr, String fields, String groupField, String groupSort, Date start, Date end, String limit, int groups)
	{
		Map<String,ArrayList<Long>> opened = new HashMap<String, ArrayList<Long>>();
		Map<String,ArrayList<Long>> closed = new HashMap<String, ArrayList<Long>>();
		
		rResult ret=null;
		
		SolrQuery query = new SolrQuery();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		qstr = qstr + " AND (mtm:[" + sdf.format(start) + " TO " + sdf.format(end) + "])";
	    query.set("q", qstr);
	    query.setFields(fields);
	    query.set("group.field", groupField);
	    query.set("group.sort", groupSort);
	    query.set("group","true");
	    query.set("group.ngroups", "true");
	    query.set("group.limit", limit);
	    query.setRows(groups);
	    
	    QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response != null)
		{
			List<GroupCommand> gcList = response.getGroupResponse().getValues();
			// we only have one group command
			List<Group> gList = gcList.get(0).getValues();
			
			System.out.println("Loading:Data... get " + Integer.toString(gList.size()) + " groups");
			
			for(Group g : gList)
			{
				SolrDocumentList results = g.getResult();
				String TR = g.getGroupValue();
				boolean resolved = TR.toLowerCase().contains("resolv");
				Pattern patternID = Pattern.compile("\\d{6}-\\d{1,3}");
				
				// retrieve customer ID
				String[] splits = TR.split(" - ");
				String customerName="";
				Pattern patternVersion = Pattern.compile("v\\d{1,3}",Pattern.CASE_INSENSITIVE);
				Matcher version = patternVersion.matcher(splits[1]);
				if (version.find())
				{
					customerName = splits[2];	
				}
				else
				{
					customerName = splits[1];
				}
				
				
				
				Matcher trID = patternID.matcher(TR);
				
				if (trID.find())
				{
				    TR = trID.group(0);
				    // attach customer name
				    TR = TR + "-[" + customerName + "]";   
				}
				else
				{
					continue;
				}
				
				int rows = results.size(); // change it to the actual rows returned
				//SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
				
				long tm=0, minTm=0, maxTm=0;

				// since the result is in ascending order, directly pick the first and last one.
				minTm = ((Date) results.get(0).getFieldValue("mtm")).getTime();
				maxTm = ((Date) results.get(rows-1).getFieldValue("mtm")).getTime();
				
				// update the min and max time of TR
			    if (resolved)
			    {
			    	if (!closed.containsKey(TR))
			    	{
			    		if (opened.containsKey(TR))
			    		{
			    			updateMinMax(opened.get(TR), minTm, maxTm);
			    			closed.put(TR, opened.get(TR));
			    			opened.remove(TR);
			    		}
			    		else
			    		{
			    			closed.put(TR, new ArrayList<Long>());
			    			updateMinMax(closed.get(TR), minTm, maxTm);
			    		}
			    	}
			    }
			    else
			    {
			    	if (closed.containsKey(TR))
			    	{
			    		updateMinMax(closed.get(TR), minTm, maxTm);
			    	}
			    	else if (!opened.containsKey(TR))
			    	{
			    		opened.put(TR, new ArrayList<Long>());
			    		updateMinMax(opened.get(TR), minTm, maxTm);
			    	}
			    }
				
			}
			
			ret=new rResult();
			ret.setRanksOpened(opened);
			ret.setRanksClosed(closed);
			return ret;
		}
		else
		{
			return ret;	
		}
	}	
	
	
	
	public rResult retrieveGroups(String qstr, String fields, String groupField, Date start, Date end, String limit, int groups)
	{		
		rResult ret=null;
		
		SolrQuery query = new SolrQuery();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		qstr = qstr + " AND (mtm:[" + sdf.format(start) + " TO " + sdf.format(end) + "])";
	    query.set("q", qstr);
	    query.setFields(fields);
	    query.set("group.field", groupField);
	    query.set("group","true");
	    query.set("group.ngroups", "true");
	    query.set("group.limit", limit);
	    query.setRows(groups);
	    
	    QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response != null)
		{
			List<GroupCommand> gcList = response.getGroupResponse().getValues();
			// we only have one group command
			List<Group> gList = gcList.get(0).getValues();
			
			System.out.println("Loading:Data... get " + Integer.toString(gList.size()) + " groups");
			
			ArrayList<String> TRList=new ArrayList<String>();
			ArrayList<String> contentList=new ArrayList<String>();
			
			for(Group g : gList)
			{
				SolrDocumentList results = g.getResult();
				String TR = g.getGroupValue();
				Pattern patternID = Pattern.compile("\\d{6}-\\d{1,3}");
				
				Matcher trID = patternID.matcher(TR);
				
				if (trID.find())
				{
				    TR = trID.group(0);   
				}
				else
				{
					continue;
				}
				
				int rows = results.size(); // change it to the actual rows returned
				SolrDocument doc=null;
				for (int j=0; j<rows; j++) // there should be only one row
				{
					doc = results.get(j);
					String fieldValue = (String)doc.getFieldValue(fields);
					if (fieldValue!=null && !TRList.contains(TR))
					{
						TRList.add(TR);
						contentList.add(fieldValue);
					}
					
				}
				
			}
			
			ret=new rResult();
			ret.setNames(TRList.toArray(new String[TRList.size()]));
			ret.setValues(contentList.toArray(new String[contentList.size()]));
			return ret;
		}
		else
		{
			return ret;	
		}
	}	
	
	
	
	
	
	protected rResult retrieveDateGroups(String qstr, String fields, String groupField, String groupSort, Date start, Date end, String limit, int groups)
	{
		
		rResult ret=null;
		
		int INTERVAL = 10;
		int dateDiff = (int)((end.getTime() - start.getTime())/(24*60*60*1000));
		if (dateDiff<=0)
		{
			System.out.println("end date should be later than start date!");
			return ret;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		int times=((dateDiff-1)/INTERVAL)+1;
		
		ArrayList<Date> dates=new ArrayList<Date>();
		ArrayList<Integer> counts=new ArrayList<Integer>();
		
		long dStart=start.getTime() - INTERVAL*(24*60*60*1000), dEnd=0;
		
		
		QueryResponse response = null;
		for (int intvl=0; intvl<times; intvl++)
		{
			
			System.out.println("Querying: " + Integer.toString(intvl+1) + "/" + Integer.toString(times) + "Data...");
			
			dStart =  dStart + INTERVAL*(24*60*60*1000);
			Date intvl_startDate = new Date(dStart);
			dEnd = dStart + INTERVAL*(24*60*60*1000)-1;
			
			dEnd = Math.min(dEnd,end.getTime());
			Date intvl_endDate = new Date(dEnd);
			
			SolrQuery query = new SolrQuery();
		    query.set("q", qstr + "AND (mtm:[" + sdf.format(intvl_startDate) + " TO " + sdf.format(intvl_endDate) + "])");
		    //System.out.println("query: from " + sdf.format(intvl_startDate) + " TO " + sdf.format(intvl_endDate));
		    query.setFields(fields);
		    query.set("group","true");
		    query.set("group.sort", groupSort);
		    query.set("group.func", "floor(div(ms(mtm),mul(24,mul(60,mul(60,1000)))))");
		    query.set("group.ngroups", "true");
		    query.set("group.limit", limit);
		    query.setRows(groups);
		    
		    
			try {
				response = server.query(query);
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.toString());
				return null;
			}
			
			if (response != null)
			{
				
				List<GroupCommand> gcList = response.getGroupResponse().getValues();
				// we only have one group command
				List<Group> gList = gcList.get(0).getValues();

				System.out.println("Get: " + Integer.toString(gList.size()) + "groups");
				
				if (gList.size()>0)
				{
					for(int i=0; i<gList.size(); i++)
					{
						Group g = gList.get(i);
						SolrDocumentList results = g.getResult();
						long ld = (long) Double.parseDouble(g.getGroupValue());
						ld = ld*24*60*60*1000 + 4*60*60*1000;  // from EDT to UTC
						dates.add( new Date(ld));
						
						ArrayList<String> trList=new ArrayList<String>();
						Pattern pattern = Pattern.compile(" \\d{6}-\\d{1,3} ");  
						SolrDocument doc=null;
						Matcher matcher=null;
						int rows = results.size();
						for (int j=0; j<rows; j++)
						{
							doc = results.get(j);
							String title = (String)doc.getFieldValue("conv");
							if (title==null)
							{
								continue;
							}
							matcher = pattern.matcher((String)doc.getFieldValue("conv"));	
							if (matcher.find())
							{
							    if (!trList.contains(matcher.group(0)))
							    {
							    	trList.add(matcher.group(0));
							    }
							}
						}
						
						counts.add(trList.size());
					}
				}
				else
				{
					dates.add(intvl_endDate);
					counts.add(0);
				}
					
			}
			
			response=null;
		}
		
		int[] retCounts=new int[dates.size()];
		ArrayList<Integer> storeCounts = new ArrayList<Integer>(counts); 
    	ArrayList<Date> storeDates = new ArrayList<Date>(dates); 
    	Collections.sort(dates);
    	for (int i=0; i<dates.size(); i++)
    	{
    	    int index = storeDates.indexOf(dates.get(i));
    	    retCounts[i]=(storeCounts.get(index));
    	}    
		
		
		ret=new rResult();
		ret.setDates(dates.toArray(new Date[dates.size()]));
		ret.setCounts(retCounts);	

		

		return ret;	
	}		
	
	
	
	
	
	protected void updateMinMax(ArrayList<Long> list, long minTm, long maxTm)
	{
		if (list.size()<2)
		{
			list.clear();
			list.add(minTm);
			list.add(maxTm);
		}
		
		else
		{
			if (list.get(1).longValue() < maxTm)
			{
				list.remove(1);
				list.add(1, maxTm);
			}
			
			if (list.get(0).longValue() > minTm)
			{
				list.remove(0);
				list.add(0, minTm);
			}
		}
	}
	
	
	public void searchDocuments() throws SolrServerException
	{
	    SolrQuery query = new SolrQuery();
	    query.set("q", "*:*");
	    //query.set("qt", "/spellCheckCompRH");
	    
	    QueryResponse response = server.query(query);
	    SolrDocumentList results = response.getResults();
	    System.out.println("retrieve " + results.size() + " documents");
	    for (int i = 0; i < results.size(); ++i) 
	    {
	      System.out.println(results.get(i));
	    }		
	}
	
	
	/*
	 * clean all documents
	 */
	public void clearIndex()
	{
		try 
		{
			server.deleteByQuery("*:*");
			server.commit();
		} 
		catch (SolrServerException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

    final SwingWorker<rResult, Void> rDG_worker = new SwingWorker<rResult, Void>() {
       /** Schedule a compute-intensive task in a background thread */
       @Override
       protected rResult doInBackground() throws Exception {
    	   //  String qstr, String fields, String groupField, String groupSort, Date start, Date end, String limit, int groups
    	   return retrieveDateGroups(rDG_qstr, rDG_fields, rDG_groupField, rDG_groupSort, rDG_start, rDG_end, rDG_limit, rDG_groups);
       }

       /** Run in event-dispatching thread after doInBackground() completes */
       @Override
       protected void done() {
          try {
             // Use get() to get the result of doInBackground()
             rResult result = get();
             featureMap.getInstance().notifySolr(result, opt_mode);
          } catch (InterruptedException e) {
             e.printStackTrace();
          } catch (ExecutionException e) {
             e.printStackTrace();
          }
       }
    };	
    
    
    
	
	
    //String qstr, String fields, String groupField, String groupSort, Date start, Date end, String limit, int groups
    public void retrieveDateGroups_thread(final String qstr, final String fields, final String groupField, final String groupSort, 
    									  final Date start, final Date end, final String limit, final int groups, final OPT_CODE mode)
    {
/*    	rDG_qstr = qstr;
    	rDG_fields=fields;
    	rDG_groupField=groupField;
    	rDG_groupSort=groupSort;
    	rDG_start=start;
    	rDG_end=end;
    	rDG_limit=limit;
    	rDG_groups=groups;
    	
    	rDG_worker.execute();
    	opt_mode = mode;*/
    	
    	Thread t = new Thread() { // Create an anonymous inner class extends Thread
            @Override
            public void run() {
            	rResult ret = retrieveDateGroups(qstr, fields, groupField, groupSort, start, end, limit, groups);
            	featureMap.getInstance().notifySolr(ret, mode);
            	System.out.println("Exit retrieveDateGroups_thread");
            }
         };
         t.start();
    }
    
	
	
/*	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		System.out.println("Start");
		solrConnector solr = new solrConnector(null, null);
		
		try 
		{
			//solr.pingSolr();
			//solr.addDocuments();
			solr.searchDocuments();
		} 
		catch (SolrServerException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

}
