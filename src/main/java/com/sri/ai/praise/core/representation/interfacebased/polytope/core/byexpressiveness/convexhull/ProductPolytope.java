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
package com.sri.ai.praise.core.representation.interfacebased.polytope.core.byexpressiveness.convexhull;

import static com.sri.ai.praise.core.representation.interfacebased.polytope.core.byexpressiveness.convexhull.Polytopes.multiplyListOfAlreadyMultipledNonIdentityAtomicPolytopesWithANewOne;
import static com.sri.ai.util.Util.accumulate;
import static com.sri.ai.util.Util.join;
import static com.sri.ai.util.Util.mapIntoList;
import static com.sri.ai.util.Util.myAssert;
import static com.sri.ai.util.Util.unionOfCollections;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;
import com.sri.ai.praise.core.representation.interfacebased.polytope.api.AtomicPolytope;
import com.sri.ai.praise.core.representation.interfacebased.polytope.api.Polytope;

/**
 * @author braz
 *
 */
public class ProductPolytope implements Polytope {
	
	private List<? extends AtomicPolytope> nonIdentityAtomicPolytopes;

	ProductPolytope(Collection<? extends AtomicPolytope> nonIdentityAtomicPolytopes) {
		super();
		myAssert(nonIdentityAtomicPolytopes.size() != 0, () -> "Cannot define product on an empty set of polytopes. Create an IdentityPolytope instead.");
		myAssert(nonIdentityAtomicPolytopes.size() != 1, () -> "Cannot define product on a single element. Use the single element instead.");
		this.nonIdentityAtomicPolytopes = new LinkedList<>(nonIdentityAtomicPolytopes);
	}
	
	public Collection<? extends Polytope> getPolytopes() {
		return nonIdentityAtomicPolytopes;
	}

	@Override
	public Polytope multiply(Polytope another) {
		Polytope result;
		if (another.isIdentity()) {
			result = this;
		}
		else if (another instanceof ProductPolytope) {
			Collection<? extends Polytope> anotherSubPolytopes = ((ProductPolytope)another).getPolytopes();
			result = accumulate(anotherSubPolytopes, Polytope::multiply, this);
		}
		else {
			AtomicPolytope nonIdentityAtomicAnother = (AtomicPolytope) another;
			result = multiplyListOfAlreadyMultipledNonIdentityAtomicPolytopesWithANewOne(nonIdentityAtomicPolytopes, nonIdentityAtomicAnother);
		}
		return result;
	}

	@Override
	public boolean isIdentity() {
		return false;
	}

	@Override
	public Collection<? extends Variable> getFreeVariables() {
		
		Collection<Collection<? extends Variable>> listOfFreeVariablesCollections = 
				mapIntoList(nonIdentityAtomicPolytopes, Polytope::getFreeVariables);
		
		Set<? extends Variable> allFreeVariables = unionOfCollections(listOfFreeVariablesCollections);
		
		return allFreeVariables;
	}

	@Override
	public String toString() {
		String result = join(nonIdentityAtomicPolytopes, "*");
		return result;
	}
	
	@Override
	public boolean equals(Object another) {
		boolean result =
				another instanceof ProductPolytope
				&&
				((ProductPolytope) another).getPolytopes().equals(getPolytopes());
		return result;
	}
	
	@Override
	public int hashCode() {
		return getPolytopes().hashCode();
	}
}