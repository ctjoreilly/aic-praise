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
package com.sri.ai.praise.model.imports.uai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.Beta;
import com.sri.ai.expresso.api.Expression;

import static com.sri.ai.praise.model.imports.uai.UAIUtil.constructTableExpression;

/**
 * 
 * @author oreilly
 *
 */
@Beta
public class UAIMARSolver {
	public static void main(String[] args) throws IOException {
		
		if (args.length != 1) {
			throw new IllegalArgumentException("Must specify UAI model directory of file to solve");
		}
		
		File uaiInput = new File(args[0]);
		if (!uaiInput.exists()) {
			throw new IllegalArgumentException("File or directory specified does not exist: "+uaiInput.getAbsolutePath());
		}
		
		List<UAIModel> models = new ArrayList<>();
		
		if (uaiInput.isDirectory()) {
			for (File uaiFile : uaiInput.listFiles((dir, name) -> name.endsWith(".uai"))) {
				models.add(read(uaiFile));
			}
		}
		else {
			models.add(read(uaiInput));
		}
		
		// Sort based on what we consider to be the simplest to hardest
		Collections.sort(models, (m1, m2) -> m1.ratioUniqueFunctionTableToCliques().compareTo(m2.ratioUniqueFunctionTableToCliques()));
		
		System.out.println("#model read="+models.size());
		models.stream().forEach(m -> {
			System.out.println("Starting to Solve: "+m.getFile().getName());
			solve(m);
			System.out.println("----\n");
		});
	}
	
	public static void solve(UAIModel model) {
		for (Map.Entry<FunctionTable, List<Integer>> tableToCliques : model.getTableToCliques()) {
			Expression tableExpression = constructTableExpression(tableToCliques.getKey());
		}
	}
	
	//
	// PRIVATE
	//
	private static UAIModel read(File uaiFile) throws IOException {
		UAIModel model = UAIModelReader.read(uaiFile);
		
		UAIEvidenceReader.read(model);
		
		return model;
	}
}
