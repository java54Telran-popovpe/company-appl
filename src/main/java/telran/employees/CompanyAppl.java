package telran.employees;

import telran.io.Persistable;
import telran.view.*;

import java.util.*;
public class CompanyAppl {

	private static final String FILE_NAME = "employeesTest.data";
	private static final String[] departments = {"Audit", "Development", "QA"};;

	public static void main(String[] args) {
		Company company = new CompanyMapsImpl();
		try {
			((Persistable)company).restore(FILE_NAME);
		} catch (Exception e) {
			
		}
		List<Item> companyItems =
				CompanyApplItems.getCompanyItems(company,
						new HashSet<String>(List.of(departments)));
		companyItems.add(Item.of("Exit & save",
				io -> ((Persistable)company).save(FILE_NAME), true));
		Menu menu = new Menu("Please choose action",
				companyItems.toArray(Item[]::new));
		menu.perform(new SystemInputOutput());
	}
}