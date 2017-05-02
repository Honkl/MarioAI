package mario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import ch.idsia.agents.IAgent;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.tools.EvaluationInfo;

public class VisualizationTool {
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Wrong number of arguments. Expected 3 got " + args.length);
		}
		int numberOfGames = Integer.parseInt(args[0]);
		String levelType = args[1];
		int visualize = Integer.parseInt(args[2]);
		
		LevelConfig level;
		switch (levelType) {
		case "flat":
			level = LevelConfig.LEVEL_0_FLAT;
			break;
		case "jumping":
			level = LevelConfig.LEVEL_1_JUMPING;
			break;
		case "gombas":
			level = LevelConfig.LEVEL_2_GOOMBAS;
			break;
		case "tubes":
			level = LevelConfig.LEVEL_3_TUBES;
			break;
		case "spikes":
			level = LevelConfig.LEVEL_4_SPIKIES;
			break;
		default:
			throw new Exception("Unknown level");
		}

		evaluateModel(numberOfGames, level, visualize);
	}

	/**
	 * Evaluates specified model and writes it to a file.
	 */
	private static void evaluateModel(int numberOfRuns, LevelConfig level, int visualize) throws IOException {

		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(System.out));
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		
		Random rng = new Random(42);
		GeneralAgent agent = null;
		double avgDist = 0;
		for (int i = 0; i < numberOfRuns; i++) {
			int nextSeed = Math.abs(rng.nextInt());
		    MarioSimulator simulator;
		    if (visualize == 1) {
		    	simulator = new MarioSimulator(level.getOptions() + FastOpts.L_RANDOM_SEED(nextSeed));
		    } else {
		    	simulator = new MarioSimulator(level.getOptionsVisualizationOff() + FastOpts.L_RANDOM_SEED(nextSeed));
		    }
		    
		    // Run the simulation
		    agent = new GeneralAgent(r, w, simulator);
		    EvaluationInfo info = simulator.run(agent);
		    
		    avgDist += info.getPassedDistance();		    
		}
		
		avgDist /= numberOfRuns; 
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String time = dateFormat.format(date);
		PrintWriter writer = new PrintWriter("mario_results_" + time + ".txt");
		writer.println("Mario evaluation [" + numberOfRuns + " tests]");
		writer.println(level.toString());
		writer.println("WIN RATE (avg dist passed): " + avgDist);
		writer.flush();
		writer.close();
		
		JsonMessageObject jmo = new JsonMessageObject(agent.mario, agent.e, agent.t, agent.reward, (float)avgDist, true);
		String json = jmo.convertToJson() + "\n";
		w.write(json);
		w.close();
		r.close();
	}
}
