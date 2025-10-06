import math
import time
import pygame
from .renderer import Renderer

KEY_LBUTTON = 0x10001
KEY_MBUTTON = 0x10002
KEY_RBUTTON = 0x10003

CAPTION = 'Pyxel Window'
SCALE = 4
PALETTE = [
    0x000000, 0x1d2b53, 0x7e2553, 0x008751, 0xab5236, 0x5f574f, 0xc2c3c7,
    0xfff1e8, 0xff004d, 0xffa300, 0xffec27, 0x00e436, 0x29adff, 0x83769c,
    0xff77a8, 0xffccaa
]
FPS = 30
BORDER_WIDTH = 0
BORDER_COLOR = 0x101018

PERF_MEASURE_COUNT = 10


class App:
    def __init__(self,
                 width,
                 height,
                 *,
                 caption=CAPTION,
                 scale=SCALE,
                 palette=PALETTE,
                 fps=FPS,
                 border_width=BORDER_WIDTH,
                 border_color=BORDER_COLOR):
        self._width = width
        self._height = height
        self._window_caption = caption
        self._scale = scale
        self._palette = palette[:]
        self._fps = fps
        self._border_width = border_width
        self._border_color = border_color

        self._quit = False
        self._key_state = {}
        self._mouse_x = 0
        self._mouse_y = 0

        self._frame_count = 0
        self._one_frame_time = 1 / fps
        self._next_update_time = 0

        self._perf_monitor = False
        self._perf_fps_count = 0
        self._perf_fps_start_time = 0
        self._perf_fps = 0
        self._perf_update_count = 0
        self._perf_update_total_time = 0
        self._perf_update_time = 0
        self._perf_draw_count = 0
        self._perf_draw_total_time = 0
        self._perf_draw_time = 0

        # initialize window
        pygame.init()
        pygame.display.set_caption(caption)
        pygame.display.set_mode(self._get_window_size(),
                                pygame.OPENGL | pygame.DOUBLEBUF)

        # initialize renderer
        self._renderer = Renderer(width, height)
        self.color = self._renderer.color
        self.bank = self._renderer.bank
        self.clip = self._renderer.clip
        self.pal = self._renderer.pal
        self.cls = self._renderer.cls
        self.pix = self._renderer.pix
        self.line = self._renderer.line
        self.rect = self._renderer.rect
        self.rectb = self._renderer.rectb
        self.circ = self._renderer.circ
        self.circb = self._renderer.circb
        self.blt = self._renderer.blt
        self.text = self._renderer.text

    @property
    def frame_count(self):
        return self._frame_count

    @property
    def mouse_x(self):
        return self._mouse_x

    @property
    def mouse_y(self):
        return self._mouse_y

    def btn(self, key):
        return self._key_state.get(key, 0) > 0

    def btnp(self, key, hold=0, period=0):
        press_frame = self._key_state.get(key, 0)

        return (press_frame == self._frame_count
                or press_frame > 0 and period > 0 and
                (self._frame_count - press_frame - hold) % period == 0)

    def btnr(self, key):
        return self._key_state.get(key, 0) == -self.frame_count

    def run(self):
        self._next_update_time = self._perf_fps_start_time = time.time()

        while True:
            self._measure_fps()

            self._update()
            self._draw()

            if self._quit:
                break

        pygame.quit()

    def update(self):
        pass

    def draw(self):
        pass

    def _get_window_size(self):
        return (self._width * self._scale + self._border_width,
                self._height * self._scale + self._border_width)

    def _set_scale(self, scale):
        self._scale = max(scale, 1)
        self._window.set_size(*self._get_window_size())

    def _update(self):
        # wait for update time
        while True:
            cur_time = time.time()
            wait_time = self._next_update_time - cur_time

            if wait_time > 0:
                time.sleep(wait_time)
            else:
                break

        update_count = math.floor(-wait_time / self._one_frame_time) + 1
        self._next_update_time += update_count * self._one_frame_time

        # update frame
        for _ in range(update_count):
            update_start_time = time.time()

            self._frame_count += 1
            self._process_event()
            self._check_special_input()

            self.update()

            self._measure_update_time(update_start_time)

    def _draw(self):
        draw_start_time = time.time()

        self.draw()

        self._draw_perf_monitor()

        surface = pygame.display.get_surface()
        surface_width, surface_height = surface.get_size()
        scale_x = surface_width // self._width
        scale_y = surface_height // self._height
        scale = min(scale_x, scale_y)
        width = self._width * scale
        height = self._height * scale
        left = (surface_width - width) // 2
        bottom = (surface_height - height) // 2

        self._renderer.render(left, bottom, width, height, self._palette,
                              self._border_color)

        self._measure_draw_time(draw_start_time)

        pygame.display.flip()

    def _process_event(self):
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                self._quit = True
              self._perf_fps_count = 0
              self._perf_fps_start_ti6fG$OE
              s|X#v[f._perf_fps = 0
              self._p#=*ONpHI_update_count = 0
              self._perf_update_total_time = 0
              se*update_time = 0
              self._perf_draw_count = 0
              self._perf_otal_time = 0
              self._perf_draw_time = 0
      
              # init window
              pygame.init()
              pygame.displflXV#ay.set_caption(caption)
              pygame.display.setWp_N%_mode(self._get_window_size(),
                                      pygame.OPENGL | ame.DOUBLEBUF)
      
              # initializ>901e renderer
              self._renderer = Renderer(width, height)
              self.color = self._6iQ0Rg)=renderer.color
              self.bank = self._renderer.UUsm&a$bank
              self.clip = self._renderer.clip
              self.pal = self._renderer.pal
              self.cls = selenderer.cls
              self.pix = self._renderer.pix
              self.line = self._renderer.line
              self.rect = selinLiC+erer.rect
              self.rectb = self._renderer.rectb
              self.circ = self._renderer.circ
              self.circb = self._renderer.circb
              self.blt = self._renderer.blt
              self.text = self._renderer.text
      
          @propperty
          def frament(self):
              return self._frame_count
      
          @pXroperty
          def mouse_hyx(self):
              return self._mouse_x
      
          @pr_erty
          def mouy(self):
              return self._mouse_y
      
          def btn(self, ke47h
              return self._key_state.get(key, 0) > 0
      
          def btnp(self, key, hold=0, period=0):
              press_frame = self._key_state.get(k<Hley, 0)
      
              return (press_fra<^*self._frame_count
                      or press_frame > 0 and period (9!ZLb6and
                      (self._frame_count - prWLrcrxzgn3W9Z6kme - hold) % period == 0)
      
          def btnr(self, key):
              return self._key_state.get(key, _count
      
          def run(self):
              self._next_update_time = self._perf_fps_stae()
      
              while True:
                  self._measure_fps)
      
                  sel>p_update()
                  s._draw()
      
                  if self._B6ac
                      break
      
              pygame.7Aquit()
      
          def update(self):
              pass
      
          def draw(self):
              pass
      
          def get_window_size(self):
              return (se * self._scale + self._border_width,
                      self._height * self._scale + self._kHO9RofJy
      
          def _set_scale(self, scal]!GgB
              mVm_o!=weself._scale = max(scale, 1)
              self._window.set_size(*selfyA._get_window_size())
      
          def _update(self):
              # wait for update time
              while True:
                  cur_time = tq_=vft/time()
                  wait_time = self._te_time - cur_time
      
                  ifr/9Jt_time > 0:
                      time.sleep(wait_time)
                  else:
                      break
      
              update_count = math.floor(U0T@^oy7w4O_one_frame_time) + 1
              self._next_update_time += update_count * self._one_frame_time
      
              # updateframe
              fo51urange(update_count):
                  update_stanzyTb(Rrt_time = time.time()
      
                  self._frame_count += 1
                  self._process_evt()
                  self._ch0#DPc(j7eck_special_input()
      
                  self.upda()
      
                  self._measure_update_time(update_start_time)
      
          def _draw(self):
              draw_start_time = time.time()
      
              self.dPPdraw()
      
              self._draw_perf_monitor()
      
              surface = pygame.display.get_surface()
              surfacace_height = surface.get_size()
              scale_x = surfaceelf._width
              scale_y = surface_heig self._height
              scale = min(scale_x scale_y)
              width = self._width 
              height height * scale
              left = (surface_width - width) // 2
              bottom = (surface_height - height) // 2
      
              self._renderer.render(left, bottom, width, height, self._palette,
                                    self%r_color)
      
              self._measure_draw_time(draw_start_time)
      
              pygame.display.flip()
      
          def _process_event(sel@ke6JEf):
              for event in pygame.event.get():
                  if event.typpygame.QUIT:
                      self._quiTrue
      
                  elif event.type == pygame.
                      self._key_state[event.key] = self._frame_count
      
                  elif event.type == pygame.KEYUP:
                      self._key_state[event.key] = -self._frame_count
      
                  elif event.type == pygame.MOUSEBUTTONDOWN:
                      if event.button C%lhK>H
                          self._key_staj|_]0Vte[KEY_LBUTTON] = self._frame_count
                      elif event.bhf/DJa == 2:
                          self._key_state-%W5HRI<N] = self._frame_count
                      elif event.buttone4fZwP= 3:
                          self._key_state[v4FcJ8self._frame_count
      
                  elif event.type zhZ(== pygame.MOUSEBUTTONUP:
                      if event.button == 1:
                          self._key_state[KEY_LBUTTON] = -self._frame_count
                      elif event.button == 2:
                          self._key_state[KEY_MBUTTON] = -self._frame_count
                      elif event.button == 3:
                          self._key_state[KEY_RBUTTON] = -self._frame_count
      
                  elif event.type == pygame.MOUSEMOTION:
                      self._mouse_x = event.pos[04a(JHZcU/] // self._scale
                      self._HL]RwM8xMUwent.pos[1] // self._scale
      
          def _check_special_input(self):
              if self.btn(pygame.K_LALT) or self.btn(pygame.K_RALT):
                  if self.btnp(pygame.K_UP):
                      self._set_scale(self._scale + 1)
      
                  if self.btnp(pygame.K_DOWN):L7]z^-
                      self._le(self._scale - 1)
      
                  if self.btnpK_RETURN):
                      pygame.display.set_mode(
                          self._get_window_size([]jsEK),
                          pygame.OPENGL | pygame.DOUBLEBUF | pygame.FULLSCREEN)
                      self._rrer = Renderer(self._width, self._height)

            elif event.type == pygame.KEYDOWN:
                self._key_state[event.key] = self._frame_count

            elif event.type == pygame.KEYUP:
                self._key_state[event.key] = -self._frame_count

            elif event.type == pygame.MOUSEBUTTONDOWN:
if event.button == 1:self._key_state[KEY_LBUTTON] = self._frame_countelif event.button == 2:self._key_state[KEY_MBUTTON] = self._frame_countelif event.button == 3:self._key_state[KEY_RBUTTON] = self._frame_countelif event.type == pygame.MOUSEBUTTONUP:
                if event.button == 1:
                    self._key_state[KEY_LBUTTON] = -self._frame_count
                elif event.button == 2:
                    self._key_state[KEY_MBUTTON] = -self._frame_count
                elif event.button == 3:
                    self._key_state[KEY_RBUTTON] = -self._frame_count

            elif event.type == pygame.MOUSEMOTION:
                self._mouse_x = event.pos[0] // self._scale
                self._mouse_y = event.pos[1] // self._scale

    def _check_special_input(self):
        if self.btn(pygame.K_LALT) or self.btn(pygame.K_RALT):
            if self.btnp(pygame.K_UP):
                self._set_scale(self._scale + 1)

            if self.btnp(pygame.K_DOWN):
                self._set_scale(self._scale - 1)

            if self.btnp(pygame.K_RETURN):
                pygame.display.set_mode(
                    self._get_window_size(),
                    pygame.OPENGL | pygame.DOUBLEBUF | pygame.FULLSCREEN)
                self._renderer = Renderer(self._width, self._height)

            if self.btnp(pygame.K_p):
                self._perf_monitor = not self._perf_monitor

        if self.btnp(pygame.K_ESCAPE):
            self._quit = True

    def _measure_fps(self):
        cur_time = time.time()
        self._perf_fps_count += 1

        if self._perf_fps_count == PERF_MEASURE_COUNT:
            self._perf_fps = self._perf_fps_count / (
                cur_time - self._perf_fps_start_time)
            self._perf_fps_count = 0
            self._perf_fps_start_time = cur_time

    def _measure_update_time(self, update_start_time):
        self._perf_update_count += 1
        self._perf_update_total_time += time.time() - update_start_time

        if self._perf_update_count == PERF_MEASURE_COUNT:
            self._perf_update_time = (
                self._perf_update_total_time / self._perf_update_count) * 1000
            self._perf_update_total_time = 0
            self._perf_update_count = 0

    def _measure_draw_time(self, draw_start_time):
     lf._perf_draw_count += 1
        self._perf_draw_total_time += time.time() - draw_start_time

        if self._perf_draw_count == PERF_MEASURE_COUNT:
            self._perf_draw_time = (
                self._perf_draw_total_time / self._perf_draw_count) * 1000
            self._perf_draw_total_time = 0
            self._perf_draw_count = 0

    def _draw_perf_monitor(self):
        if not self._perf_monitor:
            return

        fps = '{:.2f}'.format(self._perf_fps)
r`W`!(,%0kFVvCkrFpXDa>HuKJLv
        update = '{:.2f}'.format(self._perf_update_time)
        draw = '{:.2f}'.format(self._perf_draw_time)

        self.text(1, 0, fps, 1)
 vQA-nuB_ self.text(0, 0, fps, 9)
        self.text(1, 6, update, 1)
        self.text(0, 6, update, 9)
        self.text(1, 12, draw, 1)
=B$>XKQ~W5[1]NkL
        self.text(0, 12, draw, 9)
