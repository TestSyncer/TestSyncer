package analyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class MethodBeCalledAnalyzer {
	public static class usecase{
		public String signature;
		public String content;
		public String contentwithoutcomment;
		public String path;
	    @Override
	    public boolean equals(Object obj) {
	        if(this == obj){
	            return true;
	        }
	        if(obj == null){
	            return false;
	        }
	        if(!(obj instanceof usecase)){
	            return false;
	        }
	        usecase other = (usecase)obj;
	        return this.signature.equals(other.signature) && this.contentwithoutcomment.equals(other.contentwithoutcomment) && this.path.equals(other.path);
	    }
	}
	
	public ArrayList<usecase> usecases1 = new ArrayList<>();
	public ArrayList<usecase> usecases2 = new ArrayList<>(); 
	public ArrayList<String> usecasesdiff = new ArrayList<>();
	
	public ArrayList<String> parts = new ArrayList<>();
	
	public String oldprojectpath = "";
	public String oldclasspath = "";
	public String oldmethodsignature = "";
	public String oldmethodcontent = "";
	public String newprojectpath = "";
	public String newclasspath = "";
	public String newmethodsignature = "";
	public String newmethodcontent = "";
	public String oldtestcontent = "";
	
	public CombinedTypeSolver typeSolver1 = new CombinedTypeSolver();
	public CombinedTypeSolver typeSolver2 = new CombinedTypeSolver();
	
	public static String getmethodsignaturefromcontent(String methodcontent) { 
        String regex = "(\\w[\\w<>]*\\s+)?(\\w+)\\s*\\(([^)]*)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(methodcontent);
        String methodName = ""; 
        String parameters = ""; 
        if (matcher.find()) {
            methodName = matcher.group(2);
            parameters = "(" + matcher.group(3) + ")";
        }
		return methodName + parameters;
	}
	
	public void setoldprojectpath(String str) { 
		this.oldprojectpath = str.replace("\\", "/");
	}
	
	public void setoldclasspath(String str) {
		this.oldclasspath = str.replace("\\", "/");
	}
	
	public void setoldmethodsignature(String str) {
		this.oldmethodsignature = getmethodsignaturefromcontent(str);
	}
	
	public void setoldmethodcontent(String str) {
		this.oldmethodcontent = str;
		setoldmethodsignature(getmethodsignaturefromcontent(str));
	}
	
	public void setnewprojectpath(String str) {
		this.newprojectpath = str.replace("\\", "/");
	}
	
	public void setnewclasspath(String str) {
		this.newclasspath = str.replace("\\", "/");
	}
	
	public void setnewmethodsignature(String str) {
		this.newmethodsignature = getmethodsignaturefromcontent(str);
	}
	
	public void setnewmethodcontent(String str) {
		this.newmethodcontent = str;
		setnewmethodsignature(getmethodsignaturefromcontent(str));
	}
	
	public void setoldtestcontent(String str) {
		this.oldtestcontent = str;
	}
	
	public static boolean isequal(String str1, String str2) {
		String temp1 = str1.replaceAll("\\s*", "");
		String temp2 = str2.replaceAll("\\s*", "");
		return temp1.equals(temp2);
	}
	
	public static boolean iscontain(String str1, String str2) {
		String temp1 = str1.replaceAll("\\s*", "");
		String temp2 = str2.replaceAll("\\s*", "");
		return temp1.contains(temp2);
	}
	
	private void configuresolver() {
		String[] ps1 = oldclasspath.split("[\\\\/]");
		String base = ps1[0];
        for (int i = 1; i < ps1.length - 1; i++) {
        	base = base + "/" + ps1[i];
        	ps1[i] = base;
        }
        for (int i = 1; i < ps1.length - 1; i++) {
        	typeSolver1.add(new JavaParserTypeSolver(Paths.get(ps1[i].toString())));
        }
        typeSolver1.add(new ReflectionTypeSolver());
        
        String[] ps2 = newclasspath.split("[\\\\/]");
		base = ps2[0];
        for (int i = 1; i < ps2.length - 1; i++) {
        	base = base + "/" + ps2[i];
        	ps2[i] = base;
        }
        for (int i = 1; i < ps2.length - 1; i++) {
        	typeSolver2.add(new JavaParserTypeSolver(Paths.get(ps2[i].toString())));
        }
        typeSolver2.add(new ReflectionTypeSolver());
	}
	
	private boolean checkMethodSignature(MethodCallExpr methodCall, MethodDeclaration targetMethod) {
	    List<Expression> methodCallArguments = methodCall.getArguments();
	    List<Parameter> targetMethodParameters = targetMethod.getParameters();
	    if (methodCallArguments.size() != targetMethodParameters.size()) {
	        return false;
	    }
	    return true;
	}
	
	public static Map<String, Integer> getNGrams(List<String> tokens, int n) {
        Map<String, Integer> ngrams = new HashMap<>();
        for (int i = 0; i <= tokens.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                sb.append(tokens.get(j)).append(" ");
            }
            String gram = sb.toString().trim();
            ngrams.put(gram, ngrams.getOrDefault(gram, 0) + 1);
        }
        return ngrams;
    }

    public static double calculateBLEU(List<String> reference, List<String> candidate, int maxN) {
        double score = 0.0;
        for (int n = 1; n <= maxN; n++) {
            Map<String, Integer> refNgrams = getNGrams(reference, n);
            Map<String, Integer> candNgrams = getNGrams(candidate, n);
            
            int match = 0;
            int total = 0;
            for (String gram : candNgrams.keySet()) {
                total += candNgrams.get(gram);
                if (refNgrams.containsKey(gram)) {
                    match += Math.min(candNgrams.get(gram), refNgrams.get(gram));
                }
            }
            if (total == 0) break;
            score += Math.log((double) match / total);
        }
       
        score = Math.exp(score / maxN);
        
        int refLen = reference.size();
        int candLen = candidate.size();
        double brevityPenalty = (candLen > refLen) ? 1 : Math.exp(1 - (double) refLen / candLen);
        
        return brevityPenalty * score;
    }
	
	public void analysisbecalled() throws Exception{
		try {
			configuresolver();
			JavaSymbolSolver symbolSolver1 = new JavaSymbolSolver(typeSolver1);
			StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver1);
			
			CompilationUnit cu1 = StaticJavaParser.parse(Paths.get(this.oldclasspath));
	        Path projectFolder1 = Paths.get(this.oldprojectpath);            
	        cu1.findAll(MethodDeclaration.class).forEach(method -> {
		        if (iscontain(method.toString(),oldmethodcontent)) {
		        	try (Stream<Path> paths = Files.walk(projectFolder1)) {
		        		paths.filter(path -> path.toString().endsWith(".java"))
	                    	.forEach(filePath -> {
	                    		try {
	                    			CompilationUnit tempcu = StaticJavaParser.parse(filePath);
	                    			tempcu.findAll(MethodDeclaration.class).forEach(otherMethod -> {
	                    				otherMethod.findAll(MethodCallExpr.class).forEach(methodCall -> {
	                                        if (methodCall.getNameAsString().equals(method.getNameAsString()) && checkMethodSignature(methodCall, method) && otherMethod.toString().indexOf("@Test")==-1 && !iscontain(otherMethod.toString(),oldtestcontent)) {
	                                            usecase uc = new usecase();
	                                            uc.signature = getmethodsignaturefromcontent(otherMethod.toString());
	                                            uc.content = otherMethod.toString();
	                                            uc.path = filePath.toString().replace("\\", "/");
	                                            uc.contentwithoutcomment = otherMethod.removeComment().toString();
	                                            if (!uc.signature.isEmpty() && !uc.content.isEmpty() && !uc.path.isEmpty() && !usecases1.contains(uc)) {
	                                            	usecases1.add(uc);
	                                            }
	                                        }
	                    				});
	                                 });
	                             } catch (Exception e) {
	             	        		e.printStackTrace();
	                             }
	                    	});
		        	} catch (Exception e) {
		        		e.printStackTrace();
	                }
		        }
			});
			
	        JavaSymbolSolver symbolSolver2 = new JavaSymbolSolver(typeSolver2);
			StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver2);
	        CompilationUnit cu2 = StaticJavaParser.parse(Paths.get(this.newclasspath));
	        Path projectFolder2 = Paths.get(this.newprojectpath);            
	        cu2.findAll(MethodDeclaration.class).forEach(method -> {
		        if (iscontain(method.toString(),newmethodcontent)) {
		        	try (Stream<Path> paths = Files.walk(projectFolder2)) {
		        		paths.filter(path -> path.toString().endsWith(".java"))
	                    	.forEach(filePath -> {
	                    		try {
	                    			CompilationUnit tempcu = StaticJavaParser.parse(filePath);
	                    			tempcu.findAll(MethodDeclaration.class).forEach(otherMethod -> {
	                    				otherMethod.findAll(MethodCallExpr.class).forEach(methodCall -> {
	                                        if (methodCall.getNameAsString().equals(method.getNameAsString()) && checkMethodSignature(methodCall, method) && otherMethod.toString().indexOf("@Test")==-1) {
	                                            usecase uc = new usecase();
	                                            uc.signature = getmethodsignaturefromcontent(otherMethod.toString());
	                                            uc.content = otherMethod.toString();
	                                            uc.path = filePath.toString().replace("\\", "/");
	                                            uc.contentwithoutcomment = otherMethod.removeComment().toString();
	                                            if (!uc.signature.isEmpty() && !uc.content.isEmpty() && !uc.path.isEmpty() && !usecases2.contains(uc)) {
	                                            	usecases2.add(uc);
	                                            }
	                                        }
	                    				});
	                                 });
	                             } catch (Exception e) {
	             	        		e.printStackTrace();
	                             }
	                    	});
		        	} catch (Exception e) {
		        		e.printStackTrace();
	                }
		        }
			});
	        
	        for(usecase uc1: usecases1) {
		    	for(usecase uc2: usecases2) {
		    		if (uc1.path.replace(oldprojectpath, "").equals(uc2.path.replace(newprojectpath, "")) && uc1.signature.equals(uc2.signature) && !uc1.content.equals(uc2.content)) {
		    			String[] t1 = uc1.content.split("\n");
		    			List<String> original = Arrays.asList(t1);
		    			String[] t2 = uc2.content.split("\n");
		    			List<String> revised = Arrays.asList(t2);
		    			Patch<String> patch = DiffUtils.diff(original, revised);
		    			int contextSize = 10000;
		    	        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("old", "new", original, patch, contextSize);
		    			usecasesdiff.add(String.join("\n", unifiedDiff));
		    		}
		    	}
		    }
	        
	        Collections.sort(usecasesdiff, new Comparator<String>() {
	        	@Override
	        	public int compare(String s1, String s2) {
	     
	                List<String> refTokens1 = Arrays.asList(s1.split(" "));
	                List<String> refTokens2 = Arrays.asList(s2.split(" "));
	                List<String> candTokens = Arrays.asList(oldtestcontent.split(" "));
	                double bleu1 = calculateBLEU(refTokens1, candTokens, 4);
	                double bleu2 = calculateBLEU(refTokens2, candTokens, 4);
	                if(bleu1 < bleu2) {
	                	return 1;
	                }
	        		return -1;
	        	}
	        });
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
