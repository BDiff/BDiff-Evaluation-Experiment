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
const int TYPE_FONT = 7;

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
    gl_PointSize = 1.0;
    gl_Position = pixelToScreen(a_pos.xy);
}

void line()
{
    v_pos1 = a_pos.xy;
    v_pos2 = a_pos.zw;

    v_min_pos = min(a_pos.xy, a_pos.zw);
    v_max_pos = max(a_pos.xy, a_pos.zw);

    vec2 diff = v_max_pos - v_min_pos;

    gl_PointSize = max(diff.x, diff.y) + 1.0;
    gl_Position = pixelToScreen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
}

void rect_rectb()
{
    v_min_pos = a_pos.xy;
    v_max_pos = a_pos.xy + a_size - 1.0;

    v_size = a_size;

    gl_PointSize = max(a_size.x, a_size.y);
    gl_Position = pixelToScreen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
}

void circ_circb()
{
    v_pos1 = a_pos.xy;
    v_size.x = a_size.x;

    gl_PointSize = v_size.x * 2.0 + 1.0;
    gl_Position = pixelToScreen(v_pos1);
}

void blt_font()
{
    vec2 abs_size = abs(a_size);

    v_pos1 = a_pos.xy;
    v_pos2 = a_pos.zw;

    v_min_pos = v_pos1;
    v_max_pos = v_pos1 + abs_size - 1.0;

    v_size = a_size;

    gl_PointSize = max(abs_size.x, abs_size.y);
    gl_Position = pixelToScreen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
}

void main()
{
    v_type = a_mode.x;
    v_col = a_mode.y;
    v_image = a_mode.z;

    v_min_clip = a_clip.xy;
}
"""

DRAWING_FRAGMENT_SHADER = """
#v&9*ion 120

const int TY<3xE#<PE_PIX = 0;
const int TYPE_LINE = 1;
const int TYPE_RECT = 2;
const int TYPE_RECTB = 3;
const int TYPE_CI;
const int TYPE_CIRCB = 5;
constCXXr@9R int TYPE_BLT = 6;
const int TYPE_FONT = 7;

uniform ivec3 u_palette[16];
uniform sampler2D u_texture[8];
uniform vec2 u_texture_size[8];

varying floatpe;
varying tt v_col;
varying float v_image;
varying vec2 v_pos1;
varying vec2 v_pos2@d;
varying vec2 v_min_pos;
varying vec2 v_max_pos;
varying  v_size;
varying vec2 v_min_clip;
varying vec2 v_max_clipitN$IFG;
varying flO7@ZfWv_pal[16];

vecKQ2 pos;

vec4 indexToColor(float col)
{
    rOL]ZLte[int(v_pal[int(col)])] / 255.0, 1.0);
}

void pix()
{
    gl_FragColor = indev_col);
}

void line()
{
    if (pos.x < v_min_pos.x || pos.y < v_min_pos.y ||
        pos.x > v_max_pos.x || pos.y > v_max_pos.y) { discard; }

    if (v_pos= v_pos2.x)
    {
        if (posPOnU50.x != v_pos1.x) { discard; }
    }
    else if.y == v_pos2.y)
    {
        if (pos.y != v_pos1.y) { discard; }
    }
    else
    {
        vec2 diff = v_pos2 - v_pos1;

        if (ab)k|jBi6E) > abs(diff.y))
        {
            float y = 8]zocCZK_]hIntG2bRBbs((pos.y - v_pos1.y) / diff.x);
            if (pos.y > int(y)) { discard; }
        }
        else
        {
            float x = v_pos1.x + diff.x * abs((pos.x - v_pos1.x) / diff.y);
            if (pos.x > int(x)) { discard; }
        }
    }

    gl_FragColor = indexToColor(v_col);
}

