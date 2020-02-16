package com.sri.ai.praise.core.representation.interfacebased.factor.core.table.core.bydatastructure.empty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.sri.ai.praise.core.representation.interfacebased.factor.core.table.api.TableFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.table.core.base.AbstractTableFactor;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.table.core.base.TableVariable;
import com.sri.ai.util.Enclosing;

/**
 * A {@link TableFactor} implementation that does not hold any data,
 * but instead holds only its variable's information, being useful for predicting the summing costs of the results of
 * real {@link TableFactor} operations.
 * 
 * @author braz
 *
 */
public class EmptyTableFactor extends AbstractTableFactor {
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS /////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public EmptyTableFactor(String factorName, Collection<? extends TableVariable> variables) {
		super(factorName, variables);
	}
	
	public EmptyTableFactor(Collection<? extends TableVariable> variables) {
		this("empty phi", variables);
	}
	
	public EmptyTableFactor(TableFactor tableFactor) {
		this(tableFactor.getVariables());
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// BASIC METHODS ////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean isIdentity() {
		if (numberOfEntries() == 1) {
			return true;
		}
		else {
			throw new Error(getClass().getSimpleName() + " cannot decide if an empty factor is the identity factor unless it has a single entry (zero variables).");
		}
	}
	
	@Override
	public boolean isZero() {
		indicateLackOfData((new Enclosing(){}).methodName());
		return false;
	}

	@Override
	protected boolean firstParameterIsZero() {
		indicateLackOfData((new Enclosing(){}).methodName());
		return false;
	}

	@Override
	protected boolean thereAreZeroParameters() {
		return numberOfEntries() == 0;
	}
	
	@Override
	protected boolean parametersAreAllEqual() {
		indicateLackOfData((new Enclosing(){}).methodName());
		return false;
	}

	@Override
	protected String parametersString() {
		return "(no data - empty table factor)";
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// SLICING //////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public EmptyTableFactor slice(List<TableVariable> variables, List<Integer> values) {
		return (EmptyTableFactor) super.slice(variables, values);
	}

	@Override
	public EmptyTableFactor slice(Map<TableVariable, Integer> assignment) {
		return (EmptyTableFactor) super.slice(assignment);
	}

	@Override
	protected EmptyTableFactor slicePossiblyModifyingAssignment(Map<TableVariable, Integer> assignment, ArrayList<? extends TableVariable> remainingVariables) {
		return new EmptyTableFactor(remainingVariables);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// ADDITION /////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected EmptyTableFactor addTableFactor(TableFactor another) {
		return initializeNewFactorUnioningVariables(another);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// MULTIPLICATION ///////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected EmptyTableFactor multiplyTableFactor(TableFactor another) {
		return initializeNewFactorUnioningVariables(another);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// SHARED SUPPORT FOR ADDING AND MULTIPLYING ////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private EmptyTableFactor initializeNewFactorUnioningVariables(TableFactor another) {
		LinkedHashSet<TableVariable> newSetOfVariables = new LinkedHashSet<>(this.variables);
		newSetOfVariables.addAll(another.getVariables());
		return new EmptyTableFactor(new ArrayList<>(newSetOfVariables));
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// NORMALIZATION ////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected double computeNormalizationConstant() {
		indicateLackOfData((new Enclosing(){}).methodName());
		return 0.0;
	}
	
	@Override
	public EmptyTableFactor normalize() {
		return this;
	}

	@Override
	protected EmptyTableFactor normalizeBy(Double normalizationConstant) {
		indicateLackOfData((new Enclosing(){}).methodName());
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// SUMMING OUT ///////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected EmptyTableFactor sumOut(List<? extends TableVariable> eliminated, ArrayList<? extends TableVariable> remaining) {
		return new EmptyTableFactor(remaining);
	}
	

	@Override
	protected TableFactor max(List<? extends TableVariable> eliminated, ArrayList<? extends TableVariable> remaining) {
		return new EmptyTableFactor(remaining);
	}

	@Override
	protected TableFactor min(List<? extends TableVariable> eliminated, ArrayList<? extends TableVariable> remaining) {
		return new EmptyTableFactor(remaining);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// INVERSION ////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public EmptyTableFactor invert() {
		return this;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// INDEXED ACCESS - GETTERS /////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected Double getEntryFor(int[] values) {
		indicateLackOfData((new Enclosing(){}).methodName());
		return null;
	}

	@Override
	public Double getEntryFor(ArrayList<Integer> values) {
		indicateLackOfData((new Enclosing(){}).methodName());
		return null;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// GET ENTRIES //////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public ArrayList<Double> getEntries() {
		indicateLackOfData((new Enclosing(){}).methodName());
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// INDEXED ACCESS - SETTERS /////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void setEntryFor(int[] values, Double newParameterValue) {
		indicateLackOfData((new Enclosing(){}).methodName());
	}
	
	@Override
	public void setEntryFor(ArrayList<Integer> values, Double newParameterValue) {
		indicateLackOfData((new Enclosing(){}).methodName());
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// LACK OF DATA INDICATION //////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void indicateLackOfData(String methodName) {
		throw new Error(getClass().getSimpleName() + " cannot answer queries about data since it does not hold any, but had its method " + methodName + " invoked.");
	}
}
