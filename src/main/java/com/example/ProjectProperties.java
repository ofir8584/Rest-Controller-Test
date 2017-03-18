package com.example;
import java.io.File;

public class ProjectProperties {
	
	private static final String LOCATION_ADDRESS = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources";
	public static final File FILES_LOCATION = new File(LOCATION_ADDRESS);
	public static final File LIST_FILE = new File(FILES_LOCATION + File.separator + "items.json");
}
