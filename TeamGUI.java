import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class TeamGUI implements ActionListener{

    private static JFrame frame = new JFrame();
    private static JPanel panel = new JPanel();
    private static JList<String> list;
    private static DefaultListModel<String> model;
    private String[] roster;

    JButton addbutton = new JButton("ADD");
    JButton remove = new JButton("REMOVE");
    JButton submit = new JButton("SUBMIT");
    JButton reset = new JButton("RESET");

    public static void main(String[] args) {
        new ConflictChecker();
        new TeamGUI(ConflictChecker.nameColumns);
    }

    public TeamGUI(String[] roster){
        this.roster = roster;
        // open GUI
        frame.setPreferredSize(new Dimension(400, 300));

        addbutton.addActionListener(this);
        addbutton.setFocusable(false);
        remove.addActionListener(this);
        remove.setFocusable(false);
        submit.addActionListener(this);
        submit.setFocusable(false);
        reset.addActionListener(this);
        reset.setFocusable(false);

        list = new JList<String>();
        model = new DefaultListModel<String>();

        list.setVisibleRowCount(4);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(list);
        panel.add(addbutton);
        panel.add(remove);
        panel.add(submit);
        panel.add(reset);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Pandora's Box - Conflict Checker");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.validate();
    }

    public void addPlayer(){
        String chosenPlayer = (String) JOptionPane.showInputDialog(
            null, "Who would you like to add to the team?", "TEAM BUILDER",
            JOptionPane.QUESTION_MESSAGE, null, roster, roster[0]);
        // team.add(playerOptions);
        if(model.contains(chosenPlayer) && chosenPlayer != null){
            String msg = "You already added " + chosenPlayer + " to the team!";
            JOptionPane.showMessageDialog(null, msg, 
                                "DUPLICATE ENTRY", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(chosenPlayer == null){
            return;
        }
        model.addElement(chosenPlayer);
        list.setModel(model);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == addbutton){
            System.out.println("add button was pressed");
            addPlayer();
        }
        if(e.getSource() == remove){
            System.out.println("remove button was pressed");
            String toRemove = list.getSelectedValue();
            model.removeElement(toRemove);
            list.setModel(model);
        }
        if(e.getSource() == submit){
            System.out.println("submit button was pressed");
            String[] team = new String[model.getSize()];
            for(int i = 0; i < model.getSize(); i++){
                team[i] = model.getElementAt(i);
            }
            try {
                ArrayList<ArrayList<String>> conflictSuperArray = ConflictChecker.makeTeam(team);
                ArrayList<String> cc = conflictSuperArray.get(0);
                ArrayList<String> dk = conflictSuperArray.get(1);
                if(!ConflictChecker.incompletes.isEmpty()){
                    String msg = "Warning:\nThe following users did not complete the form:\n";
                    for(String missing : ConflictChecker.incompletes){
                        msg += missing + "\n";
                    }
                    msg += "Consider them as no conflicts and proceed?";
                    JOptionPane.showMessageDialog(null, msg, 
                            "WARNING - UNFILLED FORMS", JOptionPane.WARNING_MESSAGE);
                }
                if(cc.isEmpty()){
                    if(dk.isEmpty()){
                        String msg = "This team has no conflicts!";
                        JOptionPane.showMessageDialog(null, msg, 
                                "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else{
                        String msg = "This team has no avoidance conflicts.";
                        msg += "\nIt does, however, have some conflicts with teammates knowing eachother.";
                        msg += "\nYou are completely free to ignore these, but they will be listed below anyway: \n\n";
                        for(String conflict : dk){
                            msg += conflict + "\n";
                        }
                        JOptionPane.showMessageDialog(null, msg, 
                            "OPTIONAL WARNING", JOptionPane.WARNING_MESSAGE);
                    }
                }
                else{
                    String msg = "This team has the following conflicts: \n";
                    for(String c : cc){
                        msg += c + "\n";
                    }
                    JOptionPane.showMessageDialog(null, msg, 
                            "CONFLICT", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NotEnoughPlayersException f){
                String msg = "A team must consist of at least two players!";
                JOptionPane.showMessageDialog(null, msg, 
                        "NOT ENOUGH PLAYERS", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
        }
        if(e.getSource() == reset){
            System.out.println("reset button was pressed");
            model.clear();
            list.setModel(model);
        }
    }
    
}
