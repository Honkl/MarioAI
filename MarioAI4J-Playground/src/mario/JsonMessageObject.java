package mario;

import com.google.gson.*;

import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;

public class JsonMessageObject {
	
	boolean carrying, mayJump, mayShoot;
	int dtX, dtY, egoCol, egoRow, inTileX, inTileY;
	int killsByFire, killsByShell, killsByStomp, killsTotal;
	
	float dX, dY, height;
	
	// Special "property" (must have this name)
	int[] PossibleMoves = new int[2]; 
	
	/**
	 * Initializes a new instance of JsonMessageObject. Represents
	 * the current state of the game.
	 */
	public JsonMessageObject(MarioEntity m, Entities e, Tiles t) {
		// Mario properties: 
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

		// TODO: mario, entities, tiles
		
		PossibleMoves[0] = 1;
		PossibleMoves[1] = 2;
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





