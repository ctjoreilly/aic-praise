package com.sri.ai.praise.core.representation.translation.rodrigoframework.samplinggraph2d;

import static com.sri.ai.util.Util.arrayList;
import static com.sri.ai.util.Util.getFirstSatisfyingPredicateOrNull;
import static com.sri.ai.util.Util.mapIntoArrayList;
import static com.sri.ai.util.Util.myAssert;

import java.util.ArrayList;
import java.util.List;

import com.sri.ai.util.base.Pair;
import com.sri.ai.util.distribution.WeightedFrequencyArrayDistributionOfMainVariableGivenRemainingVariables;
import com.sri.ai.util.function.api.values.Value;
import com.sri.ai.util.function.api.variables.Assignment;
import com.sri.ai.util.function.api.variables.SetOfVariables;
import com.sri.ai.util.function.api.variables.Unit;
import com.sri.ai.util.function.api.variables.Variable;
import com.sri.ai.util.function.core.functions.AbstractFunction;
import com.sri.ai.util.function.core.values.DefaultValue;
import com.sri.ai.util.function.core.variables.RealVariable;

public abstract class AbstractDiscretizedProbabilityDistributionFunction extends AbstractFunction {

	protected abstract Pair<ArrayList<Object>, Double> getValuesAndWeight();

	protected Variable queryVariable;
	protected int queryVariableIndex;
	protected WeightedFrequencyArrayDistributionOfMainVariableGivenRemainingVariables indexDistribution;

	protected AbstractDiscretizedProbabilityDistributionFunction(SetOfVariables inputVariablesWithRange, int queryVariableIndex) {
		
		super(makeOutputVariable(inputVariablesWithRange.get(queryVariableIndex)), inputVariablesWithRange);

		assertVariablesAllHaveADefinedSetOfValues(inputVariablesWithRange);
		
		this.queryVariable = getInputVariables().getVariables().get(queryVariableIndex);
		this.queryVariableIndex = queryVariableIndex;

		int numberOfQueryValueIndices = queryVariable.getSetOfValuesOrNull().size() + 1;
		this.indexDistribution = new WeightedFrequencyArrayDistributionOfMainVariableGivenRemainingVariables(numberOfQueryValueIndices);

	}
	
	public static RealVariable makeOutputVariable(Variable queryVariable) {
		return new RealVariable("P(" + queryVariable.getName() + " | ...)", Unit.NONE);
	}
	
	
	@Override
	public Value evaluate(Assignment assignmentToInputVariables) {
		ArrayList<Object> valueObjects = getValues(assignmentToInputVariables);
		return evaluate(valueObjects);
	}

	private ArrayList<Object> getValues(Assignment assignment) {
		ArrayList<Object> valueObjects = mapIntoArrayList(getVariables(), v -> assignment.get(v).objectValue());
		return valueObjects;
	}
	
	protected List<? extends Variable> getVariables() {
		return getInputVariables().getVariables();
	}

	/////////////////////////////////

	@Override
	public String getName() {
		return "P(" + queryVariable.getName() + " | ...)";
	}

	public void iterate() {
		Pair<ArrayList<Object>, Double> valuesAndWeight = getValuesAndWeight();
		Pair<Integer, ArrayList<Integer>> valueIndices = getValueIndices(valuesAndWeight.first);
		Integer queryValueIndex = valueIndices.first;
		ArrayList<Integer> second = valueIndices.second;
		if (queryValueIndex != -1) { // query value is in range
			indexDistribution.register(queryValueIndex, second, valuesAndWeight.second);
		}
	}

	public Value evaluate(ArrayList<Object> valueObjects) {
		Pair<Integer, ArrayList<Integer>> valueIndices = getValueIndices(valueObjects);
		int queryValueIndex = valueIndices.first;
		ArrayList<Integer> nonQueryValueIndices = valueIndices.second;
		double probability = indexDistribution.getProbability(queryValueIndex, nonQueryValueIndices);
		return new DefaultValue(probability);
	}

	/**
	 * Returns the index of the value of the query, and the indices of values of remaining variables.
	 * If index of value of query is -1 (meaning the value is out of range), the indices of values of remaining variables are not calculated (array is empty).
	 * @param valueObjects
	 * @return
	 */
	private Pair<Integer, ArrayList<Integer>> getValueIndices(ArrayList<Object> valueObjects) {
		Object queryValueObject = getValueOfVariableAt(queryVariableIndex, valueObjects);
		int queryValueIndex = getIndexOfValue(queryVariable, queryValueObject);
		ArrayList<Integer> nonQueryValueIndices;
		if (queryValueIndex != -1) { // is in range
			nonQueryValueIndices = getNonQueryValueIndices(valueObjects);
		}
		else {
			nonQueryValueIndices = arrayList();
		}
		return new Pair<>(queryValueIndex, nonQueryValueIndices);
	}

	private Object getValueOfVariableAt(int variableIndex, ArrayList<Object> valueObjects) {
		Object valueObject = valueObjects.get(variableIndex);
		myAssert(valueObject != null, () -> "Value not available for " + getInputVariables().get(variableIndex));
		return valueObject;
	}

	private int getIndexOfValue(Variable variable, Object valueObject) {
		Value value = Value.value(valueObject);
		return getIndexOfValue(variable, value);
	}

	private int getIndexOfValue(Variable variable, Value value) {
		int result = variable.getSetOfValuesOrNull().getIndex(value);
		return result;
	}

	private ArrayList<Integer> getNonQueryValueIndices(ArrayList<Object> valueObjects) {
		ArrayList<Integer> result = arrayList(numberOfVariables() - 1);
		for (int i = 0; i != numberOfVariables(); i++) {
			if (i != queryVariableIndex) {
				Object valueObject = getValueOfVariableAt(i, valueObjects);
				int indexOfValueOfIthVariable = getIndexOfValue(getVariable(i), valueObject);
				result.add(indexOfValueOfIthVariable);
			}
		}
		return result;
	}

	protected int numberOfVariables() {
		return getInputVariables().size();
	}

	protected Variable getVariable(int i) {
		return getInputVariables().getVariables().get(i);
	}

	private void assertVariablesAllHaveADefinedSetOfValues(SetOfVariables inputVariablesWithRange) throws Error {
		List<? extends Variable> variables = inputVariablesWithRange.getVariables();
		Variable withoutSetOfValues = getFirstSatisfyingPredicateOrNull(variables, v -> v.getSetOfValuesOrNull() == null);
		if (withoutSetOfValues != null) {
			throw new Error(getClass() + " requires that all graph2d variables have a defined set of values, but " + withoutSetOfValues + " does not");
		}
	}

}