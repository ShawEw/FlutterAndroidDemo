package lib.hz.com.flutterdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class NativeActivity extends AppCompatActivity {

    TextView tv_demo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);
        tv_demo = findViewById(R.id.tv_demo);
        tv_demo.setText(getIntent().getStringExtra("name"));
        tv_demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("message", "我从原生页面回来了");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
