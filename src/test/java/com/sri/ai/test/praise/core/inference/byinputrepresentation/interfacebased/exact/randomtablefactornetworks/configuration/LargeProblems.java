package com.sri.ai.test.praise.core.inference.byinputrepresentation.interfacebased.exact.randomtablefactornetworks.configuration;

import java.util.Random;

public class LargeProblems extends AbstractConfigurationForTestsOnRandomTableFactorNetworks {

	public LargeProblems() {
		random = new Random(0); // fixed seed since we usually want to compare performance between runs
		numberOfTests = 10;
		minimumNumberOfVariables = 10;
		maximumNumberOfVariables = 25;
		minimumCardinality = 2;
		maximumCardinality = 2;
		minimumNumberOfFactors = 10;
		maximumNumberOfFactors = 25;
		minimumNumberOfVariablesPerFactor = 3;
		maximumNumberOfVariablesPerFactor = 6;
		minimumPotential = 1.0;
		maximumPotential = 4.0;
	}
}
