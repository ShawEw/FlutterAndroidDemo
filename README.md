# android原生混编Flutter

## 接入步骤

- 第一步、新建android原生项目

- 第二步、新建Flutter Module

  1. 通过命令行创建。切换到android项目的同级目录下（这里建议直接使用Terminal）。执行如下命令：

     ```
     flutter create -t module my_flutter
     ```

     其中my_flutter为改module名字。

  2. 直接使用AS创建。File --> New --> New Flutter Project，然后选择Flutter Module。然后填写module的名称、路径。最后填写module的包名，点击Finish就创建好了一个Flutter Module。

- 第三步在android项目中引入 Flutter Module

  1. 在app下的build.gradle文件中添加以下配置

     ```groovy
     compileOptions {
       sourceCompatibility 1.8
       targetCompatibility 1.8
     }
     ```

     可以解决版本兼容性问题。如果不配置可能会报错**Invoke-customs are only supported starting with Android O (--min-api 26)**。

  2. 在项目的根目录下的setting.gradle文件中配置

     ```groovy
     include ':app'
     // 加入下面配置
     setBinding(new Binding([gradle: this]))
     evaluate(new File(
             settingsDir.parentFile,
             'FlutterDemo/my_flutter/.android/include_flutter.groovy'
     ))
     ```

     需要修改为自己的module名字。

  3. 编译成功后在app的build.gradle文件下添加依赖。

     ```groovy
     implementation project(':flutter')
     ```

## android 与 Flutter交互

Flutter在1.12版本后舍弃了部分类，导致交互这里有较大变动。

### android原生跳转到Flutter界面

#### 一、Activity形式

1. Activity形式。新建一个Android Activity。进行跳转。代码如下：

   ```java
   public class FlutterPageActivity extends AppCompatActivity {
   
       @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_flutter_page);
           initViews();
       }
   
       private void initViews() {
           FlutterView flutterView = new FlutterView(this);
           FrameLayout.LayoutParams lp =  new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
           FrameLayout flContainer = findViewById(R.id.fl_container);
           flContainer.addView(flutterView, lp);
           FlutterEngine flutterEngine = new FlutterEngine(this);
           flutterEngine.getNavigationChannel().setInitialRoute("route1");
           flutterEngine.getDartExecutor().executeDartEntrypoint(
                   DartExecutor.DartEntrypoint.createDefault()
           );
           flutterView.attachToFlutterEngine(flutterEngine);
       }
   }
   ```

   - 因为io.flutter.facade，此处采用FlutterView（继承自FrameLayout）替代了原来的Flutter.createView();

   - **attachToFlutterEngine(FlutterEngine flutterEngine) ** 方法的作用就是flutter的ui显示到FlutterView中。

   - **FlutterEngine** 负责在android端执行Dart代码的引擎。

   - **flutterEngine.getNavigationChannel().setInitialRoute("route1");** 设置界面路由。如果不设置默认是“/”界面。

   - 传参可以采用类似get拼接的形式，例如：

     ```json
     "route1?{\"name\":\"LiLei\"}"
     ```

     将路由和参数采用?进行隔开，后续可以添加上json字符串。在Flutter 端解析的时候采用**window.defaultRouteName** 获取路由名称和参数。

     ```dart
     String url = window.defaultRouteName;
     // route名称
     String route =
         url.indexOf('?') == -1 ? url : url.substring(0, url.indexOf('?'));
     // 参数Json字符串
     String paramsJson =
         url.indexOf('?') == -1 ? '{}' : url.substring(url.indexOf('?') + 1);
     // 解析参数
     Map<String, dynamic> params = json.decode(paramsJson);
     ```

   - 在flutter中路由的简单写法。

     ```dart
     void main() => runApp(_widgetForRoute(window.defaultRouteName));
     Widget _widgetForRoute(String route) {
       switch (route) {
         case 'route1':
           return MyApp();
         default:
           return Center(
             child: Text('Unknown route: $route', textDirection: TextDirection.ltr),
           );
       }
     }
     ```

   - 

2. 创建好Activity后原生界面A 就可以采用Intent的方式进行跳转。

   ```java
   Intent intent = new Intent(MainActivity.this, FlutterPageActivity.class);
   startActivity(intent);
   ```

#### 二、Fragment形式

1. 新建一个Fragment 继承自Fragment。

2. 在fragment的onCreate方法中创建FlutterView。具体方法和Activity中相同。

   ```java
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
      return flutterView;
    }
   ```

   - 这里将FlutterEngine方法设置为成员变量为了后续界面间传值。

3. 在原生Activity中添加加载方法。

   ```java
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
   
           //写法2
           FlutterFragment fragment = FlutterFragment.withNewEngine().initialRoute("route1").build();
           getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commit();
   
           //写法3
           //通过FlutterFragment引入Flutter编写的页面
           FlutterFragment flutterFragment = FlutterFragment.withNewEngine()
                   .initialRoute("route2")
                   .build();
           getSupportFragmentManager()
                   .beginTransaction()
                   .replace(R.id.fl_container, flutterFragment)
                   .commit();
       }
   }
   ```

   - 写法3中 在androidx环境中 flutterFragment需要继承自androidx的fragment。

