import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Scanner;
import java.util.Stack;

import tuc.eced.cs201.io.StandardInputRead;

public class PROJECT_2_PART_1 {

	public static Connection conn;
	
	public static void main(String[] args) {
		
		StandardInputRead reader = new StandardInputRead();
		
		DbApp();
		
		System.out.println("Welcome to our remote control server.\nChoose one of the following choices.");
		boolean closemenu = true;
		int choice = 0;
		String ipaddr;
		String dbname;
		String username;
		String password;
		int acyear;
		String acseas;
		String coursecode;
		int amka;
		
		while (closemenu == true) {
		
		choice = 0;
		
		System.out.println("1) Enter credentials for Database connection.\n"
				+ "2) Commit a transaction or start a new one.\n"
				+ "3) Cancel a transaction or start a new one.\n"
				+ "4) Print students registered on a specific Course for a specific Semester.\n"
				+ "5) Print a specific student's grades for a specific Semester.\n"
				+ "6) Exit.");
		
		while (choice < 1 || choice > 6) {
			
			choice = reader.readPositiveInt("Enter your choice (1-6): ");
		}
		
		if (choice == 1) {
			
			ipaddr = reader.readString("Enter the IP address: ");
			dbname = reader.readString("Enter the Database name: ");
			username = reader.readString("Enter the username: ");
			password = reader.readString("Enter the password: ");
			
			//ipaddr = "localhost";
			//dbname = "lab";
			//username = "postgres";
			//password = "postgres";

			try {
				
				conn = DriverManager.getConnection("jdbc:postgresql://" + ipaddr + ":5432/" + dbname, username, password);
				
				System.out.println("Connection is successfull!");
				startTransactions();
				
			} catch (SQLException e) {
				System.out.println("Connection is not succesfull.");
				e.printStackTrace();
			}
		}
		
		else if (choice == 2) {
			
			commit();
			System.out.println("Transaction is commited.");
		}

		else if (choice == 3) {
	
			abort();
			System.out.println("Transaction is aborted.");
		}

		else if (choice == 4) {
	
			acyear = reader.readPositiveInt("Enter the academic year: ");
			acseas = reader.readString("Enter the academic season: ");
			coursecode = reader.readString("Enter the course's code: ");
			
			//acyear = 2013;
			//acseas = "spring";
			//coursecode = "ΠΛΗ 302";
			
			showStudents(acyear, acseas, coursecode);
		}

		else if (choice == 5) {
	
			amka = reader.readPositiveInt("Enter the student's amka: ");
			acyear = reader.readPositiveInt("Enter the academic year: ");
			acseas = reader.readString("Enter the academic season: ");
			
			//amka = 6;
			//acyear = 2012;
			//acseas = "spring";
			
			showGrades(acyear, acseas, amka);
		}

		else {
	
			closemenu = false;
			System.out.println("Thank you for using our System!");
		}
		
		}
		
}

