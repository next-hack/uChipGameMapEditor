/*
    Midi converter based on Aleq Borque's MidiConvert for UZEBox.
    This has been cleaned up and corrected (no more speed factor required!) by
    Nicola Wrachien (next-hack)
*/
package uchipgamemapeditor;

import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MidiConverter
{
    static final int NUMBER_OF_CHANNELS = 4;
    
    private MidiConvertOptions options;
    
    private static final int CONTROLER_TREMOLO = 92;
    private static final int CONTROLER_TREMOLO_RATE = 100;
    private static final int CONTROLER_VOL = 7;
    private static final int CONTROLER_EXPRESSION = 11;    
    
    static final int CMD_PROGRAM_CHANGE = 0xc0;        
    static final int CMD_PITCH_BEND = 0xe0;        
    //
    static final int META_EVT_MARKER = 0x6;
    static final int META_EVT_END_OF_TRACK = 0x2f;
    static final int META_EVT_TEMPO = 0x51;
    int ticksPerBeat = 60;
    public MidiConverter(MidiConvertOptions o)
    {
        options = o;
    }
    public void convertSong() throws Exception
    {
        // This is strongly based on Alec Borque's converter utility. Most of the stuff has been removed.
        File inputFile=new File(options.midiFileName);
        
        MidiFileFormat format = MidiSystem.getMidiFileFormat(inputFile);
        if (format.getType() != 0 && format.getType() != 1)
        {
            throw new RuntimeException("Unsupported file format " + format.getType() + ". Only MIDI file formats 0 and 1 are supported.");
        }
        Sequence inSequence = MidiSystem.getSequence(inputFile);
        if (format.getType() == 1)
        {
            Track[] tracks = inSequence.getTracks();
            for (int i = 1; i < tracks.length; i++)
            {
                for (int j = 0; j < tracks[i].size(); j++)
                {
                    tracks[0].add(tracks[i].get(j));
                }
                inSequence.deleteTrack(tracks[i]);
            }
        }
        //
        Sequence seq = new Sequence(format.getDivisionType(), format.getResolution(), 1);
        Track outTrack = seq.getTracks()[0];
        // *** CHECK
 // OLD CODE
 //       long tempo = 60000000 / format.getResolution();  // in PPQ, resolution returns the ticks per quarter note
        ticksPerBeat = format.getResolution(); //  Note= Beat == Quarter Note! TODO: Handle case of SMPTE
        long tempo = 500000;        // default MIDI tempo si 500ms per beat, i.e. 120 beats per minutes. Note= Beat == Quarter Note!
        Track track = inSequence.getTracks()[0];

        // Print track information, convert NOTE_OFF commands (0x80, w/ vol 64) into NOTE_ON commands (0x90, w/ vol 0)
        Track[] itracks = inSequence.getTracks();
        if (itracks != null)
        {
            for (int i = 0; i < itracks.length; i++)
            {
                Track mtrack = itracks[i];
                for (int j = 0; j < mtrack.size(); j++)
                {
                    MidiEvent event = mtrack.get(j);
                    MidiMessage message = event.getMessage();
                    if (message instanceof ShortMessage)
                    {
                        ShortMessage m = (ShortMessage) message;
                        if (m.getCommand() == ShortMessage.NOTE_OFF)
                        {
                            int channel = m.getChannel();
                            int pitch = m.getData1();
                            int vel = m.getData2();
                            m.setMessage(ShortMessage.NOTE_ON, channel, pitch, 0);
                        }
                    }
                }
            }
        }

        for (int e = 0; e < track.size(); e++)
        {
            MidiEvent event = track.get(e);

            if (event.getMessage() instanceof MetaMessage)
            {
                MetaMessage m = (MetaMessage) event.getMessage();
                int evtType = m.getType();
                // only deal with supported meta events
                if (evtType == META_EVT_MARKER || evtType == META_EVT_TEMPO || evtType == META_EVT_END_OF_TRACK)
                {    
                    if (evtType == META_EVT_MARKER && m.getLength() > 4)
                    {
                        throw new RuntimeException("META markers text size must by only one character: " + new String(m.getData()));
                    }

                    if (evtType == META_EVT_TEMPO)
                    { //tempo event. Returns microseconds per quarter note. 
                        byte[] t = m.getData();
                        long tmp = (t[0] << 16) + (t[1] << 8) + t[2];
                        tempo = tmp;
                    }
                    else if (!(evtType == META_EVT_MARKER && options.specifyLoop))
                    {//ignore loop meta events if we specified some on command line
                        addEvent(outTrack, event, tempo, ticksPerBeat);
                    }
                }
            }
            else if (event.getMessage() instanceof ShortMessage)
            {
                ShortMessage m = (ShortMessage) event.getMessage();
                /*
                On UZEBOX channel 3 was dedicated to PCM, so percussions (channel 9) were routed there.
                if (m.getChannel() == 9)
                {
                    ShortMessage newMessage = new ShortMessage();
                    newMessage.setMessage(m.getCommand(), 3, m.getData1(), m.getData2());
                    event = new MidiEvent(newMessage, event.getTick());
                    m = (ShortMessage) event.getMessage();
                }*/
                int cmd = m.getCommand();
                // only deal with supported commands
                if (cmd == ShortMessage.CONTROL_CHANGE || cmd == ShortMessage.NOTE_ON || cmd == ShortMessage.NOTE_OFF || cmd == ShortMessage.PITCH_BEND || cmd == ShortMessage.PROGRAM_CHANGE)
                {
                    if ( cmd == ShortMessage.CONTROL_CHANGE)
                    { //controllers
                        if (m.getData1() == CONTROLER_VOL || m.getData1() == CONTROLER_EXPRESSION || m.getData1() == CONTROLER_TREMOLO || m.getData1() == CONTROLER_TREMOLO_RATE)
                        {
                            addEvent(outTrack, event, tempo, ticksPerBeat);
                        }
                    }
                    else if (cmd == ShortMessage.NOTE_ON && m.getData2() == 0 || cmd == ShortMessage.NOTE_OFF)
                    { //note off: volume 0 or explicit note off
                        if (m.getChannel() < NUMBER_OF_CHANNELS && options.noteOff[m.getChannel()] == true)
                        {
                            addEvent(outTrack, event, tempo, ticksPerBeat);
                        }
                    }
                    else
                    {
                        addEvent(outTrack, event, tempo, ticksPerBeat);
                    }                    
                }
            }
        }
        //add looping meta events if required
        if (options.specifyLoop)
        {
            MetaMessage metaS = new MetaMessage();
            
            metaS.setMessage(META_EVT_MARKER, new byte[]{'S'}, 1); //loop start, "S" marker
            MidiEvent eventS = new MidiEvent(metaS, options.loopStart);
            addEvent(outTrack, eventS, tempo, ticksPerBeat);

            MetaMessage metaE = new MetaMessage();
            metaE.setMessage(META_EVT_MARKER, new byte[]{'E'}, 1); //loop end, "E" marker
            MidiEvent eventE = new MidiEvent(metaE, options.loopEnd);
            addEvent(outTrack, eventE, tempo, ticksPerBeat);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MidiSystem.write(seq, 0, bos);
        byte data[] = bos.toByteArray();
        //output track data skipping headers
        StringBuffer out = new StringBuffer();
        out.append("//*********************************//\r\n");
        out.append("// MIDI file: " + options.cFileName + "\r\n");
        out.append("//*********************************//\r\n");

        out.append("const char " + options.variableName + "[] =\r\n{\r\n\t");
        int totalSize = 0;

        int b;
        for (int k = (14 + 8); k < data.length; k++)
        {
            b = (int) (data[k] & 0xff);
            if (b <= 0xf)
            {
                out.append("0x0" + Integer.toHexString(b));
            }
            else
            {
                out.append("0x" + Integer.toHexString(b));
            }
            out.append(",");
            totalSize++;
            if (totalSize % 32 == 0)
            {
                out.append("\r\n\t");
            }
        }
        out.setCharAt(out.length() - 1, ' '); //remove traling comma	
        out.append("\r\n};\r\n");
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(options.cFileName), "utf-8")))
        {
            writer.write(out.toString());
        } 
    }

    static long tickDiff = 0;
    static boolean first = true;

    private static void addEvent(Track track, MidiEvent event, double microSecondsPerBeat, int ticksPerBeat)
    {

        if (first && event.getTick() != 0)
        {
            tickDiff = event.getTick();
        }
  
        
        
        Double scaled = new Double(event.getTick() - tickDiff);
        // The soundEngine is called with a frequency of 12MHz / 400 / 525. Each uChip tick is about 1/57 s and it is fixed. 
        // We must convert the delta ticks (scaled) to uChip ticks.
        double microSecondsPerTick = microSecondsPerBeat / ((double)ticksPerBeat);
        double microSecondsPerUchipTick = (400 * 525.0) / 12;
        double factor = microSecondsPerTick / microSecondsPerUchipTick;
   //     double factor = ticksPerBeat / (60000000 / microSecondsPerBeat); // tempo/60M * ticksPerBeat        
        scaled = scaled * factor;
        long l = scaled.longValue();
        event.setTick(l);

        track.add(event);
        first = false;
    }
}
