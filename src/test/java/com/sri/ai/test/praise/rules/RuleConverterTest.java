/*
 * Copyright (c) 2013, SRI International
 * All rights reserved.
 * Licensed under the The BSD 3-Clause License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://opensource.org/licenses/BSD-3-Clause
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the aic-praise nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sri.ai.test.praise.rules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sri.ai.brewer.core.Brewer;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.core.DefaultCompoundSyntaxTree;
import com.sri.ai.expresso.core.DefaultSymbol;
import com.sri.ai.praise.rules.antlr.RuleParserWrapper;
import com.sri.ai.praise.model.Model;
import com.sri.ai.praise.rules.RuleConverter;

public class RuleConverterTest {

	@SuppressWarnings("unused")
	private static int testCount = 0;

	private RuleParserWrapper parser;
	private RuleConverter     converter;
	
	
	public RuleConverterTest () {
		parser = new RuleParserWrapper();
		converter = new RuleConverter();
	}


	@Test
	public void testTranslateRules () {
		// Atomic rule tests
//		String string;
//		string = "sick(X);";
		testTranslateRules(new DefaultCompoundSyntaxTree("atomic rule", 
				new DefaultCompoundSyntaxTree("sick", "X"), "1"), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("sick", "X"), "1", "0"));

//		string = "sick(X) 0.3;";
		testTranslateRules(new DefaultCompoundSyntaxTree("atomic rule", 
				new DefaultCompoundSyntaxTree("sick", "X"), "0.3"),
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("sick", "X"), "0.3", "0.7"));

//		string = "sick(X) and happy(X);";
		testTranslateRules(new DefaultCompoundSyntaxTree("atomic rule", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("sick", "X"), 
						new DefaultCompoundSyntaxTree("happy", "X")), "1"), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("sick", "X"), 
						new DefaultCompoundSyntaxTree("happy", "X")), "1", "0"));

//		string = "sick(X) and happy(X) 0.1;";
		testTranslateRules(new DefaultCompoundSyntaxTree("atomic rule", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("sick", "X"), 
						new DefaultCompoundSyntaxTree("happy", "X")), "0.100000000"),
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("sick", "X"), 
						new DefaultCompoundSyntaxTree("happy", "X")), "0.100000000", "0.900000000"));

		// Prolog rule tests
//		string = "sick(john).";
		testTranslateRules(new DefaultCompoundSyntaxTree("prolog rule", "1", 
				new DefaultCompoundSyntaxTree("sick", "john")),
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("sick", "john"), "1", "0"));

//		string = "sick(X).";
		testTranslateRules(new DefaultCompoundSyntaxTree("prolog rule", "1", 
				new DefaultCompoundSyntaxTree("sick", "X")), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("sick", "X"), "1", "0"));

//		string = "not sick(mary).";
		testTranslateRules(new DefaultCompoundSyntaxTree("prolog rule", "1", 
				new DefaultCompoundSyntaxTree("not", 
						new DefaultCompoundSyntaxTree("sick", "mary"))), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("not", 
						new DefaultCompoundSyntaxTree("sick", "mary")), "1", "0"));

//		string = "0.3 sick(X).";
		testTranslateRules(new DefaultCompoundSyntaxTree("prolog rule", "0.300000000", 
				new DefaultCompoundSyntaxTree("sick", "X")), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("sick", "X"), "0.300000000", "0.700000000"));

//		string = "round(X) :- circle(X).";
		testTranslateRules(new DefaultCompoundSyntaxTree("prolog rule", "1", 
				new DefaultCompoundSyntaxTree("round", "X"), 
				new DefaultCompoundSyntaxTree("circle", "X")), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("circle", "X"), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
					new DefaultCompoundSyntaxTree("round", "X"), "1", "0"), "0.500000000"));

//		string = "0.7 sick(X) :- epidemic and not vaccinated(X).";
		testTranslateRules(new DefaultCompoundSyntaxTree("prolog rule", "0.700000000", 
				new DefaultCompoundSyntaxTree("sick", "X"), 
				new DefaultCompoundSyntaxTree("and", "epidemic", 
						new DefaultCompoundSyntaxTree("not", 
								new DefaultCompoundSyntaxTree("vaccinated", "X")))),
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("and", "epidemic", 
						new DefaultCompoundSyntaxTree("not", 
								new DefaultCompoundSyntaxTree("vaccinated", "X"))), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("sick", "X"), "0.700000000", "0.300000000"), "0.500000000"));

		// Conditional rule tests
//		string = "if circle(X) then round(X);";
		testTranslateRules(new DefaultCompoundSyntaxTree("conditional rule", 
				new DefaultCompoundSyntaxTree("circle", "X"), 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("round", "X"), "1")), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("circle", "X"), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("round", "X"), "1", "0"), "0.500000000"));

//		string = "if epidemic then sick(X) 0.7;";
		testTranslateRules(new DefaultCompoundSyntaxTree("conditional rule", "epidemic", 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("sick", "X"), "0.700000000")), 
			new DefaultCompoundSyntaxTree("if . then . else .", "epidemic", 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("sick", "X"), "0.700000000", "0.300000000"), "0.500000000"));

//		string = "if epidemic then sick(X) and unhappy(X) 0.9;";
		testTranslateRules(new DefaultCompoundSyntaxTree("conditional rule", "epidemic", 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("and", 
								new DefaultCompoundSyntaxTree("sick", "X"), 
								new DefaultCompoundSyntaxTree("unhappy", "X")), "0.900000000")), 
			new DefaultCompoundSyntaxTree("if . then . else .", "epidemic", 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("and", 
								new DefaultCompoundSyntaxTree("sick", "X"), 
								new DefaultCompoundSyntaxTree("unhappy", "X")), "0.900000000", "0.100000000"), "0.500000000"));

//		string = "if chilly(P) and live(X, P) then sick(X) 0.6;";
		testTranslateRules(new DefaultCompoundSyntaxTree("conditional rule", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("chilly", "P"), 
						new DefaultCompoundSyntaxTree("live", "X", "P")), 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("sick", "X"), "0.600000000")), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("chilly", "P"), 
						new DefaultCompoundSyntaxTree("live", "X", "P")), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("sick", "X"), "0.600000000", "0.400000000"), "0.500000000"));

//		string = "if colleagues(X,Y) then likes(X,Y) 0.8;";
		testTranslateRules(new DefaultCompoundSyntaxTree("conditional rule", 
				new DefaultCompoundSyntaxTree("colleagues", "X", "Y"), 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("likes", "X", "Y"), "0.800000000")), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("colleagues", "X", "Y"), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("likes", "X", "Y"), "0.800000000", "0.200000000"), "0.500000000"));

//		string = "if epidemic then if sick(X) and friends(X,Y) then sick(Y) 0.8;";
		testTranslateRules(new DefaultCompoundSyntaxTree("conditional rule", "epidemic", 
				new DefaultCompoundSyntaxTree("conditional rule", 
						new DefaultCompoundSyntaxTree("and", 
								new DefaultCompoundSyntaxTree("sick", "X"), 
								new DefaultCompoundSyntaxTree("friends", "X", "Y")), 
						new DefaultCompoundSyntaxTree("atomic rule", 
								new DefaultCompoundSyntaxTree("sick", "Y"), "0.800000000"))), 
			new DefaultCompoundSyntaxTree("if . then . else .", "epidemic", 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("and", 
								new DefaultCompoundSyntaxTree("sick", "X"), 
								new DefaultCompoundSyntaxTree("friends", "X", "Y")), 
						new DefaultCompoundSyntaxTree("if . then . else .", 
								new DefaultCompoundSyntaxTree("sick", "Y"), "0.800000000", "0.200000000"), "0.500000000"), "0.500000000"));

//		string = "if sick(X) and friends(X,Y) then sick(Y) 0.8 else sick(Y);";
		testTranslateRules(new DefaultCompoundSyntaxTree("conditional rule", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("sick", "X"), 
						new DefaultCompoundSyntaxTree("friends", "X", "Y")), 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("sick", "Y"), "0.800000000"), 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("sick", "Y"), 1)), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("sick", "X"), 
						new DefaultCompoundSyntaxTree("friends", "X", "Y")), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("sick", "Y"), "0.800000000", "0.200000000"), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("sick", "Y"), "1", "0")));

//		string = "if sick(X) and friends(X,Y) then sick(Y) 0.8 else sick(Y) 0.3;";
		testTranslateRules(new DefaultCompoundSyntaxTree("conditional rule", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("sick", "X"), 
						new DefaultCompoundSyntaxTree("friends", "X", "Y")), 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("sick", "Y"), "0.800000000"), 
				new DefaultCompoundSyntaxTree("atomic rule", 
						new DefaultCompoundSyntaxTree("sick", "Y"), "0.300000000")), 
			new DefaultCompoundSyntaxTree("if . then . else .", 
				new DefaultCompoundSyntaxTree("and", 
						new DefaultCompoundSyntaxTree("sick", "X"), 
						new DefaultCompoundSyntaxTree("friends", "X", "Y")), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("sick", "Y"), "0.800000000", "0.200000000"), 
				new DefaultCompoundSyntaxTree("if . then . else .", 
						new DefaultCompoundSyntaxTree("sick", "Y"), "0.300000000", "0.700000000")));

	}

	@Test
	public void testUpdateRandomVariableDeclaration () {
		Expression result, input;
		input = new DefaultCompoundSyntaxTree("random variable", "mother", 1, "People", "People");
		result = converter.updateRandomVariableDeclaration(input);
		System.out.println("Updating: " + input);
		System.out.println("To:       " + result);

		input = new DefaultCompoundSyntaxTree("random variable", "president", 0, "People");
		result = converter.updateRandomVariableDeclaration(input);
		System.out.println("Updating: " + input);
		System.out.println("To:       " + result);

		input = new DefaultCompoundSyntaxTree("sort", "sprinters", "bolt", "johnson");
		result = converter.updateRandomVariableDeclaration(input);
		System.out.println("Updating: " + input);
		System.out.println("To:       " + result);
	}

	@Test
	public void testIsRandomFunctionApplication () {
		Expression input;
		input = new DefaultCompoundSyntaxTree ("if . then . else .", 1, 2, 3);
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = DefaultSymbol.createSymbol("foo");
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("and", 1, 2, 3);
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("or", 1, 2, 3);
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("not", 1, 2, 3);
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("<=>", 1, 2, 3);
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("=>", 1, 2, 3);
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("there exists . : .", 1, 2, 3);
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("for all . : .", 1, 2, 3);
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("may be same as", "A", "B");
		Assert.assertEquals(false, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree("mother", 1, 2, 3, 4);
		Assert.assertEquals(true, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree("+", 1, 2, 3, 4);
		Assert.assertEquals(true, converter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree("=", 1, 2, 3, 4);
		Assert.assertEquals(true, converter.isRandomFunctionApplication(input));

	}

	@Test
	public void testCreateTransformedFunctionConstraints () {
		List<Expression> parfactors = new ArrayList<Expression>();
		converter.createTransformedFunctionConstraints("president", 1, parfactors);
		System.out.println("created constraints: " + parfactors.toString());

		parfactors = new ArrayList<Expression>();
		converter.createTransformedFunctionConstraints("mother", 2, parfactors);
		System.out.println("created constraints: " + parfactors.toString());

		parfactors = new ArrayList<Expression>();
		converter.createTransformedFunctionConstraints("foo", 4, parfactors);
		System.out.println("created constraints: " + parfactors.toString());
	}

	@Test
	public void testParse () {
		String string;
		string = "if circle(X) then round(X); sick(X); sort Dogs: 1000, rover; random +: Number x Number -> Number;";
		testParseModel(string);
		
	}
	


	/*===================================================================================
	 * PROTECTED METHODS
	 *=================================================================================*/

	protected void testTranslateRules (String input) {
		testTranslateRules(input, true);
	}
	
	protected void testTranslateRules (String input, Expression expectedResult) {
		testTranslateRules(input, null, true, true, expectedResult);
	}
	
	protected void testTranslateRulesFail (String input) {
		testTranslateRules(input, false);
	}
	
	protected void testTranslateRules (String input, boolean expectSucceed) {
		testTranslateRules(input, null, expectSucceed, false, null);
	}

	protected void testTranslateRules (Expression input) {
		testTranslateRules(input, true);
	}
	
	protected void testTranslateRules(Expression input, Expression expectedResult) {
		testTranslateRules(null, input, true, true, expectedResult);
	}
	
	protected void testTranslateRulesFail (Expression input) {
		testTranslateRules(input, false);
	}
	
	protected void testTranslateRules (Expression input, boolean expectSucceed) {
		testTranslateRules(null, input, expectSucceed, false, null);
	}

	protected void testTranslateRules (String inputString, Expression inputExpr, boolean expectSucceed, boolean checkResult, Expression expectedResult) {
		testCount ++;
		Expression result;
		if (inputExpr == null)
			result = converter.translateRule(parser.parse(inputString));
		else
			result = converter.translateRule(inputExpr);
		if (expectSucceed) {
			if (checkResult) {
				assertEquals(expectedResult.toString(), result.toString());
			}
			else {
				if(result != null) {
					if (inputString != null)
						System.out.println("generated string for \"" + inputString + "\": " + Brewer.generateBuildString(result) + "\n\n");
					else
						System.out.println("generated string : " + Brewer.generateBuildString(result) + "\n\n");
				}
				Assert.assertNotNull(result);
			}
		}
		else {
			Assert.assertNull(result);
		}
	}



	protected void testParseModel (String input) {
		testParseModel(input, true);
	}
	
	protected void testParseModel (String input, Expression expectedResult) {
		testParseModel(input, null, true, true, expectedResult);
	}
	
	protected void testParseModel (String input, boolean expectSucceed) {
		testParseModel(input, null, expectSucceed, false, null);
	}

	protected void testParseModel (List<Expression> input) {
		testParseModel(input, true);
	}
	
	protected void testParseModel(List<Expression> input, Expression expectedResult) {
		testParseModel(null, input, true, true, expectedResult);
	}
	
	protected void testParseModelFail (List<Expression> input) {
		testParseModel(input, false);
	}
	
	protected void testParseModel (List<Expression> input, boolean expectSucceed) {
		testParseModel(null, input, expectSucceed, false, null);
	}

	protected void testParseModel (String inputString, List<Expression> inputExpr, boolean expectSucceed, boolean checkResult, Expression expectedResult) {
		testCount ++;
		Model result;
		if (inputExpr == null) {
			result = converter.parseModel(inputString);
		} 
		else {
			result = converter.parseModel(inputExpr);
		}
		if (expectSucceed) {
			if (checkResult) {
				assertEquals(expectedResult.toString(), result.toString());
			}
			else {
				if(result != null) {
//					if (inputString != null)
//						System.out.println("generated string for \"" + inputString + "\": " + generateBuildString(result) + "\n\n");
//					else
//						System.out.println("generated string : " + generateBuildString(result) + "\n\n");
				}
//				Assert.assertNotNull(result);
			}
		}
		else {
			Assert.assertNull(result);
		}
	}

	/**
	 * Call this to generate the Java code to produce the given expression object.
	 * @param expr  The expression object.
	 * @return      A string of Java code for generating the given object.
	 */
	protected String generateBuildString (List<Expression> exprs) {
		StringBuffer sb = new StringBuffer();

		if (exprs == null) {
			return "";
		}

		for(Expression expr : exprs) {
			Brewer.generateFunctionApplicationString(sb, expr, 3, true);
		}
		
		return sb.toString();
	}

}
