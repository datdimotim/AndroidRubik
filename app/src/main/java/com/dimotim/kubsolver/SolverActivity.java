package com.dimotim.kubsolver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.commonsware.cwac.layouts.AspectLockedFrameLayout;
import com.dimotim.kubSolver.Kub;
import com.dimotim.kubsolver.util.StringSerializer;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

@EActivity
public class SolverActivity extends Activity {
    public enum RESULT{OK,CANCELED}
    public enum PARAMS{RESULT,POSITION}
    private FaceletController faceletController;
    private KubButton[][] facelet;
    private KubButton[] selectors;
    private KubButton[] sides;
    private Button solve;
    private Button show;
    private Button back;

    @Bean
    protected Solvers solvers;

    @AfterInject
    protected void onCreate() {
        createUI();
        initListeners();
    }

    private void initListeners(){
        faceletController=new FaceletController(facelet,sides,selectors);
        solve.setOnClickListener(v -> {
            int[][][] grani=faceletController.getGrani();
            if(!convertAndTestGraniForCorrect(grani)){
                Toast.makeText(SolverActivity.this,"Некорректная позиция",Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                AlertDialog.Builder dialog=new AlertDialog.Builder(SolverActivity.this);
                dialog.setTitle(solvers.getKubSolver().solve(new Kub(grani)).toString()+"");
                dialog.setPositiveButton("OK", (dialog1, which) -> dialog1.cancel());
                dialog.show();
            } catch (Kub.InvalidPositionException e) {
                throw new RuntimeException("incorrect position!!!");
            }
        });

        back.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(PARAMS.RESULT.toString(), RESULT.CANCELED.toString());
            setResult(RESULT_OK, intent);
            finish();
        });

        show.setOnClickListener(v -> {
            int[][][] grani=faceletController.getGrani();
            if(!convertAndTestGraniForCorrect(grani)){
                Toast.makeText(SolverActivity.this,"Некорректная позиция",Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent();
            intent.putExtra(PARAMS.RESULT.toString(), RESULT.OK.toString());
            intent.putExtra(PARAMS.POSITION.toString(), StringSerializer.serializeToString(grani));
            setResult(RESULT_OK, intent);
            finish();
        });
    }
    private boolean convertAndTestGraniForCorrect(int[][][] grani){
        for(int i=0;i<6;i++)for(int j=0;j<3;j++)for(int k=0;k<3;k++){
            grani[i][j][k]--;
            if(grani[i][j][k]<0||grani[i][j][k]>5)return false;
        }
        try {
            new Kub(grani);
        } catch (Kub.InvalidPositionException e) {
            return false;
        }
        return true;
    }

    private void createUI(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        //root.setBackgroundColor(Color.BLUE);
        root.setGravity(Gravity.CENTER);
        setContentView(root);
        FrameLayout kubSetkaPanel=new FrameLayout(this);
        FrameLayout[][] solveFrame=Setka.createSetka(1,3,root,4,false);
        root.addView(kubSetkaPanel);
        solve=new Button(this);
        solve.setTextSize(30);
        solve.setText("SOLVE");
        solve.setBackgroundResource(R.drawable.button_solve_states);
        solveFrame[0][2].addView(solve);

        show=new Button(this);
        show.setTextSize(30);
        show.setText("SHOW");
        show.setBackgroundResource(R.drawable.button_solve_states);
        solveFrame[0][1].addView(show);

        back=new Button(this);
        back.setTextSize(30);
        back.setText("BACK");
        back.setBackgroundResource(R.drawable.button_solve_states);
        solveFrame[0][0].addView(back);

        FrameLayout[][] setka=Setka.createSetka(5,5,kubSetkaPanel,1,false);
        FrameLayout[][] selectorsSetka=Setka.createSetka(1,6,root,6,false);

        selectors=new KubButton[6];
        for(int i=0;i<6;i++){
            KubButton button=new KubButton(this);
            button.setColor(i+1);
            button.setTextSize(30);
            selectors[i]=button;
            selectorsSetka[0][i].addView(button);
        }
        selectors[0].setText("S");
        facelet=new KubButton[3][3];
        for(int i=1;i<4;i++)for(int j=1;j<4;j++){
            KubButton button=new KubButton(this);
            button.setTextSize(40);
            facelet[i-1][j-1]=button;
            setka[i][j].addView(button);
        }
        KubButton up=new KubButton(this);Setka.createSetka(2,1,setka[0][2],1,false)[1][0].addView(up);
        KubButton down=new KubButton(this);Setka.createSetka(2,1,setka[4][2],1,false)[0][0].addView(down);
        KubButton left=new KubButton(this);Setka.createSetka(1,2,setka[2][0],1,false)[0][1].addView(left);
        KubButton right=new KubButton(this);Setka.createSetka(1,2,setka[2][4],1,false)[0][0].addView(right);
        left.setText("<");left.setTextSize(30);
        right.setText(">");right.setTextSize(30);
        sides=new KubButton[]{up,right,down,left};

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1);
        params.leftMargin=2;params.rightMargin=2;params.topMargin=2;params.bottomMargin=2;
    }
}

class Setka{
    static FrameLayout[][] createSetka(int rows, int columns, ViewGroup parent,float aspect,boolean debug){
        Context context=parent.getContext();
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 1);

