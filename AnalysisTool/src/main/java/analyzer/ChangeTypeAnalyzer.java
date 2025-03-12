package analyzer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class ChangeTypeAnalyzer {
	public static class method{
		public String returnType;
		public String methodName;
		public List<Parameter> parameters;
		public List<ReferenceType> thrownExceptions;
		public String content;
		public String body;
	}
	
	public static class change{
		public String type;
		public String content;
	}
	
	public method method1 = new method();
	public method method2 = new method();
	
	public String oldclasspath = "";
	public String oldmethodsignature = "";
	public String oldmethodcontent = "";
	public String newclasspath = "";
	public String newmethodsignature = "";
	public String newmethodcontent = "";
	
	public CombinedTypeSolver typeSolver1 = new CombinedTypeSolver();
	public CombinedTypeSolver typeSolver2 = new CombinedTypeSolver();
	
	
	public ArrayList<change> changes = new ArrayList<>();
	
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
	
	public void analysischangetypes() throws Exception{
		configuresolver();
		JavaSymbolSolver symbolSolver1 = new JavaSymbolSolver(typeSolver1);
		StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver1);
		CompilationUnit cu1 = StaticJavaParser.parse(Paths.get(this.oldclasspath));
		cu1.findAll(MethodDeclaration.class).forEach(method -> {
			if(iscontain(method.toString(),oldmethodcontent)){
				method1.returnType = method.getType().asString();
				method1.methodName = method.getNameAsString();
				method1.parameters = method.getParameters();
				method1.thrownExceptions = method.getThrownExceptions();
				method1.content = method.toString();
				Optional<BlockStmt> body = method.getBody();
				if (body.isPresent()) {
					method1.body = body.get().toString();
	            } else {
	            	method1.body = "";
	            }
			}
		});
		JavaSymbolSolver symbolSolver2 = new JavaSymbolSolver(typeSolver2);
		StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver2);
		CompilationUnit cu2 = StaticJavaParser.parse(Paths.get(this.newclasspath));
		cu2.findAll(MethodDeclaration.class).forEach(method -> {
			if(iscontain(method.toString(),newmethodcontent)) {
				method2.returnType = method.getType().asString();
				method2.methodName = method.getNameAsString();
				method2.parameters = method.getParameters();
				method2.thrownExceptions = method.getThrownExceptions();
				method2.content = method.toString();
				Optional<BlockStmt> body = method.getBody();
				if (body.isPresent()) {
					method2.body = body.get().toString();
	            } else {
	            	method2.body = "";
	            }
			}
		});
		
		if(method1.content != null && method2.content != null) {
			if(!method1.returnType.equals(method2.returnType)) {
				change c =new change();
				c.type = "[ReturnTypeChanged]";
				c.content = "[ReturnTypeChanged]:The return type changed from " + method1.returnType + " to " + method2.returnType + ".";
				changes.add(c);
			}
			if(!method1.methodName.equals(method2.methodName)) {
				change c =new change();
				c.type = "[Renamed]";
				c.content = "[Renamed]:The name changed from " + method1.methodName + " to " + method2.methodName + ".";
				changes.add(c);
			}
			if(!method1.parameters.equals(method2.parameters)) {
				if(method1.parameters.size() != method2.parameters.size()) {
					change c =new change();
					c.type = "[ParameterListChanged]";
					c.content = "[ParameterListChanged]:The parameter list changed from " + method1.parameters + " to " + method2.parameters + ".";
					changes.add(c);
				}
				else {
					for (int i=0; i<method1.parameters.size(); i++) {
						if (!method1.parameters.get(i).getType().equals(method2.parameters.get(i).getType())) {
							change c =new change();
							c.type = "[ParameterListChanged]";
							c.content = "[ParameterListChanged]:The parameter list changed from " + method1.parameters + " to " + method2.parameters + ".";
							changes.add(c);
							break;
						}
					}
				}
			}
			if(!method1.thrownExceptions.equals(method2.thrownExceptions)) {
				if(method1.thrownExceptions.size() != method2.thrownExceptions.size()) {
					change c =new change();
					c.type = "[ThrownExceptionsChanged]";
					c.content = "[ThrownExceptionsChanged]:The thrown exceptions changed from " + method1.thrownExceptions + " to " + method2.thrownExceptions + ".";
					changes.add(c);
				}
				else {
					for (int i=0; i<method1.thrownExceptions.size(); i++) {
						if (!method1.thrownExceptions.get(i).asString().equals(method2.thrownExceptions.get(i).asString())) {
							change c =new change();
							c.type = "[ThrownExceptionsChanged]";
							c.content = "[ThrownExceptionsChanged]:The thrown exceptions changed from " + method1.thrownExceptions + " to " + method2.thrownExceptions + ".";
							changes.add(c);
							break;
						}
					}
				}
			}
		}
	}
}
