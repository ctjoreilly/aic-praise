package com.sri.ai.test.praise.core.inference.representation.sampling;

import static com.sri.ai.util.Util.arrayList;
import static com.sri.ai.util.Util.list;
import static com.sri.ai.util.Util.println;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import com.sri.ai.praise.core.inference.byinputrepresentation.interfacebased.core.exactbp.fulltime.core.ExactBP;
import com.sri.ai.praise.core.representation.interfacebased.factor.api.Variable;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.base.DefaultFactorNetwork;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.base.DefaultVariable;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.api.factor.SamplingFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.api.sample.ImportanceFactory;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.api.sample.PotentialFactory;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.api.sample.Sample;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.distribution.EqualitySamplingFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.factor.SamplingProductFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.sample.DefaultSample;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.sample.DoubleImportanceFactory;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.sampling.core.sample.DoublePotentialFactory;

public class EqualitySamplingTest {

	private ImportanceFactory importanceFactory = new DoubleImportanceFactory();
	private PotentialFactory potentialFactory = new DoublePotentialFactory();
	private DefaultFactorNetwork network;
	private ExactBP solver;
	private SamplingFactor marginalOfX;

	//@Test
	public void testTwoEqualitiesOnSameVariable() {
		
		long numberOfSamples = 5;
		
		Variable x = new DefaultVariable("x");
		Variable y = new DefaultVariable("y");
		Variable z = new DefaultVariable("z");

		EqualitySamplingFactor xEqualsY = new EqualitySamplingFactor(x, y);
		EqualitySamplingFactor xEqualsZ = new EqualitySamplingFactor(x, z);

		SamplingFactor xEqualsYAndXEqualsZ = new SamplingProductFactor(arrayList(xEqualsY, xEqualsZ), new Random());
		println(xEqualsYAndXEqualsZ.nestedString(true));

		runTwoEqualitiesOnSameVariableTest(numberOfSamples, x, y, z, xEqualsYAndXEqualsZ);

	}

	private void runTwoEqualitiesOnSameVariableTest(long numberOfSamples, Variable x, Variable y, Variable z, SamplingFactor factor) {

		println("Working with x = y and x = z");

		Sample sample = new DefaultSample(importanceFactory, potentialFactory);
		
		try {
			println("Generating " + numberOfSamples + " samples from empty sample");
			for (int i = 0; i != numberOfSamples; i++) {
				factor.sampleOrWeigh(sample);
			}
		}
		catch (Throwable throwable) {
			if ( ! throwable.getMessage().contains("Factor was not able to complete sample") ) {
				throw throwable;
			}
			assertEquals(1.0, sample.getPotential().doubleValue(), 0.0);
			assertEquals(null, sample.getAssignment().get(x));
			assertEquals(null, sample.getAssignment().get(y));
			assertEquals(null, sample.getAssignment().get(z));
		}

		println("Generating " + numberOfSamples + " samples from X = 'A string value'");
		for (int i = 0; i != numberOfSamples; i++) {
			sample = new DefaultSample(importanceFactory, potentialFactory);
			sample.getAssignment().set(x, "A string value");
			factor.sampleOrWeigh(sample);
			assertEquals(1.0, sample.getPotential().doubleValue(), 0.0);
			assertEquals("A string value", sample.getAssignment().get(x));
			assertEquals("A string value", sample.getAssignment().get(y));
			assertEquals("A string value", sample.getAssignment().get(z));
		}
	}

	@Test
	public void testEquality() {
		
		long numberOfSamples = 5;
		
		Variable x = new DefaultVariable("x");
		Variable y = new DefaultVariable("y");

		EqualitySamplingFactor xEqualsY = new EqualitySamplingFactor(x, y);

		runEqualityTest(numberOfSamples, x, y, xEqualsY);

		network = new DefaultFactorNetwork(list(xEqualsY));
		solver = new ExactBP(x, network);
		marginalOfX = (SamplingFactor) solver.apply();

		runEqualityTest(numberOfSamples, x, y, marginalOfX);

	}

	private void runEqualityTest(long numberOfSamples, Variable x, Variable y, SamplingFactor factor) {
		println("Generating " + numberOfSamples + " samples from X = 'A string value'");
		for (int i = 0; i != numberOfSamples; i++) {
			Sample sample = new DefaultSample(importanceFactory, potentialFactory);
			sample.getAssignment().set(x, "A string value");
			factor.sampleOrWeigh(sample);
			assertEquals(1.0, sample.getPotential().doubleValue(), 0.0);
			assertEquals("A string value", sample.getAssignment().get(x));
			assertEquals("A string value", sample.getAssignment().get(y));
		}
		
		println("Generating " + numberOfSamples + " samples from Y = 'A string value'");
		for (int i = 0; i != numberOfSamples; i++) {
			Sample sample = new DefaultSample(importanceFactory, potentialFactory);
			sample.getAssignment().set(y, "A string value");
			factor.sampleOrWeigh(sample);
			assertEquals(1.0, sample.getPotential().doubleValue(), 0.0);
			assertEquals("A string value", sample.getAssignment().get(x));
			assertEquals("A string value", sample.getAssignment().get(y));
		}
		
		println("Generating " + numberOfSamples + " samples from X = 'A string value' and Y = 'A string value'");
		for (int i = 0; i != numberOfSamples; i++) {
			Sample sample = new DefaultSample(importanceFactory, potentialFactory);
			sample.getAssignment().set(x, "A string value");
			sample.getAssignment().set(y, "A string value");
			factor.sampleOrWeigh(sample);
			assertEquals(1.0, sample.getPotential().doubleValue(), 0.0);
			assertEquals("A string value", sample.getAssignment().get(x));
			assertEquals("A string value", sample.getAssignment().get(y));
		}
		
		println("Generating " + numberOfSamples + " samples from X = 'A string value' and Y = 'Another string value'");
		for (int i = 0; i != numberOfSamples; i++) {
			Sample sample = new DefaultSample(importanceFactory, potentialFactory);
			sample.getAssignment().set(x, "A string value");
			sample.getAssignment().set(y, "Another string value");
			factor.sampleOrWeigh(sample);
			assertEquals(0.0, sample.getPotential().doubleValue(), 0.0);
			assertEquals("A string value", sample.getAssignment().get(x));
			assertEquals("Another string value", sample.getAssignment().get(y));
		}
		
		println("Generating " + numberOfSamples + " samples from empty sample");
		for (int i = 0; i != numberOfSamples; i++) {
			Sample sample = new DefaultSample(importanceFactory, potentialFactory);
			factor.sampleOrWeigh(sample);
			assertEquals(1.0, sample.getPotential().doubleValue(), 0.0);
			assertEquals(null, sample.getAssignment().get(x));
			assertEquals(null, sample.getAssignment().get(y));
		}
	}
}
