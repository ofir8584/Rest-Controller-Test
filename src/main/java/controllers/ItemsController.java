package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.ProjectProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import entity.Item;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;

/**
 * 
 * @author Ofir
 * Service for item management.
 */
@RestController
@RequestMapping("/items")
public class ItemsController {

/**
 * Returns all items in item list.
 * @return All items in items list.
 */
	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation(value = "Get all items in list",
    notes = "Gets a list of all items, only id and name fields",
    response = Item.class,
    responseContainer = "List")
	 @ApiResponses(value = { 
		      @ApiResponse(code = 404, message = "List file not found", 
		                   responseHeaders = @ResponseHeader(name = "error", description = "File not found/ could not be opened.", response = String.class)),
		      @ApiResponse(code = 422, message = "Unexpected format", 
	                   responseHeaders = @ResponseHeader(name = "error", description = "File doesn't contain correct JSON format.", response = String.class))	 })
	
	public ResponseEntity<Item[]> getAllItems() {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(Item.parseItemsList(ProjectProperties.LIST_FILE));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).header("error", "File not found/ could not be opened.").body(null);
		} catch (ParseException e) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).header("error", "File doesn't contain correct JSON format.").body(null);
		} 
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Item> getItem(@PathVariable("id") long id){
		File json = new File(ProjectProperties.FILES_LOCATION + File.separator + id + ".json");
		if (!json.exists()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).header("error", "Game with id " + id + " doesn't exist.").body(null);
		}
		try {
			Item item = Item.parseItem(json);
			return ResponseEntity.status(HttpStatus.OK).body(item);
		} catch (JsonParseException|JsonMappingException e) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).header("error", "File doesn't contain correct JSON format.").body(null);
		} catch (IOException e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).header("error", "File can't be opened.").body(null);
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Long> postItem(@RequestBody Item post) throws IOException{
		String list = getList();
		long id = findNewId(list);
		post.setId(id);
		addToList(post,list);
		File toWrite = new File (ProjectProperties.FILES_LOCATION + File.separator + id + ".json");
		post.writeItem(toWrite);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Boolean> deleteItem(@PathVariable("id") long id){
		File toDelete = new File (ProjectProperties.FILES_LOCATION + File.separator + id + ".json");
		if(!toDelete.exists()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).header("error","No game with ID " + id + " exist.").body(false);
		} else {
			try {
				String list = getList();
				removeFromList(id, list);
			} catch (IOException e) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).header("error", "List file not found/ could not be opened.").body(false);
			}
			toDelete.delete();
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(true);

		}
	}

	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<Boolean> updateItem(@RequestBody Item item){
		File toWrite = new File (ProjectProperties.FILES_LOCATION + File.separator + item.getId() + ".json");
		if(!toWrite.exists()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).header("error", "Game with ID " + item.getId() + "doesn't exist.").body(false);
		}
		try {
			//update list file
			String list = getList();
			removeFromList(item.getId(), list);
			addToList(item, list);
			//write item to file
			item.writeItem(toWrite);
			return ResponseEntity.status(HttpStatus.CREATED).body(true);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).header("error", "List file not found/ could not be opened.").body(false);
		}
	}

	private long findNewId(String list) throws IOException {
		long id = 1;
		while (list.contains("\"id\": " + id)){
			id++;
		}

		return id;
	}

	private String getList() throws IOException{
		FileReader reader = new FileReader(ProjectProperties.LIST_FILE);
		BufferedReader buffReader = new BufferedReader(reader);
		String list = "";
		String newLine = "";
		newLine = buffReader.readLine();
		while (newLine != null){
			list += newLine + System.lineSeparator();
			newLine = buffReader.readLine();
		}

		buffReader.close();
		return list;
	}

	private void addToList(Item post, String list) throws IOException {
		list = list.substring(0, list.length()-4);
		list += "," + System.lineSeparator() + 
				"    {" + System.lineSeparator() + 
				"        \"id\": " + post.getId() + "," + System.lineSeparator() + 
				"        \"name\": \"" + post.getName() + "\"" + System.lineSeparator() +
				"    }" + System.lineSeparator() + 
				"]";
		writeList(list);
	}

	private void removeFromList(long id, String list) throws IOException {
		int index = list.indexOf("\"id\": " + id);
		String before = "";
		int end = list.indexOf('}', index) + 1;
		end = list.charAt(end) == ',' ? end + 3 : end;
		String after = list.substring(end, list.length());

		if (index > 16)
		{
			before = list.substring(0, index-15);
			if (end >= list.length()-5){
				before = before.substring(0,before.length()-3) + System.lineSeparator();
				after = after.substring(2,after.length());
			}
		} else {
			before = "[" + System.lineSeparator();
		}

		list = before + after;
		writeList(list);
	}

	private void writeList(String list) throws FileNotFoundException{
		PrintWriter writer = new PrintWriter(ProjectProperties.LIST_FILE);
		System.out.println(list);
		writer.print(list);
		writer.close();
	}

}
