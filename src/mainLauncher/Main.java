package mainLauncher;
import java.util.Scanner;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try {
			new Servcraft(args);
		} catch (Exception e) {
			System.out.println();
			System.out.println("FATAL ERROR: A error has ocurred. Servcraft can't continue executing after that.");
			System.out.println("Error details: ");
			e.printStackTrace();
			System.out.println("Please send me (Ivelate) the error details to help me to solve it. Together we can make a better Servcraft!");
			System.out.println("Press enter to continue and exit.");
			(new Scanner(System.in)).nextLine();
			System.exit(1);
		}

	}

}
