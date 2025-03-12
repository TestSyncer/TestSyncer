package experiment;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import promptmaker.PromptMaker;

public class MakePrompt {
	public static String testsetPath = "...\\Projects"; // The path to the Projects folder(Projects folder is provided in master branch)
	public static String oldpath = "\\old";
	public static String newpath = "\\new";
	
	public static String outputPath = "...\\PromptAndResult"; // The path to the output folder
	
	public static String jsonFilePath = "...\\gen\\test.json"; // The path to test.json(test.json is provided in master branch)
	
	public static void main(String[] args) throws Exception {
		 try {
	            FileReader reader = new FileReader(jsonFilePath);

	            JsonParser parser = new JsonParser();
	            JsonElement jsonElement = parser.parse(reader);

	            JsonArray jsonArray = jsonElement.getAsJsonArray();
	            
	            for (int i = 1; i <= 520; i++) {
	            	JsonElement element = jsonArray.get(i-1);
	                JsonObject jsonObject = element.getAsJsonObject();
	                
	                JsonArray focalDbArray = jsonObject.getAsJsonArray("focal_db");
	                JsonArray testDbArray = jsonObject.getAsJsonArray("test_db");
	                
	                String oldprojectpath = testsetPath + "\\" + String.valueOf(i) + oldpath + "\\" + focalDbArray.get(1).getAsString();
	                String oldclasspath = oldprojectpath + "\\" + focalDbArray.get(5).getAsString();
	                String oldmethodcontent = focalDbArray.get(8).getAsString();
	                
	                String newprojectpath = testsetPath + "\\" + String.valueOf(i) + newpath + "\\" + focalDbArray.get(1).getAsString();
	                String newclasspath = newprojectpath + "\\" + focalDbArray.get(7).getAsString();
	                String newmethodcontent = focalDbArray.get(10).getAsString();
	                
	                String oldtestcontent = testDbArray.get(8).getAsString();
	                
	                PromptMaker pm = new PromptMaker();
	        		pm.setoldprojectpath(oldprojectpath);
	        		pm.setoldclasspath(oldclasspath);
	        		pm.setoldmethodcontent(oldmethodcontent);
	        		pm.setnewprojectpath(newprojectpath);
	        		pm.setnewclasspath(newclasspath);
	        		pm.setnewmethodcontent(newmethodcontent);
	        		pm.setoldtestcontent(oldtestcontent);
	        		        	
	        		String prompt = pm.getPrompt();
	        		
	            	if (prompt.equals("")) {
	            		System.out.println("The prompt is empty.");
	            		continue;
	            	}
	            	
	            	System.out.print(prompt);
	                
	            	String filePath = outputPath + "\\" + String.valueOf(i) + "\\" + "Prompt.txt";
	        		
	                File file = new File(filePath);

	                File parentDir = file.getParentFile();
	                if (parentDir != null && !parentDir.exists()) {
	                    parentDir.mkdirs();
	                }
	            	
	        		try (FileWriter writer = new FileWriter(filePath)) {
	                    writer.write(prompt.toString());
	                    System.out.println("The content was successfully written to the file: " + filePath);
	                } catch (IOException e) {
	                    System.out.println("An error occurred while writing to the file.");
	                    e.printStackTrace();
	                }
	            	           	
	            }
	            
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
}
