

import java.io.IOException;

import java.io.PrintWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.simple.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/TTServlet")
public class TTServlet extends HttpServlet 
{
	//private static final long serialVersionUID = 1L;
	private PrintWriter out;

	public void init(ServletConfig config) throws ServletException 
	{
		//TODO load sql data
	}


	public void destroy() 
	{
		//TODO close sql connection
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		out = response.getWriter();
		//out.println("<h1>" + "Yep" + "</h1>");
				

		response.setContentType("text/html");
		
		//out.println("Starting");
		if(request.getParameter("request") != null)
		{
		switch(request.getParameter("request").toLowerCase())
		{
			case "storekeg": 
				if(request.getParameter("lat") != null && request.getParameter("lon") != null && request.getParameter("id") != null && request.getParameter("kegid") != null)
				{
					Double inLat = Double.parseDouble(request.getParameter("lat"));
					Double inLong = Double.parseDouble(request.getParameter("lon"));
					int id = Integer.parseInt(request.getParameter("id"));
					String inkegID = request.getParameter("kegid");

					storeKeg(id, inkegID, inLat, inLong);
				}
			break;
			
			case "storelocation": 
				if(request.getParameter("lat") != null && request.getParameter("lon") != null && request.getParameter("id") != null)
				{
					Double inLat = Double.parseDouble(request.getParameter("lat"));
					Double inLong = Double.parseDouble(request.getParameter("lon"));
					int id = Integer.parseInt(request.getParameter("id"));
					if(request.getParameter("name") == null)
					{
						storeLocation(id, inLat, inLong, null);
						//out.println("starting null insert");
					}
					else
					{
						String name = request.getParameter("name");
						storeLocation(id, inLat, inLong, name);
						//out.println("starting " + name + " insert");
					}
				}
			break;
			
			case "getinitlocations":
				if(request.getParameter("count") != null)
				{
					//out.print("Time " + request.getParameter("count"));
					getInitLocations(request.getParameter("id"), request.getParameter("count"));
				}
			break;
			
			case "getlocationswithin":
				double inCentreLat = Double.parseDouble(request.getParameter("lat"));
				double inCentreLon = Double.parseDouble(request.getParameter("lon"));
				double inRadius = Double.parseDouble(request.getParameter("radius"));
				getLocationsWithin(request.getParameter("id"), inCentreLat, inCentreLon, inRadius);
			break;
			
			case "getkegslastlocations":
				getKegsLastLocations();
				break;
			
			case "getitemids":
				//out.print("Attempting To Get Ids");
				getItemIds();
			break;
			
			case "getkegslastlocationsonadate":
				if(request.getParameter("date") != null)
				{
					String date = request.getParameter("date");
					getKegsLastLocationsOnADate(date);
				}
			break;
			
			case "getkegsalllocationsonadate":
				if(request.getParameter("date") != null)
				{
					String date = request.getParameter("date");
					getAllKegsLocationsOnADate(date);
				}
			break;
		}
		}
		else
		{
			out.print("No request received");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private void storeLocation(int inID, double inLat, double inLon, String inName)
	{
		// JDBC driver name and database URL
	    final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	    final String DB_URL="jdbc:mysql://localhost/truckytrackdatabase";
	    
	    //  Database credentials
	    final String USER = "user";
	    final String PASS = "";
	    Statement stmt = null;
	    Connection conn = null;
	    
	    int count = 0;
	    
	    try
	    {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // Open a connection
	         conn = DriverManager.getConnection(DB_URL, USER, PASS);

	         // Execute SQL query
	         stmt = conn.createStatement();
	         String sql;
	         sql = "SELECT DISTINCT id FROM IDs;";
	         ResultSet rs = stmt.executeQuery(sql);
	         
	         ArrayList<Integer> returnIds = new ArrayList<Integer>();
	         Boolean idExists = false;
	         


	         // Extract data from result set
	         while(rs.next())
	         {
	        	 count++;
	            //Retrieve by column name
	        	 //out.print("checking id " + rs.getInt("id"));
	        	 int currentID = rs.getInt("id");
	        	 //out.println("  &: id " + currentID + " is a to " + inID);
	        	 if(currentID == inID)
	        	 {
	        		 //out.println("id " + currentID + " is equal to " + inID);
	        		 idExists = true;
	        		 break;
	        	 }
	         }
	         
	         //If id does not currently exist in ids table then add a new id
	         //to the table and then return it to the driver which will then store the id as their app's new id.
	         if(!idExists)
	         {
	        	inID = count + 1;
	        	if(inName == null)
	        	{
	        		//out.print(inID);
	        		sql = "INSERT INTO IDs VALUES (" + inID + ", '" + inID + " - Truck " + inID + "');";
	        		
	        		//out.print("not exists with no name " + sql);
	        	}
	        	else
	        	{
	        		inName = inName.replace("'", " ");
	        		inName = inName.replace("_", " ");
	        		//replaces any occurances of ' as ' marks the end and start of varchars in mysql and would disrupt the sql statement.
	        	
	        		sql = "INSERT INTO IDs VALUES (" + inID + ", '" + inID + " - " + inName + "');";
	        		//out.print("not exists " + sql);
	        	}
	        	stmt.executeUpdate(sql);
	        	JSONArray jArray = new JSONArray();
	        	JSONObject obj = new JSONObject();
	        	obj.put("id", inID);
	        	jArray.add(obj);
	        	out.println(jArray);
	         }
	         //if id does exist, then update the ids table with the new name that was just entered on the app.
	         else
	         {
	        	if(inName != null)
		        {
		        	inName = inName.replace("'", " ");
		        	inName = inName.replace("_", " ");
		        	//replaces any occurances of ' as ' marks the end and start of varchars in mysql and would disrupt the sql statement.
		        
		        	sql = "UPDATE IDs SET name='" + inID + " - " + inName + "' WHERE id = " + inID + ";";
		        	//out.print("not exists " + sql);
		        }
		        stmt.executeUpdate(sql);
	         }
	         
	         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	         java.util.Date currentTimeStamp = new java.util.Date();

	         sql = "INSERT INTO Locations (id, lat, lon, timestamp, Note1, Note2) VALUES (" + inID + ", " + inLat + ", " + inLon + ", '" + dateFormat.format(currentTimeStamp) + "', '', '');";
	         //out.println(sql);
	         stmt.executeUpdate(sql);
	         //out.print("insert is " + sql);
	         
	         //out.println("Insert complete.");

	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	    }
	    catch(SQLException se)
	    {
	         //Handle errors for JDBC
	         se.printStackTrace();
	    }
	    catch(Exception e)
	    {
	         //Handle errors for Class.forName
	         e.printStackTrace();
	    }
	    finally
	    {
	         //finally block used to close resources
	         try
	         {
	            if(stmt!=null){stmt.close();};
	         }
	         catch(SQLException se2)
	         {
	         }// nothing we can do
	         try
	         {
	            if(conn!=null){conn.close();}
	         }
	         catch(SQLException se)
	         {
	            se.printStackTrace();
	         }//end finally try
	     } //end try
	}
	
	//updates a keg's data in the database when it has been scanned in the app.
	private void storeKeg(int inID, String inKegID, double inLat, double inLon)
	{
		// JDBC driver name and database URL
	    final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	    final String DB_URL="jdbc:mysql://localhost/truckytrackdatabase";
	    
	    //  Database credentials
	    final String USER = "user";
	    final String PASS = "";
	    Statement stmt = null;
	    Connection conn = null;
	    
	    int count = 0;
	    boolean kegIsFull = true;
	    
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date currentTimeStamp = new java.util.Date();
	    
	    try
	    {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // Open a connection
	         conn = DriverManager.getConnection(DB_URL, USER, PASS);

	         // Execute SQL query
	         stmt = conn.createStatement();
	         String sql;
	         sql = "SELECT DISTINCT id FROM IDs;";
	         ResultSet rs = stmt.executeQuery(sql);
	         
	         ArrayList<Integer> returnIds = new ArrayList<Integer>();
	         boolean idExists = false;
	         


	         // Extract data from result set
	         while(rs.next())
	         {
	        	 count++;
	            //Retrieve by column name
	        	 //out.print("checking id " + rs.getInt("id"));
	        	 int currentID = rs.getInt("id");
	        	 //out.println("  &: id " + currentID + " is a to " + inID);
	        	 if(currentID == inID)
	        	 {
	        		 //out.println("id " + currentID + " is equal to " + inID);
	        		 idExists = true;
	        		 break;
	        	 }
	         }
	         
	         if(idExists)
	         {
	        	 //Execute SQL query
	        	 //[Get full and empty ids from KegKeys]
	        	 String currentFullID;
	        	 String currentEmptyID;
	         	sql = "SELECT DISTINCT fullID, emptyID FROM KegKeys WHERE fullID='" + inKegID + "' OR emptyID='" + inKegID + "';";
	         	//out.println(sql);
	         	rs = stmt.executeQuery(sql);
	         
	         	ArrayList<Integer> returnKegIds = new ArrayList<Integer>();
	         	boolean kegIdExists = false;

	         	// Extract data from result set
	         	while(rs.next())
	         	{
	         		//Retrieve by column name
	         		//out.print("checking id " + rs.getInt("id"));
	         		currentFullID = rs.getString("fullID");
	         		currentEmptyID = rs.getString("emptyID");
	         		//out.println("  &: id " + currentID + " is a to " + inID);
	         		kegIdExists = true;
	         		if(currentFullID.matches(inKegID))
	         		{
	         			//out.println("id " + currentID + " is equal to " + inID);
	         			kegIsFull = true;
	         			break;
	         		}
	         		else if(currentEmptyID.matches(inKegID))
	         		{
	         			kegIsFull = false;
	         			break;
	         		}	
	         	}
	         	//[/Get full and empty ids from KegKeys]
	         
	         	//Execute SQL query
	         	//[Determine whether or no keg has been pickedUp or not] 
	         	boolean kegIsInKegDrivers = false;
	         	sql = "SELECT DISTINCT kegID FROM KegDrivers WHERE (kegID = '" + inKegID + "');";
	         	rs = stmt.executeQuery(sql);
	         	
	         	if(rs.next())
	         	{
	         		kegIsInKegDrivers = true;
	         	}	
	         	//[/Determine whether or no keg has been pickedUp or not]
	         
	         	//[Update History Table and the table of keg currently being transported by drivers]
	         	if(idExists && kegIdExists)
	         	{
	         		if(kegIsInKegDrivers)
	         		{
	         			//keg is being dropped off and is therefore removed from KegDrivers.
	         			sql = "DELETE FROM KegDrivers WHERE (kegID = '" + inKegID + "');";
	         			//out.println(sql);
	         			stmt.executeUpdate(sql);
	         		
	         			sql = "UPDATE KegHistory SET DroppedAtLat=" + inLat + ", DroppedAtLon=" + inLon + ", DroppedAtTime='" + dateFormat.format(currentTimeStamp) + "' WHERE (id = '" + inKegID + "' AND DroppedAtTime IS NULL);";
	         			//out.println(sql);
	         			stmt.executeUpdate(sql);
	         		}
	         		else
	         		{
	         			//keg is being picked up and is therefore added to Kegdrivers.
	         			sql = "INSERT INTO KegDrivers (DriverID, KegID) VALUES (" + inID + ", '" + inKegID + "');";
	         			//out.println(sql);
	         			stmt.executeUpdate(sql);
	         			
	         			sql = "INSERT INTO KegHistory (id, DriverID, PickedUpAtLat, PickedUpAtLon, PickedUpTime) VALUES ('" + inKegID + "', " + inID + ", " + inLat + ", " + inLon + ", '" + dateFormat.format(currentTimeStamp) + "');";
	         			//out.println(sql);
	         			stmt.executeUpdate(sql);
	         		}

	        	 
	         }
	         //[/Update History Table and the table of keg currently being transported by drivers]
	         }
	         
	         
	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	         
	         out.println("Done Storing Keg");
	    }
	    catch(SQLException se)
	    {
	         //Handle errors for JDBC
	         se.printStackTrace();
	    }
	    catch(Exception e)
	    {
	         //Handle errors for Class.forName
	         e.printStackTrace();
	    }
	    finally
	    {
	         //finally block used to close resources
	         try
	         {
	            if(stmt!=null){stmt.close();};
	         }
	         catch(SQLException se2)
	         {
	         }// nothing we can do
	         try
	         {
	            if(conn!=null){conn.close();}
	         }
	         catch(SQLException se)
	         {
	            se.printStackTrace();
	         }//end finally try
	     } //end try
	}
	
	private void getItemIds()
	{
		ArrayList<String> returnNames = new ArrayList<String>();
		ArrayList<Integer> returnIds = new ArrayList<Integer>();
		// JDBC driver name and database URL
	    final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	    final String DB_URL="jdbc:mysql://localhost/truckytrackdatabase";
	    
	    //  Database credentials
	    final String USER = "user";
	    final String PASS = "";
	    Statement stmt = null;
	    Connection conn = null;
	    
	    try
	    {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // Open a connection
	         conn = DriverManager.getConnection(DB_URL, USER, PASS);

	         // Execute SQL query
	         stmt = conn.createStatement();
	         String sql;
	         sql = "SELECT id, name FROM IDs;";
	         ResultSet rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getInt("id"));
	            returnNames.add(rs.getString("name"));
	            	            
	            //Display values
	            
	         }
	         
	         JSONArray jsonOut = new JSONArray();
	         int i = 0;
	         for(int aID: returnIds)
	         {
	        	 JSONObject obj = new JSONObject();
	        	 obj.put("id", aID);
	        	 obj.put("name", returnNames.get(i));
	        	 jsonOut.add(obj);
	        	 i++;
	         }
	         //out.println("Returning IDS");
	         out.println(jsonOut);

	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	    }
	    catch(SQLException se)
	    {
	         //Handle errors for JDBC
	         se.printStackTrace();
	    }
	    catch(Exception e)
	    {
	         //Handle errors for Class.forName
	         e.printStackTrace();
	    }
	    finally
	    {
	         //finally block used to close resources
	         try
	         {
	            if(stmt!=null){stmt.close();};
	         }
	         catch(SQLException se2)
	         {
	         }// nothing we can do
	         try
	         {
	            if(conn!=null){conn.close();}
	         }
	         catch(SQLException se)
	         {
	            se.printStackTrace();
	         }//end finally try
	     } //end try
	}

