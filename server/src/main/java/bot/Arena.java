package bot;

import chess.converter.ChessFunctions;
import chess.ChessGame;
import chess.InvalidMoveException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Arena {
    public static final double MUTATION_RATE = 0.05;
    private int generations;
    private int batchSize;
    private int cores;

    private final int CHECKMATE_POINTS = 200;
    private final int PIECE_CAPTURE_POINTS = 5;

    Random random = new Random();

    // A package for easily storing Campeon data
    public static class NeuronData {
        int generation;
        int a;
        int b;
        int c;

        @Override
        public String toString() {
            return generation + ", " + a + ", " + b + ", " + c + "\n";
        }

        NeuronData(int generation, int a, int b, int c) {
            this.generation = generation;
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    public static class GameResult {
        public Campeon winner;
        public int points;

        public GameResult(Campeon winner, int points) {
            this.winner = winner;
            this.points = points;
        }

    }

    // A package for easily storing game result data
    public void main(String[] args) {
        assert args.length == 2;
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
        GameResult[] previousBatchResults = new GameResult[]{};
        Campeon[] activeGeneration = new Campeon[]{};
        for (int generation = 0; generation < generations; generation++) {
            System.out.println("executing generation " + generation);

            System.out.println("Creating batch");
            activeGeneration = this.createBatch(previousBatchResults, batchSize);
            System.out.println("Finished Batch Creation");
            previousBatchResults = this.executeGeneration(activeGeneration, generation);
        }

        System.out.println("normal generations have finished");

        // Final Tournament
        while (this.batchSize > 2) {
            this.batchSize /= 2;
            System.out.println("Competing with batch size of " + this.batchSize);
            previousBatchResults = this.executeBatch(activeGeneration);
            activeGeneration = this.createBatch(previousBatchResults, batchSize);
        }

        // Write winner to file
        try {
            Functions.writeCampeonToFile(activeGeneration[0], "campeon_winner");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GameResult[] executeGeneration(Campeon[] newGeneration, int generation) {
        // Store the data
        System.out.println("Storing Data");
        for (Campeon campeon : newGeneration) {
            this.writeToFile(
                    new NeuronData(
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

    public GameResult[] executeBatch(Campeon[] batch) {
        GameResult[] winners = new GameResult[batchSize / 2];

        for (int i = 0; i < batchSize; i += 2) {
            Campeon whitePlayer = batch[i];
            Campeon blackPlayer = batch[i + 1];
            winners[i / 2] = this.executeGame(whitePlayer, blackPlayer);
        }

        return winners;
    }

    public GameResult executeGame(Campeon whitePlayer, Campeon blackPlayer) {
        ChessGame game = new ChessGame();

        int movesMade = 0;
        while (true) {
            if (game.isInCheckmate(game.getTeamTurn())) {
                System.out.println(game.getOffTeamColor() == ChessGame.TeamColor.WHITE ? "WHITE" : "BLACK" +
                        " won by Checkmate");
                Campeon winner = game.getTeamTurn() == ChessGame.TeamColor.WHITE ? blackPlayer : whitePlayer;
                this.writeToFile(ChessFunctions.exportGameToSAN(game));
                return new GameResult(winner, CHECKMATE_POINTS);

            } else if (game.isInStalemate(game.getTeamTurn()) || movesMade >= 128) {
                System.out.println("Stalemate after " + movesMade + " moves");

                // Decide who captured more pieces
                int whiteStrength = game.getTeamValue(ChessGame.TeamColor.WHITE);
                int blackStrength = game.getTeamValue(ChessGame.TeamColor.BLACK);

                if (whiteStrength > blackStrength) {
                    this.writeToFile(ChessFunctions.exportGameToSAN(game));
                    System.out.println("White won a game by piece value");
                    return new GameResult(whitePlayer, PIECE_CAPTURE_POINTS * (whiteStrength - blackStrength));
                }
                if (blackStrength > whiteStrength) {
                    this.writeToFile(ChessFunctions.exportGameToSAN(game));
                    System.out.println("Black won a game by piece value");
                    return new GameResult(blackPlayer, PIECE_CAPTURE_POINTS * (blackStrength - whiteStrength));
                }

                // If tied, that sucks.
                System.out.println("WARNING: tie found");
                this.writeToFile(ChessFunctions.exportGameToSAN(game));
                return new GameResult(blackPlayer, movesMade / 10);
            }

            try {
                game.makeMove(
                        game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                                whitePlayer.getBestMove(game) :
                                blackPlayer.getBestMove(game)
                );
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

    public Campeon[] createBatch(GameResult[] previousGeneration, int batchSize) {
        if (previousGeneration.length == 0) {
            Campeon[] firstGeneration = new Campeon[batchSize];
            for (int i = 0; i < batchSize; i++) {
                System.out.println("  created " + (i + 1) + "/" + batchSize);
                System.out.flush();
                firstGeneration[i] = this.createRandomStrain();
            }
            return firstGeneration;
        }

        int totalPointsWon = 0;
        for (GameResult gameResult : previousGeneration) {
            totalPointsWon += gameResult.points;
        }

        int remainingSlots = batchSize * 2;
        // Create new strains from the winners
        HashMap<GameResult, Integer> newGenerationData = new HashMap<>();
        for (GameResult gameResult : previousGeneration) {
            if (remainingSlots == 0) {
                break;
            }
            double percentage = (double) gameResult.points / totalPointsWon;
            int count = (int) Math.round(percentage * batchSize * 2);
            remainingSlots -= count;

            newGenerationData.put(gameResult, count);
        }

        GameResult[] newGenerationDataKeys = newGenerationData.keySet().toArray(new GameResult[0]);
        if (remainingSlots > 0) {
            System.out.println("Found an extra " + remainingSlots + " slots");
            for (int i = remainingSlots; i > 0; i--) {
                GameResult gameResult = newGenerationDataKeys[random.nextInt(newGenerationDataKeys.length)];
                newGenerationData.compute(gameResult, (k, currentOffspring) ->
                        (currentOffspring == null ? 0 : currentOffspring) + 1
                );
            }
        }


        Campeon[] newGeneration = new Campeon[batchSize];
        for (int i = 0; i < batchSize; i++) {
            GameResult pInput;
            while (true) {
                pInput = newGenerationDataKeys[random.nextInt(newGenerationDataKeys.length)];
                if (newGenerationData.get(pInput) == 0) {
                    newGenerationData.remove(pInput);
                    newGenerationDataKeys = newGenerationData.keySet().toArray(new GameResult[0]);
                } else {
                    break;
                }
            }

            GameResult sInput;
            while (true) {
                sInput = newGenerationDataKeys[random.nextInt(newGenerationDataKeys.length)];
                if (newGenerationData.get(sInput) == 0) {
                    newGenerationData.remove(sInput);
                    newGenerationDataKeys = newGenerationData.keySet().toArray(new GameResult[0]);
                } else {
                    break;
                }
            }

            int offspringCount = newGenerationData.get(pInput);
            int offspringCount2 = newGenerationData.get(sInput);

            newGeneration[i] = this.createStrainByParents(pInput.winner, sInput.winner);
            newGenerationData.put(pInput, offspringCount - 1);
            newGenerationData.put(sInput, offspringCount2 - 1);

            System.out.println("  created " + (i + 1) + "/" + batchSize);
            System.out.flush();
        }

        assert remainingSlots == 0;
        assert !newGenerationData.containsValue(1);

        return newGeneration;
    }

    public void writeToFile(NeuronData data) {
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

