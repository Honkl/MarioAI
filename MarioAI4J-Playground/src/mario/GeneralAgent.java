package mario;

import java.awt.Graphics;
import java.io.*;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.VisualizationComponent;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioControl;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.environments.IEnvironment;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.tools.EvaluationInfo;

/**
 * Code your custom agent here!
 * 
 * Change {@link #actionSelection()} implementation to alter the behavior of
 * your Mario.
 * 
 * Change
 * {@link #debugDraw(VisualizationComponent, LevelScene, IEnvironment, Graphics)}
 * to draw custom debug stuff.
 * 
 * You can change the type of level you want to play in {@link #main(String[])}.
 * 
 * Once you have your agent ready, you may use {@link Evaluate} class to
 * benchmark the quality of your AI.
 */
public class GeneralAgent extends MarioHijackAIBase implements IAgent {

	private static BufferedWriter writer;
	private static BufferedReader reader;

	@Override
	public void reset(AgentOptions options) {
		super.reset(options);
	}

	@Override
	public void debugDraw(VisualizationComponent vis, LevelScene level, IEnvironment env, Graphics g) {
		super.debugDraw(vis, level, env, g);
		if (mario == null)
			return;

		// provide custom visualization using 'g'

		// EXAMPLE DEBUG VISUALIZATION
		String debug = "MY DEBUG STRING";
		VisualizationComponent.drawStringDropShadow(g, debug, 0, 26, 1);
	}

	/**
	 * Use {@link #e} member to query entities (Goombas, Spikies, Koopas, etc.) around Mario; see {@link EntityType} for complete list of entities.
	 * Important methods you will definitely need: {@link Entities#danger(int, int)} and {@link Entities#entityType(int, int)}.
	 * 
	 * Use {@link #t} member to query tiles (Bricks, Flower pots, etc.} around Mario; see {@link Tile} for complete list of tiles.
	 * Important method you will definitely need: {@link Tiles#brick(int, int)}.
	 * 
	 * Use {@link #control} to output actions (technically this method must return {@link #action} in order for {@link #control} to work.
	 * Note that all actions in {@link #control} runs in "parallel" (except {@link MarioControl#runLeft()} and {@link MarioControl#runRight()}, which cancels each other out in consecutive calls).
	 * Note that you have to call {@link #control} methods every {@link #actionSelectionAI()} tick (otherwise {@link #control} will think you DO NOT want to perform that action}. 
	 */
	@Override
	public MarioInput actionSelectionAI() {
		try {
			JsonMessageObject jmo = new JsonMessageObject(mario, e, t);
			String json = jmo.convertToJson() + "\n";
			//System.out.println(json);
			writer.write(json);
			writer.flush();
			
			String output = reader.readLine();
			
			for (MarioInputKey key : jmo.decodeMove(output)) {
				switch (key) {
				case RUN_LEFT:
					control.runLeft();
					break;
				case RUN_RIGHT:
					control.runRight();
					break;
				case JUMP:
					control.jump();
					break;
				case SHOOT:
					control.shoot();
					break;
				case SPRINT:
					control.sprint();
					break;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return action;
	}

	public enum MarioInputKey {
		RUN_RIGHT, RUN_LEFT, JUMP, SPRINT, SHOOT
	}

	public static void main(String[] args) throws IOException {
		// UNCOMMENT THE LINE OF THE LEVEL YOU WISH TO RUN

		// LevelConfig level = LevelConfig.LEVEL_0_FLAT;
		// LevelConfig level = LevelConfig.LEVEL_1_JUMPING;
		//LevelConfig level = LevelConfig.LEVEL_2_GOOMBAS;
		// LevelConfig level = LevelConfig.LEVEL_3_TUBES;
		LevelConfig level = LevelConfig.LEVEL_4_SPIKIES;

		// CREATE SIMULATOR
		// MarioSimulator simulator = new MarioSimulator(level.getOptions());
		MarioSimulator simulator = new MarioSimulator(level.getOptionsVisualizationOff() + FastOpts.L_RANDOMIZE);
	    // MarioSimulator simulator = new MarioSimulator(level.getOptionsRandomized());

		// CREATE SIMULATOR AND RANDOMIZE LEVEL GENERATION
		// -- if you wish to use this, comment out the line above and uncomment
		// line below
		// MarioSimulator simulator = new
		// MarioSimulator(level.getOptionsRandomized());

		// START PYTHON PROCESS
		
		String pythonScriptPath = args[0];
		String pythonExePath = args[1];
		String modelConfigFile = args[2];
		
		
		//Config file for AI (relative path to master "general-ai/Game-interfaces" directory
		String gameConfigFile = "Game-interfaces\\Mario\\Mario_config.json"; 
		
		String[] params = new String[] {pythonExePath, pythonScriptPath, gameConfigFile, modelConfigFile};
        ProcessBuilder pb = new ProcessBuilder(params);
		pb.redirectErrorStream(true);
		Process p = pb.start();

		writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		// RUN THE SIMULATION
		IAgent agent = new GeneralAgent();
		EvaluationInfo info = simulator.run(agent);

		writer.write("END");

		// CHECK RESULT
		switch (info.getResult()) {
		case LEVEL_TIMEDOUT:
			System.out.println("LEVEL TIMED OUT!");
			break;

		case MARIO_DIED:
			System.out.println("MARIO KILLED");
			break;

		case SIMULATION_RUNNING:
			System.out.println("SIMULATION STILL RUNNING?");
			throw new RuntimeException("Invalid evaluation info state, simulation should not be running.");

		case VICTORY:
			System.out.println("VICTORY!!!");
			break;
		}
	}

}