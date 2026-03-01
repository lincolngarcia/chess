package bot;

import chess.ChessConverter.ChessFunctions;
import chess.ChessGame;
import chess.InvalidMoveException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Arena {
    public static final double MUTATION_RATE = 0.05;
    private int generations;
    private int batchSize;
    private int cores;

    // A package for easily storing Campeon data
    public static class neuronData {
        int generation;
        int a;
        int b;
        int c;

        @Override
        public String toString() {
            return generation + ", " + a + ", " + b + ", " + c + "\n";
        }

        neuronData(int generation, int a, int b, int c) {
            this.generation = generation;
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    Random random = new Random();

    public void main(String[] args) {
        this.generations = Integer.parseInt(args[0]);
        this.batchSize = Integer.parseInt(args[1]);

        assert generations > 0;
        assert batchSize > 0;
        assert batchSize % 2 == 0;

        // Multithreading variables
        this.cores = Runtime.getRuntime().availableProcessors();

        // assert batch size is a power of 2
        assert (batchSize & (batchSize - 1)) != 0;

        // Perform generations
        Campeon[] activeGeneration = new Campeon[]{};
        for (int generation = 0; generation < generations; generation++) {
            System.out.println("executing generation " + generation);

            System.out.println("Creating batch");
            activeGeneration = this.createBatch(activeGeneration);
            System.out.println("Finished Batch Creation");
            activeGeneration = this.executeGeneration(activeGeneration, generation);
        }

        System.out.println("normal generations have finished");

        // Final Tournament
        while (this.batchSize > 2) {
            this.batchSize /= 2;
            System.out.println("Competing with batch size of " + this.batchSize);
            activeGeneration = this.executeBatch(activeGeneration);
        }

        // Write winner to file
        try {
            Functions.writeCampeonToFile(activeGeneration[0], "campeon_winner");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Campeon[] executeGeneration(Campeon[] newGeneration, int generation) {
        // Store the data
        System.out.println("Storing Data");
        for (Campeon campeon : newGeneration) {
            this.writeToFile(
                    new neuronData(
                            generation,
                            campeon.getRawNeuronCountAmplifier(),
                            campeon.getRawNeuronSpreadAmplifier(),
                            campeon.getRawNeuronSlopeAmplifier()
                    )
            );
        }

        // Execute the batch & return the winners
        System.out.println("Executing Batch");
        return this.executeBatch(newGeneration);
    }

    public Campeon[] executeBatch(Campeon[] batch) {
        Campeon[] winners = new Campeon[batchSize / 2];

        for (int i = 0; i < batchSize; i += 2) {
            Campeon whitePlayer = batch[i];
            Campeon blackPlayer = batch[i + 1];
            winners[i / 2] = this.executeGame(whitePlayer, blackPlayer);
        }

        return winners;
    }

    public Campeon executeGame(Campeon whitePlayer, Campeon blackPlayer) {
        ChessGame game = new ChessGame();

        int movesMade = 0;
        while (true) {
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                this.writeToFile(ChessFunctions.exportGameToSAN(game));
                System.out.println("Black won a game by checkmate");
                return blackPlayer;
            }
            if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                this.writeToFile(ChessFunctions.exportGameToSAN(game));
                System.out.println("White won a game by checkmate");
                return whitePlayer;
            }
            if (movesMade >= 128) {
                // Decide who captured more pieces
                int whiteStrength = game.getTeamValue(ChessGame.TeamColor.WHITE);
                int blackStrength = game.getTeamValue(ChessGame.TeamColor.BLACK);

                if (whiteStrength > blackStrength) {
                    this.writeToFile(ChessFunctions.exportGameToSAN(game));
                    System.out.println("White won a game by piece value");
                    return whitePlayer;
                }
                if (blackStrength > whiteStrength) {
                    this.writeToFile(ChessFunctions.exportGameToSAN(game));
                    System.out.println("Black won a game by piece value");
                    return blackPlayer;
                }

                // If tied, create a new strain and return it
                System.out.println("WARNING: tie found");
                this.writeToFile(ChessFunctions.exportGameToSAN(game));
                return this.createRandomStrain();

            }

            try {
                game.makeMove(whitePlayer.getBestMove(game));
                game.makeMove(blackPlayer.getBestMove(game));
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }

            movesMade++;
        }

    }

    public Campeon createStrainByParents(Campeon pInput, Campeon sInput) {
        int[][] brainData = Campeon.createBrainDataFromParents(pInput, sInput, Arena.MUTATION_RATE);
        return new Campeon(brainData);
    }

    public Campeon createRandomStrain() {
        int metaData = random.nextInt();

        int a = Campeon.parseNeuronCountAmplifier(Campeon.parseRawNeuronCountAmplifier(metaData));
        int b = Campeon.parseNeuronSpreadAmplifier(Campeon.parseRawNeuronSpreadAmplifier(metaData));
        double c = Campeon.parseNeuronSlopeAmplifier(Campeon.parseRawNeuronSlopeAmplifier(metaData));

        int connectionCount = Campeon.parseConnectionCountFromMetaData(metaData);
        ArrayList<Integer> layerSizes = Campeon.calculateHiddenLayerSizes(a, b, c);
        int neuronCount = Campeon.calculateTotalNeuronCount(layerSizes);

        int[] connections = Campeon.generateRandomConnections(neuronCount, layerSizes, connectionCount);

        return new Campeon(metaData, connections);
    }

    public Campeon[] createBatch(Campeon[] previousGeneration) {
       if (previousGeneration.length == 0) {
           Campeon[] firstGeneration = new Campeon[batchSize];
           for (int i = 0; i < batchSize; i++) {
               System.out.println("  created " + (i + 1) + "/" + batchSize);
               System.out.flush();
               firstGeneration[i] = this.createRandomStrain();
           }
           return firstGeneration;
       }

        // Create new strains from the winners
        Campeon[] newGeneration = new Campeon[batchSize];
        for (int i = 0; i < (previousGeneration.length / 2); i++) {
            Campeon pInput = previousGeneration[i];
            Campeon sInput = previousGeneration[i + 1];

            newGeneration[i * 4] = pInput;
            newGeneration[i * 4 + 1] = createStrainByParents(pInput, sInput);
            newGeneration[i * 4 + 2] = sInput;
            newGeneration[i * 4 + 3] = createStrainByParents(pInput, sInput);

            System.out.println("  created " + (i + 1) + "-" + (i + 5) + "/" + batchSize);
            System.out.flush();
        }

       return newGeneration;
    }

    public void writeToFile(neuronData data) {
        String filename = "arena_data.txt";

        try (FileWriter fw = new FileWriter(filename, true)) {  // 'true' enables append mode
            fw.write(data.toString());
        } catch (IOException e) {
            throw new RuntimeException("IO Error");
        }
    }

    public void writeToFile(String data) {
        String filename = "game_data.txt";

        try (FileWriter fw = new FileWriter(filename, true)) {  // 'true' enables append mode
            fw.write("==========\n");
            fw.write(data);
        } catch (IOException e) {
            throw new RuntimeException("IO Error");
        }
    }
}

