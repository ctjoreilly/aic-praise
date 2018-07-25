package com.sri.ai.test.praise.learning.symbolicparameterestimation;

import static com.sri.ai.expresso.helper.Expressions.parse;
import static com.sri.ai.praise.learning.symbolicparameterestimation.util.UsefulOperationsParameterEstimation.buildOptimizedExpressionBasedModel;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sri.ai.expresso.api.Expression;
import com.sri.ai.praise.core.representation.classbased.expressionbased.api.ExpressionBasedModel;
import com.sri.ai.praise.learning.symbolicparameterestimation.ParameterEstimationForExpressionBasedModel;
import com.sri.ai.praise.learning.symbolicparameterestimation.util.ExpressionBasedModelExamples;
import com.sri.ai.util.base.Pair;

/**
 * @author Sarah Perrin
 */
public class ParameterEstimationForExpressionBasedModelTest {
	
	@Test
	public void testExpressionBasedPairs() {
		
		ExpressionBasedModel expressionBasedModel = ExpressionBasedModelExamples.buildModel1();

		List<Pair<Expression, Expression>> pairsQueryEvidence = new LinkedList<Pair<Expression, Expression>>();
		
		Pair<Expression, Expression> pair = new Pair<Expression,Expression>(parse("earthquake"), parse("null"));

		Pair<Expression, Expression> pair2 = new Pair<Expression,Expression>(parse("not earthquake"), parse("null"));
		
		pairsQueryEvidence.add(pair);
		
		for(int i = 0; i < 999; i++) {
			pairsQueryEvidence.add(pair2);
		}
		
		HashMap<Expression,Double> expected = new HashMap<Expression,Double>();
		expected.put(parse("Alpha"), 9.999997011753915E-4);
		
		HashMap<Expression,Double> mapResult = runTestExpressionBased(pairsQueryEvidence,
				expressionBasedModel, new double[] {0});

		System.out.println("expected : " + expected);
		System.out.println("result : " + mapResult);
		assertEquals(expected, mapResult);
		
		pairsQueryEvidence.clear();
		
		pair = new Pair<Expression,Expression>(parse("earthquake"), parse("alarm"));

		pair2 = new Pair<Expression,Expression>(parse("not earthquake"), parse("not alarm"));
		
		pairsQueryEvidence.add(pair);
		
		for(int i = 0; i < 999; i++) {
			pairsQueryEvidence.add(pair2);
		}
		
		expected = new HashMap<Expression,Double>();
		expected.put(parse("Alpha"), 0.008214845805751847);
		expected.put(parse("Beta"), 8.527665270285399E-9);
		
		mapResult = runTestExpressionBased(pairsQueryEvidence,
				expressionBasedModel, new double[] {0,0});
		
		System.out.println("expected : " + expected);
		System.out.println("result : " + mapResult);
		assertEquals(expected, mapResult);
		
		pairsQueryEvidence.clear();
		
		pair = new Pair<Expression,Expression>(parse("earthquake"), parse("burglary and alarm"));

		pair2 = new Pair<Expression,Expression>(parse("not earthquake"), parse("not alarm"));
		
		pairsQueryEvidence.add(pair);
		
		pairsQueryEvidence.add(pair2);
		
		
		expected = new HashMap<Expression,Double>();
		expected.put(parse("Alpha"), 0.7550338919520323);
		expected.put(parse("Beta"), 7.003268978167072E-6);
		
		mapResult = runTestExpressionBased(pairsQueryEvidence,
				expressionBasedModel, new double[] {0,0});
		
		System.out.println("expected : " + expected);
		System.out.println("result : " + mapResult);
		assertEquals(expected, mapResult);
		
	}
	
	@Test
	public void testExpressionBased() {
		
		ExpressionBasedModel expressionBasedModel = ExpressionBasedModelExamples.buildModel1();
		
		List<Pair<Expression, Expression>> pairsQueryEvidence = new LinkedList<Pair<Expression, Expression>>();
		
		Pair<Expression, Expression> pair = new Pair<Expression,Expression>(parse("earthquake"), parse("null"));
		
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);
		
		Map<Expression, Double> expected = new HashMap<Expression,Double>();
		expected.put(parse("Alpha"), 1.0);

		Map<Expression, Double> mapResult = runTestExpressionBased(pairsQueryEvidence,
				expressionBasedModel, new double[] {0});

		System.out.println("expected : " + expected);
		System.out.println("result : " + mapResult);
		assertEquals(expected, mapResult);

		pairsQueryEvidence.clear();
		
