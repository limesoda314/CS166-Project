/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Hotel {

   // reference to physical database connection.
   private Connection _connection = null;

   private int _authorisedUser = -1;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Hotel 
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Hotel(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Hotel

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement ();

      ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public int getNewUserID(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement ();
      ResultSet rs = stmt.executeQuery (sql);
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }
   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Hotel.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Hotel esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Hotel object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Hotel (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              esql._authorisedUser = Integer.parseInt(authorisedUser);
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Hotels within 30 units");
                System.out.println("2. View Rooms");
                System.out.println("3. Book a Room");
                System.out.println("4. View recent booking history");

                //the following functionalities basically used by managers
                System.out.println("5. Update Room Information");
                System.out.println("6. View 5 recent Room Updates Info");
                System.out.println("7. View booking history of the hotel");
                System.out.println("8. View 5 regular Customers");
                System.out.println("9. Place room repair Request to a company");
                System.out.println("10. View room repair Requests history");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewHotels(esql); break;
                   case 2: viewRooms(esql); break;
                   case 3: bookRooms(esql); break;
                   case 4: viewRecentBookingsfromCustomer(esql); break;
                   case 5: updateRoomInfo(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewBookingHistoryofHotel(esql); break;
                   case 8: viewRegularCustomers(esql); break;
                   case 9: placeRoomRepairRequests(esql); break;
                   case 10: viewRoomRepairHistory(esql); break;
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Hotel esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine(); 
         String type = "Customer";
			String query = String.format("INSERT INTO USERS (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
         esql.executeUpdate(query);
         System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
         
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Hotel esql){
      try{
         System.out.print("\tEnter userID: ");
         String userID = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE userID = '%s' AND password = '%s'", userID, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return userID;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   /*
      Helper function to print the table in a neater way, allowing the user
      to better see the columns and fields in a nice tabled response.

      We decided to do this over executeQueryAndPrint because it is messy and
      hard to tell what is going on.

      Credit to: https://stackoverflow.com/questions/38623194/jdbc-format-resultset-as-tabular-string
   */
   public void executeQueryAndPrettyPrint(String query) throws SQLException {

      StringBuilder stringResponse = new StringBuilder();

      // Creates a statement object based on our connection
      Statement stmt = this._connection.createStatement();

      // Execute query argument and save in ResultSet
      ResultSet res = stmt.executeQuery(query);

      // Get metadata to know how to format this thing lol
      ResultSetMetaData meta = res.getMetaData();

      // save metadata
      int tot_cols = meta.getColumnCount();
      int[] colDisplaySizes = new int[tot_cols];
      String[] colDisplayLabels = new String[tot_cols];
      int tot_rows = 0;

      stringResponse.append("\n");

      for (int i = 0; i < tot_cols; i++) {
         
         colDisplaySizes[i] = meta.getColumnLabel(i + 1).length() + 5;
         colDisplayLabels[i] = meta.getColumnLabel(i + 1);

         // // cuts off 
         if (colDisplayLabels[i].length() > colDisplaySizes[i]) {
            colDisplayLabels[i] = colDisplayLabels[i].substring(0, colDisplaySizes[i]);
         }

         for (int j = 0; j < colDisplaySizes[i] + 3; j++) {
            stringResponse.append("-");
         }
      }
      stringResponse.append("--\n");

      // print column row
      for (int i = 0; i < tot_cols; i++) {
         stringResponse.append(
            String.format("| %" + colDisplaySizes[i] + "s ", colDisplayLabels[i])
         );
      }
      stringResponse.append("|\n");

      StringBuilder hline = new StringBuilder();
      
      for (int i = 0; i < tot_cols; i++) {
         for (int j = 0; j < colDisplaySizes[i] + 3; j++) {
            hline.append("-");
         }
      }
      hline.append("--\n");

      stringResponse.append(hline);

      // System.out.print("printing horizontal line header\n");
      // print the response - untested thoroughly
      while (res.next()) {

         tot_rows += 1;
         for (int i = 0; i < tot_cols; i++) {

            String resString = res.getString(i + 1);
            if (resString.length() > colDisplayLabels[i].length() + 5) {
               resString = resString.substring(0, colDisplayLabels[i].length() + 2) + "...";
            }

            stringResponse.append(
               String.format("| %" + colDisplaySizes[i] + "s ", resString)
            );
         }
         stringResponse.append("|\n");
      }

      stringResponse.append(hline);

      // print response length
      stringResponse.append(
         String.format("  -- Rows: %s\n\n", tot_rows)
      );

      stmt.close();

      System.out.print(stringResponse);
   }

   /*
    * Browse list of hotels within 30 units distance of user 
    * Ask for user latitude and longitude 
    *
    */

   /*
   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   // already declared above 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   
   */
   // DONE
   public static void viewHotels(Hotel esql) {
      try {
         System.out.print("\tEnter latitude: ");
         double user_lat = Double.parseDouble(in.readLine());
         System.out.print("\tEnter longitude: ");
         double user_long = Double.parseDouble(in.readLine());

         // hotel name adds too many empty characters so put it at the end
         // we don't include the managerid since that's not something users need to know 
         String query = "" +
            "SELECT H.hotelID, H.latitude, H.longitude, H.dateEstablished, H.hotelName \n" +
            "FROM HOTEL H \n" +
            "WHERE calculate_distance(%f, %f, H.latitude, H.longitude) <= 30;\n";
         query = String.format(query, user_lat, user_long);

         esql.executeQueryAndPrettyPrint(query); // prints the table in a nicer way

         return; 
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }

   // DONE
   public static void viewRooms(Hotel esql) {
      try {
         System.out.print("\tEnter hotel id: ");
         int hotel_id = Integer.parseInt(in.readLine());
         System.out.print("\tEnter booking date (mm/dd/yyyy): ");
         String view_date = in.readLine();

         String query = "SELECT DISTINCT R.price, R.roomNumber, "; 
         query += String.format(" CASE WHEN (NOT EXISTS (SELECT * FROM RoomBookings A, Rooms C WHERE A.bookingDate='%s' AND A.roomNumber=R.roomNumber AND A.roomNumber = C.roomNumber AND A.hotelID = C.hotelID )) THEN 'open' ELSE 'reserved' END as Status", view_date); 
         query += String.format(" FROM Rooms R, RoomBookings B");
         query += String.format(" WHERE R.hotelID=%d AND B.hotelID=R.hotelID", hotel_id);

         esql.executeQueryAndPrettyPrint(query);

         return; 
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static void bookRooms(Hotel esql) {
      try {
         System.out.print("\tEnter hotel id: ");
         int hotel_id = Integer.parseInt(in.readLine());
         System.out.print("\tEnter room number: ");
         int room_id = Integer.parseInt(in.readLine());
         System.out.print("\tEnter your preferred booking date (mm/dd/yyyy): ");
         String book_date = in.readLine();

         String availabilityQuery = "" +
            "SELECT A.hotelID, A.roomNumber, B.bookingDate \n" +
            "FROM Rooms A, RoomBookings B \n" +
            "WHERE B.bookingDate = '%s' \n" + // book_date
            "AND B.hotelID = %d \n" + // hotel_id
            "AND A.hotelID = %d \n" + // hotel_id
            "AND A.roomNumber = B.roomNumber \n" +
            "AND A.roomNumber = %d; \n"; // room_id

         availabilityQuery = String.format(
            availabilityQuery,
            book_date,
            hotel_id,
            hotel_id,
            room_id
         );

         // write the sql query 
         int availabilityResponse = esql.executeQuery(availabilityQuery);

         // // to test
         // System.out.print(String.format("res length: %d", availabilityResponse));

         // if empty we should be good to book
         if (availabilityResponse != 0) {
            
            String errorString = "\n  -- Sorry. Room %d in hotel %d is not available for date \"%s\".\n" +
            "    You may view the room availability with option 2 in the main menu. Thank you.\n\n";
            System.out.print(
               String.format(errorString, room_id, hotel_id, book_date)
            );
            return;
         }
         
         // in this case, it is available
         String priceQuery = "" +
            "SELECT DISTINCT A.price \n" +
            "FROM Rooms A \n" +
            "WHERE A.roomNumber = %d \n" +
            "AND A.hotelID = %d; \n"; 

         priceQuery = String.format(
            priceQuery, room_id, hotel_id
         );

         // to check if room exists in hotel
         int priceResponse = esql.executeQueryAndPrintResult(priceQuery);

         // this suggests no room in given hotel id
         if (priceResponse == 0) {
            String errorString = "  -- Sorry. Room %d is not available in hotel %d" +
            "    You may view the room availability with option 2 in the main menu. Thank you.\n\n";
            System.out.print(
               String.format(errorString, room_id, hotel_id)
            );
            return;
         }

         // here is the perfect case, now we must fetch the price
         // we know there is 1 row at least, so we can fetch

         System.out.print("Proceed? (Y/N): ");
         String proceedResponse = "";

         boolean cont = false;

         while (!(proceedResponse.toLowerCase() == "Y") && !(proceedResponse.toLowerCase() == "N")) {
            proceedResponse = in.readLine();

            if (proceedResponse.toLowerCase().contains("y")) {
               cont = true;
               break;
            }
            else if (proceedResponse.toLowerCase().contains("n")) {
               break;
            }
            
            // System.out.println(proceedResponse);
            System.out.print("[ERROR] Proceed? (Y/N): ");
         }

         if (!cont) {
            return;
         }

         String insertQuery = "INSERT INTO RoomBookings(customerID, hotelID, roomNumber, bookingDate) \n" +
            "VALUES (%d, %d, %d, '%s') \n";
         insertQuery = String.format(
            insertQuery,
            esql._authorisedUser,
            hotel_id,
            room_id,
            book_date
         );

         // System.out.print(insertQuery);

         esql.executeUpdate(insertQuery);

         System.out.print("\n   -- Thank you for booking! \n\n");

         return; 
      } catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }

   }

   // TODO
   public static void viewRecentBookingsfromCustomer(Hotel esql) {

      try {
         String bookingInfoQuery = "" +
            "SELECT A.hotelID, A.roomNumber, B.price, A.bookingDate \n" +
            "FROM RoomBookings A, Rooms B \n" +
            "WHERE B.hotelID = A.hotelID \n" +
            "AND A.roomNumber = B.roomNumber \n " +
            "AND A.customerID = %d \n" +
            "LIMIT 5;";

         bookingInfoQuery = String.format(
            bookingInfoQuery,
            esql._authorisedUser
         );

         esql.executeQueryAndPrettyPrint(bookingInfoQuery);

         return; 
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void updateRoomInfo(Hotel esql) {
      try {

         // security check...
         String securityCheckQuery = "" +
            "SELECT DISTINCT A.userID \n" +
            "FROM Users A \n" +
            "WHERE (A.userType = 'manager' OR A.userType = 'admin') \n" +
            "AND A.userID = %d;";

         securityCheckQuery = String.format(
            securityCheckQuery,
            esql._authorisedUser
         );

         int securityResponse = esql.executeQuery(securityCheckQuery);

         if (securityResponse == 0) {
            System.out.print("  - Permission Error: You are not allowed to perform this operation.\n\n");
            return;
         }
         
         // manager successfully identified

         // identifying info
         System.out.print("\tEnter hotel id: ");
         int hotelID = Integer.parseInt(in.readLine());
         System.out.print("\tEnter room number: ");
         int roomNumber = Integer.parseInt(in.readLine());

         // now we need to check whether they can manage the hotel they chose...
         String hotelsManagedQuery = "" +
            "SELECT DISTINCT A.hotelID \n" +
            "FROM Hotel A \n" +
            "WHERE A.managerUserID = %d \n" +
            "AND A.hotelID = %d; \n";

         hotelsManagedQuery = String.format(
            hotelsManagedQuery,
            esql._authorisedUser,
            hotelID
         );

         // now we need to check whether they are an admin...
         String isAdminQuery = "" +
            "SELECT * \n" +
            "FROM  Users A \n" +
            "WHERE A.userID = %d \n" +
            "AND A.userType = 'admin'; \n";

         isAdminQuery = String.format(
            isAdminQuery,
            esql._authorisedUser
         );

         int hotelsManagedQueryResponse = esql.executeQuery(hotelsManagedQuery);
         int isAdminResponse = esql.executeQuery(isAdminQuery);

         if (hotelsManagedQueryResponse == 0 && isAdminResponse == 0) {
            System.out.print("  - Permission Error: You are not allowed to perform this operation in hotels you do not manage.\n\n");
            return;
         }

         // updates
         System.out.print("\tEnter new price: ");
         int newPrice = Integer.parseInt(in.readLine());
         System.out.print("\tEnter new image url: ");
         String newImageUrl = in.readLine();

         String updateRoomsQuery = "" +
            "UPDATE Rooms \n" +
            "SET price = %d, imageURL = '%s', hotelID = %d, roomNumber = %d \n" +
            "WHERE hotelID = %d \n" +
            "AND roomNumber = %d \n";

         updateRoomsQuery = String.format(
            updateRoomsQuery,
            newPrice,
            newImageUrl,
            hotelID,
            roomNumber,
            hotelID,
            roomNumber
         );

         String updateLogQuery = "" +
            "INSERT INTO RoomUpdatesLog(managerID, hotelID, roomNumber, updatedOn) \n" +
            "VALUES (%d, %d, %d, '%s'); \n";

         // credit: https://www.tutorialkart.com/java/how-to-get-current-date-in-mm-dd-yyyy-format-in-java/
         LocalDate currDate = LocalDate.now();
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
         String date = currDate.format(formatter);

         updateLogQuery = String.format(
            updateLogQuery,
            esql._authorisedUser,
            hotelID,
            roomNumber,
            date
         );

         esql.executeUpdate(updateRoomsQuery);
         System.out.print("\n   -- Updated Rooms successfully! \n\n");
         esql.executeUpdate(updateLogQuery);
         System.out.print("\n   -- Updated Log successfully! \n\n");

         return; 
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void viewRecentUpdates(Hotel esql) {
      try {
         // security check...
         String securityCheckQuery = "" +
            "SELECT DISTINCT A.userID \n" +
            "FROM Users A \n" +
            "WHERE (A.userType = 'manager' OR A.userType = 'admin') \n" +
            "AND A.userID = %d;";

         securityCheckQuery = String.format(
            securityCheckQuery,
            esql._authorisedUser
         );

         int securityResponse = esql.executeQuery(securityCheckQuery);

         if (securityResponse == 0) {
            System.out.print("  - Permission Error: You are not allowed to perform this operation.\n\n");
            return;
         }

         // we need to find all the updates with this manager userID

         String latestUpdatesQuery = "" +
            "SELECT A.updateNumber, A.managerID, A.hotelID, A.roomNumber, A.updatedOn \n" +
            "FROM RoomUpdatesLog A \n" +
            "WHERE A.managerID = %d \n" +
            "AND EXISTS (SELECT * FROM Hotel B WHERE B.managerUserID=A.managerID AND B.hotelID=A.hotelID)\n" +
            "LIMIT 5; \n";

         latestUpdatesQuery = String.format(
            latestUpdatesQuery,
            esql._authorisedUser
         );

         esql.executeQueryAndPrettyPrint(latestUpdatesQuery);

         return; 
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void viewBookingHistoryofHotel(Hotel esql) {
      try {

         // security check...
         String securityCheckQuery = "" +
            "SELECT DISTINCT A.userID \n" +
            "FROM Users A \n" +
            "WHERE (A.userType = 'manager' OR A.userType = 'admin') \n" +
            "AND A.userID = %d;";

         securityCheckQuery = String.format(
            securityCheckQuery,
            esql._authorisedUser
         );

         int securityResponse = esql.executeQuery(securityCheckQuery);

         if (securityResponse == 0) {
            System.out.print("  - Permission Error: You are not allowed to perform this operation.\n\n");
            return;
         }

         String bookingInfoQuery = "" +
            "SELECT A.hotelID, A.roomNumber, B.price, A.bookingDate \n" +
            "FROM RoomBookings A, Rooms B \n" +
            "WHERE B.hotelID = A.hotelID \n" +
            "AND A.roomNumber = B.roomNumber \n " +
            "AND EXISTS (SELECT * FROM Hotel C WHERE C.managerUserID = %d AND B.hotelID = C.hotelID) \n" +
            "LIMIT 5;";

         bookingInfoQuery = String.format(
            bookingInfoQuery,
            esql._authorisedUser
         );

         esql.executeQueryAndPrettyPrint(bookingInfoQuery);

         return; 
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void viewRegularCustomers(Hotel esql) {
      try {
         // security check...
         String securityCheckQuery = "" +
            "SELECT DISTINCT A.userID \n" +
            "FROM Users A \n" +
            "WHERE (A.userType = 'manager' OR A.userType = 'admin') \n" +
            "AND A.userID = %d;";

         securityCheckQuery = String.format(
            securityCheckQuery,
            esql._authorisedUser
         );

         int securityResponse = esql.executeQuery(securityCheckQuery);

         if (securityResponse == 0) {
            System.out.print("  - Permission Error: You are not allowed to perform this operation.\n\n");
            return;
         }

         String viewMostBookingCustomersQuery = "" +
            "SELECT D.hotelID, A.userID, A.name, COUNT(B.customerID) \n" +
            "FROM Hotel D, Users A, RoomBookings B \n" +
            "WHERE A.userID = B.customerID \n" +
            "AND D.hotelID = B.hotelID \n" +
            "AND EXISTS (SELECT * FROM Hotel C WHERE C.managerUserID = %d AND D.hotelID = C.hotelID AND B.hotelID = C.hotelID) \n" +
            "GROUP BY A.userID, D.hotelID \n" +
            "ORDER BY COUNT(B.customerID) DESC \n" +
            "LIMIT 5; \n";
         
         viewMostBookingCustomersQuery = String.format(
            viewMostBookingCustomersQuery,
            esql._authorisedUser
         );

         esql.executeQueryAndPrettyPrint(viewMostBookingCustomersQuery);

         return;
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }

   }
   
   public static void placeRoomRepairRequests(Hotel esql) {
      try {
         // security check...
         String securityCheckQuery = "" +
            "SELECT DISTINCT A.userID \n" +
            "FROM Users A \n" +
            "WHERE (A.userType = 'manager' OR A.userType = 'admin') \n" +
            "AND A.userID = %d;";

         securityCheckQuery = String.format(
            securityCheckQuery,
            esql._authorisedUser
         );

         int securityResponse = esql.executeQuery(securityCheckQuery);

         if (securityResponse == 0) {
            System.out.print("  - Permission Error: You are not allowed to perform this operation.\n\n");
            return;
         }
   
         System.out.print("\tEnter hotel id: ");
         int hotelID = Integer.parseInt(in.readLine());
         System.out.print("\tEnter room number: ");
         int roomNumber = Integer.parseInt(in.readLine());
         System.out.print("\tEnter the maintenance company ID: ");
         int companyID = Integer.parseInt(in.readLine());

         // Check if they manage the hotel
         String hotelManagedQuery = "" +
            "SELECT A.hotelID \n" +
            "FROM Hotel A \n" +
            "WHERE A.hotelID = %d \n" + // hotelID
            "AND A.managerUserID = %d \n"; // managerUserID

         hotelManagedQuery = String.format(
            hotelManagedQuery,
            hotelID,
            esql._authorisedUser
         );

         int hotelManagedResponse = esql.executeQuery(hotelManagedQuery);

         if (hotelManagedResponse == 0) {
            System.out.print(
               "\n  - Sorry. You cannot place repair requests on hotels you do not manage.\n\n"
            );
            return;
         }

         // Check if room repair request has already been made for the current room/hotel by companyID
         String alreadyMadeQuery = "" +
         "SELECT A.requestNumber \n" +
         "FROM RoomRepairRequests A \n" +
         "WHERE A.repairID = (SELECT B.repairID \n" +
         "                    FROM RoomRepairs B \n" +
         "                    WHERE B.companyID = %d \n" +
         "                    AND   B.hotelID = %d \n" +
         "                    AND   B.roomNumber = %d); \n";

         alreadyMadeQuery = String.format(
            alreadyMadeQuery,
            companyID,
            hotelID,
            roomNumber
         );

         int alreadyMadeResponse = esql.executeQuery(alreadyMadeQuery);

         if (alreadyMadeResponse != 0) {
            System.out.print(
               "\n  - Sorry. This request from the company to the particular hotel and room already exists.\n\n"
            );
            return;
         }

         // Check if the company ID exists for given hotel and room number combination
         String availabilityQuery = "" +
            "SELECT A.repairID \n" +
            "FROM RoomRepairs A \n" +
            "WHERE A.companyID = %d \n" + // companyID
            "AND A.hotelID = %d \n" + // hotelID
            "AND A.roomNumber = %d; \n"; // roomNumber

         availabilityQuery = String.format(
            availabilityQuery,
            companyID,
            hotelID,
            roomNumber
         );

         // write the sql query 
         int availabilityResponse = esql.executeQuery(availabilityQuery);

         if (availabilityResponse == 0) {
            System.out.print(
               String.format(
                  "\n  - Sorry. Room %d in hotel %d is not currently repaired by company %d\n\n",
                  roomNumber,
                  hotelID,
                  companyID
               )
            );
            return;
         }

         String maintenanceRequestQuery = "" +
         "INSERT INTO RoomRepairRequests(managerID, repairID) \n" +
         "VALUES (%d, (SELECT B.repairID \n" +
         "             FROM roomRepairs B \n" +
         "             WHERE B.companyID=%d \n" +
         "             AND B.hotelID=%d \n" +
         "             AND B.roomNumber=%d)); \n";

         maintenanceRequestQuery = String.format(
            maintenanceRequestQuery,
            esql._authorisedUser,
            companyID,
            hotelID,
            roomNumber
         );

         esql.executeUpdate(maintenanceRequestQuery);

         System.out.print(
            "\n  - Updated repair requests successfully\n\n"
         );

         return;
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void viewRoomRepairHistory(Hotel esql) {
      try {
         // security check...
         String securityCheckQuery = "" +
            "SELECT DISTINCT A.userID \n" +
            "FROM Users A \n" +
            "WHERE (A.userType = 'manager' OR A.userType = 'admin') \n" +
            "AND A.userID = %d;";

         securityCheckQuery = String.format(
            securityCheckQuery,
            esql._authorisedUser
         );

         int securityResponse = esql.executeQuery(securityCheckQuery);

         if (securityResponse == 0) {
            System.out.print("  - Permission Error: You are not allowed to perform this operation.\n\n");
            return;
         }

         // Check if room repair request has already been made for the current room/hotel by companyID
         String repairHistoryQuery = "" +
         "SELECT B.companyID, B.hotelID, B.roomNumber, B.repairDate \n" +
         "FROM RoomRepairRequests A, RoomRepairs B \n" +
         "WHERE A.repairID = B.repairID \n" +
         "AND EXISTS (SELECT D.hotelID \n" +
         "            FROM Hotel D \n" +
         "            WHERE D.hotelID = B.hotelID \n" +
         "            AND D.managerUserID = %d); \n"; // authorizedUser

         repairHistoryQuery = String.format(
            repairHistoryQuery,
            esql._authorisedUser
         );

         esql.executeQueryAndPrettyPrint(repairHistoryQuery);

         return;
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }

}//end Hotel

