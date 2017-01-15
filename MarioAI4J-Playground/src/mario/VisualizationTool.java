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
		String pythonScriptPath = args[0];
		String pythonExePath = args[1];
		
		
		double[] winRates = new double[5];
		for (int i = 0; i < winRates.length; i++) {
			String modelConfigFile = "C:/Users/Jan/Desktop/models/best_" + i + ".json";
			System.out.println("Testing model: " + modelConfigFile);
			winRates[i] = evaluateModel(pythonScriptPath, pythonExePath, modelConfigFile);
		}		
	}

	/**
	 * Evaluates specified model and returns its win rate.
	 */
	private static double evaluateModel(String pythonScriptPath, String pythonExePath, String modelConfigFile) throws IOException {
		
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
		//LevelConfig level = LevelConfig.LEVEL_2_GOOMBAS;
		// LevelConfig level = LevelConfig.LEVEL_3_TUBES;
		LevelConfig level = LevelConfig.LEVEL_4_SPIKIES;
		// MarioSimulator simulator = new MarioSimulator(level.getOptions() + FastOpts.L_RANDOM_SEED(seed));

		// RUN THE SIMULATION
		int iters = 250;
		int wins = 0;
		for (int i = 0; i < iters; i++) {
			int seed = Math.abs(rng.nextInt());
			String opts = level.getOptionsVisualizationOff();
			//String opts = level.getOptions();
					
		    opts += FastOpts.L_RANDOM_SEED(seed);
		    MarioSimulator simulator = new MarioSimulator(opts);
			
			IAgent agent = new GeneralAgent(reader, writer);
			EvaluationInfo info = simulator.run(agent);
			if (info.marioStatus == Mario.STATUS_WIN) {
				wins++;
				//System.out.println("WIN");
				
			} else {
				//System.out.println("LOSE");
			}
		}
		
		double winRate = ((double)wins / iters) * 100;
		System.out.println("WIN RATE: " + winRate + "% ("+ wins + "/" + iters + ")");
		
		writer.write("END");
		
		writer.close();
		reader.close();
		
		return winRate;
	}
}
