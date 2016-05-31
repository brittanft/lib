package org.summoners.util;

import java.io.*;
import java.util.*;

public class LineCounter {

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Total: " + getLineCount(new File(args[0])));
	}
	
	public static int getLineCount(File file) throws FileNotFoundException {
		int count = 0;
		if (file.isDirectory()) {
			for (File f : file.listFiles())
				count += getLineCount(f);
		} else if (file.getName().endsWith(".java")) {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				count++;
				scanner.nextLine();
			}
			scanner.close();
			System.out.println(file.getName() + ": " + count + " lines.");
		}
		return count;
	}
}