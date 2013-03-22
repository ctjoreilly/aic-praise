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
package com.sri.ai.praise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.api.ExpressionAndContext;
import com.sri.ai.expresso.api.SyntaxTree;
import com.sri.ai.expresso.core.DefaultCompoundSyntaxTree;
import com.sri.ai.expresso.core.DefaultExpressionAndContext;
import com.sri.ai.expresso.core.DefaultSymbol;
import com.sri.ai.expresso.helper.ExpressionKnowledgeModule;
import com.sri.ai.expresso.helper.Expressions;
import com.sri.ai.expresso.helper.SubSyntaxTreeAndPathWhoseParentsSatisfyAGivenPredicateIterator;
import com.sri.ai.grinder.api.NoOpRewriter;
import com.sri.ai.grinder.api.RewritingProcess;
import com.sri.ai.grinder.core.AbstractRewriter;
import com.sri.ai.grinder.core.FunctionApplicationProvider;
import com.sri.ai.grinder.library.ScopedVariables;
import com.sri.ai.grinder.library.function.InjectiveModule;
import com.sri.ai.grinder.library.function.MutuallyExclusiveCoDomainsModule;
import com.sri.ai.grinder.library.lambda.Lambda;
import com.sri.ai.praise.model.ExpressionInExpressionPathPairIsValueOfRandomVariable;
import com.sri.ai.praise.model.IsRandomVariableValueExpression;
import com.sri.ai.util.Util;
import com.sri.ai.util.base.Pair;
import com.sri.ai.util.collect.FunctionIterator;
import com.sri.ai.util.collect.PredicateIterator;

/**
 * 
 * @author braz
 *
 */
