package mario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;
import ch.idsia.agents.IAgent;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.tools.EvaluationInfo;

public class VisualizationTool {
	public static void main(String[] args) throws IOException {

		// Start python process
		String pythonScriptPath = args[0];
		String pythonExePath = args[1];
		String modelConfigFile = args[2];
		int gameBatchSize = Integer.parseInt(args[3]);
		
		//Config file for AI (relative path to master "general-ai/Game-interfaces" directory
		String gameConfigFile = "Game-interfaces\\Mario\\Mario_config.json"; 
		
		String[] params = new String[] {pythonExePath, pythonScriptPath, gameConfigFile, modelConfigFile};
        ProcessBuilder pb = new ProcessBuilder(params);
		pb.redirectErrorStream(true);
		Process p = pb.start();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		Random rng = new Random(42);

		// LevelConfig level = LevelConfig.LEVEL_0_FLAT;
		// LevelConfig level = LevelConfig.LEVEL_1_JUMPING;
		LevelConfig level = LevelConfig.LEVEL_2_GOOMBAS;
		// LevelConfig level = LevelConfig.LEVEL_3_TUBES;
		// LevelConfig level = LevelConfig.LEVEL_4_SPIKIES;
		// MarioSimulator simulator = new MarioSimulator(level.getOptions() + FastOpts.L_RANDOM_SEED(seed));

		// RUN THE SIMULATION
		int iters = 200;
		int wins = 0;
		for (int i = 0; i < iters; i++) {
			int seed = rng.nextInt();
			MarioSimulator simulator = new MarioSimulator(level.getOptionsVisualizationOff() + FastOpts.L_RANDOM_SEED(seed));
			//MarioSimulator simulator = new MarioSimulator(level.getOptions() + FastOpts.L_RANDOM_SEED(seed));
			IAgent agent = new GeneralAgent(reader, writer);
			EvaluationInfo info = simulator.run(agent);
			if (info.marioStatus == Mario.STATUS_WIN) {
				wins++;
				System.out.println("WIN");
			} else {
				System.out.println("LOSE");
			}
		}
		
		System.out.println("WIN RATE: " + ((double)wins / iters) * 100 + "% ("+ wins + "/" + iters + ")");
		
		writer.write("END");
		
		writer.close();
		reader.close();
	}
}
