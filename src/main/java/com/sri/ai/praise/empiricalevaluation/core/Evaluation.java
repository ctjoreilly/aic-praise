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
package com.sri.ai.praise.empiricalevaluation.core;

import static com.sri.ai.util.Util.mapIntoList;
import static com.sri.ai.util.Util.myAssert;

import java.util.List;
import java.util.StringJoiner;

import com.sri.ai.praise.empiricalevaluation.api.configuration.SolverEvaluationConfiguration;
import com.sri.ai.praise.model.common.io.ModelPage;
import com.sri.ai.praise.model.common.io.PagedModelContainer;
import com.sri.ai.praise.pimt.ExpressionFactorsAndTypes;
import com.sri.ai.praise.probabilisticsolver.Solver;
import com.sri.ai.praise.probabilisticsolver.SolverConfiguration;
import com.sri.ai.praise.probabilisticsolver.SolverResult;

/**
 * Class responsible for performing an evaluation of one or more solvers on a given problem set.
 * 
 * @author oreilly
 *
 */
public class Evaluation {	
	
	// NOTE: Based on http://www.hlt.utdallas.edu/~vgogate/uai14-competition/information.html
	public enum ProblemType {
		PR,   // Computing the the partition function and probability of evidence
		MAR,  // Computing the marginal probability distribution over variable(s) given evidence
		MAP,  // Computing the most likely assignment to all variables given evidence (also known as MPE, Most Probable Explanation)
		MMAP, // Computing the most likely assignment to a subset of variables given evidence (Marginal MAP)
	};
	
	public void evaluate(SolverEvaluationConfiguration configuration, List<SolverConfiguration> solverConfigurations, PagedModelContainer modelsToEvaluateContainer, OutputListener outputListener) {
		long evaluationStart = System.currentTimeMillis();	
		try {
			evaluateWithoutTiming(configuration, solverConfigurations, modelsToEvaluateContainer, outputListener);
		}
		catch (Exception exception) {
			outputListener.notificationException(exception);
		}
		long evaluationEnd = System.currentTimeMillis();
		outputListener.notification("Evaluation took " + toDurationString(evaluationEnd - evaluationStart) + " to run to completion.");
	}

	private void evaluateWithoutTiming(SolverEvaluationConfiguration configuration, List<SolverConfiguration> solverConfigurations, PagedModelContainer modelsToEvaluateContainer, OutputListener outputListener) throws Exception {

		List<Solver> solvers = initializeSolversAndHeader(configuration, solverConfigurations, modelsToEvaluateContainer, outputListener);
		evaluateAllModels(solvers, modelsToEvaluateContainer, configuration, outputListener);
	}

	private List<Solver> initializeSolversAndHeader(SolverEvaluationConfiguration configuration, List<SolverConfiguration> solverConfigurations, PagedModelContainer modelsToEvaluateContainer, OutputListener outputListener) throws Exception {
		
		List<Solver> solvers = makeSolvers(solverConfigurations);
		
		doInitialBurnInToEnsureOSCachingEtcOccurBeforeMeasuringPerformance(configuration, solvers, modelsToEvaluateContainer, outputListener);
		
		outputReportHeaderLine(solvers, outputListener);
		
		return solvers;
	}

	private List<Solver> makeSolvers(List<SolverConfiguration> solverConfigurations) {
		List<Solver> result = mapIntoList(solverConfigurations, this::tryToMakeSolver);
		return result;
	}

	private Solver tryToMakeSolver(SolverConfiguration solverConfiguration) {
		return makeSolverFromSolverConfiguration(solverConfiguration);
	}

	@SuppressWarnings("unchecked")
	private Solver makeSolverFromSolverConfiguration(SolverConfiguration solverConfiguration) {
		try {
			String solverClassName = solverConfiguration.getImplementationClassName();
			Class<? extends Solver> solverClass = (Class<? extends Solver>) Class.forName(solverClassName);
			Solver result = makeSolver(solverClass, solverConfiguration);
			return result;
		} catch (Throwable throwable) {
			throw new Error(throwable);
		}
	}

	private Solver makeSolver(Class<? extends Solver> solverImplementationClass, SolverConfiguration solverConfiguration) throws InstantiationException, IllegalAccessException {
		Solver solver = solverImplementationClass.newInstance();
		solver.setConfiguration(solverConfiguration);
		return solver;
	}

