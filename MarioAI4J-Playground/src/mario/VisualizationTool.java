package mario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import ch.idsia.agents.IAgent;
import ch.idsia.benchmark.mario.MarioSimulator;
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
		
		int seed = 42; 

		// LevelConfig level = LevelConfig.LEVEL_0_FLAT;
		// LevelConfig level = LevelConfig.LEVEL_1_JUMPING;
		LevelConfig level = LevelConfig.LEVEL_2_GOOMBAS;
		// LevelConfig level = LevelConfig.LEVEL_3_TUBES;
		// LevelConfig level = LevelConfig.LEVEL_4_SPIKIES;
		MarioSimulator simulator = new MarioSimulator(level.getOptions() + FastOpts.L_RANDOM_SEED(seed));

		// RUN THE SIMULATION
		IAgent agent = new GeneralAgent(reader, writer);
		EvaluationInfo info = simulator.run(agent);
		
		System.out.println("passed_distance=" + info.getPassedDistance());
		
		writer.write("END");
		
		writer.close();
		reader.close();
	}
}
