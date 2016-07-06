package com.example.jura.myapplication;

/**
 * Created by Jura on 16.06.2016.
 */

import android.widget.SeekBar;

public class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

    JuraActivity main;
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        //mTextValue.setText(String.valueOf(seekBar.getProgress()));
        //main.editTextView(String.valueOf(seekBar.getProgress()));
        main.setPosition(seekBar.getProgress());
    }
    public void init(JuraActivity _main) {
        main = _main;
    }
}
