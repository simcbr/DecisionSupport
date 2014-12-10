package DecisionSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.solr.client.solrj.SolrServerException;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import DecisionSupport.perfPanel.HIST_OPTION;
import DecisionSupport.perfPanel.RATIO_OPTION;
import DecisionSupport.perfPanel.SUMMARY_MODE;
import DecisionSupport.rConnector.DataModel;
import DecisionSupport.solrConnector.OPT_CODE;

import org.apache.solr.client.solrj.SolrQuery;

/*
 * this class is the interface between solrDocument and the internal data structure
 * which can be sent to the rServe 
 */
public class featureMap 
{

	private rConnector rc;
	private solrConnector solr;	
	
	private fDocument fdoc=null; 
	public static featureMap fm=null;
	private fFilter filter=null;
	private TRFeatureDB trFDB=null;
	
	private OPT_CODE opt_mode=OPT_CODE.FREE;
	
	featureMap()
	{
		rc = rConnector.connector();
		solr = new solrConnector(null, null);
		filter = fFilter.getInstance();
		trFDB = new TRFeatureDB(solr);
	}
	
	
	public static featureMap getInstance()
	{
		if (fm==null)
		{
			fm = new featureMap();
		}
		
		return fm;
	}
	
	
	/****************************************
	 * 
	 * @param libName
	 * load a library from Rserve and save it to solr
	 ****************************************/	
	public void loadLibrarytoSolr(String libName) throws RserveException, REXPMismatchException, SolrServerException, IOException
	{
		//read dataframe from R
		fDocument fdoc = new fDocument();
		fdoc = rc.frameDocument(libName);
		
		//load data into solr		
		solr.addDocuments(fdoc);
	}
	
	
	public void connectSolr(String hostname, String port)
	{
		solr.setServer(hostname, port);
	}
	
	
	/****************************************
	 * 
	 * @param qstr
	 * @return fDocument
	 * query documents from solr
	 ****************************************/
	public void loadfromSolr(String docName, String qstr, String fields, int rows)
	{
		
		//fdoc = solr.retrieveDcuments(qstr + filter.getInterestedQuery(), rows);
		fdoc = solr.retrieveDcuments(qstr, fields, null, SolrQuery.ORDER.asc, rows);
		fdoc.setDocName(docName);		
	}
	
	
	public String[] interestedUsers()
	{
		return filter.interestedUsers();
	}
	
	public fDocument getFDoc()
	{
		return fdoc;
	}
	
	
	public void clearIndex()
	{
		solr.clearIndex();
	}
	
	
	public rResult trainModel(DataModel model) throws RserveException, REXPMismatchException
	{
		return rc.trainModel(model, fdoc);	
	}
	
	
	public rResult statSummary(ArrayList<String> selectedUsers, String queryStr, int queryRows, ArrayList<String> choices, Date start, Date end, SUMMARY_MODE mode) throws RserveException, REXPMismatchException
	{
		
/*		//old architecture
 		if (fdoc.getNames().size()!=choices.size())
		{
			System.out.println("Re-search to update fdoc.");
			return null;
		}
		else
		{
			return rc.statSummary(choices, fdoc, filter, mode);
		}*/
		
		if (mode == SUMMARY_MODE.ABSOLUTE_M)
		{
			return solr.retrieveFacets(selectedUsers, queryStr, "mtm", "fromdisp_sort", start, end, solr.maxFacetLimit);
		}
		else if (mode == SUMMARY_MODE.FREQUENT_M) 
		{
			return solr.retrieveFacets(selectedUsers, queryStr, "mtm", "fromdisp_sort", start, end, solr.maxFacetLimit);
		}
		
		return null;
	}
	
	
	public rResult statHist(String queryStr, int queryRows, ArrayList<String> choices, Date start, Date end, HIST_OPTION option) throws RserveException, REXPMismatchException
	{
		
		if (option == HIST_OPTION.EMAIL_O)
		{
			// get the earliest time
			/*fDocument f;
			f=solr.retrieveDcuments("*.*", "mtm", "mtm", SolrQuery.ORDER.asc, 1);
			Date start= (Date)f.getRawData(0, 0);
			Date now =new Date();*/
			return solr.retrieveDateFacets(queryStr, "mtm", "mtm", start, end, "+1DAY", queryRows);
		}
		else if (option == HIST_OPTION.TR_O)
		{
			//queryStr = queryStr + "AND (from:crm AND conv:* NOT \"RESOLVED\")";
			//return solr.retrieveDateFacets(queryStr, "mtm", "mtm", start, end, "+1DAY", queryRows);
			solr.retrieveDateGroups_thread(queryStr, "conv", "mtm", "mtm asc", start, end, solr.maxPerGroup, solr.maxGroups, OPT_CODE.TR_RDG);
		}
		
		return null;
	}
	
	
	public rResult statRatio(String queryStr, Date start, Date end, RATIO_OPTION option) throws RserveException, REXPMismatchException
	{
		
		if (option == RATIO_OPTION.OPENED_R)
		{
			// get the earliest time
			queryStr = "(" + queryStr + ") AND (" +  "fromdisp:crm AND conv:/.*CVLT.*/ )";
			solr.retrieveDateGroups_thread(queryStr, "conv", "mtm", "mtm asc", 
											start, end, solr.maxPerGroup, solr.maxGroups, OPT_CODE.OPENED_RDG);
		}
		else if (option == RATIO_OPTION.RESOLVED_R)
		{
			queryStr = "(" + queryStr + ") AND (fromdisp:crm AND (conv:/.*RESOLVED.*/ OR conv:/.*RESOLV.*/))";
			solr.retrieveDateGroups_thread(queryStr, "conv", "mtm", "mtm asc", 
											start, end, solr.maxPerGroup, solr.maxGroups, OPT_CODE.RESOLVED_RDG);
			
		}
		
		return null;
	}	
	
	
	
