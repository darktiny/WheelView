package me.dt2dev.wheelview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.dt2dev.wheelview.widget.WheelView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WheelView wheelView = (WheelView) findViewById(R.id.wheel_view);
        wheelView.setValues(new String[]{"HELLO", "MOTO", "HELLO", "WORLD", "HELLO", "MOTO", "HELLO", "WORLD"}, 0);
        wheelView.setOnSelectionChangedListener(new WheelView.OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(WheelView view, int selection) {
                System.out.println(selection);
            }
        });
    }
}
