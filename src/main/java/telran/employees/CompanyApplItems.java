package telran.employees;

import java.util.*;
import java.util.function.Function;

import org.json.JSONObject;

import telran.view.InputOutput;
import telran.view.Item;
import telran.view.Menu;

public class CompanyApplItems {
	private static final String EMPTY_COMPANY_MESSAGE = "Impossible to display data - no employees exist in the company\n";
	private static final int MIN_EMPLOYEE_ID = 1000;
	private static final int MAX_EMPLOYEE_ID = 10_000;
	static Company company;
	static HashSet<String> departments;

	public static List<Item> getCompanyItems(Company company, HashSet<String> departments) {
		CompanyApplItems.company = company;
		CompanyApplItems.departments = departments;
		Item[] items = { 	new Menu("add employee", getTitlesItems().toArray(Item[]::new)),
							Item.of("display employee data", CompanyApplItems::getEmployee),
							Item.of("remove employee", CompanyApplItems::removeEmployee),
							new Menu("display department budget", getDepartmentItems().toArray(Item[]::new)),
							Item.of("display departments", CompanyApplItems::getDepartments),
							Item.of("display managers with most factor", CompanyApplItems::getManagersWithMostFactor), };
		return new ArrayList<Item>(List.of(items));

	}
	
	private static List<Item> getDepartmentItems() {
		List<Item> items = new ArrayList<>();
		for(String department: departments ) {
			items.add(Item.of(department, io -> io.writeLine(String.format("for department %s budget equals to %d%n", 
					department, company.getDepartmentBudget(department))), true));
		}
		items.add(Item.of("Return to main menu", io -> {}, true));
		return items;

	}
	
	private static List<Item> getTitlesItems( ) {
		List<Item> items = new ArrayList<>();
		items.add(Item.of("WageEmployee", io -> addEmployee(io,CompanyApplItems::getWageEmployee), true));
		items.add(Item.of("Manager", io -> addEmployee(io,CompanyApplItems::getManager), true));
		items.add(Item.of("SalesPerson", io -> addEmployee(io,CompanyApplItems::getSalesPerson), true));
		items.add(Item.of("Return to main menu", io -> {}, true));
		return items;

	}
	
	private static void addEmployee( InputOutput io, Function<InputOutput, Employee> func ) {
		Employee emp = func.apply(io);
		company.addEmployee(func.apply(io));
		io.writeLine(String.format("Employee with id %d succesfully added", emp.getId()));
	}
	
	private static Employee getSalesPerson (InputOutput io) {
		WageEmployee wageEmpl = (WageEmployee)getWageEmployee(io);
		float percents = io.readNumberRange("Enter percents", "Wrong percents value", 0.5, 2).floatValue();
		long sales = io.readNumberRange("Enter sales", "Wrong sales value", 500, 50000).longValue();
		return new SalesPerson(wageEmpl.getId(), wageEmpl.getBasicSalary(), wageEmpl.getDepartment(), wageEmpl.getHours(),
				wageEmpl.getWage(), percents, sales);
	}

	private static Employee getManager(InputOutput io) {
		Employee empl = readEmployee(io);
		float factor = io.readNumberRange("Enter factor", "Wrong factor value", 1.5, 5).floatValue();
		return new Manager(empl.getId(), empl.getBasicSalary(), empl.getDepartment(), factor);
	}

	private static Employee getWageEmployee(InputOutput io) {
		Employee empl = readEmployee(io);
		int hours = io.readNumberRange("Enter working hours", "Wrong hours value", 10, 200).intValue();
		int wage = io.readNumberRange("Enter hour wage", "Wrong wage value", MIN_EMPLOYEE_ID, MAX_EMPLOYEE_ID).intValue();
		return new WageEmployee(empl.getId(), empl.getBasicSalary(), empl.getDepartment(), hours, wage);
	}

	private static Employee readEmployee(InputOutput io) {

		long id = readEmployeeID(io);
		int basicSalary = io.readNumberRange("Enter basic salary", "Wrong basic salary", 2000, 20000).intValue();
		String department = io.readStringOptions("Enter department " + departments, "Wrong department", departments);
		return new Employee(id, basicSalary, department);
	}

	private static void getEmployee(InputOutput io) {
			Optional<Employee> employee  = Optional.ofNullable(io.readObject("Enter employee id", "Error occured", 
					str -> company.getEmployee(Long.parseLong(str))));
			employee.ifPresentOrElse(emp -> writeEmployee(io, emp), () -> io.writeLine("Employee not found"));
	}

	private static void writeEmployee(InputOutput io, Employee employee) {
		JSONObject jsonObject = new JSONObject( employee.getJSON());
		replaceClassNameWithTitle( jsonObject );
		String[] fieldNames = JSONObject.getNames(jsonObject);
		int maxLength = Arrays.stream(fieldNames).mapToInt(String::length).max().orElseThrow();
		for ( String fieldName: JSONObject.getNames(jsonObject)) {
			int numberOfTabs = getNumberOfTabs(fieldName, maxLength);
			io.writeLine(String.format("%s%s%s", fieldName, "\t".repeat(numberOfTabs), jsonObject.get(fieldName).toString()));
		}
	}


	private static int getNumberOfTabs(String fieldName, int maxLength) {
		int tabPosition =  (maxLength / 8 + 1) * 8;
		return ( tabPosition - fieldName.length() ) / 8 + 1;
	}

	private static void replaceClassNameWithTitle(JSONObject jsonObject) {
		String className = jsonObject.getString("className");
		String title = className.substring(className.lastIndexOf('.') + 1);
		jsonObject.remove("className");
		jsonObject.put("title", title);
	}

	private static void removeEmployee(InputOutput io) {
		long id = readEmployeeID(io);
		try {
			company.removeEmployee(id);
			io.writeLine(String.format("The employee with id %d removed", id));
		}
		catch (NoSuchElementException e) {
			io.writeLine(String.format("Employee with id %d not found", id));
		}
	}

	private static long readEmployeeID(InputOutput io) {
		return io.readNumberRange(String.format("Enter id value (should be between %d and %d)", MIN_EMPLOYEE_ID, MAX_EMPLOYEE_ID - 1), 
				"Wrong id value", MIN_EMPLOYEE_ID, MAX_EMPLOYEE_ID).longValue();
	}

	private static void getDepartments(InputOutput io) {
		String[] departments = company.getDepartments();
		if ( departments.length != 0 ) {
			for ( String department: departments) {
				io.writeLine(department);
			}
		} else {
			io.writeLine(EMPTY_COMPANY_MESSAGE);
		}
	}

	private static void getManagersWithMostFactor(InputOutput io) {
		Manager[] managersWithMostFactor = company.getManagersWithMostFactor();
		if ( managersWithMostFactor.length == 0 ) {
			io.writeLine("No managers exist in the company\n");
		} else {
			Float maxFactorValue = managersWithMostFactor[0].factor;
			io.writeLine(String.format("Maximum factor value %.2f is related to the following managers%n", maxFactorValue));
			for(Manager manager: managersWithMostFactor) {
				writeEmployee(io,manager);
				io.writeLine("");
			}
		}
	}
}