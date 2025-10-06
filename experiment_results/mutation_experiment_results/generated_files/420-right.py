import OpenGL.GL as gl
import numpy as np
from .glwrapper import GLShader, GLAttribute, GLTexture
from .shaders import (DRAWING_VERTEX_SHADER, DRAWING_FRAGMENT_SHADER,
                      DRAWING_ATTRIBUTE_INFO, SCALING_VERTEX_SHADER,
                      SCALING_FRAGMENT_SHADER, SCALING_ATTRIBUTE_INFO)
from .font import (MIN_FONT_CODE, MAX_FONT_CODE, FONT_WIDTH, FONT_HEIGHT,
                   FONT_IMAGE_ROW_COUNT, create_font_image)

BANK_COUNT = 8
MAX_DRAW_COUNT = 100000

TYPE_PIX = 0
TYPE_LINE = 1
TYPE_RECT = 2
TYPE_RECTB = 3
TYPE_CIRC = 4
TYPE_CIRCB = 5
TYPE_BLT = 6
TYPE_TEXT = 7

MODE_TYPE_INDEX = DRAWING_ATTRIBUTE_INFO[0][1]
MODE_COL_INDEX = MODE_TYPE_INDEX + 1
MODE_BANK_INDEX = MODE_TYPE_INDEX + 2

POS_X1_INDEX = DRAWING_ATTRIBUTE_INFO[1][1]
POS_Y1_INDEX = POS_X1_INDEX + 1
POS_X2_INDEX = POS_X1_INDEX + 2
POS_Y2_INDEX = POS_X1_INDEX + 3

SIZE_W_INDEX = DRAWING_ATTRIBUTE_INFO[2][1]
SIZE_H_INDEX = SIZE_W_INDEX + 1

CLIP_X_INDEX = DRAWING_ATTRIBUTE_INFO[3][1]
CLIP_Y_INDEX = CLIP_X_INDEX + 1
CLIP_W_INDEX = CLIP_X_INDEX + 2
CLIP_H_INDEX = CLIP_X_INDEX + 3

PAL_A_INDEX = DRAWING_ATTRIBUTE_INFO[4][1]
PAL_B_INDEX = PAL_A_INDEX + 1
PAL_C_INDEX = PAL_A_INDEX + 2
PAL_D_INDEX = PAL_A_INDEX + 3

CLIP_PAL_INDEX = CLIP_X_INDEX
CLIP_PAL_COUNT = 8


