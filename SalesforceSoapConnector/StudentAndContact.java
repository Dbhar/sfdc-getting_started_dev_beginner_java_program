import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;
import java.text.*;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.enterprise.sobject.Student__c;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.soap.enterprise.sobject.Class__c;
import com.sforce.soap.enterprise.sobject.Contact;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.ConnectionException;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.SaveResult;
public class StudentAndContact {
	EnterpriseConnection connection;
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	String authEndPoint = "";
	public static void main(String args[]){
		if (args.length < 1) {
	         System.out.println("Usage: com.example.samples."
	               + "QuickstartApiSamples <AuthEndPoint>");

	         System.exit(-1);
		}
		StudentAndContact scon = new StudentAndContact(args[0]);
		
		if(scon.login()){
			scon.createStudents();
			scon.queryContact();
			scon.logout();
		}
		
	}
	
   public StudentAndContact(String authEndPoint) {
      this.authEndPoint = authEndPoint;
   }
	
	private String getUserInput(String prompt){
		String result = "";
		try{
			System.out.print(prompt);
			result = reader.readLine();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return result;
	}
	
	private boolean login(){
		Boolean success = false;
		String username = getUserInput("Enter username : ");
		String password = getUserInput("Enter password : ");
		try{
			ConnectorConfig config = new ConnectorConfig();
			config.setUsername(username);
			config.setPassword(password);
			System.out.println("Auth End Point : " + authEndPoint);
			config.setAuthEndpoint(authEndPoint);
			connection = new EnterpriseConnection(config);
			printUserInfo(config);
			success = true;
		}catch(ConnectionException ce){
			ce.printStackTrace();
		}
		return success;
	}
	
	private void printUserInfo(ConnectorConfig config) {
	      try {
	         GetUserInfoResult userInfo = connection.getUserInfo();

	         System.out.println("\nLogging in ...\n");
	         System.out.println("UserID: " + userInfo.getUserId());
	         System.out.println("User Full Name: " + userInfo.getUserFullName());
	         System.out.println("User Email: " + userInfo.getUserEmail());
	         System.out.println();
	         System.out.println("SessionID: " + config.getSessionId());
	         System.out.println("Auth End Point: " + config.getAuthEndpoint());
	         System.out
	               .println("Service End Point: " + config.getServiceEndpoint());
	         System.out.println();
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      }
	   }

	   private void logout() {
	      try {
	         connection.logout();
	         System.out.println("Logged out.");
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      }
	   }
	   
	   private void createStudents(){
		   SaveResult[] saveResult = new SaveResult[10];
		   try{
			   double numberOfStudents = Double.valueOf(getUserInput("Enter number of Students : "));
			   Class__c dummyClass = new Class__c();
			   dummyClass.setClassName__c("WSDL Connection Dummy Class");
			   dummyClass.setMaxSize__c(numberOfStudents);
			   dummyClass.setBoard__c("CBSE");
			   QueryResult query = connection.query("SELECT Id FROM Class__c WHERE ClassName__c = 'WSDL Connection Dummy Class'");
			   if(query.getSize() > 0){
				   connection.delete(new String[]{query.getRecords()[0].getId()});
			   }
			   saveResult = connection.create(new Class__c[]{dummyClass}); 
			   ArrayList<Student__c> students = new ArrayList<Student__c>();
			   for(int i = 0; i < numberOfStudents; i++){
				   Student__c student = new Student__c();
				   student.setClass__c(saveResult[0].getId());
				   student.setFirstName__c(getUserInput("Enter Student First Name : "));
				   student.setLastName__c(getUserInput("Enter Student Last Name : "));
				   DateFormat format = new SimpleDateFormat("dd/mm/yyyy", Locale.ENGLISH);
				   Date birthDate = format.parse(getUserInput("Enter BirthDate in dd/mm/yyy format : "));
				   Calendar cal = Calendar.getInstance();
				   cal.setTime(birthDate);  
				   student.setDOB__c(cal);
				   students.add(student);
			   }
			   saveResult =  connection.create(students.toArray(new Student__c[students.size()])); 
		   }catch(ConnectionException ce){
			   ce.printStackTrace();
		   }catch(ParseException pe){
			   pe.printStackTrace();
		   }
		   System.out.println(Arrays.toString(saveResult));
	   }
	   
	   private void queryContact(){
		   try{
			   QueryResult query = connection.queryAll("SELECT Name FROM Contact");
			   if(query.getSize() > 0){
				   System.out.println("Displaying contacts");
				   boolean done = false;
				   while(!done){
					   System.out.println("");
					   SObject[] records = query.getRecords();
					   for(int i = 0; i < records.length; i++){
						   Contact contact = (Contact)records[i];
						   System.out.println("Contact : " + contact.getName());
					   }
					   if(query.isDone()){
						   done = true;
					   }else{
						   query = connection.queryMore(query.getQueryLocator());
					   }
				   }
			   }
		   }catch(ConnectionException ce){
			   ce.printStackTrace();
		   }
		   
		   
	   }
}
