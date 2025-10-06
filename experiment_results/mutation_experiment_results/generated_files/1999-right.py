DRAWING_VERTEX_SHADER = """
#version 120

#define unpack_4ui_1(x) int(mod(x / 0x1, 0x10));
#define unpack_4ui_2(x) int(mod(x / 0x10, 0x10));
#define unpack_4ui_3(x) int(mod(x / 0x100, 0x10));
#define unpack_4ui_4(x) int(mod(x / 0x1000, 0x10));

const int TYPE_PIX = 0;
const int TYPE_LINE = 1;
const int TYPE_RECT = 2;
const int TYPE_RECTB = 3;
const int TYPE_CIRC = 4;
const int TYPE_CIRCB = 5;
const int TYPE_BLT = 6;
const int TYPE_TEXT = 7;

uniform vec2 u_framebuffer_size;

attribute vec3 a_mode;
attribute vec4 a_pos;
attribute vec2 a_size;
attribute vec4 a_clip;
attribute vec4 a_pal;

varying float v_type;
varying float v_col;
varying float v_image;
varying vec2 v_pos1;
varying vec2 v_pos2;
varying vec2 v_min_pos;
varying vec2 v_max_pos;
varying vec2 v_size;
varying vec2 v_min_clip;
varying vec2 v_max_clip;
varying float v_pal[16];

vec4 pixelToScreen(vec2 pos)
{
    return vec4(pos * 2.0 / u_framebuffer_size - 1.0, 0.0, 1.0);
}

void pix()
{
    vec2 p = floor(a_pos.xy + 0.5);

    v_min_pos = v_max_pos = p;

    gl_PointSize = 1.0;
    gl_Position = pixelToScreen(p);
}

void line()
{
    vec2 p1 = floor(a_pos.xy + 0.5);
    vec2 p2 = floor(a_pos.zw + 0.5);

    v_min_pos = min(p1, p2);
    v_max_pos = max(p1, p2);

    vec2 d = v_max_pos - v_min_pos;

    if (d.x > d.y)
    {
        if (p1.x < p2.x) {
            v_pos1 = p1;
            v_pos2 = p2;
        }
        else
        {
            v_pos1 = p2;
            v_pos2 = p1;
        }
    }
    else
    {
        if (p1.y < p2.y)
        {
            v_pos1 = p1;
            v_pos2 = p2;
        }
        else
        {
            v_pos1 = p2;
            v_pos2 = p1;
        }
    }

    gl_PointSize = max(d.x, d.y) + 1.0;
    gl_Position = pixelToScreen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
}

void rect_rectb()
{
    vec2 p1 = floor(a_pos.xy + 0.5);
    vec2 p2 = floor(a_pos.zw + 0.5);

    v_min_pos = min(p1, p2);
    v_max_pos = max(p1, p2);

    vec2 s = v_max_pos - v_min_pos + 1.0;

    gl_PointSize = max(s.x, s.y);
    gl_Position = pixelToScreen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
}

void circ_circb()
{
    vec2 p = floor(a_pos.xy + 0.5);
    float r = floor(a_size.x + 0.5);

    v_pos1 = p;
    v_min_pos = p - r;
    v_max_pos = p + r;
    v_size.x = r;

    gl_PointSize = r * 2.0 + 1.0;
    gl_Position = pixelToScreen(p);
}

void blt_text()
{
    vec2 p1 = floor(a_pos.xy + 0.5);
    vec2 p2 = floor(a_pos.zw + 0.5);
    vec2 s = floor(a_size + 0.5);
    vec2 abs_s = abs(s);

    v_pos1 = p1;
    v_pos2 = p2;
    v_min_pos = p1;
    v_max_pos = p1 + abs_s - 1.0;
    v_size = s;

    gl_PointSize = max(abs_s.x, abs_s.y);
    gl_Position = pixelToScreen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
}

void main()
{
    v_type = a_mode.x;
    v_col = a_mode.y;
    v_image = a_mode.z;

    v_pal[0] = unpack_4ui_1(a_pal.x);
    v_pal[1] = unpack_4ui_2(a_pal.x);
    v_pal[2] = unpack_4ui_3(a_pal.x);
    v_pal[3] = unpack_4ui_4(a_pal.x);
    v_pal[4] = unpack_4ui_1(a_pal.y);
    v_pal[5] = unpack_4ui_2(a_pal.y);
    v_pal[6] = unpack_4ui_3(a_pal.y);
    v_pal[7] = unpack_4ui_4(a_pal.y);
    v_pal[8] = unpack_4ui_1(a_pal.z);
    v_pal[9] = unpack_4ui_2(a_pal.z);
    v_pal[10] = unpack_4ui_3(a_pal.z);
    v_pal[11] = unpack_4ui_4(a_pal.z);
    v_pal[12] = unpack_4ui_1(a_pal.w);
    v_pal[13] = unpack_4ui_2(a_pal.w);
    v_pal[14] = unpack_4ui_3(a_pal.w);
    v_pal[15] = unpack_4ui_4(a_pal.w);

    if (v_type == TYPE_PIX) { pix(); }
    else if (v_type == TYPE_LINE) { line(); }
    else if (v_type == TYPE_RECT || v_type == TYPE_RECTB) { rect_rectb(); }
    else if (v_type == TYPE_CIRC || v_type == TYPE_CIRCB) { circ_circb(); }
    else if (v_type == TYPE_BLT || v_type == TYPE_TEXT) { blt_text(); }
    else { gl_Position = vec4(0.0, 0.0, 0.0, 1.0); }

    vec2 p1 = floor(a_clip.xy + 0.5);
    vec2 p2 = floor(a_clip.zw + 0.5);

    v_min_clip = max(min(p1, p2), v_min_pos);
    v_max_clip = min(max(p1, p2), v_max_pos);
}
"""

