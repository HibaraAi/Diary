package cn.snowt.drawboard;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.PopupWindowCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.io.IOException;
import java.io.OutputStream;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.LoginActivity;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.drawboard.view.DrawBoardView;
import cn.snowt.note.NoteActivity;

/**
 * @Author: HibaraAi
 * @Date: 2025-01-18 14:05
 * @Description: 画板、画布Activity
 * 整个画板功能参考：https://github.com/jenly1314/DrawBoard
 */
public class DrawBoardActivity extends AppCompatActivity {
    public static final Integer OPEN_FROM_MAIN_ACTIVITY = 1;  //从主界面打开的
    public static final Integer OPEN_FROM_BEFORE_LOGIN = 2;  //登录之前就已经打开
    public static final String OPEN_FROM = "open_from";
    //记录这个open_from的值
    private Integer OPEN_FROM_VALUE = 0;


    private boolean isGreen = false;

    private PopupWindow popup = null;

    ImageView ivDrawMode;
    ImageView ivPen;
    ImageView ivClear;
    ImageView ivUndo;
    ImageView ivRedo;
    ImageView ivSave;

    String penText;


    cn.snowt.drawboard.view.DrawBoardView drawBoardView;
    Context context = DrawBoardActivity.this;

    private View popContentView;


    private void bindingView() {
        ivDrawMode = findViewById(R.id.ivDrawMode);
        ivPen = findViewById(R.id.ivPen);
        ivClear = findViewById(R.id.ivClear);
        ivUndo = findViewById(R.id.ivUndo);
        ivRedo = findViewById(R.id.ivRedo);
        ivSave = findViewById(R.id.ivSave);
        drawBoardView = findViewById(R.id.drawBoardView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_board);
        bindingView();
        OPEN_FROM_VALUE = getIntent().getIntExtra(OPEN_FROM,0);
        popContentView = LayoutInflater.from(this).inflate(R.layout.pop_select, null);
        popContentView.measure(0, 0);
        ivDrawMode.measure(0, 0);

        // 绘制直线时，是否带箭头
        // binding.drawBoardView.isDrawLineArrow = true;

        drawBoardView.setShowSelectedBox(true);
        drawBoardView.setPaintColor(Color.RED);
        penText = new String("文本");
        drawBoardView.setDrawText(penText);
        drawBoardView.setDrawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_label));
    }


    private void showSelectPopupWindow() {
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
            return;
        }
        popup = new PopupWindow(this);
        popup.setOutsideTouchable(true);
        popup.setContentView(popContentView);

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
        int y = popContentView.getMeasuredHeight() + ivDrawMode.getMeasuredHeight() + padding;

        PopupWindowCompat.showAsDropDown(popup, ivDrawMode, -padding, -y, Gravity.CENTER_HORIZONTAL);
    }

    /**
     * 改变绘制模式
     */
    private void changeDrawMode(@DrawBoardView.DrawMode int drawMode, @DrawableRes int resId) {
        drawBoardView.setDrawMode(drawMode);
        if (popup != null) {
            popup.dismiss();
        }
        ivDrawMode.setImageResource(resId);
    }

    /**
     * 保存图片
     */
    private void saveBitmap() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "draw_" + System.currentTimeMillis());
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/xiaoxiaole");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                drawBoardView.getResultBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                BaseUtils.shortTipInCoast(context,"保存成功");
            } catch (IOException e) {
                e.printStackTrace();
                BaseUtils.shortTipInCoast(context,"保存失败");
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivDrawMode:
                showSelectPopupWindow();
                break;
            case R.id.ivPen:
                if (!isGreen) {
                    isGreen = true;
                    drawBoardView.setPaintColor(Color.GREEN);
                    ivPen.setImageResource(R.drawable.btn_menu_pen_green);
                } else {
                    isGreen = false;
                    drawBoardView.setPaintColor(Color.RED);
                    ivPen.setImageResource(R.drawable.btn_menu_pen_red);
                }
                break;
            case R.id.ivClear:{
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setMessage("清除画布所有内容？");
                builder.setPositiveButton("清除", (dialog, which) -> {
                    drawBoardView.clear();
                });
                builder.setNegativeButton("取消",null);
                builder.show();
                break;
            }
            case R.id.ivUndo:
                drawBoardView.undo();
                break;
            case R.id.ivRedo:
                drawBoardView.redo();
                break;
            case R.id.ivSave:
                saveBitmap();
                break;
            case R.id.ivPath:
                changeDrawMode(DrawBoardView.DrawMode.DRAW_PATH, R.drawable.btn_menu_path);
                break;
            case R.id.ivLine:
                changeDrawMode(DrawBoardView.DrawMode.DRAW_LINE, R.drawable.btn_menu_line);
                break;
            case R.id.ivRect:
                changeDrawMode(DrawBoardView.DrawMode.DRAW_RECT, R.drawable.btn_menu_rect);
                break;
            case R.id.ivOval:
                changeDrawMode(DrawBoardView.DrawMode.DRAW_OVAL, R.drawable.btn_menu_oval);
                break;
            case R.id.ivText:{
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setMessage("先输入要插入的文字，再绘制");
                EditText editText = new EditText(context);
                editText.setBackgroundResource(R.drawable.edge);
                editText.setMinLines(2);
                editText.setMaxLines(2);
                editText.setPadding(10,5,10,5);
                builder.setView(editText);
                builder.setPositiveButton("确认", (dialog, which) -> {
                    penText = editText.getText().toString().trim();
                    if("123".equals(penText) && OPEN_FROM_VALUE.equals(OPEN_FROM_BEFORE_LOGIN)){
                        Intent intent = new Intent(DrawBoardActivity.this, LoginActivity.class);
                        intent.putExtra("trueLogin",true);
                        DrawBoardActivity.this.startActivity(intent);
                        finish();
                        return;
                    }
                    drawBoardView.setDrawText(penText);
                    changeDrawMode(DrawBoardView.DrawMode.DRAW_TEXT, R.drawable.btn_menu_text);
                });
                builder.setNegativeButton("取消",null);
                builder.show();
                break;
            }
//            case R.id.ivBitmap:
//                changeDrawMode(DrawBoardView.DrawMode.DRAW_BITMAP, R.drawable.btn_menu_bitmap);
//                break;
            case R.id.ivMosaic:
                changeDrawMode(DrawBoardView.DrawMode.MOSAIC, R.drawable.btn_menu_mosaic);
                break;
            case R.id.ivEraser:
                changeDrawMode(DrawBoardView.DrawMode.ERASER, R.drawable.btn_menu_eraser);
                break;
        }
    }
}