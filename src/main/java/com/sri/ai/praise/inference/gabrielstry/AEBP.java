package com.sri.ai.praise.inference.gabrielstry;

import com.google.common.base.Function;
import com.sri.ai.praise.inference.anytimeexactbp.polytope.api.Polytope;
import com.sri.ai.praise.inference.gabrielstry.aebpmodel.AEBPModel;
import com.sri.ai.praise.inference.gabrielstry.aebpmodel.aebpmodeliterator.BFS;
import com.sri.ai.praise.inference.gabrielstry.aebpmodel.aebpmodeliterator.api.AEBPTreeIterator;
import com.sri.ai.praise.inference.gabrielstry.aebptree.AEBPFactorTreeNode;
import com.sri.ai.praise.inference.gabrielstry.aebptree.AEBPQueryTreeNode;
import com.sri.ai.praise.inference.gabrielstry.representation.api.EditableFactorNetwork;
import com.sri.ai.praise.inference.representation.api.Variable;
import com.sri.ai.util.collect.EZIterator;

public class AEBP extends EZIterator<Polytope> {
	AEBPModel model;
	AEBPQueryTreeNode tree;//Tree built from the query
	
	AEBPTreeIterator getNextNodeToPutOnTheTree;
	
	public AEBP(EditableFactorNetwork network, 
			Variable query,
			Function<AEBPModel,AEBPTreeIterator> getNextNodeToPutOnTheTree) {
		this.model = new AEBPModel(network, query);
				
		//Function<Variable,Boolean> isExhausted = (v) -> this.model.isExhausted(v);
		
		this.getNextNodeToPutOnTheTree = getNextNodeToPutOnTheTree.apply(model);

		tree = this.getNextNodeToPutOnTheTree.getRootOfTree();//new AEBPQueryTreeNode(query, null, isExhausted);
	}
	
	public AEBP(EditableFactorNetwork network, 
			Variable query) {
		this(network, query, model -> new BFS(model));
	}

	@Override
	protected Polytope calculateNext() {
		expand();
		Polytope result = computeInference();
		return result;
	}

	private Polytope computeInference() {
		return tree.messageSent();
	}

	private void expand() {
		//Get next factor (and it's children variables)
		AEBPFactorTreeNode nextTreeNodeToAddToTheTree = getNextNodeToPutOnTheTree.next();
		//Add new factor to model
		model.ExpandModel(nextTreeNodeToAddToTheTree.getRoot());
		//Add new factor to the tree
		tree.addNodeToTheTree(nextTreeNodeToAddToTheTree);
	}
}
