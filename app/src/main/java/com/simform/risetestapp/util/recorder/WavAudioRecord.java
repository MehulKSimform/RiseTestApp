package com.simform.risetestapp.util.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.simform.risetestapp.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WavAudioRecord
{
    private static final String TAG = WavAudioRecord.class.getSimpleName();

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private final static int[] sampleRates = {44100, 22050, 11025, 8000};

    private int mSampleRate;

    private AudioRecord mRecorder = null;
    private int mBufferSize = 0;
    private Thread mRecordingThread = null;
    private boolean mIsRecording = false;

    private String mRecordFilePath;
    private String mTempRecordFilePath = FileUtils.createTempFile("record_temp.raw");

    public WavAudioRecord(String recordFilePath)
    {
        this.mRecordFilePath = recordFilePath;
    }

    public void startRecording()
    {
        byte data[];

        for(int sampleRate : sampleRates)
        {
            mSampleRate = sampleRate;

            mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, mBufferSize);
            data = new byte[mBufferSize];

            if(AudioRecord.ERROR_INVALID_OPERATION != mRecorder.read(data, 0, mBufferSize))
            {
                break;
            }
        }

        if(mRecorder.getState() == AudioRecord.STATE_INITIALIZED)
        {
            mRecorder.startRecording();
        }

        mIsRecording = true;

        mRecordingThread = new Thread(this::writeAudioDataToFile,"AudioRecorder Thread");

        mRecordingThread.start();
    }

    public void stopRecording()
    {
        if(mRecorder != null)
        {
            mIsRecording = false;

            if(mRecorder.getState() == AudioRecord.STATE_INITIALIZED)
            {
                mRecorder.stop();
            }

            mRecorder.release();

            mRecorder = null;
            mRecordingThread = null;
        }

        copyWaveFile(mTempRecordFilePath, mRecordFilePath);
        deleteTempFile();
    }

    private void deleteTempFile()
    {
        File file = new File(mTempRecordFilePath);
        file.delete();
    }

    private void copyWaveFile(String inFilename, String outFilename)
    {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = mSampleRate;
        int channels = 2;
        long byteRate = RECORDER_BPP * mSampleRate * channels/8;

        byte[] data = new byte[mBufferSize];

        try
        {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.i(TAG, "File size: " + totalDataLen);

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

            while(in.read(data) != -1)
            {
                out.write(data);
            }

            in.close();
            out.close();

        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

    }

    private void writeAudioDataToFile()
    {
        byte data[] = new byte[mBufferSize];
        FileOutputStream outputStream = null;

        try
        {
            outputStream = new FileOutputStream(mTempRecordFilePath);
        }
        catch (FileNotFoundException e)
        {
            Log.e(TAG, e.getMessage());
        }

        int read;

        if(outputStream != null)
        {
            while(mIsRecording)
            {
                read = mRecorder.read(data, 0, mBufferSize);

                Log.d("Works code: ", String.valueOf(read));

                if(AudioRecord.ERROR_INVALID_OPERATION != read)
                {
                    try
                    {
                        outputStream.write(data);
                    }
                    catch (IOException e)
                    {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }

            try
            {
                outputStream.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException
    {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }


}
