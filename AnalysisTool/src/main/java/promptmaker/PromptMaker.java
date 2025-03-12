package promptmaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import analyzer.*;
import analyzer.MethodBeCalledAnalyzer.usecase;
import analyzer.MethodCallAnalyzer.Calledenum;
import analyzer.MethodCallAnalyzer.Calledmethod;

public class PromptMaker {
	public MethodCallAnalyzer mca1 = new MethodCallAnalyzer();
	public MethodCallAnalyzer mca2 = new MethodCallAnalyzer();
	public MethodBeCalledAnalyzer mba = new MethodBeCalledAnalyzer();
	public ChangeTypeAnalyzer cta = new ChangeTypeAnalyzer();
	
	public String oldtestcontent;
	
	public String focalmethoddiff;
	
	public ArrayList<String> calledmethodsdiff = new ArrayList<>();
	public ArrayList<String> calledenumsdiff = new ArrayList<>();
	
	public void setoldprojectpath (String path) {
		mba.setoldprojectpath(path);
	}
	
	public void setnewprojectpath (String path) {
		mba.setnewprojectpath(path);
	}
	
	public void setoldclasspath (String path) {
		mba.setoldclasspath(path);
		mca1.setclasspath(path);
		cta.setoldclasspath(path);
	}
	
	public void setnewclasspath (String path) {
		mba.setnewclasspath(path);
		mca2.setclasspath(path);
		cta.setnewclasspath(path);
	}

	public void setoldmethodcontent (String content) {
		mba.setoldmethodcontent(content);
		mca1.setmethodcontent(content);
		cta.setoldmethodcontent(content);
	}
	
	public void setnewmethodcontent (String content) {
		mba.setnewmethodcontent(content);
		mca2.setmethodcontent(content);
		cta.setnewmethodcontent(content);
	}
	
	public void setoldtestcontent (String content) {
		mba.setoldtestcontent(content);
		this.oldtestcontent = content;
	}
	
	public String getPrompt(){
		try {
			mba.analysisbecalled();
			mca1.analysiscalledmethods();
			mca2.analysiscalledmethods();
			cta.analysischangetypes();
			
			String[] m1 = mba.oldmethodcontent.split("\n");
			List<String> or = Arrays.asList(m1);
			String[] m2 = mba.newmethodcontent.split("\n");
			List<String> re = Arrays.asList(m2);
			Patch<String> pa= DiffUtils.diff(or, re);
			int contextSize = 10000;
	        List<String> ud = UnifiedDiffUtils.generateUnifiedDiff("old", "new", or, pa, contextSize);
			focalmethoddiff = String.join("\n", ud);
	
			for (Calledmethod cm2: mca2.calledmethods) {
				boolean isnew = true;
				for (Calledmethod cm1: mca1.calledmethods) {
					if (cm1.signature.equals(cm2.signature) && !MethodCallAnalyzer.isequal(cm1.content, cm2.content) && cm1.path.replace("\\", "/").replace(mba.oldprojectpath, "").equals(cm2.path.replace("\\", "/").replace(mba.newprojectpath, ""))) {
			    		isnew = false;
						String[] t1 = cm1.content.split("\n");
		    			List<String> original = Arrays.asList(t1);
		    			String[] t2 = cm2.content.split("\n");
		    			List<String> revised = Arrays.asList(t2);
		    			Patch<String> patch = DiffUtils.diff(original, revised);
		    	        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("old", "new", original, patch, contextSize);
		    			calledmethodsdiff.add(String.join("\n", unifiedDiff));
		    			break;
					}
					else if (MethodCallAnalyzer.isequal(cm1.content, cm2.content) && cm1.path.replace(mba.oldprojectpath, "").replace("\\", "/").equals(cm2.path.replace(mba.newprojectpath, "").replace("\\", "/"))) {
						isnew  = false;
						break;
					}
		    	}
				if (isnew == true) {
					String[] t1 = new String[]{};
	    			List<String> original = Arrays.asList(t1);
	    			String[] t2 = cm2.content.split("\n");
	    			List<String> revised = Arrays.asList(t2);
	    			Patch<String> patch = DiffUtils.diff(original, revised);
	    	        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("old", "new", original, patch, contextSize);
	    			calledmethodsdiff.add(String.join("\n", unifiedDiff));
	    			break;
				}
	    	}
			
			for (Calledenum ce2: mca2.calledenums) {
				boolean isnew = true;
				for (Calledenum ce1: mca1.calledenums) {
					if (MethodCallAnalyzer.isequal(ce1.content, ce2.content)) {
			    		isnew = false;
		    			break;
					}
		    	}
				if (isnew == true) {
					String[] t1 = new String[]{};
	    			List<String> original = Arrays.asList(t1);
	    			String[] t2 = ce2.content.split("\n");
	    			List<String> revised = Arrays.asList(t2);
	    			Patch<String> patch = DiffUtils.diff(original, revised);
	    	        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("old", "new", original, patch, contextSize);
	    			calledenumsdiff.add(String.join("\n", unifiedDiff));
	    			break;
				}
	    	}
			
			StringBuilder prompt = new StringBuilder(); 
			
			StringBuilder instruction = new StringBuilder();
			instruction.append("Please revise the original test method to accommodate the changes in the focal method, and output the updated test method without any additional text.\r\n\r\n");
			
			StringBuilder oldtestmethod = new StringBuilder();
			oldtestmethod.append("[Original test method]:\r\n\"");
			oldtestmethod.append(oldtestcontent + "\r\n\"\r\n\r\n");
			
			StringBuilder promethoddiff = new StringBuilder();
			promethoddiff.append("[Focal method diff]:\r\n\"");
			promethoddiff.append(focalmethoddiff + "\r\n\"\r\n\r\n");
			
			StringBuilder changetypes = new StringBuilder();
			changetypes.append("[Focal method change types]:\r\n\"");
			for(int i=0; i<cta.changes.size(); i++) {
				changetypes.append(String.valueOf(i+1) + "." + cta.changes.get(i).content + "\r\n");
			}
			changetypes.append("\"\r\n\r\n");
			
			StringBuilder othermethoddiff = new StringBuilder();
			othermethoddiff.append("[Related contexts diff]:\r\n\"");
			int index = 1;
			for(int i=0; i<calledmethodsdiff.size(); i++) {
				othermethoddiff.append(String.valueOf(index) + ".\r\n" + calledmethodsdiff.get(i) + "\r\n\r\n");
				index++;
			}
			for(int i=0; i<calledenumsdiff.size(); i++) {
				othermethoddiff.append(String.valueOf(index) + ".\r\n" + calledenumsdiff.get(i) + "\r\n\r\n");
				index++;
			}
			othermethoddiff.append("\"\r\n\r\n");
			
			StringBuilder usecases = new StringBuilder();
			usecases.append("[Some changes in usage]:\r\n\"");
			int index1 = 1;
			int limit = 3;
			for(int i=0; i<mba.usecasesdiff.size(); i++) {
				if (index1 > limit) {
					break;
				}
				usecases.append(String.valueOf(index1) + ".\r\n" + mba.usecasesdiff.get(i) + "\r\n\r\n");
				index1++;
			}
			usecases.append("\"\r\n\r\n");
			
			prompt.append(instruction);
			prompt.append(oldtestmethod);
			prompt.append(promethoddiff);
			if (cta.changes.size()>0) {
				prompt.append(changetypes);
			}
			if (index > 1) {
				prompt.append(othermethoddiff);
			}
			if (index1 > 1) {
				prompt.append(usecases);
			}
			
			return prompt.toString();
		}catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
