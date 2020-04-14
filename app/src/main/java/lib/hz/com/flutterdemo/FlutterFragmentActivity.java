package lib.hz.com.flutterdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.flutter.embedding.android.FlutterFragment;


public class FlutterFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flutter_fragment);
        initViews();
    }

    private void initViews() {
        //写法1
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, new FlutterPageFragment()).commit();

//        //写法2
//        FlutterFragment fragment = FlutterFragment.withNewEngine().initialRoute("route1").build();
//        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commit();
//
//        //写法3
//        //通过FlutterFragment引入Flutter编写的页面
//        FlutterFragment flutterFragment = FlutterFragment.withNewEngine()
//                .initialRoute("route2")
//                .build();
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fl_container, flutterFragment)
//                .commit();
    }
}