        AspectLockedFrameLayout squarePanel=new AspectLockedFrameLayout(context);
        squarePanel.setAspectRatio(aspect);
        //squarePanel.setBackgroundColor(Color.BLACK);
        parent.addView(squarePanel,params);

        LinearLayout kubPanel=new LinearLayout(context);
        kubPanel.setOrientation(LinearLayout.VERTICAL);
        kubPanel.setWeightSum(rows);
        //kubPanel.setBackgroundColor(Color.DKGRAY);
        squarePanel.addView(kubPanel);

        LinearLayout[] lines=new LinearLayout[rows];
        for(int i=0;i<rows;i++){
            LinearLayout line=new LinearLayout(context);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setWeightSum(columns);
            lines[i]=line;
            kubPanel.addView(line,params);
        }
        FrameLayout[][] setka=new FrameLayout[rows][columns];
        for(int i=0;i<columns;i++)for(int j=0;j<rows;j++){
            FrameLayout frame=new FrameLayout(context);
            if(debug)frame.setBackgroundColor((i+j)%2==0?Color.rgb(0,0,0):Color.rgb(255,255,255));
            setka[j][i]=frame;
            lines[j].addView(frame,params);
        }
        return setka;
    }
}
class KubButton extends Button{
    private static final int[] colors=new int[]{   R.drawable.gray_button_states,
                                                    R.drawable.red_button_states,
                                                    R.drawable.white_button_states,
                                                    R.drawable.green_button_states,
                                                    R.drawable.blue_button_states,
                                                    R.drawable.yellow_button_states,
                                                    R.drawable.oreange_button_states};
    private int currentColor;
    public KubButton(Context context) {
        super(context);
        setColor(0);
    }
    public KubButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColor(0);
    }
    public KubButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setColor(0);
    }
    public void setColor(int colorNumber){
        setBackgroundResource(colors[colorNumber]);
        currentColor=colorNumber;
    }
    public int getColor(){return currentColor;}
}

class SelectColorController implements View.OnClickListener{
    private final KubButton[] selectors;
    private final OnChangeColorListener listener;
    SelectColorController(KubButton[] selectors,OnChangeColorListener listener){
        this.listener=listener;
        this.selectors=selectors;
        for(KubButton selector:selectors)selector.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        for(KubButton selector:selectors)selector.setText("");
        ((KubButton)v).setText("S");
        listener.onChangeColor(((KubButton) v).getColor());
    }
    interface OnChangeColorListener{
        void onChangeColor(int newColor);
    }
}

class FaceletController{
    private static final String[] names={"D","F","R","B","L","U"};
    private static final int[][] sideColors={{2,4,5,3,1},{6,4,1,3,2},{6,5,1,2,4},{6,3,1,4,5},{6,2,1,5,3},{4,2,3,5,6}};
    private final KubButton[][] facelet;
    private final KubButton[] sides;
    private int selectedColor=1;
    private int selectedSide=0;
    private final int[][][] grani;
    FaceletController(KubButton[][] facelet, KubButton[] sides, final KubButton[] selectors){
        this.facelet=facelet;
        this.sides=sides;
        new SelectColorController(selectors, newColor -> selectedColor =newColor);
        for(int i=0;i<3;i++)for(int j=0;j<3;j++){
            if(i==j&&j==1)continue;
            facelet[i][j].setOnClickListener(v -> ((KubButton)v).setColor(selectedColor));
        }
        sides[3].setOnClickListener(v -> changeSide(selectedSide-1));
        sides[1].setOnClickListener(v -> changeSide(selectedSide+1));
        facelet[1][1].setColor(1);
        grani=new int[6][3][3];
        for(int i=0;i<6;i++)grani[i][1][1]=i+1;
        changeSide(0);
    }
    private void changeSide(int newSide) {
        if (newSide < 0 || newSide > 5) return;
        int oldSide=selectedSide;
        selectedSide=newSide;
        facelet[1][1].setText(names[newSide]);
        for (int i = 0; i < 4; i++) sides[i].setColor(sideColors[newSide][i]);

        for(int i=0;i<3;i++)for(int j=0;j<3;j++){
            int[] koords=convertKoords(new int[]{i,j},oldSide);
            grani[koords[0]][koords[1]][koords[2]]=facelet[i][j].getColor();
        }

        for (int i = 0; i < 3; i++)for(int j=0;j<3;j++){
            int[] koords=convertKoords(new int[]{i,j},newSide);
            facelet[i][j].setColor(grani[koords[0]][koords[1]][koords[2]]);
        }
    }
    private static int[] convertKoords(int[] faceletKoords,int side){
        switch (side){
            case 0:return new int[]{0,2-faceletKoords[0],faceletKoords[1]};
            case 1:return new int[]{1,faceletKoords[0],faceletKoords[1]};
            case 2:return new int[]{3,faceletKoords[0],faceletKoords[1]};
            case 3:return new int[]{4,faceletKoords[0],2-faceletKoords[1]};
            case 4:return new int[]{2,faceletKoords[0],2-faceletKoords[1]};
            case 5:return new int[]{5,faceletKoords[1],2-faceletKoords[0]};
            default:throw new RuntimeException("incorrectSide: side="+side);
        }
    }
    int[][][] getGrani(){
        changeSide(selectedSide);
        int[][][] ret=new int[6][3][];
        for(int i=0;i<6;i++)for(int j=0;j<3;j++)ret[i][j]=grani[i][j].clone();
        return ret;
    }
}