	public rResult statRank(String queryStr, Date start, Date end, String fields, OPT_CODE option) throws RserveException, REXPMismatchException
	{
		
		if (option == OPT_CODE.RESOLVED_RDG || option == OPT_CODE.OPENED_RDG)
		{
			queryStr = "(" + queryStr + ") AND (" +  "fromdisp:crm)";
			// since we group by conv, the max items retrieved for each group will alwyas be 1
			return solr.retrieveRankGroups(queryStr, fields, "conv_sort", "mtm asc", start, end, "1", solr.maxGroups);
		}
		
		return null;
	}	
	
	
	public Date startDate()
	{
		// get the earliest time
		fDocument f;
		f=solr.retrieveDcuments("*.*", "mtm", "mtm", SolrQuery.ORDER.asc, 1);
		return (Date)f.getRawData(0, 0);
	}
	
	
	public void notifySolr(rResult ret, OPT_CODE mode)
	{
		switch (mode)
		{
		case TR_RDG:
			if (ret!=null)
			{
				GUI.getInstance().loadHistPanel(ret, HIST_OPTION.TR_O);
			}
			break;
			
		case OPENED_RDG:
			if (ret!=null)
			{
				GUI.getInstance().loadRatioPanel(ret, RATIO_OPTION.OPENED_R);
			}
			break;
			
		case RESOLVED_RDG:
			if (ret!=null)
			{
				GUI.getInstance().loadRatioPanel(ret, RATIO_OPTION.RESOLVED_R);
			}
			break;
			
		case RECOMM_RDG:
			if (ret!=null)
			{
				GUI.getInstance().loadRecommPanel(ret);
			}
			
		default:
			break;
		}
		
		opt_mode = OPT_CODE.FREE;
	}
	
	
	public void buildTRDB()
	{
		trFDB.retrieveTRs_thread();
	}
	
	
	public rResult queryTRDB(String TRID)
	{
		trFDB.queryTR_thread(TRID);
		return null;
	}
	
	
	public void notifyBuildTRDB()
	{
		GUI.getInstance().enableMenus();
	}
	
	
	
	/*public static void main(String[] args) 
	{
		featureMap fm = featureMap.getInstance();
		fDocument fdoc = new fDocument();
		
		try 
		{
			fm.clearIndex();
			fm.loadLibrarytoSolr("Carseats");
			fm.loadfromSolr("Carseats", "*:*", 10);
			fm.trainModel(DataModel.LOGISTIC_T);
		} 
		catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
}
