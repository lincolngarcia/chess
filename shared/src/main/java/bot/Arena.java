package bot;

import chess.ChessGame;
import chess.InvalidMoveException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Arena {
    public static final double MUTATION_RATE = 0.05;
    int generations;
    int batchSize;

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

        // assert batch size is a power of 2
        assert (batchSize & (batchSize - 1)) != 0;

        Campeon[] activeGeneration = new Campeon[]{};
        for (int generation = 0; generation < generations; generation++) {
            System.out.println("executing batch " + generation);
            activeGeneration = this.createBatch(activeGeneration);
            activeGeneration = this.executeGeneration(activeGeneration, generation);
            System.out.println("Finished batch creation");
        }

        System.out.println("normal generations have finished");
        // Tournament time

        while (this.batchSize >= 2) {
            this.batchSize /= 2;
            activeGeneration = this.executeBatch(activeGeneration);
        }

        try {
            Functions.writeCampeonToFile(activeGeneration[0], "campeon_winner");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Campeon[] executeGeneration(Campeon[] newGeneration, int generation) {
        // Store the data
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
        System.out.println("Finished Data Storage");

        // Execute the batch & return the winners
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
                return blackPlayer;
            }
            if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                return whitePlayer;
            }
            if (movesMade >= 128) {
                // Decide who captured more pieces
                int whiteStrength = game.getTeamValue(ChessGame.TeamColor.WHITE);
                int blackStrength = game.getTeamValue(ChessGame.TeamColor.BLACK);

                if (whiteStrength > blackStrength) {
                    return whitePlayer;
                }
                if (blackStrength > whiteStrength) {
                    return blackPlayer;
                }

                // If tied, create a new strain and return it
                System.out.println("WARNING: tie found");
                return createStrainByParents(whitePlayer, blackPlayer);

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
        return new Campeon(pInput, sInput, Arena.MUTATION_RATE);
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
               firstGeneration[i] = this.createRandomStrain();
           }
           return firstGeneration;
       }

        System.out.println("Finished batch creation");

        // Create new strains from the winners
        Campeon[] newGeneration = new Campeon[batchSize];
        for (int i = 0; i < previousGeneration.length / 2; i += 2) {
            Campeon pInput = previousGeneration[i];
            Campeon sInput = previousGeneration[i + 1];

            newGeneration[i * 4] = pInput;
            newGeneration[i * 4 + 1] = createStrainByParents(pInput, sInput);
            newGeneration[i * 4 + 2] = sInput;
            newGeneration[i * 4 + 3] = createStrainByParents(pInput, sInput);
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
}

