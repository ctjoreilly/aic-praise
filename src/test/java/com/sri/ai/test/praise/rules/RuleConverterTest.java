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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sri.ai.brewer.BrewerConfiguration;
import com.sri.ai.brewer.api.Grammar;
import com.sri.ai.brewer.core.Brewer;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.core.DefaultCompoundSyntaxTree;
import com.sri.ai.expresso.core.DefaultSymbol;
import com.sri.ai.expresso.helper.Expressions;
import com.sri.ai.grinder.GrinderConfiguration;
import com.sri.ai.grinder.api.RewritingProcess;
import com.sri.ai.grinder.parser.antlr.AntlrGrinderParserWrapper;
import com.sri.ai.grinder.ui.TreeUtil;
import com.sri.ai.praise.LPIGrammar;
import com.sri.ai.praise.result.ResultConverter;
import com.sri.ai.praise.rules.antlr.RuleParserWrapper;
import com.sri.ai.praise.model.Model;
import com.sri.ai.praise.model.Model.ModelException;
import com.sri.ai.praise.model.example.TrivialEpidemicSickbob;
import com.sri.ai.praise.rules.ReservedWordException;
import com.sri.ai.praise.rules.RuleConverter;
import com.sri.ai.util.Util;
import com.sri.ai.util.base.Pair;

public class RuleConverterTest {

	@SuppressWarnings("unused")
	private static int testCount = 0;

	private RuleParserWrapper         ruleParser;
	private RuleConverter             ruleConverter;
	private AntlrGrinderParserWrapper lowParser;

	@Before
	public void setUp () {
		Grammar grammar = new LPIGrammar();
		// Ensure the grammar class passed in is used where necessary.
		BrewerConfiguration.setProperty(BrewerConfiguration.KEY_DEFAULT_GRAMMAR_CLASS, grammar.getClass().getName());
		
		ruleParser = new RuleParserWrapper();
		ruleConverter = new RuleConverter();
		
		lowParser = new AntlrGrinderParserWrapper();
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
		input = new DefaultCompoundSyntaxTree("randomVariable", "mother", 1, "People", "People");
		result = ruleConverter.updateRandomVariableDeclaration(input);
		assertEquals(lowParser.parse("randomVariable(mother, 2, People, People, Boolean)"), result);

		input = new DefaultCompoundSyntaxTree("randomVariable", "president", 0, "People");
		result = ruleConverter.updateRandomVariableDeclaration(input);
		assertEquals(lowParser.parse("randomVariable(president, 1, People, Boolean)"), result);

		input = new DefaultCompoundSyntaxTree("sort", "sprinters", "bolt", "johnson");
		result = ruleConverter.updateRandomVariableDeclaration(input);
		assertEquals(null, result);
	}

