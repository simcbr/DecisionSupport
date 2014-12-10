package DecisionSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

/*
 * This class defines the internal data structure
 */
public class fDocument 
{
	private ArrayList<String> names = new ArrayList<String>();
	private int cols=0,rows=0;
	private Object[][] data=null;
	private String docName;
	private REXP[] rexps=null;
	
	
	
	// the purpose is to transfer a column to a factor of R, the column should be string object
	private REXPFactor colToFactor(int index)
	{
		REXPFactor ret=null;
		ArrayList<String> categories = new ArrayList<String>();
		int[] ids=new int[rows];
		
		for (int i=0; i<rows; i++)
		{
			if (categories.contains(data[i][index]) == true)
			{
				ids[i] = categories.indexOf(data[i][index]);
			}
			else
			{
				categories.add(categories.size(), (String)data[i][index]);
				ids[i] = categories.size();
			}
		}
		
		ret = new REXPFactor(ids, categories.toArray(new String[categories.size()]));
		return ret;
	}
	
	
	// build dataframe with all columns
	public REXP dataFrame() throws REXPMismatchException
	{
		
		if (rexps==null)
		{
			rexps = new REXP[cols]; 
		    
			for (int j=0; j<cols; j++)
			{
				rexps[j] = column(names.get(j), false, null);
			}				
		}
 

		String[] names = getNames().toArray(new String[getCols()]);
		REXP xdf = REXP.createDataFrame(new RList(rexps, names));
		
		return xdf;
	}	
	

	
	
	// build dataframe with selected columns
	public REXP dataFrame(ArrayList<String> choices, fFilter filter) throws REXPMismatchException
	{
		ArrayList<Integer> retRows = new ArrayList<Integer>();
		// initialize retRows with all rows
		for (int i=0; i<rows; i++)
		{
			retRows.add(i);
		}
		
		// following commented code is to collect all columns first, then select columns interested. But due to the existence of filter,
		// we have to filter selected columns separately each time.
/*		if (rexps==null)
		{
			rexps = new REXP[cols]; 
		    
			for (int j=0; j<cols; j++)
			{
				retRows = filterRows(j, filter, retRows);
				rexps[j] = column(j, false, retRows);  // change them as factor
			}				
		}
  
		REXP[] selRexps=new REXP[choices.size()];
		for (int i=0; i<choices.size(); i++)
		{
			selRexps[i] = rexps[choices.get(i)];
		}*/

		
		REXP[] selRexps=new REXP[choices.size()];
		for (int i=0; i<choices.size(); i++)
		{
			retRows = filterRows(choices.get(i), filter, retRows);
		}
		
		for (int i=0; i<choices.size(); i++)
		{
			selRexps[i] = column(choices.get(i), false, retRows);
		}
		
		
		String[] names = choices.toArray(new String[choices.size()]);
		REXP xdf = REXP.createDataFrame(new RList(selRexps, names));
		
		return xdf;		
	}	

	
	// remove uninterested rows
	private ArrayList<Integer> filterRows(String name, fFilter filter, ArrayList<Integer>retRows)
	{
		
		int index = names.indexOf(name);
		Object value = data[0][index];
		
		if (value instanceof Double)
		{
			
		}
		else if (value instanceof String)
		{
			for (int i=0; i<rows; i++)
			{
				if (filter != null)
				{

					try{
						//if (!filter.isFiltered((String)data[i][index])==false)						
						if (filter.isInterested((String)data[i][index])==false)
						{
							if (retRows.contains(i)==true)
							{
								retRows.remove(new Integer(i));
							}
						}

					}
					catch (Exception e)
					{
						System.out.println(Integer.toString(index) + ", " + Integer.toString(i));
					}
				}
			}			
		}
		else if (value instanceof Integer)
		{
			
		}
		else if (value instanceof Boolean)
		{
			
		}
		else if (value instanceof Date)
		{
			
		}
		
		return retRows;
	}

	
	
	// change to relevant REXP type 
	private REXP column(String name, boolean toFactor, ArrayList<Integer> retRows)
	{
		int index = names.indexOf(name);
		REXP ret=null;
		Object value = data[0][index];
		
		if (value instanceof Double)
		{
			double[] vect = new double[retRows.size()];
			for (int i=0; i<retRows.size(); i++)
			{
				vect[i] = (double) data[retRows.get(i)][index];
			}
			ret = new REXPDouble(vect);
		}
		else if (value instanceof String)
		{
			if (toFactor == true)
			{
				ret = colToFactor(index);
			}
			else
			{
				String[] vect = new String[retRows.size()];
				for (int i=0; i<retRows.size(); i++)
				{
					vect[i] = (String) data[retRows.get(i)][index];
				}
				ret = new REXPString(vect);
			}
			
		}
		else if (value instanceof Integer)
		{
			int[] vect = new int[retRows.size()];
			for (int i=0; i<retRows.size(); i++)
			{
				vect[i] = (int) data[retRows.get(i)][index];
			}
			ret = new REXPInteger(vect);
		}
		else if (value instanceof Boolean)
		{
			boolean[] vect = new boolean[retRows.size()];
			for (int i=0; i<retRows.size(); i++)
			{
				vect[i] = (boolean) data[retRows.get(i)][index];
			}
			ret = new REXPLogical(vect);
		}
		else if (value instanceof Date)
		{
			
			int[] vect = new int[retRows.size()];
			Calendar cal = Calendar.getInstance();
			
			for (int i=0; i<retRows.size(); i++)
			{
				vect[i] =  (int) ((cal.getTime().getTime() - ((Date) data[retRows.get(i)][index]).getTime())/(1000*60*60*24));    // the time difference in days
			}
			ret = new REXPInteger(vect);
		}
		
		
		return ret;
	}
	
	
	
	public ArrayList<String> getNames() 
	{
		return names;
	}
	
	
	// get selected fields names
	public ArrayList<String> getNames(ArrayList<Integer> choices) 
	{
		ArrayList<String> ret=new ArrayList<String>();
		
		for (int i=0; i<choices.size(); i++)
		{
			ret.add(ret.size(), names.get(choices.get(i)));
		}
		return ret;
	}
	
	
	public List<String> getRawNames() 
	{
		return (List<String>) names.subList(1, cols-1);  // exclude id and _version_
	}
	
	
	public String getName(int index)
	{
		return names.get(index);
	}
	
	public String getRawName(int index)
	{
		return getName(index+1);
	}	
	
	
	public void setNames(String[] names)
	{
		for (String name:names)
		{
			this.names.add(name);
		}
	}
	
	public void setNames(ArrayList<String> names) 
	{
		this.names = names;
	}
	
	
	public void filterNames()
	{
		// filter names to get rid of surfix
		for (int i=1; i<names.size()-1; i++)
		{
			names.set(i, names.get(i).substring(0,names.get(i).length()-2));
		}		
	}
	
	
	public void addName(String name)
	{
		this.names.add(name);
	}
	
	
	public Object[][] getData() 
	{
		return data;
	}
	
	public void setData(Object[][] data) 
	{
		this.data = data;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getCols() {
		return cols;
	}
	
	public int getRawCols() {
		return getCols()-2;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}
	
	
	public Object getRawData(int row, int col)
	{
		return data[row][col];
	}
	
}
