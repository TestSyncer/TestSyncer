package experiment;

import promptmaker.PromptMaker;

public class Usage {
	public static void main(String[] args) throws Exception {
		 String oldprojectpath = "D:/桌面/Projects/1/old/winder/Universal-G-Code-Sender";
         String oldclasspath = "D:/桌面/Projects/1/old/winder/Universal-G-Code-Sender/ugs-core/src/com/willwinder/universalgcodesender/listeners/ControllerListener.java";
         String oldmethodcontent = "void messageForConsole(String msg, Boolean verbose);";
         
         String newprojectpath = "D:/桌面/Projects/1/new/winder/Universal-G-Code-Sender";
         String newclasspath = "D:/桌面/Projects/1/new/winder/Universal-G-Code-Sender/ugs-core/src/com/willwinder/universalgcodesender/listeners/ControllerListener.java";
         String newmethodcontent = "void messageForConsole(MessageType type, String msg);";
         
         String oldtestcontent = "@Test\r\n"
         		+ "    public void testMessageForConsole() {\r\n"
         		+ "        System.out.println(\"messageForConsole\");\r\n"
         		+ "        String msg = \"\";\r\n"
         		+ "        Boolean verbose = null;\r\n"
         		+ "        GUIBackend instance = new GUIBackend();\r\n"
         		+ "        instance.messageForConsole(msg, verbose);\r\n"
         		+ "        // TODO review the generated test code and remove the default call to fail.\r\n"
         		+ "        fail(\"The test case is a prototype.\");\r\n"
         		+ "    }";
         
         PromptMaker pm = new PromptMaker();
 		 pm.setoldprojectpath(oldprojectpath);
 		 pm.setoldclasspath(oldclasspath);
 		 pm.setoldmethodcontent(oldmethodcontent);
 		 pm.setnewprojectpath(newprojectpath);
 		 pm.setnewclasspath(newclasspath);
 		 pm.setnewmethodcontent(newmethodcontent);
 		 pm.setoldtestcontent(oldtestcontent);
 		        	
 		 String prompt = pm.getPrompt();
 		 
 		 System.out.println(prompt);
	}
}
