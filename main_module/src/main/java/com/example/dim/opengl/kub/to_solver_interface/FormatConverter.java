package com.example.dim.opengl.kub.to_solver_interface;

import com.example.dim.opengl.kub.PovorotInf;

import java.util.ArrayList;
public class FormatConverter {
    public static PovorotInf[] convertHods(int[] hods){
        ArrayList<PovorotInf> listHods=new ArrayList<>();
        for(int hod:hods){
            switch (hod){
                case 1:listHods.add(new PovorotInf(1,1,1));break;
                case 2:listHods.add(new PovorotInf(1,1,-1));break;
                case 3:listHods.add(new PovorotInf(1,1,1));listHods.add(new PovorotInf(1,1,1));break;

                case 4:listHods.add(new PovorotInf(1,2,1));break;
                case 5:listHods.add(new PovorotInf(1,2,-1));break;
                case 6:listHods.add(new PovorotInf(1,2,1));listHods.add(new PovorotInf(1,2,1));break;

                case 7:listHods.add(new PovorotInf(1,3,1));break;
                case 8:listHods.add(new PovorotInf(1,3,-1));break;
                case 9:listHods.add(new PovorotInf(1,3,1));listHods.add(new PovorotInf(1,3,1));break;

                case 10:listHods.add(new PovorotInf(3,3,-1));break;
                case 11:listHods.add(new PovorotInf(3,3,1));break;
                case 12:listHods.add(new PovorotInf(3,3,-1));listHods.add(new PovorotInf(3,3,-1));break;

                case 13:listHods.add(new PovorotInf(3,2,-1));break;
                case 14:listHods.add(new PovorotInf(3,2,1));break;
                case 15:listHods.add(new PovorotInf(3,2,-1));listHods.add(new PovorotInf(3,2,-1));break;

                case 16:listHods.add(new PovorotInf(3,1,-1));break;
                case 17:listHods.add(new PovorotInf(3,1,1));break;
                case 18:listHods.add(new PovorotInf(3,1,-1));listHods.add(new PovorotInf(3,1,-1));break;
                default:throw new RuntimeException("Incorrect hod");
            }
        }
        return listHods.toArray(new PovorotInf[listHods.size()]);
    }
    public static int[][][] normalizeGrani(int[][][] grani){
        int[][][] graniNorm=new int[6][3][3];
        for(int i=0;i<6;i++)for(int j=0;j<3;j++)for(int k=0;k<3;k++){
            int x;
            for(x=0;x<6;x++)if(grani[i][j][k]==grani[x][1][1])break;
            graniNorm[i][j][k]=x;
        }
        return graniNorm;
    }
}
