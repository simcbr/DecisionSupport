package DecisionSupport;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class rMLModel {
	
	private static boolean dataLoaded=false;   // we can only have one instance to load the data into R workspace
	protected static RConnection c=null;
	
	protected static boolean isDataLoaded()
	{
		return dataLoaded;
	}
	
	protected static RConnection getRC() throws RserveException
	{
		if (c==null)
		{
			c=rConnector.connector().getRC();
		}
		
		return c;
	}
	
	
	protected String train(fDocument fdoc) throws RserveException
	{
		return "";
	}
	
	public rResult trainModel(fDocument fdoc) throws RserveException, REXPMismatchException
	{
		String tag;
		
		loadData(fdoc);
		
		clearEnv();
		
		tag = train(fdoc);
				
		return evaluation(tag);		
		
	}
	
	
	protected void clearEnv() throws RserveException
	{
		c=getRC();
		
		c.voidEval("rm(\"tm\", \"tm.class\", \"tm.pred\", \"tm.prob\", \"tm.prob\", \"c\", \"auc\", \"roc\", \"pred\")");
	}
	
	protected rResult evaluation(String tag) throws RserveException, REXPMismatchException
	{
		c=getRC();	
		
		// evaluate performance
		c.voidEval("library(ROCR)");
		
		c.voidEval("c=table(tm.class, High.test)");
		double acc=c.eval("(c[1]+c[4])/(c[1]+c[2]+c[3]+c[4])").asDouble();
		System.out.println(tag + " accuracy: " + acc);
		
		c.voidEval("pred=prediction(tm.prob, High.test)");
		c.voidEval("auc=performance(pred, \"auc\")");
		double auc=c.eval("unlist(slot(auc, \"y.values\"))").asDouble();
		System.out.println(tag + " AUC: " + auc);
		
		c.voidEval("acc=performance(pred, \"acc\")");
		acc=c.eval("max(unlist(slot(acc, \"y.values\")))").asDouble();
		System.out.println(tag + " ACC: " + acc);
		
		c.voidEval("roc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		double[] roc_x = c.eval("unlist(slot(roc, \"x.values\"))").asDoubles();
		double[] roc_y = c.eval("unlist(slot(roc, \"y.values\"))").asDoubles();
		
		rResult ret=new rResult();
		ret.setAcc(acc);
		ret.setAuc(auc);
		ret.setX(roc_x);
		ret.setY(roc_y);
		return ret;		
	}
	
	
	protected void loadData(fDocument fdoc) throws REXPMismatchException, RserveException
	{
		
		if (isDataLoaded()==false)
		{
			System.out.println("Loading data...");
			
		}	
		else
		{
			// it's already loaded
			return;
		}
		
	    
/*		int cols = fdoc.getRawCols();    
 		fdoc.filterNames();    //get rid of the "_d" of names
	    REXP[] rexps = new REXP[cols]; 
	    
	    for (int i=0; i<cols; i++)
	    {
	    	rexps[i] = new REXPDouble(fdoc.rawColumn(i));
	    }
		//prepare dataframe
		String[] names = fdoc.getRawNames().toArray(new String[fdoc.getRawCols()]);
		REXP xdf = REXP.createDataFrame(new RList(rexps, names));*/
	    
		REXP xdf = fdoc.dataFrame();
		
	    c = getRC();
	    // clear environment
	    c.voidEval("rm(list = setdiff(ls(), lsf.str()))");		    	    
	    

		c.assign("xdf", xdf);
		
		// prepare train,test data
		c.voidEval("attach(xdf)");
		c.voidEval("High=ifelse(Sales<=8,\"No\", \"Y\")");
		c.voidEval("xdf=data.frame(xdf,High)");
		c.voidEval("set.seed(2)");
		c.voidEval("train=sample(1:nrow(xdf),floor(nrow(xdf)*4/5))");
		c.voidEval("xdf.test=xdf[-train,]");
		c.voidEval("High.test=High[-train]");
		
		// Now, other child ML models can directly use xdf, train, xdf.test, High.test
		dataLoaded = true;
	}

	
	protected void finalize() throws RserveException
	{
		//detach
		c.voidEval("detach(xdf)");
		
		// clear environment
	    c.voidEval("rm(list = setdiff(ls(), lsf.str()))");				
	}
	
	
	
}
