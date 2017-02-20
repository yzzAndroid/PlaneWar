package a6he.android.yzz.com.planewar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yzz on 2017/2/18 0018.
 */
public class PlanWarView extends SurfaceView implements SurfaceHolder.Callback, Runnable {


    private SurfaceHolder mHolder;
    private ExecutorService mThradPool;
    private Bg bg;
    private Rect mRect;
    private Canvas mCanvas;
    private Paint mPaint;
    //承载所有游戏相关的图片
    private Bitmap mPicture;

    //装在所有的图片
    private List<ImageErji> listErji;

    private boolean isGameover = false;

    //背景图片
    private Bitmap mBackGroungBitMap;
    //用户飞机
    private Bitmap mUserBitMap;
    //用户飞机需要裁剪
    private List<Bitmap> mUserLisrt;
    //敌机
    private Bitmap mEnemyBitMap;
    private List<Bitmap> mEnemyBitMapList;

    private Random mRandom;
    //敌机的集合
    private List<Enemy> mEnemyList;
    //子弹的图片
    private Bitmap mZiDuanBitMap;
    //子弹的集合
    private List<ZiDan> mZiDanList;

    //触摸事件的开始位置
    private Point startTuchPoint;

    private UserPlane mUserPlane;
    private boolean isUserUseful = true;


    public PlanWarView(Context context) {
        super(context);
        init();
    }

