package mario;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

import ch.idsia.agents.controllers.modules.*;
import ch.idsia.benchmark.mario.engine.generalization.Entity;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import mario.GeneralAgent.MarioInputKey;

public class JsonMessageObject {

	private class SerializableEntity {

		int dTX, dTY;
		float dX, dY, height, speedX, speedY;
		String type;

		public SerializableEntity(Entity e) {
			dTX = e.dTX;
			dTY = e.dTY;
			dX = e.dX;
			dY = e.dY;
			height = e.height;
			speedX = e.speed.x;
			speedY = e.speed.y;
			type = e.type.toString();
			// TODO: Sprite
		}
	}

	private class SerializableMario {

		boolean carrying, mayJump, mayShoot, onGround;
		int dtX, dtY, egoCol, egoRow, inTileX, inTileY;
		int killsByFire, killsByShell, killsByStomp, killsTotal;
		float dX, dY, height, speedX, speedY;
		String mode;
		int receptiveFieldHeight, receptiveFieldWidth, status, timeLeft, timeSpent;
		int zLevelEntities, zLevelTiles;
		int[] state;

		public SerializableMario(MarioEntity m) {
			carrying = m.carrying;
			dtX = m.dTX;
			dtY = m.dTY;
			dX = m.dX;
			dY = m.dY;
			egoCol = m.egoCol;
			egoRow = m.egoRow;
			height = m.height;
			inTileX = m.inTileX;
			inTileY = m.inTileY;
			killsByFire = m.killsByFire;
			killsByShell = m.killsByShell;
			killsByStomp = m.killsByStomp;
			killsTotal = m.killsTotal;
			mayJump = m.mayJump;
			mayShoot = m.mayShoot;
			mode = m.mode.toString();
			onGround = m.onGround;
			receptiveFieldHeight = m.receptiveFieldHeight;
			receptiveFieldWidth = m.receptiveFieldWidth;
			speedX = m.speed.x;
			speedY = m.speed.y;
			state = m.state;
			status = m.status;
			timeLeft = m.timeLeft;
			timeSpent = m.timeSpent;
			zLevelEntities = m.zLevelEntities;
			zLevelTiles = m.zLevelTiles;
		}
	}

	// Properties that will be serialized to JSON
	SerializableMario mario = null;
	Tile[][] map = null;
	List<SerializableEntity> entities = new ArrayList<>();

	// Special "property" (must have this name)
	int[] possible_moves;

	// This property is not serialized via Gson
	private transient List<List<MarioInputKey>> subsets;

	/**
	 * Initializes a new instance of JsonMessageObject. Represents the current
	 * state of the game.
	 */
	public JsonMessageObject(MarioEntity m, Entities e, Tiles t) {
		mario = new SerializableMario(m);
		map = t.tileField;

		for (Entity en : e.entities) {
			if (en.type.equals(EntityType.NOTHING)) {
				continue;
			}
			entities.add(new SerializableEntity(en));
		}

		// Generate subsets of available actions.  AI agent will choose
		// one of those subsets as a result (which will be decoded back
		// to the action later)

		List<MarioInputKey> set = new ArrayList<>();

		// JUMP action can be done only if mario may jump (or is in air)
		if (mario.mayJump || !mario.onGround) {
			set.add(MarioInputKey.JUMP);
		}
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
		possible_moves = encodeMoves();
	}

	public int[] encodeMoves() {
		int[] result = new int[subsets.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		return result;
	}

	public List<MarioInputKey> decodeMove(int inputForMario) {
		return subsets.get(inputForMario);
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
