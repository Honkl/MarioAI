package mario;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

import ch.idsia.agents.controllers.modules.*;
import ch.idsia.benchmark.mario.engine.generalization.Entity;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;
import ch.idsia.benchmark.mario.engine.generalization.Tile;

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
		float dX, dY, height,speedX, speedY;
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
	int[] possible_moves = new int[2]; 
	
	/**
	 * Initializes a new instance of JsonMessageObject. Represents
	 * the current state of the game.
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
		
		possible_moves[0] = 1;
		possible_moves[1] = 2;
	}
	
	
	/**
	 * Converts the current object to JSON representation (using Gson).
	 * @return the current object converted to JSON (as a String).
	 */
	public String convertToJson() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
	}
}





