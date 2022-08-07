import java.sql.*;
import org.apache.derby.jdbc.EmbeddedDriver;
import java.util.*;

public class Main {
	private static final Scanner in = new Scanner(System.in);
	private static final PrintStream out = System.out;

  public static void main(String[] args) {

		public static void main(String[] args) {
		try (Connection conn = getConnection("jdbc:derby:db/studentdb")) {
			displayMenu();
			loop: while (true) {
				switch (requestString("Selection (0 to quit, 9 for menu)? ")) {
				case "0": // Quit
					break loop;

				case "1": // Reset
					resetTables(conn);
					break;

				case "2": // List patients
					listPatients(conn);
					break;
				case "3": // Show appointments
					showAppointments(conn);
					break;
				case "4": // Add patient
					addPatient(conn);
					break;

				default:
					displayMenu();
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		out.println("Done");
	}



    /**
 * Attempt to open a connection to an embedded Derby database at the given URL.
 * If the database does not exist, create it with empty tables.
 *
 * @param url
 * @return
 */
private static Connection getConnection(String url) {
  Driver driver = new EmbeddedDriver();

  // try to connect to an existing database
  Properties prop = new Properties();
  prop.put("create", "false");
  try {
    Connection conn = driver.connect(url, prop);
    return conn;
  } catch (SQLException e) {
    // database doesn't exist, so try creating it
    try {
      prop.put("create", "true");
      Connection conn = driver.connect(url, prop);
      createTables(conn);
      return conn;
    } catch (SQLException e2) {
      throw new RuntimeException("cannot connect to database", e2);
    }
  }
}


    private static void displayMenu() {
		out.println("0: Quit");
		out.println("1: Reset tables");
		out.println("2: List patients");
		out.println("3: Show appointments");
		out.println("4: Add patients");
	}

	private static String requestString(String prompt) {
		out.print(prompt);
		out.flush();
		return in.nextLine();
	}

	private static void createTables(Connection conn) {
		// First clean up from previous runs, if any
		dropTables(conn);

		// Now create the schema
		addTables(conn);
	}
//  do Update helps us pass sql querry
  private static void doUpdate(Connection conn, String statement, String message) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(statement);
			System.out.println(message);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void doUpdateNoError(Connection conn, String statement, String message) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(statement);
			System.out.println(message);
		} catch (SQLException e) {
			// Ignore error
		}
	}


  // create tables for clinic database
  public static void addTable(Connection conn){
    StringBuilder sb = new StringBuilder();
		sb.append("create table DOCTOR(");
		sb.append("  idDoctor int not null,");
		sb.append("  doctorName varchar(8) not null,");
    sb.append("  specialty varchar(255) not null,");
		sb.append("  primary key (idDoctor)");
		sb.append(")");
    doUpdate(conn, sb.toString(), "Table DOCTOR created.");


    StringBuilder sb = new StringBuilder();
		sb.append("create table PATIENT(");
		sb.append("  idPatient int not null,");
		sb.append("  patientName varchar(8) not null,");
    sb.append("  phoneNumber varchar(20) not null,");
    sb.append("  primary key (idPatient)");
		sb.append(")");
    doUpdate(conn, sb.toString(), "Table PATIENT created.");


    StringBuilder sb = new StringBuilder();
		sb.append("create table APPOINTMENT(");
		sb.append("  idAppointment int not null,");
		sb.append("  idPatient int not null,");
		sb.append("  idDoctor int not null,");
    sb.append("  reason varchar(500) not null,");
    sb.append("  price float not null,");
    sb.append("  primary key (idAppointment)");
		sb.append("  foreign key (idDoctor) references DOCTOR on delete set null");
    sb.append("  foreign key (idPatient) references PATIENT on delete set null");
		sb.append(")");
    doUpdate(conn, sb.toString(), "Table APPOINTMENT created.");
  }

  /**
	 * Delete the tables for the clinic database. Note that the tables are dropped
	 * in the reverse order that they were created, to satisfy referential integrity
	 * (foreign key) constraints.
	 *
	 * @param conn
	 */
	private static void dropTables(Connection conn) {
		doUpdateNoError(conn, "drop table APPOINTMENT", "Table APPOINTMENT dropped.");
		doUpdateNoError(conn, "drop table PATIENT", "Table PATIENT dropped.");
		doUpdateNoError(conn, "drop table DOCTOR", "Table DOCTOR dropped.");

	}

	/**
 * Delete the contents of the tables, then reinsert the sample data .
 * Again, note that the order is important, so that foreign key references
 * already exist before they are used.
 *
 * @param conn
 */
private static void resetTables(Connection conn) {
	try (Statement stmt = conn.createStatement()) {
		int count = 0;
		count += stmt.executeUpdate("delete from APPOINTMENT");
		count += stmt.executeUpdate("delete from PATIENT");
		count += stmt.executeUpdate("delete from DOCTOR");
		System.out.println(count + " records deleted");

		String[] doctorVals = {
				"(10, 'Taha' , 'Cancer Specialist')", "(20, 'Omer' , 'Eyes Specialist')", "(30, 'Soha', 'Dentist')"
		};
		count = 0;
		for (String val : doctorVals) {
			count += stmt.executeUpdate("insert into DOCTOR(idDoctor, doctorName, specialty ) values " + val);
		}
		System.out.println(count + " DOCTORT records inserted.");

		String[] patientVals = {
				"(1, 'murtaza', '765-264-3000')",
				"(2, 'mustafa', '765-264-3001')",
				"(3, 'musa', '765-264-3002')",
				"(4, 'suri', '765-264-3003')",
		};
		count = 0;
		for (String val : patientVals) {
			count += stmt.executeUpdate("insert into PATIENT(idPatient, patientName, phoneNumber) values " + val);
		}
		System.out.println(count + " PATIENT records inserted.");

		String[] appointmentVals = {
				"(12, 1, 10, 'has Lung cancer', 500)",
				"(22, 2, 20, 'weak eye-sight', 200)",
				"(32, 3, 30, 'wisdom tooth', 1000)",
				};
		count = 0;
		for (String val : appointmentVals) {
			count += stmt.executeUpdate("insert into APPOINTMENT(idAppointment, idPatient, idDoctor, reason, price) values " + val);
		}
		System.out.println(count + " APPOINTMENT records inserted.");


	} catch (SQLException e) {
		e.printStackTrace();
	}

}

/**
 * Print a table of all patients with their id number, name, and phoneNumber.
 *
 * @param conn
 */
private static void listPatients(Connection conn) {
	StringBuilder query = new StringBuilder();
	query.append("select p.idPatient, p.patientName, p.phoneNumber");
	query.append("  from PATIENT p");

	try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query.toString())) {
		//  correct formating
		out.printf("%-3s %-10s %-4s %-8s\n", "IdPatient", "patientName", "phoneNumber");
		out.println("----------------------------");
		while (rs.next()) {
			int idPatient = rs.getInt("idPatient");
			String patientName = rs.getString("patientName");
			int phoneNumber = rs.getInt("phoneNumber");

			out.printf("%3d %-10s %-4d %-8s\n", idPatient, patientName, phoneNumber);
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}

}

//  show appointments

private static void showAppointments(Connection conn) {
	StringBuilder query = new StringBuilder();
	query.append("select a.idAppointment, a.idPatient, a.idDoctor, a.reason, a.price");
	query.append("  from APPOINTMENT a");

	try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query.toString())) {
		//  correct formating
		out.printf("%-3s %-10s %-4s %-8s\n", "idAppointment", "idPatient", "idDoctor", "reason", "price");
		out.println("----------------------------");
		while (rs.next()) {
			int idAppointment = rs.getInt("idAppointment");
			int idPatient = rs.getInt("idPatient");
			int idDoctor = rs.getInt("idDoctor");
			String reason = rs.getString("reason");
			String price = rs.getString("price");
			out.printf("%3d %-10s %-4d %-8s\n", idAppointment, idPatient, idDoctor, reason , price);
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}

}

/**
	 * Request information to add a new patient to the database. The id number must
	 * be unique.
	 *
	 * @param conn
	 */
	private static void addPatient(Connection conn) {
		String pid = requestString("Id number? ");
		String pname = requestString("Patient name? ");
		String phoneNumber = requestString("phoneNumber? ");


		StringBuilder command = new StringBuilder();
		command.append("insert into PATIENT values (?, '?', ?)");


		try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
			pstmt.setString(1, pid);
			pstmt.setString(2, pname);
			pstmt.setString(3, phoneNumber);
			int count = pstmt.executeUpdate();

			out.println(count + " patient(s) inserted");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}




  }

}