	private void doInitialBurnInToEnsureOSCachingEtcOccurBeforeMeasuringPerformance(SolverEvaluationConfiguration solverEvaluationConfiguration, List<Solver> solvers, PagedModelContainer modelsToEvaluateContainer, OutputListener outputListener) throws Exception {
		ModelPage burnInModel = modelsToEvaluateContainer.getPages().get(0);
		String    burnInQuery = burnInModel.getDefaultQueriesToRun().get(0);  
		notifyAboutBeginningOfBurnInForAllSolvers(modelsToEvaluateContainer, outputListener, burnInModel, burnInQuery);
		for (Solver solver : solvers) {
			performBurnInForSolver(solver, solverEvaluationConfiguration, burnInModel, burnInQuery, outputListener);
		}
	}

	private void notifyAboutBeginningOfBurnInForAllSolvers(PagedModelContainer modelsToEvaluateContainer, OutputListener outputListener, ModelPage burnInModel, String burnInQuery) {
		outputListener.notification("Starting burn in for all solvers based on '" + modelsToEvaluateContainer.getName() + " - " + burnInModel.getName() + " : " + burnInQuery + "'");
	}

	private void performBurnInForSolver(Solver solver, SolverEvaluationConfiguration configuration, ModelPage burnInModel, String burnInQuery, OutputListener outputListener) throws Exception {
		SolverEvaluationResult solverEvaluationResult = new SolverEvaluationResult();	
		for (int i = 0; i < configuration.getNumberRunsToAverageOver(); i++) {
			performOneRunOfBurnInForSolver(solver, configuration.getType(), burnInModel, burnInQuery, solverEvaluationResult);
		}
		solverEvaluationResult.recordAverageTime(configuration.getNumberRunsToAverageOver());
		notifyAboutOneRunOfBurnInResult(outputListener, solver, solverEvaluationResult);
	}

	private void performOneRunOfBurnInForSolver(Solver solver, ProblemType type, ModelPage burnInModel, String burnInQuery, SolverEvaluationResult result) throws Exception {
		myAssert(type == ProblemType.PR, () -> unsupported(type));
		performOneRunOfPRBurnIn(solver, burnInModel, burnInQuery, result);
	}

	private void performOneRunOfPRBurnIn(Solver solver, ModelPage burnInModel, String burnInQuery, SolverEvaluationResult solverEvaluationResult) throws Exception {
		SolverResult burnInResult = getBurnInResult(solver, burnInModel, burnInQuery);
		solverEvaluationResult.aggregateSingleRunSolverResult(burnInResult); 
	}

	private SolverResult getBurnInResult(Solver solver, ModelPage burnInModel, String burnInQuery) throws Exception {
		SolverResult result = 
				solver.solve(
						burnInModel.getName() + " - " + burnInQuery, 
						burnInModel.getLanguage(), 
						burnInModel.getModel(), 
						burnInQuery);
		return result;
	}

	private void notifyAboutOneRunOfBurnInResult(OutputListener outputListener, Solver solver, SolverEvaluationResult result) {
		outputListener.notification("Burn in for " + solver.getName() + " complete. Average inference time = " + toDurationString(result.averageInferenceTimeInMilliseconds));
	}

	private void outputReportHeaderLine(List<Solver> solvers, OutputListener outputListener) {
		StringJoiner csvLine = new StringJoiner(",");
		writeInitialHeader(csvLine);
		for (Solver solver : solvers) {
			writeHeaderForSolver(csvLine, solver);
		}
		outputListener.notification("Starting to generate Evaluation Report");
		outputListener.csvResultOutput(csvLine.toString());
	}

	private void writeInitialHeader(StringJoiner csvLine) {
		csvLine.add("Problem");
		csvLine.add("Inference Type");
		csvLine.add("Domain Size(s)");
		csvLine.add("# runs values averaged over");
	}

	private void writeHeaderForSolver(StringJoiner csvLine, Solver solver) {
		csvLine.add("Solver");
		csvLine.add("Result for " + solver.getName());
		csvLine.add("Inference ms. for " + solver.getName());
		csvLine.add("HH:MM:SS.");
		csvLine.add("Translation ms. for " + solver.getName());
		csvLine.add("HH:MM:SS.");
	}

	private void evaluateAllModels(List<Solver> solvers, PagedModelContainer modelsToEvaluateContainer, SolverEvaluationConfiguration configuration, OutputListener outputListener) throws Exception {
		for (ModelPage model : modelsToEvaluateContainer.getPages()) {
			evaluateModel(model, modelsToEvaluateContainer, solvers, configuration, outputListener);
		}
	}

	private void evaluateModel(ModelPage model, PagedModelContainer modelsToEvaluateContainer, List<Solver> solvers, SolverEvaluationConfiguration configuration, OutputListener outputListener) throws Exception {
		String domainSizes = getDomainSizes(model.getModel());
		for (String query: model.getDefaultQueriesToRun()) {
			evaluateQuery(query, model, modelsToEvaluateContainer, solvers, domainSizes, configuration, outputListener);
		}
	}