	private void getInitLocations(String inID, String inCount)
	{
		ArrayList<String> returnNotes1 = new ArrayList<String>();
		ArrayList<String> returnNotes2 = new ArrayList<String>();
		ArrayList<Double> returnLats = new ArrayList<Double>();
		ArrayList<Double> returnLons = new ArrayList<Double>();
		ArrayList<java.util.Date> returnTimeStamps = new ArrayList<java.util.Date>();
		ArrayList<Integer> returnIds = new ArrayList<Integer>();
		// JDBC driver name and database URL
	    final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	    final String DB_URL="jdbc:mysql://localhost/truckytrackdatabase";
	    
	    //  Database credentials
	    final String USER = "user";
	    final String PASS = "";
	    Statement stmt = null;
	    Connection conn = null;
	    
	    //out.println("InitLocations");
	    
	    try
	    {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // Open a connection
	         conn = DriverManager.getConnection(DB_URL, USER, PASS);

	         // Execute SQL query
	         stmt = conn.createStatement();
	         String sql;
	         sql = "select * from Locations WHERE id=" + inID + " ORDER BY timestamp ASC Limit " +  inCount + ";";
	         //out.println(sql);
	         ResultSet rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getInt("id"));
	            returnLats.add(rs.getDouble("lat"));
	            returnLons.add(rs.getDouble("lon"));
	            java.util.Date adate = rs.getTimestamp("timestamp");
	            returnTimeStamps.add(adate);
	            returnNotes1.add(rs.getString("Note1"));
	            returnNotes2.add(rs.getString("Note2"));
	            	            
	            //Display values
	            
	         }
	         
