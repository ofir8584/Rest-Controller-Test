package entity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Item {

	private long id;
	private String name;
	private String description;
	private double weight;
	private String dimensions;

	public Item() {}

	public Item(long id, String name, String description, double weight, String dimensions) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.weight = weight;
		this.dimensions = dimensions;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getDimensions() {
		return dimensions;
	}

	public void setDimensions(String dimensions) {
		this.dimensions = dimensions;
	}

	public static Item[] parseItemsList(File json) throws IOException, ParseException{
		FileReader reader = new FileReader(json);
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = (JSONArray) parser.parse(reader);
		ArrayList<Item> itemArray = new ArrayList<>();
		for (Object obj : jsonArray){
			JSONObject jobj = (JSONObject) obj;
			long id = (long)jobj.get("id");
			String name = (String)jobj.get("name");

			/*
			String description = (jobj.get("description") != null) ? (String)jobj.get("description") : null;
			double weight = (jobj.get("weight") != null) ? (double)jobj.get("weight") : 0;
			String dimensions = (jobj.get("dimensions") != null) ? (String)jobj.get("dimensions") : null;
			 */

			itemArray.add(new Item(id, name, null, 0, null));
		}

		return itemArray.toArray(new Item[itemArray.size()] );
	}

	public static Item parseItem(File json) throws JsonParseException, JsonMappingException, IOException{
		/*
		FileReader reader = new FileReader(json);
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(reader);
		long id = (long)jsonObj.get("id");
		String name = (String)jsonObj.get("name");			
		String description =  (String)jsonObj.get("description");
		double weight = (double)jsonObj.get("weight");
		String dimensions = (String)jsonObj.get("dimensions");

		return new Item(id, name, description, weight, dimensions) ;
		*/
		 
		ObjectMapper mapper = new ObjectMapper();
		Item item = mapper.readValue(json, Item.class);
		return item; 
	}
	
	public void writeItem(File file) throws IOException{
		file.createNewFile();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(file,this);
	}
}