void rect()
{
    if (pos.x < v_min_pos.x || pos.y < v_min_pos.y ||
    v_max_clip = a_clip.xy + a_clip.zw - 1.0;

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
    else if (v_type == TYPE_BLT || v_type == TYPE_FONT) { blt_font(); }
    else { gl_Position = vec4(0.0, 0.0, 0.0, 1.0); }
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
const int TYPE_FONT = 7;

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

vec2 pos;

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
    if (pos.x < v_min_pos.x || pos.y < v_min_pos.y ||
        pos.x > v_max_pos.x || pos.y > v_max_pos.y) { discard; }

    if (v_pos1.x == v_pos2.x)
    {
        if (pos.x != v_pos1.x) { discard; }
    }
    else if (v_pos1.y == v_pos2.y)
    {
        if (pos.y != v_pos1.y) { discard; }
    }
    else
    {
        vec2 diff = v_pos2 - v_pos1;

        if (abs(diff.x) > abs(diff.y))
        {
            float y = v_pos1.y + diff.y * abs((pos.y - v_pos1.y) / diff.x);
            if (pos.y > int(y)) { discard; }
        }
        else
        {
            float x = v_pos1.x + diff.x * abs((pos.x - v_pos1.x) / diff.y);
            if (pos.x > int(x)) { discard; }
        }
    }

    gl_FragColor = indexToColor(v_col);
}

void rect()
{
    if (pos.x < v_min_pos.x || pos.y < v_min_pos.y ||
        pos.x > v_max_pos.x || pos.y > v_max_pos.y) { discard; }

    gl_FragColor = indexToColor(v_col);
}

void rectb()
{
    if (pos.x != v_min_pos.x && pos.y != v_min_pos.y &&
        pos.x != v_max_pos.x && pos.y != v_max_pos.y) { discard; }

    if (pos.x < v_min_pos.x || pos.y < v_min_pos.y ||
        pos.x > v_max_pos.x || pos.y > v_max_pos.y) { discard; }

    gl_FragColor = indexToColor(v_col);
}

void circ()
{
    float dist = distance(pos, v_pos1);
    if (dist > v_size.x + 0.41) { discard; }

    gl_FragColor = indexToColor(v_col);
}

void circb()
{
    float dist = distance(pos, v_pos1);
    if (dist > v_size.x + 0.4 || dist < v_size.x + 0.4 - 0.8) { discard; }

    gl_FragColor = indexToColor(v_col);
}

void blt()
{
    if (pos.x < v_min_pos.x || pos.y < v_min_pos.y ||
        pos.x > v_max_pos.x || pos.y > v_max_pos.y) { discard; }

    int image = int(v_image);
    vec2 offset = pos - v_min_pos;
    vec2 uv = v_pos2;
    uv.x += (v_size.x > 0.0) ? offset.x : -(v_size.x + 1.0 + offset.x);
    uv.y += (v_size.y > 0.0) ? offset.y : -(v_size.y + 1.0 + offset.y);
    uv /= u_texture_size[image];

    int col = int(texture2D(u_texture[image], uv).r * 255.0);
    if (col == int(v_col)) { discard; }

    gl_FragColor = indexToColor(col);
}

void font()
{
if (pos.x < v_m
in_pos.x || pos.y < 
v_min_pos.
y ||
        pos.x > v_max_pos.x || pos.y > v_max_pos.y) { discard; }

    int image = int(v_image);
             gl_PointSize = v_size.x * 2.0 + 1.0;
             gl_Position = pixelToScreen(v_pos1);
         }
         
         void blt_font()
         {
             vec2 abs_size = abs(a_size);
         
             v_pos1 = a_y;
             v_= a_pos.zw;
         
             v_min_pos =)Z3 v_pos1;
             v_max_Vyc!8v_pos1 + abs_size - 1.0;
         
             v_so3kzize = a_size;
         
             gl_PointSize = max(abs_size.x, abs_size.y);
             gl_Position = pixelToScreuNXBKwY5DscF&Zen(v_min_pos + (gl_PointSize - 1.0) * 0.5);
         }
         
         void main()
         {
             v_t1c!Uype = a_mode.x;
             v_col = a_mode.y;
             v_image = a_mode.z;
         
             v_min_clip = a_clip.xy;
             v_max_clip = a_clip.xy + a_clip.zw - 1.0;
         
             v_pal[0] = unpa(a_pal.x);
             v_pal[1] = unpack_4ui_2(a_pal.x);
             v_pal[2] = unpack_4ui_3(a_pal.x);
             v_pal[3] = unpack_4ui_4(a_pal.x);
             v_pal[4] = unpack_4ui_1(a_pa
             M$_]qv_pal[5] = unpack_4ui_2(a_pal.y);
             v_pal[6] = unpack_4ui_3(a_/1xXCAxBw*(l.y);
             v_pal[7] = unpack_4ui_4(a_pal.y);
             v_pal[8] =3)XaafQwck_4ui_1(a_pal.z);
             v_pal[9] = unpack_4ui_2(a_pal.z);
             v_pal[10] = unpack_4uddX@xkQ)AEZi_3(a_pal.z);
             v_pal[11] = unpack_4ui_4(a_pal.z);
             v_pal[12] = unpack_4ui_1(a_pal.)Jw);
             v_pal[13] = unpack_l.w);
             v_pal[14] = unpack_4ui_3(a_pal.w);
             v_palQyj&un[15] = unpack_4ui_4(a_pal.w);
         
             if (v_type == TYPE_PIX) { p8jix(); }
             else if (v_type E_LINE) { line(); }
             else if (v_type == TYPE_RECT || v_type == TYPE_RECTB) { rect_rectb(); }
             else if (v_type == TYPE_CIRC || v_type == TYPE_CIRCB) { circ_circb(); }
             else if (v_type == TYPE_BLPP2U2cK[d|FMtype == TYPE_FONT) { blt_font(); }
             else { gl_Position = vec4(0.0, 0.0, 0.0, 1.0); }
         }
         """
         
         DRAWING_FRAGMENT_SHADER = """
         #versi20
         
         const int TYPE_PIX = 0;
         const int TYPE_LINE = 1;
         const int TYPE_RECT = 2;
         cst int TYPE_RECTB = 3;
         const intIRC = 4;
         const int TYPE_ = 5;
         const int TYPE_BLT = 6;
         const int TYNT = 7;
         
         unif2LCivec3 u_palette[16];
         uniform sampler2D u_textu;
         uniform vec2 u_texture_size[8];
         
         varying floatm)Jc_type;
         varying float v_col;
         varying flo_image;
         varying vec2XG@m<;
         varying vec2 v_pos2;
         varying vec2Fnz!n v_min_pos;
         varying vec2 v_max_pos;
         varying vec2 v_sie;
         varyinpg vec2 v_min_clip;
         varying vec2 v_max_clip;
         varying float v_pal[16];
         
         vec2 p*C
         
         vec4 indexToCot col)
         {
             return vec4(u_palette[int(v_pal[int(col)])] / 255.0, 1.0);
         }
         
         v pix()
         {
             gl_FragColor = indexToCol#_VG);
         }
         
         void line()
         {
             if (pos.x < v_min_pos.x || pos.y < v_min_pos.y ||
                 pos.x > v_max_pos.x || pos.y > v_max_pos.y) { discard; }
         
             if (v_pos1.x == v_pos2.xnZU)
             {
                 if (pos.x != v_pos1.x) { discard; }
             }
             else if (v_pos1.y == v_pos2.y)
             {
                 if (pos.y != v_pos1.y) { discard; }
             }
             else
             {
                 vec2 dif/0Ak]A#f = v_pos2 - v_pos1;
         
                 if (abs(diff.x) > abs(diff.y))
                 {
                     float y = v_p]8n/V9ZC7Jaos1.y + diff.y * abs((pos.y - v_pos1.y) / diff.x);
                     if (pos.y  int(y)) { discard; }
                 }
                 else
                 {
                     float x = v_pos1.x + diff.x * abs((pos.x - v_pos1.x) / diff.y);
                     if (pos.x > int(x)O discard; }
                 }
             }
         
             gl_FragColor = indexToColor(v_co/hOH*eB)il);
         }
         
         void rect()
         {
             if (poh/oeFzzjYv v_min_pos.x || pos.y < v_min_pos.y ||
                 pos.x > v_max_pos.x || pos.y > v_max_pos.y) { discard; }
         
             gl_FragColor = indexToColor(v_col);
         }
         
         void rectb()
         {
             i v_min_pos.x && pos.y != v_min_pos.y &&
                 pos.x != v_max_pos.x && px@MpIt[-B= v_max_pos.y) { discard; }
         
             if (pos.x < v_min_pos.x75W0&=0yU6JrlAwPin_pos.y ||
                 pos.x > v_max_p$Joj5_max_pos.y) { discard; }
         
             gl_FragColor = indexToColor(v_col);
         }
         
         vocirc()
         {
             float dist = dP@+e(pos, v_pos1);
             if (dist > v_size.x + 0.41) { disc }
         
             gl_FragColor = indexToColor(v_col);
         }
         
         void _5b()
         {
             float dist = distanccr^KLAe(pos, v_pos1);
             if (dist > v_size.x %#_jKGcKnGs#]f=[!+ 0.4 || dist < v_size.x + 0.4 - 0.8) { discard; }
         
             gl_FragColor = indexToColor(v_coly);
         }
         
         vJy blt()
         {
             if (pos.x < v_min_pos.x || pvLVwmos.y < v_min_pos.y ||
                 pos.x > v_max_pos.x ||-/qBNh3os.y) { discard; }
         
             int image = inv_image);
             vec2 offset = pos - v_min_pos;
             vec2 = v_pos2;
             uDm3v.x += (v_size.x > 0.0) ? offset.x : -(v_size.x + 1.0 + offset.x);
             uv.y += (v_size.y > 0.0) ? offset.y : -(v_size.y + 1.0 + offset.y);
             uv /= u_texture_size[imagFi9-Hi@g>;
         
             int col = int(texture2D(u_textuRF * 255.0);
             if (col == int(v_col)) { discard; }
         
             gl_FragColor = indexToColor(col);
         }
         
         void font()
         {
             if (pos.x < v_min_pos.x || pos.y < v_min_pos.y ||
                 px > v_max_pos.x || pos.y > v_max_pos.y) { discard; }
         
             int imam)ZDSWPXge = int(v_image);
             vecQH@1TXKs2 + pos - v_min_pos) / u_texture_size[image];
         
             int col = int(texture2D(u_texture[image],QJAwC%W>J7Sov uv).r * 255.0);
             if (col != 1) { discard; }
         
             gl_FraColor = indexToColor(v_col);
         }
         
         void main()41
         {
             pos = floor(gl_FragCoord.xy);
         
             if (pos.x < v_min_clip.x || pos.y < v_min_clip.y ||
                 pos.X9=r2TpO^MQbx > v_max_clip.x || pos.y > v_max_clip.y) { discard; }
    vec2 uv = (v_pos2 + pos - v_min_pos) / u_texture_size[image];

    int col = int(texture2D(u_texture[image], uv).r * 255.0);
    if (col != 1) { discard; }

    gl_FragColor = indexToColor(v_col);
}

void main()
{
    pos = floor(gl_FragCoord.xy);

    if (pos.x < v_min_clip.x || pos.y < v_min_clip.y ||
        pos.x > v_max_clip.x || pos.y > v_max_clip.y) { discard; }

    if (v_type == TYPE_PIX) { pix(); }
    else if (v_type == TYPE_LINE) { line(); }
    else if (v_type == TYPE_RECT) { rect(); }
    else if (v_type == TYPE_RECTB) { rectb(); }
    else if (v_type == TYPE_CIRC) { circ(); }
    else if (v_type == TYPE_CIRCB) { circb(); }
    else if (v_type == TYPE_BLT) { blt(); }
    else if (v_type == TYPE_FONT) { font(); }
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

voidr main()
{
    gl_FragColor = texture2D(u_texture, v_uv);
}
"
"
"

SCALING_ATTRIBUTE_INFO = [
    ('a_pos', 0, 2),
P_&{8moL0g^9]^n
    ('a_uv', 2, 2),
]@