	public static void showGrades(int acyear, String acseas, int amka) {
		try {

			PreparedStatement pst2 = conn.prepareStatement("select count(*) from \"Course\" ;");
			ResultSet res2 = pst2.executeQuery();
			res2.next();
			int size = res2.getInt(1);
			res2.close();
			
PreparedStatement pst = conn.prepareStatement("select course_code, course_title, lab_grade, exam_grade, serial_number from (((\"Register\" natural join \"CourseRun\") natural join \"Course\") regcr inner join \"Semester\" sem on regcr.semesterrunsin = sem.semester_id) as x where x.amka = ? and academic_year = ? and academic_season = ?::semester_season_type order by course_code asc; ");

		pst.setInt(1, amka);
		pst.setInt(2, acyear);
		pst.setString(3, acseas);

		ResultSet res = pst.executeQuery();
		
		int counter = 0;
		StandardInputRead reader2 = new StandardInputRead();
		String[] array1 = new String[size + 1];
		String[] array2 = new String[size + 1];
		int[] array3 = new int[size + 1];
		int[] array4 = new int[size + 1];
		int[] array5 = new int[size + 1];
		int nextmove;
		boolean close = true;
		int row;
		int labgrade;
		int examgrade;
		Scanner scan = new Scanner(System.in);
		Stack<Savepoint> savepoints = new Stack<Savepoint>();
		
		while (res.next()) {
			
			counter = counter + 1;
			
			array1[counter] = res.getString(1);
			array2[counter] = res.getString(2);
			array3[counter] = res.getInt(3);
			array4[counter] = res.getInt(4);
			array5[counter] = res.getInt(5);
			
			System.out.println("Result: " + counter + " course_code: " + res.getString(1) + " course_title: " + res.getString(2) + " lab_grade: " + res.getInt(3) + " exam_grade: " + res.getInt(4));
		}
		
		res.close();
		
		while(close == true) {
			
			System.out.println("\nPress a number between 1 and " + counter + " to change the lab and exam grades of a course.");
			System.out.println("Press 0 to return to the main menu.");
			System.out.println("Press -1 to cancel the last grade update.");
				
			nextmove = scan.nextInt();
			
			if (nextmove == 0) {
				
				close = false;
				System.out.println("You will return to the main menu now.");
			}
			
			else if (nextmove == -1) {
				
				if (savepoints.empty() == true) {
					
					System.out.println("There are no actions to cancel.");
				}
				
				else {
					
					conn.rollback(savepoints.pop());
				}
			}
			
			else if (nextmove > 0 && nextmove <= counter) {
				
				row = nextmove;
				
				labgrade = -1;
				
				while (labgrade < 0 || labgrade > 10) {
					
				labgrade = reader2.readPositiveInt("Enter the new lab grade (0-10): ");
				}
				
				examgrade = -1;
				
				while (examgrade < 0 || examgrade > 10) {
					
					examgrade = reader2.readPositiveInt("Enter the new exam grade (0-10): ");
				}
				
				PreparedStatement pst3 = conn.prepareStatement("update \"Register\" set lab_grade = ?::numeric , exam_grade = ?::numeric where course_code = ?::character(7) and amka = ? and serial_number = ? ;");
				
				pst3.setInt(1, labgrade);
				pst3.setInt(2, examgrade);
				pst3.setString(3, array1[row]);
				pst3.setInt(4, amka);
				pst3.setInt(5, array5[row]);
				
				savepoints.push(conn.setSavepoint());
				
				pst3.executeUpdate();
				
			}
			
			else {
				
				System.out.println("Enter a number between -1 and " + counter);
			}
		}
				
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("Query failed.");
	}
	
}
	
	public static void showStudents(int acyear, String acseas, String coursecode) {
		try {
			
PreparedStatement pst = conn.prepareStatement("select x.amka from ((\"Register\" natural join \"CourseRun\") regcr inner join \"Semester\" sem on regcr.semesterrunsin = sem.semester_id) as x where x.academic_year = ? and x.academic_season = ?::semester_season_type and x.course_code = ?::character(7) ;");

			pst.setInt(1, acyear);
			pst.setString(2, acseas);
			pst.setString(3, coursecode);

			ResultSet res = pst.executeQuery();
		
			while (res.next()) {
				System.out.println("amka: " + res.getInt("amka"));
			}
			
			res.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query failed.");
		}
		
	}
	
	public static void DbApp() {
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("Driver Found!");
		} catch (ClassNotFoundException e) {
			System.out.println("Driver not found!");
		}
	}
	
	public static void startTransactions() {
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			System.out.println("Transaction commit failed.");
			e.printStackTrace();
		}
	}
	
	public static void abort() {
		try {
			conn.rollback();
		} catch (SQLException e) {
			System.out.println("Transaction rollback failed.");
			e.printStackTrace();
		}
	}
	
}