@Beta
public class BracketedExpressionSubExpressionsProvider extends AbstractRewriter
implements
NoOpRewriter,
ScopedVariables.Provider,
ExpressionKnowledgeModule.Provider,
InjectiveModule.Provider,
MutuallyExclusiveCoDomainsModule.Provider {

	private static List<Expression> _emptyExpressionList = Collections.emptyList();
	//
	public static final String SYNTAX_TREE_LABEL = "[ . ]";
	//
	private Cache<Expression, Expression> injectiveFunctionTokenCache = CacheBuilder.newBuilder().maximumSize(100).build();

	public static boolean isBracketedExpression(Expression expression) {
		return expression.getSyntaxTree().getLabel().equals(SYNTAX_TREE_LABEL);
	}
	
	public static Expression make(Expression valueExpression) {
		return Expressions.make(BracketedExpressionSubExpressionsProvider.SYNTAX_TREE_LABEL, valueExpression);
	}

	public static Expression getRandomVariableValueExpression(Expression bracketedRandomVariable) {
		Expression result = bracketedRandomVariable.getSyntaxTree().getSubTree(0); // does need to be the syntax tree here, not just expression
		return result;
	}

	public static Expression getExpressionInBrackets(Expression bracketedExpression) {
		Expression result = bracketedExpression.getSyntaxTree().getSubTree(0); // does need to be the syntax tree here, not just expression
		return result;
	}

	@Override
	public Expression getScopedVariablesAsExpression(Expression syntaxTree, RewritingProcess process) {
		if (isBracketedExpression(syntaxTree)) {
			return DefaultCompoundSyntaxTree.make("list", _emptyExpressionList);
		}
		return null;
	}

	@Override
	public Object getSyntacticFormType(Expression expression, RewritingProcess process) {
		if (isBracketedExpression(expression)) {
			return "Bracketed expression";
		}
		return null;
	}

	@Override
	public Iterator<ExpressionAndContext> getImmediateSubExpressionsAndContextsIterator(Expression expression, final RewritingProcess process) {
		if (isBracketedExpression(expression)) {
			// The sub-expressions (and contexts) of a bracketed expression are made from
			// those expression-path pairs whose parents
			// are random variable value expressions, excepting the functors of those expressions.
			// For example, p(X,a) is the parent of p, X and a. We reject the functor p and
			// take X and a to be sub-expressions (with their respective contexts).

			Iterator<Pair<Expression, List<Integer>>> subExpressionOfRandomVariableValueExpressionAndPathPairsIterator =
				new SubSyntaxTreeAndPathWhoseParentsSatisfyAGivenPredicateIterator(
						expression,
						new ExpressionInExpressionPathPairIsValueOfRandomVariable(process));
			
			Iterator<Pair<Expression, List<Integer>>> subExpressionOfRandomVariableValueAndPathPairsMinusFunctorIterator =
				new PredicateIterator<Pair<Expression, List<Integer>>>(subExpressionOfRandomVariableValueExpressionAndPathPairsIterator, REJECT_FUNCTOR);

			
			Iterator<ExpressionAndContext> result =
				new FunctionIterator<Pair<Expression, List<Integer>>, ExpressionAndContext>(
						subExpressionOfRandomVariableValueAndPathPairsMinusFunctorIterator,
						new DefaultExpressionAndContext.MakerFromExpressionAndPathPair(_emptyExpressionList));

			return result;
		}
		return null;
	}

	@Override
	public void rewritingProcessInitiated(RewritingProcess process) {
		ScopedVariables scopedVariables =
			(ScopedVariables) process.findModule(ScopedVariables.class);
		if (scopedVariables != null) {
			scopedVariables.register(this);
		}
		
		ExpressionKnowledgeModule knowledgeBasedExpressionModule =
			(ExpressionKnowledgeModule) process.findModule(ExpressionKnowledgeModule.class);
		if (knowledgeBasedExpressionModule != null) {
			knowledgeBasedExpressionModule.register(this);
		}

		InjectiveModule injectiveModuleModule = (InjectiveModule) process.findModule(InjectiveModule.class);
		if (injectiveModuleModule != null) {
			injectiveModuleModule.register(this);
		}

		MutuallyExclusiveCoDomainsModule mutuallyExclusiveCoDomainsModule = (MutuallyExclusiveCoDomainsModule) process.findModule(MutuallyExclusiveCoDomainsModule.class);
		if (mutuallyExclusiveCoDomainsModule != null) {
			mutuallyExclusiveCoDomainsModule.register(this);
		}
	}

	@Override
	public Expression rewriteAfterBookkeeping(Expression expression, RewritingProcess process) {
		// Note: is a NoOpRewriter
		return expression; // will be removed eventually, not a real rewriter, just a module.
	}

	private static final Predicate<Pair<Expression, List<Integer>>> REJECT_FUNCTOR = new Predicate<Pair<Expression, List<Integer>>>() {
		public boolean apply(Pair<Expression, List<Integer>> pair) {
			boolean result = ! Util.getLast(pair.second).equals(FunctionApplicationProvider.INDEX_OF_FUNCTOR_IN_FUNCTION_APPLICATIONS);
			return result;
		}
	};

	@Override
	public boolean haveMutuallyExclusiveCoDomains(Expression expression1,
			Expression expression2, RewritingProcess process) {
		if (isBracketedExpression(expression1) && isBracketedExpression(expression2) /* &&
				isRandomVariable(expression1, process) &&
				isRandomVariable(expression2, process) */) {
			
			Object functionToken1 = getInjectiveFunctionToken(expression1, process);
			Object functionToken2 = getInjectiveFunctionToken(expression2, process);
			return ! functionToken1.equals(functionToken2);
		}
		return false;
	}

	public static boolean isRandomVariable(Expression expression,
			RewritingProcess process) {
		return IsRandomVariableValueExpression.apply(getExpressionInBrackets(expression), process);
	}
	
	// TODO - currently does not handle bracketed expressions containing quantified sub-expressions.
	@Override
	public Object getInjectiveFunctionToken(Expression expression, RewritingProcess process) {
		Expression result = null;
		if (isBracketedExpression(expression)) {
			// Check if we have already calculated first
			result = injectiveFunctionTokenCache.getIfPresent(expression);
			if (result == null) {
				// Collect up the expressions to be parameterized and their corresponding paths.
				// (i.e. constants and logical variables used inside of random variable predicates).		
				Iterator<ExpressionAndContext> subExpressionsIterator = this.getImmediateSubExpressionsAndContextsIterator(expression, process);
				int i = 1;
				List<Expression> parameters = new ArrayList<Expression>();
				Expression       lambdaBody = expression;
				while (subExpressionsIterator.hasNext()) {
					ExpressionAndContext subExpressionAndContext = subExpressionsIterator.next();
					
					// Note: By using paths for the replacement call, 
					// we don't need to worry about having to ensure there is not a 
					// clash between the normalized parameter names to be used (i.e. X1 to Xn) 
					// and the existing parameters, as each instance of an existing parameter (even if repeated)
					// is replaced with a new unique normalized parameter.
					Expression parameter = DefaultSymbol.createSymbol("X"+i++);
					parameters.add(parameter);
					lambdaBody = Expressions.replaceAtPath(lambdaBody, subExpressionAndContext.getPath(), parameter);
				}
				
				// Create the token
				result = Lambda.make(parameters, lambdaBody);
				
				injectiveFunctionTokenCache.put(expression, result);
			}
		}
		return result;
	}
	
	public static class GetRandomVariableValueExpression implements Function<SyntaxTree, SyntaxTree> {
		@Override
		public SyntaxTree apply(SyntaxTree syntaxTree) {
			return syntaxTree.getSubTree(0); // does need to be sub tree
		}
	}
}