### Flutter界面跳转到Android原生界面

1. 在布局中添加一个按钮。

   ```dart
   static const nativeChannel = const MethodChannel('com.example.flutter/native');
   ```

   首先要定义一个channel，全局唯一，需要和android端约定好。

   ```dart
   RaisedButton(
        child: Text('跳转到原生界面'),
        onPressed:() {
        // 返回给上一页的数据
        Map<String, dynamic> result = {'name': '我从Flutter页面过来了'};
        nativeChannel.invokeMethod('jumpToNative', result);
   }),
   ```

   - dart采用map形式发送。定义好key value
   - 通过**nativeChannel.invokeMethod('jumpToNative', result);** 方法传递出去。其中第一个参数是和android端预定好的方法名。

2. 在fragment中进行接收

   ```java
   private static final String CHANNEL_NATIVE = "com.example.flutter/native";
   ```

   ```java
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
                       		//因为写的demo所以直接采用了魔法数字 方便文章中看的直观
                           startActivityForResult(jumpToNativeIntent, 1001);
                           break;
                       default:
                           result.notImplemented();
                           break;
                   }
               }
           });
       }
   ```

   在实现Flutter页面跳转Android原生页面之前首先介绍一下**Platform Channel**，它是Flutter和原生通信的工具，有三种类型：

   - **BasicMessageChannel**：用于传递字符串和半结构化的信息，Flutter和平台端进行消息数据交换时候可以使用。
   - **MethodChannel**：用于传递方法调用（method invocation），Flutter和平台端进行直接方法调用时候可以使用。
   - **EventChannel**：用于数据流（event streams）的通信，Flutter和平台端进行事件监听、取消等可以使用。

   Flutter 跳转到原生界面主要通过MethodChannel来实现。

   - 在创建MethodChannel的时候需要传入一个常量。需要保证唯一性。和dart端约定好采用一样的常量。就是上文中提到的**nativeChannel** 。
   - 第一个参数是BinaryMessenger类型。需要通过mFlutterEngine.getDartExecutor()方法获取到。
     - 这里有两个前提：
       - a. 需要界面加载完成后。所以我们写在onViewCreated中；
       - b. 需要从flutterEngine中获取，所以我们将该变量改成成员变量。
   - 通过setMethodCallHandler回调方式判断返回的方法名。在进行相应处理。
   - 获取到想要的方法名之后，使用intent将获取到的数据发送出去。
   - 这里采用**startActivityForResult** 方法是因为在原生界面中做了数据回调。

### 从android原生界面返回到Flutter界面数据传递

 1. 接上面的跳转原生界面成功后，在原生界面中添加按钮进行返回并传值。

    ```java
    tv_demo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("message", "我从原生页面回来了");
                    setResult(RESULT_OK, intent);
                    finish();
                }
    });
    ```

 2. 在上诉的fragment的**onActivityResult**方法中进行接收。

    ```java
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
    ```

    - 调用了flutter中的方法将值传入到flutter中并显示在界面上。

    - 定义一个全局唯一常量，和flutter中约定好。

      ```java
      private static final String CHANNEL_FLUTTER = "com.example.flutter/flutter";
      ```

      ```dart
      static const flutterChannel = const MethodChannel('com.example.flutter/flutter');
      ```

    - 在initState中进行接收

      ```dart
      String _backResult = "初步设置";
      @override
        void initState() {
          super.initState();
          Future<dynamic> handler(MethodCall call) async {
            switch (call.method) {
              case 'onActivityResult':
              // 获取原生页面传递的参数
                print(call.arguments['message']);
                setState(() {
                  _backResult = call.arguments['message'];
                });
                break;
            }
          }
          flutterChannel.setMethodCallHandler(handler);
        }
      ```

      用变量来记录

    - 新建一个text来显示这label

      ```dart
      Text(
         '$_backResult'
      ),
      ```

    - 

### Flutter界面返回到android原生界面数据传递

1. 新建一个返回按钮

   ```dart
   RaisedButton(
         child: Text('返回上一页'),
         onPressed: () {
         // 返回给上一页的数据
         Map<String, dynamic> result = {'message': '我从Flutter页面回来了'};
         nativeChannel.invokeMethod('goBackWithResult', result);
   }),
   ```

   并执行返回的方法。方法名和key提前约定好。

2. 原生fragmen中先接收相应的参数，然后使用intent进行传递。

   ```java
   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
           super.onViewCreated(view, savedInstanceState);
           MethodChannel nativeChannel = new MethodChannel(mFlutterEngine.getDartExecutor(), CHANNEL_NATIVE);
           nativeChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
               @Override
               public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
                   switch (methodCall.method) {
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
   ```

   

3. 在上一个原生界面的activity中进行接收信息。

   ```java
   @Override
   protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
           super.onActivityResult(requestCode, resultCode, data);
           if (resultCode == RESULT_OK) {
               tv_result.setVisibility(View.VISIBLE);
               String result = data.getStringExtra("message");
               tv_result.setText("result" + result);
           }
    }
   ```

   
