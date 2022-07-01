package bacy.game.snack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

public class SnackView extends View {
    CommonData commonData;
    Paint snack_paint, eye_paint, wall_paint, point_paint;
    Paint text_paint, text2_paint, start_paint;
    Canvas canvas;
    Context context;
    AlertDialog alertDialog;

    final int offest_x = 9; 
    final int offest_y = 25; 
    final int wallWidth = 8; 
    final int fontSize = 85;
    final int bodyNum = 26; 
    final int bodyRadius = 15, foodRadius = 14;

    int head_x, head_y;
    int head_next_x, head_next_y;
    int clickPos_x, clickPos_y;
    int eatedfood;
    int food_x, food_y;

    List<Integer> snack_x, snack_y;
    List<Integer> snack_x_new, snack_y_new;

    static Runnable gameRunnable, timeRunnable;
    Thread gameThread, timeThread;

    Rect rect;

    boolean isDead, newFood;
    boolean isPause;

    public SnackView(Context context) {
        super(context);
        this.context = context;
        commonData = new CommonData();
        CommonData.bodySize = CommonData.screenWidth / bodyNum;
        CommonData.gameTime = 0;
        isDead = false;
        isPause = false;
        newFood = true;
        eatedfood = 0;
        snack_x_new = new ArrayList<Integer>();
        snack_y_new = new ArrayList<Integer>();
        snack_x = new ArrayList<Integer>();
        snack_y = new ArrayList<Integer>();
        initPaint();
        initSnack();
        initRect();
        // 20210708: 使用new Runnable()
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDead) {
                    Log.i("!!!! gameThread isDead="," true");
                    gameThread.interrupt();
                }
                while (!isDead && !isPause) {
                    //间隔500毫秒
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        gameThread.interrupt();
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                    postInvalidate();

                }
            }
        };
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDead) {
                    timeThread.interrupt();
                }
                while (!isDead) {
                    if (timeThread.isInterrupted()) {
                        break;
                    }
                    // 间隔1秒
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    CommonData.gameTime++;
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }
        };
        runGame();

    }

    final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                invalidate(); //刷新界面
            } else if (msg.what == 2) {
                invalidate(rect);
            }
        }
    };


    private void initPaint() {
        snack_paint = new Paint();
        text2_paint = new Paint();
        wall_paint = new Paint();
        point_paint = new Paint();
        text_paint = new Paint();
        start_paint = new Paint();
        eye_paint = new Paint();
        snack_paint.setColor(Color.parseColor("#81ff90"));
        snack_paint.setAntiAlias(true);
        snack_paint.setDither(true);
        snack_paint.setStyle(Paint.Style.FILL);
        snack_paint.setStrokeWidth(0.5f);
        eye_paint.setAntiAlias(true);
        eye_paint.setColor(Color.BLACK);
        eye_paint.setDither(true);
        text2_paint.setColor(Color.LTGRAY);
        text2_paint.setStyle(Paint.Style.FILL);
        text2_paint.setAntiAlias(true);
        text2_paint.setDither(true);
        text2_paint.setTextSize(50f);
        point_paint.setColor(Color.parseColor("#ffab95"));
        point_paint.setAntiAlias(true);
        point_paint.setDither(true);
        point_paint.setStyle(Paint.Style.FILL);
        point_paint.setStrokeWidth(1f);
        wall_paint.setColor(Color.BLUE);
        wall_paint.setAntiAlias(true);
        wall_paint.setDither(true);
        wall_paint.setStyle(Paint.Style.STROKE);
        wall_paint.setStrokeWidth(wallWidth);
        text_paint.setAntiAlias(true);
        text_paint.setDither(true);
        text_paint.setColor(Color.GREEN);
        text_paint.setTextSize(fontSize);
        start_paint.setAntiAlias(true);
        start_paint.setDither(true);
        start_paint.setColor(Color.RED);
        start_paint.setTextSize(fontSize+55);
    }

    private void initSnack() {
        int ix = commonData.makeRandom(2,19);
        int iy = commonData.makeRandom(2,19);
        snack_x.add(ix); 
        snack_y.add(iy);
        Log.i("init snack head x = ",String.valueOf(ix));
        Log.i("init snack head y = ",String.valueOf(iy));
        CommonData.snackLength = 3; 

        int x = snack_x.get(0);
        int y = snack_y.get(0);
        snack_x.add(x-1);
        snack_x.add(x-2);

        snack_y.add(y);
        snack_y.add(y);
        snack_x_new = snack_x;
        snack_y_new = snack_y;
        CommonData.moveDirection = 3;
    }

    private void initRect() {
        int x = 35;
        int y = (offest_y+bodyNum*CommonData.bodySize)+375;
        rect = new Rect(x,y,x+fontSize,y);
    }

    private void drawWall(Canvas canvas) {
        canvas.drawLine(offest_x,offest_y, offest_x+bodyNum*CommonData.bodySize,offest_y,wall_paint); 
        canvas.drawLine(offest_x,offest_y+bodyNum*CommonData.bodySize, offest_x+bodyNum*CommonData.bodySize,offest_y+bodyNum*CommonData.bodySize,wall_paint); 
        canvas.drawLine(offest_x,offest_y, offest_x,offest_y+bodyNum*CommonData.bodySize,wall_paint); 
        canvas.drawLine(offest_x+bodyNum*CommonData.bodySize,offest_y,offest_x+bodyNum*CommonData.bodySize,offest_y+CommonData.bodySize*bodyNum,wall_paint); 
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
        moveSnack(); // 控制方向
        setNextHead();
        checkBoundary();
        checkEatfood();
        checkEatMyself();
        drawWall(canvas);
        drawSnack(canvas);
        drawButton(canvas);
        drawFood(canvas);
        drawTime(canvas);
        drawEatfood(canvas);
    }

    private void drawSnack(Canvas canvas) {
        Log.i("!!!! is drawSnack()"," ");
        CommonData.snackLength = snack_x.size();
        int x = 0, y = 0;
        if (!isDead) {
            for (int i=0; i<CommonData.snackLength; i++) {
                x = snack_x_new.get(i);
                y = snack_y_new.get(i);
                
                //canvas.drawRect((offest_x+wallWidth)+CommonData.bodySize*x,(offest_y+wallWidth)+commonData.bodySize*y,(offest_x+wallWidth)+CommonData.bodySize*x+CommonData.bodySize,(offest_y+wallWidth)+CommonData.bodySize*y+CommonData.bodySize,snack_paint);
                
                canvas.drawCircle(((offest_x+wallWidth)+CommonData.bodySize*x)+(CommonData.bodySize/2),((offest_y+wallWidth)+CommonData.bodySize*y+CommonData.bodySize)-CommonData.bodySize/2,bodyRadius,snack_paint);
            }
        } else {
            for (int i=0; i<CommonData.snackLength; i++) {
                x = snack_x_new.get(i);
                y = snack_y_new.get(i);
                
                switch (CommonData.moveDirection) {
                    case 0:
                        y = y + 1;
                        break;
                    case 1:
                        y = y - 1;
                        break;
                    case 2:
                        x = x + 1;
                        break;
                    case 3:
                        x = x - 1;
                        break;
                }
                //canvas.drawRect((offest_x+wallWidth)+CommonData.bodySize*x,(offest_y+wallWidth)+commonData.bodySize*y,(offest_x+wallWidth)+CommonData.bodySize*x+CommonData.bodySize,(offest_y+wallWidth)+CommonData.bodySize*y+CommonData.bodySize,snack_paint);
                canvas.drawCircle(((offest_x+wallWidth)+CommonData.bodySize*x)+(CommonData.bodySize/2),((offest_y+wallWidth)+CommonData.bodySize*y+CommonData.bodySize)-CommonData.bodySize/2,bodyRadius,snack_paint);
            }
        }
    }

    private void moveSnack() {
        if (!isDead) {
            snack_x = snack_x_new;
            snack_y = snack_y_new;
            head_x = snack_x.get(0);
            head_y = snack_y.get(0);

            if (CommonData.moveDirection == 0) {
                moveUp();
            } else if (CommonData.moveDirection == 1) {
                moveDown();
            } else if (CommonData.moveDirection == 2) {
                moveLeft();
            }  else if (CommonData.moveDirection == 3) {
                moveRight();
            }
        } else {
            Log.i("!!!! is moveSnack() dead!"," ");
        }

    }

    private void setNextHead() {
        snack_x_new.set(0, head_next_x); 
        snack_y_new.set(0, head_next_y);
    }

    private void moveUp() {
        List<Integer> new_x = new ArrayList<Integer>();
        List<Integer> new_y = new ArrayList<Integer>();
        head_next_x = head_x; 
        head_next_y = head_y - 1; 
        for (int k=0;k<CommonData.snackLength;k++) {
            new_x.add(snack_x.get(k));
            new_y.add(snack_y.get(k));
            if (k !=0 ) {
                int index = k - 1;
                int x = new_x.get(index);
                int y = new_y.get(index);
                setNewPos(k, x, y);
            }
        }
    }

    private void moveDown() {
        List<Integer> new_x = new ArrayList<Integer>();
        List<Integer> new_y = new ArrayList<Integer>();
        head_next_x = head_x;
        head_next_y = head_y + 1; 
        for (int k=0;k<CommonData.snackLength;k++) {
            new_x.add(snack_x.get(k));
            new_y.add(snack_y.get(k));
            if (k !=0 ) {
                int index = k - 1;
                int x = new_x.get(index);
                int y = new_y.get(index);
                setNewPos(k, x, y);
            }
        }
    }

    private void moveLeft() {
        List<Integer> new_x = new ArrayList<Integer>();
        List<Integer> new_y = new ArrayList<Integer>();
        head_next_x = head_x - 1; 
        head_next_y = head_y; 
        for (int k=0;k<CommonData.snackLength;k++) {
            new_x.add(snack_x.get(k));
            new_y.add(snack_y.get(k));
            if (k !=0 ) {
                int index = k - 1;
                int x = new_x.get(index);
                int y = new_y.get(index);
                setNewPos(k, x, y);
            }
        }
    }

    private void moveRight() {
        List<Integer> new_x = new ArrayList<Integer>(); 
        List<Integer> new_y = new ArrayList<Integer>(); 
        head_next_x = head_x + 1; 
        head_next_y = head_y; 
        for (int k=0; k<CommonData.snackLength; k++) {
            new_x.add(snack_x.get(k));
            new_y.add(snack_y.get(k));
            if (k !=0 ) {
                int index = k - 1;
                int x = new_x.get(index);
                int y = new_y.get(index);
                setNewPos(k, x, y);
            }
        }
    }

    protected void setNewPos(int index, int x, int y) {
        snack_x_new.set(index, x);
        snack_y_new.set(index, y);
    }

    private void checkBoundary() {
        int x = snack_x_new.get(0);
        int y = snack_y_new.get(0);
        if (x < 0 || x >= bodyNum) {
            isDead = true;
            this.handler.removeCallbacks(gameRunnable);
            gameThread.interrupt();
            gameOverDialog();
        }
        if (y < 0 || y >= bodyNum) {
            isDead = true;
            this.handler.removeCallbacks(gameRunnable);
            gameThread.interrupt();
            gameOverDialog();
        }
    }

    private void drawButton(Canvas canvas) {
        int y = (offest_y+bodyNum*CommonData.bodySize)+150;
        int x= CommonData.screenWidth/2;
        canvas.drawText("↑",x,y,text_paint); // x,y为左下角坐标
        canvas.drawText("↓",x,y+250,text_paint);
        canvas.drawText("←",x-300,y+125,text_paint);
        canvas.drawText("→",x+300,y+125,text_paint);
        canvas.drawText("||", x+170, y+270, start_paint);
        canvas.drawText("↻",x+275,y+270,start_paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clickPos_x = (int)event.getX(); // 2017-05-17 change getRawX to getX
                clickPos_y = (int)event.getY();
                checkClick();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void checkClick() {
        int y = (offest_y+bodyNum*CommonData.bodySize)+150;
        int x = CommonData.screenWidth/2;
        if (clickPos_y >= y-fontSize && clickPos_y <= y) {
            if (clickPos_x >= x && clickPos_x < x+fontSize) {
                Log.i("!!!! click UP!! "," ");
                if (CommonData.moveDirection != 0 && CommonData.moveDirection != 1) {
                    CommonData.moveDirection = 0;
                }
            }
        } else if (clickPos_y >= y+250-fontSize && clickPos_y <= y+250) {
            if (clickPos_x >= x && clickPos_x < x+fontSize) {
                Log.i("!!!! click DOWN!! "," ");
                if (CommonData.moveDirection != 0 && CommonData.moveDirection != 1) {
                    CommonData.moveDirection = 1;
                }
            } else if (clickPos_x >= x+170 && clickPos_x <= x+170+100) {
                isPause = true;
            } else if (clickPos_x >= x+275 && clickPos_x <= x+275+130) {
                Log.i("!!!! click Restart!! "," ");
                reStart();
            }
        } else if (clickPos_y >= y+125-fontSize && clickPos_y <= y+125) {
            Log.i("click y is OK = ",String.valueOf(clickPos_y));
            if (clickPos_x >= x-300 && clickPos_x < x-300+fontSize) {
                Log.i("!!!! click LEFT!! "," ");
                if (CommonData.moveDirection != 2 && CommonData.moveDirection != 3) {
                    CommonData.moveDirection = 2;
                }
            } else if (clickPos_x >= x+300 && clickPos_x < x+300+fontSize) {
                Log.i("!!!! click RIGHT!! "," ");
                if (CommonData.moveDirection != 2 && CommonData.moveDirection != 3) {
                    CommonData.moveDirection = 3;
                }
            }
        }
    }

    private void checkEatMyself() {
        int x0 = snack_x_new.get(0);
        int y0 = snack_y_new.get(0);
        Log.i("!!!! checkEatMyself() x0 = ",String.valueOf(x0));
        Log.i("!!!! checkEatMyself() y0 = ",String.valueOf(y0));
        for (int i=1; i<commonData.snackLength; i++) {
            int x = snack_x_new.get(i);
            int y = snack_y_new.get(i);
            if (x == x0 && y == y0) {
                isDead = true;
                Log.i("!!!! checkEatMyself() "," true");
                gameOverDialog();
            }
        }
    }

    private void drawFood(Canvas canvas) {
        int food_last_x = food_x;
        int food_last_y = food_y;
        int fx = commonData.makeRandom(1,20);
        int fy = commonData.makeRandom(1,20);

        if (newFood && isDead) {
            if (fx == food_last_x) {
                fx = commonData.makeRandom(1,20);
            }
            if (fy == food_last_y) {
                fy = commonData.makeRandom(1,20);
            }
            food_x = fx;
            food_y = fy;
            //canvas.drawRect((offest_x+wallWidth)+CommonData.bodySize*food_x,(offest_y+wallWidth)+commonData.bodySize*food_y,(offest_x+wallWidth)+commonData.bodySize*food_x+commonData.bodySize,(offest_y+wallWidth)+commonData.bodySize*food_y+commonData.bodySize,point_paint);
            canvas.drawCircle((offest_x+wallWidth)+CommonData.bodySize*food_x,(offest_y+wallWidth)+commonData.bodySize*food_y, foodRadius, point_paint);
            newFood = false;
            return;
        }
        if (newFood && fx != snack_x_new.get(0) && fy != snack_y_new.get(0)) {
            food_x = fx;
            food_y = fy;
        }
        //canvas.drawRect((offest_x+wallWidth)+CommonData.bodySize*food_x,(offest_y+wallWidth)+commonData.bodySize*food_y,(offest_x+wallWidth)+commonData.bodySize*food_x+commonData.bodySize,(offest_y+wallWidth)+commonData.bodySize*food_y+commonData.bodySize,point_paint);
        canvas.drawCircle((offest_y+wallWidth)+commonData.bodySize*food_x,(offest_y+wallWidth)+commonData.bodySize*food_y, foodRadius, point_paint);
        newFood = false;
    }

    private void checkEatfood() {
        int hx = snack_x_new.get(0);
        int hy = snack_y_new.get(0);
        int size = CommonData.snackLength;

        if (hx == food_x && hy == food_y) {
            List<Integer> body_x = new ArrayList<>();
            List<Integer> body_y = new ArrayList<>();
            newFood = true;
            eatedfood++;

            switch (CommonData.moveDirection) {
                case 0:
                    body_x.add(hx);
                    body_y.add(hy - 1);
                    for (int i = 0; i < size; i++) {
                        body_x.add(snack_x_new.get(i));
                        body_y.add(snack_y_new.get(i));
                    }
                    break;
                case 1:
                    body_x.add(hx);
                    body_y.add(hy + 1);
                    for (int i = 0; i < size; i++) {
                        body_x.add(snack_x_new.get(i));
                        body_y.add(snack_y_new.get(i));
                    }
                    break;
                case 2:
                    body_x.add(hx - 1);
                    body_y.add(hy);
                    for (int i = 0; i < size; i++) {
                        body_x.add(snack_x_new.get(i));
                        body_y.add(snack_y_new.get(i));
                    }
                    break;
                case 3:
                    body_x.add(hx + 1);
                    body_y.add(hy);
                    for (int i = 0; i < size; i++) {
                        body_x.add(snack_x_new.get(i));
                        body_y.add(snack_y_new.get(i));
                    }
                    break;
                default:
                    break;
            }
            snack_x_new = body_x;
            snack_y_new = body_y;
            snack_x = body_x;
            snack_y = body_y;
            CommonData.snackLength++;
        }
    }

    private void drawTime(Canvas canvas) {
        String Time;
        int Min, Sec;
        int y = (offest_y+bodyNum*CommonData.bodySize)+150;
        if (CommonData.gameTime <= 59) {
            Time = String.valueOf(CommonData.gameTime) + "秒";
        } else {
            Min = (int)CommonData.gameTime / 60;
            Sec = (int)CommonData.gameTime % 60;
            Time = String.valueOf(Min) + "分" + String.valueOf(Sec) + "秒";
        }
        String gtime = "时间："+String.valueOf(Time);
        canvas.drawText(gtime, 35, y+325, text2_paint);
    }

    private void drawEatfood(Canvas canvas) {
        int y = (offest_y+bodyNum*CommonData.bodySize)+150;
        String Eat = "吃到食物："+String.valueOf(eatedfood);
        canvas.drawText(Eat, 35, y+250, text2_paint);
    }

    private void reStart() {
        snack_x.clear();
        snack_y.clear();
        snack_x_new.clear();
        snack_y_new.clear();
        eatedfood = 0;
        isDead = true;
        try {
            gameThread.sleep(505); 
            timeThread.sleep(505);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gameThread.interrupt();
        timeThread.interrupt();
        newFood = true;
        drawFood(canvas);
        CommonData.gameTime = 0;
        initSnack();
        // 重启游戏
        runGame();
    }

    private void runGame() {
        Log.i("runGame ","创建游戏线程");
        gameThread = new Thread(gameRunnable);
        timeThread = new Thread(timeRunnable);
        gameThread.start();
        timeThread.start();
        isDead = false;
        isPause = false;
    }

    private void gameOverDialog() {
        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(this.getContext());
        alertdialogbuilder.setMessage("游戏结束！");
        alertdialogbuilder.setPositiveButton("再来一次", listener1);
        alertdialogbuilder.setNeutralButton("退出", listener2);
        alertdialogbuilder.show();
        alertDialog = alertdialogbuilder.create();
    }

    private DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            alertDialog.dismiss();
            reStart();
        }
    };

    private DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Activity activity = (Activity)(SnackView.super.getContext());
            activity.finish();
        }
    };
}
