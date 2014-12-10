package DecisionSupport;

import java.util.ArrayList;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import DecisionSupport.perfPanel.HIST_OPTION;

public class rHist 
{
	
	protected static RConnection c=null;
	
	
	protected static RConnection getRC() throws RserveException
	{
		if (c==null)
		{
			c=rConnector.connector().getRC();
		}
		
		return c;
	}
	
	
	public rResult statHist(ArrayList<String> choices, fDocument fdoc, fFilter filter, HIST_OPTION option) throws REXPMismatchException, RserveException
	{
		rResult ret=new rResult();

		REXP xdf = fdoc.dataFrame(choices, filter);
		
	    c = getRC();
		
	 // clear environment
	    c.voidEval("rm(list = setdiff(ls(), lsf.str()))");		    	    
	    
		c.assign("xdf", xdf);
		
		int mtm = 0;
		mtm = c.eval("\"mtm\" %in% names(xdf)").asInteger(); 
		if (  mtm!=1 )
		{
			System.out.println("Error: mtm field is not selected!!");
			return null;
		}
		
		if (option == HIST_OPTION.EMAIL_O)
		{

			// hist comes from the column except mtm
			int[] hist = c.eval("table(xdf[,\"mtm\"])").asIntegers();
			String[] names = c.eval("names(table(xdf[,!(names(xdf) %in% c(\"mtm\"))]))").asStrings();
				
			int[] period = new int[names.length];
				
			for (int i=0; i<names.length; i++)
			{
				c.assign("col", names[i]);
				period[i] = c.eval("max(xdf$mtm[xdf[,-which(names(xdf) == \"mtm\")]==col])").asInteger();
			}
				
			ret.setHist(hist);
			ret.setNames(names);
			ret.setPeriods(period);

		}
		else if (option == HIST_OPTION.TR_O)
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
				
			ret.setHist(hist);
			ret.setNames(names);
			ret.setPeriods(period);

		}
		
		return ret;
	}	

}