	@Test
	public void testIsRandomFunctionApplication () {
		Expression input;
		input = new DefaultCompoundSyntaxTree ("if . then . else .", 1, 2, 3);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = DefaultSymbol.createSymbol("foo");
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("and", 1, 2, 3);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("or", 1, 2, 3);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("not", 1, 2, 3);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("<=>", 1, 2, 3);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("=>", 1, 2, 3);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("there exists . : .", 1, 2, 3);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("for all . : .", 1, 2, 3);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree ("may be same as", "A", "B");
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree("mother", 1, 2, 3, 4);
		Assert.assertEquals(true, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree("+", 1, 2, 3, 4);
		Assert.assertEquals(true, ruleConverter.isRandomFunctionApplication(input));

		input = new DefaultCompoundSyntaxTree("=", 1, 2, 3, 4);
		Assert.assertEquals(false, ruleConverter.isRandomFunctionApplication(input));

	}

	@Test
	public void testCreateTransformedFunctionConstraints () {
		List<Expression> expected;

		List<Expression> parfactors = new ArrayList<Expression>();
		ruleConverter.createTransformedFunctionConstraints("president", 1, parfactors);
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if president(Y) then if not president(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : president(Y) then 1 else 0"));
		assertEquals(expected, parfactors);

		parfactors = new ArrayList<Expression>();
		ruleConverter.createTransformedFunctionConstraints("mother", 2, parfactors);
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(X0, Y) then if not mother(X0, Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(X0, Y) then 1 else 0"));
		assertEquals(expected, parfactors);

		parfactors = new ArrayList<Expression>();
		ruleConverter.createTransformedFunctionConstraints("foo", 4, parfactors);
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if foo(X0, X1, X2, Y) then if not foo(X0, X1, X2, Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : foo(X0, X1, X2, Y) then 1 else 0"));
		assertEquals(expected, parfactors);
	}

	@Test
	public void testTranslateFunctions ()
	{
		List<Expression> potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("mother(john) = mother(bob);")));
		List<Expression> expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(john, X0) and mother(bob, X1) and X0 = X1 then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("mother(john) = mother(bob) = jane;")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(john, X0) and mother(bob, X1) and X0 = X1 = jane then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));
		
		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("mother(john) = jane = mother(bob);")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(john, X0) and mother(bob, X1) and X0 = jane = X1 then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));
		
		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("jane = mother(john) = mother(bob);")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(john, X0) and mother(bob, X1) and jane = X0 = X1 then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));




		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("sick(mother(X));")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(X, X0) and sick(X0) then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("sick(mother(X), bob);")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(X, X0) and sick(X0, bob) then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("sick(bob, mother(X));")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(X, X0) and sick(bob, X0) then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("sick(mother(X), father(X));")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if mother(X, X0) and father(X, X1) and sick(X0, X1) then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		expected.add(lowParser.parse("if father(Y) then if not father(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : father(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("sick(mother(father(X)));")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if 'and'(father(X, X1), mother(X1, X0)) and sick(X0) then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		expected.add(lowParser.parse("if father(Y) then if not father(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : father(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("mother(john) = bestfriend(mother(bob));")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if and(mother(john, X0), and(mother(bob, X2), bestfriend(X2, X1)), (X0 = X1)) then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		expected.add(lowParser.parse("if bestfriend(Y) then if not bestfriend(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : bestfriend(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, null));

		// Test of zero arg function.
		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(ruleParser.parse("if president = obama then government(socialism) else government(freedom);")));
		Set<Integer> count = new HashSet<Integer>();
		count.add(0);
		Map<String, Set<Integer>> randomVariableIndex = new HashMap<String, Set<Integer>>();
		randomVariableIndex.put("president", count);
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if president(X0) and X0 = obama then if government(socialism) then 1 else 0 else (if government(freedom) then 1 else 0)"));
		expected.add(lowParser.parse("if president(Y) then if not president(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : president(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, randomVariableIndex));

		// Test multiple tiered functions.
		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("sick(bob, mother(bestfriend(X)));")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if and(and(bestfriend(X, X1), mother(X1, X0)), sick(bob, X0)) then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		expected.add(lowParser.parse("if bestfriend(Y) then if not bestfriend(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : bestfriend(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, randomVariableIndex));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse("sick(bob, mother(bestfriend(father(X))));")));
		expected = new ArrayList<Expression>();
		expected.add(lowParser.parse("if and(and(and(father(X, X2), bestfriend(X2, X1)), mother(X1, X0)), sick(bob, X0)) then 1 else 0"));
		expected.add(lowParser.parse("if mother(Y) then if not mother(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : mother(Y) then 1 else 0"));
		expected.add(lowParser.parse("if bestfriend(Y) then if not bestfriend(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : bestfriend(Y) then 1 else 0"));
		expected.add(lowParser.parse("if father(Y) then if not father(Z) then 1 else 0 else 0.500000000"));
		expected.add(lowParser.parse("if there exists Y : father(Y) then 1 else 0"));
		assertEquals(expected, ruleConverter.translateFunctions(potentialExpressions, randomVariableIndex));

	}

	@Test
	public void testTranslateQuantifiers () {
		List<Expression> potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse(
				"if young(X) and (for all Y : (friends(Y,X) => smokes(Y))) then smokes(X) 0.8;")));
		List<Expression> expected = new ArrayList<Expression>();
		expected.add(ruleConverter.translateConditionalRule(ruleParser.parse("if young(X) and 'for all Y : friends(Y, X) => smokes(Y)'(X) then smokes(X) 0.8;")));
		expected.add(ruleConverter.translateConditionalRule(ruleParser.parse("if not (friends(Y, X) => smokes(Y)) then not 'for all Y : friends(Y, X) => smokes(Y)'(X);")));
		assertEquals(expected, ruleConverter.translateQuantifiers(potentialExpressions));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse(
				"there exists X : president(X, Country);")));
		expected = new ArrayList<Expression>();
		expected.add(ruleConverter.translateRule(ruleParser.parse("'there exists X : president(X, Country)'(Country);")));
		expected.add(ruleConverter.translateRule(ruleParser.parse("if president(X, Country) then 'there exists X : president(X, Country)'(Country);")));
		assertEquals(expected, ruleConverter.translateQuantifiers(potentialExpressions));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse(
				"friends(X,Y) and (there exists Z : friends(X,Z));")));
		expected = new ArrayList<Expression>();
		expected.add(ruleConverter.translateRule(ruleParser.parse("friends(X,Y) and 'there exists Z : friends(X, Z)'(X);")));
		expected.add(ruleConverter.translateRule(ruleParser.parse("if friends(X,Z) then 'there exists Z : friends(X, Z)'(X);")));
		assertEquals(expected, ruleConverter.translateQuantifiers(potentialExpressions));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse(
				"friends(X,Y) and (there exists Z : Z may be same as X and loves(X,Z));")));
		expected = new ArrayList<Expression>();
		expected.add(ruleConverter.translateRule(ruleParser.parse("friends(X,Y) and 'there exists Z : \\\'may be same as\\\'(Z, X) and loves(X, Z)'(X);")));
		expected.add(ruleConverter.translateRule(ruleParser.parse("if Z may be same as X and loves(X,Z) then 'there exists Z : \\\'may be same as\\\'(Z, X) and loves(X, Z)'(X);")));
		assertEquals(expected, ruleConverter.translateQuantifiers(potentialExpressions));

		// Test nested quantifiers.
		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(this.ruleConverter.translateRule(this.ruleParser.parse(
				"there exists Z : for all Y : loves(X, Y, Z);")));
		expected = new ArrayList<Expression>();
		expected.add(ruleConverter.translateRule(ruleParser.parse("'there exists Z : for all Y : loves(X, Y, Z)'(X);")));
		expected.add(ruleConverter.translateRule(ruleParser.parse("if 'for all Y : loves(X, Y, Z)'(X, Z) then 'there exists Z : for all Y : loves(X, Y, Z)'(X);")));
		expected.add(ruleConverter.translateRule(ruleParser.parse("if not loves(X, Y, Z) then not 'for all Y : loves(X, Y, Z)'(X, Z);")));
		assertEquals(expected, ruleConverter.translateQuantifiers(potentialExpressions));

		doTreeUtilWaitUnilClosed(); 
	}
	
	@Test
	public void testDisembedConstraints () {
		List<Expression> potentialExpressions;
		List<Pair<Expression, Expression>> expected;

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(lowParser.parse(
				"if friends(X,Y) and likes(X,Z) and 'may be same as'(X, Z) then if likes(Y,Z) then 0.8 else 0.2 else 0.5"));
		expected = new ArrayList<Pair<Expression, Expression>>();
		expected.add(new Pair<Expression, Expression>(
				lowParser.parse("if friends(X, Y) and likes(X, Z) then if likes(Y, Z) then 0.800000000 else 0.200000000 else 0.500000000"),
				lowParser.parse("Y != Z and Y != X")));
		assertEquals(expected, ruleConverter.disembedConstraints(potentialExpressions));

		potentialExpressions = new ArrayList<Expression>();
		potentialExpressions.add(lowParser.parse(
				"if colleagues(X,Y) and Y != bob then (if likes(X,Y) then 0.8 else 0.2) else 0.5"));
		System.out.println(potentialExpressions);
		expected = new ArrayList<Pair<Expression, Expression>>();
		expected.add(new Pair<Expression, Expression>(
				lowParser.parse("if colleagues(X, Y) then if likes(X, Y) then 0.800000000 else 0.200000000 else 0.500000000"),
				lowParser.parse("Y != X and Y != bob")));
		assertEquals(expected, ruleConverter.disembedConstraints(potentialExpressions));

	}

	@Test
	public void testQueryRuleAndAtom () {
		String string;
		Pair<Expression, Expression> result, expected;
		
		string = "sick(X)";
		result = ruleConverter.queryRuleAndAtom(ruleParser.parseFormula(string), null);
		expected = null;
		assertEquals(expected, result);
		
		string = "sick(john) and sick(mary)";
		result = ruleConverter.queryRuleAndAtom(ruleParser.parseFormula(string), null);
		expected = new Pair<Expression, Expression>(ruleParser.parseFormula("query"), 
				ruleParser.parse("query <=> sick(john) and sick(mary);"));
		assertEquals(expected, result);

		string = "not sick(X)";
		result = ruleConverter.queryRuleAndAtom(ruleParser.parseFormula(string), null);
		expected = new Pair<Expression, Expression>(ruleParser.parseFormula("query(X)"), 
				ruleParser.parse("query(X) <=> not sick(X);"));
		assertEquals(expected, result);

		string = "there exists X : friends(X,Y)";
		result = ruleConverter.queryRuleAndAtom(ruleParser.parseFormula(string), null);
		expected = new Pair<Expression, Expression>(ruleParser.parseFormula("query(Y)"), 
				ruleParser.parse("query(Y) <=> there exists X : friends(X,Y);"));
		assertEquals(expected, result);

		string = "conspiracy(C) and leader(C) = X and member(C,Y) and member(C,Z)";
		result = ruleConverter.queryRuleAndAtom(ruleParser.parseFormula(string), null);
		expected = new Pair<Expression, Expression>(ruleParser.parseFormula("query(C, Y, X, Z)"), 
				ruleParser.parse("query(C, Y, X, Z) <=> conspiracy(C) and leader(C) = X and member(C,Y) and member(C,Z);"));
		assertEquals(expected, result);

		string = "mother(X) = lucy";
		result = ruleConverter.queryRuleAndAtom(ruleParser.parseFormula(string), null);
		expected = new Pair<Expression, Expression>(ruleParser.parseFormula("query(X)"), 
				ruleParser.parse("query(X) <=> mother(X) = lucy;"));
		assertEquals(expected, result);
	}

	@Test
	public void testCreateModel () {
		List<Expression> sorts = new ArrayList<Expression>();
		sorts.add(lowParser.parse("sort(People,   Unknown, {ann, bob})"));
		sorts.add(lowParser.parse("sort(Treasure, Unknown, {gold, silver, diamonds})"));
		
		List<Expression> randomVariables = new ArrayList<Expression>();
		randomVariables.add(lowParser.parse("randomVariable(gaveTreasureTo, 3, People, Treasure, People)"));
		randomVariables.add(lowParser.parse("randomVariable(owns, 2, People, Treasure)"));
		randomVariables.add(lowParser.parse("randomVariable(rich, 1, People)"));

		List<Expression> parfactors = new ArrayList<Expression>();
		parfactors.add(lowParser.parse(
				"{{(on X in People, Y in People, Z in Treasure) [if gaveTreasureTo(X,Z,Y) then (if owns(Y,Z) then 1 else 0)  else 1] }}"));
		parfactors.add(lowParser.parse(
				"{{(on X in People, Z in Treasure) [if owns(X,Z) then if rich(X) then 1 else 0 else 1] }}"));

		Model model = ruleConverter.createModel("Gave Treasure To", 
				"An example of how hard it is to model things without aggregate factors.", 
				sorts, randomVariables, parfactors);
		System.out.println(model);
	}

	@Test
	public void testParse () {
		String modelString, queryString;
		Pair<Expression, Model> result;

		modelString = "if mother(X) = Y then X != Y;" +
				"if trait(mother(X)) then trait(X) 0.8 else trait(X) 0.3;" +
				"there exists X : trait(X);" +
				"mother(bob)=mary;" +
				"mother(ann)=mary;" +
				"mother(john)=ann;" +
				"trait(john);";
		queryString = "trait(mary)";
		try {
			result = ruleConverter.parseModel("Test Model", "Description", modelString, queryString);
			System.out.println(result.first);
			System.out.println(result.second);
		}
		catch (ReservedWordException e) {
			e.printStackTrace();
		}
		catch (ModelException e) {
			e.printStackTrace();
			Assert.fail("Errors in model string " + modelString + ": " + Util.join(e.getErrors()));
		}

		// Missing sort declaration for People.
		modelString = "random president: -> People;" +
				"random firstLady: -> People;" +
				"president = barrackObama <=> firstLady = michelleObama;" +
				"president = billClinton <=> firstLady = hillaryClinton;" +
				"firstLady = michelleObama 0.9;";
		queryString = "president";
		try {
			result = ruleConverter.parseModel("Test Model", "Description", modelString, queryString);
			System.out.println(result.first);
			System.out.println(result.second);
		}
		catch (ReservedWordException e) {
			e.printStackTrace();
		}
		catch (ModelException e) {
			e.printStackTrace();
			Assert.fail("Errors in model string " + modelString + ": " + Util.join(e.getErrors()));
		}

		modelString = "there exists X : X = bestFriend(X) 0.9;";
		queryString = "bestFriend(john)";
		try {
			result = ruleConverter.parseModel("Test Model", "Description", modelString, queryString);
			System.out.println(result.first);
			System.out.println(result.second);
		}
		catch (ReservedWordException e) {
			e.printStackTrace();
		}
		catch (ModelException e) {
			e.printStackTrace();
			Assert.fail("Errors in model string " + modelString + ": " + Util.join(e.getErrors()));
		}
		
		modelString = "/**\n"+
				" * Example 1: Epidemic and Sick with Symtoms.\n"+
				" * An example of the interplay between symtoms.\n" +
				" * Using Atomic and Conditional Rule Syntax.\n" +
				" */\n"+
				"//\n"+
				"// SORT DECLARATIONS:\n"+
				"sort People: 10, bob, dave, rodrigo, ciaran;\n"+
				"\n"+
				"//\n"+
				"// RANDOM VARIABLE DECLARATIONS:\n"+
				"random epidemic: -> Boolean;\n"+
				"random sick: People -> Boolean;\n"+
				"random fever: People -> Boolean;\n"+
				"random rash: People -> Boolean;\n"+
				"random notAtWork: People -> Boolean;\n" +
				"\n"+
				"//\n"+
				"// RULES\n" +
				"if epidemic then sick(X) 0.6 else sick(X) 0.05;\n" +
				"if sick(X) then fever(X) 0.7 else fever(X) 0.01;\n"+
				"if sick(X) then rash(X) 0.6 else rash(X) 0.07;\n"+
				"if sick(X) then notAtWork(X) 0.8 else notAtWork(X) 0.05;\n"+
				"\n"+
				"// By default, how likely is an epidemic?\n" +
				"epidemic 0.001;\n" +
				"\n"+
				"//\n"+
				"// By default, how likely are the following conditions?\n" +
				"sick(X) 0.009;\n"+
				"rash(X) 0.005;\n"+
				"fever(X) 0.001;\n";
		queryString = "sick(X)";
		try {
			result = ruleConverter.parseModel("Test Model", "Description", modelString, queryString);
			System.out.println(result.first);
			System.out.println(result.second);
		}
		catch (ReservedWordException e) {
			e.printStackTrace();
		}
		catch (ModelException e) {
			e.printStackTrace();
			Assert.fail("Errors in model string " + modelString + ": " + Util.join(e.getErrors()));
		}
	}
	
	@Test
	public void testQueryResultToRule () {
		Expression input, queryAtom, query, result, expected;

		input = lowParser.parse("if query then 0.3 else 0.7");
		queryAtom = lowParser.parse("query");
		query = lowParser.parse("sick(bob)");
		result = ruleConverter.queryResultToRule(input, queryAtom, query);
		expected = ruleParser.parse("sick(bob) 0.3;");
		assertEquals(expected, result);

		input = lowParser.parse("if query(bob) then 0.3 else 0.7");
		queryAtom = lowParser.parse("query(X)");
		query = lowParser.parse("sick(X)");
		result = ruleConverter.queryResultToRule(input, queryAtom, query);
		expected = ruleParser.parse("sick(bob) 0.3;");
		assertEquals(expected, result);

		input = lowParser.parse("if query(bob, mary) then 0.3 else 0.7");
		queryAtom = lowParser.parse("query(X, Y)");
		query = lowParser.parse("sick(X) = cold(Y)");
		result = ruleConverter.queryResultToRule(input, queryAtom, query);
		expected = ruleParser.parse("sick(bob) = cold(mary) 0.3;");
		assertEquals(expected, result);

	}

	@Test
	public void testPotentialExpressionToRule () {
		Expression input, result, expected;

		input = lowParser.parse("if sick(bob) then 0.3 else 0.7");
		result = ruleConverter.potentialExpressionToRule(input);
		expected = ruleParser.parse("sick(bob) 0.3;");
		assertEquals(expected, result);

		input = lowParser.parse("if X = bob then if sick(bob) then 0.7 else 0.3 else if sick(X) then 0.2 else 0.8");
		result = ruleConverter.potentialExpressionToRule(input);
		expected = ruleParser.parse("if X = bob then sick(bob) 0.7 else sick(X) 0.2;");
		assertEquals(expected, result);
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
			result = ruleConverter.translateRule(ruleParser.parse(inputString));
		else
			result = ruleConverter.translateRule(inputExpr);
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



	protected void testParseModel (String name, String desc, String input) {
		testParseModel(name, desc, input, true);
	}
	
	protected void testParseModel (String name, String desc, String input, Expression expectedResult) {
		testParseModel(name, desc, input, null, true, true, expectedResult);
	}
	
	protected void testParseModel (String name, String desc, String input, boolean expectSucceed) {
		testParseModel(name, desc, input, null, expectSucceed, false, null);
	}

	protected void testParseModel (String name, String desc, List<Expression> input) {
		testParseModel(name, desc, input, true);
	}
	
	protected void testParseModel(String name, String desc, List<Expression> input, Expression expectedResult) {
		testParseModel(name, desc, null, input, true, true, expectedResult);
	}
	
	protected void testParseModelFail (String name, String desc, List<Expression> input) {
		testParseModel(name, desc, input, false);
	}
	
	protected void testParseModel (String name, String desc, List<Expression> input, boolean expectSucceed) {
		testParseModel(name, desc, null, input, expectSucceed, false, null);
	}

	protected void testParseModel (String name, String desc, String inputString, List<Expression> inputExpr, boolean expectSucceed, boolean checkResult, Expression expectedResult) {
		testCount ++;
		Model result;
		try {
			if (inputExpr == null) {
				result = ruleConverter.parseModel(name, desc, inputString).second;
			} 
			else {
				result = ruleConverter.parseModel(name, desc, inputExpr).second;
			}
			if (expectSucceed) {
				if (checkResult) {
					assertEquals(expectedResult.toString(), result.toString());
				}
				else {
					if(result != null) {
//						if (inputString != null)
//							System.out.println("generated string for \"" + inputString + "\": " + generateBuildString(result) + "\n\n");
//						else
//							System.out.println("generated string : " + generateBuildString(result) + "\n\n");
					}
//					Assert.assertNotNull(result);
				}
			}
			else {
				Assert.assertNull(result);
			}
		}
		catch (ReservedWordException e) {
			e.printStackTrace();
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
	
	// Note: Pass the VM argument
    // -Dgrinder.wait.until.ui.closed.enabled=true
    // in the Eclipse run configuration for this test, in order for the tree to stay up when this is called.
    protected void doTreeUtilWaitUnilClosed() {
        if (GrinderConfiguration.isWaitUntilUIClosedEnabled()) {
            TreeUtil.waitUntilUIClosed();
        }
    } 
	
//	private void compareTree (Expression e1, Expression e2) {
//		if (e1.equals(e2)) {
//			System.out.println("True : " + e1 + " = " + e2);
//		}
//		else {
//			System.out.println("False: " + e1 + " != " + e2);
//		}
//		if (e1.getArguments().size() > 0) {
//			List<Expression> e1Args = e1.getArguments();
//			List<Expression> e2Args = e2.getArguments();
//			for (int i = 0; i < e1Args.size(); i++) {
//				compareTree(e1Args.get(i), e2Args.get(i));
//			}
//		}
//	}
//	
//	private void printTree (Expression e) {
//		if (e.getArguments().size() == 0) {
//			System.out.print(e.toString());
//		}
//		else {
//			System.out.print(e.getFunctor());
//			List<Expression> args = e.getArguments();
//			System.out.print('(');
//			boolean first = true;
//			for (Expression arg : args) {
//				if (first) {
//					first = false;
//				}
//				else {
//					System.out.print(", ");
//				}
//				printTree(arg);
//			}
//			System.out.print(')');
//		}
//	}

}