	         JSONArray jsonOut = new JSONArray();
	         int i = 0;
	         for(int aID: returnIds)
	         {
	        	 JSONObject obj = new JSONObject();
	        	 obj.put("id", aID);
	        	 obj.put("lat", returnLats.get(i));
	        	 obj.put("lon", returnLons.get(i));
	        	 
	        	 obj.put("Note1", returnNotes1.get(i));
	        	 obj.put("Note2", returnNotes2.get(i));
	        	 
	        	 DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	        	 obj.put("timestamp", format.format(returnTimeStamps.get(i)));
	        	       	 
	        	 jsonOut.add(obj);
	        	 i++;
	         }
	         //out.println("Returning IDS");
	         out.println(jsonOut);

	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	    }
	    catch(SQLException se)
	    {
	         //Handle errors for JDBC
	         se.printStackTrace();
	    }
	    catch(Exception e)
	    {
	         //Handle errors for Class.forName
	         e.printStackTrace();
	    }
	    finally
	    {
	         //finally block used to close resources
	         try
	         {
	            if(stmt!=null){stmt.close();};
	         }
	         catch(SQLException se2)
	         {
	         }// nothing we can do
	         try
	         {
	            if(conn!=null){conn.close();}
	         }
	         catch(SQLException se)
	         {
	            se.printStackTrace();
	         }//end finally try
	     } //end try
	    //out.println("End of Init Locations");
	}
	
	private void getKegsLastLocations()
	{
		ArrayList<Double> returnLats = new ArrayList<Double>();
		ArrayList<Double> returnLons = new ArrayList<Double>();
		ArrayList<String> returnIds = new ArrayList<String>();
		// JDBC driver name and database URL
	    final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	    final String DB_URL="jdbc:mysql://localhost/truckytrackdatabase";
	    
	    //  Database credentials
	    final String USER = "user";
	    final String PASS = "";
	    Statement stmt = null;
	    Connection conn = null;
	    
	    //out.println("InitLocations");
	    
	    try
	    {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // Open a connection
	         conn = DriverManager.getConnection(DB_URL, USER, PASS);

	         // Execute SQL query
	         stmt = conn.createStatement();
	         String sql;
	         //get the id and drop location of the latest entry for any keg that isn't currently being transported(i.e. has a droppedat of null)
	         sql = "select id, DroppedAtLat, DroppedAtLon from KegHistory t1 WHERE t1.DroppedAtTime = (SELECT MAX(t2.DroppedAtTime) FROM KegHistory t2 WHERE t2.id = t1.id) AND (SELECT t3.id FROM KegHistory t3 WHERE t3.id = t1.id AND t3.DroppedAtTime IS NULL) IS NULL;";
	         //out.println(sql);
	         ResultSet rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getString("id"));
	            returnLats.add(rs.getDouble("DroppedAtLat"));
	            returnLons.add(rs.getDouble("DroppedAtLon"));
	                     	                        
	         }
	         
	         //get the id of every keg currently being transported by a driver, and the location of said driver.
	         sql = "select t2.kegID, loc.lat, loc.lon from Locations AS loc INNER JOIN KegDrivers AS t2 ON loc.id = t2.driverID WHERE loc.timestamp = (SELECT MAX(t3.timestamp) FROM Locations t3 WHERE t3.id = loc.id);";
	         //out.println(sql);
	         rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getString("t2.kegID"));
	            returnLats.add(rs.getDouble("loc.lat"));
	            returnLons.add(rs.getDouble("loc.lon"));         	                        
	         }
	         
	         
	         JSONArray jsonOut = new JSONArray();
	         int i = 0;
	         for(String aID: returnIds)
	         {
	        	 JSONObject obj = new JSONObject();
	        	 obj.put("kegID", aID);
	        	 obj.put("lat", returnLats.get(i));
	        	 obj.put("lon", returnLons.get(i));
	        	 
	        	       	 
	        	 jsonOut.add(obj);
	        	 i++;
	         }
	         //out.println("Returning IDS");
	         out.println(jsonOut);

	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	    }
	    catch(SQLException se)
	    {
	         //Handle errors for JDBC
	         se.printStackTrace();
	    }
	    catch(Exception e)
	    {
	         //Handle errors for Class.forName
	         e.printStackTrace();
	    }
	    finally
	    {
	         //finally block used to close resources
	         try
	         {
	            if(stmt!=null){stmt.close();};
	         }
	         catch(SQLException se2)
	         {
	         }// nothing we can do
	         try
	         {
	            if(conn!=null){conn.close();}
	         }
	         catch(SQLException se)
	         {
	            se.printStackTrace();
	         }//end finally try
	     } //end try
	    //out.println("End of Init Locations");
	}
	
	private void getKegsLastLocationsOnADate(String inDate)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try
        {
        c.setTime(dateFormat.parse(inDate));    
        }
        catch(ParseException e)
        {
        	out.print("ERROR: ParseException in date parsing, date input invalid.");
        }
		String dayOfTheKeg = dateFormat.format(c.getTime()) + " 00:00:01";
		c.add(Calendar.DATE, 1);
		String dayAfterTheKeg = dateFormat.format(c.getTime()) + " 00:00:01";
		
		ArrayList<Double> returnLats = new ArrayList<Double>();
		ArrayList<Double> returnLons = new ArrayList<Double>();
		ArrayList<String> returnIds = new ArrayList<String>();
		// JDBC driver name and database URL
	    final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	    final String DB_URL="jdbc:mysql://localhost/truckytrackdatabase";
	    
	    //  Database credentials
	    final String USER = "user";
	    final String PASS = "";
	    Statement stmt = null;
	    Connection conn = null;
	    
	    //out.println("InitLocations");
	    
	    try
	    {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // Open a connection
	         conn = DriverManager.getConnection(DB_URL, USER, PASS);

	         // Execute SQL query
	         stmt = conn.createStatement();
	         String sql;
	         //get the id and drop location of the latest entry for any keg that isn't currently being transported(i.e. has a droppedat of null)
	         //sql = "select id, DroppedAtLat, DroppedAtLon from KegHistory t1 WHERE t1.DroppedAtTime = (SELECT MAX(t2.DroppedAtTime) FROM KegHistory t2 WHERE t2.id = t1.id) AND (SELECT t3.id FROM KegHistory t3 WHERE t3.id = t1.id AND t3.DroppedAtTime IS NULL) IS NULL;";
	         //sql = "select id, DroppedAtLat, DroppedAtLon from KegHistory t1 WHERE (t1.DroppedAtTime = (SELECT MAX(t2.DroppedAtTime) FROM KegHistory t2 WHERE t2.id = t1.id) AND (SELECT t3.id FROM KegHistory t3 WHERE t3.id = t1.id AND t3.DroppedAtTime IS NULL) IS NULL AND ((t1.DroppedAtTime < '" + dayAfterTheKeg + "' AND t1.DroppedAtTime > '" + dayOfTheKeg +"') OR (t1.DroppedAtTime > '2017-05-11' AND t1.PickedUpTime < '" + dayAfterTheKeg +"' AND t1.PickedUpTime > '" + dayOfTheKeg +"')) );";
	         sql = "select id, DroppedAtLat, DroppedAtLon from KegHistory t1 WHERE (t1.DroppedAtTime = (SELECT MAX(t2.DroppedAtTime) FROM KegHistory t2 WHERE t2.id = t1.id AND (SELECT t3.id FROM KegHistory t3 WHERE t3.id = t1.id AND t3.DroppedAtTime IS NULL) IS NULL AND ((t2.DroppedAtTime < '" + dayAfterTheKeg + "' AND t2.DroppedAtTime > '" + dayOfTheKeg +"')) AND (SELECT t4.fullID FROM KegKeys t4 WHERE t4.fullID = t1.id) IS NOT NULL ));";
	         //out.println(sql);
	         ResultSet rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getString("id") + " Dropped Off");
	            returnLats.add(rs.getDouble("DroppedAtLat"));
	            returnLons.add(rs.getDouble("DroppedAtLon"));
	                     	                        
	         }
	         
	         sql = "select id, PickedUpAtLat, PickedUpAtLon from KegHistory t1 WHERE (t1.PickedUpTime = (SELECT MAX(t2.PickedUpTime) FROM KegHistory t2 WHERE t2.id = t1.id AND (SELECT t3.id FROM KegHistory t3 WHERE t3.id = t1.id AND t3.DroppedAtTime IS NULL) IS NULL AND ((t2.PickedUpTime < '" + dayAfterTheKeg + "' AND t2.PickedUpTime > '" + dayOfTheKeg +"')) AND (SELECT t4.emptyID FROM KegKeys t4 WHERE t4.emptyID = t1.id) IS NOT NULL));";
	         //out.println(sql);
	         rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getString("id") + " Picked Up");
	            returnLats.add(rs.getDouble("PickedUpAtLat"));
	            returnLons.add(rs.getDouble("PickedUpAtLon"));
	                     	                        
	         }
	         
	         
	         JSONArray jsonOut = new JSONArray();
	         int i = 0;
	         for(String aID: returnIds)
	         {
	        	 JSONObject obj = new JSONObject();
	        	 obj.put("kegID", aID );
	        	 obj.put("lat", returnLats.get(i));
	        	 obj.put("lon", returnLons.get(i));
	        	 
	        	       	 
	        	 jsonOut.add(obj);
	        	 i++;
	         }
	         //out.println("Returning IDS");
	         out.println(jsonOut);

	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	    }
	    catch(SQLException se)
	    {
	         //Handle errors for JDBC
	         se.printStackTrace();
	    }
	    catch(Exception e)
	    {
	         //Handle errors for Class.forName
	         e.printStackTrace();
	    }
	    finally
	    {
	         //finally block used to close resources
	         try
	         {
	            if(stmt!=null){stmt.close();};
	         }
	         catch(SQLException se2)
	         {
	         }// nothing we can do
	         try
	         {
	            if(conn!=null){conn.close();}
	         }
	         catch(SQLException se)
	         {
	            se.printStackTrace();
	         }//end finally try
	     } //end try
	    //out.println("End of Init Locations");
	}
	
	private void getAllKegsLocationsOnADate(String inDate)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try
        {
        c.setTime(dateFormat.parse(inDate));    
        }
        catch(ParseException e)
        {
        	out.print("ERROR: ParseException in date parsing, date input invalid.");
        }
		String dayOfTheKeg = dateFormat.format(c.getTime()) + " 00:00:01";
		c.add(Calendar.DATE, 1);
		String dayAfterTheKeg = dateFormat.format(c.getTime()) + " 00:00:01";
		
		ArrayList<Double> returnLats = new ArrayList<Double>();
		ArrayList<Double> returnLons = new ArrayList<Double>();
		ArrayList<String> returnIds = new ArrayList<String>();
		// JDBC driver name and database URL
	    final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	    final String DB_URL="jdbc:mysql://localhost/truckytrackdatabase";
	    
	    //  Database credentials
	    final String USER = "user";
	    final String PASS = "";
	    Statement stmt = null;
	    Connection conn = null;
	    
	    //out.println("InitLocations");
	    
	    try
	    {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // Open a connection
	         conn = DriverManager.getConnection(DB_URL, USER, PASS);

	         // Execute SQL query
	         stmt = conn.createStatement();
	         String sql;
	         //get the id and drop location of the latest entry for any keg that isn't currently being transported(i.e. has a droppedat of null)
	         //sql = "select id, DroppedAtLat, DroppedAtLon from KegHistory t1 WHERE t1.DroppedAtTime = (SELECT MAX(t2.DroppedAtTime) FROM KegHistory t2 WHERE t2.id = t1.id) AND (SELECT t3.id FROM KegHistory t3 WHERE t3.id = t1.id AND t3.DroppedAtTime IS NULL) IS NULL;";
	         sql = "select id, DroppedAtLat, DroppedAtLon from KegHistory t1 WHERE (SELECT t3.id FROM KegHistory t3 WHERE t3.id = t1.id AND t3.DroppedAtTime IS NULL) IS NULL AND (t1.DroppedAtTime < '" + dayAfterTheKeg + "' AND t1.DroppedAtTime > '" + dayOfTheKeg +"') AND (SELECT t4.fullID FROM KegKeys t4 WHERE t4.fullID = t1.id) IS NOT NULL;";
	         //out.println(sql);
	         ResultSet rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getString("id"));
	            returnLats.add(rs.getDouble("DroppedAtLat"));
	            returnLons.add(rs.getDouble("DroppedAtLon"));
	                     	                        
	         }
	         
	         sql = "select id, PickedUpAtLat, PickedUpAtLon from KegHistory t1 WHERE (SELECT t3.id FROM KegHistory t3 WHERE t3.id = t1.id AND t3.DroppedAtTime IS NULL) IS NULL AND (t1.PickedUpTime < '" + dayAfterTheKeg + "' AND t1.PickedUpTime > '" + dayOfTheKeg + "') AND (SELECT t4.emptyID FROM KegKeys t4 WHERE t4.emptyID = t1.id) IS NOT NULL;";
	         //out.println(sql);
	         rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getString("id") + " Picked Up");
	            returnLats.add(rs.getDouble("PickedUpAtLat"));
	            returnLons.add(rs.getDouble("PickedUpAtLon"));
	                     	                        
	         }
	         
	         
	         JSONArray jsonOut = new JSONArray();
	         int i = 0;
	         for(String aID: returnIds)
	         {
	        	 JSONObject obj = new JSONObject();
	        	 obj.put("kegID", aID);
	        	 obj.put("lat", returnLats.get(i));
	        	 obj.put("lon", returnLons.get(i));
	        	 
	        	       	 
	        	 jsonOut.add(obj);
	        	 i++;
	         }
	         //out.println("Returning IDS");
	         out.println(jsonOut);

	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	    }
	    catch(SQLException se)
	    {
	         //Handle errors for JDBC
	         se.printStackTrace();
	    }
	    catch(Exception e)
	    {
	         //Handle errors for Class.forName
	         e.printStackTrace();
	    }
	    finally
	    {
	         //finally block used to close resources
	         try
	         {
	            if(stmt!=null){stmt.close();};
	         }
	         catch(SQLException se2)
	         {
	         }// nothing we can do
	         try
	         {
	            if(conn!=null){conn.close();}
	         }
	         catch(SQLException se)
	         {
	            se.printStackTrace();
	         }//end finally try
	     } //end try
	    //out.println("End of Init Locations");
	}
	
	//returns the data for every location entry with matching id that is within inRadius of a (CentreLat, CentreLon)
	private void getLocationsWithin(String inID, double inCentreLat, double inCentreLon, double inRadius)
	{
		ArrayList<String> returnNotes1 = new ArrayList<String>();
		ArrayList<String> returnNotes2 = new ArrayList<String>();
		ArrayList<Double> returnLats = new ArrayList<Double>();
		ArrayList<Double> returnLons = new ArrayList<Double>();
		ArrayList<java.util.Date> returnTimeStamps = new ArrayList<java.util.Date>();
		ArrayList<Integer> returnIds = new ArrayList<Integer>();
		// JDBC driver name and database URL
	    final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	    final String DB_URL="jdbc:mysql://localhost/truckytrackdatabase";
	    
	    //  Database credentials
	    final String USER = "user";
	    final String PASS = "";
	    Statement stmt = null;
	    Connection conn = null;
	    
	    //out.println("InitLocations");
	    
	    try
	    {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");

	         // Open a connection
	         conn = DriverManager.getConnection(DB_URL, USER, PASS);

	         // Execute SQL query
	         stmt = conn.createStatement();
	         String sql;
	         sql = "select * from Locations where id=" + inID + " AND (lat < ("+ (inCentreLat + inRadius) +") AND lat > ("+ (inCentreLat - inRadius) +")) AND (lon < ("+ (inCentreLon + inRadius) +") AND lon > ("+ (inCentreLon - inRadius) +"));";
	         //out.println(sql);
	         ResultSet rs = stmt.executeQuery(sql);

	         // Extract data from result set
	         while(rs.next())
	         {
	            //Retrieve by column name
	            returnIds.add(rs.getInt("id"));
	            returnLats.add(rs.getDouble("lat"));
	            returnLons.add(rs.getDouble("lon"));
	            java.util.Date adate = rs.getTimestamp("timestamp");
	            returnTimeStamps.add(adate);
	            returnNotes1.add(rs.getString("Note1"));
	            returnNotes2.add(rs.getString("Note2"));
	            	            
	            //Display values
	            
	         }
	         
	         JSONArray jsonOut = new JSONArray();
	         int i = 0;
	         for(int aID: returnIds)
	         {
	        	 JSONObject obj = new JSONObject();
	        	 obj.put("id", aID);
	        	 obj.put("lat", returnLats.get(i));
	        	 obj.put("lon", returnLons.get(i));
	        	 DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	        	 obj.put("timestamp", format.format(returnTimeStamps.get(i)));
	        	 obj.put("Note1", returnNotes1.get(i));
	        	 obj.put("Note2", returnNotes2.get(i));
	        	 jsonOut.add(obj);
	        	 i++;
	         }
	         //out.println("Returning IDS");
	         out.println(jsonOut);

	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	    }
	    catch(SQLException se)
	    {
	         //Handle errors for JDBC
	         se.printStackTrace();
	    }
	    catch(Exception e)
	    {
	         //Handle errors for Class.forName
	         e.printStackTrace();
	    }
	    finally
	    {
	         //finally block used to close resources
	         try
	         {
	            if(stmt!=null){stmt.close();};
	         }
	         catch(SQLException se2)
	         {
	         }// nothing we can do
	         try
	         {
	            if(conn!=null){conn.close();}
	         }
	         catch(SQLException se)
	         {
	            se.printStackTrace();
	         }//end finally try
	     } //end try
	    //out.println("End of Init Locations");
	}
	
}