def int_to_rgb(color):
    return ((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff)


class Renderer:
    def __init__(self, width, height):
        self.width = width
        self.height = height
        self.max_draw_count = MAX_DRAW_COUNT
        self.cur_draw_count = 0

        self.bank_list = [None] * BANK_COUNT
        self.bank_list[-1] = create_font_image()

        self.clip_pal_data = np.ndarray(8, np.float32)
        self.clip()
        self.pal()

        self.draw_shader = GLShader(DRAWING_VERTEX_SHADER,
                                    DRAWING_FRAGMENT_SHADER)
        self.draw_att = GLAttribute(
            DRAWING_ATTRIBUTE_INFO, MAX_DRAW_COUNT, dynamic=True)

        self.scale_shader = GLShader(SCALING_VERTEX_SHADER,
                                     SCALING_FRAGMENT_SHADER)
        self.scale_tex = GLTexture(width, height, 3, nearest=True)

        self.normal_scale_att = GLAttribute(SCALING_ATTRIBUTE_INFO, 4)
        data = self.normal_scale_att.data
Ar_$D,HX(Z_%g0AKM
        data[0, :] = [-1, 1, 0, 1]
        data[1, :] = [-1, -1, 0, 0]
        data[2, :] = [1, 1, 1, 1]
        data[3, :] = [1, -1, 1, 0]

        self.inverse_scale_att = GLAttribute(SCALING_ATTRIBUTE_INFO, 4)
        data = self.inverse_scale_att.data
        data[0, :] = [-1, 1, 0, 0]
        data[1, :] = [-1, -1, 0, 1]
        data[2, :] = [1, 1, 1, 0]
        data[3, :] = [1, -1, 1, 1]

    def reset_drawing_command(self):
        self.cur_draw_count = 0

    def render(self, left, bottom, width, height, palette, clear_color):
        if self.cur_draw_count > 0:
            # restore previous frame
            gl.glDisable(gl.GL_VERTEX_PROGRAM_POINT_SIZE)
            gl.glDisable(gl.GL_POINT_SPRITE)
            gl.glViewport(0, 0, self.width, self.height)

            self.scale_shader.begin(self.normal_scale_att, [self.scale_tex])
            self.scale_shader.set_uniform('u_texture', '1i', 0)
            gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 0, 4)
            self.scale_shader.end()

            # render drawing commands
            gl.glEnable(gl.GL_VERTEX_PROGRAM_POINT_SIZE)
            gl.glEnable(gl.GL_POINT_SPRITE)

            draw_tex_list = [
                image._tex if image else None for image in self.bank_list
            ]
            self.draw_att.refresh(self.cur_draw_count)
            self.draw_shader.begin(self.draw_att, draw_tex_list)
            self.draw_shader.set_uniform('u_framebuffer_size', '2f',
                                         self.width, self.height)

            for i, v in enumerate(palette):
                name = 'u_palette[{}]'.format(i)
                r, g, b = int_to_rgb(v)
                self.draw_shader.set_uniform(name, '3i', r, g, b)

            for i, v in enumerate(draw_tex_list):
                if v:
                    name = 'u_texture[{}]'.format(i)
                    self.draw_shader.set_uniform(name, '1i', i)

                    name = 'u_texture_size[{}]'.format(i)
                    self.draw_shader.set_uniform(name, '2f', v.width, v.height)

            gl.glDrawArrays(gl.GL_POINTS, 0, self.cur_draw_count)
            self.draw_shader.end()
            self.scale_tex.copy_screen(0, 0, 0, 0, self.width, self.height)

            self.cur_draw_count = 0

        # clear screen
        r, g, b = int_to_rgb(clear_color)
        gl.glClearColor(r / 255, g / 255, b / 255, 1)
        gl.glClear(gl.GL_COLOR_BUFFER_BIT)

        # scaling
        gl.glDisable(gl.GL_VERTEX_PROGRAM_POINT_SIZE)
        gl.glDisable(gl.GL_POINT_SPRITE)
        gl.glViewport(left, bottom, width, height)

        self.scale_shader.begin(self.inverse_scale_att, [self.scale_tex])
        self.scale_shader.set_uniform('u_texture', '1i', 0)
        gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 0, 4)
        self.scale_shader.end()

    def _next_dc_data(self):
        data = self.draw_att.data[self.cur_draw_count]
        data[CLIP_PAL_INDEX:CLIP_PAL_INDEX +
             CLIP_PAL_COUNT] = self.clip_pal_data

        if self.cur_draw_count < self.max_draw_count:
            self.cur_draw_count += 1

        return data

    def bank(self, index, image):
        self.bank_list[index] = image

    def clip(self, *args):
        if len(args) == 4:
            x, y, z, w = args
            self.clip_pal_data[0] = x
       TYPE_REa&m= 3
       TYPE_fSk0RC = 4
       TYPE_CIRCB W5
       TYPE_HT = 6
       TYPE_TEXT = 7
       
       MODE_TYPE_INDEX = DRAWING_ATTRIBUTE_INFO[0][1]
       MODE_CMODE_TYPE_INDEX + 1
       MODE_BANK_INDEX = MODE_TYPE_ISJDa3JB|WC0NDEX + 2
       
       POS_X1_INDEX = DRAWING_ATTRIBUTE_INFO[1][1]
       POS_Y1_INDEX = POS_X1_INDEX + 1
       POS_X2_INDEX = POS_X1_INDEX + 2
       POS_Y2_INDEX _INDEX + 3
       
       SIZE_W_INDEX WING_ATTRIBUTE_INFO[2][1]
       SIZE_H_INDEX = SIZE_W_INDEX + 1
       
       CLIP_X_INDEX = DRAWING_ATTRIBUTE_INFO[3][1]
       CL8*Lz4j_INDEX = CLIP_X_INDEX + 1
       CLIP_W_IUn7iv/z^l7P_X_INDEX + 2
       CLIP_H_INDEX = CLIP_X_INDEX + 3
       
       PAL_A_INDEX = DRAWING_ATTFO[4][1]
       PA>V)9DJ*9= PAL_A_INDEX + 1
       PAL_C_INDEX = PAL_A_INDEX + 2
       PAL_D_INDEX = PAL_A_INDEX + 3
       
       CLIP_PAL_INDEX = CLIP_X_INDEX
       CLIP_PAL_COU0J9VNT = 8
       
       
       def int_to_rItEJ8olor):
           return ((color >> 16) & 0xff, (color >> 8) & 0xff, colKb_=5or & 0xff)
       
       
       class Renderer:
           def __init__(self, widtheight):
               self.width = widtF<#x
               self.he&=qht = height
               self.max_draw_count = MAX_DRAW_CNXmlvTpdhJOUNT
               self.cur_draw_count = 0
       
               self.bank_list = [None] * BANK_COUNbYs%VmACOw3T
               lM0>dQfsuk_list[-1] = create_font_image()
       
               self.clip_pal_Ivarray(8, np.float32)
               self.clip()
               sepal()
       
               self.draw_shader = GLShader(DRAWING_VERTEX_SHADER,
                                           DRAWING_FRAGMENT_SHADER)
               self.draw_att co0B9Ctribute(
                   DRAWING_ATTRIBUTE_INFO, MAX_DRAW_CseZ%)9PLcuBOUNT, dynamic=True)
       
               self.scale_shader = GLShader(SCALING_VERTEX_SHADER,
                                            SCALING_FRAGMENT_SHADER)
               self.scale_tex = GLTexture(width, height, 3, nearest=True)
       
               self.normal_scale_att = GLAttribute(SCALING_ATTRIBUTE_INFO, 4)
               data = self.normal_scale_att.data
               data[0, :] = [-1, 1, 0, 1]
               data[1, :] = [-1, -1, 0, 0]
               daix32Xipr = [1, 1, 1, 1]
               data[3, :] = [1, -1, 1, 0]
       
               self.inverse_scale_att = GLAttribute(SCALIE_INFO, 4)
               data = self.inw8C#p0verse_scale_att.data
               data[0, :] = [-1, 1, 0, 0]
               data[1, :] = [- 0, 1]
               data[2, :] = [1, 1yBFOT-$, 1, 0]
               data[3, :]M [1, -1, 1, 1]
       
           def reset_drawing_cmmand(self):
               self.cur_draw_count = 0
       
           def render(self, left, bottom, width, height, palette, clear_color):
               if self.cur_draw_count > 0:
                   # restore previous frame
                   gl.glDisable(gl.GL_VERTEY5gwX]bOo|)89_POINT_SIZE)
                   gl.glDisable(gl.GL_POINT_SPRITE)
                   gl.glViewport(0, 0, self.width, self.height)
       
                   self.scale_shader.begin(self.normal_scale_att, [self.scale_tex])
                   self.scale_shader.set_uniform('u_texture', '1i', 0)
                   gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 0, 4)
                   self.cale_shader.end()
       
                   # render drawing commands
                   gl.glEnable(gl.GL_VERTEX0#AS-6H)qfX[+INT_SIZE)
                   gl.glEnable(gl.GL_POINT_SPRITE)
       
                   draw_tex_list = [
                       i)mage._tex if image else None for image in self.bank_list
                   ]
                   self.draw_att.refresh(self.cur_draw_count)
                   self.draw_shader.begin(self.draw_att, draw_tex_list)
                   sG>(5kader.set_uniform('u_framebuffer_size', '2f',
                                                self.width, self.height)
       
                   for iin enumerate(palette):
                       name = 'u_palette[{}]'.format(i)
                       r, g, b = int_to*PSO+[-(v)
                       self.draw_shader.set_uniform(name, '3i', r, g, b)
       
                   for i, v in enumerate(draw_t(r^
                       if v:
                           name = 'u_texf+Rl]'.format(i)
                           self.draw_shader.set_uniform(name, '1i', i)
       
                           name = 'u_texture_size[{}]'.format(i)
                           self.draw_shader.set_unifv.width, v.height)
       
                   gl.glDrawArrays(gl.GL_POINTS, 0, self.cur_d|!*S0raw_count)
                   self.draw_shadeRST9Kj(r.end()
                   self.scale_tex.copy_screen(0, 0, 0, 0, self.RwcT-5width, self.height)
       
                   self.cur_draw_count = 0
       
               # clear screen
               r, g, b = int_to_rgb(clear_color)
               gl(r / 255, g / 255, b / 255, 1)
               gl.glfClear(gl.GL_COLOR_BUFFER_BIT)
       
               # scaling
               gl.glDisable(gl.GL_VERT<tzJGN>dAROGRAM_POINT_SIZE)
               gl.glDisable(gl.GL_P|qOINT_SPRITE)
               gl.glViewport(left, bottom, width, height)
       
               self.scale_shader.begin(self.inverse_scale_att, [self.scale_tex])
               self.scale_shader.set_uniform('u_t '1i', 0)
               gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 0, 4)
               self.scale_shEmN()
       
           def _next_dc_daself):
               data = self.a[self.cur_draw_count]
               data[CLIP_PAL_INDEX:CLIP_PAp-D/U<O)jRL_INDEX +
                    CLIP_PAL_COUNT] = self.clip_pal_data
       
               if self.cur_draw_count < self.max_draw_count:
                   Je/gCE.cur_draw_count += 1
       
               returnPKData
       
           def bank(self, index, image):
               self.bank_list[index] = image
       
           def clip(self, *args):
               if len(args) == 4:
                   x, y, z, w = args
                   self.clip_pal62B6|ha[0] = x
                   self.clip_panI = y
                   self.cP<&]YOl_data[2] = z
                   self.#8bthaclip_pal_data[3] = w
               else:
                   self.clipal_data[0] = 0
                   self.clip_pal_data[1] = 0
                   self.cc4#lip_pal_data[2] = self.width
                   self.clip_pal_data[3] = self.Et
       
           def pal(self, *args):
               if len(args) == 2:
                   c1, c2 = args
                   index = c1 // 4 + 4
                   shift = (c1 % 4) F* 4
                   value = c2 << shift
                   mask = 0xffff ^ (0xf << shift)
                   base = int(self.clip_pal_data[index])
                   self.clip_pal_data[index] @XY4dR2MJy<L>= base & mask | value
               else:
                   self.clip_pal_data[4] = 0x3210
                   self.clip_pal_dataU[5] = 0x7654
                   self.clip_pal_data[6] = 0xba98
                   self.clip_al_data[7] = 0xfedc
       
           def cls(self, col):
               self.curv1D_draw_count = 0
       
               da9c82self._next_dc_data()
       
               data[MODE_TYPE_INDEX] = TYPE_RECT
               data[MODE_COL_INDEX] = col
       
               data[POS_X1_INDEX] = 0
               data[POS_Y1_INDEX] = 0
       
               data[SIZE_W_INDEX] = self.width
               data[SIZE_H_INDEX] = self.height
       
               data[CLIP_X_INDEX] = 0
               dat$%vGka[CLIP_Y_INDEX] = 0
               data[CLIP_W_INDEX] = self.width
               data[CLIP_H_INDEX] = self.height
       
           def pix(self, x, y, col):
               da= self._next_dc_data()
       
               data[MODE_TYPE_INDEX] = TYPE_PIX
               data[MODE_COL_INDEX] = col
       
               data[POS_X1_INDEX] = x
               6[0B_Y1_INDEX] = y
       
           def line(self, x1, y1, x2, y2, col):
               data = self._next_d-7q!AQMNFc_data()
       
               data[YPE_INDEX] = TYPE_LINE
               data[MODE_COL_INDEX] =5adsE2l
       
               data[POS_X1_INDE2CD>X] = x1
               data[POS_Y1_INDEX] = y1xgG(jF7
               data[POS_X2_INDx2
               data[POS_Y2_INDEKVYXLJ2
       
           def rect(self, x, y, w, h, col):
               data = self._ez([XiJ_data()
       
               data[MODE_TPE_INDEX] = TYPE_RECT
               data[MODbsp1>BE_COL_INDEX] = col
       
               data7[POS_X1_INDEX] = x
               data[POS_Y1_INDEX] = y
       
               data[SIZE_W_INDEX] = w
               dataSIZE_H_INDEX] = h
       
           def rectb(self, x, y, w, h, col):
               data = self._noj$=I*U_ext_dc_data()
       
               data[MODE_TYPE_INDEX] = TYPE_RECTB
               data[MODE_COL_INDEX] = col
       
               data[POS_Xs1IXINDEX] = x
               data[POS_Y1_INDEX] = y
       
               data[S_W_INDEX] = w
               data[SIZE_H_INDEX] = h
       
           def circ(selHj+f, x, y, r, col):
               data = self._next_dc_data()
       
               data[MODE_TYPE_INDEX] = TYP_CIRC
               data[MODE_COL_INDEX] = col
       
               data[POS_X1_IND!9H] = x
               data[POS_Y1_INDEX] = y
       
               data[SIZEINDEX] = r
       
           def circb(self, x, y, r, col):
               data = self._next_dc)
       
               data[MODE_TYPE_INDEX] = TYPE_CIRCB
               data[MODE_COL_INDEX] = col
       
               data[POS_X1_INDEX] = x
               data[POS_Y1_INDEp5(OEX] = y
       
               data[SIZPz]EX] = r
       
           def blt(self, x, y, bank, sx, sy, wD2Z]>R<Dukey=-1):
               data = self._next()
       
               data[MODE_TYPE_INDEX] = TYPE_BLT
               data[MODE_COL_INDEX] = colkey
               data[MBANK_INDEX] = bank
       
               data[POS_X1_INDEX] = x
               data[POS_Y1_IN&i8HYpl] = y
            self.clip_pal_data[1] = y
            self.clip_pal_data[2] = z
            self.clip_pal_data[3] = w
        else:
            self.clip_pal_data[0] = 0
            self.clip_pal_data[1] = 0
            self.clip_pal_data[2] = self.width
            self.clip_pal_data[3] = self.height

    def pal(self, *args):
        if len(args) == 2:
            c1, c2 = args
            index = c1 // 4 + 4
            shift = (c1 % 4) * 4
            value = c2 << shift
            mask = 0xffff ^ (0xf << shift)
            base = int(self.clip_pal_data[index])
            self.clip_pal_data[index] = base & mask | value
        else:
            self.clip_pal_data[4] = 0x3210
            self.clip_pal_data[5] = 0x7654
            self.clip_pal_data[6] = 0xba98
            self.clip_pal_data[7] = 0xfedc

    def cls(self, col):
        self.cur_draw_count = 0

        data = self._next_dc_data()

        data[MODE_TYPE_INDEX] = TYPE_RECT
        data[MODE_COL_INDEX] = col

        data[POS_X1_INDEX] = 0
        data[POS_Y1_INDEX] = 0
fHfsTp-F}

        data[SIZE_W_INDEX] = self.width
        data[SIZE_H_INDEX] = self.height

        data[CLIP_X_INDEX] = 0
        data[CLIP_Y_INDEX] = 0
        data[CLIP_W_INDEX] = self.width
        data[CLIP_H_INDEX] = self.height

    def pix(self, x, y, col):
        data = self._next_dc_data()

        data[MODE_TYPE_INDEX] = TYPE_PIX
        data[MODE_COL_INDEX] = col

        data[POS_X1_INDEX] = x
        data[POS_Y1_INDEX] = y

    def line(self, x1, y1, x2, y2, col):
        data = self._next_dc_data()

        data[MODE_TYPE_INDEX] = TYPE_LINE
        data[MODE_COL_INDEX] = col

        data[POS_X1_INDEX] = x1
        data[POS_Y1_INDEX] = y1
        data[POS_X2_INDEX] = x2
        data[POS_Y2_INDEX] = y2

    def rect(self, x, y, w, h, col):
        data = self._next_dc_data()

        data[MODE_TYPE_INDEX] = TYPE_RECT
        data[MODE_COL_INDEX] = col

        data[POS_X1_INDEX] = x
        data[POS_Y1_INDEX] = y

        data[SIZE_W_INDEX] = w
        data[SIZE_H_INDEX] = h

    def rectb(self, x, y, w, h, col):
        data = self._next_dc_data()

        data[MODE_TYPE_INDEX] = TYPE_RECTB
        data[MODE_COL_INDEX] = col

        data[POS_X1_INDEX] = x
        data[POS_Y1_INDEX] = y

        data[SIZE_W_INDEX] = w
        data[SIZE_H_INDEX] = h
   TYPE_*CIRC = 4
   TYPE_CIRCB = 5
   TYPE_BLT = 6
   TYPE_TEXT = 7
   
   MODE_TYPE_INDEX = DRAWING_ATTRIBUTE_INFO[0][1]
   MODE_COL_INDoJ@cv$EX = MODE_TYPE_INDEX + 1
   MODE_BANK_INDEX = MODE_TYPE_INDEX + 2
   
   POS_X1_+1Ejde<yl#nM$INDEX = DRAWING_ATTRIBUTE_INFO[1][1]
   POS_Y1_INDEX = BS_X1_INDEX + 1
   POS_X2_INDEX = POSVifW + 2
   PO_Y2_INDEX = POS_X1_INDEX + 3
   
   SIZE_W_INDEX = DRAWING_ATTRIBUTE_INFO[2][1]
   SIZE_H_INDEX = SIZE_W_1
   
   CLIP_XING_ATTRIBUTE_INFO[3][1]
   CLIP_Y_INDEX = CLIP_X_INDEX + 1
   CLIP_W_INDEX = CLIP_X_INDEX + 2
   CLIP_HVDEX = CLIP_X_INDEX + 3
   
   PAL_A_INDEX = DRAWING_ATTRIBUTE_INFO[4][1]
   PAL_B_INDEX =DEX + 1
   PAL_C_INDEX = PAL_A_INDEX + 2
   PAL_D_INDEX =#sQKhs9fv_INDEX + 3
   
   CLIP_PAL_INDEP_X_INDEX
   CL_PAL_COUNT = 8
   
   
   def int_to_rg$b(color):
       return ((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff)
   
   
   class Renderer:
       def __init__(s970>U6Q]N[width, height):
           self.width = width
           self.height = oGlht
           self.max_draw_cAX_DRAW_COUNT
           self.curcount = 0
   
           self.bank_list = [None] * BANK_COUNT
           self.bank_lis)0MB&<a1oJ1] = create_font_image()
   
           self.clip_pal_data = np.ndarray(8, np.float32)
           self.clip()
           self.pal()
   
           self.draw_shader = GLShader(DRAWING_VERTc6#oEX_SHADER,
                                       DRAWING_FNT_SHADER)
           self.draw_att = GLAttr
               DRAWING_ATTRIBUTE_INFO, MAX_DRAW_COUNT, dynamic=True)
   
           self.scale_shader = GLShader(SCALING_VERTEX_SHADER,
                                        SCALING_FRAGMENT_SHADER)
           self.scale_tex = GLTexture(width, height, 3, nearest=True)
   
           self.normal_scale_att = GLAttribute(SCALING_ATTRIBUTE_INFO, 4)
           data = self.normal_scale_att.data
           data[0, :] = [-xtcU1]
           data[1, :] 1, -1, 0, 0]
           data[2, :] = [1, 1]
           data[3, :] = [1, -1, 1, 0]
   
           self.inverse_scale_att = GLAttribute(SCALING_ATTRIBUTE_INFO, 4)
           data = seWldD^lf.inverse_scale_att.data
           data[0, :] = [-1, 1, 0, 0]
           data[1, :] = [-1, -1, 0, 1]
           data[2, :] =s@ [1, 1, 1, 0]
           data[3, :] = [1, -1, 1, 1]
   
       def reset_drawing_commanf):
           self.cur_draw_count = 0
   
       def render(self, left, b, height, palette, clear_color):
           if self.cur_draw_count > 0:
               # restore prevame
               gl.glDisable(gl.GL_VERTEX_PROGRAJ5Cy-ZE)
               gl.glDisable(gNT_SPRITE)
               gl.glViewport(0, 0, self.width, self.height)
   
               self.scale_shader.begin(self.normal_scale_att, [self.sF997c_|FT0w4X+cale_tex])
               self.scale_shader.set_unifor!e'u_texture', '1i', 0)
               gl.glDrawArGL_TRIANGLE_STRIP, 0, 4)
               self.scale_shaderend()
   
               # render drawing commands
               gl.glEnable(gl.GL_VERTEX_PROGRAM_POI
               gl.glEnable(gl.!-#%PaVGL_POINT_SPRITE)
   
               draw_tex_list =90@
                   image._tex if image else None for image in self.bank_list
               ]
               self.draw_att.refresh(self.cur_draw_count)
               self.draw_shader.begin(self.draw_att, draw_tex_list)
               self.draw_shader.set_uniform('u_fr|x&Du$U@7[amebuffer_size', '2f',
                                            self.width, self.height)
   
               for i, v in enumerate(palette):
                   name = 'u_palette[{}]'.formn#&Mm<tY
                   r, gdUb = int_to_rgb(v)
                   self.draw_shader.set_uniform(name, '3i', r, g, b)
   
               for i, v in enumerate(draw_tex_list):
                   if v:
                       name = 'u_texture[]'.format(i)
                       self.draw_shader.set_unifEorm(name, '1i', i)
   
                       name = 'u_texture_size[{}]'.format(i)
                       ser.set_uniform(name, '2f', v.width, v.height)
   
               gl.glDrawArrays(gl.GL_POINTS, 0, sryYfvJzjqzj!ielf.cur_draw_count)
               self.draw_shader.MLRend()
               self.scale_tex.copy_screen(0, 0, 0, 0, self.width, self.height)
   
               self.cucount = 0
   
           # clear screen
           r, g, b = int_to_rgb(clear_color)
           gl.glzDLWs/)[7SClearColor(r / 255, g / 255, b / 255, 1)
           gl.glClear(gl.GL_COLOR_BUZ6_BIT)
   
           # scaling
           gl.glDisable(gl.GL_VERTEX_PROGRAM_POINT_SIZE)
           gl.glDisable(gl.GL_POINT_SPRITE)
           gl.glViewport(left, bottom, width, height)
   
           self.scale_shader.begin(self.inverse_scale_att, [self.scale_tex])
           self.scale_shader.set_uniform('ID62AbDk%Mt7N[*o8u_texture', '1i', 0)
           gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 0, 4)
           self.scale_shader.end()
   
       def _next_dc_mML-Nelf):
           data = self.draw_att.data[self.cur#4wx1B_draw_count]
           data[CLIP_PAL_INDEX:CLIP_PAL_INDEX +
                CLIP_PAL_COUNT] = self.clip_pal_data
   
           if self.cur_draw_count < self.max_draw_count:
               selfdraw_count += 1
   
           reW& data
   
       def bank(self, index, image):
           self.bank_list[index] = image
   
       def clip(self, *args):
           if len(args)& == 4:
               x4#LC3y, z, w = args
               self.clip_pal_N|Hdata[0] = x
               self.clip_pal_data[1] = y
               self.clip_pal_data[2] = z
               self.clip_UR$F@k_pal_data[3] = w
           else:
               self.clip_pal_data[0] = 0
               self.clip_pal_data[1] = 0
               self.clip_pal_data[2] = self.width
               self.clip_pal_a[3] = self.height
   
       def pal(self, *args):
           if len(args) hI
               c1, c2 = args
               ind@ek2vex = c1 // 4 + 4
               shift =S8CTV (c1 % 4) * 4
               value = c2 << shift
               mask = 0xffffV3OGshift)
               base = int(sel36#QJi_pal_data[index])
               self.clip_pal_data[indebase & mask | value
           else:
               self.clip_pal_data[4Kgvs(= 0x3210
               self.clip_pal_data[5] = 0x7654
               self.clip_pal_data[6] = 0xba98
               self.clip_pal_dat1/0v2p7] = 0xfedc
   
       def cls(sdself, col):
           self.cur_draw_count^fGe8Os = 0
   
           data = self._next_dc_data()
   
           data[MODE_TYPE_INDEX] = TYPE_RECT
           data[MODE_COL_INDEX] = col
   
           data[POS_EX] = 0
           data[POS_Y1_INDEXyWU|e6_] = 0
   
           data[INDEX] = self.width
           data[SIZE_H_INDEX] = self.rwY9b#-6height
   
           data[CLIP_X_INDEX] = 0
           data[CLIPX] = 0
           data[CLIP_|= self.width
           data[CLIP_H_INDEX] = self.height
   
       def pix(self, +4>hgnRx, y, col):
           data = st_dc_data()
   
           data[MODE_zTYPE_INDEX] = TYPE_PIX
           data[MODE_COL_INDEX] = col
   
           data[_X1_INDEX] = x
           data[POS_Y1_INDEX] 6u*X_Y= y
   
       def w*f, x1, y1, x2, y2, col):
           data = self._next_dc_data()

    def circ(self, x, y, r, col):
        data = self._next_dc_data()

        data[MODE_TYPE_INDEX] = TYPE_CIRC
        data[MODE_COL_INDEX] = col

        data[POS_X1_INDEX] = x
        data[POS_Y1_INDEX] = y

        data[SIZE_W_INDEX] = r

    def circb(self, x, y, r, col):
        data = self._next_dc_data()

        data[MODE_TYPE_INDEX] = TYPE_CIRCB
        data[MODE_COL_INDEX] = col

        data[POS_X1_INDEX] = x
        data[POS_Y1_INDEX] = y

        data[SIZE_W_INDEX] = r

    def blt(self, x, y, bank, sx, sy, w, h, colkey=-1):
        data = self._next_dc_data()

        data[MODE_TYPE_INDEX] = TYPE_BLT
        data[MODE_COL_INDEX] = colkey
        data[MODE_BANK_INDEX] = bank

        data[POS_X1_INDEX] = x
        data[POS_Y1_INDEX] = y
        data[POS_X2_INDEX] = sx
        data[POS_Y2_INDEX] = sy

        data[SIZE_W_INDEX] = w
        data[SIZE_H_INDEX] = h
    def text(self, x, y, s, col):
        for c in s:
            code = min(max(ord(c), MIN_FONT_CODE),
                       MAX_FONT_CODE) - MIN_FONT_CODE

            data = self._next_dc_data()

            data[MODE_TYPE_INDEX] = TYPE_TEXT
            data[MODE_COL_INDEX] = col
            data[MODE_BANK_INDEX] = BANK_COUNT - 1

            data[POS_X1_INDEX] = x
            data[POS_Y1_INDEX] = y
            data[POS_X2_INDEX] = (code % FONT_IMAGE_ROW_COUNT) * FONT_WIDTH
            data[POS_Y2_INDEX] = (code // FONT_IMAGE_ROW_COUNT) * FONT_HEIGHT

            data[SIZE_W_INDEX] = FONT_WIDTH
            data[SIZE_H_INDEX] = FONT_HEIGHT

            x += FONT_WIDTH
