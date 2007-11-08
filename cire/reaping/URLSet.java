package reaping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class URLSet {
	public boolean needsUpdateingInDatabase=false;
	private int urlSetID;
	public int rank=0;
	private String label;
	private List<Integer> userIDs= new ArrayList<Integer>();
	private List<Integer> userCount= new ArrayList<Integer>();
	private List<Boolean> isUserIDAssociatedWithSetInDatabase= new ArrayList<Boolean>();
	private List<Integer> urlIDs= new ArrayList<Integer>();
	private List<Boolean> isUrlIDAssociatedWithSetInDatabase= new ArrayList<Boolean>();
	private List<Integer> urlCount= new ArrayList<Integer>();
	private List<String> recommendations= new ArrayList<String>();
	private List<Integer> recommendationCount= new ArrayList<Integer>();
	
	public URLSet (int uRLSetID,String label,int rank,boolean newSet) {
		this.urlSetID = uRLSetID;
		this.label=label;
		this.rank=rank;
		urlIDs.add(uRLSetID);
		urlCount.add(rank);
		isUrlIDAssociatedWithSetInDatabase.add(!newSet);
		needsUpdateingInDatabase=newSet;
	}
	
	public void addUser(int userID,int userCount,boolean newUser)
	{
		//don't add if already there
		for(int i=0;i<userIDs.size();i++)
		{
			if(userIDs.get(i)==userID)
			{
				return;
			}
		}
		isUserIDAssociatedWithSetInDatabase.add(!newUser);
		userIDs.add(userID);
		this.userCount.add(userCount);
	}
	
	
	private void addUser(int userID)
	{
		//don't add if already there
		for(int i=0;i<userIDs.size();i++)
		{
			if(userIDs.get(i)==userID)
			{
				isUserIDAssociatedWithSetInDatabase.set(i, false);//this will need to be updated
				this.userCount.set(i, userCount.get(i)+1);
				return;
			}
		}
		isUserIDAssociatedWithSetInDatabase.add(false);
		userIDs.add(userID);
		this.userCount.add(1);
	}
	
	private void addUrl(int urlID)
	{
		//don't add if already there
		for(int i=0;i<urlIDs.size();i++)
		{
			if(urlIDs.get(i)==urlID)
			{
				isUrlIDAssociatedWithSetInDatabase.set(i, false);//this will need to be updated
				this.urlCount.set(i, urlCount.get(i)+1);
				return;
			}
		}
		isUrlIDAssociatedWithSetInDatabase.add(false);
		urlIDs.add(urlID);
		this.urlCount.add(1);
	}
	
	public void addUrl(int urlID,int urlCount,boolean newUrl)
	{
		//don't add if already there
		for(int i=0;i<urlIDs.size();i++)
		{
			if(urlIDs.get(i)==urlID)
			{
				return;
			}
		}
		isUrlIDAssociatedWithSetInDatabase.add(!newUrl);
		urlIDs.add(urlID);
		this.urlCount.add(urlCount);
	}
	
	public void setLabel(String label)
	{
		this.label=label;
		this.needsUpdateingInDatabase=true;	
	}
	
	public int getUserCount () {
		return this.userIDs.size();
	}
	
	public int getURLCount()
	{
		return this.urlIDs.size();
	}

	
	public int getID () {
		return this.urlSetID;
	}
	
	public List<Integer> getUrlIDs()
	{
		return urlIDs;
	}
	
	public List<Integer> getUserIDs()
	{
		return userIDs;
		
	}
	
	public void addBookmarksCommonToTwoUsers(int userID1,int userID2,List<Integer> commonBookmarks)
	{
		// if the defining bookmark is not in this set then just return.
		boolean doCommonBookmarksBelongInSet=false;
		for(int i=0;i<commonBookmarks.size();i++)
		{
			if(urlSetID==commonBookmarks.get(i))
			{
				doCommonBookmarksBelongInSet=true;
			}
		}
		
		if(!doCommonBookmarksBelongInSet)
		{
			return;
		}
		
		addUser(userID1);
		addUser(userID2);
		
		for(int i=0;i<commonBookmarks.size();i++)
		{
			addUrl(commonBookmarks.get(i));
		}
	}

	
	public synchronized void updateInDatabase(Connection conn)
	{
//update("CREATE TABLE URLSetsTabel (SetID INTEGER, Label VARCHAR(256),  UserCount INTEGER)", conn);

		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs;

			try {	
				if(needsUpdateingInDatabase)
				{
			        st = conn.createStatement();
			        st.executeUpdate("DELETE FROM URLSetsTabel WHERE SetID = "+urlSetID);
					conn.commit();
			        st.close();
			        
					ps = conn.prepareStatement(
					"INSERT INTO URLSetsTabel(SetID, Label, UserCount) VALUES(?,?,?)");
					ps.setInt(1, urlSetID);
					ps.setString(2, this.label);
					ps.setInt(3, userIDs.size());	
			        ps.executeUpdate();   
			        conn.commit();
			        ps.close();
	        
				}
				
				
//update("CREATE TABLE URLsForSets (SetID INTEGER, URLID INTEGER, Count INTEGER)", conn);
				ps = conn.prepareStatement(
    			"INSERT INTO URLsForSets(SetID, URLID, UrlCount) VALUES(?,?,?)");
				for (int i = 0 ; i < urlIDs.size(); i++) 
				{
					if(!isUrlIDAssociatedWithSetInDatabase.get(i)){
						
						ps.setInt(1, urlSetID);
						ps.setInt(2, urlIDs.get(i));	
						ps.setInt(3, urlCount.get(i));
				        ps.executeUpdate();  
				        conn.commit();
				        
				        isUrlIDAssociatedWithSetInDatabase.set(i, true);
					}
				}
				ps.close();

//update("CREATE TABLE UsersForSets (SetID INTEGER, UserID INTEGER,Count INTEGER)", conn);
				ps = conn.prepareStatement(
    			"INSERT INTO UsersForSets(SetID, UserID, UserCOUNT) VALUES(?,?,?)");
		        for(int i=0;i<userIDs.size();i++)
		        {
		        	if(!isUserIDAssociatedWithSetInDatabase.get(i))
		        	{
						
						ps.setInt(1, urlSetID);
						ps.setInt(2, userIDs.get(i));
						ps.setInt(3, userCount.get(i));
				        ps.executeUpdate();  
				        conn.commit();
				        
				        isUserIDAssociatedWithSetInDatabase.set(i, true);
		        	}
		        }
		        ps.close();
		        

			}	
			catch (Exception ex) {
				ex.printStackTrace();
			}
			finally {
				
				if (ps != null)
					try{
						ps.close();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
			}
			
			needsUpdateingInDatabase=false;
		}

}
