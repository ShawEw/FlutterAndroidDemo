package lib.hz.com.flutterdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * @author jh.jiang
 * @date 2020/4/9.
 */
public class FlutterPageFragment extends Fragment {
    private static final String TAG = "FlutterPageFragment";

    private static final String CHANNEL_NATIVE = "com.example.flutter/native";
    private static final String CHANNEL_FLUTTER = "com.example.flutter/flutter";

    private FlutterEngine mFlutterEngine;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FlutterView flutterView = new FlutterView(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mFlutterEngine = new FlutterEngine(getContext());
        mFlutterEngine.getNavigationChannel().setInitialRoute("route1");
        mFlutterEngine.getDartExecutor().executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        );
        flutterView.attachToFlutterEngine(mFlutterEngine);
        flutterView.addOnFirstFrameRenderedListener(new FlutterUiDisplayListener() {
            @Override
            public void onFlutterUiDisplayed() {

            }

            @Override
            public void onFlutterUiNoLongerDisplayed() {

            }
        });
        return flutterView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MethodChannel nativeChannel = new MethodChannel(mFlutterEngine.getDartExecutor(), CHANNEL_NATIVE);
        nativeChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
                switch (methodCall.method) {
                    case "jumpToNative":
                        // 跳转原生页面
                        Intent jumpToNativeIntent = new Intent(getActivity(), NativeActivity.class);
                        jumpToNativeIntent.putExtra("name", (String) methodCall.argument("name"));
                        startActivityForResult(jumpToNativeIntent, 1001);
                        break;
                    case "goBackWithResult":
                        // 返回上一页，携带数据
                        Intent backIntent = new Intent();
                        backIntent.putExtra("message", (String) methodCall.argument("message"));
                        getActivity().setResult(Activity.RESULT_OK, backIntent);
                        getActivity().finish();
                        break;
                    default:
                        result.notImplemented();
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            Log.e(TAG,"接收到回调结果");
            // NativePageActivity返回的数据
            String message = data.getStringExtra("message");
            Map<String, Object> result = new HashMap<>();
            result.put("message", message);
            // 创建MethodChannel，这里的flutterView即Flutter.createView所返回的View
            MethodChannel flutterChannel = new MethodChannel(mFlutterEngine.getDartExecutor(), CHANNEL_FLUTTER);
            // 调用Flutter端定义的方法
            flutterChannel.invokeMethod("onActivityResult", result);
        }
    }
}
