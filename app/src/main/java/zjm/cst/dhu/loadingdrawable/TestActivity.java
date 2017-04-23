package zjm.cst.dhu.loadingdrawable;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rl_tset);
        //获取屏幕宽高
        Point point = new Point();
        WindowManager windowManager = getWindowManager();
        windowManager.getDefaultDisplay().getSize(point);
        int scrWidth = point.x;
        int scrHeight = point.y;

        LoadingViewGroup viewGroup = (LoadingViewGroup) findViewById(R.id.lvg_test);
        ImageView imageView = (ImageView) findViewById(R.id.iv_test);
        LoadingDrawable loadingDrawable = new LoadingDrawable(
                new WhorlLoadingRenderer.Builder(this)
                        .setWidth(scrWidth)
                        .setHeight(scrWidth)
                        .setCenterRadius(scrWidth / 4)
                        .setStrokeWidth(scrWidth / 16)
                        .build());
        //通过View.setBackground方式
        viewGroup.setBackground(loadingDrawable);
        //通过ImageView.setImageDrawable方式
        //imageView.setImageDrawable(loadingDrawable);
        //开始动画
        loadingDrawable.start();
    }
}
