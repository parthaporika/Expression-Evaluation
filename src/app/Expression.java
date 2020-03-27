package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	
    	expr = expr.replaceAll("\\s", "");
		String newdelim = delims + "1234567890";
		for (int i = 0; i < expr.length(); i++) {
			if (newdelim.indexOf(expr.charAt(i)) < 0) { 
				String name = "";
				int index = 0;
				for (int j = i; j < expr.length(); j++) { 
					if (newdelim.indexOf(expr.charAt(j)) >= 0) {
						i += name.length();
						if (expr.charAt(j) == '[') 
						index = j; 
						break;
					}
					else {
						name += expr.charAt(j); 
					}
				}
				if (index != 0) { 
					Array arr = new Array(name);
					if (arrays.indexOf(arr) == -1) {
						arrays.add(arr);
					}
					
				} else {
					Variable var = new Variable(name);
					if (vars.indexOf(var) == -1) {
						vars.add(var);
					}
				}
			}

		}
	}

    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	// following line just a placeholder for compilation
    	float ans = 0;
    	Stack<Integer> values = new Stack<Integer>();
    	Stack<Character> operators = new Stack<Character>();
    	Stack<Integer> beg_parens = new Stack<Integer>();
    	boolean inParens = false;
    	String varname = "";
    	//expr = expr.replaceAll("\\s", "");
    	char[] tokens = expr.toCharArray();
    	int beg_var = 0;
    	
    	
    	for (int i = 0; i < tokens.length; i++) {
    		
    		if (inParens && !(tokens[i] == '(' ) && !(tokens[i] == ')') && !(tokens[i] == '[') && !(tokens[i] == ']')) {
    			//System.out.println("In the paren");
    			//System.out.println(i);
    			continue;
    		}
    		
    		if (tokens[i] == ' ') {
    			continue;
    		}
    		
    		if (delims.indexOf(tokens[i]) < 0) {
    			beg_var = i;
    			while (i < tokens.length && delims.indexOf(tokens[i]) < 0) {
    				i++;
    			}
    			varname = expr.substring(beg_var, i);
    			varname = varname.trim();
    			//System.out.println(varname);
    			if (!varname.equals("")) {
    				if (isNumber(varname)) {
    					values.push(Integer.parseInt(varname));
    				}else {
    					Variable var = new Variable(varname);
    					int index = vars.indexOf(var);
    					if (index > -1) {
    						values.push(vars.get(index).value);
    					}
    					
    				}
    			}//end of the variable name
    			
    		}//end of delims block
    		
    			if (i == tokens.length) {
    				break;
    			}
    			
    			
    			if (tokens[i] == '(' || tokens[i] == '[') {
    
    				//System.out.println("Reached (");
    				beg_parens.push(i);
    				inParens = true;
    				
    			}
    			
    			if (tokens[i] == ')') {
    				int start = beg_parens.pop()+1;
    				if (beg_parens.isEmpty()) {
    					inParens = false;
    					String parenFunc = expr.substring(start, i);
    					//System.out.println(parenFunc);
    					int temp = (int)evaluate(parenFunc, vars, arrays);
    					//System.out.println(temp);
    					values.push(temp);
    					varname = "";
    				}
    			}// end of )
    			
    			if (tokens[i] == ']') {
    				int start = beg_parens.pop()+1;
    				if (beg_parens.isEmpty()) {
    					inParens = false;
    					String parenFunc = expr.substring(start, i);
    					int temp = (int) evaluate(parenFunc, vars, arrays);
    					Array arr = new Array(varname);
    					int arrayIndex = arrays.indexOf(arr);
    					int value = arrays.get(arrayIndex).values[temp];
    					values.push(value);
    					varname = "";
    				}
    			}
    			
    			if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/') {
    				//System.out.println("Reached in the function block");
    				while(!operators.isEmpty() && orderOper(tokens[i], operators.peek())) {
    					values.push(executeFunc(operators.pop(), values.pop(), values.pop()));
    				}
    				operators.push(tokens[i]);
    			}
    			
    	}//end of for-loop
    	while(!operators.isEmpty()) {
    		values.push(executeFunc(operators.pop(), values.pop(), values.pop()));
    	}
    	return (float)values.pop();
    }
    
    private void setVar(String name, int value) {
    	
    }
    
    private static int executeFunc(char operator, int y, int x) {
    	
    	switch (operator) {
    	
    		case '+': return x+y;
    		case '-': return x-y;
    		case '*': return x*y;
    		case '/': return x/y;
    		default: return 0;
    		
    	}
    }
    
    private static boolean orderOper(char oper1, char oper2) {
    	
    	if ((oper1 =='*' || oper1 =='/') && (oper2 == '+' || oper2 == '-')) {
    		return false;
    	}else{
    		return true;
    	}
    }
    
    private static boolean isNumber(String num) {
    	try {
    		int d = Integer.parseInt(num);
    	}catch (NumberFormatException ex) {
    		return false;
    	}
    	return true;
    }
    
    public static void main(String[] args) {
		String str = "(A[2] + A[3] - d)*a + (A[2]+A[4]+b*c)/A[3]";
		ArrayList<Variable> vars = new ArrayList<>();
		ArrayList<Array> arrays = new ArrayList<>();
		Expression.makeVariableLists(str, vars, arrays);
		
		
		Variable a = new Variable("a");
		int index = vars.indexOf(a);
		vars.get(index).value = 8;
		
		Variable b = new Variable("b");
		index = vars.indexOf(b);
		vars.get(index).value = 4;
		
		Variable c = new Variable("c");
		index = vars.indexOf(c);
		vars.get(index).value = 9;
		
		Variable d = new Variable("d");
		index = vars.indexOf(d);
		vars.get(index).value = 3;
		
		
		Array varA = new Array("A");
		int indexA = arrays.indexOf(varA);
		int[] temp = {0, 1, 2, 3, 4, 5};
		arrays.get(indexA).values = temp;

		
		/*
		for (int i = 0; i < arrays.size(); i ++) {
			System.out.println("Arrays " + i + " :" + arrays.get(i).name);
		}
		for (int i = 0; i < vars.size(); i ++) {
			System.out.println("Variable " + i + " :" + vars.get(i).name);
		}
		*/
		System.out.println(evaluate(str, vars, arrays));
		
	}
}
