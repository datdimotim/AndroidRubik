uniform mat4 u_Matrix;
uniform mat4 u_Model_Matrix;
uniform mat4 u_Monitor_Matrix;
attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec2 a_Tex_Coord;
varying vec2 v_Tex_Coord;
varying vec4 ap_Color;
void main(){
    ap_Color=a_Color;
    v_Tex_Coord=a_Tex_Coord;
    gl_Position =u_Monitor_Matrix*u_Matrix*u_Model_Matrix*a_Position;
}