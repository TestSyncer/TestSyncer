package analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Paths;

public class MethodCallAnalyzer {
	public static class Calledmethod{
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
	        if(!(obj instanceof Calledmethod)){
	            return false;
	        }
	        Calledmethod other = (Calledmethod)obj;
	        return this.signature.equals(other.signature) && this.content.equals(other.content) && this.path.equals(other.path);
	    }
	}
	
	public static class Calledenum{
		public String content;
		@Override
	    public boolean equals(Object obj) {
	        if(this == obj){
	            return true;
	        }
	        if(obj == null){
	            return false;
	        }
	        if(!(obj instanceof Calledenum)){
	            return false;
	        }
	        Calledenum other = (Calledenum)obj;
	        return this.content.equals(other.content);
	    }
	}
	
	public ArrayList<Calledmethod> calledmethods = new ArrayList<>();
	public ArrayList<Calledenum> calledenums = new ArrayList<>();
	
	public String targetclasspath = "";
	public String targetmethodsignature = "";
	public String targetmethodcontent = "";
	
	public CombinedTypeSolver typeSolver = new CombinedTypeSolver();
	
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
	
	public void setclasspath(String str) {
		this.targetclasspath = str.replace("\\", "/");
	}
	
	public void setmethodsignature(String str) {
		this.targetmethodsignature = getmethodsignaturefromcontent(str);
	}
	
	public void setmethodcontent(String str) { 
		this.targetmethodcontent = str;
		setmethodsignature(getmethodsignaturefromcontent(str));
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
		String[] ps = targetclasspath.split("[\\\\/]");
		String base = ps[0];
        for (int i = 1; i < ps.length - 1; i++) {
        	base = base + "/" + ps[i];
        	ps[i] = base;
        }
        for (int i = 1; i < ps.length - 1; i++) {
        	typeSolver.add(new JavaParserTypeSolver(Paths.get(ps[i].toString())));
        }
        typeSolver.add(new ReflectionTypeSolver());
	}
	
    private static boolean isEnumType(CompilationUnit cu, String typeName) {
        return cu.findAll(EnumDeclaration.class).stream()
                .anyMatch(enumDecl -> enumDecl.getNameAsString().equals(typeName));
    }

    private static String getEnumDefinition(CompilationUnit cu, String enumName) {
    	StringBuilder enumDefinition = new StringBuilder();
    	cu.findAll(EnumDeclaration.class).stream()
                .filter(enumDecl -> enumDecl.getNameAsString().equals(enumName))
                .forEach(enumDecl -> {
                    enumDefinition.append(enumDecl.toString());
                });
        return enumDefinition.toString();
    }
    
	public void analysiscalledmethods() throws Exception{
		configuresolver();
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
		CompilationUnit cu = StaticJavaParser.parse(Paths.get(this.targetclasspath));
		cu.findAll(MethodDeclaration.class).forEach(method -> {
			if(iscontain(method.toString(),targetmethodcontent)) {
				method.findAll(MethodCallExpr.class).forEach(call -> {
					try {
						Calledmethod newCalledmethod = new Calledmethod();
						newCalledmethod.signature = "";
						newCalledmethod.content = "";
						newCalledmethod.path = "";
						ResolvedMethodDeclaration resolvedMethod = call.resolve();
						ResolvedReferenceTypeDeclaration declaringType = resolvedMethod.declaringType();
						
		                if (resolvedMethod instanceof JavaParserMethodDeclaration) {
		                    MethodDeclaration methodDeclaration = ((JavaParserMethodDeclaration) resolvedMethod).getWrappedNode();
		                	newCalledmethod.signature = getmethodsignaturefromcontent(methodDeclaration.toString());
		                	newCalledmethod.content = methodDeclaration.toString();
		                	newCalledmethod.contentwithoutcomment = methodDeclaration.removeComment().toString();
		                }
		                
		                if (declaringType instanceof JavaParserClassDeclaration) {
		                	JavaParserClassDeclaration javaParserTypeDeclaration = (JavaParserClassDeclaration) declaringType;
		                    CompilationUnit declaringCu = javaParserTypeDeclaration.getWrappedNode().findCompilationUnit().orElse(null);
		                    if (declaringCu != null) {
		                        String filePath = declaringCu.getStorage().orElseThrow(null).getPath().toString();
		                        newCalledmethod.path = filePath.replace("\\", "/");
		                    }
		                } else if (declaringType instanceof JavaParserInterfaceDeclaration) {
		                	JavaParserInterfaceDeclaration javaParserTypeDeclaration = (JavaParserInterfaceDeclaration) declaringType;
		                    CompilationUnit declaringCu = javaParserTypeDeclaration.getWrappedNode().findCompilationUnit().orElse(null);
		                    if (declaringCu != null) {
		                        String filePath = declaringCu.getStorage().orElseThrow(null).getPath().toString();
		                        newCalledmethod.path = filePath.replace("\\", "/");
		                    }
		                }
		                if (!newCalledmethod.content.equals("") && !newCalledmethod.signature.equals("") && !newCalledmethod.path.equals("")) {
		                	if (!calledmethods.contains(newCalledmethod)){
		                		calledmethods.add(newCalledmethod);
		                	}	
		                }
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				method.getParameters().forEach(parameter -> {
	                Type paramType = parameter.getType();
	                if (paramType instanceof ClassOrInterfaceType) {
	                    ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) paramType;
	                    String typeName = classOrInterfaceType.getNameAsString();
	                    if (isEnumType(cu, typeName)) {
	                    	Calledenum ce = new Calledenum();
	                    	ce.content = getEnumDefinition(cu, typeName);
	                    	if (!calledenums.contains(ce) && !ce.content.equals("")){
	                    		calledenums.add(ce);
	                    	}
	                    }
	                }
	            });
				
	            method.getBody().ifPresent(body -> {
	                List<Expression> expressions = body.findAll(Expression.class);
	                for (Expression expr : expressions) {
	                    if (expr instanceof NameExpr) {
	                        NameExpr nameExpr = (NameExpr) expr;
	                        String typeName = nameExpr.getNameAsString();
	                        if (isEnumType(cu, typeName)) {
	                        	Calledenum ce = new Calledenum();
		                    	ce.content = getEnumDefinition(cu, typeName);
		                    	if (!calledenums.contains(ce)){
		                    		calledenums.add(ce);
		                    	}
	                        }
	                    }
	                }
	            });
			}
		});
	}
}
