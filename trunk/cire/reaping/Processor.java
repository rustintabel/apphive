package reaping;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.HashMap;

public class Processor {	
	static final String DEL_ICIO_US_URL = "http://del.icio.us/";
	static final int SAVED_BY_THRESHOLD = 1000;
	static final String SAVED_URL_XPATH = "//LI[@class='post']";
	static final String SAVED_HREF_XPATH = "H4[@class='desc']/A/@href";
	static final String SAVED_OTHERS_HREF_XPATH = "DIV/A[@class='pop']/@href";
	static final String SAVED_NUMBER_OF_SAVES_XPATH = "DIV/A[@class='pop']/text()";
	static final String OTHERS_HREF_XPATH = "//A[@class='who']/@href";
	private ProcessorMessageListener processorMessengerListener;
	private ProcessorThreadStateChangedListener processorThreadStateChangedListener;
	public String username;
	public String currentMessage="";
	private HashMap<Integer,User> linkedUsers = new HashMap<Integer,User>();
	private HashMap<Integer,Url> urls = new HashMap<Integer,Url>();
	private HashMap<Integer,URLSet> sets = new HashMap<Integer,URLSet>();
	private List<String> recommendedBookmarks;

	// Constructor.
	public Processor (ProcessorMessageListener processorMessengerListener,
			ProcessorThreadStateChangedListener processorThreadStateChangedListener)  {
		this.processorMessengerListener=processorMessengerListener;
		this.processorThreadStateChangedListener=processorThreadStateChangedListener;
		Connection conn;
		try {
			Class.forName("org.hsqldb.jdbcDriver");		
	        conn = DriverManager.getConnection("jdbc:hsqldb:Processor","sa","");
	        
	        try{
	        	//update("DROP TABLE UserProfileTabel", conn);
	        	update("CREATE TABLE UserProfileTabel ( UserName VARCHAR(256), URLsUpdated DATE )", conn);
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        try{
	        	//update("DROP TABLE UserTable", conn);
	        	update("CREATE TABLE UserTable ( UserID INTEGER, UserName VARCHAR(256), SetsUpdated DATE)", conn);
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        try{
	        	//update("DROP TABLE URLTabel", conn);
	        	update("CREATE TABLE URLTabel ( URLID INTEGER, URL VARCHAR(1000), UsersPageURL VARCHAR(1000), SavedByHowManyUsers INTEGER)", conn);
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        try{
	        	//update("DROP TABLE URLUsersTabel", conn);
	        	update("CREATE TABLE URLUsersTabel ( URLID INTEGER, UserID INTEGER )", conn);
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        

	        
	        try{
	        	//update("DROP TABLE URLSetsTabel", conn);
	        	//this table should have a last updated field.
	        	update("CREATE TABLE URLSetsTabel (SetID INTEGER, Label VARCHAR(256),  UserCount INTEGER)", conn);
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        try{
	        	//update("DROP TABLE URLsForSets", conn);
	        	update("CREATE TABLE URLsForSets (SetID INTEGER, URLID INTEGER, UrlCount INTEGER)", conn);
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        try{
	        	//update("DROP TABLE UsersForSets", conn);
	        	update("CREATE TABLE UsersForSets (SetID INTEGER, UserID INTEGER, UserCount INTEGER)", conn);
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
			
			conn.close();

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
    private boolean doesTableExist(String tableName, Connection conn) throws SQLException
    {
    	 //this does not currently work
    	ResultSet rs;
    	
		rs = query("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_NAME = '"+tableName+"'",conn);
		
		if(rs.next())
		{
			return true;
		}
		else
		{
			return false;
		}
    }
	
	private void setUser(String userName, Connection conn)
	{
		this.username=userName;
		
		try {
			update("DELETE FROM UserProfileTabel",conn);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		try {
			update("DELETE FROM UserTable",conn);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		try {
			update("DELETE FROM URLTabel",conn);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		try {
			update("DELETE FROM URLUsersTabel",conn);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		try {
			update("DELETE FROM URLSetsTabel",conn);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		try {
			update("DELETE FROM URLsForSets",conn);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		try {
			update("DELETE FROM UsersForSets",conn);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		try {
			update("INSERT INTO UserProfileTabel(username) VALUES('"+userName+"')",conn);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		linkedUsers.clear();
		urls.clear();
		sets.clear();
		
	}

	// make this the only place a user is set
 	public void process(String userName) throws Exception {
 		changeProcessorThreadState(ProcessorThread.ProcessorThreadState.PROCESSING);
 		Connection conn;
		Class.forName("org.hsqldb.jdbcDriver");		
        conn = DriverManager.getConnection("jdbc:hsqldb:Processor","sa","");

 		
		//if(!getLastUser(conn).equalsIgnoreCase(userName))
		//{
			sendMessage("Warning: Reaping in progress.");
			setUser(userName, conn);
		//}
		//else
		//{
			this.username=userName;
		//}

        DOMParser parser = new DOMParser();
		
		// get the users first page.
        parser.parse(DEL_ICIO_US_URL + username);

		//extract the number of pages the user has.
		int endPage = getNumberOfPages(parser.getDocument());

		// scrape the user page to find all their links
		for (int i = 1; i <= endPage; i++) {
			processPage(DEL_ICIO_US_URL + this.username + "?page=" + i,this.username,conn);
		}

		int count = 1;
		// loop users links and find all user that have it linked as well
		User me = getUser(this.username);
		for(int i=0;i<me.getUrls().size();i++)
		{
			processOthers(urls.get(me.getUrl(i)),conn);

			sendMessage("Processing bookmark number "+count);
			changeProcessorThreadState(ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGUSER);

			updateURLs(conn);
			updateUsers(conn);
			count++;
		}

    	changeProcessorThreadState(ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGUSERS);

    	processSets(conn);
    	
    	changeProcessorThreadState(ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGSETS);
    	
    	conn.close();
    	changeProcessorThreadState(ProcessorThread.ProcessorThreadState.STOPPED);
    }
 	
 	public void updateURLs(Connection conn)
 	{
		HashMap<Integer,Url> tempUrls= getURLs(conn);
		Collection<Url> tempUrlsCollection=tempUrls.values();
		Iterator<Url> tempUrlsIterator=tempUrlsCollection.iterator();
		Url currentUrl;
 		for(int i=0;i<tempUrlsCollection.size();i++)
 		{
 			currentUrl=tempUrlsIterator.next();
 			currentUrl.updateInDatabase(conn);
 		}
 	}
 	
 	public List<String> getRecommedation()
 	{
 		return recommendedBookmarks;
 	}
 	
 	public void updateUsers(Connection conn)
 	{
		HashMap<Integer,User> users= getUsers(conn);
		Collection<User> tempUsers=users.values();
		Iterator<User> tempUsersIterator=tempUsers.iterator();
		User currentUser;
 		for(int i=0;i<tempUsers.size();i++)
 		{
 			currentUser=tempUsersIterator.next();
 			currentUser.updateInDatabase(conn);
 		}
 	}
 	
 	public void updateSets(Connection conn)
 	{
		HashMap<Integer,URLSet> tempSets= getSets(conn);
		Collection<URLSet> tempSetsCollection=tempSets.values();
		Iterator<URLSet> tempSetsIterator=tempSetsCollection.iterator();
		URLSet currentSet;
 		for(int i=0;i<sets.size();i++)
 		{
 			currentSet=tempSetsIterator.next();
 			currentSet.updateInDatabase(conn);
 		}
 	}
 	
 	public User getUser(String userName)
 	{
 		for(int i=0;i<linkedUsers.values().size();i++)
 		{
 			if(linkedUsers.get(i).getUsername().equals(userName))
 			{
 				return linkedUsers.get(i);
 			}
 		}

 		return null;
 	}
 	
 	public Url getURL(String urlString)
 	{
 		Url url=null;
 		
 		for(int i=0;i<urls.values().size();i++)
 		{
 			if(urls.get(i).getUrl().equals(urlString))
 			{
 				return urls.get(i);
 			}
 		}
 		
 		return url;
 	}
	
	// scrape the users page for their links.
	private void processPage (String url,String userName,Connection conn) throws Exception {
		DOMParser parser = new DOMParser();
        User currentUser;
        parser.parse(url);
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		// xpath to extract the LI that the users urls are in.
		XPathExpression expr = xpath.compile(SAVED_URL_XPATH);
		Object result = expr.evaluate(parser.getDocument(), XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		currentUser= getUser(userName);
		if(currentUser==null)
		{
	 		int tempID=linkedUsers.values().size();
	 		currentUser=new User(username,tempID,true);
	 		linkedUsers.put(tempID,currentUser);
		}
		// loop thru eachs users link
		for (int i = 0; i < nodes.getLength(); i++) {
			processNode(nodes.item(i),currentUser,conn);
		}
		

	}
	
	// find sets.
	private void processSets (Connection conn){
		//update("CREATE TABLE UserProfileTabel ( UserName VARCHAR(256)), URLsUpdated DATE )", conn);
		//update("CREATE TABLE UserTable ( UserID INTEGER, UserName VARCHAR(256), SetsUpdated DATE)", conn);
		//update("CREATE TABLE URLTabel ( URLID INTEGER, URL VARCHAR(1000), UsersPageURL VARCHAR(1000))", conn);
		//update("CREATE TABLE URLUsersTabel ( URLID INTEGER, UserID INTEGER )", conn);
		//update("CREATE TABLE URLSetsTabel (SetID INTEGER, Label VARCHAR(256),  UserCount INTEGER)", conn);
		//update("CREATE TABLE URLsForSets (SetID INTEGER, URLID INTEGER)", conn);
		//update("CREATE TABLE UsersForSets (SetID INTEGER, UserID INTEGER)", conn);
		List<Integer> commonBookmarks= new ArrayList<Integer>();
		sets.clear();
		
		for(int i=0;i<urls.values().size();i++)
		{
			URLSet tempSet=new URLSet(i,"",1,true);
			tempSet.addUser(0, 1, true);
			sets.put(i,tempSet);
		}
		
		try{
			
			if(linkedUsers.values().size()<3)
			{
				return;
			}
			//compare the users bookmarks to each other user and set if common bookmarks are found
			//check to see that it does not already exist if it does simply add the user to the sets users
			//otherwise make new set ither with the username of the current user and another user or
			//with the username of the current user and the users of the intersecting set.
			for(int i=1;i<linkedUsers.values().size();i++)
			{
				for(int j=i+1;j<linkedUsers.values().size();j++)
				{

					commonBookmarks=getCommonBookmarks(linkedUsers.get(i).getUrls(),linkedUsers.get(j).getUrls());
					
					if(commonBookmarks.size()>0)
					{
						for(int k=0;k<sets.values().size();k++)
						{
							
							URLSet tempSet=sets.get(k);
							
							tempSet.addBookmarksCommonToTwoUsers(
									linkedUsers.get(i).getId(), linkedUsers.get(j).getId(), commonBookmarks);
							
							sets.put(tempSet.getID(),tempSet);
							
						}
					}
				}
				
				sendMessage("Checked user "+linkedUsers.get(i).getUsername()+" for groups.");
				//changeProcessorThreadState(ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGSET);
				
			}
			updateSets(conn);
			sendMessage("Finished finding categories.");
			changeProcessorThreadState(ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGSETS);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void computeSetRanks()
	{
		int maxUsers=maxSetsUserCount();
		int maxURLCount=maxSetsURLCount();
		URLSet tempURLSet;
		for(int i=0;i<sets.values().size();i++)
		{
			tempURLSet=sets.get(i);
			tempURLSet.rank=tempURLSet.getURLCount();
				//(int)(((double)tempURLSet.getUserCount()/(double)maxUsers)*100.0+
				//	((double)tempURLSet.getURLCount()/(double)maxURLCount)*100.0);
		}
	}
	
	public int maxSetsUserCount()
	{
		Collection<URLSet> tempSets=sets.values();
		Iterator<URLSet> tempSetsIterator=tempSets.iterator();
		URLSet tempSet;
		int max=0;
		for(int i=0;i<sets.values().size();i++)
		{
			tempSet=tempSetsIterator.next();
			if(tempSet.getUserCount()>max)
			{
				max=tempSet.getUserCount();
			}
		}
		return max;
	}
	
	public int maxSetsURLCount()
	{
		Collection<URLSet> tempSets=sets.values();
		Iterator<URLSet> tempSetsIterator=tempSets.iterator();
		URLSet tempSet;
		int max=0;
		for(int i=0;i<sets.values().size();i++)
		{
			tempSet=tempSetsIterator.next();
			if(tempSet.getURLCount()>max)
			{
				max=tempSet.getURLCount();
			}
		}
		return max;
	}
	
	public List<Integer> getCommonBookmarks(List<Integer> bookmarkIDList1, List<Integer> bookmarkIDList2)  
	{
		List<Integer> commonBookmarkIDs= new ArrayList<Integer>();


		for(int k=0;k<bookmarkIDList1.size();k++)
		{
			for(int l=0;l<bookmarkIDList2.size();l++)
			{
				
				if(bookmarkIDList1.get(k)==bookmarkIDList2.get(l)
						&&!urls.get(bookmarkIDList1.get(k)).equals("") )
				{
					commonBookmarkIDs.add(bookmarkIDList1.get(k));
				}
			}
		}
		
		return commonBookmarkIDs;
	}
	
	
	
	public void sendMessage(String message)
	{
		processorMessengerListener.messageSent(new ProcessorMessageEvent(this,message));	
	}
	
	public void changeProcessorThreadState(ProcessorThread.ProcessorThreadState state)
	{
		processorThreadStateChangedListener.processorThreadstateChanged(new ProcessorThreadStateChangedEvent(this,
				state));
	}

	// etract the users url.
	private void processNode(Node node,User user,Connection conn) throws Exception {
		int tempURLID=0;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		// extract the url
		XPathExpression expr = xpath.compile(SAVED_HREF_XPATH);
		String urlString = expr.evaluate(node);		
		
		// extract the url that has the other users who have linked as well.
		expr = xpath.compile(SAVED_OTHERS_HREF_XPATH);
		String linkedUrl = expr.evaluate(node);

		expr = xpath.compile(SAVED_NUMBER_OF_SAVES_XPATH);
		String saved_by = expr.evaluate(node);
		int savedBy = getSavedBy(saved_by);
		Url url= getURL(urlString);
		
		if(url==null)
		{
			url=new Url(urlString,linkedUrl,urls.values().size(),savedBy,true);
			urls.put(url.getURLID(), url);
		}
		
		user.addUrl(url.getURLID(), true);
		linkedUsers.put(user.getId(), user);
		sendMessage("URL Processed "+urlString);
	}

	private void processOthers (Url url,Connection conn) {
		try {
			DOMParser parser = new DOMParser();
        
			if(url.GetHowManyOtherUsersHaveSavedThisURL()<0)
			{
				return;
			}
			
			if (url.GetHowManyOtherUsersHaveSavedThisURL() < SAVED_BY_THRESHOLD) {
	        	parser.parse(DEL_ICIO_US_URL +url.getUsersPageURL() + "?all");
			}
			else {
				parser.parse(DEL_ICIO_US_URL + url.getUsersPageURL());
			}

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			XPathExpression expr = xpath.compile(OTHERS_HREF_XPATH);
			Object result = expr.evaluate(parser.getDocument(), XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;

			for (int i = 0; i < nodes.getLength(); i++) {
				String currentUserName=nodes.item(i).getNodeValue();
				currentUserName=currentUserName.replace("/", "");
				User currentUser=getUser(currentUserName);
				
				if(currentUserName.equals(this.username))
				{
					continue;
				}

				if(currentUser==null)
				{
			 		int tempID=linkedUsers.values().size();
			 		currentUser=new User(currentUserName,tempID,true);
			 		linkedUsers.put(tempID,currentUser);
				}

				currentUser.addUrl(url.getURLID(),true);
				linkedUsers.put(currentUser.getId(),currentUser);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void getBookmarksForSet(int setID)
	{
 		changeProcessorThreadState(ProcessorThread.ProcessorThreadState.PROCESSING);
 		User tempUser;
		recommendedBookmarks=new ArrayList<String>();
		String currentUser="";
 
 		try{
			Class.forName("org.hsqldb.jdbcDriver");		
			Connection conn = DriverManager.getConnection("jdbc:hsqldb:Processor","sa","");
 			for(int i=0;i<sets.get(setID).getUserIDs().size();i++)
 			{
 				currentUser=linkedUsers.get(sets.get(setID).getUserIDs().get(i)).getUsername();

				//first check if this method has been run on this set
				//recently and get from the database if so.
				//also check that each user has not been updated recently befor getting thier bookmarks again.
				//get the bookmarks for each user of the set.
			    DOMParser parser = new DOMParser();
				
				// get the users first page.
			    parser.parse(DEL_ICIO_US_URL + currentUser);
			
				//extract the number of pages the user has.
				int endPage = getNumberOfPages(parser.getDocument());
			
				if(endPage>10)
				{
					continue;
				}
				// scrape the user page to find all their links
				for (int j = 1; j <= endPage; j++) {
					processPage(DEL_ICIO_US_URL + currentUser + "?page=" + j,currentUser,conn);
				}
				
				tempUser=getUser(currentUser);
				for(int k=0;k<tempUser.getUrls().size();k++)
				{
					recommendedBookmarks.add(urls.get(tempUser.getUrls().get(k)).getUrl());
				}
				//then give every bookmark a rank for this set that is the number of 
				//users in this set that have that bookmark
				//this can go in the set object as a list of bookmarkids and a list of ranks
				//this can also be stored in the database in a new table called recomended bookmarks having the following fields
				//int recommended bookmarkid, url, userurl,setid
				//sort the bookmarks by rank and return.
				updateURLs(conn);
 			}
 			conn.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}

 		changeProcessorThreadState(ProcessorThread.ProcessorThreadState.STOPPED);
	}

	private int getNumberOfPages(Document document) throws Exception{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		XPathExpression expr = xpath.compile("//P[@class='pager']");
		String pager = expr.evaluate(document);	

		Pattern p = Pattern.compile("\\sof\\s\\d+");
 		Matcher m = p.matcher(pager);

		if (m.find()) {
			pager = pager.substring(m.start(),m.end());
			p = Pattern.compile("\\d+");
			m = p.matcher(pager);
			if (m.find()) {
				pager = pager.substring(m.start(),m.end());
			}
		}

		try {
			Integer endPage = new Integer(pager);
			return endPage.intValue();
		}
		catch (Exception ex) {
			return 1;
		}
	}

	private int getSavedBy(String savedBy) throws Exception{		
		Pattern p = Pattern.compile("\\s\\d+\\s");
 		Matcher m = p.matcher(savedBy);
		String saved_by = null;

		if (m.find()) {
			saved_by = savedBy.substring(m.start(),m.end());			
		}

		try {
			Integer endPage = new Integer(saved_by.trim());
			return endPage.intValue();
		}
		catch (Exception ex) {
			return 1;
		}
	}

	public void print(Node node, String indent) {
        System.out.println(indent+node.getClass().getName()+node.getNodeName() );
        Node child = node.getFirstChild();
        while (child != null) {
            print(child, indent+" ");
            child = child.getNextSibling();
        }
    }
	
	public HashMap<Integer,User> getUsers(Connection conn) {
		
		//update("CREATE TABLE UserTable ( UserID INTEGER IDENTITY, UserName VARCHAR(256), SetsUpdated DATE)", conn);
		//update("CREATE TABLE URLUsersTabel ( URLID INTEGER, UserID INTEGER )", conn);
        int tempUserID=0;
        int tempURLID=0;
        String tempUserName="";
        User tempUser;
		ResultSet rs = null;
		if(linkedUsers.values().size()>0)
		{
			return linkedUsers;
		}
		try {
			//get the users
			rs = query("Select UserID, UserName  From UserTable ",conn);

			while(rs.next())
			{
				tempUserID=rs.getInt("UserID");
				tempUserName=rs.getString("UserName");
				tempUser=new User(tempUserName,tempUserID,false);
				linkedUsers.put(tempUserID,tempUser);
			}
			
			//get the users for the urls and add them to the appropriate user.
			rs = query("Select URLID, UserID  From URLUsersTabel ",conn);
			while(rs.next())
			{
				tempUserID=rs.getInt("UserID");
				tempURLID=rs.getInt("URLID");
				linkedUsers.get(tempUserID).addUrl(tempURLID,false);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (rs != null)
				try{
					rs.close();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
		}

		return linkedUsers;
	}
	
	public HashMap<Integer,Url> getURLs(Connection conn)
	{
//		update("CREATE TABLE URLTabel ( URLID INTEGER IDENTITY, URL VARCHAR(1000), UsersPageURL VARCHAR(1000))", conn);
        int tempURLID=0;
        int tempSavedByHowManyUsers=0;
        String tempURL="";
        String tempUsersPageURL="";
        Url tempUrl;
		ResultSet rs = null;
		if(urls.values().size()>0)
		{
			return urls;
		}
		try {
			rs = query("Select URLID, URL, UsersPageURL,SavedByHowManyUsers  From URLTabel ",conn);

			while(rs.next())
			{
				tempURLID=rs.getInt("URLID");
				tempURL=rs.getString("URL");
				tempUsersPageURL=rs.getString("UsersPageURL");
				tempSavedByHowManyUsers=rs.getInt("SavedByHowManyUsers");
				tempUrl=new Url(tempURL,tempUsersPageURL,tempURLID,tempSavedByHowManyUsers,false);
				urls.put(tempURLID,tempUrl);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (rs != null)
				try{
				rs.close();
				}catch(Exception ex)
				{
					ex.printStackTrace();
				}
		}

		return urls;
	}
	
	public HashMap<Integer,URLSet> getSets(Connection conn){

		ResultSet setsResultSet = null;
		ResultSet urlsResultSet = null;
		ResultSet usersResultSet = null;
		int tempId=0;
		String tempLabel="";
		int tempUserCount=0;
		int tempSetRank=0;
		URLSet tempSet;
		List<Integer> tempUrlIDs;
		
		if(sets.values().size()>0)
		{
			return sets;
		}
		try {
//			update("CREATE TABLE URLSetsTabel (SetID INTEGER, Label VARCHAR(256),  UserCount INTEGER)", conn);
			//get all sets
			setsResultSet = query("Select SetID, Label, UserCount From URLSetsTabel",conn);
			while (setsResultSet.next()) {
				tempId=setsResultSet.getInt("SetID");
				tempLabel=setsResultSet.getString("Label");
				tempSetRank=setsResultSet.getInt("UserCount");
				
//				update("CREATE TABLE URLsForSets (SetID INTEGER, URLID INTEGER, Count INTEGER)", conn);				
				//get the urls for this set
				tempUrlIDs= new ArrayList<Integer>();
				urlsResultSet = query("Select URLID, UrlCount From URLsForSets where SetID = "+tempId,conn);
				

				tempSet = new URLSet(tempId ,tempLabel,tempSetRank, false);
				
				while (urlsResultSet.next()) {
					tempSet.addUrl(urlsResultSet.getInt("URLID"),urlsResultSet.getInt("UrlCount") ,false);
				}
				
				
//				update("CREATE TABLE UsersForSets (SetID INTEGER, UserID INTEGER,Count INTEGER)", conn);				
				//get the users for this set
				usersResultSet = query("Select UserID, UserCount From UsersForSets where SetID = "+tempId,conn);
				while (usersResultSet.next()) {
					tempSet.addUser(usersResultSet.getInt("UserID"),usersResultSet.getInt("UserCount"), false);
				}
				
				sets.put(tempId,tempSet);
			}

			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (setsResultSet != null)
				try{
					setsResultSet.close();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
		}

		return sets;
	}

	
    public void shutdown(Connection conn) throws SQLException {

        Statement st = conn.createStatement();

        // db writes out to files and performs clean shuts down
        // otherwise there will be an unclean shutdown
        // when program ends
        st.execute("SHUTDOWN");
        conn.close();    // if there are no other open connection
    }
    
    public String getLastUser(Connection conn)
    {

        
    	ResultSet rs = null;
    	String currentUser=null;
    	try{
    		rs=query("SELECT UserName FROM UserProfileTabel",conn);
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	
    	try
    	{
    		rs.next();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	try
    	{
    		
    		currentUser=rs.getString("UserName");
    	}
    	catch(SQLException e)
    	{
    		if(e.getMessage().equalsIgnoreCase("No data is available"))
    		{
    			currentUser="";
    		}
    	}

    	this.username=currentUser;
    	return currentUser;
    }

	//use for SQL command SELECT
    private synchronized ResultSet query(String expression,Connection conn) throws SQLException {

        Statement st = null;
        ResultSet rs = null;

        st = conn.createStatement();         // statement objects can be reused with

        // repeated calls to execute but we
        // choose to make a new one each time
        rs = st.executeQuery(expression);    // run the query

		
        // do something with the result set.
        //dump(rs);
        st.close();    // NOTE!! if you close a statement the associated ResultSet is
		return rs;
        // closed too
        // so you should copy the contents to some other object.
        // the result set is invalidated also  if you recycle an Statement
        // and try to execute some other query before the result set has been
        // completely examined.
    }

	//use for SQL commands CREATE, DROP, INSERT and UPDATE
    private synchronized void update(String expression,Connection conn) throws SQLException {

        Statement st = null;

        st = conn.createStatement();    // statements

        int i = st.executeUpdate(expression);    // run the query

        if (i == -1) {
            System.out.println("db error : " + expression);
        }

		conn.commit();
        st.close();
    }    // void update()

    
}


