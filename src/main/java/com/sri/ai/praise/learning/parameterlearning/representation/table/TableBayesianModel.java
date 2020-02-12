package com.sri.ai.praise.learning.parameterlearning.representation.table;

import static com.sri.ai.util.Util.list;

import java.util.List;

import com.sri.ai.praise.core.representation.interfacebased.factor.core.table.core.base.TableFactorNetwork;
import com.sri.ai.praise.learning.parameterlearning.BayesianModel;

public class TableBayesianModel extends TableFactorNetwork implements BayesianModel {
	
	private List<TableBayesianNode> nodes;
	
	public TableBayesianModel(List<TableBayesianNode> nodes) {
		super(nodes);
		this.nodes = nodes;
	}
	
	@Override
	public List<? extends TableBayesianNode> getNodes() {
		return nodes;
	}
	
	@Override
	public TableBayesianModel copy() {
		List<TableBayesianNode> copiedNodes = list();
		for(TableBayesianNode node : this.nodes) {
			copiedNodes.add(node.copy());
		}
		TableBayesianModel copy = new TableBayesianModel(copiedNodes);
		return copy;
	}
	
}
