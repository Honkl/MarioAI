package mario;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

import ch.idsia.agents.controllers.modules.*;
import ch.idsia.benchmark.mario.engine.generalization.Entity;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import jdk.management.resource.internal.inst.SocketOutputStreamRMHooks;
import mario.GeneralAgent.MarioInputKey;

public class JsonMessageObject {

	// Properties that will be serialized to JSON
	int current_phase = 0;
	Double[] state;
	
	// This property is not serialized via Gson
	private transient List<List<MarioInputKey>> subsets;

	/**
	 * Initializes a new instance of JsonMessageObject. Represents the current
	 * state of the game.
	 */
	public JsonMessageObject(MarioEntity m, Entities e, Tiles t) {
		state = encodeState(m, e, t);
				
		// Generate subsets of available actions.  AI agent will choose
		// one of those subsets as a result (which will be decoded back
		// to the action later)

		List<MarioInputKey> set = new ArrayList<>();
		set.add(MarioInputKey.JUMP);
		set.add(MarioInputKey.RUN_LEFT);
		set.add(MarioInputKey.RUN_RIGHT);
		set.add(MarioInputKey.SHOOT);
		set.add(MarioInputKey.SPRINT);

		// Generate all subsets of specified set. Then remove
		// unnecessary subsets (empty set or sets which contain
		// both RUN_LEFT and RUN_RIGHT)
		subsets = getSubsets(set);
		List<List<MarioInputKey>> toBeRemoved = new ArrayList<>();
		for (List<MarioInputKey> subset : subsets) {
			boolean hasLeft = false;
			boolean hasRight = false;
			for (int i = 0; i < subset.size(); i++) {
				if (subset.get(i).equals(MarioInputKey.RUN_LEFT)) {
					hasLeft = true;
				}
				if (subset.get(i).equals(MarioInputKey.RUN_RIGHT)) {
					hasRight = true;
				}
			}
			if (hasLeft && hasRight) {
				toBeRemoved.add(subset);
			}
		}
		subsets.removeAll(toBeRemoved);
	}

	private Double[] encodeState(MarioEntity m, Entities e, Tiles t) {

		List<Double> state = new ArrayList<>();

		// MARIO VALUES 
		// Boolean values
		state.add(booleanToDouble(m.carrying));
		state.add(booleanToDouble(m.mayJump));
		state.add(booleanToDouble(m.mayShoot));
		state.add(booleanToDouble(m.onGround));
		
		// Int values
		state.add((double)m.dTX);
		state.add((double)m.dTY);
		state.add((double)m.egoCol);
		state.add((double)m.egoRow);
		state.add((double)m.inTileX);
		state.add((double)m.inTileY);
		state.add((double)m.killsByFire);
		state.add((double)m.killsByShell);
		state.add((double)m.killsByStomp);
		state.add((double)m.killsTotal);
		state.add((double)m.receptiveFieldHeight);
		state.add((double)m.receptiveFieldWidth);
		state.add((double)m.status);
		state.add((double)m.timeLeft);
		state.add((double)m.timeSpent);
		state.add((double)m.zLevelEntities);
		state.add((double)m.zLevelTiles);
		
		// Float values
		state.add((double)m.dX);
		state.add((double)m.height);
		state.add((double)m.speed.x);
		state.add((double)m.speed.y);

		// String / Enum
		state.add((double)m.mode.ordinal());
		
		// ENVIROMENT VALUES
		for (int i = 0; i < t.tileField.length; i++) {
			for (int j = 0; j < t.tileField[0].length; j++) {
				state.add((double)t.tileField[i][j].ordinal());
			}
		}
		
		for (int i = 0; i < e.entityField.length; i++) {
			for (int j = 0; j < e.entityField[0].length; j++) {
				List<Entity> ents = e.entityField[i][j];
				for (double value : getWorstEnemyProperties(ents)) {
					state.add(value);
				}
			}
		}
		
		Double[] result = new Double[state.size()];
		state.toArray(result);
		return result;
	}
	
	private double[] getWorstEnemyProperties(List<Entity> ents) {
		int numOfProperties = 8;
		if (ents.isEmpty()) {
			return new double[numOfProperties];
		}
		
		Entity worst = null;
		for (Entity en : ents) {
			if (en.type.equals(EntityType.SPIKES)) {
				worst = en;
				break;
			} else if (en.type.equals(EntityType.DANGER)) {
				worst = en;
			}
			if (en.type.equals(EntityType.NOTHING) && worst != null) {
				continue;
			}
			worst = en;
		}
		
		return new double[] { 
				worst.dTX,
				worst.dTY,
				worst.dX,
				worst.dY,
				worst.height,
				worst.speed.x,
				worst.speed.y,
				worst.type.ordinal()
			};
	}
	
	/**
	 * Converts the specified boolean value to double.
	 * @param value Value to be converted.
	 * @return 1 or 0.
	 */
	private double booleanToDouble(boolean value) {
		return value ? 1 : 0;
	}
	
	/**
	 * Decodes a result sent from the AI.
	 * @param outputFromAI AI result.
	 * @return List of MarioInputKey - keys that AI wants to press.
	 */
	public List<MarioInputKey> decodeMove(String outputFromAI) {
		String[] parts = outputFromAI.split(" ");
		
		if (parts.length != subsets.size()) {
			throw new RuntimeException("Wrong number of results from AI.\n " + outputFromAI);
		}
		
		int maxIndex = -1;
		double maxValue = Double.MIN_VALUE;
		for (int i = 0; i < subsets.size(); i++) {
			double value = Double.parseDouble(parts[i]);
			if (value > maxValue) {
				maxValue = value;
				maxIndex = i;
			}
		}
		return subsets.get(maxIndex);
	}

	/**
	 * Generates all subsets of the specified set.
	 * 
	 * @return List of lists of all subsets.
	 */
	private List<List<MarioInputKey>> getSubsets(List<MarioInputKey> set) {
		int length = set.size();
		int count = (int) Math.pow(2, set.size());
		List<List<MarioInputKey>> subsets = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			List<MarioInputKey> subset = new ArrayList<>();
			int position = 0;
			while (position < length) {
				if ((i & (1 << position)) > 0) {
					subset.add(set.get(position));
				}
				position++;
			}
			subsets.add(subset);
		}
		return subsets;
	}

	/**
	 * Converts the current object to JSON representation (using Gson).
	 * 
	 * @return the current object converted to JSON (as a String).
	 */
	public String convertToJson() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
	}
}