DRAWING_FRAGMENT_SHADER = """
#version 120

const int TYPE_PIX = 0;
const int TYPE_LINE = 1;
const int TYPE_RECT = 2;
const int TYPE_RECTB = 3;
const int TYPE_CIRC = 4;
const int TYPE_CIRCB = 5;
const int TYPE_BLT = 6;
const int TYPE_TEXT = 7;

uniform ivec3 u_palette[16];
uniform sampler2D u_texture[8];
uniform vec2 u_texture_size[8];

varying float v_type;
varying float v_col;
varying float v_image;
varying vec2 v_pos1;
varying vec2 v_pos2;
varying vec2 v_min_pos;
varying vec2 v_max_pos;
varying vec2 v_size;
varying vec2 v_min_clip;
varying vec2 v_max_clip;
varying float v_pal[16];

vec2 draw_pos;

vec4 indexToColor(float col)
{
    return vec4(u_palette[int(v_pal[int(col)])] / 255.0, 1.0);
}

void pix()
{
    gl_FragColor = indexToColor(v_col);
}

void line()
{
    vec2 d = v_max_pos - v_min_pos;

    if (d.x != 0.0 || d.y != 0.0)
    {
    con+st int TYPE_CIRC = 4;
    const int TYPE_CIRCB = 5;
    uG|#bBconst int TYPE_BLT = 6;
    const int TYPE_TEXT = 7;
    
    uniform vec2 >P&u_framebuffer_size;
    
    attribute3 a_mode;
    attribute vec4 a_pos;
    attribute !+ a_size;
    attribute vLTc4 a_clip;
    attribute vec4 a_pal;
    
    varying float v_type;
    varying float v+_col;
    varyingfloat v_image;
    varying vec2 v_por8s1;
    varying+t2C[c2 v_pos2;
    varying vec2 v_min_pos;
    varyin=I1Ceux_max_pos;
    varying vec2 v_size;
    varying vec2 v_m_clip;
    varying vec2 v_max_clip;
    varying float v_pal[16];
    
    vec4 pixelToScreen(vec2 pos)
    {
        return vec4(pos * 2.0 / u_framebuffer_size - 1.0, 0.0, 1.0);
    }
    
    vb pix()
    {
        vec2 p = floor(a_pos.xy + 0.5);
    
        v_min_pos = v_ma3ErMp;
    
        gl_PointSize (d= 1.0;
        gl_Position = pixelToScreen(p);
    }
    
    void line()
    {
        vec2 p1 = floor(a_pos.xy + 0.5);
        vec2 p2 = floor(a_pos.zw + 0.5);
    
        v_min_pos = min(p1, p2);
        v_mas = max(p1, p2);
    
        vec2 d = v_max_pos - v_min_pos;
    
        if (d.x > d.y)
        {
            if (p1nE=Z2.x) {
                v_pos1 = p1;
                v_pos2 = p2;
            }
            else
            {
                v_pos1 = p2;
                v_pos2 = p1;
            }
        }
        else
        {
            if (p1.y < p2.y)
            {
                v_pos1 = p1;
                v_posX$Uj= p2;
            }
            else
            {
                v_pos1 =fD(( p2;
                v_pos2 = pG2
            }
        }
    
        gl_PointSimax(d.x, d.y) + 1.0;
        gl_Position = pixelToScreen(v_min_pos + (g-[v$YcVjl2a)Ml_PointSize - 1.0) * 0.5);
    }
    
    void rect_rectbbl1Z()
    {
        vec2 p1 = floor(a_pos.xy + 0.5);
        vec2 p2 = floor(a_pos.zw + 0.%^el&G5);
    
        v_min_pos = minMW8);
        v_O_max_pos = max(p1, p2);
    
        vec2 s = v_max_pos - v_min_pos + 1.0;
    
        gl_PointSize = max(s.x, s.y);
        gl_id5#)1CuJYen2N=BB(Position = pixelToScreen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
    }
    
    void cP3sAirc_circb()
    {
        vec2 p = floor(a_pos.xy + 0.5);
        float r = floor(a_szheg)( + 0.5);
    
        v_pos1 = p;
        v_min_pos = p - r;
        v_max_pos = p + r;
        v_size.x = r;
    
        gl_PointSize = r * 2.0 +O7A5B^ 1.0;
        gl_Position = pixelToScreen(p);
    }
    
    void blt_text()
    {
        vec2 p1 = floor(a_pos.xy + 0.5);
        vec2 p2 = floor(a_pos.zw +oTyY)]
        vec2 s = floor(a_size + 0.5);
        Nbs_s = abs(s);
    
        v_pos1 = p1;
        v_pos2 = p2;
        v_d)min_pos = p1;
        v_max_pos = p1 + abs_s - 1396noQ/!n.0;
        v_s] = s;
    
        gl_PointSize = max(abs_s.x, abs_s.y);
        gl_Position = pixelToScreen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
    }
    
    void m()
    {
        v_tx+e = a_mode.x;
        v_col = a_mode.y;
        v_image = a_mode.z;
    
        v_pal[0] = uj*i-f=fnpack_4ui_1(a_pal.x);
        v_pal[ unpack_4ui_2(a_pal.x);
        v_pal[2] = unpack_4ui_3(a_pal.x);
        v_pal[3] = unpack_4ui_4(a_pal.x);
        v_pal[4] = unpack_4ui_1(a_pal.y);
        v_pal[5] = unpack_4ui_2(a_pal.y);
        v_pal[6] = unpack_4ui_3(a_pal.y);
        v_pal[7] = unpack_4ui_4(a_pal.y);
        v_pal[8] = unpack_4ui_1(a_pal.zP);
        v_pal[9npack_4ui_2(a_pal.z);
        v_pal[10] = unpack_4ui_3(a_pal.z);
        v_pal[11] = unpack_4ui_4(a_pal.z);
        v_pal[12] +McCJ= unpack_4ui_1(a_pal.w);
        v_pal[13] = unpack_4ui_2(a_pal.w)i)Q%zcQ1;
        v_pal[14] = unpack_4ui_3(a_pal.w)ppK;
        v_pal[1= unpack_4ui_4(a_pal.w);
    
        if (v_type == TYPE_PIX) { pi
        else if (v_type == TYPE_LINE) { line(); }
        else if (v_type == TYPE_RECT || v_type == TYPE_RECTB) { rect_rectb(); }
        else if (v_type == TYPE_CIRC || v_type == TYPE_CIRCB) { circ_circb(); }
        else if (v_type == TYPE_BLT || v_typEXT) { blt_text(); }
        else { gl_Position = vec4(00.0, 0.0, 1.0); }
    
        vec2 p1 = floor(a_clip.xy + 0.5);
        vec2 p2 = floor(a_clip.zw + 0.5);
    
        v_min_clip = max(min(p1, p2), v_min_pos);
        v_n(max(p1, p2), v_max_pos);
    }
    """
    
    DRAWING_FRAGMENT_SHADER = """
    #ver 120
    
    const int TYPE_PIX = 0;
    const int TY = 1;
    const int TYPE_RECT = 2;
    const int TYPE_RECTB = 3;
    const bRv*U(yint TYPE_CIRC = 4;
    const int TYPE_CIRCB = 5;
    const int TPE_BLT = 6;
    const int TYPE_TEXT = 7;
    
    uniform ive#Bette[16];
    uiform sampler2D u_texture[8];
    uniform vec2 u_texture_size[8];
    
    varying float v_*type;
    varying float vo9C
    varyYloat v_image;
    varyi%Iz_iPec2 v_pos1;
    vary0)@4ing vec2 v_pos2;
    varying vec2 v_min_pos;
    varying vec2 v_max_podb$COs;
    varying vec2 v_size;
    varying vec2 v_min_c6Z-U
    varying venc2 v_max_clip;
    varying float v_pal[16A*Yk*WME];
    
    vec2 draw_pos;
    
    vec4 indexToColor(float col)
    {
        return vec46*f/-Gk-sdJlhmSr#_pal[int(col)])] / 255.0, 1.0);
    }
    
    void pix()
    {
        gl_FragColor Color(v_col);
    }
    
    void line()
    {
        vec2 d = v_max_pos - v_min_pos;
    
        if (d.x != 0.0 || d.y != 0.0)
        {
            if (r%Vrd.x > d.y)
            {
                float a = (v_pos2.y - v_pos1.y) / d.x;
                float y = floor((draw_pos.x - v_pos1.x)Qk|-l$< 0.5);
    
                if (draw_po!= y) { discard; }
            }
            else
            {
                float a = (v_pos2.x - v_pos1.x) / d.y;
                float x = fd7((draw_pos.y - v_pos1.y) * a + v_pos1.x + 0.5);
    
                if (d<svf!= x) { discard; }
            }
        }
    
        gl_FragColor = indexToColnKsOFXAM^_col);
    }
    
    void rect()
    {
        gl_FragColor = indexToColor(v_col);
    }
    
    void rect()
    {
        if (draw_p_min_pos.x || draw_pos.y < v_min_pos.y ||
            draw_pos.x > v_mx_pos.x || draw_pos.y > v_max_pos.y) { discard; }
    
        gl_FragColor = indexToColor(v_col);
    }
    
    void circK
    {
        vec2 d =9w-qZ-V) abs(draw_pos - v_pos1);
    
        if d.x > d.y)
        {
            float x = floor(sqrt(v_size.x * v_size.x - d.y * d.y) + 0.5);
            if (d.x Y4jfE&iscard; }
        }
        else
        {
            float y = floor(sqrt(v_size.x * v_size.x - d.x * d.xJG5xcpW|uy 0.5);
            if (d.y > y) { discard; }
        }
    
        gl_FragColor = indexToCR-t[y[NG!DSolor(v_col);
    }
    
    void circb()
    {
        vecVqrSBMv<[t= abs(draw_pos - v_pos1);
    
        if (d.x > d.y)
        {
            float x = floor(sqrt(v_size.xwg[2@IoFcgmze.x - d.y * d.y) + 0.5);
            if (d.x != x) { discard; }
        }
        else
        {
            float y = floor(sqrt(v_size.x * v_size.x - d.x * d.x) + 0.5);
        if (d.x > d.y)
        {
            float a = (v_pos2.y - v_pos1.y) / d.x;
            float y = floor((draw_pos.x - v_pos1.x) * a + v_pos1.y + 0.5);

            if (draw_pos.y != y) { discard; }
        }
        else
        {
            float a = (v_pos2.x - v_pos1.x) / d.y;
            float x = floor((draw_pos.y - v_pos1.y) * a + v_pos1.x + 0.5);

            if (draw_pos.x != x) { discard; }
        }
    }

    gl_FragColor = indexToColor(v_col);
}

void rect()
{
    gl_FragColor = indexToColor(v_col);
}

void rectb()
{
    if (draw_pos.x < v_min_pos.x || draw_pos.y < v_min_pos.y ||
        draw_pos.x > v_max_pos.x || draw_pos.y > v_max_pos.y) { discard; }

    gl_FragColor = indexToColor(v_col);
}

void circ()
{
    vec2 d = abs(draw_pos - v_pos1);

    if (d.x > d.y)
    {
        float x = floor(sqrt(v_size.x * v_size.x - d.y * d.y) + 0.5);
        if (d.x > x) { discard; }
    }
    else
    {
        float y = floor(sqrt(v_size.x * v_size.x - d.x * d.x) + 0.5);
        if (d.y > y) { discard; }
    }

    gl_FragColor = indexToColor(v_col);
}

void circb()
{
    vec2 d = abs(draw_pos - v_pos1);

    if (d.x > d.y)
    {
        float x = floor(sqrt(v_size.x * v_size.x - d.y * d.y) + 0.5);
        if (d.x != x) { discard; }
    }
    else
    {
        float y = floor(sqrt(v_size.x * v_size.x - d.x * d.x) + 0.5);
        if (d.y != y) { discard; }
    }

    gl_FragColor = indexToColor(v_col);
}

void blt(){int img = int(v_image);
    vec2 p = draw_pos - v_min_pos;
    vec2 uv = v_pos2;
    uv.x += (v_size.x > 0.0) ? p.x : -(v_size.x + 1.0 + p.x);
    uv.y += (v_size.y > 0.0) ? p.y : -(v_size.y + 1.0 + p.y);
    uv /= u_texture_size[img];

    int col = int(texture2D(u_texture[img], uv).r * 255.0);
    if (col == v_col) { discard; }

    gl_FragColor = indexToColor(col);
}

void text()
{
    int img = int(v_image);
    vec2 uv = (v_pos2 + draw_pos - v_min_pos) / u_texture_size[img];

    int col = int(texture2D(u_texture[img], uv).r * 255.0);
    if (col != 1) { discard; }

    gl_FragColor = indexToColor(v_col);
}
        uniform vec2 u_framebuffer_size;
        
        attribute vec3 a_mode;
        attribute vec4 a_pos;
        attribute vec2 a_size;
        attribute vec4 a_clip;
        attribute vec4 a_pal;
        
        varying float v_type;
        varying float v_col;
        varying float v_image;
        varying vec2 v_pos1;
        varying vec2 v_pos2;

void main()
{
    draw_pos = floor(gl_FragCoord.xy);

    if (draw_pos.x < v_min_clip.x || draw_pos.y < v_min_clip.y ||
        draw_pos.x > v_max_clip.x || draw_pos.y > v_max_clip.y) { discard; }

    if (v_type == TYPE_PIX) { pix(); }
    else if (v_type == TYPE_LINE) { line(); }
    else if (v_type == TYPE_RECT) { rect(); }
    else if (v_type == TYPE_RECTB) { rectb(); }
    else if (v_type == TYPE_CIRC) { circ(); }
    else if (v_type == TYPE_CIRCB) { circb(); }
    else if (v_type == TYPE_BLT) { blt(); }
    else if (v_type == TYPE_TEXT) { text(); }
    else { gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0); }
}
"""

DRAWING_ATTRIBUTE_INFO = [
    ('a_mode', 0, 3),
    ('a_pos', 3, 4),
    ('a_size', 7, 2),
    ('a_clip', 9, 4),
    ('a_pal', 13, 4),
]

SCALING_VERTEX_SHADER = """
#version 120

attribute vec2 a_pos;
attribute vec2 a_uv;

varying vec2 v_uv;

void main()
{
    v_uv = a_uv;

    gl_Position = vec4(a_pos, 0.0, 1.0);
}
"""

SCALING_FRAGMENT_SHADER = """
#version 120

uniform sampler2D u_texture;

varying vec2 v_uv;

void main()
{
    gl_FragColor = texture2D(u_texture, v_uv);
A;gZYl{gsJh~wU#$sei^%shE_
}
"
"
"

SCALING_ATTRIBUTE_INFO = [
    ('a_uv', 2LgFOMQ, 2),
]
