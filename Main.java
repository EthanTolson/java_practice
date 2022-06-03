package java_practice;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main
{
    //Array that holds all notes that hvae been played for current song
    public static ArrayList<String> listOfNotes = new ArrayList<String>();

    public static void main(String args[])
    {
        try{
            
            //load the midi file
            //base song is nocturne opus 9 no 2
            File midiFile = new File("java_practice\\Chopin_Nocturne9-2.mid");
            //create sequence to pass to sequencer from the midi file
            Sequence sequence = MidiSystem.getSequence(midiFile);
            //get the sequencer from midi system
            Sequencer sequencer = MidiSystem.getSequencer();

            //create reciever to get notes from midi file
            Receiver reciever = new customReciever();
            //get the transmitter to pull the data from
            Transmitter seqTrans = sequencer.getTransmitter();
            //set the reciever to the one created above
            seqTrans.setReceiver(reciever);

            //create window for user to interact with
            GUI window = new GUI(sequencer, sequence);
        }
        catch (Exception e)
        {
            System.out.println("Something went wrong" + e);
            return;
        }
    }

    public static class GUI 
    {
        //frame, buttons, text, song drop down, sequence of current song
        private JFrame frame;
        private JLabel text;
        private JButton playButton;
        private JButton stopButton;
        private JButton pauseButton;
        private JComboBox selectSong;
        private Sequence songSequence;

        public GUI(Sequencer sequencer, Sequence sequence)
        {
            //set the sequence to the one declared in main
            this.songSequence = sequence;

            //list of available songs in folder to be displayed in the drop down
            String listOfSongs[] = {"Chopin_Nocturne9-2", "Canon_in_D", "Fr_Elise", "Maple-Leaf-Rag", "Sonate_No_14", "Clair_de_Lune"};
            //drop down menu of song names
            this.selectSong = new JComboBox(listOfSongs);
            this.selectSong.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if( e.getSource() == selectSong)
                    {
                        try
                        {
                            //if the sequencer in not open load the song
                            if (!sequencer.isOpen())
                            {
                                songSequence = MidiSystem.getSequence(new File("java_practice\\"+ selectSong.getSelectedItem() + ".mid"));
                            }
                            //if the sequencer is playing stop it and start playing the new song
                            else
                            {
                                sequencer.stop();
                                sequencer.close();
                                listOfNotes.clear();
                                songSequence = MidiSystem.getSequence(new File("java_practice\\"+ selectSong.getSelectedItem() + ".mid"));
                                sequencer.open();
                                sequencer.setSequence(songSequence);
                                sequencer.start(); 
                                text.setText(selectSong.getSelectedItem() + " - Playing");
                            }
                        }
                        catch (Exception r)
                        {
                            text.setText("Something went wrong.\n" + r);
                            return;
                        }
                    }
                }
            });

            //create a frame to hold all buttons and text
            this.frame = new JFrame("SimpleMusicPlayer");

            //text to hold messages as actions are performed
            this.text = new JLabel("Press Play To Play Song", SwingConstants.CENTER);

            //button to start the sequencer
            this.playButton = new JButton("Play Song");
            this.playButton.setBounds(50,50,100,30);
            this.playButton.addActionListener(new ActionListener() 
            {    
                public void actionPerformed (ActionEvent e) 
                {   
                    //if nothing is playing start the song
                    if (!sequencer.isRunning())
                    {   
                        try
                        {
                            listOfNotes.clear();
                            sequencer.open();
                            sequencer.setSequence(songSequence);
                            sequencer.start(); 
                            text.setText(selectSong.getSelectedItem() + " - Playing");
                        }
                        catch(Exception w)
                        {
                            return;
                        }  
                    } 
                }    
            });

            //button to stop the song 
            //also clears the list of notes
            this.stopButton = new JButton("Stop Song");
            this.stopButton.setBounds(50,100,100,30);
            this.stopButton.addActionListener(new ActionListener() 
            {    
                public void actionPerformed (ActionEvent e) 
                {   
                    //stops the sequencer and displays message to user
                    sequencer.stop();
                    sequencer.close();
                    text.setText("Song Stopped");         
                }    
            });

            //pauses the current song
            this.pauseButton = new JButton("Pause Song");
            this.pauseButton.setBounds(50,150,100,30);
            this.pauseButton.addActionListener(new ActionListener()
            {
                public void actionPerformed (ActionEvent e)
                {
                    //if the sequencer is not playing then there is nothing to stop
                    if (sequencer.isRunning())
                    {
                        sequencer.stop();
                        text.setText(selectSong.getSelectedItem() + " - Paused");
                    }       
                }
            });
            createWindow();
        }

        //packs everything to the frame and sets it to visible
        private void createWindow()
        {
            this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.text.setPreferredSize(new Dimension(400, 400));
            this.frame.getContentPane().add(text, BorderLayout.CENTER);
            this.frame.getContentPane().add(playButton);
            this.frame.getContentPane().add(stopButton);
            this.frame.getContentPane().add(pauseButton);
            this.frame.getContentPane().add(selectSong);
            this.frame.setLocationRelativeTo(null);
            this.frame.setLayout(new FlowLayout());
            this.frame.pack();
            this.frame.setVisible(true);
        }
    }

    //overrides the reciever to pass information to note class
    public static class customReciever implements Receiver
    {
        public customReciever() 
        {
        }
        
        //sends midi data to note class is called everytime a midi message is passed to sequencer
        @Override
        public void send(MidiMessage message, long timeStamp) 
        {
            if(message instanceof ShortMessage) 
            {
                ShortMessage sm = (ShortMessage) message;

                //only want to pass messages that contain note data
                if (sm.getCommand() == ShortMessage.NOTE_ON) 
                {
                    //key dat is an int between 0 - 88
                    int key = sm.getData1();
                    //int velocity = sm.getData2();
                    //get note as a string and add it to the listOfNotes
                    listOfNotes.add(getNoteName(key));

                } 
                else if (sm.getCommand() == ShortMessage.NOTE_OFF) 
                {
                    int key = sm.getData1();
                    //int velocity = sm.getData2();
                    listOfNotes.add(getNoteName(key));
                }
            }
        }

        @Override
        public void close()
        {
        }
    }

    //converts the midi data to string(note+octave)
    public static String getNoteName(int note)
    {
        final String NOTE_NAMES[] = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int octave = note/12 - 1;
        String noteString = NOTE_NAMES[note % 12];
        return noteString + octave;
    }
}