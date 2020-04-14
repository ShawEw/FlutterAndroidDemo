package lib.hz.com.flutterdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tv_demo;
    private TextView tv_fragment;
    TextView tv_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_demo = findViewById(R.id.tv_demo);
        tv_fragment = findViewById(R.id.tv_fragment);
        tv_result = findViewById(R.id.tv_result);
        tv_result.setVisibility(View.GONE);
        tv_demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FlutterPageActivity.class);
                startActivity(intent);
            }
        });
        tv_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FlutterFragmentActivity.class);
                startActivityForResult(intent, 1000);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            tv_result.setVisibility(View.VISIBLE);
            String result = data.getStringExtra("message");
            tv_result.setText("result" + result);
        }
    }
}