    public PlanWarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlanWarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mThradPool = Executors.newFixedThreadPool(5);
        mRect = new Rect(0, 0, getWidth(), getHeight());
        mPaint = new Paint();
        mPicture = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mBackGroungBitMap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg);
        mUserBitMap = BitmapFactory.decodeResource(getResources(), R.mipmap.ship2);
        mUserLisrt = new ArrayList<>();

        mEnemyBitMap = BitmapFactory.decodeResource(getResources(), R.mipmap.enemy2);
        mEnemyBitMapList = new ArrayList<>();
        int w = mUserBitMap.getWidth();
        int h = mUserBitMap.getHeight();
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                mUserLisrt.add(Bitmap.createBitmap(mUserBitMap, 0, 0, w / 4, h));
                mEnemyBitMapList.add(Bitmap.createBitmap(mEnemyBitMap, 0, 0, w / 4, h));
            } else {
                mUserLisrt.add(Bitmap.createBitmap(mUserBitMap, w / 4 * i, 0, w / 4, h));
                mEnemyBitMapList.add(Bitmap.createBitmap(mEnemyBitMap, w / 4 * i, 0, w / 4, h));
            }
        }
        mEnemyList = new ArrayList<>();
        mZiDuanBitMap = BitmapFactory.decodeResource(getResources(), R.mipmap.meci_neprijatelji);
        mZiDanList = new ArrayList<>();
        mRandom = new Random();
        bg = new Bg();
        mUserPlane = new UserPlane();
        mCanvas = new Canvas();
        listErji = new ArrayList<>();
        //先加入背景
        listErji.add(bg);
        listErji.add(mUserPlane);
        startTuchPoint = new Point();
        mThradPool.execute(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        int flag = 0;
        int flagE = 100;
        while (!isGameover) {

            //检查子弹和敌船是否相撞
            List<ZiDan> ziDanCop = new ArrayList<>();
            for (int i = 0; i <mZiDanList.size() ; i++) {
                ziDanCop.add(mZiDanList.get(i));
                mZiDanList.get(i).CheckPengZhuang(ziDanCop);
            }
            //复制的子弹集合贴到当前集合中
            mZiDanList = ziDanCop;

            //循环绘制图片，将图片贴到mPicture上
            for (ImageErji imageErji : listErji) {
                Bitmap b = imageErji.draw();
                mCanvas.setBitmap(mPicture);
                mCanvas.drawBitmap(b, imageErji.getX(), imageErji.getY(), mPaint);
            }


            creatEnemyPoint();
            //循环绘制敌机
            for (Enemy e : mEnemyList) {
                Bitmap b = e.draw();
                mCanvas.setBitmap(mPicture);
                mCanvas.drawBitmap(b, e.getX(), e.getY(), mPaint);
                e.setY(e.getY() + getMeasuredHeight()/flagE);
            }

            if (flag==2) {
                creatZiDan();
            }


            //循环绘制子弹
            for (ZiDan z : mZiDanList) {
                Bitmap b = z.draw();
                mCanvas.setBitmap(mPicture);
                mCanvas.drawBitmap(b, z.getX(), z.getY(), mPaint);
                z.setY(z.getY()-getMeasuredHeight()/50);
            }


            //移除屏幕外的子弹
            moveZiDan();
            //将mPicture贴到View上
            Canvas ca = mHolder.lockCanvas(mRect);
            ca.drawBitmap(mPicture, 0, 0, mPaint);
            mHolder.unlockCanvasAndPost(ca);

            //线程循环
            flag++;
            flagE--;
            if (flagE<10){
                flagE = 100;
            }
            if (flag>2){
                flag = 0;
            }
            //移除屏幕外的敌机
            moveEmemy();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveEmemy(){
        //移除屏幕外的
        if (mEnemyList.size()==0){
            return;
        }
            float y = mEnemyList.get(0).getY();
            if (y > getMeasuredHeight()) {
                mEnemyList.get(0).recycle();
                mEnemyList.remove(0);
                reStart();
                return;
                //moveEmemy();
            }
    }

    private void reStart() {
        mEnemyList.clear();
        mZiDanList.clear();
        mUserPlane.reset();
        bg.reset();
    }


    private void creatEnemyPoint() {
        boolean isNeed = false;
        //找最近的
        if (mEnemyList.size() == 0) {
            isNeed = true;
        } else {
            Enemy e = mEnemyList.get(mEnemyList.size() - 1);
            if (e.getY() > mEnemyBitMap.getHeight()) {
                isNeed = true;
            }
        }

        if (isNeed) {
            //最大飞机的数量
            int num = mRandom.nextInt(getMeasuredWidth() / mEnemyBitMap.getWidth());
            int countW;
            //把横向高度分成num份，产生的随机数加上前面的num份距离
            if (num == 0) {
                countW = getMeasuredWidth() - mEnemyBitMap.getWidth();
            } else {
                countW = getMeasuredWidth() / num;
            }
            for (int i = 0; i < num; i++) {
                //减去一个飞机的宽度
                int x = mRandom.nextInt(countW - mEnemyBitMap.getWidth()) + i * countW;
                Enemy ee = new Enemy();
                ee.setX(x);
                ee.setY(-mEnemyBitMap.getHeight());
                mEnemyList.add(ee);
            }

        }
    }

    private void creatZiDan() {

        boolean isNeed = true;
        if (mZiDanList.size()>0) {
            ZiDan zz = mZiDanList.get(mZiDanList.size() - 1);
            if (zz.getY()<mUserPlane.getY()-mZiDuanBitMap.getHeight()){
                isNeed = true;
            }else {
                isNeed = false;
            }
        }
        if (isNeed) {
            //生产子弹
            float xx = mUserPlane.getX();
            float yy = mUserPlane.getY();
            float positionX =xx+(mUserBitMap.getWidth()/4-mZiDuanBitMap.getWidth())/2;
            ZiDan z = new ZiDan();
            z.setX(positionX);
            z.setY(yy - mZiDuanBitMap.getHeight());
            mZiDanList.add(z);
        }

    }

    private void moveZiDan(){
        //移除子弹
        if (mZiDanList.size()==0){
            return;
        }
            ZiDan zz = mZiDanList.get(0);
            if (zz.getY() <-mZiDuanBitMap.getHeight()) {
                mZiDanList.remove(0);
                zz.recycle();
                //递归
                moveZiDan();
            } else {
                return;
            }

    }




    class Bg implements ImageErji {
        private Bitmap newBitMap = null;
        private int hight = 0;
        private float x;
        private float y;

        public Bg() {
            newBitMap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public Bitmap draw() {
            mCanvas.setBitmap(newBitMap);
            mCanvas.drawBitmap(mBackGroungBitMap, new Rect(0, 0, mBackGroungBitMap.getWidth(), mBackGroungBitMap.getHeight())
                    , new Rect(0, -getMeasuredHeight() + hight, getMeasuredWidth(), hight), mPaint);

            mCanvas.drawBitmap(mBackGroungBitMap, new Rect(0, 0, mBackGroungBitMap.getWidth(), mBackGroungBitMap.getHeight()),
                    new Rect(0, hight, getMeasuredWidth(), hight + getMeasuredHeight()), mPaint);
            hight+=10;
            if (hight >= getMeasuredHeight()) {
                hight = 0;
            }
            return newBitMap;
        }

        public void reset(){
            hight = 0;
        }

        @Override
        public void recycle() {
            newBitMap.recycle();
        }
    }


    class UserPlane implements ImageErji {
        private Bitmap newBitMap;
        private int flag = 0;
        private float x;
        private float y;

        public UserPlane() {
            newBitMap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            //设置初始位置
            y = getMeasuredHeight() - mUserBitMap.getHeight();
            x = (getMeasuredWidth() - mUserBitMap.getWidth() / 4) / 2;
        }


        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        public boolean check() {
            if (startTuchPoint.x < x || startTuchPoint.x > (x + mUserBitMap.getWidth() / 4)) {
                return false;
            }

            if (startTuchPoint.y < y || startTuchPoint.y > y + mUserBitMap.getHeight()) {
                return false;
            }
            return true;
        }

        //设置userPlane的坐标
        public void resetXY(float x, float y) {
            float xx = x - mUserBitMap.getWidth() / 8;
            float yy = y - mUserBitMap.getHeight() / 2;
            //边界检查
            if (xx < 0 || xx > getMeasuredWidth() - mUserBitMap.getWidth() / 4) {
                return;
            }

            if (yy < 0 || yy > getMeasuredHeight() - mUserBitMap.getHeight()) {
                return;
            }
            //设置
            setX(xx);
            setY(yy);
        }

        @Override
        public Bitmap draw() {
            mCanvas.setBitmap(newBitMap);
            Bitmap b = mUserLisrt.get(flag / 10);
            mCanvas.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight())
                    , new Rect(0, 0, b.getWidth(), b.getHeight()), mPaint);
            flag++;
            if (flag == 40) {
                flag = 0;
            }
            return newBitMap;
        }

        @Override
        public void recycle() {
            newBitMap.recycle();
        }

        public void reset() {
            y = getMeasuredHeight() - mUserBitMap.getHeight();
            x = (getMeasuredWidth() - mUserBitMap.getWidth() / 4) / 2;
        }
    }

    class Enemy implements ImageErji {
        private Bitmap newBitMap;
        private int flag = 0;
        private float x;
        private float y;

        public Enemy() {
            newBitMap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public Bitmap draw() {
            mCanvas.setBitmap(newBitMap);
            Bitmap b = mEnemyBitMapList.get(flag / 10);
            mCanvas.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight())
                    , new Rect(0, 0, b.getWidth(), b.getHeight()), mPaint);
            flag++;
            if (flag == 40) {
                flag = 0;
            }

            return newBitMap;
        }

        public void reset(){
            flag = 0;
        }

        @Override
        public void recycle() {
            newBitMap.recycle();
        }


    }

    //子弹
    class ZiDan implements ImageErji {
        private Bitmap newBitMap;
        private float x;
        private float y;
        private boolean isDelete = false;

        public boolean isDelete() {
            return isDelete;
        }

        public void setDelete(boolean delete) {
            isDelete = delete;
        }

        public ZiDan() {
            newBitMap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        @Override
        public Bitmap draw() {
            mCanvas.setBitmap(newBitMap);
            mCanvas.drawBitmap(mZiDuanBitMap, 0, 0, mPaint);
            return newBitMap;
        }

        @Override
        public void recycle() {
            newBitMap.recycle();
        }

        private boolean CheckPengZhuang(List<ZiDan> list){
            for (int i = 0; i <mEnemyList.size() ; i++) {
                Enemy e = mEnemyList.get(i);
                if (x+mZiDuanBitMap.getWidth()<e.getX()||x>e.getX()+mEnemyBitMap.getWidth()/4){
                    continue;
                }
                if (y-e.getY()+mEnemyBitMap.getHeight()<=0){
                    mEnemyList.remove(i);
                    e.recycle();
                    list.remove(this);
                    recycle();
                    return true;
                }
            }
            return false;
        }
    }


    public interface ImageErji {
        float getX();

        float getY();

        Bitmap draw();

        //销毁BitMap
        void recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isUserUseful = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTuchPoint.set(event.getX(), event.getY());
                if (mUserPlane.check()) {
                    isUserUseful = true;
                } else {
                    isUserUseful = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isUserUseful) {
                    //选中User的飞机,让飞机在点击位置的中间位置
                    mUserPlane.resetXY(event.getX(), event.getY());
                }

                break;
            case MotionEvent.ACTION_UP:
                break;

        }
        return true;
    }

    class Point {
        float x;
        float y;

        public Point() {
        }

        public void set(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }
}
