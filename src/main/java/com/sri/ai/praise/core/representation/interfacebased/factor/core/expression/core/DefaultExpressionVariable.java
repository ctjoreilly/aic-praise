/*
 * Copyright (c) 2015, SRI International
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
package com.sri.ai.praise.core.representation.interfacebased.factor.core.expression.core;

import static com.sri.ai.expresso.helper.Expressions.makeSymbol;
import static com.sri.ai.expresso.helper.Expressions.parse;

import java.util.List;

import com.sri.ai.expresso.api.Expression;
import com.sri.ai.expresso.helper.WrappedExpression;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.expression.api.ExpressionVariable;
import com.sri.ai.util.Enclosing;

public class DefaultExpressionVariable extends WrappedExpression implements ExpressionVariable {

	private static final long serialVersionUID = 1L;

	/**
	 * Given an expression, returns an {@link ExpressionVariable} based on it,
	 * unless the given expression is already an {@link ExpressionVariable},
	 * in which case it is returned itself.
	 * <p>
	 * Because we may return the original object as a result, we do not allow a public constructor
	 * in this class, which would be forced to always construct a new instance.
	 * @param expression
	 * @return
	 */
	public static ExpressionVariable expressionVariable(Expression expression) {
		if (expression instanceof ExpressionVariable) {
			return (ExpressionVariable) expression;
		}
		else {
			return new DefaultExpressionVariable(expression);
		}
	}
	
	public static ExpressionVariable expressionVariable(String expressionString) {
		return expressionVariable(parse(expressionString));
	}

	protected DefaultExpressionVariable(Expression expression) {
		super(expression);
	}
	
	@Override
	public List<? extends Object> getValues() {
		throw new Error(getClass() + ".getValues() not implemented -- getValues() is deemed not needed for most algorithms.");
		// TODO: refactor to make getValues part of a specific interface used by the algorithms that require values
	}

	@Override
	/**
	 * If this expression variable is based on a symbol with a string value, returns
	 * a new expression value based on the symbol with the same string concatenated with a prime symbol.
	 * Otherwise, throws an Error.
	 */
	public ExpressionVariable makeNewVariableWithSameRangeButDifferentEqualsIdentity() {
		if (getSyntacticFormType().equals("Symbol") && getValue() instanceof String) {
			var stringValue = (String) getValue();
			var primedStringValue = stringValue + "'";
			var primedSymbol = makeSymbol(primedStringValue);
			return expressionVariable(primedSymbol);
		}
		else {
			throw new Error((new Enclosing() {}).methodName() + " not supported for " + getClass() + " that are not Symbols or do not have a String value: " + this + ", of class: " + getClass());
		}
	}

}