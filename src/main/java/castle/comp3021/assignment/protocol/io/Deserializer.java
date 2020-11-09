package castle.comp3021.assignment.protocol.io;

import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Deserializer {
    @NotNull
    private Path path;

    private Configuration configuration;

    private Integer[] storedScores;

    Place centralPlace;

    private ArrayList<MoveRecord> moveRecords = new ArrayList<>();



    public Deserializer(@NotNull final Path path) throws FileNotFoundException {
        if (!path.toFile().exists()) {
            throw new FileNotFoundException("Cannot find file to load!");
        }

        this.path = path;
    }

    /**
     * Returns the first non-empty and non-comment (starts with '#') line from the reader.
     *
     * @param br {@link BufferedReader} to read from.
     * @return First line that is a parsable line, or {@code null} there are no lines to read.
     * @throws IOException if the reader fails to read a line
     * @throws InvalidGameException if unexpected end of file
     */
    @Nullable
    private String getFirstNonEmptyLine(@NotNull final BufferedReader br) throws IOException {
        // TODO
        String parsableLine = null;
        parsableLine = br.readLine();
        while(parsableLine != null){
            if (!parsableLine.isBlank() && !parsableLine.startsWith("#")){
                return parsableLine;
            }
            parsableLine = br.readLine();
        }
        throw new InvalidGameException("Unexpected End of File!");
    }

    public void parseGame() {
        try (var reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;

            int size;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                // TODO: get size here
                try{
                    line = line.split(":")[1].strip();
                    size = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    throw new InvalidGameException("Fail to parse board size. Please check format!");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of board size");
            }

            int numMovesProtection;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                // TODO: get numMovesProtection here
                try{
                    line = line.split(":")[1].strip();
                    numMovesProtection = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    throw new InvalidGameException("Fail to parse numMovesProtection. Please check format!");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of columns");
            }

            //TODO
            /**
             *  read central place here
             *  If success, assign to {@link Deserializer#centralPlace}
             *  Hint: You may use {@link Deserializer#parsePlace(String)}
             */
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                try{
                    line = line.split(":")[1].strip();
                    this.centralPlace = parsePlace(line);
                } catch (NumberFormatException | InvalidConfigurationError e) {
                    throw e;
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing central place!");
            }

            int numPlayers;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                //TODO: get number of players here
                try{
                    line = line.split(":")[1].strip();
                    numPlayers = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    throw new InvalidGameException("Failed to parse number of player. Please check format!");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of players");
            }

            // TODO:
            /**
             * create an array of players {@link Player} with length of numPlayers, and name it by the read-in name
             * Also create an array representing scores {@link Deserializer#storedScores} of players with length of numPlayers
             */
            Player[] players = new Player[numPlayers];
            this.storedScores = new Integer[numPlayers];
            for (int i = 0; i < numPlayers; ++i) {
                line = getFirstNonEmptyLine(reader);
                if (line == null) {
                    throw new InvalidGameException("Unexpected EOF when parsing information of players");
                }

                String name;
                name = line.split(";")[0].split(":")[1].strip();


                players[i] = new ConsolePlayer(name);
                int score;
                try {
                    score = Integer.parseInt(line.split("; ")[1].split(":")[1].strip());
                } catch (NumberFormatException e) {
                    throw new InvalidGameException("Parse score failed. Please check format!");
                }

                this.storedScores[i] = score;
            }

            // TODO
            /**
             * try to initialize a configuration object  with the above read-in variables
             * if fail, throw InvalidConfigurationError exception
             * if success, assign to {@link Deserializer#configuration}
             */
            this.configuration = new Configuration(size, players, numMovesProtection);

            // TODO
            /**
             * Parse the string of move records into an array of {@link MoveRecord}
             * Assign to {@link Deserializer#moveRecords}
             * You should first implement the following methods:
             * - {@link Deserializer#parseMoveRecord(String)}}
             * - {@link Deserializer#parseMove(String)} ()}
             * - {@link Deserializer#parsePlace(String)} ()}
             */
            while ((line = getFirstNonEmptyLine(reader)) != null) {
                if (line != null && !line.isBlank() && !line.startsWith("END")) {
                    try {
                        MoveRecord currentRecord = this.parseMoveRecord(line.strip());
                        this.moveRecords.add(currentRecord);
                        continue;
                    } catch (Exception e) {
                        throw new InvalidGameException("Parse move record failed. Please check format!");
                    }
                }
                break;
            }
        } catch (IOException ioe) {
            throw new InvalidGameException(ioe);
        }
    }

    public Configuration getLoadedConfiguration(){
        return configuration;
    }

    public Integer[] getStoredScores(){
        return storedScores;
    }

    public ArrayList<MoveRecord> getMoveRecords(){
        return moveRecords;
    }

    /**
     * Parse the string into a {@link MoveRecord}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveRecordString a string of a move record
     * @return a {@link MoveRecord}
     */
    private MoveRecord parseMoveRecord(String moveRecordString){
        // TODO
        Player currentPlayer;
        String movePlayer = moveRecordString.split(";")[0].split(":")[1].strip();
        if (this.configuration.getPlayers()[0].getName().equals(movePlayer)){
            currentPlayer = this.configuration.getPlayers()[0];
        }else{
            currentPlayer = this.configuration.getPlayers()[1];
        }
        Move currentMove = parseMove(moveRecordString.split(";")[1].strip());
        return new MoveRecord(currentPlayer, currentMove);
    }

    /**
     * Parse a string of move to a {@link Move}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveString given string
     * @return {@link Move}
     */
    private Move parseMove(String moveString) {
        // TODO
        String[] movePlace = new String[2];
        String[] temp = moveString.split(":")[1].split("->");
        if (temp.length < 2) {
            throw new InvalidConfigurationError("One move should contain both source and target!");
        }

        Place start = parsePlace(temp[0].strip());
        Place end = parsePlace(temp[1].strip());
        return new Move(start, end);
    }

    /**
     * Parse a string of move to a {@link Place}
     * Handle InvalidConfigurationError if the parse fails.
     * @param placeString given string
     * @return {@link Place}
     */
    private Place parsePlace(String placeString) {
        //TODO
        try{
            int centerX;
            int centerY;
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(placeString);
            if (matcher.find()){
                centerX =  Integer.parseInt(matcher.group());
            }else{
                throw new InvalidConfigurationError("Fail to Parse place: No X coordinate");
            }

            if (matcher.find()){
                centerY =  Integer.parseInt(matcher.group());
            }else{
                throw new InvalidConfigurationError("Fail to Parse place: No Y coordinate");
            }
            return new Place(centerX, centerY);

        } catch (NumberFormatException e) {
            throw new InvalidConfigurationError(e.getMessage());
        }
    }


}