		pair = new Pair<Expression,Expression>(parse("not earthquake"), parse("null"));
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);

		expected.put(parse("Alpha"), 1.4905019930035748E-22);

		mapResult = runTestExpressionBased(pairsQueryEvidence,
				expressionBasedModel, new double[] {0});

		System.out.println("expected : " + expected);
		System.out.println("result : " + mapResult);
		assertEquals(expected, mapResult);

		



		pairsQueryEvidence.clear();
		pair = new Pair<Expression,Expression>(parse("earthquake"), parse("null"));
		Pair<Expression,Expression> pair2 = new Pair<Expression,Expression>(parse("not earthquake"), parse("null"));
		
		pairsQueryEvidence.add(pair2);
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);
		pairsQueryEvidence.add(pair);

		expected.put(parse("Alpha"), 0.9000011823080569);

		mapResult = runTestExpressionBased(pairsQueryEvidence,
				 expressionBasedModel, new double[] {0});

		System.out.println("expected : " + expected);
		System.out.println("result : " + mapResult);
		assertEquals(expected, mapResult);

		pairsQueryEvidence.clear();
		
		pair = new Pair<Expression,Expression>(parse("burglary"), parse("null"));
		pairsQueryEvidence.add(pair2);
		pairsQueryEvidence.add(pair);

		expected.put(parse("Alpha"), 6.289892249011522E-23);
		expected.put(parse("Beta"), 1.0);

		mapResult = runTestExpressionBased(pairsQueryEvidence, 
				expressionBasedModel, new double[] {0,0});

		System.out.println("expected : " + expected);
		System.out.println("result : " + mapResult);
		assertEquals(expected, mapResult);
		
		//////////
		
		pairsQueryEvidence.clear();
		pair = new Pair<Expression,Expression>(parse("earthquake and not burglary"), parse("null"));
		
		pairsQueryEvidence.add(pair);

		expected.put(parse("Alpha"), 1.0);
		expected.put(parse("Beta"), 6.289892248981177E-23);

		mapResult = runTestExpressionBased(pairsQueryEvidence,
				 expressionBasedModel, new double[] {0,0});

		System.out.println("expected : " + expected);
		System.out.println("result : " + mapResult);
		assertEquals(expected, mapResult);

	}
	
	@Test
	
	public void testBuildOptimizedExpressionBasedModel() {
				
				ExpressionBasedModel expressionBasedModel = ExpressionBasedModelExamples.buildModel1();
				
				System.out.println("expressionBasedModel before optimization : " + expressionBasedModel);
				
				List<Pair<Expression, Expression>> pairsQueryEvidence = new LinkedList<Pair<Expression, Expression>>();
				
				Pair<Expression, Expression> pair = new Pair<Expression,Expression>(parse("earthquake"), parse("null"));

				pairsQueryEvidence.add(pair);
				
				ParameterEstimationForExpressionBasedModel parameterEstimationForExpressionBasedModel = new ParameterEstimationForExpressionBasedModel(expressionBasedModel, pairsQueryEvidence);
				HashMap<Expression,Double> result = parameterEstimationForExpressionBasedModel.optimize(
						new double[] {0});
				ExpressionBasedModel newModel = buildOptimizedExpressionBasedModel(result, expressionBasedModel);
				
				System.out.println(newModel);
				
				List<Pair<Expression, Expression>> pairsQueryEvidence2 = new LinkedList<Pair<Expression, Expression>>();
				Pair<Expression, Expression> pair2 = new Pair<Expression,Expression>(parse("burglary"), parse("null"));
				pairsQueryEvidence2.add(pair2);
				ParameterEstimationForExpressionBasedModel parameterEstimationForExpressionBasedModel2 = new ParameterEstimationForExpressionBasedModel(newModel, pairsQueryEvidence2);
				HashMap<Expression,Double> result2 = parameterEstimationForExpressionBasedModel2.optimize(
						new double[] {0});
				ExpressionBasedModel newModel2 = buildOptimizedExpressionBasedModel(result2, newModel);
				System.out.println(newModel2);
		
	}
	
	private HashMap<Expression,Double> runTestExpressionBased(List<Pair<Expression, Expression>> pairsQueryEvidence, ExpressionBasedModel expressionBasedModel, double[] startPoint) {

		ParameterEstimationForExpressionBasedModel parameterEstimationForExpressionBasedModel = new ParameterEstimationForExpressionBasedModel(expressionBasedModel, pairsQueryEvidence);
		HashMap<Expression,Double> result = parameterEstimationForExpressionBasedModel.optimize(
				startPoint);
		ExpressionBasedModel newModel = buildOptimizedExpressionBasedModel(result, expressionBasedModel);
		System.out.println(" New Model : " + newModel);
		
		return result;

	}

}
