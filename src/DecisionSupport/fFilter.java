package DecisionSupport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class fFilter {

	private ArrayList<String> emailFilters=new ArrayList<String>();
	private ArrayList<String> interested=new ArrayList<String>();

	private String interestedQuery="";
	
	public static fFilter filter=null;
	
	public static fFilter getInstance()
	{
		if (filter==null)
		{
			filter = new fFilter();
			try {
				filter.installEmailFilter();
				filter.installInterested();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return filter;
	}
	
	private void installEmailFilter() throws IOException
	{
		FileInputStream fstream = new FileInputStream("emailsFilter.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
			emailFilters.add(strLine);
		}

		//Close the input stream
		br.close();
		
		System.out.println("Load " + Integer.toString(emailFilters.size()) + " filtered users.");
	}
	
	
	private void installInterested() throws IOException
	{
		FileInputStream fstream = new FileInputStream("interested.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
			interested.add(strLine);
			
			if (interestedQuery.length()!=0)
			{
				interestedQuery = interestedQuery + " OR ";
			}
			else
			{
				interestedQuery = " AND (";
			}
			
			interestedQuery = interestedQuery + "fromdisp:\"" + strLine.substring(0,strLine.indexOf("<")-1) + "\"";
		}
		interestedQuery = interestedQuery + ") ";
		

		//Close the input stream
		br.close();
		
		System.out.println("Load " + Integer.toString(interested.size()) + " interested users.");
	}	
	
	
	public String getInterestedQuery()
	{
		return interestedQuery;
	}
	
	
	public boolean isFiltered(String value)
	{
		return (emailFilters.contains(value));
	}
	
	public boolean isInterested(String value)
	{
		boolean ret=false;
		if (value==null)
		{
			return ret;
		}
		
		for (int i=0; i<interested.size(); i++)
		{
			try{
			ret = (interested.get(i).indexOf(value)!=-1);
			}
			catch (Exception e)
			{
				System.out.println(value);
			}
			if (ret==true)
			{
				return ret;
			}
		}
		return ret;
	}
	
	
	public String[] interestedUsers()
	{
		String[] ret=new String[interested.size()];
		
		for (int i=0; i<interested.size(); i++)
		{
			ret[i]=interested.get(i).substring(0,interested.get(i).indexOf("<")-1);
		}
		
		return ret;
	}
	
}
