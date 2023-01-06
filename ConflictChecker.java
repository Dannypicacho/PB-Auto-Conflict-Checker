/**
 * @author Danny Picazo
 */


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.regex.*;
import javax.swing.*;

public class ConflictChecker{
    // < Entry Player , < Other Player, Response > >
    private static HashMap<String, HashMap<String, String>> entries = new HashMap<String, HashMap<String, String>>();
    public static String[] nameColumns;
    public static ArrayList<String> incompletes = new ArrayList<String>();
    private static final String FILENAME = "./responses.tsv";
    private static HashMap<String, Integer> desiredKnownResponses = new HashMap<String, Integer>();

    ConflictChecker() {
        // first, read all the responses
        try {
            readResponses();
        } catch (TooManyMesException e) {
            String msg = "The following names had multiple entries mark them as 'That's me!': \n\n";
            for(Map.Entry<String, ArrayList<String>> set : e.failures.entrySet()){
                msg += set.getKey() + ": ";
                for(String disc : set.getValue()){
                    msg += disc + ", "; // dont care
                }
                msg += "\n";
            }
            msg += "\nPlease have these users correct it before proceding.";
            JOptionPane.showMessageDialog(null, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IdentityCrisisException f) {
            String msg = "The following users did not correctly enter one 'That's me!' option: \n";
            for(String idiot : f.perpetrators){
                msg += idiot + "\n";
            }
            msg += "Please have these users correct it before proceding.";
            JOptionPane.showMessageDialog(null, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IllegalStateException g) {
            String msg = "Regex error. It appears the response sheet has been manually tampered with."
                        +"\nIt's likely that you've manually edited something which broke the program."
                        + "\nPlease remember to delete any extra rows/columns (such as counters) prior to downloading the sheet."
                        +"\nIf that's not the case, please ensure that all the names at the top of the columns are encased with square brackets. [name]";
            JOptionPane.showMessageDialog(null, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
            g.printStackTrace();
            System.exit(1);
        } catch (Exception z){
            JOptionPane.showMessageDialog(null, 
                    "An unknown error occured.\nPlease contact Dannypicacho.", 
                                                            "ERROR", JOptionPane.ERROR_MESSAGE);
            z.printStackTrace();
            System.exit(1);
        }

    }

    private static void readResponses() throws TooManyMesException, IdentityCrisisException{
        try {
            File myObj = new File(FILENAME);
            Scanner myReader = new Scanner(myObj);
            // the line in the .tsv
            String line = myReader.nextLine();
            // splits line by tabs, each index being a column
            // [1] = discord
            String[] splitted = line.split("\t");
            // get names as columns
            nameColumns = new String[splitted.length-3];

            // holds every identity crisis
            ArrayList<String> identityCrises = new ArrayList<String>();
            // holds an arraylist of arraylists, index representing the user and arraylist
            // representing the list of people who put "That's me!" for them - should be one each.
            ArrayList<ArrayList<String>> meList = new ArrayList<ArrayList<String>>();

            for(int i = 3; i < splitted.length; i++){
                Pattern pat = Pattern.compile("\\[(.*?)\\]"); 
                Matcher mat = pat.matcher(splitted[i]);
                mat.find();
                String name = mat.group();
                name = name.substring(1, name.length()-1);
                nameColumns[i-3] = name;
            }
            for(int j = 0; j < nameColumns.length; j++){
                meList.add(new ArrayList<String>());
            }
            while (myReader.hasNextLine()) {
                // read line
                line = myReader.nextLine();
                splitted = line.split("\t");
                // accounting for bias of first three columns
                // may need to +/- 3 from following indexing
                @SuppressWarnings("unused")
                String timestamp = splitted[0];
                String discord = splitted[1];
                // this number will represent how many teammates the player wants to know
                int desiredKnown = Character.getNumericValue(splitted[2].charAt(0)) - 1;
                // in the case that the entry user variable is not properly 
                // updated, then it will take a default error value
                String entryUser = "DEFAULT VALUE - ERROR";
                // counts number of "That's me!" responses -- must be 1
                int meCount = 0;
                HashMap<String, String> responses = new HashMap<String, String>();
                System.out.println(discord + " wants to know at least " + desiredKnown + " teammates.");
                for(int i = 3; i < splitted.length; i++){
                    // splitted[i] represents answer (e.g., Neutral)
                    // nameColumns[i-3] represents the columns (e.g., yumi)
                    if(splitted[i].equals("That's me!") || splitted[i].equals("This is me")){
                        meCount++;
                        // entry user will be the user who submitted the entry
                        // aka, whatever column their "thats me" is
                        entryUser = nameColumns[i-3];
                        meList.get(i-3).add(discord);
                    }
                    else{
                        System.out.println(discord + " put " + splitted[i] + " for " + nameColumns[i-3]);
                        responses.put(nameColumns[i-3], splitted[i]);
                    }
                }
                if(meCount != 1){
                    // identity crisis error
                    System.out.println("Identity Crisis: " + discord);
                    identityCrises.add(discord);
                }
                desiredKnownResponses.put(entryUser, desiredKnown);
                entries.put(entryUser, responses);
                System.out.println("FINISHING ENTRY FOR: " + entryUser);
            }
            myReader.close();
            // check for duplicate "That's me!" entries
            HashMap<String, ArrayList<String>> dupeList = new HashMap<String, ArrayList<String>>();
            boolean meError = false;
            for(int x = 0; x < meList.size(); x++){
                ArrayList<String> arrlist = meList.get(x);
                if(arrlist.size() > 1){
                    dupeList.put(nameColumns[x], arrlist);
                    meError = true;
                }
            }
            if(meError){
                throw new TooManyMesException(dupeList);
            }
            if(identityCrises.size() > 0){
                throw new IdentityCrisisException(identityCrises);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
            String msg = "The file was not found. Make sure you have 'responses.tsv' in the same folder as the .jar file.\n";
            msg += "Please make sure the file is called responses.tsv";
            JOptionPane.showMessageDialog(null, msg, 
                            "ERROR - FILE NOT FOUND", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static ArrayList<ArrayList<String>> makeTeam(String[] players) throws NotEnoughPlayersException{
        // cannot make it only 2
        if(players.length < 2){
            System.out.println("Please enter at least 2 players.");
            throw new NotEnoughPlayersException();
        }
        ArrayList<String> conflicts = new ArrayList<String>();
        ArrayList<String> unfinished = new ArrayList<String>();
        ArrayList<String> dontKnowConflicts = new ArrayList<String>();
        for(String player : players){
            ArrayList<String> dontKnows = new ArrayList<String>();
            if(!entries.containsKey(player)){
                System.out.println(player + " has not filled out the form!");
                unfinished.add(player);
                continue;
            }
            HashMap<String, String> playerEntries = entries.get(player);
            for(String teammate : players){
                // if teammate is the player
                if(player.equals(teammate)) {continue;}
                // in the case of a new player being added, many old entries
                // will not have a response for said new player
                // thus these must be skipped
                else if(!playerEntries.containsKey(teammate)){
                    System.out.println(player + " has no response for " + teammate);
                    continue;
                }

                // there exists a response!
                String response = playerEntries.get(teammate);
                // conflict
                if(response.equals("I'd rather not") || response.equals("Please don't")){
                    System.out.print("CONFLICT: ");
                    String msg = player + " does not want to team with " + teammate;
                    System.out.println(msg);
                    conflicts.add(msg);
                }
                // response is "don't know"
                else if (response.equals("Don't know")){
                    dontKnows.add(teammate);
                    System.out.println(player + " does not know " + teammate + " (no conflict)");
                }
                // yes or neutral
                else{
                    System.out.println(player + " does not have a conflict with " + teammate);
                }
            }
            // dont know responses surpases desired amount by player
            int dk = desiredKnownResponses.get(player);
            if(dk > players.length - dontKnows.size() - 1){
                String dkConflict = player + " would prefer to know at least " + dk + " teammate";
                if(dk > 1){
                    dkConflict += "s";
                }
                dkConflict += ".";
                for(String randomscrandom : dontKnows){
                    dkConflict += "\n    >>" + player + " does not know " + randomscrandom;
                }
                dontKnowConflicts.add(dkConflict);
            }
        }
        incompletes = unfinished;
        // return an array with conflicts
        // at index 0: arraylist of 'avoid' conflicts
        // at index 1: arraylist of 'dont know' conflicts
        ArrayList<ArrayList<String>> allConflictArray = new ArrayList<ArrayList<String>>();
        allConflictArray.add(0, conflicts);
        allConflictArray.add(1, dontKnowConflicts);
        return allConflictArray;
    }

    @SuppressWarnings("unused")
    private static void createFile(String file){
        try{
            File outp = new File(file);
            if(outp.createNewFile()){
              System.out.println("\nFile created: " + outp.getName());
            }
            else{
                System.out.println("\nFile already exists. Overriding.");
            }
        } 
        catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }  
    }

}
