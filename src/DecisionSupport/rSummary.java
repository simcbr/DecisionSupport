package DecisionSupport;

import java.util.ArrayList;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import DecisionSupport.perfPanel.SUMMARY_MODE;

public class rSummary {

	protected static RConnection c=null;
	
	
	protected static RConnection getRC() throws RserveException
	{
		if (c==null)
		{
			c=rConnector.connector().getRC();
		}
		
		return c;
	}
	
	
	public rResult statSummary(ArrayList<String> choices, fDocument fdoc, fFilter filter, SUMMARY_MODE mode) throws REXPMismatchException, RserveException
	{
		rResult ret=null;

		REXP xdf = fdoc.dataFrame(choices, filter);
		
	    c = getRC();
		
	 // clear environment
	    c.voidEval("rm(list = setdiff(ls(), lsf.str()))");		    	    
	    
		c.assign("xdf", xdf);
		
		if (mode == SUMMARY_MODE.ABSOLUTE_M)
		{
			int[] hist = c.eval("table(xdf[,1])").asIntegers();
			String[] names = c.eval("names(table(xdf[,1]))").asStrings();
			
			ret = new rResult();
			ret.setHist(hist);
			ret.setNames(names);
		}
		else if (mode == SUMMARY_MODE.FREQUENT_M)
		{
			int mtm = 0;
			mtm = c.eval("\"mtm\" %in% names(xdf)").asInteger(); 
			if (  mtm==1 )
			{
				// hist comes from the column except mtm
				c.voidEval("table(xdf[,!(names(xdf) %in% c(\"mtm\"))])");
				int[] hist = c.eval("table(xdf[,!(names(xdf) %in% c(\"mtm\"))])").asIntegers();
				String[] names = c.eval("names(table(xdf[,!(names(xdf) %in% c(\"mtm\"))]))").asStrings();
				
				int[] period = new int[names.length];
				
				for (int i=0; i<names.length; i++)
				{
					c.assign("col", names[i]);
					period[i] = c.eval("max(xdf$mtm[xdf[,-which(names(xdf) == \"mtm\")]==col])").asInteger();
				}
				
				ret = new rResult();
				ret.setHist(hist);
				ret.setNames(names);
				ret.setPeriods(period);
			}
			else
			{
				System.out.println("Error: mtm field is not selected!!");
			}
		}
		
		return ret;
	}
	
}