	private void evaluateQuery(String query, ModelPage model, PagedModelContainer modelsToEvaluateContainer, List<Solver> solvers, String domainSizes, SolverEvaluationConfiguration configuration, OutputListener outputListener) throws Exception {
		String problemName = modelsToEvaluateContainer.getName()+" - "+model.getName() + " : " + query;
		StringJoiner csvLine = initializeQueryLine(domainSizes, configuration, outputListener, problemName);
		for (Solver solver : solvers) {
			evaluateSolver(solver, query, model, configuration, csvLine, problemName, outputListener);
		}
		outputListener.csvResultOutput(csvLine.toString());
	}

	private StringJoiner initializeQueryLine(String domainSizes, SolverEvaluationConfiguration configuration, OutputListener outputListener, String problemName) {
		outputListener.notification("Starting to evaluate " + problemName);
		StringJoiner csvLine;
		csvLine = new StringJoiner(",");
		csvLine.add(problemName);
		csvLine.add(configuration.getType().name());
		csvLine.add(domainSizes);
		csvLine.add(" " + configuration.getNumberRunsToAverageOver());
		return csvLine;
	}

	private void evaluateSolver(Solver solver, String query, ModelPage model, SolverEvaluationConfiguration configuration, StringJoiner csvLine, String problemName, OutputListener outputListener) throws Exception {
		SolverEvaluationResult solverEvaluationResult = new SolverEvaluationResult();	
		
		for (int i = 0; i < configuration.getNumberRunsToAverageOver(); i++) {
			tryToPerformOneRunForConfiguation(solver, query, model, configuration, solverEvaluationResult);
		}
		solverEvaluationResult.recordAverageTime(configuration.getNumberRunsToAverageOver());
		outputSolverOutput(problemName, solver, csvLine, solverEvaluationResult, outputListener);
	}

	private void tryToPerformOneRunForConfiguation(Solver solver, String query, ModelPage model, SolverEvaluationConfiguration configuration, SolverEvaluationResult solverResult) throws Exception {
		if (configuration.getType() == ProblemType.PR) {
			performOneRun(solver, query, model, solverResult);
		}
		else {
			throw new UnsupportedOperationException(configuration.getType().name() + " type evaluations are currently not supported");
		}
	}

	private void performOneRun(Solver solver, String query, ModelPage model, SolverEvaluationResult solverEvaluationResult) throws Exception {
		String queryName = model.getName() + " - " + query;
		SolverResult solverResult = solver.solve(queryName, model.getLanguage(), model.getModel(), query);
		solverEvaluationResult.aggregateSingleRunSolverResult(solverResult);
	}

	private void outputSolverOutput(String problemName, Solver solver, StringJoiner csvLine, SolverEvaluationResult solverResult, OutputListener outputListener) {
		csvLine.add(solver.getName());
		csvLine.add(solverResult.failed ? "FAILED" : "" + solverResult.answer);
		csvLine.add("" + solverResult.averageInferenceTimeInMilliseconds);
		csvLine.add(toDurationString(solverResult.averageInferenceTimeInMilliseconds));
		csvLine.add("" + solverResult.averagelTranslationTimeInMilliseconds);
		csvLine.add(toDurationString(solverResult.averagelTranslationTimeInMilliseconds));
		
		outputListener.notification("Solver "+solver.getName()+" took an average inference time of "+toDurationString(solverResult.averageInferenceTimeInMilliseconds)+" to solve "+problemName);
	}

	private String getDomainSizes(String model) {
		StringJoiner result = new StringJoiner(",");
		ExpressionFactorsAndTypes factorsAndTypes = new ExpressionFactorsAndTypes(model);
		for (com.sri.ai.expresso.api.Type type : factorsAndTypes.getAdditionalTypes()) {
			result.add("" + type.cardinality().intValueExact());
		}
		return result.toString();
	}
	
	private String toDurationString(long duration) {
		long hours = 0L, minutes = 0L, seconds = 0L, milliseconds = 0L;
		long remainingDuration = duration;
		
		if (remainingDuration != 0) {
			hours    = remainingDuration / 3600000;
			remainingDuration = remainingDuration % 3600000; 
		}
		if (remainingDuration != 0) {
			minutes  = remainingDuration / 60000;
			remainingDuration = remainingDuration % 60000;
		}
		if (remainingDuration != 0) {
			seconds  = remainingDuration / 1000;
			remainingDuration = remainingDuration % 1000;
		}
		milliseconds = remainingDuration;
		
		return hours + "h" + minutes + "m" + seconds + "." + milliseconds + "s";
	}

	private String unsupported(ProblemType type) {
		return type + " is current unsupported by " + Evaluation.class;
	}
}
