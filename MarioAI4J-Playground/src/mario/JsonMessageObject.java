package mario;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

import ch.idsia.agents.controllers.modules.*;
import ch.idsia.benchmark.mario.engine.generalization.Entity;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.sprites.Mario.MarioMode;
import mario.GeneralAgent.MarioInputKey;

public class JsonMessageObject {

	// Properties that will be serialized to JSON
	int current_phase = 0;
	Double[] state;
	
	double[] score = new double[1];
	double reward = 0.0;
	int done = 0;
	
	// This property is not serialized via Gson
	private transient List<MarioInputKey> marioMoves;
	private transient final int tileTypeCount = Tile.values().length;
	private transient final int totalMoves = 5;
	
	// Stores all "used" entites / tiles types
	private transient Object[] items = new Object[] { 
			Tile.NOTHING,
			Tile.BRICK,
			Tile.BORDER_HILL,
			Tile.BORDER_CANNOT_PASS_THROUGH,
			Tile.FLOWER_POT,
			Tile.FLOWER_POT_OR_CANNON,
			Tile.PRINCESS,
			
			EntityType.DANGER,
			EntityType.GOOMBA,
			EntityType.GOOMBA_WINGED,
			EntityType.WAVE_GOOMBA,
			
			EntityType.ENEMY_FLOWER,
			EntityType.FIRE_FLOWER,
			EntityType.GREEN_KOOPA,
			EntityType.GREEN_KOOPA_WINGED,
			
			EntityType.MUSHROOM,
			EntityType.SHELL_MOVING,
			EntityType.SHELL_STILL,
			
			EntityType.SPIKES,
			EntityType.SPIKY,
			EntityType.SPIKY_WINGED			
	};

	/**
	 * Initializes a new instance of JsonMessageObject. Represents the current
	 * state of the game.
	 */
	public JsonMessageObject(MarioEntity m, Entities e, Tiles t, double reward, float score, boolean done) {
		state = encodeState(m, e, t);
		
		this.reward = reward;
		this.score[0] = score;
		this.done = done ? 1 : 0;
		
		marioMoves = new ArrayList<>();
		marioMoves.add(MarioInputKey.JUMP);
		marioMoves.add(MarioInputKey.RUN_LEFT);
		marioMoves.add(MarioInputKey.RUN_RIGHT);
		marioMoves.add(MarioInputKey.SHOOT);
		marioMoves.add(MarioInputKey.SPRINT);
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
		state.add((double)m.receptiveFieldHeight);
		state.add((double)m.receptiveFieldWidth);
		state.add((double)m.status);
		state.add((double)m.zLevelEntities);
		state.add((double)m.zLevelTiles);
		
		// Float values
		state.add((double)m.dX);
		state.add((double)m.dY);
		state.add((double)m.height);
		state.add((double)m.speed.x);
		state.add((double)m.speed.y);
		
		// RESCALE DATA
		for (int i = 4; i <= 19; i++) {
			if (state.get(i) > 0) {
				state.set(i, Math.log(state.get(i)));
			}
		}
		
		// String / Enum
		state.add(booleanToDouble(m.mode == MarioMode.FIRE_LARGE));
		state.add(booleanToDouble(m.mode == MarioMode.LARGE));
		state.add(booleanToDouble(m.mode == MarioMode.SMALL));
		
		String test = "";
		
		// ENVIRONMENT VALUES
		// Loop over visible field (19 x 19) and store tiles and proper entities:
		for (int i = 0; i < t.tileField.length; i++) {
			for (int j = 0; j < t.tileField[0].length; j++) {
				double value = getRepresentation(t.tileField[i][j], e.entityField[i][j]);
				state.add(value);
			}
		}
			
		Double[] result = new Double[state.size()];
		state.toArray(result);
		// System.err.println("STATE SIZE: " + state.size()); // Debug purposes
		return result;
	}
	
	/**
	 * Evaluates a numeric representation for specific position. Tile t and entities ens are on that position.
	 * @param t Tile at the specific position.
	 * @param ens Entities on the specific position.
	 * @return Numeric representation for specific position.
	 */
	private double getRepresentation(Tile t, List<Entity> ens) {
		Entity result = null;
		if (ens.size() > 0) {
			// SEARCH FOR THE MOST DANGEROUS ONE
			result = ens.get(0);
			for (Entity entity : ens) {
				if (result.type.getKind().getThreatLevel() > entity.type.getKind().getThreatLevel()) result = entity;
			}
			
			for (int i = 0; i < items.length; i++) {
				if (items[i].equals(result.type)) {
					return i;
				}
				
			}
		}
		
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(t)) {
				return i;
			}
		}
		
		if (result != null) {
			if (result.type.equals(EntityType.FIREBALL) || result.type.equals(EntityType.NOTHING)) {
				return 0;
			}
		}
		return -1;
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
	 * @return MarioInputKey - key that AI wants to press.
	 */
	public MarioInputKey decodeMove(String outputFromAI) {
		String[] parts = outputFromAI.split(" ");
		
		if (parts.length != totalMoves) {
			throw new RuntimeException("Wrong number of results from AI.\n " + outputFromAI);
		}
		
		int maxIndex = -1;
		double maxValue = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < parts.length; i++) {
			double value = Double.parseDouble(parts[i]);
			if (value > maxValue) {
				maxValue = value;
				maxIndex = i;
			}
		}
		return marioMoves.get(maxIndex);
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
